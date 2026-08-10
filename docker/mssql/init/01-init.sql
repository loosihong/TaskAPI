IF NOT EXISTS (
   SELECT name
   FROM sys.databases
   WHERE name = '$(DB_NAME)'
)
BEGIN
    EXEC ('CREATE DATABASE [$(DB_NAME)]');
END
GO

IF EXISTS (
    SELECT name
   FROM sys.databases
   WHERE name = '$(DB_NAME)'
        AND is_read_committed_snapshot_on = 0
)
BEGIN
    EXEC ('ALTER DATABASE [$(DB_NAME)] SET READ_COMMITTED_SNAPSHOT ON WITH ROLLBACK IMMEDIATE');
END
GO

IF NOT EXISTS (
    SELECT name
    FROM sys.server_principals
    WHERE name = '$(APP_USER)'
)
BEGIN
    EXEC ('CREATE LOGIN [$(APP_USER)] WITH PASSWORD = ''$(APP_PASSWORD)''');
END
GO

DECLARE @CreateUserSql NVARCHAR(MAX);
SET @CreateUserSql =
        N'USE [$(DB_NAME)];' +
        N'IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = ''$(APP_USER)'')' +
        N'BEGIN' +
        N'  CREATE USER [$(APP_USER)] FOR LOGIN [$(APP_USER)];' +
        N'  ALTER ROLE db_owner ADD MEMBER [$(APP_USER)];' +
        N'END';

EXEC sp_executesql @CreateUserSql;
GO

DECLARE @CreateSchemaSql NVARCHAR(MAX);
SET @CreateSchemaSql =
        N'USE [$(DB_NAME)];' +
        N'IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = ''jobrunr_task'')' +
        N'BEGIN' +
        N'  EXEC(''CREATE SCHEMA jobrunr_task AUTHORIZATION dbo'');' +
        N'END';

EXEC sp_executesql @CreateSchemaSql;
GO