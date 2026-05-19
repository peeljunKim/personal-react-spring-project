package org.personal.project.repository.search;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import org.personal.project.dto.order.OrderDetailDTO;
import org.personal.project.dto.order.OrderItemDTO;
import org.personal.project.dto.order.OrderListDTO;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.entity.Order;
import org.personal.project.entity.QOrder;
import org.personal.project.entity.QOrderItem;
import org.personal.project.entity.QProduct;
import org.personal.project.entity.QProductImage;
import org.personal.project.util.ImageFileNameUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderSearchImpl extends QuerydslRepositorySupport implements OrderSearch {

    private static final int PAGE_SIZE = 10;  // 주문 목록은 한 페이지당 10건으로 고정

    public OrderSearchImpl() {
        super(Order.class);
    }

    @Override
    public PageResponseDTO<OrderListDTO> searchOrders(String email, PageRequestDTO pageRequestDTO) {
        PageRequestDTO safeRequest = normalizePageRequest(pageRequestDTO);
        List<Order> orders = fetchOrderWindow(email, safeRequest);
        boolean hasNext = orders.size() > PAGE_SIZE;
        List<Order> currentOrders = hasNext ? orders.subList(0, PAGE_SIZE) : orders;
        List<Long> orderIds = currentOrders.stream()
                .map(Order::getOno)
                .toList();
        Map<Long, List<OrderItemDTO>> itemMap = fetchItemMap(orderIds);

        List<OrderListDTO> dtoList = currentOrders.stream()
                .map(order -> toListDTO(order, itemMap.getOrDefault(order.getOno(), List.of())))
                .toList();

        Long nextCursorId = null;
        LocalDateTime nextCursorCreatedAt = null;
        if (hasNext && !currentOrders.isEmpty()) {
            Order lastOrder = currentOrders.get(currentOrders.size() - 1);
            nextCursorId = lastOrder.getOno();
            nextCursorCreatedAt = lastOrder.getCreatedAt();
        }

        return PageResponseDTO.<OrderListDTO>withCursor()
                .dtoList(dtoList)
                .pageRequestDTO(safeRequest)
                .hasNextPage(hasNext)
                .nextCursorId(nextCursorId)
                .nextCursorCreatedAt(nextCursorCreatedAt)
                .build();
    }

    @Override
    public Optional<OrderDetailDTO> findOrderDetail(String email, Long orderId) {
        QOrder order = QOrder.order;
        Order foundOrder = from(order)
                .where(order.member.email.eq(email), order.ono.eq(orderId))
                .fetchOne();

        return toDetailDTO(foundOrder);
    }

    @Override
    public Optional<OrderDetailDTO> findPaymentDetail(String email, String paymentId) {
        QOrder order = QOrder.order;
        Order foundOrder = from(order)
                .where(order.member.email.eq(email), order.paymentId.eq(paymentId))
                .fetchOne();

        return toDetailDTO(foundOrder);
    }

    private List<Order> fetchOrderWindow(String email, PageRequestDTO pageRequestDTO) {
        QOrder order = QOrder.order;
        int pageIndex = pageRequestDTO.hasCursor() ? 0 : pageRequestDTO.getPage() - 1;
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE + 1);

        JPQLQuery<Order> query = from(order)
                .where(order.member.email.eq(email), cursorCondition(pageRequestDTO, order))
                .orderBy(order.createdAt.desc(), order.ono.desc());

        getQuerydsl().applyPagination(pageable, query);
        return query.fetch();
    }

    private BooleanExpression cursorCondition(PageRequestDTO pageRequestDTO, QOrder order) {
        if (!pageRequestDTO.hasCursor()) {
            return null;
        }
        return order.createdAt.lt(pageRequestDTO.getCursorCreatedAt())
                .or(order.createdAt.eq(pageRequestDTO.getCursorCreatedAt())
                        .and(order.ono.lt(pageRequestDTO.getCursorId())));
    }

    private Map<Long, List<OrderItemDTO>> fetchItemMap(List<Long> orderIds) {
        Map<Long, List<OrderItemDTO>> itemMap = new LinkedHashMap<>();
        orderIds.forEach(orderId -> itemMap.put(orderId, new ArrayList<>()));

        if (orderIds.isEmpty()) {
            return itemMap;
        }

        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        JPQLQuery<Tuple> query = from(orderItem)
                .leftJoin(orderItem.product, product)
                .leftJoin(product.imageList, productImage).on(productImage.ord.eq(0))
                .select(
                        orderItem.order.ono,
                        orderItem.oino,
                        product.pno,
                        orderItem.productName,
                        orderItem.price,
                        orderItem.qty,
                        orderItem.lineAmount,
                        productImage.fileName
                )
                .where(orderItem.order.ono.in(orderIds))
                .orderBy(orderItem.order.ono.asc(), orderItem.oino.asc());

        for (Tuple row : query.fetch()) {
            Long orderId = row.get(orderItem.order.ono);
            itemMap.computeIfAbsent(orderId, ignored -> new ArrayList<>())
                    .add(toItemDTO(row, orderItem, product, productImage));
        }

        return itemMap;
    }

    private Optional<OrderDetailDTO> toDetailDTO(Order order) {
        if (order == null) {
            return Optional.empty();
        }
        List<OrderItemDTO> items = fetchItemMap(List.of(order.getOno()))
                .getOrDefault(order.getOno(), List.of());

        return Optional.of(OrderDetailDTO.builder()
                .orderId(order.getOno())
                .paymentId(order.getPaymentId())
                .status(order.getStatus())
                .payMethod(order.getPayMethod())
                .amount(order.getAmount())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .cancelledAt(order.getCancelledAt())
                .itemCount(items.size())
                .items(items)
                .build());
    }

    private OrderListDTO toListDTO(Order order, List<OrderItemDTO> items) {
        return OrderListDTO.builder()
                .orderId(order.getOno())
                .paymentId(order.getPaymentId())
                .status(order.getStatus())
                .payMethod(order.getPayMethod())
                .amount(order.getAmount())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .cancelledAt(order.getCancelledAt())
                .itemCount(items.size())
                .items(items)
                .build();
    }

    private OrderItemDTO toItemDTO(Tuple row, QOrderItem orderItem, QProduct product, QProductImage productImage) {
        return OrderItemDTO.builder()
                .orderItemId(row.get(orderItem.oino))
                .productId(row.get(product.pno))
                .productName(row.get(orderItem.productName))
                .price(row.get(orderItem.price))
                .qty(row.get(orderItem.qty))
                .lineAmount(row.get(orderItem.lineAmount))
                .thumbnailFileName(ImageFileNameUtil.toThumbnailFileName(row.get(productImage.fileName)))
                .build();
    }

    private PageRequestDTO normalizePageRequest(PageRequestDTO pageRequestDTO) {
        PageRequestDTO safeRequest = pageRequestDTO == null ? new PageRequestDTO() : pageRequestDTO;
        safeRequest.setPage(Math.max(safeRequest.getPage(), 1));
        safeRequest.setSize(PAGE_SIZE);
        safeRequest.setCount(false);
        return safeRequest;
    }
}
