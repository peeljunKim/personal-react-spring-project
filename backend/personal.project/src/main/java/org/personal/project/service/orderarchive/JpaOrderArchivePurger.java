package org.personal.project.service.orderarchive;

import lombok.RequiredArgsConstructor;
import org.personal.project.entity.Order;
import org.personal.project.repository.OrderItemRepository;
import org.personal.project.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JpaOrderArchivePurger implements OrderArchivePurger {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public OrderArchivePurgeResult purge(Collection<Long> orderIds, OrderArchivePolicy policy, LocalDateTime now) {
        if (orderIds.isEmpty()) {
            return new OrderArchivePurgeResult(0, List.of(), List.of());
        }

        List<Order> lockedOrders = orderRepository.findAllByOnoInForUpdate(orderIds);
        Set<Long> requestedOrderIds = new HashSet<>(orderIds);
        List<Long> purgeableOrderIds = lockedOrders.stream()
                .filter(order -> policy.isEligible(order, now))
                .map(Order::getOno)
                .toList();
        Set<Long> purgeableOrderIdSet = new HashSet<>(purgeableOrderIds);
        List<Long> skippedOrderIds = requestedOrderIds.stream()
                .filter(orderId -> !purgeableOrderIdSet.contains(orderId))
                .toList();

        if (purgeableOrderIds.isEmpty()) {
            return new OrderArchivePurgeResult(orderIds.size(), List.of(), skippedOrderIds);
        }

        orderItemRepository.deleteByOrderIds(purgeableOrderIds);
        int deletedOrders = orderRepository.deleteByOnoIn(purgeableOrderIds);
        if (deletedOrders != purgeableOrderIds.size()) {
            throw new IllegalStateException("아카이브 주문 삭제 건수가 일치하지 않습니다. expected="
                    + purgeableOrderIds.size() + ", actual=" + deletedOrders);
        }

        long remainingOrders = orderRepository.countByOnoIn(purgeableOrderIds);
        if (remainingOrders > 0) {
            throw new IllegalStateException("아카이브 주문 삭제 후 메인 DB에 잔여 데이터가 남아 있습니다. remaining="
                    + remainingOrders);
        }

        return new OrderArchivePurgeResult(orderIds.size(), purgeableOrderIds, skippedOrderIds);
    }
}
