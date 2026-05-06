package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_pg_errors")
@Getter
@NoArgsConstructor
public class PgError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @Lob // TEXT 타입
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}