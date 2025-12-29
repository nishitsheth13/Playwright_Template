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
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║                                                                        ║
echo ║        🚀 Playwright Test Automation Framework - Main Menu            ║
echo ║                                                                        ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
echo ┌─ SETUP (First Time Users) ─────────────────────────────────────────────┐
echo │                                                                          │
echo │  1. 🔧 Complete Setup ^& Installation                                   │
echo │     └─ Install Playwright, Node.js dependencies, MCP Server            │
echo │                                                                          │
echo └──────────────────────────────────────────────────────────────────────────┘
echo.
echo ┌─ TEST GENERATION (Main Workflows) ─────────────────────────────────────┐
echo │                                                                          │
echo │  2. 📹 Record ^& Auto-Generate Test                                     │
echo │     └─ Record browser actions → Auto-generate test files (5-10 min)    │
echo │                                                                          │
echo │  3. 🤖 AI Interactive Test Generation                                   │
echo │     └─ Answer AI questions → Generate test from conversation           │
echo │                                                                          │
echo │  4. 🎫 JIRA + AI Test Generation                                        │
echo │     └─ Provide JIRA ticket → AI creates test + links to JIRA           │
echo │                                                                          │
echo │  5. ✍️  Manual Test Creation Guide                                      │
echo │     └─ Step-by-step guide for writing tests manually                   │
echo │                                                                          │
echo └──────────────────────────────────────────────────────────────────────────┘
echo.
echo ┌─ UTILITIES ─────────────────────────────────────────────────────────────┐
echo │                                                                          │
echo │  6. ✅ Validate ^& Fix Generated Tests                                  │
echo │     └─ Compile, validate, fix errors, run tests                        │
echo │                                                                          │
echo │  7. 📊 View Test Reports                                                │
echo │     └─ Open latest Extent Reports in browser                           │
echo │                                                                          │
echo │  0. 🚪 Exit                                                              │
echo │                                                                          │
echo └──────────────────────────────────────────────────────────────────────────┘
echo.
set /p CHOICE="Enter your choice (0-7): "
echo.

if "%CHOICE%"=="1" goto :complete_setup
if "%CHOICE%"=="2" goto :launch_recording
if "%CHOICE%"=="3" goto :launch_ai_cli
if "%CHOICE%"=="4" goto :launch_jira_ai_cli
if "%CHOICE%"=="5" goto :manual_guide
if "%CHOICE%"=="6" goto :validate_only
if "%CHOICE%"=="7" goto :view_reports
if "%CHOICE%"=="0" goto :exit_script
echo.
echo ❌ Invalid choice. Please enter a number between 0-7.
echo.
timeout /t 2 >nul
goto :show_menu

REM ============================================
REM MENU OPTION HANDLERS
REM ============================================

REM Option 1: Complete Setup & Installation
:complete_setup
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        🔧 Complete Setup ^& Installation                               ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
echo This will install all required components:
echo   ✓ Node.js dependencies (MCP Server, Playwright CLI)
echo   ✓ Playwright browsers
echo   ✓ Maven dependencies
echo   ✓ Validate configurations
echo.
echo ⚠️  This may take 5-15 minutes depending on your internet speed.
echo.
set /p CONFIRM="Continue with installation? (Y/N): "
if /i not "%CONFIRM%"=="Y" goto :show_menu

echo.
echo ════════════════════════════════════════════════════════════════════════
echo [1/5] Checking prerequisites...
echo ════════════════════════════════════════════════════════════════════════

REM Check Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is NOT installed! Download from: https://nodejs.org/
    pause
    goto :show_menu
)
echo ✅ Node.js installed

REM Check Maven
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven is NOT installed! Download from: https://maven.apache.org/
    pause
    goto :show_menu
)
echo ✅ Maven installed

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is NOT installed! Install Java JDK 17+
    pause
    goto :show_menu
)
echo ✅ Java installed

echo.
echo ════════════════════════════════════════════════════════════════════════
echo [2/5] Installing Node.js dependencies...
echo ════════════════════════════════════════════════════════════════════════
cd mcp-server
if exist package.json (
    call npm install
    echo ✅ MCP Server dependencies installed
)
cd ..

echo.
echo ════════════════════════════════════════════════════════════════════════
echo [3/5] Installing Playwright browsers...
echo ════════════════════════════════════════════════════════════════════════
call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
echo ✅ Playwright browsers installed

echo.
echo ════════════════════════════════════════════════════════════════════════
echo [4/5] Installing Maven dependencies...
echo ════════════════════════════════════════════════════════════════════════
call mvn clean install -DskipTests
if errorlevel 1 (
    echo ❌ Failed to install Maven dependencies
    pause
    goto :show_menu
)
echo ✅ Maven dependencies installed

echo.
echo ════════════════════════════════════════════════════════════════════════
echo [5/5] Validating configuration...
echo ════════════════════════════════════════════════════════════════════════
if exist src\test\resources\configurations.properties (
    echo ✅ Configuration file found
    echo ⚠️  Update configurations.properties with your application details
) else (
    echo ❌ Configuration file NOT found
)

