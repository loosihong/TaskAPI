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
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public PageableRequest {
        if (sortBy == null) {
            sortBy = "";
        }

        if (pageNumber < 0) {
            pageNumber = 0;
        }

        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }

    public Pageable toPageable(Sort.Direction defaultSortDirection, String defaultSortBy) {
        return PageRequest.of(pageNumber, pageSize, Sort.by(
                sortDirection == null ? defaultSortDirection : sortDirection,
                sortBy.isBlank() ? defaultSortBy : sortBy));
    }
}
