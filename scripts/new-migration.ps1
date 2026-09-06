param(
    [Parameter(Mandatory)][ValidateSet("identity","task")]$Service,
    [Parameter(Mandatory)][string]$Description,
    [string]$Profile = "default",
    [string]$ExcludeObjects
)
$ErrorActionPreference = "Stop"

function Get-OwnedTables {
    param([Parameter(Mandatory)][string]$ChangesDir)

    $tables = @()
    $pattern = '(?im)^CREATE TABLE\s+(?:\[?\w+\]?\.)?\[?(?<table>\w+)\]?\s*\('

    Get-ChildItem $ChangesDir -Filter "*.mssql.sql" | ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        [regex]::Matches($content, $pattern) | ForEach-Object {
            $tables += $_.Groups['table'].Value
        }
    }

    return $tables | Select-Object -Unique
}

$schemaProfiles = @{
    "default" = @{ Schemas = "dbo";          IncludeSchema = $false }
    "jobrunr" = @{ Schemas = "jobrunr_task";  IncludeSchema = $true  }
}
$profile = $schemaProfiles[$Profile]
if (-not $profile) { throw "Unknown profile '$Profile'. Known: $($schemaProfiles.Keys -join ', ')" }

if ($Service -eq 'task' -and $Profile -eq 'default' -and -not $PSBoundParameters.ContainsKey('ExcludeObjects')) {
    $identityChangesDir = "$PSScriptRoot\..\service-identity\src\main\resources\database\changelog\identity\changes"
    $ownedByIdentity = Get-OwnedTables -ChangesDir $identityChangesDir

    if ($ownedByIdentity.Count -eq 0) {
        Write-Warning "No identity-owned tables detected in $identityChangesDir — exclude-objects will be empty."
    } else {
        Write-Host "Excluding identity-owned tables: $($ownedByIdentity -join ', ')"
        $ExcludeObjects = ($ownedByIdentity | ForEach-Object { "table:$_" }) -join ','
    }
}

Push-Location $PSScriptRoot\..\service-$Service

$changesDir = "src/main/resources/database/changelog/$Service/changes"
$nextNumber = 1
$existing = Get-ChildItem $changesDir -Filter "*.mssql.sql" | Where-Object { $_.Name -match '^(\d{3})-' }
if ($existing) {
    $nextNumber = ($existing | ForEach-Object { [int]$Matches[1] } | Measure-Object -Maximum).Maximum + 1
}
$number = "{0:D3}" -f $nextNumber
$slug = ($Description -replace '[^a-zA-Z0-9]+', '-').Trim('-').ToLower()
$changelogFile = "$changesDir/$number-$slug.mssql.sql"

$liquibaseArgs = @(
    "--defaultsFile=liquibase.properties"
    "diff-changelog"
    "--changelog-file=$changelogFile"
    "--schemas=$($profile.Schemas)"
)
if ($profile.IncludeSchema) {
    $liquibaseArgs += "--include-schema=true"
    $liquibaseArgs += "--reference-schemas=$($profile.Schemas)"
}
if ($ExcludeObjects) { $liquibaseArgs += "--exclude-objects=$ExcludeObjects" }

Write-Host "Generating $changelogFile ..."
liquibase @liquibaseArgs
Write-Host "Created $changelogFile"
Write-Host "Add it to db.changelog-master.yaml before running update."
Pop-Location