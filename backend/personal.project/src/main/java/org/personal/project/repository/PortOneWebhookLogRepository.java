package org.personal.project.repository;

import org.personal.project.entity.pg.PortOneWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortOneWebhookLogRepository extends JpaRepository<PortOneWebhookLog, Long> {

    boolean existsByWebhookId(String webhookId);
}
