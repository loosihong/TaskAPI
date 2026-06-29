package com.example.TaskAPI.user.domain.entity;

import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.validation.ValidationError;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = User.Reference.TABLE_NAME)
public class User extends BaseEntity {
    @Size(max = Constraints.Values.USERNAME_MAX)
    @Column(unique = true, nullable = false)
    private String username;

    @Size(max = Constraints.Values.PASSWORD_MAX)
    @Column(nullable = false)
    private String password;

    public static final class Reference {
        public static final String TABLE_NAME = "[user]";
        public static final String IDENTIFIER_NAME = "user_id";

        private Reference() {
        }
    }

    public static final class Constraints {
        private Constraints() {
        }

        public static final class Values {
            public static final int USERNAME_MAX = 127;
            public static final int PASSWORD_MAX = 127;

            private Values() {
            }
        }

        public static final class Messages {
            public static final String USERNAME_REQUIRED = "Username" + ValidationError.REQUIRED;
            public static final String USERNAME_MAX = "Username" + ValidationError.MAX_LENGTH;
            public static final String PASSWORD_REQUIRED = "Password" + ValidationError.REQUIRED;
            public static final String PASSWORD_MAX = "Password" + ValidationError.MAX_LENGTH;

            private Messages() {
            }
        }
    }
}