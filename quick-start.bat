@echo off
REM ============================================================================
REM PLAYWRIGHT AUTOMATION - ALL-IN-ONE UNIFIED SCRIPT
REM ============================================================================
REM
REM VERSION: 3.0 (Unified - Everything in One File)
REM CREATED: February 9, 2026
REM
REM FEATURES:
REM   ✓ Test recording and auto-generation
REM   ✓ JIRA integration
REM   ✓ AI-assisted test creation
REM   ✓ Maven build and test execution
REM   ✓ Java code validation (embedded PowerShell)
REM   ✓ Project setup and configuration
REM   ✓ Cucumber tag filtering
REM   ✓ Report management
REM   ✓ All utilities embedded (no external files needed)
REM
REM USAGE:
REM   Simply run: automation-all-in-one.bat
REM   Or double-click in Windows Explorer
REM
REM DEPENDENCIES:
REM   - Java JDK 11+ (required)
REM   - Maven 3.6+ (required)
REM   - Node.js 14+ (optional, for AI features)
REM   - PowerShell 5.1+ (built-in Windows)
REM
REM NOTES:
REM   - This is a COMPLETE, self-contained automation solution
REM   - No external .bat or .ps1 files required
REM   - All previous functionality preserved and enhanced
REM   - Portable and easy to share
REM
REM ============================================================================

setlocal enabledelayedexpansion

:menu
cls
echo.
echo ================================================================
echo.
echo        AI Test Automation - Main Menu
echo.
echo ================================================================
echo.
echo  TEST GENERATION:
echo   1. [RECORD] Record ^& Auto-Generate (Fastest - 5-10 min)
echo   1B. [RETRY] Retry from Existing Recording
echo   2. [JIRA] Generate from JIRA Story
echo   3. [AI CLI] AI-Assisted Interactive (Node.js required)
echo   4. [VALIDATE] Validate ^& Run Tests
echo.
echo  SETUP:
echo   S. [SETUP] Complete Project Setup with MCP Server
echo.
echo  UTILITIES:
echo   5. Maven Clean Compile
echo   6. Maven Clean Test
echo   7. Run Specific Tag Tests
echo   8. Quick Java Validation ^& Fix
echo.
echo  HELP:
echo   H. Show Help
echo   0. Exit
echo.
echo ================================================================
echo.

set /p choice="Enter your choice: "

if /i "%choice%"=="1" goto recording
if /i "%choice%"=="1B" goto retry_recording
if /i "%choice%"=="2" goto jira_story
if /i "%choice%"=="3" goto ai_cli
if /i "%choice%"=="4" goto test_validation
if /i "%choice%"=="S" goto project_setup
if /i "%choice%"=="5" goto maven_compile
if /i "%choice%"=="6" goto maven_test
if /i "%choice%"=="7" goto run_tests
if /i "%choice%"=="8" goto quick_validate
if /i "%choice%"=="H" goto help
if /i "%choice%"=="0" goto exit
echo.
echo Invalid choice. Please try again.
timeout /t 2 >nul
goto menu

:recording
cls
echo.
echo ================================================================
echo [RECORD] Starting Playwright Recording
echo ================================================================
echo.
echo.
echo +================================================================+
echo ^|   PLAYWRIGHT RECORDER - Auto-Generate Test Scripts          ^|
echo +================================================================+
echo.
echo [INFO] Using Pure Java file generator (Node.js not required)
echo.
echo.
echo  Enter test details:
echo.

REM ============================================================================
REM Get and validate feature name (required field)
REM ============================================================================
set /p feature="Feature Name (e.g., Login, Profile): "

REM Validate feature name is not empty
if "!feature!"=="" (
    echo.
    echo [ERROR] Feature name is required and cannot be empty
    echo [INFO] Please provide a descriptive name for your test feature
    pause
    goto menu
)

REM ============================================================================
REM Get and validate URL configuration
REM ============================================================================
echo.
echo [URL OPTIONS]
echo 1. Use base URL from config + custom path
echo    Base: https://uksestdevtest02.ukest.lan/MRIEnergy/
echo 2. Enter completely custom full URL
echo.
set /p url_choice="Choose option (1 or 2, default=1): "

REM Default to option 1 if no choice provided
if "!url_choice!"=="" set "url_choice=1"

REM Validate choice is 1 or 2
if not "!url_choice!"=="1" if not "!url_choice!"=="2" (
    echo [WARN] Invalid option '!url_choice!', defaulting to option 1
    set "url_choice=1"
)

if "!url_choice!"=="1" (
    echo.
    echo Enter ONLY the path to append to config URL
    echo Examples: /start-page, /login, /profile, or press Enter to skip
    set /p url_path="Path only: "
    REM Combine config URL with path (remove trailing slash from BASE_URL first)
    set "BASE_URL=https://uksestdevtest02.ukest.lan/MRIEnergy"
    REM Remove trailing slash if exists
    if "!BASE_URL:~-1!"=="/" set "BASE_URL=!BASE_URL:~0,-1!"
    if "!url_path!"=="" (
        set "final_url=!BASE_URL!"
    ) else (
        REM Ensure path starts with /
        if not "!url_path:~0,1!"=="/" set "url_path=/!url_path!"
        set "final_url=!BASE_URL!!url_path!"
    )
)

if "!url_choice!"=="2" (
    echo.
    set /p final_url="Enter full URL: "
)

if not "!url_choice!"=="1" if not "!url_choice!"=="2" (
    echo.
    echo [ERROR] Invalid choice!
    pause
    goto menu
)

echo.
set /p jira_id="JIRA Story ID (optional, press Enter to skip): "
if "!jira_id!"=="" set "jira_id=AUTO-GEN"

REM Create temp directory for recording
set "RECORDING_DIR=temp_recording_%RANDOM%"
mkdir "!RECORDING_DIR!" 2>nul

echo.
echo ================================================================
echo  Step 1: RECORD ACTIONS (Browser Recording)
echo ================================================================
echo.
echo  WHAT WILL HAPPEN:
echo   1. Playwright Inspector window will open (recording toolbar)
echo   2. Chromium browser will open automatically
echo   3. Your URL will be loaded: !final_url!
echo   4. Any actions you perform will be recorded as Java code
echo.
echo  HOW TO RECORD:
echo   * Click buttons -> Recorded as .click()
echo   * Fill text fields -> Recorded as .fill()
echo   * Select dropdowns -> Recorded as .selectOption()
echo   * Navigate pages -> Recorded as .navigate()
echo.
echo [OK]  IMPORTANT INSTRUCTIONS:
echo   * DO: Perform your test actions naturally
echo   * DO: Close the BROWSER window when done (not Inspector)
echo   X DON'T: Close this command window during recording
echo   X DON'T: Close Inspector window (closes automatically)
echo.
echo Starting Playwright Codegen Recorder
echo.

REM Run Playwright codegen to record actions with improved error handling
echo [INFO] Launching Playwright Recorder Please wait
echo.

REM Check if Playwright CLI is accessible
echo [PRE-CHECK] Verifying Playwright CLI and browsers
echo.

