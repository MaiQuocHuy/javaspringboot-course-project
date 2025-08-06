param (
    [string]$LiquibaseGoal = "liquibase:update"
)

# Load .env file
Get-Content ".env" | ForEach-Object {
    if ($_ -match "^\s*#") { return }        # Ignore comments
    if ($_ -match "^\s*$") { return }        # Ignore blank lines
    $name, $value = $_ -split "=", 2
    $name = $name.Trim()
    $value = $value.Trim()
    [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
}

# Run Liquibase Maven command with goal
Write-Host "Running Liquibase with goal: $LiquibaseGoal"
./mvnw $LiquibaseGoal
