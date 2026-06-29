IF NOT EXISTS (SELECT name
               FROM sys.databases
               WHERE name = '$(DB_NAME)')
    BEGIN
        EXEC ('CREATE DATABASE [$(DB_NAME)]');
    END
GO

IF NOT EXISTS (SELECT name
               FROM sys.server_principals
               WHERE name = '$(APP_USER)')
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