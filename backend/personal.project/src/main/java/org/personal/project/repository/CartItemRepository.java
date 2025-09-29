package org.personal.project.repository;

import org.personal.project.dto.cart.CartItemListDTO;
import org.personal.project.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 특정 사용자(이메일)의 모든 장바구니 아이템들을 가져옴
     * input: email
     * output: CartItemListDTO - jpa 프로젝션으로 해결
     */
    @Query("select " +
            " new org.personal.project.dto.cart.CartItemListDTO(ci.cino, ci.qty, p.pno, p.pname, p.price , pi.fileName ) " +
            " from " +
            " CartItem ci inner join Cart mc on ci.cart = mc " +
            " left join Product p on ci.product = p " +
            " left join p.imageList pi" + // product에 imageList은 엘리먼트 컬렉션이라서 조인 방식이 약간 다름
            " where " +
            " mc.owner.email = :email " +
            " and pi.ord = 0 " +
            " order by ci.cino desc ")
    public List<CartItemListDTO> getItemsOfCartDTOByEmail(@Param("email") String email);

    // 이메일, 상품 번호로 해당 상품이 장바구니(CartItem)에 존재하는지 확인
    @Query("select" +
            " ci " +
            " from " +
            " CartItem ci inner join Cart c on ci.cart = c " +
            " where " +
            " c.owner.email = :email and ci.product.pno = :pno")
    public CartItem getItemOfPno(@Param("email") String email, @Param("pno") Long pno);


    // 장바구니(CartItem) 아이템 번호로 카트 번호 확인
    @Query("select " +
            " c.cno " +
            " from " +
            " Cart c inner join CartItem ci on ci.cart = c " +
            " where " +
            " ci.cino = :cino")
    public Long getCartFromItem(@Param("cino") Long cino);

    // 장바구니(Cart) 번호로 모든 장바구니 조회
    @Query("select " +
            " new org.personal.project.dto.cart.CartItemListDTO(ci.cino, ci.qty, p.pno, p.pname, p.price , pi.fileName ) " +
            " from " +
            " CartItem ci inner join Cart mc on ci.cart = mc " +
            " left join Product p on ci.product = p " +
            " left join p.imageList pi" +
            " where " +
            " mc.cno = :cno and pi.ord = 0 " +
            " order by ci desc ")
    public List<CartItemListDTO> getItemsOfCartDTOByCart(@Param("cno") Long cno);
}