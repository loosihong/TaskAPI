package com.example.TaskAPI.task.domain.repository;

import com.example.TaskAPI.task.api.dto.TaskDashboardFilter;
import com.example.TaskAPI.task.api.dto.TaskDashboardItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepositoryDashboard {
    Page<TaskDashboardItem> findTaskDashboardItems(
            TaskDashboardFilter taskDashboardFilter,
            Pageable pageable,
            Long userId
    );
}
