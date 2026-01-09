@echo off
REM ============================================================================
REM PLAYWRIGHT AUTOMATION - UNIFIED ENTRY POINT
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
echo [RECORD] Starting Playwright Recording...
echo ================================================================
echo.
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  🎥 PLAYWRIGHT RECORDER - Auto-Generate Test Scripts          ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo [INFO] Using Pure Java file generator (Node.js not required)
echo.
echo.
echo 📝 Enter test details:
echo.

REM Get feature name
set /p feature="Feature Name (e.g., Login, Profile): "
if "!feature!"=="" (
    echo.
    echo [ERROR] Feature name cannot be empty!
    pause
    goto menu
)

echo.
echo [URL OPTIONS]
echo 1. Use config URL + path (Config: https://uksestdevtest02.ukest.lan/MRIEnergy/)
echo 2. Enter completely custom full URL
echo.
set /p url_choice="Choose option (1 or 2, default=1): "

if "!url_choice!"=="" set "url_choice=1"

if "!url_choice!"=="1" (
    echo.
    echo Enter ONLY the path to append to config URL
    echo Examples: /start-page, /login, /profile, or press Enter to skip
    set /p url_path="Path only: "
    REM Combine config URL with path
    set "BASE_URL=https://uksestdevtest02.ukest.lan/MRIEnergy"
    if "!url_path!"=="" (
        set "final_url=!BASE_URL!"
    ) else (
        set "final_url=!BASE_URL!!url_path!"
    )
) else if "!url_choice!"=="2" (
    echo.
    set /p final_url="Enter full URL: "
) else (
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
echo ════════════════════════════════════════════════════════════════
echo 🎬 Step 1: RECORD ACTIONS
echo ════════════════════════════════════════════════════════════════
echo.
echo Instructions:
echo   1. Browser will open with Playwright Inspector
echo   2. Perform your test actions (click, type, navigate)
echo   3. Inspector will record all actions automatically
echo   4. Close browser when done to save recording
echo.
echo Starting Playwright Codegen Recorder...
echo.

REM Run Playwright codegen to record actions
if "!final_url!"=="" (
    mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="codegen --target java --output !RECORDING_DIR!\recorded-actions.java"
) else (
    mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="codegen --target java --output !RECORDING_DIR!\recorded-actions.java !final_url!"
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Recording failed! Check logs above.
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    pause
    goto menu
)

REM Check if recording file was created
if not exist "!RECORDING_DIR!\recorded-actions.java" (
    echo.
    echo [ERROR] No recording file created. Did you record any actions?
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    pause
    goto menu
)

REM Check if recording file has content
for %%A in ("!RECORDING_DIR!\recorded-actions.java") do set size=%%~zA
if !size! LSS 100 (
    echo.
    echo [ERROR] Recording file is too small ^(!size! bytes^). Did you record any actions?
    echo [INFO] Please close the browser AFTER recording actions, not immediately.
    rmdir /s /q "!RECORDING_DIR!" 2>nul
    pause
    goto menu
)

echo.
echo [SUCCESS] Recording captured ^(!size! bytes^)

echo.
echo ════════════════════════════════════════════════════════════════
echo 🎬 Step 2: GENERATE TEST FILES
echo ════════════════════════════════════════════════════════════════
echo.

REM Get absolute path and convert to forward slashes
for %%F in ("!RECORDING_DIR!\recorded-actions.java") do set "RECORDING_FILE=%%~fF"
set "RECORDING_FILE=!RECORDING_FILE:\=/!"

echo [DEBUG] Recording file: !RECORDING_FILE!
echo [DEBUG] Feature: !feature!
echo [DEBUG] URL: !final_url!
echo [DEBUG] JIRA: !jira_id!
echo.

REM Generate test files from recording - use quoted arguments
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" "-Dexec.args=\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\""

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Test generation failed! Check logs above.
    echo [DEBUG] Keeping temp directory for inspection: !RECORDING_DIR!
    pause
    goto menu
)

echo.
echo [SUCCESS] Test files generated successfully!
echo.

REM Cleanup temp directory
echo [INFO] Cleaning up temp directory...
rmdir /s /q "!RECORDING_DIR!" 2>nul

echo.
echo ════════════════════════════════════════════════════════════════
echo ✅ RECORDING COMPLETE
echo ════════════════════════════════════════════════════════════════
echo.
echo Generated files for: !feature!
echo.
echo 📂 Generated Files:
echo    - Page Object:      src/main/java/pages/!feature!.java
echo    - Feature File:     src/test/java/features/!feature!.feature
echo    - Step Definitions: src/test/java/stepDefs/!feature!Steps.java
echo.
echo.
echo 📋 Next: Validate and compile with option 3 or 4
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
echo [INFO] Scanning for recording files...
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
echo 📝 Enter test details:
echo.

REM Get feature name
set /p feature="Feature Name (e.g., Login, Profile): "
if "!feature!"=="" (
    echo [ERROR] Feature name cannot be empty!
    pause
    goto menu
)

echo.
echo [URL OPTIONS]
echo 1. Use config URL + path (Config: https://uksestdevtest02.ukest.lan/MRIEnergy/)
echo 2. Enter completely custom full URL
echo.
set /p url_choice="Choose option (1 or 2, default=1): "

if "!url_choice!"=="" set "url_choice=1"

if "!url_choice!"=="1" (
    echo.
    echo Enter ONLY the path to append to config URL
    echo Examples: /start-page, /login, /profile, or press Enter to skip
    set /p url_path="Path only: "
    REM Combine config URL with path
    set "BASE_URL=https://uksestdevtest02.ukest.lan/MRIEnergy"
    if "!url_path!"=="" (
        set "final_url=!BASE_URL!"
    ) else (
        set "final_url=!BASE_URL!!url_path!"
    )
) else if "!url_choice!"=="2" (
    echo.
    set /p final_url="Enter full URL: "
) else (
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
echo ════════════════════════════════════════════════════════════════
echo 🔄 GENERATING TEST FILES FROM EXISTING RECORDING
echo ════════════════════════════════════════════════════════════════
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

REM Generate test files from recording - use quoted arguments
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" "-Dexec.args=\"!RECORDING_FILE!\" \"!feature!\" \"!final_url!\" \"!jira_id!\""

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Test generation failed! Check logs above.
    echo [DEBUG] Recording file preserved for inspection
    pause
    goto menu
)

echo.
echo [SUCCESS] Test files generated successfully!
echo.
echo ════════════════════════════════════════════════════════════════
echo ✅ REGENERATION COMPLETE
echo ════════════════════════════════════════════════════════════════
echo.
echo Generated files for: !feature!
echo.
echo 📂 Generated Files:
echo    - Page Object:      src/main/java/pages/!feature!.java
echo    - Feature File:     src/test/java/features/!feature!.feature
echo    - Step Definitions: src/test/java/stepDefs/!feature!Steps.java
echo.
echo.
echo 📋 Next: Validate and compile with option 3 or 4
echo.
echo [INFO] Recording file kept for future retries
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
echo ════════════════════════════════════════════════════════════════
echo 📋 FETCHING JIRA STORY AND GENERATING TESTS
echo ════════════════════════════════════════════════════════════════
echo.
echo [INFO] Fetching JIRA story: !jira_story_id!
echo [INFO] This will:
echo   1. Fetch story details from JIRA
echo   2. Extract requirements and acceptance criteria
echo   3. Generate Page Object, Feature, and Step Definitions
echo.

REM Generate test files from JIRA story
call mvn exec:java -Dexec.mainClass="configs.TestGeneratorHelper" "-Dexec.args=!jira_story_id!"

if %ERRORLEVEL% NEQ 0 (
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
echo ════════════════════════════════════════════════════════════════
echo ✅ JIRA TEST GENERATION COMPLETE
echo ════════════════════════════════════════════════════════════════
echo.
echo Generated files based on JIRA story: !jira_story_id!
echo.
echo 📂 Check the following directories:
echo    - src/main/java/pages/
echo    - src/test/java/features/
echo    - src/test/java/stepDefs/
echo.
echo 📋 Next: Validate and compile with option 5
echo.
pause
goto menu

:project_setup
cls
echo.
echo ╔═══════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║     COMPLETE PROJECT SETUP WITH MCP SERVER                     ║
echo ║                                                                ║
echo ╚═══════════════════════════════════════════════════════════════╝
echo.
echo This will set up your complete test automation environment:
echo.
echo ✅ Prerequisites Check:
echo    - Java 17+
echo    - Maven 3.6+
echo    - Node.js 18+ (for MCP Server)
echo    - Git (optional)
echo.
echo ✅ Installation Steps:
echo    1. Install Maven dependencies
echo    2. Install Playwright browsers
echo    3. Setup MCP Server (Node.js)
echo    4. Configure test properties
echo    5. Verify installation
echo.

choice /C YN /M "Do you want to proceed with complete setup"
if errorlevel 2 goto menu
if errorlevel 1 goto run_setup

:run_setup
echo.
echo ════════════════════════════════════════════════════════════════
echo 🚀 STEP 1: CHECKING PREREQUISITES
echo ════════════════════════════════════════════════════════════════
echo.

REM Check Java
echo [1/4] Checking Java...
java -version 2>&1 | findstr /C:"version" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found! Please install Java 17 or higher.
    echo Download: https://adoptium.net/
    pause
    goto menu
)
java -version
echo [OK] Java is installed
echo.

REM Check Maven
echo [2/4] Checking Maven...
mvn -version 2>&1 | findstr /C:"Maven" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found! Please install Maven 3.6+
    echo Download: https://maven.apache.org/download.cgi
    pause
    goto menu
)
mvn -version | findstr /C:"Maven"
echo [OK] Maven is installed
echo.

REM Check Node.js
echo [3/4] Checking Node.js...
node --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
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
echo [4/4] Checking Git...
git --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARN] Git not found (optional)
    echo.
) else (
    git --version
    echo [OK] Git is installed
    echo.
)

