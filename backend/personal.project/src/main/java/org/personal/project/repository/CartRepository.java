package org.personal.project.repository;

import org.personal.project.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("select cart from Cart cart where cart.owner.email = :email")
    public Optional<Cart> getCartOfMember(@Param("email") String email);
}
