package org.personal.project.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.payment.PaymentSyncResponse;
import org.personal.project.entity.Order;
import org.personal.project.entity.OrderItem;
import org.personal.project.entity.OrderStatus;
import org.personal.project.entity.Product;
import org.personal.project.entity.pg.TradeStatus;
import org.personal.project.exception.PaymentException;
import org.personal.project.exception.PaymentVerificationException;
import org.personal.project.repository.OrderItemRepository;
import org.personal.project.repository.OrderRepository;
import org.personal.project.repository.ProductRepository;
import org.personal.project.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * PortOne 조회 후 DB 주문 금액 비교, 불일치/재고 부족 시 즉시 취소 후 롤백
 */
@Service
@Slf4j
public class PaymentSynchronizer {

    private static final String CANCEL_REASON_AMOUNT_MISMATCH = "ORDER_AMOUNT_MISMATCH";
    private static final String CANCEL_REASON_STOCK_SHORTAGE = "STOCK_SHORTAGE";
    private static final String CANCEL_REASON_PROVIDER_REJECTED = "PROVIDER_REJECTED";

    private final PortOnePaymentClient portOnePaymentClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final TradeRepository tradeRepository;
    private final PaymentLockExecutor lockExecutor;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public PaymentSynchronizer(
            PortOnePaymentClient portOnePaymentClient,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            TradeRepository tradeRepository,
            PaymentLockExecutor lockExecutor,
            PlatformTransactionManager transactionManager
    ) {
        this.portOnePaymentClient = portOnePaymentClient;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.tradeRepository = tradeRepository;
        this.lockExecutor = lockExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public PaymentSyncResponse synchronize(String paymentId) {
        return lockExecutor.execute(
                List.of("payment:sync:" + paymentId),
                3000,
                15000,
                () -> synchronizeWithPaymentLock(paymentId)
        );
    }

    private PaymentSyncResponse synchronizeWithPaymentLock(String paymentId) {
        PortOnePayment payment = portOnePaymentClient.getPayment(paymentId);

        if (!payment.isPaid()) {
            return transactionTemplate.execute(status -> applyNonPaidStatus(paymentId, payment));
        }

        List<String> productLockKeys = orderItemRepository.findProductIdsByPaymentId(paymentId).stream()
                .map(productId -> "stock:product:" + productId)
                .toList();

        return lockExecutor.execute(
                productLockKeys,
                3000,
                15000,
                () -> {
                    try {
                        return transactionTemplate.execute(status -> applyPaidStatus(paymentId, payment));
                    } catch (PaymentVerificationException e) {
                        markOrderCancelledAfterRollback(paymentId, payment.status(), e.getMessage());
                        return PaymentSyncResponse.builder()
                                .paymentId(paymentId)
                                .paymentStatus(payment.status())
                                .orderStatus(OrderStatus.CANCEL.name())
                                .message(e.getMessage())
                                .build();
                    }
                }
        );
    }

    private PaymentSyncResponse applyNonPaidStatus(String paymentId, PortOnePayment payment) {
        Order order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("주문을 찾을 수 없습니다. paymentId=" + paymentId));

        if (payment.isFailedOrCancelled()) {
            order.markPaymentFailed();
            tradeRepository.findByTid(paymentId)
                    .filter(trade -> trade.getStatus() != TradeStatus.APPROVED)
                    .ifPresent(trade -> trade.fail(payment.status(), CANCEL_REASON_PROVIDER_REJECTED));
        } else {
            order.markPaymentPending();
            tradeRepository.findByTid(paymentId)
                    .ifPresent(trade -> trade.markProviderPending(payment.status()));
        }

        return PaymentSyncResponse.builder()
                .paymentId(paymentId)
                .paymentStatus(payment.status())
                .orderStatus(order.getStatus().name())
                .message("PAID 상태가 아니므로 주문 완료 처리를 보류했습니다.")
                .build();
    }

    private PaymentSyncResponse applyPaidStatus(String paymentId, PortOnePayment payment) {
        Order order = orderRepository.findByPaymentIdWithItems(paymentId)
                .orElseThrow(() -> new PaymentException("주문을 찾을 수 없습니다. paymentId=" + paymentId));

        if (order.getStatus() == OrderStatus.PAID) {
            return PaymentSyncResponse.builder()
                    .paymentId(paymentId)
                    .paymentStatus(payment.status())
                    .orderStatus(order.getStatus().name())
                    .message("이미 결제 완료 처리된 주문입니다.")
                    .build();
        }

        if (order.getStatus() == OrderStatus.CANCEL) {
            return PaymentSyncResponse.builder()
                    .paymentId(paymentId)
                    .paymentStatus(payment.status())
                    .orderStatus(order.getStatus().name())
                    .message("이미 취소 처리된 주문입니다.")
                    .build();
        }

        if (!order.getAmount().equals(payment.totalAmount())) {
            cancelAndRollback(paymentId, CANCEL_REASON_AMOUNT_MISMATCH);
        }

        decreaseStock(order);
        order.markPaid();
        tradeRepository.findByTid(paymentId)
                .ifPresent(trade -> trade.approve(payment.status()));

        return PaymentSyncResponse.builder()
                .paymentId(paymentId)
                .paymentStatus(payment.status())
                .orderStatus(order.getStatus().name())
                .message("결제 금액 검증 및 재고 차감이 완료되었습니다.")
                .build();
    }

    private void decreaseStock(Order order) {
        List<String> shortageProductIds = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getPno())
                    .orElseThrow(() -> new PaymentException("상품을 찾을 수 없습니다. productNo=" + item.getProduct().getPno()));

            if (product.isDelFlag() || product.getStock() < item.getQty()) {
                shortageProductIds.add(String.valueOf(product.getPno()));
                continue;
            }

            product.decreaseStock(item.getQty());
        }

        if (!shortageProductIds.isEmpty()) {
            cancelAndRollback(order.getPaymentId(), CANCEL_REASON_STOCK_SHORTAGE + ":" + String.join(",", shortageProductIds));
        }
    }

    private void cancelAndRollback(String paymentId, String reason) {
        portOnePaymentClient.cancelPayment(paymentId, reason);
        throw new PaymentVerificationException("포트원 결제 취소 후 DB 트랜잭션을 롤백했습니다. reason=" + reason);
    }

    private void markOrderCancelledAfterRollback(String paymentId, String providerStatus, String failureReason) {
        requiresNewTransactionTemplate.execute(status -> {
            orderRepository.findByPaymentId(paymentId)
                    .ifPresent(Order::markPaymentFailed);
            tradeRepository.findByTid(paymentId)
                    .filter(trade -> trade.getStatus() != TradeStatus.APPROVED)
                    .ifPresent(trade -> trade.fail(providerStatus, failureReason));
            return null;
        });
    }
}