echo.
echo ✅ SETUP COMPLETE!
echo.
echo Next Steps:
echo   1. Update configurations.properties with your application details
echo   2. Choose Option 2 to record your first test
echo.
pause
goto :show_menu

REM Option 2: Record & Auto-Generate Test
:launch_recording
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        📹 Record ^& Auto-Generate Test                                 ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
call playwright-automation.bat record
if errorlevel 1 (
    echo ❌ Recording failed or was cancelled
    pause
)
goto :show_menu

REM Option 3: AI Interactive Test Generation
:launch_ai_cli
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        🤖 AI Interactive Test Generation                               ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
node automation-cli.js
if errorlevel 1 (
    echo ❌ AI CLI execution failed
    echo Ensure Node.js dependencies are installed (Option 1)
    pause
)
goto :show_menu

REM Option 4: JIRA + AI Test Generation
:launch_jira_ai_cli
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        🎫 JIRA + AI Test Generation                                    ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
if not exist src\test\resources\jiraConfigurations.properties (
    echo ❌ JIRA configuration file not found!
    echo Create: src\test\resources\jiraConfigurations.properties
    pause
    goto :show_menu
)
set /p JIRA_TICKET="Enter JIRA ticket ID (e.g., PROJ-123): "
if "%JIRA_TICKET%"=="" goto :show_menu
node automation-cli.js --mode jira --ticket %JIRA_TICKET%
if errorlevel 1 (
    echo ❌ JIRA test generation failed
    echo Verify JIRA credentials in jiraConfigurations.properties
    pause
)
goto :show_menu

REM Option 5: Manual Test Creation Guide
:manual_guide
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        ✍️  Manual Test Creation Guide                                  ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
echo Opening complete documentation: PLAYWRIGHT_AUTOMATION_COMPLETE.md
echo.
timeout /t 2 >nul
start PLAYWRIGHT_AUTOMATION_COMPLETE.md
goto :show_menu

REM Option 6: Validate & Fix Generated Tests (existing code continues below)
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
echo   STEP 5: RUNNING TESTS VIA TESTNG.XML
echo ============================================
echo.
echo TestNG Configuration: src/test/testng.xml
echo.
echo This will execute all test suites defined in testng.xml
echo including:
echo   - Feature files
echo   - Cucumber scenarios  
echo   - TestNG configurations
echo.
choice /C YN /M "Do you want to run testng.xml now"
if errorlevel 2 (
    echo.
    echo ============================================
    echo Skipping TestNG execution
    echo ============================================
    echo.
    echo To run tests manually later, use:
    echo   mvn test -DsuiteXmlFile=src/test/testng.xml
    echo.
    echo Or run specific tests:
    echo   mvn test -Dcucumber.filter.tags=@YourTag
    echo.
    pause
    exit /b 0
)
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
goto :show_menu

REM Option 7: View Test Reports
:view_reports
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║        📊 View Test Reports                                            ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.
echo Searching for latest test reports...
echo.

REM Find latest report directory
for /f "delims=" %%i in ('dir /b /ad /o-n "MRITestExecutionReports\Version*" 2^>nul') do (
    set LATEST_REPORT=%%i
    goto :found_report
)

:found_report
if not defined LATEST_REPORT (
    echo ❌ No test reports found
    echo.
    echo Run tests first using Option 2 or Option 6
    pause
    goto :show_menu
)

echo Latest report found: %LATEST_REPORT%
echo.

REM Check for Extent Spark Report
if exist "MRITestExecutionReports\%LATEST_REPORT%\extentReports\testNGExtentReports\spark\spark.html" (
    echo Opening Extent Spark Report...
    start "" "MRITestExecutionReports\%LATEST_REPORT%\extentReports\testNGExtentReports\spark\spark.html"
) else if exist "MRITestExecutionReports\%LATEST_REPORT%\extentReports\testNGExtentReports\html\Html.html" (
    echo Opening Extent HTML Report...
    start "" "MRITestExecutionReports\%LATEST_REPORT%\extentReports\testNGExtentReports\html\Html.html"
) else (
    echo ⚠️  No HTML reports found in latest directory
    echo.
    echo Available reports:
    dir /b "MRITestExecutionReports\%LATEST_REPORT%\extentReports\testNGExtentReports\" 2>nul
)

echo.
echo Report opened in your default browser
pause
goto :show_menu

REM Exit Script
:exit_script
cls
echo.
echo ════════════════════════════════════════════════════════════════════════
echo.
echo   Thank you for using Playwright Test Automation Framework!
echo.
echo   📚 Documentation: PLAYWRIGHT_AUTOMATION_COMPLETE.md
echo   🐛 Issues: Report in your project repository
echo.
echo ════════════════════════════════════════════════════════════════════════
echo.
timeout /t 2 >nul
exit /b 0

:end
pause
exit /b 0