echo.
echo ════════════════════════════════════════════════════════════════
echo 🚀 STEP 2: INSTALLING MAVEN DEPENDENCIES
echo ════════════════════════════════════════════════════════════════
echo.

mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven dependency installation failed!
    pause
    goto menu
)

echo.
echo [SUCCESS] Maven dependencies installed successfully!
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo 🚀 STEP 3: INSTALLING PLAYWRIGHT BROWSERS
echo ════════════════════════════════════════════════════════════════
echo.

mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"

if %ERRORLEVEL% NEQ 0 (
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
    echo ════════════════════════════════════════════════════════════════
    echo 🚀 STEP 4: SETTING UP MCP SERVER
    echo ════════════════════════════════════════════════════════════════
    echo.

    cd mcp-server

    echo [INFO] Installing MCP Server dependencies...
    call npm install

    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo [ERROR] MCP Server dependency installation failed!
        cd..
        pause
        goto menu
    )

    echo.
    echo [INFO] Building MCP Server...
    call npm run build

    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo [WARN] MCP Server build had issues, but continuing...
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
echo ════════════════════════════════════════════════════════════════
echo 🚀 STEP 5: VERIFYING INSTALLATION
echo ════════════════════════════════════════════════════════════════
echo.

echo [INFO] Compiling project to verify setup...
mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Project compilation failed!
    pause
    goto menu
)

