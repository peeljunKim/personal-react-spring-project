package org.personal.project.repository.search;

import org.personal.project.dto.order.OrderDetailDTO;
import org.personal.project.dto.order.OrderListDTO;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;

import java.util.Optional;

public interface OrderSearch {

    PageResponseDTO<OrderListDTO> searchOrders(String email, PageRequestDTO pageRequestDTO);

    Optional<OrderDetailDTO> findOrderDetail(String email, Long orderId);

    Optional<OrderDetailDTO> findPaymentDetail(String email, String paymentId);
}
