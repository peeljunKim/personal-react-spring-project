package org.personal.project.service.payment;

import org.personal.project.exception.PaymentException;
import org.personal.project.properties.PortOnePaymentProperties;
import org.personal.project.dto.payment.PaymentPrepareResponse;
import org.personal.project.entity.CartItem;
import org.personal.project.entity.Member;
import org.personal.project.entity.Order;
import org.personal.project.entity.OrderItem;
import org.personal.project.entity.coupon.DiscountSourceType;
import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.OrderCoupon;
import org.personal.project.entity.coupon.OrderCouponStatus;
import org.personal.project.entity.coupon.OrderDiscount;
import org.personal.project.entity.pg.PaymentRequest;
import org.personal.project.entity.pg.Trade;
import org.personal.project.repository.CartItemRepository;
import org.personal.project.repository.MemberRepository;
import org.personal.project.repository.OrderRepository;
import org.personal.project.repository.PaymentRequestRepository;
import org.personal.project.repository.TradeRepository;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.personal.project.repository.coupon.OrderCouponRepository;
import org.personal.project.repository.coupon.OrderDiscountRepository;
import org.personal.project.service.coupon.CouponAppliedDiscount;
import org.personal.project.service.coupon.CouponApplyService;
import org.personal.project.service.coupon.CouponReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 결제 준비 처리
 * <p>
 * 장바구니 기준 주문/상품 스냅샷을 만들고, 선택 쿠폰이 있으면 결제 요청 전에 쿠폰을 예약합니다.
 * 상품명/가격은 주문 시점 값으로 고정해야 하므로 OrderItem 스냅샷으로 저장합니다.
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
    private final CouponApplyService couponApplyService;
    private final MemberCouponRepository memberCouponRepository;
    private final OrderCouponRepository orderCouponRepository;
    private final OrderDiscountRepository orderDiscountRepository;
    private final CouponReservationService couponReservationService;
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
            CouponApplyService couponApplyService,
            MemberCouponRepository memberCouponRepository,
            OrderCouponRepository orderCouponRepository,
            OrderDiscountRepository orderDiscountRepository,
            CouponReservationService couponReservationService,
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
        this.couponApplyService = couponApplyService;
        this.memberCouponRepository = memberCouponRepository;
        this.orderCouponRepository = orderCouponRepository;
        this.orderDiscountRepository = orderDiscountRepository;
        this.couponReservationService = couponReservationService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public PaymentPrepareResponse prepare(String email) {
        return prepare(email, null);
    }

    /**
     * 결제 준비 유스케이스
     */
    public PaymentPrepareResponse prepare(String email, Long memberCouponId) {
        return lockExecutor.execute(
                List.of("payment:prepare:member:" + email),
                3000,
                10000,
                () -> transactionTemplate.execute(status -> prepareWithTransaction(email, memberCouponId))
        );
    }

    /**
     * 결제 준비 트랜잭션 처리
     * <p>
     * 쿠폰 할인 금액을 먼저 계산한 뒤 주문/결제 요청 금액에는 할인 후 금액을 저장합니다.
     * 쿠폰 예약, OrderCoupon, OrderDiscount 저장이 실패하면 주문 생성도 함께 롤백됩니다.
     */
    private PaymentPrepareResponse prepareWithTransaction(String email, Long memberCouponId) {
        assertClientPaymentConfig();

        List<CartItem> cartItems = cartItemRepository.findItemsForCheckout(email);
        if (cartItems.isEmpty()) {
            throw new PaymentException("결제할 장바구니 상품이 없습니다.");
        }

        int originalAmount = calculateAndValidateAmount(cartItems);
        CouponAppliedDiscount couponDiscount = null;
        int payableAmount = originalAmount;
        if (memberCouponId != null) {
            couponDiscount = couponApplyService.calculateForPayment(email, memberCouponId, cartItems);
            payableAmount = couponDiscount.payableAmount();
        }

        Member member = memberRepository.getReferenceById(email);

        String payMethod = resolvePayMethod();
        Order order = orderRepository.save(Order.ready(member, payableAmount, payMethod));
        String paymentId = "order-" + order.getOno() + "-" + UUID.randomUUID();
        order.assignPaymentId(paymentId);

        for (CartItem cartItem : cartItems) {
            order.addItem(OrderItem.snapshot(cartItem.getProduct(), cartItem.getQty()));
        }

        if (couponDiscount != null) {
            createCouponSnapshot(order, couponDiscount);
        }

        PaymentRequest paymentRequest = paymentRequestRepository.save(PaymentRequest.create(order.getOno(), payableAmount));
        tradeRepository.save(Trade.create(paymentRequest, paymentId));

        if (couponDiscount != null) {
            reserveCouponInRedis(email, order, couponDiscount);
        }

        return PaymentPrepareResponse.builder()
                .orderId(order.getOno())
                .paymentId(paymentId)
                .orderName(buildOrderName(cartItems))
                .totalAmount(payableAmount)
                .originalAmount(originalAmount)
                .discountAmount(couponDiscount == null ? 0 : couponDiscount.discountAmount())
                .payableAmount(payableAmount)
                .currency(CURRENCY_KRW)
                .payMethod(payMethod)
                .storeId(properties.getStoreId())
                .channelKey(properties.getChannelKey())
                .noticeUrl(properties.getWebhookUrl())
                .build();
    }

    /**
     * 주문 쿠폰 할인 스냅샷 저장
     * <p>
     * MemberCoupon 상태는 변경하지 않고, OrderCoupon은 이 주문에 적용된 쿠폰 할인 내역을 남깁니다.
     */
    private void createCouponSnapshot(Order order, CouponAppliedDiscount couponDiscount) {
        MemberCoupon memberCoupon = couponDiscount.memberCoupon();
        Order orderReference = orderRepository.getReferenceById(order.getOno());
        MemberCoupon memberCouponReference = memberCouponRepository.getReferenceById(memberCoupon.getMemberCouponId());
        orderCouponRepository.save(OrderCoupon.builder()
                .order(orderReference)
                .memberCoupon(memberCouponReference)
                .discountAmount(couponDiscount.discountAmount())
                .status(OrderCouponStatus.RESERVED)
                .build());
        orderDiscountRepository.save(OrderDiscount.builder()
                .order(orderReference)
                .sourceType(DiscountSourceType.COUPON)
                .sourceId(memberCoupon.getMemberCouponId())
                .discountAmount(couponDiscount.discountAmount())
                .appliedOrder(1)
                .description("쿠폰 할인: " + memberCoupon.getPolicy().getName())
                .build());
    }

    /**
     * Redis 쿠폰 임시 예약 생성
     */
    private void reserveCouponInRedis(String email, Order order, CouponAppliedDiscount couponDiscount) {
        couponReservationService.reserve(
                couponDiscount.memberCoupon().getMemberCouponId(),
                email,
                order.getOno(),
                order.getPaymentId(),
                order.getPayMethod()
        );
    }

    /**
     * 장바구니 원 주문 금액 계산
     */
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

    /**
     * 결제창 표시 주문명 생성
     */
    private String buildOrderName(List<CartItem> cartItems) {
        String firstName = cartItems.get(0).getProduct().getPname();
        if (cartItems.size() == 1) {
            return firstName;
        }
        return firstName + " 외 " + (cartItems.size() - 1) + "건";
    }

    /**
     * 포트원 클라이언트 설정 검증
     */
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

    /**
     * 결제 수단 결정
     */
    private String resolvePayMethod() {
        return payMethodResolver.resolve();
    }
}