REM ============================================================================
REM Validate Playwright CLI accessibility
REM ============================================================================
REM Attempt to access Playwright CLI via Maven (silent mode)
call mvn -q exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="--version" >nul 2>&1

REM Check if CLI invocation succeeded (exit code 0 = accessible)
if !ERRORLEVEL! GTR 0 (
    echo [OK] Playwright CLI not accessible
    echo    Maven dependencies may not be installed
    echo.
    echo [AUTO-FIX] Downloading Maven dependencies
    echo    Please wait, this may take 1-2 minutes
    echo.
    REM Use compile instead of install to avoid cucumber-reporting plugin issues
    call mvn clean compile -DskipTests 2>&1 | findstr /V /C:"Downloading from" /C:"Downloaded from" /C:"Progress"
    
    set "COMPILE_CODE=!ERRORLEVEL!"
    if !COMPILE_CODE! GTR 0 (
        echo.
        echo ERROR: Maven compile failed
        echo    Try manually: mvn clean compile
        pause
        goto menu
    )
    
    echo.
    echo [OK] Maven dependencies installed
    echo.
    
    REM RECHECK: Verify CLI is now accessible after dependency installation
    echo [RECHECK] Verifying Playwright CLI is now accessible
    call mvn -q exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="--version" >nul 2>&1
    if !ERRORLEVEL! GTR 0 (
        echo.
        echo ERROR: Playwright CLI still not accessible after dependency installation!
        echo.
        echo This is unusual. Possible issues:
        echo   1. Maven dependency resolution failed
        echo   2. Playwright artifact corrupted
        echo   3. Java classpath issues
        echo.
        echo Try these commands manually:
        echo   1. mvn clean install -DskipTests
        echo   2. mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="--version"
        echo.
        pause
        goto menu
    )
    echo [OK] Playwright CLI is now accessible
    echo.
) else (
    echo [OK] Playwright CLI accessible
    echo.
)

REM Note: Browser check via --dry-run is unreliable as it always shows installation paths
REM Instead, let codegen attempt to launch and handle browser installation if needed

echo.
REM Launch Playwright codegen with explicit browser (chromium by default)
echo ================================================================
echo  LAUNCHING PLAYWRIGHT CODEGEN
echo ================================================================
echo.
echo [INFO] Starting browser recorder (Chromium)
echo [INFO] Target URL: !final_url!
echo [INFO] Output: !RECORDING_DIR!\recorded-actions.java
echo.
echo  Please wait for browser and inspector to open
echo    This may take 5-15 seconds on first launch
echo.
echo [DEBUG] Final URL value: [!final_url!]
echo.

if "!final_url!"=="" (
    echo [DEBUG] Launching codegen without URL
    call mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="codegen --target java --browser chromium --ignore-https-errors --output !RECORDING_DIR!\recorded-actions.java"
) else (
    echo [DEBUG] Launching codegen with URL: !final_url!
    call mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="codegen \"!final_url!\" --target java --browser chromium --ignore-https-errors --output !RECORDING_DIR!\recorded-actions.java"
)
REM Capture exit code immediately
REM ============================================================================
REM Capture and validate recorder exit code
REM ============================================================================
set "RECORDER_EXIT_CODE=!ERRORLEVEL!"

echo.
echo ================================================================
echo [INFO] Recorder session ended - Exit code: !RECORDER_EXIT_CODE!
echo ================================================================
echo.
echo [INFO] Recording directory: "!RECORDING_DIR!"

REM Validate recorder completed successfully (exit code 0 = success)
if !RECORDER_EXIT_CODE! GTR 0 (
    echo.
    echo ================================================================
    echo ERROR: RECORDING FAILED
    echo ================================================================
    echo.
    echo Exit code: !RECORDER_EXIT_CODE!
    echo.
    echo  TROUBLESHOOTING STEPS:
    echo.
    echo [1] Check if browsers are installed:
    echo     mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --dry-run"
    echo.
    echo [2] Reinstall browsers:
    echo     mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install chromium"
    echo.
    echo [3] Test browser directly:
    echo     mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="open chromium"
    echo.
    echo [4] Common issues:
    echo     * Antivirus blocking browser execution
    echo     * Another Chrome/Chromium instance running
    echo     * Insufficient disk space
    echo     * Windows Defender SmartScreen blocking
    echo.
    echo [5] Try alternative browser:
    echo     mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="codegen --browser firefox"
    echo.
    
    choice /C YN /M "Do you want to try manual browser installation now"
    if errorlevel 2 (
        rmdir /s /q "!RECORDING_DIR!" 2>nul
        pause
        goto menu
    )
    if errorlevel 1 (
        echo.
        echo Installing browsers (this may take a few minutes)
        call mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
        echo.
        echo Installation complete. Please try Option 1 again.
        pause
        rmdir /s /q "!RECORDING_DIR!" 2>nul
        goto menu
    )
)

REM ============================================================================
REM CHECK 1: Validate Recording File Creation
REM ============================================================================
echo [CHECK 1] Verifying recording file exists
echo [INFO] Expected location: !RECORDING_DIR!\recorded-actions.java

REM Verify file was created by recorder
if not exist "!RECORDING_DIR!\recorded-actions.java" (
    echo.
    echo ================================================================
    echo ERROR: NO RECORDING FILE FOUND
    echo ================================================================
    echo.
    echo Expected file: !RECORDING_DIR!\recorded-actions.java
    echo.
    echo  Directory contents:
    dir /b "!RECORDING_DIR!" 2>nul
    echo.
    echo  POSSIBLE REASONS:
    echo   1. Browser was closed too quickly without any actions
    echo   2. Recording was cancelled before saving
    echo   3. File permission issues in the directory
    echo   4. Playwright codegen didn't create the output file
    echo.
    echo  SOLUTION:
    echo   - Try again and perform at least one action before closing
    echo   - Make sure to close the browser window, not just minimize
    echo   - Wait for "Recording stopped" message in the Inspector
    echo   - Check if antivirus is blocking file creation
    echo.
    pause
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    goto menu
)
echo [OK] Recording file exists

REM Show first few lines to verify content
echo [DEBUG] Recording file preview - first 10 lines
type "!RECORDING_DIR!\recorded-actions.java" 2>nul | findstr /N "." | findstr /R "^[1-9]: ^10:"
echo.

REM ============================================================================
REM CHECK 2: Validate Recording File Size
REM ============================================================================
echo [CHECK 2] Verifying recording file size

REM Extract file size in bytes
for %%A in ("!RECORDING_DIR!\recorded-actions.java") do set "size=%%~zA"

REM Validate size variable is set
if not defined size (
    echo [WARN] Unable to determine file size - setting to 0
    set "size=0"
)

REM Convert to numeric value for comparison (minimum threshold: 100 bytes)
set /a "sizeNum=!size!" 2>nul
if !ERRORLEVEL! GTR 0 (
    echo [WARN] Size conversion failed - treating as invalid
    set "sizeNum=0"
)

