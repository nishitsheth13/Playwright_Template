param(
    [string]$InputFile
)

$lines = Get-Content $InputFile
$patterns = @{}

foreach ($line in $lines) {
    if ($line -match '@(Given|When|Then)\("([^"]+)"\)') {
        $pattern = $matches[2]
        if ($patterns.ContainsKey($pattern)) {
            $patterns[$pattern]++
        } else {
            $patterns[$pattern] = 1
        }
    }
}

$dups = $patterns.GetEnumerator() | Where-Object { $_.Value -gt 1 }

if ($dups) {
    Write-Host '[ERROR] Duplicate step patterns found:' -ForegroundColor Red
    $dups | ForEach-Object {
        Write-Host "  - `"$($_.Key)`" ($($_.Value) times)" -ForegroundColor Yellow
    }
    exit 1
} else {
    Write-Host '[OK] No duplicate step patterns' -ForegroundColor Green
    exit 0
}
