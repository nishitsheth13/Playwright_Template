# Smart Compiler Auto-Fix Script
# Analyzes Maven compilation errors and automatically fixes common issues
# Includes syntax correction, code review, and cleanup

param(
    [string]$projectRoot = ".",
    [switch]$verbose = $false
)

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     Smart Compiler Auto-Fix & Code Review System         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

# Statistics
$fixesApplied = 0
$filesModified = 0
$syntaxIssuesFixed = 0
$cleanupIssuesFixed = 0
$codeReviewIssues = @()

Write-Host "`n[PHASE 1] Scanning Java files for issues..." -ForegroundColor Yellow

# Get all Java files
$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    $fileModified = $false
    
    # FIX 0: Class name must match file name
    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    if ($content -match 'public class (\w+)') {
        $className = $Matches[1]
        
        if ($className -ne $fileName) {
            # Rename file to match class name
            $newFileName = "$className.java"
            $newFilePath = Join-Path $file.DirectoryName $newFileName
            
            if (Test-Path $newFilePath) {
                # File with correct name already exists, rename the class instead
                $content = $content -replace "public class $className", "public class $fileName"
                $fileModified = $true
                Write-Host "  [FIX] Renamed class '$className' to '$fileName' to match file: $($file.Name)" -ForegroundColor Green
            } else {
                # Rename the file to match class name
                Move-Item -Path $file.FullName -Destination $newFilePath -Force
                Write-Host "  [FIX] Renamed file '$($file.Name)' to '$newFileName' to match class name" -ForegroundColor Green
                $file = Get-Item $newFilePath
                continue
            }
        }
    }
    
    # FIX 1: Missing imports
    if ($content -match 'loadProps\.getProperty' -and $content -notmatch 'import configs\.loadProps') {
        $packageLine = ($content -split "`n" | Where-Object { $_ -match '^package ' })[0]
        $content = $content -replace "(?m)^package .*`n", "$packageLine`nimport configs.loadProps;`n"
        $fileModified = $true
        Write-Host "  [FIX] Added missing loadProps import: $($file.Name)" -ForegroundColor Green
    }
    
    # FIX 2: Protected methods to public
    if ($content -match 'protected static') {
        $content = $content -replace 'protected static', 'public static'
        $fileModified = $true
        Write-Host "  [FIX] Changed protected to public: $($file.Name)" -ForegroundColor Green
    }
    
    # FIX 3: Wrong method names - capitalize first letter
    # Fix method calls like page.click() to Page.click()
    $content = $content -replace '\bpage\.', 'Page.'
    
    # FIX 4: Class name capitalization issues
    # Ensure class names start with uppercase
    $classMatch = $content -match 'public class (\w+)'
    if ($classMatch) {
        $className = $Matches[1]
        $correctClassName = $className.Substring(0,1).ToUpper() + $className.Substring(1)
        if ($className -ne $correctClassName) {
            $content = $content -replace "public class $className", "public class $correctClassName"
            $fileModified = $true
            Write-Host "  [FIX] Capitalized class name: $className -> $correctClassName" -ForegroundColor Green
        }
    }
    
    # FIX 5: Common method name fixes
    $methodFixes = @{
        'loginToApplication' = 'loginWith'
        'BASE_URL\(\)' = 'getProperty("URL")'
        'navigateToUrl' = 'navigateTo'
        'clickElement' = 'clickOnElement'
        'enterValue' = 'enterText'
        'selectDropdown' = 'selectFromDropdown'
    }
    
    foreach ($wrong in $methodFixes.Keys) {
        if ($content -match $wrong) {
            $content = $content -replace $wrong, $methodFixes[$wrong]
            $fileModified = $true
            Write-Host "  [FIX] Fixed method name: $wrong -> $($methodFixes[$wrong]) in $($file.Name)" -ForegroundColor Green
        }
    }
    
    # FIX 6: Missing extends BasePage for Page Objects
    if ($file.FullName -match '\\pages\\.*\.java$' -and $content -notmatch 'extends BasePage') {
        if ($content -match 'public class (\w+)') {
            $content = $content -replace '(public class \w+)', '$1 extends BasePage'
            $fileModified = $true
            Write-Host "  [FIX] Added 'extends BasePage': $($file.Name)" -ForegroundColor Green
            
            # Add import if missing
            if ($content -notmatch 'import configs\.BasePage') {
                $packageLine = ($content -split "`n" | Where-Object { $_ -match '^package ' })[0]
                $content = $content -replace "(?m)^package .*`n", "$packageLine`nimport configs.BasePage;`n"
            }
        }
    }
    
    # FIX 7: Missing extends browserSelector for Step Definitions
    if ($file.FullName -match '\\stepDefs\\.*\.java$' -and $content -notmatch 'extends browserSelector') {
        if ($content -match 'public class (\w+)') {
            $content = $content -replace '(public class \w+)', '$1 extends browserSelector'
            $fileModified = $true
            Write-Host "  [FIX] Added 'extends browserSelector': $($file.Name)" -ForegroundColor Green
            
            # Add import if missing
            if ($content -notmatch 'import configs\.browserSelector') {
                $packageLine = ($content -split "`n" | Where-Object { $_ -match '^package ' })[0]
                $content = $content -replace "(?m)^package .*`n", "$packageLine`nimport configs.browserSelector;`n"
            }
        }
    }
    
    # FIX 8: Remove duplicate imports
    $lines = $content -split "`n"
    $imports = $lines | Where-Object { $_ -match '^import ' }
    $uniqueImports = $imports | Select-Object -Unique
    if ($imports.Count -ne $uniqueImports.Count) {
        $fileModified = $true
        Write-Host "  [FIX] Removed duplicate imports: $($file.Name)" -ForegroundColor Green
    }
    
    # FIX 9: Fix variable naming (camelCase)
    # Convert LOGIN_BUTTON to loginButton for local variables
    if ($content -match '\s+String\s+[A-Z_]+\s*=') {
        $content = $content -replace '(\s+String\s+)([A-Z_]+)(\s*=)', {
            param($match)
            $varName = $match.Groups[2].Value
            $camelCase = ($varName -split '_' | ForEach-Object { 
                $_.Substring(0,1).ToUpper() + $_.Substring(1).ToLower() 
            }) -join ''
            $camelCase = $camelCase.Substring(0,1).ToLower() + $camelCase.Substring(1)
            "$($match.Groups[1].Value)$camelCase$($match.Groups[3].Value)"
        }
        $fileModified = $true
        Write-Host "  [FIX] Fixed variable naming to camelCase: $($file.Name)" -ForegroundColor Green
    }
    
    # FIX 10: Missing Page import in step definitions
    if ($file.FullName -match '\\stepDefs\\' -and $content -match 'import com\.microsoft\.playwright\.Page') {
        # Remove it - not needed when extending browserSelector
        $content = $content -replace 'import com\.microsoft\.playwright\.Page;?\s*', ''
        $fileModified = $true
        Write-Host "  [FIX] Removed unnecessary Page import: $($file.Name)" -ForegroundColor Green
    }
    
    # ============================================================
    # SYNTAX CORRECTIONS
    # ============================================================
    
        $filesModified++
    }
}

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              Auto-Fix Summary Report                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host "`n[RESULTS]" -ForegroundColor Green
Write-Host "  Files Scanned:          $($javaFiles.Count)" -ForegroundColor White
Write-Host "  Files Modified:         $filesModified" -ForegroundColor Green
Write-Host "  Total Fixes Applied:    $fixesApplied" -ForegroundColor Green
Write-Host "  Syntax Issues Fixed:    $syntaxIssuesFixed" -ForegroundColor Magenta
Write-Host "  Cleanup Issues Fixed:   $cleanupIssuesFixed" -ForegroundColor Yellow

