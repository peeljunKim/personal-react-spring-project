package org.personal.project.service.orderarchive;

import java.util.Collection;
import java.util.List;

public interface OrderArchiveExtractor {

    OrderArchiveBatch extract(OrderArchiveCriteria criteria);

    List<OrderArchiveCandidate> findCurrentCandidates(Collection<Long> orderIds);
}
