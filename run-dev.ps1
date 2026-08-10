# Starts the app with the credentials from .env.local (gitignored).
# Usage: .\run-dev.ps1

$envFile = Join-Path $PSScriptRoot ".env.local"

if (-not (Test-Path $envFile)) {
    Write-Host "Missing .env.local. Copy it from .env.local.example and fill it in." -ForegroundColor Red
    exit 1
}

$required = @(
    "WHATSAPP_ACCESS_TOKEN",
    "WHATSAPP_PHONE_NUMBER_ID",
    "WHATSAPP_APP_SECRET",
    "WHATSAPP_VERIFY_TOKEN"
)

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $name, $value = $line -split "=", 2
        Set-Item -Path "env:$($name.Trim())" -Value $value.Trim()
    }
}

$missing = $required | Where-Object { -not (Get-Item "env:$_" -ErrorAction SilentlyContinue).Value }
if ($missing) {
    Write-Host "Missing values in .env.local: $($missing -join ', ')" -ForegroundColor Red
    exit 1
}

Write-Host "Starting with phone_number_id=$env:WHATSAPP_PHONE_NUMBER_ID" -ForegroundColor Green
& (Join-Path $PSScriptRoot "gradlew.bat") bootRun --console=plain
