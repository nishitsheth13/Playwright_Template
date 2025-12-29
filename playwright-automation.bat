@echo off
setlocal enabledelayedexpansion
REM ============================================================================
REM Playwright Test Automation - Unified Workflow
REM Setup MCP Server, Record Actions, Generate Tests - All in One
REM ============================================================================

REM Check if called with direct action parameter
set "ACTION=%1"
if "%ACTION%"=="record" goto :RECORD_AND_GENERATE
if "%ACTION%"=="setup" goto :SETUP_MCP
if "%ACTION%"=="full" goto :FULL_SETUP
if "%ACTION%"=="validate" goto :VALIDATE_ONLY

:MAIN_MENU
cls
echo.
echo ================================================================
echo   PLAYWRIGHT TEST AUTOMATION - UNIFIED WORKFLOW
echo ================================================================
echo.
echo   Choose your action:
echo.
echo   1. [SETUP] Setup MCP Server (First-time setup)
echo   2. [RECORD] Record ^& Generate Tests (Main workflow)
echo   3. [FULL] Complete Setup + Recording (First-time users)
echo   4. [VALIDATE] Validate ^& Run Existing Tests
echo   0. [EXIT] Exit
echo.
echo ================================================================
set /p MAIN_CHOICE="Enter your choice (0-4): "
echo.

if "%MAIN_CHOICE%"=="1" goto :SETUP_MCP
if "%MAIN_CHOICE%"=="2" goto :RECORD_AND_GENERATE
if "%MAIN_CHOICE%"=="3" goto :FULL_SETUP
if "%MAIN_CHOICE%"=="4" goto :VALIDATE_ONLY
if "%MAIN_CHOICE%"=="0" exit /b 0

echo Invalid choice. Please try again.
timeout /t 2 >nul
goto :MAIN_MENU

REM ============================================================================
REM OPTION 1: SETUP MCP SERVER
REM ============================================================================
:SETUP_MCP
cls
echo.
echo ================================================================
echo   [SETUP] MCP SERVER SETUP
echo ================================================================
echo.

REM Check Node.js installation
echo [1/6] Checking Node.js installation...
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed!
    echo Please install Node.js 18+ from https://nodejs.org/
    pause
    goto :MAIN_MENU
)

for /f "tokens=*" %%i in ('node -v') do set NODE_VERSION=%%i
echo [OK] Node.js found: %NODE_VERSION%
echo.

REM Check npm installation
echo [2/6] Checking npm installation...
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm is not installed!
    pause
    goto :MAIN_MENU
)

for /f "tokens=*" %%i in ('npm -v') do set NPM_VERSION=%%i
echo [OK] npm found: %NPM_VERSION%
echo.

REM Navigate to MCP server directory
echo [3/6] Installing MCP server dependencies...
cd mcp-server
if %errorlevel% neq 0 (
    echo [ERROR] mcp-server directory not found!
    pause
    cd ..
    goto :MAIN_MENU
)

call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install dependencies
    cd ..
    pause
    goto :MAIN_MENU
)
echo [OK] Dependencies installed
echo.

REM Build TypeScript project
echo [4/6] Building TypeScript project...
call npm run build
if %errorlevel% neq 0 (
    echo [ERROR] Build failed
    cd ..
    pause
    goto :MAIN_MENU
)
echo [OK] Build successful
cd ..
echo.

REM Verify project structure
echo [5/6] Verifying project structure...
if not exist "src\main\java\pages" (
    echo [ERROR] Missing directory: src\main\java\pages
    pause
    goto :MAIN_MENU
)
if not exist "src\test\java\features" (
    echo [ERROR] Missing directory: src\test\java\features
    pause
    goto :MAIN_MENU
)
if not exist "src\test\java\stepDefs" (
    echo [ERROR] Missing directory: src\test\java\stepDefs
    pause
    goto :MAIN_MENU
)
if not exist "mcp-server\dist" (
    echo [ERROR] Missing directory: mcp-server\dist
    pause
    goto :MAIN_MENU
)
echo [OK] Project structure verified
echo.

REM Create VS Code MCP configuration
echo [6/6] Creating VS Code MCP configuration...
if not exist ".vscode" mkdir .vscode

set PROJECT_ROOT=%CD%
set PROJECT_ROOT=%PROJECT_ROOT:\=\\%

(
echo ^{
echo   "mcpServers": ^{
echo     "playwright-automation": ^{
echo       "command": "node",
echo       "args": [
echo         "%PROJECT_ROOT%\\mcp-server\\dist\\index.js"
echo       ],
echo       "env": ^{
echo         "PROJECT_ROOT": "%PROJECT_ROOT%"
echo       ^}
echo     ^}
echo   ^}
echo ^}
) > .vscode\mcp-settings.json

