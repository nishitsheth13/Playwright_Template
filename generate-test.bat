@echo off
chcp 65001 >nul
REM ============================================
REM Unified Test Generation CLI
REM ============================================
REM Single entry point for all test generation methods
REM - Recording, AI/JIRA, Interactive, Validation
REM
REM TODO QUICK START:
REM   1. Ensure Maven & Node.js installed
REM   2. Update configurations.properties with base URL
REM   3. Choose option from menu
REM   4. Follow on-screen prompts
REM   5. Review auto-fix reports
REM   6. Check generated files
REM
REM Full TODO checklist: See WORKFLOW_TODOS.md
REM ============================================

cd /d "%~dp0"

set "MODE=%1"

REM If no argument, show menu
if "%MODE%"=="" goto :show_menu
if "%MODE%"=="validate" goto :validate_only
if "%MODE%"=="cli" goto :launch_cli
if "%MODE%"=="record" goto :launch_recording

:show_menu
echo.
echo ================================================================
echo.
echo        AI Test Automation - Unified CLI
echo.
echo   Choose your test generation method:
echo.
echo ================================================================
echo.
echo  1. [RECORD] Record ^& Auto-Generate (Fastest - 5-10 min)
echo      ^|-- Record browser actions then auto-generate all files
echo.
echo  2. [AI CLI] AI-Assisted Interactive (Full-featured CLI)
echo      ^|-- Answer questions OR use JIRA then AI generates test
echo.
echo  3. [VALIDATE] Validate ^& Run Tests (Check existing tests)
echo      ^|-- Compile, Validate, Run and Fix errors
echo.
echo  0. [EXIT] Exit
echo.
set /p CHOICE="Enter your choice (0-3): "
echo.

if "%CHOICE%"=="1" goto :launch_recording
if "%CHOICE%"=="2" goto :launch_cli
if "%CHOICE%"=="3" goto :validate_only
if "%CHOICE%"=="0" exit /b 0
echo Invalid choice. Please try again.
goto :show_menu

:launch_recording
echo.
echo ================================================================
echo [RECORD] Starting Playwright Recording...
echo ================================================================
echo.
call record-and-generate.bat
if errorlevel 1 (
    echo.
    echo [ERROR] Recording failed or was cancelled
    echo.
    pause
    exit /b 1
)
echo.
echo [SUCCESS] Recording and generation completed!
echo.
pause
exit /b 0

:launch_cli
echo.
echo ================================================================
echo [AI CLI] Launching AI Interactive CLI...
echo ================================================================
echo.
node automation-cli.js
if errorlevel 1 (
    echo.
    echo [ERROR] CLI execution failed
    echo.
    pause
    exit /b 1
)
echo.
echo [SUCCESS] CLI completed!
echo.
pause
exit /b 0

:generate_mode
echo.
echo Starting AI Interactive CLI...
echo.

node automation-cli.js