echo [INFO] File size: !sizeNum! bytes (minimum required: 100 bytes)

REM Validate file meets minimum size requirement
if !sizeNum! GEQ 100 (
    echo [OK] Size validation passed
    goto :check3_actions
)

REM File size below threshold - recording likely empty or incomplete
echo.
echo ================================================================
echo ERROR: RECORDING FILE TOO SMALL
echo ================================================================
echo.
echo File size: !size! bytes (expected: 100+ bytes)
echo Location: !RECORDING_DIR!\recorded-actions.java
echo.
echo  POSSIBLE REASONS:
echo   1. No actions were recorded
echo   2. Browser was closed immediately after opening
echo   3. Recording was interrupted
echo.
echo  SOLUTION:
echo   - Record at least one action (click, type, navigate)
echo   - Close the browser AFTER performing actions
echo   - Make sure Inspector shows "Recording" status
echo   - DON'T use Ctrl+C - close browser window normally
echo.
pause
rmdir /s /q "!RECORDING_DIR!" 2>nul
goto menu

:check3_actions
echo [CHECK 3] Verifying recording contains actions
findstr /C:"page." "!RECORDING_DIR!\recorded-actions.java" >nul 2>&1
if errorlevel 1 (
    echo.
    echo ================================================================
    echo [OK]  NO PLAYWRIGHT ACTIONS FOUND
    echo ================================================================
    echo.
    echo File exists but contains no page.* actions
    echo.
    echo  File contents:
    type "!RECORDING_DIR!\recorded-actions.java"
    echo.
    echo  POSSIBLE REASONS:
    echo   1. Codegen didn't record any actions
    echo   2. File was created but recording failed
    echo   3. Wrong output format
    echo.
    echo  SOLUTION:
    echo   - Perform clicks, typing, or navigation in the browser
    echo   - Ensure Playwright Inspector shows recorded actions
    echo   - Try recording again with visible actions
    echo.
    pause
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    goto menu
)
echo [OK] Recording contains Playwright actions

echo.
echo [SUCCESS] Recording captured ^(!size! bytes^) with valid actions

echo.
echo ================================================================
echo  Step 2: GENERATE TEST FILES
echo ================================================================
echo.

REM Get absolute path and convert to forward slashes
for %%F in ("!RECORDING_DIR!\recorded-actions.java") do set "RECORDING_FILE=%%~fF"
set "RECORDING_FILE=!RECORDING_FILE:\=/!"

echo [DEBUG] Recording file: !RECORDING_FILE!
echo [DEBUG] Feature: !feature!
echo [DEBUG] URL: !final_url!
echo [DEBUG] JIRA: !jira_id!
echo.

REM Generate test files from recording
echo [INFO] Starting test file generation
echo [DEBUG] Command arguments:
echo   Recording: !RECORDING_FILE!
echo   Feature:   !feature!
echo   URL:       !final_url!
echo   JIRA:      !jira_id!
echo.
echo [DEBUG] Calling Maven exec:java - please wait
echo [DEBUG] Full command: mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" -Dexec.args="\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\""
echo.

REM Capture Maven output to extract the fixed feature name
set "MAVEN_LOG=maven_output_%RANDOM%.txt"
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" -Dexec.args="\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\"" > "!MAVEN_LOG!" 2>&1
type "!MAVEN_LOG!"

REM ============================================================================
REM Extract the auto-fixed feature name from Maven output
REM ============================================================================
set "FIXED_FEATURE=!feature!"
for /f "tokens=5 delims=: " %%a in ('findstr /C:"[INFO] Feature name (fixed):" "!MAVEN_LOG!"') do (
    set "FIXED_FEATURE=%%a"
)
del "!MAVEN_LOG!" 2>nul

if not "!FIXED_FEATURE!"=="!feature!" (
    echo.
    echo [DEBUG] Feature name was auto-fixed: '!feature!' -^> '!FIXED_FEATURE!'
    set "feature=!FIXED_FEATURE!"
    echo [DEBUG] Using fixed feature name: !feature!
)

REM ============================================================================
REM Validate test generation results
REM ============================================================================
set "GEN_EXIT_CODE=!ERRORLEVEL!"

echo.
echo ================================================================
echo [DEBUG] Maven process completed
echo ================================================================
echo [DEBUG] ERRORLEVEL = !ERRORLEVEL!
echo [DEBUG] GEN_EXIT_CODE = !GEN_EXIT_CODE!
echo [DEBUG] Recording source: !RECORDING_FILE!
echo [DEBUG] Feature name: !feature!
echo.
echo [INFO] Checking if files were actually generated (case-insensitive)...
echo.

REM ============================================================================
REM Verify all generated files exist - CASE INSENSITIVE
REM ============================================================================
echo.
echo [SUCCESS] Test generation completed successfully!
echo.
echo [INFO] Verifying generated files (case-insensitive search for !feature!)
echo.

set "filesOK=true"
set "FOUND_COUNT=0"

REM Check for Page Object (case-insensitive)
dir /b "src\main\java\pages\!feature!.java" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\main\java\pages\!feature!.java" 2^>nul') do (
        echo   [OK] Page Object:      src/main/java/pages/%%F
    )
) || (
    echo   [WARN] Page Object:    NOT FOUND
    set "filesOK=false"
)

REM Check for Feature File (case-insensitive)
dir /b "src\test\java\features\!feature!.feature" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\test\java\features\!feature!.feature" 2^>nul') do (
        echo   [OK] Feature File:     src/test/java/features/%%F
    )
) || (
    echo   [WARN] Feature File:   NOT FOUND
    set "filesOK=false"
)

REM Check for Step Definitions (case-insensitive)
dir /b "src\test\java\stepDefs\!feature!Steps.java" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\test\java\stepDefs\!feature!Steps.java" 2^>nul') do (
        echo   [OK] Step Definitions: src/test/java/stepDefs/%%F
    )
) || (
    echo   [WARN] Step Definitions: NOT FOUND
    set "filesOK=false"
)

echo.
echo [DEBUG] filesOK = [!filesOK!]
echo [DEBUG] RECORDING_DIR = [!RECORDING_DIR!]
echo.

if /i "!filesOK!"=="false" (
    echo [DEBUG] Files verification FAILED - preserving recording
    echo.
    echo [WARN] Some files were not created - check Maven output above
    echo [INFO] Temp recording preserved for debugging: !RECORDING_DIR!
    echo [INFO] Use Option 1B to retry with this recording
    echo.
    pause
    goto menu
)

echo [DEBUG] Files verification PASSED - proceeding to cleanup
REM ONLY cleanup temp directory if ALL files generated successfully
echo.
echo [SUCCESS] All files verified - cleaning up temp directory

