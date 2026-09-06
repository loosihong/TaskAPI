-- liquibase formatted sql

-- changeset loosi:1788691173738-1 splitStatements:false
CREATE TABLE audit_field_log (audit_log_id bigint, id bigint IDENTITY (1, 1) NOT NULL, field_name varchar(63) NOT NULL, new_value nvarchar(MAX), old_value nvarchar(MAX), CONSTRAINT PK__audit_fi__3213E83F4C109388 PRIMARY KEY (id));

-- changeset loosi:1788691173738-2 splitStatements:false
CREATE TABLE audit_log (created_at datetime2 NOT NULL, created_by bigint, id bigint IDENTITY (1, 1) NOT NULL, entity_uuid uniqueidentifier NOT NULL, entity_name varchar(63) NOT NULL, CONSTRAINT PK__audit_lo__3213E83F3924AA0A PRIMARY KEY (id));

-- changeset loosi:1788691173738-3 splitStatements:false
CREATE TABLE [user] (is_deleted bit, version int, created_at datetime2 NOT NULL, created_by bigint, id bigint IDENTITY (1, 1) NOT NULL, updated_at datetime2 NOT NULL, updated_by bigint, uuid uniqueidentifier NOT NULL, password varchar(127) NOT NULL, username varchar(127) NOT NULL, CONSTRAINT PK__user__3213E83F6D44A40A PRIMARY KEY (id));

-- changeset loosi:1788691173738-4 splitStatements:false
ALTER TABLE [user] ADD CONSTRAINT UK5c856itaihtmi69ni04cmpc4m UNIQUE (username);

-- changeset loosi:1788691173738-5 splitStatements:false
ALTER TABLE [user] ADD CONSTRAINT UKic90joc3tdi0f3ggnoi3l4hse UNIQUE (uuid);

-- changeset loosi:1788691173738-6 splitStatements:false
ALTER TABLE audit_field_log ADD CONSTRAINT FKahsycx1ojbvmbl4yumcuwewy FOREIGN KEY (audit_log_id) REFERENCES audit_log (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

