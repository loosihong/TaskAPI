package com.example.TaskAPI.auth.api;

import com.example.TaskAPI.user.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public interface AuthRequest {
    @Builder
    record Register(
            @Size(max = User.Constraints.Values.USERNAME_MAX, message = User.Constraints.Messages.USERNAME_MAX)
            @NotBlank(message = User.Constraints.Messages.USERNAME_REQUIRED)
            String username,
            @Size(max = User.Constraints.Values.PASSWORD_MAX, message = User.Constraints.Messages.PASSWORD_MAX)
            @NotBlank(message = User.Constraints.Messages.PASSWORD_REQUIRED)
            String password
    ) {
    }

    @Builder
    record Login(
            @Size(max = User.Constraints.Values.USERNAME_MAX, message = User.Constraints.Messages.USERNAME_MAX)
            @NotBlank(message = User.Constraints.Messages.USERNAME_REQUIRED)
            String username,
            @Size(max = User.Constraints.Values.PASSWORD_MAX, message = User.Constraints.Messages.PASSWORD_MAX)
            @NotBlank(message = User.Constraints.Messages.PASSWORD_REQUIRED)
            String password
    ) {
    }
}