if exist "!RECORDING_DIR!" (
    echo [DEBUG] Directory exists, attempting deletion...
    echo [DEBUG] Running: rmdir /s /q "!RECORDING_DIR!"
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    
    REM Wait briefly for filesystem to complete deletion
    timeout /t 1 /nobreak >nul 2>&1
    
    if exist "!RECORDING_DIR!" (
        echo [WARN] Could not delete temp directory - you can manually delete: !RECORDING_DIR!
        echo [DEBUG] Possible reasons: File in use, permissions, or antivirus blocking
    ) else (
        echo [OK] Temp directory cleaned successfully
    )
) else (
    echo [WARN] Directory !RECORDING_DIR! not found - may have been already deleted
)
echo.
echo ================================================================
echo [OK] RECORDING COMPLETE
echo ================================================================
echo.
echo Generated files for: !feature!
echo.
echo  Generated Files:
echo    - Page Object:      src/main/java/pages/!feature!.java
echo    - Feature File:     src/test/java/features/!feature!.feature
echo    - Step Definitions: src/test/java/stepDefs/!feature!Steps.java
echo.
echo.
echo  Next: Validate and compile with option 3 or 4
echo.
pause
goto menu

:retry_recording
cls
echo.
echo ================================================================
echo [RETRY] Generate Tests from Existing Recording
echo ================================================================
echo.
echo This option allows you to regenerate tests from a recording
echo that was previously saved or if generation failed before.
echo.

REM Check for existing temp directories
echo [INFO] Scanning for recording files
echo.

set "FOUND_RECORDINGS=0"
set /a "RECORD_COUNT=0"

REM Look for temp_recording directories
for /d %%D in (temp_recording_*) do (
    if exist "%%D\recorded-actions.java" (
        set /a RECORD_COUNT+=1
        set "RECORDING_!RECORD_COUNT!=%%D\recorded-actions.java"
        for %%F in ("%%D\recorded-actions.java") do (
            set "SIZE_!RECORD_COUNT!=%%~zF"
            echo   !RECORD_COUNT!. %%D\recorded-actions.java (%%~zF bytes)
        )
        set "FOUND_RECORDINGS=1"
    )
)

if "!FOUND_RECORDINGS!"=="0" (
    echo [ERROR] No recording files found in temp directories!
    echo.
    echo Please either:
    echo   1. Use option 1 to create a new recording, OR
    echo   2. Place a recorded-actions.java file in a temp_recording_* directory
    echo.
    pause
    goto menu
)

echo.
echo   0. Specify custom recording file path
echo.
set /p rec_choice="Choose recording (1-!RECORD_COUNT! or 0): "

if "!rec_choice!"=="0" (
    echo.
    set /p custom_rec="Enter full path to recording file: "
    if not exist "!custom_rec!" (
        echo [ERROR] File not found: !custom_rec!
        pause
        goto menu
    )
    set "SELECTED_RECORDING=!custom_rec!"
) else (
    if !rec_choice! LEQ 0 goto menu
    if !rec_choice! GTR !RECORD_COUNT! (
        echo [ERROR] Invalid choice!
        pause
        goto menu
    )
    REM Direct assignment based on choice
    if "!rec_choice!"=="1" set "SELECTED_RECORDING=!RECORDING_1!"
    if "!rec_choice!"=="2" set "SELECTED_RECORDING=!RECORDING_2!"
    if "!rec_choice!"=="3" set "SELECTED_RECORDING=!RECORDING_3!"
    if "!rec_choice!"=="4" set "SELECTED_RECORDING=!RECORDING_4!"
    if "!rec_choice!"=="5" set "SELECTED_RECORDING=!RECORDING_5!"
)

echo.
echo [DEBUG] Selected recording: !SELECTED_RECORDING!
echo.

echo.
echo  Enter test details:
echo.

REM ============================================================================
REM Get and validate feature name (required field)
REM ============================================================================
set /p feature="Feature Name (e.g., Login, Profile): "

REM Validate feature name is not empty
if "!feature!"=="" (
    echo.
    echo [ERROR] Feature name is required and cannot be empty
    echo [INFO] Please provide a descriptive name for your test feature
    pause
    goto menu
)

REM ============================================================================
REM Get and validate URL configuration
REM ============================================================================
echo.
echo [URL OPTIONS]
echo 1. Use base URL from config + custom path
echo    Base: https://uksestdevtest02.ukest.lan/MRIEnergy/
echo 2. Enter completely custom full URL
echo.
set /p url_choice="Choose option (1 or 2, default=1): "

REM Default to option 1 if no choice provided
if "!url_choice!"=="" set "url_choice=1"

REM Validate choice is 1 or 2
if not "!url_choice!"=="1" if not "!url_choice!"=="2" (
    echo [WARN] Invalid option '!url_choice!', defaulting to option 1
    set "url_choice=1"
)

if "!url_choice!"=="" set "url_choice=1"

if "!url_choice!"=="1" (
    echo.
    echo Enter ONLY the path to append to config URL
    echo Examples: /start-page, /login, /profile, or press Enter to skip
    set /p url_path="Path only: "
    REM Combine config URL with path (remove trailing slash from BASE_URL first)
    set "BASE_URL=https://uksestdevtest02.ukest.lan/MRIEnergy"
    REM Remove trailing slash if exists
    if "!BASE_URL:~-1!"=="/" set "BASE_URL=!BASE_URL:~0,-1!"
    if "!url_path!"=="" (
        set "final_url=!BASE_URL!"
    ) else (
        REM Ensure path starts with /
        if not "!url_path:~0,1!"=="/" set "url_path=/!url_path!"
        set "final_url=!BASE_URL!!url_path!"
    )
)

if "!url_choice!"=="2" (
    echo.
    set /p final_url="Enter full URL: "
)

if not "!url_choice!"=="1" if not "!url_choice!"=="2" (
    echo.
    echo [ERROR] Invalid choice!
    pause
    goto menu
)

echo.
set /p jira_id="JIRA Story ID (optional, press Enter to skip): "
if "!jira_id!"=="" set "jira_id=AUTO-GEN"

echo.
echo [DEBUG] SELECTED_RECORDING before conversion: !SELECTED_RECORDING!
echo.

echo.
echo ================================================================
echo  GENERATING TEST FILES FROM EXISTING RECORDING
echo ================================================================
echo.

REM Verify the file exists before processing
if not exist "!SELECTED_RECORDING!" (
    echo [ERROR] Recording file not found: !SELECTED_RECORDING!
    pause
    goto menu
)

REM Get absolute path and convert to forward slashes
set "TEMP_RECORDING=!SELECTED_RECORDING!"
for %%F in ("!TEMP_RECORDING!") do set "RECORDING_FILE=%%~fF"
set "RECORDING_FILE=!RECORDING_FILE:\=/!"

echo [DEBUG] Recording file: !RECORDING_FILE!
echo [DEBUG] Feature: !feature!
echo [DEBUG] URL: !final_url!
echo [DEBUG] JIRA: !jira_id!
echo.

REM Generate test files from recording
echo [INFO] Starting test file generation
echo [DEBUG] Command arguments:
echo   Recording: !RECORDING_FILE!
echo   Feature:   !feature!
echo   URL:       !final_url!
echo   JIRA:      !jira_id!
echo.
echo [DEBUG] Calling Maven exec:java - please wait
echo [DEBUG] Full command: mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" -Dexec.args="\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\""
echo.

