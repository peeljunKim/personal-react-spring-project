package org.personal.project.repository;

import org.personal.project.entity.Order;
import org.personal.project.entity.OrderStatus;
import org.personal.project.repository.search.OrderSearch;
import org.personal.project.service.orderarchive.OrderArchiveCandidate;
import org.personal.project.service.orderarchive.OrderArchiveRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderSearch {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("""
            select distinct o
            from Order o
            left join fetch o.items oi
            left join fetch oi.product
            where o.paymentId = :paymentId
            """)
    Optional<Order> findByPaymentIdWithItems(@Param("paymentId") String paymentId);

    @Query("""
            select new org.personal.project.service.orderarchive.OrderArchiveCandidate(
                o.ono,
                o.paymentId,
                o.status,
                o.payMethod,
                o.createdAt
            )
            from Order o
            where (:lastOrderId is null or o.ono > :lastOrderId)
              and o.status in :statuses
              and (
                  (upper(coalesce(o.payMethod, :defaultPayMethod)) in :immediateMethods and o.createdAt <= :immediateCutoff)
                  or (upper(coalesce(o.payMethod, :defaultPayMethod)) in :delayedMethods and o.createdAt <= :delayedCutoff)
                  or (upper(coalesce(o.payMethod, :defaultPayMethod)) not in :knownMethods and o.createdAt <= :unknownCutoff)
              )
            order by o.ono asc
            """)
    List<OrderArchiveCandidate> findArchiveCandidates(
            @Param("lastOrderId") Long lastOrderId,
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("defaultPayMethod") String defaultPayMethod,
            @Param("immediateMethods") Set<String> immediateMethods,
            @Param("delayedMethods") Set<String> delayedMethods,
            @Param("knownMethods") Set<String> knownMethods,
            @Param("immediateCutoff") LocalDateTime immediateCutoff,
            @Param("delayedCutoff") LocalDateTime delayedCutoff,
            @Param("unknownCutoff") LocalDateTime unknownCutoff,
            Pageable pageable
    );

    @Query("""
            select new org.personal.project.service.orderarchive.OrderArchiveCandidate(
                o.ono,
                o.paymentId,
                o.status,
                o.payMethod,
                o.createdAt
            )
            from Order o
            where o.ono in :orderIds
            """)
    List<OrderArchiveCandidate> findArchiveCandidatesByOrderIds(@Param("orderIds") Collection<Long> orderIds);

    @Query("""
            select new org.personal.project.service.orderarchive.OrderArchiveRecord(
                o.ono,
                o.paymentId,
                o.status,
                o.paymentStatus,
                t.providerStatus,
                t.failureReason,
                o.payMethod,
                o.amount,
                o.createdAt,
                o.updatedAt,
                t.verifiedAt,
                o.paidAt,
                o.cancelledAt,
                o.member.email,
                oi.oino,
                p.pno,
                oi.productName,
                oi.price,
                oi.qty,
                oi.lineAmount
            )
            from Order o
            left join Trade t on t.tid = o.paymentId
            left join o.items oi
            left join oi.product p
            where o.ono in :orderIds
            order by o.ono asc, oi.oino asc
            """)
    List<OrderArchiveRecord> findArchiveRecordsByOrderIds(@Param("orderIds") Collection<Long> orderIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.ono in :orderIds")
    List<Order> findAllByOnoInForUpdate(@Param("orderIds") Collection<Long> orderIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Order o where o.ono in :orderIds")
    int deleteByOnoIn(@Param("orderIds") Collection<Long> orderIds);

    long countByOnoIn(Collection<Long> orderIds);
}
