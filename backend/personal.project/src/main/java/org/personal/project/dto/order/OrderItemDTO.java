package org.personal.project.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private Integer price;
    private Integer qty;
    private Integer lineAmount;
    private String thumbnailFileName;
}
