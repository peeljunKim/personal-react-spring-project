package org.personal.project.dto.payment;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 결제 준비 요청 값
 */
@Data
public class PaymentPrepareRequest {

    // 적용할 사용자 쿠폰 ID
    @Positive
    private Long memberCouponId;
}
