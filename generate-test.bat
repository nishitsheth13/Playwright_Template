@echo off
REM ============================================
REM Unified Test Generation CLI
REM ============================================
REM Single entry point for all test generation methods
REM - Recording, AI/JIRA, Interactive, Validation

cd /d "%~dp0"

set "MODE=%1"

REM If no argument, show menu
if "%MODE%"=="" goto :show_menu
if "%MODE%"=="validate" goto :validate_only
if "%MODE%"=="cli" goto :launch_cli
if "%MODE%"=="record" goto :launch_recording

:show_menu
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                                                            ║
echo ║       🤖 AI Test Automation - Unified CLI 🚀             ║
echo ║                                                            ║
echo ║  Choose your test generation method:                      ║
echo ║                                                            ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo  1. 🎥 Record & Auto-Generate (Fastest - 5-10 min)
echo      └─ Record browser actions → Auto-generate all files
echo.
echo  2. 🤖 AI-Assisted Interactive (Full-featured CLI)
echo      └─ Answer questions OR use JIRA → AI generates test
echo.
echo  3. ✅ Validate & Run Tests (Check existing tests)
echo      └─ Compile → Validate → Run → Fix errors
echo.
echo  0. Exit
echo.
choice /C 1230 /N /M "👉 Enter your choice (0-3): "
set CHOICE=%ERRORLEVEL%

if %CHOICE%==1 goto :launch_recording
if %CHOICE%==2 goto :launch_cli
if %CHOICE%==3 goto :validate_only
if %CHOICE%==4 exit /b 0
goto :show_menu

:launch_recording
echo.
echo ════════════════════════════════════════════════════════════
echo 🎥 Starting Playwright Recording...
echo ════════════════════════════════════════════════════════════
echo.
call record-and-generate.bat
if errorlevel 1 (
    echo.
    echo ❌ Recording failed or was cancelled
    echo.
    pause
    exit /b 1
)
echo.
echo ✅ Recording and generation completed!
echo.
pause
exit /b 0

:launch_cli
echo.
echo ════════════════════════════════════════════════════════════
echo 🤖 Launching AI Interactive CLI...
echo ════════════════════════════════════════════════════════════
echo.
node automation-cli.js
if errorlevel 1 (
    echo.
    echo ❌ CLI execution failed
    echo.
    pause
    exit /b 1
)
echo.
echo ✅ CLI completed!
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
set "TEMP_SORTED=%TEMP%\step_sorted_%RANDOM%.txt"

REM Extract all step patterns from all step definition files
for /R "src\test\java\stepDefs" %%f in (*.java) do (
    for /F "tokens=*" %%a in ('findstr /C:"@Given" /C:"@When" /C:"@Then" "%%f" 2^>nul') do (
        echo %%a >> "%TEMP_FILE%"
    )
)

REM Sort and find duplicates
if exist "%TEMP_FILE%" (
    sort "%TEMP_FILE%" > "%TEMP_SORTED%"
    
    REM Check for duplicates using PowerShell
    powershell -Command "$lines = Get-Content '%TEMP_SORTED%'; $patterns = @{}; foreach ($line in $lines) { if ($line -match '@(Given|When|Then)\(\"([^\"]+)\"\)') { $pattern = $matches[2]; if ($patterns.ContainsKey($pattern)) { $patterns[$pattern]++ } else { $patterns[$pattern] = 1 } } }; $dups = $patterns.GetEnumerator() | Where-Object { $_.Value -gt 1 }; if ($dups) { Write-Host '[ERROR] Duplicate patterns found:' -ForegroundColor Red; $dups | ForEach-Object { Write-Host ('  - \"' + $_.Key + '\" (' + $_.Value + ' times)') -ForegroundColor Yellow }; exit 1 } else { Write-Host '[OK] No duplicate step patterns detected' -ForegroundColor Green; exit 0 }"
    
    set "DUPLICATE_CHECK=%ERRORLEVEL%"
    del "%TEMP_FILE%" 2>nul
    del "%TEMP_SORTED%" 2>nul
    
    if %DUPLICATE_CHECK% NEQ 0 (
        echo.
        echo Fix: Rename duplicate methods with Given/When/Then suffix
        echo See: COMPLETE_TEST_GUIDE.md for examples
        pause
        exit /b 1
    )
) else (
    echo [OK] No step definition files found or no annotations to check
)

REM Auto-fix protected methods
echo.
echo Checking for protected methods in page objects...
findstr /S /M "protected static" src\main\java\pages\*.java >nul 2>&1
if not errorlevel 1 (
    echo [FOUND] Protected methods detected
    choice /C YN /M "Auto-fix protected methods to public"
    if errorlevel 2 goto :skip_autofix
    if errorlevel 1 (
        powershell -Command "(Get-ChildItem -Path 'src\main\java\pages\*.java' -Recurse) | ForEach-Object { (Get-Content $_.FullName) -replace 'protected static', 'public static' | Set-Content $_.FullName }"
        echo [OK] Fixed protected methods to public
    )
) else (
    echo [OK] No protected methods found
)

:skip_autofix
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

call mvn clean compile test-compile

if errorlevel 1 (
    echo.
    echo ============================================
    echo   COMPILATION FAILED!
    echo ============================================
    echo.
    
    if %COMPILE_ATTEMPT% LSS %MAX_COMPILE_ATTEMPTS% (
        echo Common fixes:
        echo 1. Missing imports: Add 'import configs.loadProps;'
        echo 2. Wrong access: Change 'protected static' to 'public static'
        echo 3. Wrong method: Use 'getProperty("URL")' not 'BASE_URL()'
        echo 4. Wrong login: Use 'login.loginWith()' not 'loginToApplication()'
        echo.
        echo See TEST_GENERATION_BEST_PRACTICES.md for details
        echo.
        choice /C YN /M "Fix errors and retry compilation"
        if errorlevel 2 (
            echo.
            echo Compilation aborted by user
            pause
            exit /b 1
        )
        if errorlevel 1 goto :compile_loop
    ) else (
        echo.
        echo Maximum compilation attempts reached
        echo Please review errors in TEST_GENERATION_BEST_PRACTICES.md
        pause
        exit /b 1
    )
) else (
    echo.
    echo [OK] Compilation successful!
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
        echo Fix: Remove 'private Page page' declarations
        echo.
    )
    
    if %TEST_ATTEMPT% LSS %MAX_TEST_ATTEMPTS% (
        echo See TEST_GENERATION_BEST_PRACTICES.md for all error fixes
        echo.
        choice /C YN /M "Fix errors and retry tests"
        if errorlevel 2 (
            echo.
            echo Test execution aborted by user
            pause
            exit /b 1
        )
        if errorlevel 1 goto :compile_loop
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