package com.example.TaskAPI.core;

import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.model.BaseExtensionEntity;
import org.assertj.core.api.RecursiveComparisonAssert;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseMapperTest {
    private static final String[] IGNORE_BASE_ENTITY_FIELDS = {
            BaseEntity.Fields.id,
            BaseEntity.Fields.uuid,
            BaseEntity.Fields.createdAt,
            BaseEntity.Fields.createdBy,
            BaseEntity.Fields.updatedAt,
            BaseEntity.Fields.updatedBy,
            BaseEntity.Fields.deleted
    };

    private static final String[] IGNORE_BASE_EXTENSION_ENTITY_FIELDS = {
            BaseEntity.Fields.id,
            BaseEntity.Fields.updatedAt,
            BaseEntity.Fields.updatedBy,
            BaseEntity.Fields.deleted
    };

    protected <T> RecursiveComparisonAssert<?> assertThatMappedEntity(T actual, String... ignoreExtraFields) {
        String[] ignoreFields;

        switch (actual) {
            case BaseEntity _ -> ignoreFields = IGNORE_BASE_ENTITY_FIELDS;
            case BaseExtensionEntity _ -> ignoreFields = IGNORE_BASE_EXTENSION_ENTITY_FIELDS;
            default -> ignoreFields = new String[0];
        }

        return assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(ignoreFields)
                .ignoringFields(ignoreExtraFields);
    }
}