REM Capture Maven output to extract the fixed feature name
set "MAVEN_LOG=maven_output_%RANDOM%.txt"
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" -Dexec.args="\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\"" > "!MAVEN_LOG!" 2>&1
type "!MAVEN_LOG!"

REM ============================================================================
REM Extract the auto-fixed feature name from Maven output
REM ============================================================================
set "FIXED_FEATURE=!feature!"
for /f "tokens=5 delims=: " %%a in ('findstr /C:"[INFO] Feature name (fixed):" "!MAVEN_LOG!"') do (
    set "FIXED_FEATURE=%%a"
)
del "!MAVEN_LOG!" 2>nul

if not "!FIXED_FEATURE!"=="!feature!" (
    echo.
    echo [DEBUG] Feature name was auto-fixed: '!feature!' -^> '!FIXED_FEATURE!'
    set "feature=!FIXED_FEATURE!"
    echo [DEBUG] Using fixed feature name: !feature!
)

set "GEN_EXIT_CODE=!ERRORLEVEL!"
echo.
echo ================================================================
echo [DEBUG] Maven process completed
echo ================================================================
echo [DEBUG] ERRORLEVEL = !ERRORLEVEL!
echo [DEBUG] GEN_EXIT_CODE = !GEN_EXIT_CODE!
echo.
echo [INFO] Checking if files were actually generated (case-insensitive)...
echo.

REM ============================================================================
REM Verify all generated files exist - CASE INSENSITIVE
REM ============================================================================
echo.
echo [SUCCESS] Test generation completed successfully!
echo.
echo [INFO] Verifying generated files (case-insensitive search for !feature!)
echo.

set "filesOK=true"
set "FOUND_COUNT=0"

REM Check for Page Object (case-insensitive)
dir /b "src\main\java\pages\!feature!.java" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\main\java\pages\!feature!.java" 2^>nul') do (
        echo   [OK] Page Object:      src/main/java/pages/%%F
    )
) || (
    echo   [WARN] Page Object:    NOT FOUND
    set "filesOK=false"
)

REM Check for Feature File (case-insensitive)
dir /b "src\test\java\features\!feature!.feature" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\test\java\features\!feature!.feature" 2^>nul') do (
        echo   [OK] Feature File:     src/test/java/features/%%F
    )
) || (
    echo   [WARN] Feature File:   NOT FOUND
    set "filesOK=false"
)

REM Check for Step Definitions (case-insensitive)
dir /b "src\test\java\stepDefs\!feature!Steps.java" 2>nul >nul && (
    set /a FOUND_COUNT+=1
    for /f %%F in ('dir /b "src\test\java\stepDefs\!feature!Steps.java" 2^>nul') do (
        echo   [OK] Step Definitions: src/test/java/stepDefs/%%F
    )
) || (
    echo   [WARN] Step Definitions: NOT FOUND
    set "filesOK=false"
)

echo.
echo [DEBUG] filesOK = [!filesOK!]
echo [DEBUG] Recording path = [!SELECTED_RECORDING!]
echo.

if /i "!filesOK!"=="false" (
    echo [DEBUG] Files verification FAILED - preserving recording
    echo.
    echo [WARN] Some files were not created - check Maven output above
    echo [INFO] Recording preserved for retry
    echo.
    pause
    goto menu
)

echo [DEBUG] Files verification PASSED - proceeding to cleanup

REM Extract the temp directory from recording path
REM SELECTED_RECORDING looks like: temp_recording_18077\recorded-actions.java
for %%F in ("!SELECTED_RECORDING!") do set "TEMP_DIR=%%~dpF"
REM Remove trailing backslash
if "!TEMP_DIR:~-1!"=="\" set "TEMP_DIR=!TEMP_DIR:~0,-1!"

echo.
echo [SUCCESS] All files verified successfully!
echo [INFO] Cleaning up temp recording directory: !TEMP_DIR!
echo [DEBUG] Running: rmdir /s /q "!TEMP_DIR!"

if exist "!TEMP_DIR!" (
    rmdir /s /q "!TEMP_DIR!" 2>nul
    
    REM Wait briefly for filesystem to complete deletion
    timeout /t 1 /nobreak >nul 2>&1
    
    if exist "!TEMP_DIR!" (
        echo [WARN] Could not delete temp directory - you can manually delete: !TEMP_DIR!
        echo [DEBUG] Possible reasons: File in use, permissions, or antivirus blocking
    ) else (
        echo [OK] Temp directory cleaned successfully
    )
) else (
    echo [WARN] Directory !TEMP_DIR! not found - may have been already deleted
)

echo.
echo ================================================================
echo [OK] REGENERATION COMPLETE
echo ================================================================
echo.
echo Generated files for: !feature!
echo.
echo  Generated Files:
echo    - Page Object:      src/main/java/pages/!feature!.java
echo    - Feature File:     src/test/java/features/!feature!.feature
echo    - Step Definitions: src/test/java/stepDefs/!feature!Steps.java
echo.
echo.
echo  Next: Validate and compile with option 3 or 4
echo.
pause
goto menu

:jira_story
cls
echo.
echo ================================================================
echo [JIRA] Generate Tests from JIRA Story
echo ================================================================
echo.
echo This option fetches requirements from a JIRA story and
echo generates complete test files automatically.
echo.

set /p jira_story_id="Enter JIRA Story ID (e.g., ECS-123): "
if "!jira_story_id!"=="" (
    echo.
    echo [ERROR] JIRA Story ID cannot be empty!
    pause
    goto menu
)

echo.
echo ================================================================
echo  FETCHING JIRA STORY AND GENERATING TESTS
echo ================================================================
echo.
echo [INFO] Fetching JIRA story: !jira_story_id!
echo [INFO] This will:
echo   1. Fetch story details from JIRA
echo   2. Extract requirements and acceptance criteria
echo   3. Generate Page Object, Feature, and Step Definitions
echo.

REM Generate test files from JIRA story
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" "-Dexec.args=!jira_story_id!"

if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] JIRA test generation failed! Check logs above.
    echo.
    echo Possible issues:
    echo   - JIRA story not found
    echo   - JIRA credentials not configured
    echo   - Network connectivity issues
    echo.
    echo Check: src/test/resources/jiraConfigurations.properties
    pause
    goto menu
)

echo.
echo ================================================================
echo [OK] JIRA TEST GENERATION COMPLETE
echo ================================================================
echo.
echo Generated files based on JIRA story: !jira_story_id!
echo.
echo  Check the following directories:
echo    - src/main/java/pages/
echo    - src/test/java/features/
echo    - src/test/java/stepDefs/
echo.
echo  Next: Validate and compile with option 5
echo.
pause
goto menu

