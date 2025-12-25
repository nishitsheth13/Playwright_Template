param(
    [string]$SearchPath = "src\main\java\pages\*.java"
)

$fixed = $false
Get-ChildItem -Path $SearchPath -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $newContent = $content -replace 'BASE_URL\(\)', 'getProperty("URL")'
    
    if ($content -ne $newContent) {
        Set-Content $_.FullName -Value $newContent -NoNewline
        Write-Host "Fixed BASE_URL in $($_.Name)" -ForegroundColor Yellow
        $fixed = $true
    }
}

Write-Host "[OK] BASE_URL replacement complete" -ForegroundColor Green
exit 0
