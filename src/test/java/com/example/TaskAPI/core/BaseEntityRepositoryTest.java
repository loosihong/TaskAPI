package com.example.TaskAPI.core;

import com.example.TaskAPI.core.audit.AuditFieldLog;
import com.example.TaskAPI.core.audit.AuditLog;
import com.example.TaskAPI.core.audit.AuditLogRepository;
import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.model.repository.BaseEntityRepository;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public abstract class BaseEntityRepositoryTest<T extends BaseEntity> extends BaseRepositoryTest {
    @Autowired
    private AuditLogRepository auditLogRepository;

    protected void assertCreationData(T entity) {
        assertThat(entity.getVersion()).isEqualTo(0L);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedBy()).isNotNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(entity.getUpdatedBy()).isEqualTo(entity.getCreatedBy());
        assertThat(entity.isDeleted()).isFalse();
    }

    protected void assertVersionConflict(T entity, BaseEntityRepository<T> repository) {
        ReflectionTestUtils.setField(entity, BaseEntity.Fields.version, 99);

        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    protected void assertSoftDeleteById(T entity, BaseEntityRepository<T> repository) {
        repository.deleteById(entity.getId());
        repository.flush();
        entityManager.clear();

        assertThat(repository.findById(entity.getId())).isEmpty();

        Object isDeleted = entityManager.getEntityManager().createNativeQuery(
                        "SELECT is_deleted FROM "
                                + getNativeHibernateTableName(entity.getClass())
                                + " WHERE id = :id")
                .setParameter("id", entity.getId())
                .getSingleResult();

        assertThat((Boolean) isDeleted).isTrue();
    }

    protected void assertSoftDeleteByUuid(T entity, BaseEntityRepository<T> repository) {
        repository.deleteByUuid(entity.getUuid());
        repository.flush();
        entityManager.clear();

        assertThat(repository.findByUuid(entity.getUuid())).isEmpty();

        Object isDeleted = entityManager.getEntityManager().createNativeQuery(
                        "SELECT is_deleted FROM "
                                + getNativeHibernateTableName(entity.getClass())
                                + " WHERE uuid = :uuid")
                .setParameter("uuid", entity.getUuid())
                .getSingleResult();

        assertThat((Boolean) isDeleted).isTrue();
    }

    protected void assertAuditableField(
            T entity, BaseEntityRepository<T> repository, List<Pair<String, Object>> updateFieldValuePairs) {
        if (CollectionUtils.isEmpty(updateFieldValuePairs)) {
            fail("updateFieldValuePairs is empty");
            return;
        }

        List<String> oldValues = new ArrayList<>();

        entity = repository.findById(entity.getId()).get();

        for (Pair<String, Object> fieldValuePair : updateFieldValuePairs) {
            oldValues.add(ReflectionTestUtils.getField(entity, fieldValuePair.getFirst()).toString());
            ReflectionTestUtils.setField(entity, fieldValuePair.getFirst(), fieldValuePair.getSecond());
        }

        repository.saveAndFlush(entity);

        List<AuditLog> auditLogs = auditLogRepository.findByEntityUuid(entity.getUuid());

        assertThat(auditLogs).isNotEmpty();

        if (!CollectionUtils.isEmpty(auditLogs)) {
            List<AuditFieldLog> auditFieldLogs = auditLogs.getLast().getAuditFieldLogs();

            assertThat(auditFieldLogs).isNotEmpty();

            for (int i = 0; i < updateFieldValuePairs.size(); i++) {
                Pair<String, Object> fieldValuePair = updateFieldValuePairs.get(i);
                int finalI = i;

                assertThat(auditFieldLogs)
                        .anySatisfy(fieldLog -> {
                            assertThat(fieldLog.getFieldName()).isEqualTo(fieldValuePair.getFirst());
                            assertThat(fieldLog.getOldValue()).isEqualTo(oldValues.get(finalI));
                            assertThat(fieldLog.getNewValue()).isEqualTo(fieldValuePair.getSecond());
                        });
            }
        }
    }

    protected void assertNonAuditableField(
            T entity, BaseEntityRepository<T> repository, List<Pair<String, Object>> updateFieldValuePairs) {
        if (CollectionUtils.isEmpty(updateFieldValuePairs)) {
            fail("updateFieldValuePairs is empty");
            return;
        }

        long countBefore = auditLogRepository.count();

        entity = repository.findById(entity.getId()).get();

        for (Pair<String, Object> updateFieldValuePair : updateFieldValuePairs) {
            ReflectionTestUtils.setField(entity, updateFieldValuePair.getFirst(), updateFieldValuePair.getSecond());
        }

        repository.saveAndFlush(entity);

        assertThat(auditLogRepository.count()).isEqualTo(countBefore);
    }

    private String getNativeHibernateTableName(Class<?> entityClass) {
        Session session = entityManager.getEntityManager().unwrap(Session.class);
        SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) session.getSessionFactory();
        EntityPersister persister = sessionFactory
                .getRuntimeMetamodels()
                .getMappingMetamodel()
                .getEntityDescriptor(entityClass);

        return persister.getTableName();
    }
}