:project_setup
cls
echo.
echo +===============================================================+
echo ^|                                                                ^|
echo ^|     COMPLETE PROJECT SETUP WITH MCP SERVER                     ^|
echo ^|                                                                ^|
echo +===============================================================+
echo.
echo This will set up your complete test automation environment:
echo.
echo [OK] Prerequisites Check:
echo    - Java 17+
echo    - Maven 3.6+
echo    - Node.js 18+ (for MCP Server)
echo    - Git (optional)
echo.
echo [OK] Installation Steps:
echo    1^. Install Maven dependencies
echo    2^. Install Playwright browsers
echo    3^. Setup MCP Server (Node.js)
echo    4^. Configure test properties
echo    5^. Verify installation
echo.

choice /C YN /M "Do you want to proceed with complete setup"
if errorlevel 2 goto menu
if errorlevel 1 goto run_setup

:run_setup
echo.
echo ================================================================
echo  STEP 1: CHECKING PREREQUISITES
echo ================================================================
echo.

REM Check Java
echo [1/4] Checking Java
java -version 2>&1 | findstr /C:"version" >nul
if !ERRORLEVEL! GTR 0 (
    echo [ERROR] Java not found! Please install Java 17 or higher.
    echo Download: https://adoptium.net/
    pause
    goto menu
)
java -version
echo [OK] Java is installed
echo.

REM Check Maven
echo [2/4] Checking Maven
mvn -version 2>&1 | findstr /C:"Maven" >nul
if !ERRORLEVEL! GTR 0 (
    echo [ERROR] Maven not found! Please install Maven 3.6+
    echo Download: https://maven.apache.org/download.cgi
    pause
    goto menu
)
mvn -version | findstr /C:"Maven"
echo [OK] Maven is installed
echo.

REM Check Node.js
echo [3/4] Checking Node.js
node --version >nul 2>&1
if !ERRORLEVEL! GTR 0 (
    echo [WARN] Node.js not found!
    echo [INFO] Node.js is optional but recommended for AI features
    echo Download: https://nodejs.org/
    echo.
    set "NODE_AVAILABLE=0"
) else (
    node --version
    echo [OK] Node.js is installed
    set "NODE_AVAILABLE=1"
    echo.
)

REM Check Git
echo [4/4] Checking Git
git --version >nul 2>&1
if !ERRORLEVEL! GTR 0 (
    echo [WARN] Git not found (optional)
    echo.
) else (
    git --version
    echo [OK] Git is installed
    echo.
)

echo.
echo ================================================================
echo  STEP 2: INSTALLING MAVEN DEPENDENCIES
echo ================================================================
echo.
echo [INFO] Using clean compile to avoid reporting plugin issues
echo.

mvn clean compile -DskipTests

if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] Maven dependency installation failed!
    pause
    goto menu
)

echo.
echo [SUCCESS] Maven dependencies installed successfully!
echo.

echo.
echo ================================================================
echo  STEP 3: INSTALLING PLAYWRIGHT BROWSERS
echo ================================================================
echo.

mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"

if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] Playwright browser installation failed!
    pause
    goto menu
)

echo.
echo [SUCCESS] Playwright browsers installed successfully!
echo.

if "!NODE_AVAILABLE!"=="1" (
    echo.
    echo ================================================================
    echo  STEP 4: SETTING UP MCP SERVER
    echo ================================================================
    echo.

    cd mcp-server

    echo [INFO] Installing MCP Server dependencies
    call npm install

    if !ERRORLEVEL! GTR 0 (
        echo.
        echo [ERROR] MCP Server dependency installation failed!
        cd..
        pause
        goto menu
    )

    echo.
    echo [INFO] Building MCP Server
    call npm run build

    if !ERRORLEVEL! GTR 0 (
        echo.
        echo [WARN] MCP Server build had issues, but continuing
    )

    cd..

    echo.
    echo [SUCCESS] MCP Server setup complete!
    echo.
) else (
    echo.
    echo [SKIP] MCP Server setup skipped (Node.js not available)
    echo [INFO] You can set it up later when Node.js is installed
    echo.
)

echo.
echo ================================================================
echo  STEP 5: VERIFYING INSTALLATION
echo ================================================================
echo.

echo [INFO] Compiling project to verify setup
mvn compile -q

if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] Project compilation failed!
    pause
    goto menu
)

echo.
echo [SUCCESS] Project compiled successfully!
echo.

echo.
echo +===============================================================+
echo ^|                                                                ^|
echo ^|           [OK] SETUP COMPLETE! [OK]                                ^|
echo ^|                                                                ^|
echo +===============================================================+
echo.
echo  Installation Summary:
echo    [OK] Maven dependencies installed
echo    [OK] Playwright browsers installed
if "!NODE_AVAILABLE!"=="1" (
    echo    [OK] MCP Server configured
) else (
    echo    [OK]  MCP Server skipped ^(Node.js not available^)
)
echo    [OK] Project verified and compiled
echo.
echo  Next Steps:
echo    1. Configure test properties in:
echo       - src/test/resources/configurations.properties
echo       - src/test/resources/jiraConfigurations.properties
echo.
echo    2. Start creating tests with:
echo       - Option 1: Record ^& Auto-Generate
echo       - Option 2: Generate from JIRA Story
echo       - Option 3: AI-Assisted Interactive
echo.
echo    3. Run tests with: Option 6 (Maven Clean Test)
echo.
pause
goto menu

:ai_cli
cls
echo.
echo ================================================================
echo [AI CLI] Starting AI-Assisted Interactive Generation
echo ================================================================
echo.

REM Check if Node.js is available
node --version >nul 2>&1
if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] Node.js is not installed!
    echo.
    echo This feature requires Node.js 18+
    echo Download from: https://nodejs.org/
    echo.
    pause
    goto menu
)

echo.
echo Choose AI generation mode:
echo.
echo 1. Interactive Mode (Answer questions)
echo 2. JIRA Mode (Fetch from JIRA story)
echo.
set /p ai_mode="Enter choice (1 or 2): "

if "!ai_mode!"=="1" (
    echo.
    echo Starting interactive AI generation
    node automation-cli.js interactive
)

if "!ai_mode!"=="2" (
    echo.
    set /p jira_story="Enter JIRA Story ID: "
    if "!jira_story!"=="" (
        echo [ERROR] JIRA Story ID required!
        pause
        goto menu
    )
    echo.
    echo Fetching JIRA story and generating tests
    node automation-cli.js jira !jira_story!
)

if not "!ai_mode!"=="1" if not "!ai_mode!"=="2" (
    echo.
    echo [ERROR] Invalid choice!
    pause
    goto menu
)

if !ERRORLEVEL! GTR 0 (
    echo.
    echo [ERROR] AI generation failed! Check logs above.
    pause
    goto menu
)

echo.
echo ================================================================
echo [OK] AI GENERATION COMPLETE
echo ================================================================
echo.
echo Validating and compiling
echo.

REM Auto-validate and compile
powershell -ExecutionPolicy Bypass -File "%~dp0pw-utils.ps1" -ValidateJava -AutoFix
mvn clean compile

echo.
pause
goto menu


