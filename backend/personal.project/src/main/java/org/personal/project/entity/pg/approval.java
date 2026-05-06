package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카드 승인 결과 테이블
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "tbl_approval")
public class approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Column(length = 100, nullable = false)
    private String tid; // PG가 전달한 거래 고유 번호

    @Column(name = "card_number", length = 30)
    private String cardNumber;

    @Column(nullable = false)
    private Integer amount;  // 승인 금액

    @Column(name = "result_code", length = 10, nullable = false)
    private String resultCode;  // 결과 코드

    @Column(name = "requested_at")
    private LocalDateTime requestedAt; // 승인 요청 수신 시각

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;  // 승인 처리 완료 시각


    /**
     * 이건 결과 코드 확인해야 됨
     */
    public boolean isSuccess() {
        return "0000".equals(this.resultCode);
    }
}
