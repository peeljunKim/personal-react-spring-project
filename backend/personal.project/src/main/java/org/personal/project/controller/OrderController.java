package org.personal.project.controller;

import lombok.RequiredArgsConstructor;
import org.personal.project.dto.order.OrderDetailDTO;
import org.personal.project.dto.order.OrderListDTO;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.service.OrderQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping
    public PageResponseDTO<OrderListDTO> getOrders(PageRequestDTO pageRequestDTO, Principal principal) {
        return orderQueryService.getOrders(principal.getName(), pageRequestDTO);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/{orderId}")
    public OrderDetailDTO getOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        return orderQueryService.getOrder(principal.getName(), orderId);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/payment/{paymentId}")
    public OrderDetailDTO getPayment(@PathVariable("paymentId") String paymentId, Principal principal) {
        return orderQueryService.getPayment(principal.getName(), paymentId);
    }
}
