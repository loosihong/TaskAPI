param(
    [Parameter(Mandatory)][ValidateSet("identity","task")]$Service,
    [Parameter(Mandatory)][string]$Description,
    [string]$Schemas = "dbo",
    [string]$ExcludeObjects = "",
    [switch]$IncludeSchema
)
$ErrorActionPreference = "Stop"
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

Write-Host "Generating $changelogFile ..."

$liquibaseArgs = @(
    "--defaultsFile=liquibase.properties"
    "diff-changelog"
    "--changelog-file=$changelogFile"
    "--schemas=$Schemas"
)
if ($ExcludeObjects) { $liquibaseArgs += "--exclude-objects=$ExcludeObjects" }
if ($IncludeSchema) {
    $liquibaseArgs += "--include-schema=true"
    $liquibaseArgs += "--reference-schemas=$Schemas"
}

liquibase @liquibaseArgs

Write-Host "Created $changelogFile"
Write-Host "Add it to db.changelog-master.yaml before running update."
Pop-Location