echo [OK] VS Code MCP configuration created
echo.

REM Compile Java project (optional)
echo [OPTIONAL] Compiling Java project...
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    call mvn clean compile test-compile -q
    if %errorlevel% equ 0 (
        echo [OK] Java project compiled successfully
    ) else (
        echo [WARN] Java compilation had warnings
    )
) else (
    echo [SKIP] Maven not found - skipping Java compilation
)
echo.

echo ============================================
echo           SETUP COMPLETE!
echo ============================================
echo.
echo Next Steps:
echo   1. Restart VS Code to activate MCP server
echo   2. Return to main menu and choose "Record ^& Generate Tests"
echo.
pause
goto :MAIN_MENU

REM ============================================================================
REM OPTION 2: RECORD AND GENERATE TESTS
REM ============================================================================
:RECORD_AND_GENERATE
cls
echo.
echo ================================================================
echo   [RECORD] PLAYWRIGHT RECORDER - Auto-Generate Test Scripts
echo ================================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found. Please install Maven first.
    pause
    goto :MAIN_MENU
)

echo [INFO] Using Pure Java file generator (Node.js not required)
echo.

REM Get test details from user
echo [INFO] Enter test details:
echo.

set /p FEATURE_NAME="Feature Name (e.g., Login, Profile): "

echo.
echo [URL OPTIONS]
echo 1. Use config URL + path (Config: https://uksestdevtest02.ukest.lan/MRIEnergy/)
echo 2. Enter completely custom full URL
echo.
set /p URL_CHOICE="Choose option (1 or 2, default=1): "

if "%URL_CHOICE%"=="2" (
    echo.
    echo Enter COMPLETE URL including https://
    set /p CUSTOM_URL="Full URL: "
    set "PAGE_URL="
) else (
    echo.
    echo Enter ONLY the path to append to config URL
    echo Examples: /start-page, /login, /profile, or press Enter to skip
    set /p PAGE_URL="Path only: "
    set "CUSTOM_URL="
)

set /p JIRA_STORY="JIRA Story ID (optional, press Enter to skip): "

REM Validate inputs
if "%FEATURE_NAME%"=="" (
    echo [ERROR] Feature name is required
    pause
    goto :MAIN_MENU
)

if "%JIRA_STORY%"=="" (
    set JIRA_STORY=AUTO-GEN
)

REM Create output directory for recording
set RECORDING_DIR=temp_recording_%RANDOM%
mkdir "%RECORDING_DIR%" 2>nul

if not exist "%RECORDING_DIR%" (
    echo [ERROR] Failed to create recording directory: %RECORDING_DIR%
    pause
    goto :MAIN_MENU
)

echo.
echo ================================================================
echo [STEP 1] RECORD ACTIONS
echo ================================================================
echo.
echo Instructions:
echo   1. Browser will open with Playwright Inspector
echo   2. Perform your test actions (click, type, navigate)
echo   3. Inspector will record all actions automatically
echo   4. Close browser when done to save recording
echo.

REM Determine recording URL
echo ================================================================
echo [INFO] URL Configuration Setup
echo ================================================================
echo.

if not "%CUSTOM_URL%"=="" (
    set "RECORDING_URL=%CUSTOM_URL%"
    echo   Mode: Custom URL
    echo   Recording URL: %RECORDING_URL%
) else (
    echo   Mode: Config URL + Path
    echo.
    echo   [Step 1] Extracting base URL from configurations.properties...
    
    set "BASE_URL="
    set "SCRIPT_DIR=%~dp0"
    
    for /f "usebackq tokens=2 delims==" %%a in (`findstr /b "URL=" "%SCRIPT_DIR%src\test\resources\configurations.properties"`) do (
        set "BASE_URL=%%a"
    )

    if not "!BASE_URL!"=="" (
        set "BASE_URL=!BASE_URL:\:=:!"
        echo   [OK] URL extracted: !BASE_URL!
    )
    
    if "!BASE_URL!"=="" (
        echo   [WARNING] Could not read URL from configurations.properties
        echo   [WARNING] Using fallback URL: http://localhost:8080
        set "BASE_URL=http://localhost:8080"
    )
    
    echo.
    echo   [Step 2] Processing path...
    
    if not "!BASE_URL!"=="" (
        if "!BASE_URL:~-1!"=="/" (
            set "BASE_URL=!BASE_URL:~0,-1!"
        )
    )
    
    if not "!PAGE_URL!"=="" (
        set "PAGE_URL=!PAGE_URL: =!"
        if not "!PAGE_URL:~0,1!"=="/" set "PAGE_URL=/!PAGE_URL!"
        echo   [OK] Path provided: !PAGE_URL!
        set "RECORDING_URL=!BASE_URL!!PAGE_URL!"
    ) else (
        echo   [INFO] No path provided - using base URL only
        set "RECORDING_URL=!BASE_URL!"
    )
)

echo ================================================================
echo   [SUCCESS] RECORDING URL: !RECORDING_URL!
echo ================================================================
echo.

REM Install Playwright browsers
echo [INFO] Installing Playwright Browsers...
call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium" -D exec.cleanupDaemonThreads=false >nul 2>&1

echo.
echo [INFO] Starting Playwright Recorder
echo.
echo Opening browser for recording...
pause
echo.

REM Run Playwright codegen
call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen --target java -o %RECORDING_DIR%\recorded-actions.java !RECORDING_URL!" -D exec.cleanupDaemonThreads=false

if not exist "%RECORDING_DIR%\recorded-actions.java" (
    echo [ERROR] Recording file NOT found!
    pause
    goto :MAIN_MENU
)

echo [SUCCESS] Recording completed!
echo.

REM Compile generator
echo ================================================================
echo [STEP 2] COMPILE JAVA GENERATOR
echo ================================================================
echo.

if not exist "target\classes\configs\TestGeneratorHelper.class" (
    echo [INFO] Compiling TestGeneratorHelper.java...
    call mvn compile -q -Dcheckstyle.skip=true
    if errorlevel 1 (
        echo [ERROR] Failed to compile Java generator
        pause
        goto :MAIN_MENU
    )
    echo [SUCCESS] Java generator compiled
) else (
    echo [OK] Java generator already compiled
)
echo.

REM Generate test files
echo ================================================================
echo [STEP 3] GENERATE TEST FILES
echo ================================================================
echo.

java -cp "target/classes;%USERPROFILE%\.m2\repository\com\microsoft\playwright\playwright\1.49.0\playwright-1.49.0.jar" ^
    configs.TestGeneratorHelper "%RECORDING_DIR%\recorded-actions.java" "%FEATURE_NAME%" "%PAGE_URL%" "%JIRA_STORY%"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java file generation failed!
    pause
    goto :MAIN_MENU
)

