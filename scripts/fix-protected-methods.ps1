param(
    [string]$SearchPath = "src\main\java\pages\*.java"
)

Get-ChildItem -Path $SearchPath -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName
    $fixed = $content -replace 'protected static', 'public static'
    
    if (Compare-Object $content $fixed) {
        Set-Content $_.FullName -Value $fixed
        Write-Host "Fixed protected methods in $($_.Name)" -ForegroundColor Yellow
    }
}

Write-Host "[OK] Protected method replacement complete" -ForegroundColor Green
