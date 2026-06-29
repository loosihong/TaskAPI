package com.example.TaskAPI.core.dto;

import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
public record PageableRequest(
        String sortBy,
        Sort.Direction sortDirection,
        int pageNumber,
        int pageSize
) {
    public PageableRequest {
        if (sortBy == null) sortBy = "";
        if (pageNumber < 0) pageNumber = 0;
        if (pageSize <= 0) pageSize = 20;
        if (pageSize > 100) pageSize = 100;
    }

    public Pageable toPageable() {
        return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
    }

    public Pageable toPageable(Sort.Direction defaultSortDirection, String defaultSortBy) {
        return PageRequest.of(pageNumber, pageSize, Sort.by(
                sortDirection == null ? defaultSortDirection : sortDirection,
                sortBy.isBlank() ? defaultSortBy : sortBy));
    }

    public static class PageableRequestBuilder {
        public PageableRequest build() {
            return new PageableRequest(
                    sortBy,
                    sortDirection,
                    pageNumber,
                    pageSize
            );
        }
    }
}