echo.
echo [SUCCESS] Project compiled successfully!
echo.

echo.
echo ╔═══════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║           ✅ SETUP COMPLETE! ✅                                ║
echo ║                                                                ║
echo ╚═══════════════════════════════════════════════════════════════╝
echo.
echo 📊 Installation Summary:
echo    ✅ Maven dependencies installed
echo    ✅ Playwright browsers installed
if "!NODE_AVAILABLE!"=="1" (
    echo    ✅ MCP Server configured
) else (
    echo    ⚠️  MCP Server skipped ^(Node.js not available^)
)
echo    ✅ Project verified and compiled
echo.
echo 🚀 Next Steps:
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
echo [AI CLI] Starting AI-Assisted Interactive Generation...
echo ================================================================
echo.

REM Check if Node.js is available
node --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
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
    echo Starting interactive AI generation...
    node automation-cli.js interactive
) else if "!ai_mode!"=="2" (
    echo.
    set /p jira_story="Enter JIRA Story ID: "
    if "!jira_story!"=="" (
        echo [ERROR] JIRA Story ID required!
        pause
        goto menu
    )
    echo.
    echo Fetching JIRA story and generating tests...
    node automation-cli.js jira !jira_story!
) else (
    echo.
    echo [ERROR] Invalid choice!
    pause
    goto menu
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] AI generation failed! Check logs above.
    pause
    goto menu
)

