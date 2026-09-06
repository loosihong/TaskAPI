-- liquibase formatted sql

-- changeset loosi:1788691474209-1 splitStatements:false
CREATE TABLE jobrunr_task.jobrunr_recurring_jobs (id nchar(128) NOT NULL, version int NOT NULL, jobAsJson nvarchar(MAX), createdAt bigint CONSTRAINT DF__jobrunr_r__creat__0F624AF8 DEFAULT 0 NOT NULL, CONSTRAINT PK__jobrunr___3213E83F65F3AFF7 PRIMARY KEY (id));

-- changeset loosi:1788691474209-2 splitStatements:false
CREATE TABLE jobrunr_task.jobrunr_backgroundjobservers (id nchar(36) NOT NULL, workerPoolSize int NOT NULL, pollIntervalInSeconds int NOT NULL, firstHeartbeat datetime2 NOT NULL, lastHeartbeat datetime2 NOT NULL, running int NOT NULL, systemTotalMemory bigint NOT NULL, systemFreeMemory bigint NOT NULL, systemCpuLoad decimal(3, 2) NOT NULL, processMaxMemory bigint NOT NULL, processFreeMemory bigint NOT NULL, processAllocatedMemory bigint NOT NULL, processCpuLoad decimal(3, 2) NOT NULL, deleteSucceededJobsAfter varchar(32), permanentlyDeleteJobsAfter varchar(32), name varchar(128), CONSTRAINT PK__jobrunr___3213E83F1F6A2B61 PRIMARY KEY (id));

-- changeset loosi:1788691474209-3 splitStatements:false
CREATE TABLE jobrunr_task.jobrunr_jobs (id nchar(36) NOT NULL, version int NOT NULL, jobAsJson nvarchar(MAX), jobSignature nvarchar(512), state varchar(36) NOT NULL, createdAt datetime2 NOT NULL, updatedAt datetime2 NOT NULL, scheduledAt datetime2, recurringJobId varchar(128), CONSTRAINT PK__jobrunr___3213E83F13AE468A PRIMARY KEY (id));

-- changeset loosi:1788691474209-4 splitStatements:false
CREATE TABLE jobrunr_task.jobrunr_metadata (id varchar(156) NOT NULL, name varchar(92) NOT NULL, owner varchar(64) NOT NULL, value nvarchar(MAX), createdAt datetime2 NOT NULL, updatedAt datetime2 NOT NULL, CONSTRAINT PK__jobrunr___3213E83F75E35E90 PRIMARY KEY (id));

-- changeset loosi:1788691474209-5 splitStatements:false
CREATE TABLE jobrunr_task.jobrunr_migrations (id nchar(36) NOT NULL, script varchar(64) NOT NULL, installedOn varchar(29) NOT NULL, CONSTRAINT PK__jobrunr___3213E83F0D0ED481 PRIMARY KEY (id));

-- changeset loosi:1788691474209-6 splitStatements:false
CREATE VIEW jobrunr_task.jobrunr_jobs_stats AS with job_stat_results AS (SELECT state, count(*) AS count
    FROM jobrunr_task.jobrunr_jobs
    GROUP BY state
)
SELECT coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results), 0)                            AS total,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'AWAITING'), 0)   AS awaiting,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'SCHEDULED'), 0)  AS scheduled,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'ENQUEUED'), 0)   AS enqueued,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'PROCESSING'), 0) AS processing,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'PROCESSED'), 0)  AS processed,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'FAILED'), 0)     AS failed,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'SUCCEEDED'), 0)  AS succeeded,
       coalesce((SELECT cASt(cASt(value AS char(10)) AS decimal(10, 0))
                 FROM jobrunr_task.jobrunr_metadata jm
                 WHERE jm.id = 'succeeded-jobs-counter-cluster'),
                0)                                                                                        AS allTimeSucceeded,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'DELETED'), 0)    AS deleted,
       (SELECT count(*) FROM jobrunr_task.jobrunr_backgroundjobservers)                                                AS nbrOfBackgroundJobServers,
       (SELECT count(*) FROM jobrunr_task.jobrunr_recurring_jobs)                                                      AS nbrOfRecurringJobs;

-- changeset loosi:1788691474209-7 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_recurring_job_created_at_idx ON jobrunr_task.jobrunr_recurring_jobs(createdAt);

-- changeset loosi:1788691474209-8 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_bgjobsrvrs_fsthb_idx ON jobrunr_task.jobrunr_backgroundjobservers(firstHeartbeat);

-- changeset loosi:1788691474209-9 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_bgjobsrvrs_lsthb_idx ON jobrunr_task.jobrunr_backgroundjobservers(lastHeartbeat);

-- changeset loosi:1788691474209-10 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_job_created_at_idx ON jobrunr_task.jobrunr_jobs(createdAt);

-- changeset loosi:1788691474209-11 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_job_rci_idx ON jobrunr_task.jobrunr_jobs(recurringJobId);

-- changeset loosi:1788691474209-12 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_job_scheduled_at_idx ON jobrunr_task.jobrunr_jobs(scheduledAt);

-- changeset loosi:1788691474209-13 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_job_signature_idx ON jobrunr_task.jobrunr_jobs(jobSignature);

-- changeset loosi:1788691474209-14 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_jobs_state_updated_idx ON jobrunr_task.jobrunr_jobs(state, updatedAt);

-- changeset loosi:1788691474209-15 splitStatements:false
CREATE NONCLUSTERED INDEX jobrunr_state_idx ON jobrunr_task.jobrunr_jobs(state);

