package org.personal.project.service;

import lombok.RequiredArgsConstructor;
import org.personal.project.dto.order.OrderDetailDTO;
import org.personal.project.dto.order.OrderListDTO;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public PageResponseDTO<OrderListDTO> getOrders(String email, PageRequestDTO pageRequestDTO) {
        return orderRepository.searchOrders(email, pageRequestDTO);
    }

    @Override
    public OrderDetailDTO getOrder(String email, Long orderId) {
        return orderRepository.findOrderDetail(email, orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
    }

    @Override
    public OrderDetailDTO getPayment(String email, String paymentId) {
        return orderRepository.findPaymentDetail(email, paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 내역을 찾을 수 없습니다."));
    }
}