echo [SUCCESS] Test files generated successfully!
echo.

REM Compile project with smart error detection and auto-fix
echo ================================================================
echo [STEP 4] COMPILING PROJECT WITH AUTO-FIX
echo ================================================================
echo.

set MAX_COMPILE_ATTEMPTS=3
set COMPILE_ATTEMPT=0

:compile_with_autofix
set /a COMPILE_ATTEMPT+=1
echo [Attempt %COMPILE_ATTEMPT%/%MAX_COMPILE_ATTEMPTS%] Compiling...
echo.

REM Compile and capture output to log file (Windows compatible)
call mvn clean compile test-compile > target\compile-errors.log 2>&1
set COMPILE_RESULT=%ERRORLEVEL%

REM Show compilation output
type target\compile-errors.log
echo.

if %COMPILE_RESULT% EQU 0 (
    echo [SUCCESS] Compilation successful!
    goto :compilation_success
)

REM Compilation failed - apply smart fixes
echo.
echo [DETECTED] Compilation errors - Analyzing and applying fixes...
echo.
set FIX_APPLIED=0

REM Fix 1: Missing imports
echo [FIX 1/8] Checking missing imports...
findstr /C:"cannot find symbol" target\compile-errors.log >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Detected missing symbols - Adding common imports...
    set FIX_APPLIED=1
    
    REM Add missing Page import
    findstr /C:"symbol:   class Page" target\compile-errors.log >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "Get-ChildItem -Path src -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'import com.microsoft.playwright.Page' -and $content -match 'Page page') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport com.microsoft.playwright.Page;\" ; Set-Content $_.FullName $content } }"
        echo   └─ Added: import com.microsoft.playwright.Page;
    )
    
    REM Add missing browserSelector import
    findstr /C:"browserSelector" target\compile-errors.log >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "Get-ChildItem -Path src\test\java\stepDefs -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'import configs.browserSelector' -and $content -match 'extends browserSelector') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport configs.browserSelector;\" ; Set-Content $_.FullName $content } }"
        echo   └─ Added: import configs.browserSelector;
    )
    
    REM Add missing Cucumber imports
    findstr /C:"Given" target\compile-errors.log >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "Get-ChildItem -Path src\test\java\stepDefs -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'import io.cucumber.java.en' -and $content -match '@(Given|When|Then|And)') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport io.cucumber.java.en.*;\" ; Set-Content $_.FullName $content } }"
        echo   └─ Added: import io.cucumber.java.en.*;
    )
    
    REM Add missing login page import
    findstr /C:"symbol:   variable login" target\compile-errors.log >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "Get-ChildItem -Path src\test\java\stepDefs -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'import pages.login' -and $content -match 'login\s+login') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport pages.login;\" ; Set-Content $_.FullName $content } }"
        echo   └─ Added: import pages.login;
    )
) else (
    echo   └─ No missing import issues detected
)