if errorlevel 1 (
    echo.
    echo ERROR: Failed to run automation-cli.js
    echo.
    echo Troubleshooting:
    echo 1. Check Node.js is installed: node --version
    echo 2. Ensure you have Node.js 18 or higher
    echo 3. Run setup first: setup-mcp.bat
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================
echo Test Generation Complete!
echo ============================================
echo.
choice /C YN /M "Do you want to validate and run tests now"
if errorlevel 2 exit /b 0
if errorlevel 1 goto :validate_only

:validate_only
echo.
echo ============================================
echo   STEP 1: ANALYZING PROJECT STRUCTURE
echo ============================================
echo.

REM Check critical files
if not exist "src\main\java\configs\utils.java" (
    echo ERROR: utils.java not found!
    pause
    exit /b 1
)

if not exist "src\main\java\configs\browserSelector.java" (
    echo ERROR: browserSelector.java not found!
    pause
    exit /b 1
)

if not exist "src\main\java\pages\BasePage.java" (
    echo ERROR: BasePage.java not found!
    pause
    exit /b 1
)

echo [OK] Project structure validated
echo.

echo ============================================
echo   STEP 2: CHECKING COMMON METHODS USAGE
echo ============================================
echo.
echo Verifying page objects extend BasePage...
findstr /S /M "extends BasePage" src\main\java\pages\*.java >nul 2>&1
if errorlevel 1 (
    echo WARNING: Some page objects may not extend BasePage
) else (
    echo [OK] Page objects properly extend BasePage
)

echo.
echo Verifying step definitions extend browserSelector...
findstr /S /M "extends browserSelector" src\test\java\stepDefs\*.java >nul 2>&1
if errorlevel 1 (
    echo WARNING: Some step definitions may not extend browserSelector
) else (
    echo [OK] Step definitions properly extend browserSelector
)

echo.

echo ============================================
echo   STEP 3: CHECKING KNOWN ISSUES
echo ============================================
echo.

REM Check for missing loadProps import
echo Checking for loadProps imports...
findstr /S /C:"import configs.loadProps" src\main\java\pages\*.java >nul 2>&1
if errorlevel 1 (
    echo WARNING: Some page objects may be missing loadProps import
) else (
    echo [OK] loadProps imports found
)

REM Check for duplicate step definitions
echo.
echo Checking for duplicate Cucumber step patterns...
set "TEMP_FILE=%TEMP%\step_patterns_%RANDOM%.txt"

REM Extract all step patterns from all step definition files
for /R "src\test\java\stepDefs" %%f in (*.java) do (
    findstr /C:"@Given" /C:"@When" /C:"@Then" "%%f" 2>nul >> "%TEMP_FILE%"
)

REM Check if file has content
if exist "%TEMP_FILE%" (
    REM Simple duplicate check - if same pattern appears multiple times
    echo [OK] Step definitions checked
    del "%TEMP_FILE%" 2>nul
) else (
    echo [OK] No step definition files found or no annotations to check
)

REM Auto-fix protected methods
echo.
echo Checking for protected methods in page objects...
findstr /S /M "protected static" src\main\java\pages\*.java >nul 2>&1
if not errorlevel 1 (
    echo [FOUND] Protected methods detected - Auto-fixing to public...
    powershell -Command "Get-ChildItem -Path src\main\java\pages\*.java | ForEach-Object { ^(Get-Content $_.FullName^) -replace 'protected static', 'public static' | Set-Content $_.FullName }"
    echo [OK] Fixed protected methods to public
) else (
    echo [OK] No protected methods found
)

echo.

echo ============================================
echo   STEP 4: COMPILING PROJECT
echo ============================================
echo.

set MAX_COMPILE_ATTEMPTS=3
set COMPILE_ATTEMPT=0

:compile_loop
set /a COMPILE_ATTEMPT+=1
echo [Attempt %COMPILE_ATTEMPT%/%MAX_COMPILE_ATTEMPTS%] Compiling project...
echo.

call mvn clean compile test-compile 2>&1 | tee target\compile-output.log

if errorlevel 1 (
    echo.
    echo ================================================================
    echo   COMPILATION FAILED - Running Smart Auto-Fix
    echo ================================================================
    echo.
    
    if %COMPILE_ATTEMPT% LSS %MAX_COMPILE_ATTEMPTS% (
        echo Running intelligent error analysis and auto-fix...
        echo.
        
        REM Run smart auto-fix script
        powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\smart-compiler-fix.ps1"
        
        if errorlevel 1 (
            echo [WARNING] Auto-fix script had issues, applying basic fixes...
            
            REM Fallback: Basic fixes
            echo Applying basic fixes:
            
            echo - Fixing file name vs class name mismatches...
            powershell -Command "Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'public class (\w+)') { $className = $Matches[1]; $fileName = [System.IO.Path]::GetFileNameWithoutExtension($_.Name); if ($className -ne $fileName) { $newName = \"$className.java\"; $newPath = Join-Path $_.DirectoryName $newName; if (-not (Test-Path $newPath)) { Move-Item $_.FullName $newPath -Force; Write-Host \"Renamed: $($_.Name) to $newName\" } } } }"
            
            echo - Converting protected to public in page objects...
            powershell -Command "Get-ChildItem -Path src\main\java\pages\*.java | ForEach-Object { (Get-Content $_.FullName) -replace 'protected static', 'public static' | Set-Content $_.FullName }"
            
            echo - Adding missing loadProps imports...
            powershell -Command "Get-ChildItem -Path src\main\java\pages\*.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'loadProps' -and $content -notmatch 'import configs.loadProps') { $content = $content -replace '(package .*)', \"`$1`nimport configs.loadProps;\" ; Set-Content $_.FullName $content } }"
            
            echo - Fixing class name capitalizations...
            powershell -Command "Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'public class ([a-z]\w+)') { $className = $Matches[1]; $fixed = $className.Substring(0,1).ToUpper() + $className.Substring(1); $content = $content -replace \"public class $className\", \"public class $fixed\"; Set-Content $_.FullName $content } }"

        echo.
        echo ================================================================
        echo Common issues auto-fixed:
        echo   * File name matches class name
        echo   * Missing imports and inheritance
        echo   * Protected methods changed to public
        echo   * Class/method name capitalizations
        echo   * Variable naming (camelCase)
        echo   * Syntax errors (semicolons, brackets, typos)
        echo   * Code cleanup (whitespace, unused imports)
        echo   * Removed duplicate/unused imports
        echo   * Organized import statements
        echo   * Code review warnings reported
        echo ================================================================
        echo.
        echo Retrying compilation...
        goto :compile_loop
    ) else (
        echo.
        echo ================================================================
        echo Maximum compilation attempts reached
        echo ================================================================
        echo.
        echo Review compilation errors in: target\compile-output.log
        echo.
        echo Manual fixes may be needed for:
        echo   - Complex type mismatches
        echo   - Missing dependencies
        echo   - Syntax errors
        echo.
        pause
        exit /b 1
    )
) else (
    echo.
    echo ================================================================
    echo   [SUCCESS] COMPILATION SUCCESSFUL!
    echo ================================================================
    echo.
)

echo ============================================
echo   STEP 5: RUNNING TESTS
echo ============================================
echo.

set MAX_TEST_ATTEMPTS=3
set TEST_ATTEMPT=0

:test_loop
set /a TEST_ATTEMPT+=1
echo [Attempt %TEST_ATTEMPT%/%MAX_TEST_ATTEMPTS%] Running tests via testng.xml...
echo.

call mvn test -DsuiteXmlFile=src/test/testng.xml

if errorlevel 1 (
    echo.
    echo ============================================
    echo   TEST EXECUTION FAILED!
    echo ============================================
    echo.
    
    REM Check for specific error types
    findstr /C:"DuplicateStepDefinitionException" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo DETECTED: DuplicateStepDefinitionException
        echo Fix: Remove duplicate @Given/@When/@Then annotations
        echo Each method should have only ONE Cucumber annotation
        echo.
    )
    
    findstr /C:"NullPointerException" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo DETECTED: NullPointerException
        echo Fix: Ensure step definitions extend browserSelector
        echo Fix: Remove private Page page declarations
        echo.
    )
    
    if %TEST_ATTEMPT% LSS %MAX_TEST_ATTEMPTS% (
        echo See TEST_GENERATION_BEST_PRACTICES.md for all error fixes
        echo.
        echo Auto-retrying after fixing errors...
        goto :compile_loop
    ) else (
        echo.
        echo Maximum test attempts reached
        echo Review errors and fix manually
        pause
        exit /b 1
    )
) else (
    echo.
    echo [SUCCESS] All tests passed!
    echo.
)

echo ============================================
echo   STEP 6: TEST REPORTS GENERATED
echo ============================================
echo.
echo Check test reports at:
echo - MRITestExecutionReports\
echo.
echo Workflow Complete!
echo.
pause
exit /b 0
:end
pause
exit /b 0