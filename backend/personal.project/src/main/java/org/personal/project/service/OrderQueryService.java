package org.personal.project.service;

import org.personal.project.dto.order.OrderDetailDTO;
import org.personal.project.dto.order.OrderListDTO;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;

public interface OrderQueryService {

    PageResponseDTO<OrderListDTO> getOrders(String email, PageRequestDTO pageRequestDTO);

    OrderDetailDTO getOrder(String email, Long orderId);

    OrderDetailDTO getPayment(String email, String paymentId);
}
