package org.personal.project.dto.page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;


public final class OffsetLimitPageRequest implements Pageable, Serializable {

    private final int pageIndex;
    private final int requestedSize;
    private final int limit;
    private final Sort sort;

    private OffsetLimitPageRequest(int pageIndex, int requestedSize, Sort sort) {
        if (pageIndex < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (requestedSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        this.pageIndex = pageIndex;
        this.requestedSize = requestedSize;
        this.limit = requestedSize + 1;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    public static OffsetLimitPageRequest of(int pageIndex, int requestedSize, Sort sort) {
        return new OffsetLimitPageRequest(pageIndex, requestedSize, sort);
    }

    public int getRequestedSize() {
        return requestedSize;
    }

    @Override
    public int getPageNumber() {
        return pageIndex;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return (long) pageIndex * requestedSize;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetLimitPageRequest(pageIndex + 1, requestedSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetLimitPageRequest(pageIndex - 1, requestedSize, sort) : this;
    }

    @Override
    public Pageable first() {
        return new OffsetLimitPageRequest(0, requestedSize, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetLimitPageRequest(pageNumber, requestedSize, sort);
    }

    @Override
    public boolean hasPrevious() {
        return pageIndex > 0;
    }
}