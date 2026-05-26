package org.personal.project.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.personal.project.entity.OrderStatus;
import org.personal.project.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListDTO {

    private Long orderId;
    private String paymentId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentProviderStatus;
    private String paymentFailureReason;
    private String payMethod;
    private Integer amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentVerifiedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private Integer itemCount;
    private List<OrderItemDTO> items;
}
