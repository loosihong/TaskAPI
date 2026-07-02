package com.example.TaskAPI.user.domain.repository;

import com.example.TaskAPI.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByUuidIn(Collection<UUID> uuids);
}
