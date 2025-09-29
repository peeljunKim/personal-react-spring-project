package org.personal.project.service;

import org.personal.project.dto.cart.CartItemDTO;
import org.personal.project.dto.cart.CartItemListDTO;

import java.util.List;

public interface CartService {

    //장바구니 아이템 추가 혹은 변경
    public List<CartItemListDTO> addOrModify(CartItemDTO cartItemDTO);

    //모든 장바구니 아이템 목록
    public List<CartItemListDTO> getCartItems(String email);

    //아이템 삭제
    public List<CartItemListDTO> remove(Long cino);

}
