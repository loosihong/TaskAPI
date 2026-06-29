package com.example.TaskAPI.core.model.repository;

import com.example.TaskAPI.core.model.BaseEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BaseEntityRepository<T extends BaseEntity> extends JpaRepository<T, Long> {
    Optional<T> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Transactional
    void deleteByUuid(UUID uuid);
}
