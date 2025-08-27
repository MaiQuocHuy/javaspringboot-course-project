param (
    [string]$LiquibaseGoal = "liquibase:update"
)

# Load .env file
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^\s*#") { return }
        if ($_ -match "^\s*$") { return }
        $name, $value = $_ -split "=", 2
        $name = $name.Trim()
        $value = $value.Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
    Write-Host "✅ Loaded environment variables from .env"
}

Write-Host "🚀 Running Liquibase with goal: $LiquibaseGoal"
Write-Host "⚙️ Extra args: " ($args -join " ")

if ($args.Count -gt 0) {
    & ./mvnw.cmd $LiquibaseGoal @args
} else {
    & ./mvnw.cmd $LiquibaseGoal
}