$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot\..

$envFile = Get-Content .env.dev | Where-Object { $_ -match '^\w+=' } | ConvertFrom-StringData
$container = $envFile.DB_CONTAINER_NAME
$saPassword = $envFile.MSSQL_SA_PASSWORD
$appUser = $envFile.APP_USER
$sqlcmdPath = "/opt/mssql-tools18/bin/sqlcmd"

Write-Host "Dropping taskdb_scratch if it exists..."
docker exec $container $sqlcmdPath -S localhost -U sa -P $saPassword -No -Q "IF EXISTS (SELECT name FROM sys.databases WHERE name = 'taskdb_scratch') BEGIN ALTER DATABASE taskdb_scratch SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE taskdb_scratch; END"

Write-Host "Creating taskdb_scratch..."
docker exec $container $sqlcmdPath -S localhost -U sa -P $saPassword -No -Q "CREATE DATABASE taskdb_scratch"

Write-Host "Enabling READ_COMMITTED_SNAPSHOT..."
docker exec $container $sqlcmdPath -S localhost -U sa -P $saPassword -No -Q "ALTER DATABASE taskdb_scratch SET READ_COMMITTED_SNAPSHOT ON WITH ROLLBACK IMMEDIATE"

Write-Host "Creating user and jobrunr_task schema..."
docker exec $container $sqlcmdPath -S localhost -U sa -P $saPassword -No -d taskdb_scratch -Q "CREATE USER [$appUser] FOR LOGIN [$appUser]; ALTER ROLE db_owner ADD MEMBER [$appUser]; EXEC('CREATE SCHEMA jobrunr_task AUTHORIZATION dbo')"

Write-Host "taskdb_scratch reset complete."
Pop-Location