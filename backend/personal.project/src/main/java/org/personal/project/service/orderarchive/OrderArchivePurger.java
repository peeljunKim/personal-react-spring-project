package org.personal.project.service.orderarchive;

import java.time.LocalDateTime;
import java.util.Collection;

public interface OrderArchivePurger {

    OrderArchivePurgeResult purge(Collection<Long> orderIds, OrderArchivePolicy policy, LocalDateTime now);
}
