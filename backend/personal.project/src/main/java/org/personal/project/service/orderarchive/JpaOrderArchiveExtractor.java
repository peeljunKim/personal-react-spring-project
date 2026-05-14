package org.personal.project.service.orderarchive;

import lombok.RequiredArgsConstructor;
import org.personal.project.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaOrderArchiveExtractor implements OrderArchiveExtractor {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderArchiveBatch extract(OrderArchiveCriteria criteria) {
        List<OrderArchiveCandidate> candidates = orderRepository.findArchiveCandidates(
                criteria.lastOrderId(),
                criteria.archivableStatuses(),
                criteria.defaultPayMethod(),
                criteria.immediatePayMethods(),
                criteria.delayedPayMethods(),
                criteria.knownPayMethods(),
                criteria.immediateCutoff(),
                criteria.delayedCutoff(),
                criteria.unknownCutoff(),
                PageRequest.ofSize(criteria.pageSize())
        );

        if (candidates.isEmpty()) {
            return new OrderArchiveBatch(List.of(), List.of());
        }

        List<OrderArchiveRecord> records = orderRepository.findArchiveRecordsByOrderIds(
                candidates.stream().map(OrderArchiveCandidate::orderId).toList()
        );
        return new OrderArchiveBatch(candidates, records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderArchiveCandidate> findCurrentCandidates(Collection<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findArchiveCandidatesByOrderIds(orderIds);
    }
}