REM Fix 2: Duplicate methods
echo [FIX 2/8] Checking duplicate methods...
findstr /C:"already defined in class" target\compile-errors.log >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Detected duplicate methods - Removing duplicates...
    set FIX_APPLIED=1
    powershell -Command "$files = Get-ChildItem -Path src -Recurse -Include *.java; foreach ($file in $files) { $lines = Get-Content $file.FullName; $methods = @{}; $output = @(); $skipUntilCloseBrace = $false; $braceCount = 0; foreach ($line in $lines) { if ($skipUntilCloseBrace) { if ($line -match '\{') { $braceCount++ } if ($line -match '\}') { $braceCount--; if ($braceCount -le 0) { $skipUntilCloseBrace = $false } } continue } if ($line -match 'public static void (\w+)\(') { $methodName = $matches[1]; if ($methods.ContainsKey($methodName)) { $skipUntilCloseBrace = $true; $braceCount = 0; continue } else { $methods[$methodName] = $true } } $output += $line } Set-Content $file.FullName $output }"
    echo   └─ Removed duplicate method definitions
) else (
    echo   └─ No duplicate method issues detected
)

REM Fix 3: Protected methods
echo [FIX 3/8] Checking protected methods...
findstr /S /M "protected static" src\main\java\pages\*.java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Converting protected to public...
    set FIX_APPLIED=1
    powershell -Command "Get-ChildItem -Path src\main\java\pages\*.java | ForEach-Object { (Get-Content $_.FullName) -replace 'protected static', 'public static' | Set-Content $_.FullName }"
    echo   └─ Changed: protected static → public static
) else (
    echo   └─ No protected method issues detected
)

REM Fix 4: Missing extends BasePage
echo [FIX 4/8] Checking BasePage inheritance...
findstr /C:"cannot find symbol" target\compile-errors.log | findstr /C:"BasePage" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Adding extends BasePage to page objects...
    set FIX_APPLIED=1
    powershell -Command "Get-ChildItem -Path src\main\java\pages -Include *.java -Exclude BasePage.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'extends BasePage' -and $content -match 'public class (\w+)') { $className = $matches[1]; $content = $content -replace \"public class $className \{\" , \"public class $className extends BasePage {\" ; Set-Content $_.FullName $content } }"
    echo   └─ Added: extends BasePage
) else (
    echo   └─ No BasePage inheritance issues detected
)

REM Fix 5: Missing extends browserSelector
echo [FIX 5/8] Checking browserSelector inheritance...
findstr /C:"page has private access" target\compile-errors.log >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Adding extends browserSelector to step definitions...
    set FIX_APPLIED=1
    powershell -Command "Get-ChildItem -Path src\test\java\stepDefs -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -notmatch 'extends browserSelector' -and $content -match 'public class (\w+)') { $className = $matches[1]; $content = $content -replace \"public class $className \{\" , \"public class $className extends browserSelector {\" ; if ($content -notmatch 'import configs.browserSelector') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport configs.browserSelector;\" } Set-Content $_.FullName $content } }"
    echo   └─ Added: extends browserSelector
) else (
    echo   └─ No browserSelector inheritance issues detected
)

REM Fix 6: Unused imports
echo [FIX 6/8] Cleaning up imports...
powershell -Command "Get-ChildItem -Path src -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; $content = $content -replace '(?m)^import [^;]+;\s*$(?=\s*import [^;]+;\s*$)', ''; Set-Content $_.FullName $content }"
echo   └─ Removed duplicate imports

REM Fix 7: Static imports for utils methods
echo [FIX 7/8] Checking static imports...
findstr /C:"non-static method" target\compile-errors.log >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   └─ Adding static imports for utils...
    set FIX_APPLIED=1
    powershell -Command "Get-ChildItem -Path src\main\java\pages -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'clickOnElement|enterText|selectDropDownValue' -and $content -notmatch 'import static configs.utils') { $content = $content -replace '(package [^;]+;)', \"`$1`r`nimport static configs.utils.*;\" ; Set-Content $_.FullName $content } }"
    echo   └─ Added: import static configs.utils.*;
) else (
    echo   └─ No static import issues detected
)

