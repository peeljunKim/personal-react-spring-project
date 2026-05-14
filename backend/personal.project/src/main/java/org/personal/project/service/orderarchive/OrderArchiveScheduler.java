package org.personal.project.service.orderarchive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.properties.OrderArchiveProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderArchiveScheduler {

    private final OrderArchiveService orderArchiveService;
    private final OrderArchiveProperties properties;

    @Scheduled(cron = "${order.archive.cron:0 0 3 * * *}", zone = "${order.archive.zone-id:Asia/Seoul}")
    public void archiveAbandonedOrders() {
        if (!properties.isEnabled()) {
            log.debug("주문 이탈 데이터 아카이브 배치가 비활성화되어 있습니다.");
            return;
        }
        orderArchiveService.archiveEligibleOrders();
    }
}
