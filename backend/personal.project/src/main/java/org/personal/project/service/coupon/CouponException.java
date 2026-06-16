package org.personal.project.service.coupon;

/**
 * 쿠폰 도메인 예외
 */
public class CouponException extends RuntimeException {

    public CouponException(String message) {
        super(message);
    }

    public CouponException(String message, Throwable cause) {
        super(message, cause);
    }
}
