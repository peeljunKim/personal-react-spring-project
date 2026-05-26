package org.personal.project.service.payment;

import org.personal.project.properties.PortOnePaymentProperties;
import org.personal.project.dto.payment.PaymentPrepareResponse;
import org.personal.project.entity.CartItem;
import org.personal.project.entity.Member;
import org.personal.project.entity.Order;
import org.personal.project.entity.OrderItem;
import org.personal.project.entity.pg.PaymentRequest;
import org.personal.project.entity.pg.Trade;
import org.personal.project.repository.CartItemRepository;
import org.personal.project.repository.MemberRepository;
import org.personal.project.repository.OrderRepository;
import org.personal.project.repository.PaymentRequestRepository;
import org.personal.project.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 결제 준비 시 주문/상품 스냅샷 생성
 * <p>
 * 상품명이나 가격을 수정할 수 있습니다. 만약 스냅샷을 찍지 않으면 아래 와 같은 문제가 발생
 * <p>
 * 상황: 사용자가 '신발'을 50,000원에 결제했는데 그 직후 관리자가 상품 가격을 60,000원으로 올렸습니다
 * <p>
 * 문제: 나중에 사용자가 주문 내역을 봤을 때 본인은 50,000원에 샀는데 화면에는 현재 가격인 60,000원이 뜨거나
 * 데이터 무결성을 지킬 수 있음
 */
@Service
public class PaymentPrepareService {

    private static final String CURRENCY_KRW = "CURRENCY_KRW";

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TradeRepository tradeRepository;
    private final PaymentLockExecutor lockExecutor;
    private final PortOnePaymentProperties properties;
    private final PortOnePayMethodResolver payMethodResolver;
    private final TransactionTemplate transactionTemplate;

    public PaymentPrepareService(
            CartItemRepository cartItemRepository,
            MemberRepository memberRepository,
            OrderRepository orderRepository,
            PaymentRequestRepository paymentRequestRepository,
            TradeRepository tradeRepository,
            PaymentLockExecutor lockExecutor,
            PortOnePaymentProperties properties,
            PortOnePayMethodResolver payMethodResolver,
            PlatformTransactionManager transactionManager
    ) {
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.tradeRepository = tradeRepository;
        this.lockExecutor = lockExecutor;
        this.properties = properties;
        this.payMethodResolver = payMethodResolver;
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

        String payMethod = resolvePayMethod();
        Order order = orderRepository.save(Order.ready(member, totalAmount, payMethod));
        String paymentId = "order-" + order.getOno() + "-" + UUID.randomUUID();
        order.assignPaymentId(paymentId);
        PaymentRequest paymentRequest = paymentRequestRepository.save(PaymentRequest.create(order.getOno(), totalAmount));
        tradeRepository.save(Trade.create(paymentRequest, paymentId));

        for (CartItem cartItem : cartItems) {
            order.addItem(OrderItem.snapshot(cartItem.getProduct(), cartItem.getQty()));
        }

        return PaymentPrepareResponse.builder()
                .orderId(order.getOno())
                .paymentId(paymentId)
                .orderName(buildOrderName(cartItems))
                .totalAmount(totalAmount)
                .currency(CURRENCY_KRW)
                .payMethod(payMethod)
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
        if (!StringUtils.hasText(resolvePayMethod())) {
            throw new PaymentException("PORTONE_PAY_METHOD 환경 변수가 설정되어 있지 않습니다.");
        }
    }

    private String resolvePayMethod() {
        return payMethodResolver.resolve();
    }
}
