package com.example.TaskAPI.core.dto;

import com.example.TaskAPI.core.model.BaseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

public class PageableRequestTest {
    @ParameterizedTest(name = "[{0}, {1}] -> [{2}, {3}]")
    @CsvSource({
            "0, 0, 0, 20",
            "-1, -5, 0, 20",
            "2, 50, 2, 50",
            "0, 101, 0, 100"
    })
    void normalisesPagingValues(int pageNumber, int pageSize, int expectedNumber, int expectedSize) {
        PageableRequest request = PageableRequest.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();

        assertThat(request.pageNumber()).isEqualTo(expectedNumber);
        assertThat(request.pageSize()).isEqualTo(expectedSize);
    }

    @Test
    void toPageable_usesDefault_whenSortUnspecified() {
        Pageable pageable = PageableRequest.builder().build()
                .toPageable(Sort.Direction.DESC, BaseEntity.Fields.createdAt);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor(BaseEntity.Fields.createdAt))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void toPageable_prefersRequestValues_overDefaults() {
        Pageable pageable = PageableRequest.builder()
                .sortBy(BaseEntity.Fields.updatedBy)
                .sortDirection(Sort.Direction.ASC)
                .pageNumber(2)
                .pageSize(50)
                .build()
                .toPageable(Sort.Direction.DESC, BaseEntity.Fields.createdAt);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor(BaseEntity.Fields.createdAt)).isNull();
        assertThat(pageable.getSort().getOrderFor(BaseEntity.Fields.updatedBy))
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }
}
