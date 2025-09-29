package org.personal.project.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemListDTO {

    private Long cino;
    private int qty;
    private Long pno;
    private String pname;
    private int price;
    private String imageFile;

}