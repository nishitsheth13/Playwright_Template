# PowerShell script to extract URL from configurations.properties
# Usage: .\extract-url.ps1

$configFile = "src\test\resources\configurations.properties"

if (-not (Test-Path $configFile)) {
    Write-Error "Configuration file not found: $configFile"
    exit 1
}

$props = Get-Content $configFile
$urlLine = $props | Where-Object { $_ -match '^URL=' }

if ($urlLine) {
    # Extract value after '=' and handle escaped colons
    $url = ($urlLine -split '=', 2)[1] -replace '\\:', ':'
    Write-Output $url
} else {
    Write-Error "URL property not found in configuration file"
    exit 1
}
