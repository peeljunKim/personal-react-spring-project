package org.personal.project.service.payment;

import org.personal.project.properties.PortOnePaymentProperties;
import org.personal.project.dto.payment.PaymentPrepareResponse;
import org.personal.project.entity.CartItem;
import org.personal.project.entity.Member;
import org.personal.project.entity.Order;
import org.personal.project.entity.OrderItem;
import org.personal.project.repository.CartItemRepository;
import org.personal.project.repository.MemberRepository;
import org.personal.project.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentPrepareService {

    private static final String CURRENCY_KRW = "CURRENCY_KRW";
    private static final String PAY_METHOD_CARD = "EASY_PAY";

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentLockExecutor lockExecutor;
    private final PortOnePaymentProperties properties;
    private final TransactionTemplate transactionTemplate;

    public PaymentPrepareService(
            CartItemRepository cartItemRepository,
            MemberRepository memberRepository,
            OrderRepository orderRepository,
            PaymentLockExecutor lockExecutor,
            PortOnePaymentProperties properties,
            PlatformTransactionManager transactionManager
    ) {
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.lockExecutor = lockExecutor;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public PaymentPrepareResponse prepare(String email) {
        return lockExecutor.execute(
                List.of("payment:prepare:member:" + email),
                3000,
                10000,
                () -> transactionTemplate.execute(status -> prepareWithTransaction(email))
        );
    }

    private PaymentPrepareResponse prepareWithTransaction(String email) {
        assertClientPaymentConfig();

        List<CartItem> cartItems = cartItemRepository.findItemsForCheckout(email);
        if (cartItems.isEmpty()) {
            throw new PaymentException("결제할 장바구니 상품이 없습니다.");
        }

        int totalAmount = calculateAndValidateAmount(cartItems);
        Member member = memberRepository.getReferenceById(email);

        Order order = orderRepository.save(Order.ready(member, totalAmount));
        String paymentId = "order-" + order.getOno() + "-" + UUID.randomUUID();
        order.assignPaymentId(paymentId);

        for (CartItem cartItem : cartItems) {
            order.addItem(OrderItem.snapshot(cartItem.getProduct(), cartItem.getQty()));
        }

        return PaymentPrepareResponse.builder()
                .orderId(order.getOno())
                .paymentId(paymentId)
                .orderName(buildOrderName(cartItems))
                .totalAmount(totalAmount)
                .currency(CURRENCY_KRW)
                .payMethod(PAY_METHOD_CARD)
                .storeId(properties.getStoreId())
                .channelKey(properties.getChannelKey())
                .noticeUrl(properties.getWebhookUrl())
                .build();
    }

    private int calculateAndValidateAmount(List<CartItem> cartItems) {
        long total = 0L;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getQty() <= 0) {
                throw new PaymentException("결제 수량이 올바르지 않습니다.");
            }
            if (cartItem.getProduct().isDelFlag()) {
                throw new PaymentException("삭제된 상품은 결제할 수 없습니다. productNo=" + cartItem.getProduct().getPno());
            }
            if (cartItem.getProduct().getStock() < cartItem.getQty()) {
                throw new PaymentException("상품 재고가 부족합니다. productNo=" + cartItem.getProduct().getPno());
            }
            total += Math.multiplyExact((long) cartItem.getProduct().getPrice(), cartItem.getQty());
            if (total > Integer.MAX_VALUE) {
                throw new PaymentException("주문 금액이 허용 범위를 초과했습니다.");
            }
        }
        return (int) total;
    }

    private String buildOrderName(List<CartItem> cartItems) {
        String firstName = cartItems.get(0).getProduct().getPname();
        if (cartItems.size() == 1) {
            return firstName;
        }
        return firstName + " 외 " + (cartItems.size() - 1) + "건";
    }

    private void assertClientPaymentConfig() {
        if (!StringUtils.hasText(properties.getStoreId())) {
            throw new PaymentException("PORTONE_STORE_ID 환경 변수가 설정되어 있지 않습니다.");
        }
        if (!StringUtils.hasText(properties.getChannelKey())) {
            throw new PaymentException("PORTONE_CHANNEL_KEY 환경 변수가 설정되어 있지 않습니다.");
        }
    }
}
