package org.personal.project.entity;

import jakarta.persistence.*;
import lombok.*;

// 장바구니
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "owner")
@Table(
        name = "tbl_cart",
        indexes = {@Index(name = "idx_cart_email", columnList = "member_owner")}
)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cno;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_owner")
    private Member owner;
}