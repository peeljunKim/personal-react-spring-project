package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가맹점 정보 테이블
 */
@Entity
@Table(name = "tbl_merchant")
@Getter
@NoArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "webhook_url", length = 255)
    private String webhookUrl;

    @Column(name = "api_key", length = 255, nullable = false)
    private String apiKey;
}