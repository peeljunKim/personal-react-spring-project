package org.personal.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_order_item", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Getter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false)
    private Integer lineAmount;

    public static OrderItem snapshot(Product product, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
        }

        OrderItem item = new OrderItem();
        item.product = product;
        item.productName = product.getPname();
        item.price = product.getPrice();
        item.qty = qty;
        item.lineAmount = Math.multiplyExact(product.getPrice(), qty);
        return item;
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}
