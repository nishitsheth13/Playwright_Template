@echo off
REM ============================================
REM AI Test Generator with Auto-Validation
REM ============================================
REM This script generates tests and validates them automatically
REM Workflow: Generate → Analyze → Compile → Fix → Run → Repeat

cd /d "%~dp0"

set "MODE=%1"

if "%MODE%"=="validate" goto :validate_only

:generate_mode
echo.
echo ============================================
echo   AI Test Automation Generator
echo ============================================
echo.
echo Starting Interactive CLI...
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

REM Check for duplicate annotations
echo.
echo Checking for potential duplicate Cucumber annotations...
findstr /S /C:"@Given" src\test\java\stepDefs\*.java | find /C "@Given" >nul
echo [INFO] Review step definitions for duplicate annotations manually

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
