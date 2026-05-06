package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 수단 테이블
 */
@Entity
@Table(name = "tbl_payment_method")
@Getter
@NoArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Long methodId;      // 결제 수단 id

    @Column(length = 50, nullable = false)
    private String name;        // 결제 수단명 (카드 등)
}