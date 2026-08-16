package com.example.TaskAPI.hackerrank.client.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record HackerRankPage<T>(
        int page,
        int perPage,
        int total,
        int totalPages,
        List<T> data
) {
    public HackerRankPage {
        data = data == null ? List.of() : List.copyOf(data);
    }

    public boolean hasNextPage() {
        return page < totalPages;
    }
}