REM Fix 8: Method name capitalization
echo [FIX 8/8] Fixing method name conventions...
powershell -Command "Get-ChildItem -Path src -Recurse -Include *.java | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'public static void [A-Z]') { $content = $content -replace 'public static void ([A-Z])(\w+)', { param($match) 'public static void ' + $match.Groups[1].Value.ToLower() + $match.Groups[2].Value }; Set-Content $_.FullName $content } }"
echo   └─ Fixed method naming (camelCase)

echo.
if %FIX_APPLIED% EQU 1 (
    echo [INFO] Fixes applied - Retrying compilation...
) else (
    echo [WARNING] No automatic fixes available for detected errors
    echo [INFO] Checking error log for manual resolution...
    type target\compile-errors.log | findstr /C:"error:" /C:"ERROR"
)
echo.

if %COMPILE_ATTEMPT% LSS %MAX_COMPILE_ATTEMPTS% goto :compile_with_autofix

REM Max attempts reached
echo ================================================================
echo [ERROR] Maximum compilation attempts reached (%MAX_COMPILE_ATTEMPTS%)
echo ================================================================
echo.
echo Please check: target\compile-errors.log
echo.
echo Common remaining issues:
echo   - Complex type mismatches
echo   - Missing method implementations
echo   - Incorrect method signatures
echo.
pause
goto :MAIN_MENU

:compilation_success
echo.

REM Ask user if they want to run tests
echo ================================================================
echo [STEP 5] RUN TESTS?
echo ================================================================
echo.
echo Test files have been generated and compiled successfully!
echo.
echo Generated Files:
echo   - src/main/java/pages/%FEATURE_NAME%.java
echo   - src/test/java/features/%FEATURE_NAME%.feature
echo   - src/test/java/stepDefs/%FEATURE_NAME%Steps.java
echo.
choice /C YN /M "Do you want to run tests via testng.xml now"
if errorlevel 2 goto :SKIP_TEST_RUN
if errorlevel 1 goto :RUN_TESTS

:RUN_TESTS
echo.
echo ================================================================
echo [RUNNING TESTS VIA TESTNG.XML]
echo ================================================================
echo.
echo Running: mvn test -DsuiteXmlFile=src/test/testng.xml
echo.

call mvn test -DsuiteXmlFile=src/test/testng.xml

echo.
echo ================================================================
echo [TEST EXECUTION COMPLETE]
echo ================================================================
echo.
echo Reports: MRITestExecutionReports\Version*\extentReports\
echo.
pause
goto :MAIN_MENU

:SKIP_TEST_RUN
echo.
echo ================================================================
echo [SUCCESS] GENERATION COMPLETE!
echo ================================================================
echo.
echo Feature: %FEATURE_NAME%
echo JIRA Story: %JIRA_STORY%
echo.
echo To run tests later:
echo   mvn test -DsuiteXmlFile=src/test/testng.xml
echo   OR
echo   mvn test -Dcucumber.filter.tags=@%FEATURE_NAME%
echo.
pause
goto :MAIN_MENU

REM ============================================================================
REM OPTION 3: FULL SETUP (Setup + Record)
REM ============================================================================
:FULL_SETUP
cls
echo.
echo ================================================================
echo   [FULL SETUP] Complete Setup + Recording Workflow
echo ================================================================
echo.
echo This will:
echo   1. Setup MCP Server (if needed)
echo   2. Compile project
echo   3. Start recording workflow
echo.
pause

REM Check if MCP is already set up
if exist "mcp-server\dist\index.js" (
    echo [INFO] MCP Server already set up - skipping setup
    echo.
) else (
    echo [INFO] MCP Server not found - running setup...
    call :SETUP_MCP
)

REM Proceed with recording
echo.
echo [INFO] Proceeding to recording workflow...
timeout /t 2 >nul
goto :RECORD_AND_GENERATE

REM ============================================================================
REM OPTION 4: VALIDATE ONLY
REM ============================================================================
:VALIDATE_ONLY
cls
echo.
echo ================================================================
echo   [VALIDATE] Validate ^& Run Tests
echo ================================================================
echo.

REM Check Maven
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found!
    pause
    goto :MAIN_MENU
)

echo [1/3] Compiling project...
call mvn clean compile test-compile -q

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    goto :MAIN_MENU
)

echo [SUCCESS] Compilation successful
echo.

echo [2/3] Running tests...
call mvn test -DsuiteXmlFile=src/test/testng.xml

echo.
echo [3/3] Check reports in: MRITestExecutionReports\
echo.
pause
goto :MAIN_MENU