:test_validation
cls
echo.
echo ================================================================
echo [VALIDATE] Validate ^& Run Tests
echo ================================================================
echo.
echo Choose an option:
echo.
echo 1. Validate Java Code Only
echo 2. Validate ^& Auto-Fix Java Code
echo 3. Compile Project (mvn clean compile)
echo 4. Run All Tests (mvn test)
echo 5. Run Specific Feature Tag
echo 6. Back to Main Menu
echo.
set /p val_choice="Enter choice (1-6): "

if "!val_choice!"=="1" (
    echo.
    echo Validating Java code
    powershell -ExecutionPolicy Bypass -File "%~dp0pw-utils.ps1" -ValidateJava
    pause
    goto test_validation
)

if "!val_choice!"=="2" (
    echo.
    echo Validating and fixing Java code
    powershell -ExecutionPolicy Bypass -File "%~dp0pw-utils.ps1" -ValidateJava -AutoFix
    pause
    goto test_validation
)

if "!val_choice!"=="3" (
    echo.
    echo Compiling project with filtered output
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean compile 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto test_validation
)

if "!val_choice!"=="4" (
    echo.
    echo ================================================================
    echo Choose test execution mode:
    echo ================================================================
    echo.
    echo 1. Run Current Generated Features ^(Cucumber^)
    echo 2. Run TestNG Suite ^(testng.xml^)
    echo 3. Back
    echo.
    set /p test_mode="Enter choice (1-3): "

    if "!test_mode!"=="1" (
        echo.
        echo Running Cucumber tests with filtered output
        echo.
        powershell -ExecutionPolicy Bypass -Command "& { mvn test 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
        echo.
    )

    if "!test_mode!"=="2" (
        echo.
        echo Running TestNG suite with filtered output
        echo.
        powershell -ExecutionPolicy Bypass -Command "& { mvn test '-DsuiteXmlFile=src/test/testng.xml' 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
        echo.
    )

    if "!test_mode!"=="3" (
        goto test_validation
    )

    if not "!test_mode!"=="1" if not "!test_mode!"=="2" if not "!test_mode!"=="3" (
        echo Invalid choice!
        timeout /t 2 >nul
    )
    pause
    goto test_validation
)

if "!val_choice!"=="5" (
    echo.
    set /p tag="Enter tag (e.g., @smoke, @login): "
    echo.
    echo Running tests with tag: !tag!
    echo.
    call mvn test "-Dcucumber.filter.tags=!tag!"
    echo.
    pause
    goto test_validation
)

if "!val_choice!"=="6" (
    cls
    goto menu
)

echo.
echo Invalid choice!
timeout /t 2 >nul
goto test_validation

:maven_compile
cls
echo.
echo ======================================================================
echo   MAVEN CLEAN COMPILE (Filtered Output)
echo ======================================================================
echo.
powershell -ExecutionPolicy Bypass -Command "& { mvn clean compile 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
echo.
pause
goto menu

:maven_test
cls
echo.
echo ======================================================================
echo   MAVEN CLEAN TEST (Choose Execution Mode)
echo ======================================================================
echo.
echo Choose test execution mode:
echo.
echo 1. Run Current Generated Features (Cucumber)
echo 2. Run TestNG Suite (testng.xml)
echo 3. Back to Main Menu
echo.
set /p test_mode="Enter choice (1-3): "

if "!test_mode!"=="1" (
    echo.
    echo ======================================================================
    echo   Running Cucumber Tests (Filtered Output)
    echo ======================================================================
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean test 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
)

if "!test_mode!"=="2" (
    echo.
    echo ======================================================================
    echo   Running TestNG Suite (Filtered Output)
    echo ======================================================================
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean test '-DsuiteXmlFile=src/test/testng.xml' 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
)

if "!test_mode!"=="3" (
    goto menu
)

echo.
echo Invalid choice!
timeout /t 2 >nul
goto maven_test

:run_tests
cls
echo.
echo ======================================================================
echo   RUN SPECIFIC TESTS (Filtered Output)
echo ======================================================================
echo.
echo Choose test type:
echo.
echo 1. Run by Cucumber Tag (e.g., @smoke, @login)
echo 2. Run by TestNG Group (e.g., smoke, regression)
echo 3. Back to Main Menu
echo.
set /p test_type="Enter choice (1-3): "

if "!test_type!"=="1" (
    echo.
    set /p tag="Enter Cucumber tag to run (e.g., @smoke, @login): "
    if "!tag!"=="" (
        echo [ERROR] Tag cannot be empty!
        pause
        goto run_tests
    )
    echo.
    echo Running Cucumber tests with tag: !tag!
    echo.
    call mvn test "-Dcucumber.filter.tags=!tag!"
    echo.
    pause
    goto menu
)

if "!test_type!"=="2" (
    echo.
    set /p group="Enter TestNG group to run (e.g., smoke, regression): "
    if "!group!"=="" (
        echo [ERROR] Group cannot be empty!
        pause
        goto run_tests
    )
    echo.
    echo Running TestNG tests with group: !group!
    echo.
    powershell -ExecutionPolicy Bypass -Command "$group='!group!'; & { mvn test '-DsuiteXmlFile=src/test/testng.xml' \"-Dgroups=$group\" 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
)

if "!test_type!"=="3" (
    goto menu
)

echo.
echo Invalid choice!
timeout /t 2 >nul
goto run_tests

:help
cls
echo.
echo ======================================================================
echo   PLAYWRIGHT AUTOMATION - HELP
echo ======================================================================
echo.
echo UNIFIED ENTRY POINT - All operations in one place
echo.
echo TEST GENERATION OPTIONS:
echo   [RECORD]   - Records browser actions and auto-generates tests
echo              - Uses Playwright Codegen recorder
echo              - Generates Page Object, Feature file, and Step Definitions
echo              - No Node.js required
echo.
echo   [AI CLI]   - Uses AI to generate tests (requires Node.js)
echo              - Interactive mode: Answer questions
echo              - JIRA mode: Fetch from JIRA story
echo.
echo   [VALIDATE] - Validates and runs existing tests
echo              - Code validation and auto-fix
echo              - Compile and run tests
echo.
echo UTILITY OPTIONS:
echo   Maven Clean Compile - Compile all Java sources
echo   Maven Clean Test    - Run all tests
echo   Run Specific Tags   - Run tests by Cucumber tags
echo.
echo For PowerShell utilities help:
echo   powershell -File pw-utils.ps1 -Help
echo.
pause
goto menu

REM ============================================================================
REM QUICK JAVA VALIDATION - EMBEDDED POWERSHELL (NO EXTERNAL FILES NEEDED)
REM ============================================================================

:quick_validate
cls
echo.
echo ======================================================================
echo   Quick Java Validation ^& Auto-Fix (Embedded)
echo ======================================================================
echo.
echo Choose an option:
echo.
echo 1. Validate Only (Check for errors)
echo 2. Validate ^& Auto-Fix (Recommended)
echo 3. Back to Main Menu
echo.
set /p val_mode="Enter choice (1-3): "

if "!val_mode!"=="1" (
    call :embedded_validate_check_only
    echo.
    pause
    goto quick_validate
)