if ($codeReviewIssues.Count -gt 0) {
    Write-Host "`n[CODE REVIEW WARNINGS]" -ForegroundColor Yellow
    Write-Host "  Total Issues Found: $($codeReviewIssues.Count)" -ForegroundColor Yellow
    Write-Host "`n  Review these manually:" -ForegroundColor Yellow
    foreach ($issue in $codeReviewIssues | Select-Object -First 10) {
        Write-Host "    - $($issue.File): $($issue.Issue)" -ForegroundColor DarkYellow
    }
    if ($codeReviewIssues.Count -gt 10) {
        Write-Host "    ... and $($codeReviewIssues.Count - 10) more" -ForegroundColor DarkYellow
    }
}

Write-Host "`n╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan Check if line needs semicolon (not comment, not bracket, not annotation, not empty)
            if ($line -match '^\s*[^/\*@\s}].*[^;{}\s]$' -and 
                $line -notmatch '^\s*(public|private|protected|class|interface|enum|@|\*|//)' -and
                $line -notmatch '(if|else|for|while|try|catch|finally)\s*\(') {
                $lines[$i] = $line + ';'
                $fileModified = $true
                $syntaxIssuesFixed++
            }
        }
        $content = $lines -join "`n"
        if ($fileModified) {
            Write-Host "  [SYNTAX] Added missing semicolons: $($file.Name)" -ForegroundColor Magenta
        }
    }
    
    # SYNTAX 2: Missing closing brackets
    $openBraces = ($content -split '' | Where-Object { $_ -eq '{' }).Count
    $closeBraces = ($content -split '' | Where-Object { $_ -eq '}' }).Count
    if ($openBraces -gt $closeBraces) {
        $content += "`n" + ('}' * ($openBraces - $closeBraces))
        $fileModified = $true
        $syntaxIssuesFixed++
        Write-Host "  [SYNTAX] Added missing closing braces: $($file.Name)" -ForegroundColor Magenta
    }
    
    # SYNTAX 3: Fix common typos
    $typoFixes = @{
        'Sytem\.out' = 'System.out'
        'Interger' = 'Integer'
        'Strig' = 'String'
        'publc' = 'public'
        'privte' = 'private'
        'protcted' = 'protected'
        'retrun' = 'return'
        'imoprt' = 'import'
        'calss' = 'class'
        'voild' = 'void'
    }
    
    foreach ($typo in $typoFixes.Keys) {
        if ($content -match $typo) {
            $content = $content -replace $typo, $typoFixes[$typo]
            $fileModified = $true
            $syntaxIssuesFixed++
            Write-Host "  [SYNTAX] Fixed typo: $typo -> $($typoFixes[$typo]) in $($file.Name)" -ForegroundColor Magenta
        }
    }
    
    # SYNTAX 4: Fix incorrect string quotes
    $content = $content -replace '\"\"', '""'
    $content = $content -replace '''''', "''"
    
    # ============================================================
    # CODE REVIEW & CLEANUP
    # ============================================================
    
    # REVIEW 1: Unused imports (basic detection)
    $importLines = $content -split "`n" | Where-Object { $_ -match '^import ' }
    $codeContent = $content -replace '(?s)/\*.*?\*/', '' # Remove block comments
    
    foreach ($importLine in $importLines) {
        if ($importLine -match 'import\s+([\w\.]+);') {
            $importClass = $Matches[1] -replace '.*\.', ''
            # Check if class is used in code
            if ($codeContent -notmatch "\b$importClass\b" -and $importClass -ne '*') {
                $content = $content -replace [regex]::Escape($importLine) + '\s*', ''
                $fileModified = $true
                $cleanupIssuesFixed++
                Write-Host "  [CLEANUP] Removed unused import: $importClass in $($file.Name)" -ForegroundColor Yellow
            }
        }
    }
    
    # CLEANUP 1: Remove trailing whitespace
    $content = $content -replace '[ \t]+$', ''
    
    # CLEANUP 2: Fix multiple blank lines
    $content = $content -replace '(\r?\n){3,}', "`n`n"
    
    # CLEANUP 3: Fix indentation (basic - ensure tabs or spaces consistency)
    if ($content -match '\t' -and $content -match '^    ') {
        # Mixed tabs and spaces - convert tabs to 4 spaces
        $content = $content -replace '\t', '    '
        $fileModified = $true
        $cleanupIssuesFixed++
        Write-Host "  [CLEANUP] Fixed mixed tabs/spaces indentation: $($file.Name)" -ForegroundColor Yellow
    }
    
    # CLEANUP 4: Remove commented-out code blocks (// TODO comments are kept)
    $lines = $content -split "`n"
    $cleanedLines = @()
    $commentedBlockCount = 0
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        # Skip lines that are just commented code (not TODO/FIXME/NOTE)
        if ($line -match '^\s*//' -and $line -notmatch '(TODO|FIXME|NOTE|HACK|XXX)') {
            if ($line -match '//\s*[A-Za-z_][A-Za-z0-9_]*\s*[=\(]') {
                # Looks like commented code
                $commentedBlockCount++
                continue
            }
        }
        $cleanedLines += $line
    }
    
    if ($commentedBlockCount -gt 0) {
        $content = $cleanedLines -join "`n"
        $fileModified = $true
        $cleanupIssuesFixed++
        Write-Host "  [CLEANUP] Removed $commentedBlockCount lines of commented code: $($file.Name)" -ForegroundColor Yellow
    }
    
    # REVIEW 2: Detect potential issues
    if ($content -match '==\s*null' -or $content -match 'null\s*==') {
        $codeReviewIssues += @{
            File = $file.Name
            Issue = "Null comparison detected - consider using Objects.isNull() or Optional"
            Line = ($content -split "`n" | Select-String '==\s*null|null\s*==' -SimpleMatch)[0].LineNumber
        }
    }
    
    if ($content -match 'Thread\.sleep\(') {
        $codeReviewIssues += @{
            File = $file.Name
            Issue = "Thread.sleep() detected - consider using explicit waits in Playwright"
            Line = ($content -split "`n" | Select-String 'Thread\.sleep' -SimpleMatch)[0].LineNumber
        }
    }
    
    if ($content -match 'System\.out\.println') {
        $codeReviewIssues += @{
            File = $file.Name
            Issue = "System.out.println detected - consider using proper logging framework"
            Line = ($content -split "`n" | Select-String 'System\.out\.println' -SimpleMatch)[0].LineNumber
        }
    }
    
    # CLEANUP 5: Organize imports (group by package)
    if ($content -match 'import ') {
        $importSection = ($content -split "`n" | Where-Object { $_ -match '^import ' }) | Sort-Object
        if ($importSection.Count -gt 0) {
            $nonImportContent = $content -replace '(?m)^import .*$\s*', ''
            $packageLine = ($content -split "`n" | Where-Object { $_ -match '^package ' })[0]
            $content = $packageLine + "`n`n" + ($importSection -join "`n") + "`n`n" + $nonImportContent.TrimStart()
            $cleanupIssuesFixed++
        }
    }
    
    # Save file if modified
    if ($fileModified -and $content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $fixesApplied++
    }
}

Write-Host "`n=== Auto-Fix Complete ===" -ForegroundColor Cyan
Write-Host "Files modified: $fixesApplied" -ForegroundColor Green
Write-Host ""

exit 0
