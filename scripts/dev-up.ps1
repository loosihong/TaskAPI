$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot\..

docker-compose --env-file .env.dev up -d

Write-Host "Waiting for MSSQL..." -NoNewline
$name = (Get-Content .env.dev | Select-String '^DB_CONTAINER_NAME=').ToString().Split('=')[1].Trim()
do {
    Start-Sleep -Seconds 2
    Write-Host "." -NoNewline
    $status = docker inspect --format '{{.State.Health.Status}}' $name 2>$null
} while ($status -ne "healthy")
Write-Host " ready."

./gradlew bootRunAll --parallel
Pop-Location