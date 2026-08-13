package com.example.TaskAPI.core.audit;

import com.example.TaskAPI.core.audit.annotation.AuditableField;
import com.example.TaskAPI.core.model.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionAuditListenerTest {
    private static final Long AUDITOR_ID = 49L;

    private ReflectionAuditListener auditListener;
    private List<Object> publishedEvents;

    @BeforeEach
    void setUp() {
        publishedEvents = new ArrayList<>();
        auditListener = new ReflectionAuditListener();
        auditListener.init(publishedEvents::add, () -> Optional.of(AUDITOR_ID));
    }

    private AuditEntry singlePublishedEntry() {
        assertThat(publishedEvents).singleElement().isInstanceOf(AuditEntry.class);

        return (AuditEntry) publishedEvents.getFirst();
    }

    @Test
    void inheritedAuditableField_isAudited() {
        TestEntity entity = new TestEntity();
        entity.inheritedField = "before";
        entity.captureSnapshot();

        entity.inheritedField = "after";
        auditListener.onPreUpdate(entity);

        assertThat(singlePublishedEntry().fieldDiffs())
                .singleElement()
                .satisfies(diff -> {
                    assertThat(diff.fieldName()).isEqualTo("inheritedField");
                    assertThat(diff.oldValue()).isEqualTo("before");
                    assertThat(diff.newValue()).isEqualTo("after");
                });
    }

    @Test
    void unchangedField_publishesNothing() {
        TestEntity entity = new TestEntity();
        entity.title = "same title";
        entity.captureSnapshot();

        auditListener.onPreUpdate(entity);

        assertThat(publishedEvents).isEmpty();
    }

    @Test
    void fieldWithoutAnnotation_isIgnored() {
        TestEntity entity = new TestEntity();
        entity.captureSnapshot();
        entity.untracked = "changed";

        auditListener.onPreUpdate(entity);

        assertThat(publishedEvents).isEmpty();
    }

    @Test
    void displayName_overridesFieldName() {
        TestEntity entity = new TestEntity();
        entity.status = TestStatus.TODO;
        entity.captureSnapshot();
        entity.status = TestStatus.DONE;

        auditListener.onPreUpdate(entity);

        assertThat(singlePublishedEntry().fieldDiffs())
                .singleElement()
                .satisfies(diff -> assertThat(diff.fieldName()).isEqualTo("Task Status"));
    }

    @Test
    void nullOldValue_isStringifiedNotSkipped() {
        TestEntity entity = new TestEntity();
        entity.captureSnapshot();
        entity.title = "first value";

        auditListener.onPreUpdate(entity);

        assertThat(singlePublishedEntry().fieldDiffs())
                .singleElement()
                .satisfies(diff -> assertThat(diff.oldValue()).isEqualTo("null"));
    }

    @Test
    void absentAuditor_yieldsNullAuditorId() {
        auditListener.init(publishedEvents::add, Optional::empty);

        TestEntity entity = new TestEntity();
        entity.captureSnapshot();

        entity.title = "new";
        auditListener.onPreUpdate(entity);

        assertThat(singlePublishedEntry().auditorId()).isNull();
    }

    @Test
    void remove_publishesDeletionEntry() {
        TestEntity entity = new TestEntity();

        auditListener.onPreRemove(entity);

        AuditEntry entry = singlePublishedEntry();
        assertThat(entry.entityName()).isEqualTo(TestEntity.class.getSimpleName());
        assertThat(entry.entityUUID()).isEqualTo(entity.getUuid());
        assertThat(entry.auditorId()).isEqualTo(AUDITOR_ID);
        assertThat(entry.fieldDiffs())
                .singleElement()
                .satisfies(diff -> {
                    assertThat(diff.fieldName()).isEqualTo("deleted");
                    assertThat(diff.oldValue()).isEqualTo("false");
                    assertThat(diff.newValue()).isEqualTo("true");
                });
    }

    @Test
    void remove_nonAuditableEntity_publishesNothing() {
        auditListener.onPreRemove(new NonAuditableEntity());

        assertThat(publishedEvents).isEmpty();
    }

    private enum TestStatus {TODO, DONE}

    private abstract static class AbstractTestEntity extends BaseEntity {
        @AuditableField
        protected String inheritedField;
    }

    private static class TestEntity extends AbstractTestEntity implements Auditable {
        @AuditableField
        private String title;
        @AuditableField(displayName = "Task Status")
        private TestStatus status;

        private String untracked;

        TestEntity() {
            this.uuid = UUID.randomUUID();
        }

        void captureSnapshot() {
            autoSnapshot();
        }
    }

    private static class NonAuditableEntity extends BaseEntity {
        @AuditableField
        private String title;
    }
}