echo.
echo ════════════════════════════════════════════════════════════════
echo ✅ AI GENERATION COMPLETE
echo ════════════════════════════════════════════════════════════════
echo.
echo Validating and compiling...
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
    echo Validating Java code...
    powershell -ExecutionPolicy Bypass -File "%~dp0pw-utils.ps1" -ValidateJava
    pause
    goto test_validation
) else if "!val_choice!"=="2" (
    echo.
    echo Validating and fixing Java code...
    powershell -ExecutionPolicy Bypass -File "%~dp0pw-utils.ps1" -ValidateJava -AutoFix
    pause
    goto test_validation
) else if "!val_choice!"=="3" (
    echo.
    echo Compiling project with filtered output...
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean compile 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto test_validation
) else if "!val_choice!"=="4" (
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
        echo Running Cucumber tests with filtered output...
        echo.
        powershell -ExecutionPolicy Bypass -Command "& { mvn test 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
        echo.
    ) else if "!test_mode!"=="2" (
        echo.
        echo Running TestNG suite with filtered output...
        echo.
        powershell -ExecutionPolicy Bypass -Command "& { mvn test '-DsuiteXmlFile=src/test/testng.xml' 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
        echo.
    ) else if "!test_mode!"=="3" (
        goto test_validation
    ) else (
        echo Invalid choice!
        timeout /t 2 >nul
    )
    pause
    goto test_validation
) else if "!val_choice!"=="5" (
    echo.
    set /p tag="Enter tag (e.g., @smoke, @login): "
    echo.
    echo Running tests with tag: !tag! (filtered output)
    echo.
    powershell -ExecutionPolicy Bypass -Command "$tag='!tag!'; & { mvn test -Dcucumber.filter.tags=\"$tag\" 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto test_validation
) else if "!val_choice!"=="6" (
    goto menu
) else (
    echo.
    echo Invalid choice!
    timeout /t 2 >nul
    goto test_validation
)

:maven_compile
cls
echo.
echo ======================================================================
echo   MAVEN CLEAN COMPILE (Filtered Output)...
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
    echo   Running Cucumber Tests (Filtered Output)...
    echo ======================================================================
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean test 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
) else if "!test_mode!"=="2" (
    echo.
    echo ======================================================================
    echo   Running TestNG Suite (Filtered Output)...
    echo ======================================================================
    echo.
    powershell -ExecutionPolicy Bypass -Command "& { mvn clean test '-DsuiteXmlFile=src/test/testng.xml' 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
) else if "!test_mode!"=="3" (
    goto menu
) else (
    echo.
    echo Invalid choice!
    timeout /t 2 >nul
    goto maven_test
)

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
    powershell -ExecutionPolicy Bypass -Command "$tag='!tag!'; & { mvn test -Dcucumber.filter.tags=\"$tag\" 2>&1 | Where-Object { $_ -notmatch 'WARNING: package sun.misc' -and $_ -notmatch 'WARNING: A terminally deprecated' -and $_ -notmatch 'WARNING: sun.misc.Unsafe' -and $_ -notmatch 'WARNING: Please consider reporting' } | ForEach-Object { Write-Host $_ } }"
    echo.
    pause
    goto menu
) else if "!test_type!"=="2" (
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
) else if "!test_type!"=="3" (
    goto menu
) else (
    echo.
    echo Invalid choice!
    timeout /t 2 >nul
    goto run_tests
)

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

:exit
cls
echo.
echo ======================================================================
echo   Thanks for using Playwright Automation!
echo ======================================================================
echo.
exit /b 0