if "!val_mode!"=="2" (
    call :embedded_validate_autofix
    echo.
    pause
    goto quick_validate
)

if "!val_mode!"=="3" (
    goto menu
)

echo Invalid choice!
timeout /t 2 >nul
goto quick_validate

REM ----------------------------------------------------------------------------
REM EMBEDDED VALIDATION: CHECK ONLY
REM ----------------------------------------------------------------------------
:embedded_validate_check_only
echo.
echo [INFO] Validating Java code...
echo.
powershell -ExecutionPolicy Bypass -NoProfile -Command "$files = Get-ChildItem -Path 'src' -Filter '*.java' -Recurse -ErrorAction SilentlyContinue; $errorCount = 0; $warningCount = 0; Write-Host '[CHECK] Scanning Java files...' -ForegroundColor Cyan; Write-Host ''; foreach ($file in $files) { $content = Get-Content $file.FullName -Raw; $hasIssue = $false; if ($content -match 'public\s+static\s+void\s+methodName\s*\(') { if (-not $hasIssue) { Write-Host \"  File: $($file.Name)\" -ForegroundColor Yellow; $hasIssue = $true } $errorCount++; Write-Host \"    [ERROR] 'methodName' should be 'main'\" -ForegroundColor Red }; if ($content -match 'System\.out\.printline') { if (-not $hasIssue) { Write-Host \"  File: $($file.Name)\" -ForegroundColor Yellow; $hasIssue = $true } $errorCount++; Write-Host \"    [ERROR] 'printline' should be 'println'\" -ForegroundColor Red }; if ($content -match 'login\.enter(Username|Password)' -or $content -match 'login\.clickSignIn') { if (-not $hasIssue) { Write-Host \"  File: $($file.Name)\" -ForegroundColor Yellow; $hasIssue = $true } $errorCount++; Write-Host \"    [ERROR] 'login.' should be 'Login.' (capital L)\" -ForegroundColor Red }; if ($content -match 'Login\.enter(Username|Password)\(page,') { if (-not $hasIssue) { Write-Host \"  File: $($file.Name)\" -ForegroundColor Yellow; $hasIssue = $true } $errorCount++; Write-Host \"    [ERROR] Use smart methods: Login.UsernameField(text) or Login.PasswordField(text)\" -ForegroundColor Red }; if ($content -match 'public\s+class\s+\w+Page\s+extends\s+(base|BasePage|utils)' -and $content -notmatch 'import.*Page;') { if (-not $hasIssue) { Write-Host \"  File: $($file.Name)\" -ForegroundColor Yellow; $hasIssue = $true } $warningCount++; Write-Host \"    [WARNING] Possible missing Page import\" -ForegroundColor Yellow } }; Write-Host ''; Write-Host '[SUMMARY]' -ForegroundColor Cyan; Write-Host \"  Errors: $errorCount\" -ForegroundColor $(if ($errorCount -eq 0) { 'Green' } else { 'Red' }); Write-Host \"  Warnings: $warningCount\" -ForegroundColor $(if ($warningCount -eq 0) { 'Green' } else { 'Yellow' }); if ($errorCount -eq 0 -and $warningCount -eq 0) { Write-Host ''; Write-Host '[OK] No issues found! Code looks good.' -ForegroundColor Green } else { Write-Host ''; Write-Host '[TIP] Run option 2 to auto-fix these issues' -ForegroundColor Cyan }"
goto :eof

REM ----------------------------------------------------------------------------
REM EMBEDDED VALIDATION: AUTO-FIX
REM ----------------------------------------------------------------------------
:embedded_validate_autofix
echo.
echo [INFO] Validating and auto-fixing Java code...
echo.
powershell -ExecutionPolicy Bypass -NoProfile -Command "$files = Get-ChildItem -Path 'src' -Filter '*.java' -Recurse -ErrorAction SilentlyContinue; $fixCount = 0; $filesFixed = @(); Write-Host '[FIX] Auto-fixing Java files...' -ForegroundColor Green; Write-Host ''; foreach ($file in $files) { $content = Get-Content $file.FullName -Raw; $modified = $false; $fileIssues = @(); if ($content -match 'public\s+static\s+void\s+methodName\s*\(') { $content = $content -replace '(public\s+static\s+void\s+)methodName(\s*\()', '$1main$2'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Renamed 'methodName' to 'main'\" }; if ($content -match 'System\.out\.printline') { $content = $content -replace 'System\.out\.printline', 'System.out.println'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Changed 'printline' to 'println'\" }; if ($content -match 'login\.enterUsername|login\.enterPassword|login\.clickSignIn') { $content = $content -replace 'login\.enterUsername', 'Login.UsernameField'; $content = $content -replace 'login\.enterPassword', 'Login.PasswordField'; $content = $content -replace 'login\.clickSignIn', 'Login.SignInButton'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Changed 'login.' to 'Login.' with smart methods\" }; if ($content -match 'Login\.enterUsername\s*\(\s*page\s*,\s*([^)]+)\)') { $content = $content -replace 'Login\.enterUsername\s*\(\s*page\s*,\s*([^)]+)\)', 'Login.UsernameField($1)'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Updated to smart method: Login.UsernameField()\" }; if ($content -match 'Login\.enterPassword\s*\(\s*page\s*,\s*([^)]+)\)') { $content = $content -replace 'Login\.enterPassword\s*\(\s*page\s*,\s*([^)]+)\)', 'Login.PasswordField($1)'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Updated to smart method: Login.PasswordField()\" }; if ($content -match 'Login\.clickSignIn\s*\(\s*page\s*\)') { $content = $content -replace 'Login\.clickSignIn\s*\(\s*page\s*\)', 'Login.SignInButton()'; $modified = $true; $fixCount++; $fileIssues += \"    [FIXED] Updated to smart method: Login.SignInButton()\" }; if ($modified) { Set-Content -Path $file.FullName -Value $content -NoNewline -Force; Write-Host \"  File: $($file.Name)\" -ForegroundColor Cyan; $fileIssues | ForEach-Object { Write-Host $_ -ForegroundColor Green }; $filesFixed += $file.Name } }; Write-Host ''; Write-Host '[SUMMARY]' -ForegroundColor Cyan; Write-Host \"  Files Fixed: $($filesFixed.Count)\" -ForegroundColor Green; Write-Host \"  Total Fixes: $fixCount\" -ForegroundColor Green; if ($fixCount -eq 0) { Write-Host ''; Write-Host '[OK] No issues to fix! Code is already clean.' -ForegroundColor Green } else { Write-Host ''; Write-Host '[SUCCESS] All issues fixed!' -ForegroundColor Green; Write-Host '[TIP] Run mvn clean compile to verify changes' -ForegroundColor Cyan }"
goto :eof

REM ============================================================================
REM EXIT
REM ============================================================================

:exit
cls
echo.
echo ======================================================================
echo   Thanks for using Playwright Automation!
echo ======================================================================
echo.
echo [INFO] This was the ALL-IN-ONE unified script
echo [INFO] No external files needed - everything is embedded
echo.
exit /b 0

