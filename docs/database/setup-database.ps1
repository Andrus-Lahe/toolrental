param([string]$PsqlPath)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$sqlFile = Join-Path $PSScriptRoot 'create_toolrental_database.sql.sql'
$propertiesFile = Join-Path $projectRoot 'backend/src/main/resources/application.properties'

if (-not $PsqlPath) {
    $command = Get-Command psql.exe -ErrorAction SilentlyContinue
    if ($command) {
        $PsqlPath = $command.Source
    } else {
        $PsqlPath = Get-ChildItem "$env:ProgramFiles/PostgreSQL/*/bin/psql.exe" |
            Sort-Object { [version]($_.Directory.Parent.Name + '.0') } -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
}
if (-not $PsqlPath -or -not (Test-Path $PsqlPath)) {
    throw 'PostgreSQL client not found. Use -PsqlPath to specify psql.exe.'
}

$properties = @{}
foreach ($line in Get-Content $propertiesFile) {
    if ($line -match '^spring\.datasource\.(username|password)=(.*)$') {
        $properties[$Matches[1]] = $Matches[2]
    }
}
$previousPassword = $env:PGPASSWORD
if (-not $env:PGPASSWORD) { $env:PGPASSWORD = $properties['password'] }

function Invoke-DatabaseCommand {
    param([string]$Database, [string[]]$Commands)
    $result = & $PsqlPath -X -w -h localhost -p 5432 -U $properties['username'] `
        -d $Database -v ON_ERROR_STOP=1 @Commands
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL command failed for $Database." }
    return $result
}

try {
    $exists = Invoke-DatabaseCommand 'postgres' @('-At', '-c', "SELECT 1 FROM pg_database WHERE datname = 'database';")
    if ($exists -ne '1') {
        Invoke-DatabaseCommand 'postgres' @('-c', 'CREATE DATABASE "database";')
    }

    $tableQuery = "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;"
    $tables = @(Invoke-DatabaseCommand 'database' @('-At', '-c', $tableQuery))
    $expected = @([regex]::Matches((Get-Content $sqlFile -Raw), '(?m)^CREATE TABLE (\w+) \(') |
        ForEach-Object { $_.Groups[1].Value } | Sort-Object)
    if ($expected.Count -eq 0) { throw 'No CREATE TABLE statements found in the SQL file.' }

    if ($tables.Count -eq 0) {
        Invoke-DatabaseCommand 'database' @('--single-transaction', '-c', 'SET search_path TO public;', '-f', $sqlFile)
        $tables = @(Invoke-DatabaseCommand 'database' @('-At', '-c', $tableQuery))
    } else {
        Write-Host 'Existing tables detected. SQL import skipped; no data changed.'
    }
    if (Compare-Object $expected $tables) {
        throw 'Existing tables differ from the SQL table list. Review the database manually; no reset is performed.'
    }
    Write-Host "database at localhost:5432 has all $($expected.Count) expected tables in public."
    Write-Host 'Existing column definitions are not migrated by this setup script.'
} finally {
    $env:PGPASSWORD = $previousPassword
}
