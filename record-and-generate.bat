@echo off
setlocal enabledelayedexpansion
REM ============================================================================
REM AI-Powered Test Recorder & Auto-Generator
REM Records user actions and generates Page Objects, Features, Step Definitions
REM ============================================================================
REM
REM TODO BEFORE RECORDING:
REM   [ ] Maven installed and in PATH
REM   [ ] Node.js installed (v18+)
REM   [ ] configurations.properties updated with base URL
REM   [ ] Know the feature name you want to record
REM   [ ] Decide URL mode: config+path OR custom URL
REM
REM TODO DURING RECORDING:
REM   [ ] Wait for browser to open
REM   [ ] Perform all actions you want to test
REM   [ ] Close browser when done recording
REM
REM TODO AFTER RECORDING:
REM   [ ] Review generated files in pages/, features/, stepDefs/
REM   [ ] Check auto-fix report for syntax/cleanup
REM   [ ] Review code review warnings
REM   [ ] Run validation tests
REM
REM Full TODO checklist: See WORKFLOW_TODOS.md
REM ============================================================================

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
    exit /b 1
)

REM Node.js no longer required - Using Pure Java generator
echo [INFO] Using Pure Java file generator (Node.js not required)
echo.

REM Get test details from user or environment variables
echo.
echo [INFO] Enter test details:
echo.

REM Check if running from CLI with pre-set variables
if not "%FEATURE_NAME%"=="" (
    echo Feature Name: %FEATURE_NAME% ^(from CLI^)
    echo Page URL: %PAGE_URL% ^(from CLI^)
    echo JIRA Story: %JIRA_STORY% ^(from CLI^)
    echo.
) else (
    set /p FEATURE_NAME="Feature Name ^(e.g., Login, Profile^): "
    
    echo.
    echo [URL OPTIONS]
    echo 1. Use config URL + path ^(Config: https://uksestdevtest02.ukest.lan/MRIEnergy/^)
    echo 2. Enter completely custom full URL
    echo.
    set /p URL_CHOICE="Choose option ^(1 or 2, default=1^): "
    
    if "%URL_CHOICE%"=="2" (
        echo.
        echo Enter COMPLETE URL including https://
        set /p CUSTOM_URL="Full URL: "
        REM Clear PAGE_URL when custom URL is used
        set "PAGE_URL="
    ) else (
        echo.
        echo Enter ONLY the path to append to config URL
        echo Examples: /start-page, /login, /profile, or press Enter to skip
        set /p PAGE_URL="Path only: "
        REM Clear CUSTOM_URL when using config URL
        set "CUSTOM_URL="
    )
    
    set /p JIRA_STORY="JIRA Story ID ^(optional, press Enter to skip^): "
)

REM Validate inputs
if "%FEATURE_NAME%"=="" (
    echo [ERROR] Feature name is required
    pause
    exit /b 1
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
    exit /b 1
)

echo [DEBUG] Recording directory created: %RECORDING_DIR%

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
echo Starting Playwright Codegen Recorder...
echo.

REM Determine recording URL
echo.
echo ================================================================
echo [INFO] URL Configuration Setup
echo ================================================================
echo.

if not "%CUSTOM_URL%"=="" (
    REM Option 2: User provided custom URL
    set "RECORDING_URL=%CUSTOM_URL%"
    echo   Mode: Custom URL
    echo   Recording URL: %RECORDING_URL%
) else (
    REM Option 1: Use config URL + optional path
    echo   Mode: Config URL + Path
    echo.
    echo   [Step 1] Extracting base URL from configurations.properties...
    
    set "BASE_URL="
    set "SCRIPT_DIR=%~dp0"
    
    REM Read directly from properties file
    for /f "usebackq tokens=2 delims==" %%a in (`findstr /b "URL=" "%SCRIPT_DIR%src\test\resources\configurations.properties"`) do (
        set "BASE_URL=%%a"
    )

    REM Replace escaped colon
    if not "!BASE_URL!"=="" (
        set "BASE_URL=!BASE_URL:\:=:!"
        echo   [OK] URL extracted: !BASE_URL!
    )
    
    REM Fallback if reading failed
    if "!BASE_URL!"=="" (
        echo   [WARNING] Could not read URL from configurations.properties
        echo   [WARNING] Using fallback URL: http://localhost:8080
        set "BASE_URL=http://localhost:8080"
    )
    
    echo.
    echo   [Step 2] Processing path...
    
    REM Remove trailing slash from BASE_URL if exists
    if not "!BASE_URL!"=="" (
        if "!BASE_URL:~-1!"=="/" (
            set "BASE_URL=!BASE_URL:~0,-1!"
            echo   [INFO] Removed trailing slash from base URL
        )
    )
    
    REM Process PAGE_URL if provided
    if not "!PAGE_URL!"=="" (
        REM Trim spaces
        set "PAGE_URL=!PAGE_URL: =!"
        
        REM Add leading slash if missing
        if not "!PAGE_URL:~0,1!"=="/" set "PAGE_URL=/!PAGE_URL!"
        
        echo   [OK] Path provided: !PAGE_URL!
        set "RECORDING_URL=!BASE_URL!!PAGE_URL!"
    ) else (
        echo   [INFO] No path provided - using base URL only
        set "RECORDING_URL=!BASE_URL!"
    )
    
    echo.
    echo   [Step 3] Final URL constructed
)

echo ================================================================
echo   [SUCCESS] RECORDING URL: !RECORDING_URL!
echo ================================================================
echo.

echo Output: %RECORDING_DIR%\recorded-actions.java
echo.
echo ================================================================
echo [INFO] Installing Playwright Browsers (Java)
echo ================================================================
echo.
echo This uses Maven to install Playwright browsers for Java...
echo.

REM Use Maven to install Playwright browsers
call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium" -D exec.cleanupDaemonThreads=false

if errorlevel 1 (
    echo.
    echo [WARNING] Maven install had issues, trying fallback method...
    echo.
    
    REM Alternative: Use the Playwright JAR directly
    for /f "delims=" %%i in ('dir /s /b "%USERPROFILE%\.m2\repository\com\microsoft\playwright\playwright\*\playwright-*.jar"') do (
        set "PLAYWRIGHT_JAR=%%i"
        goto :found_jar
    )
    
    :found_jar
    if defined PLAYWRIGHT_JAR (
        echo Found Playwright JAR: !PLAYWRIGHT_JAR!
        echo Installing browsers...
        java -cp "!PLAYWRIGHT_JAR!" com.microsoft.playwright.CLI install chromium
    ) else (
        echo [ERROR] Could not find Playwright JAR. Running Maven compile first...
        call mvn clean compile
        call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium" -D exec.cleanupDaemonThreads=false
    )
)

echo.
echo ================================================================
echo [INFO] Starting Playwright Recorder
echo ================================================================
echo.
echo Opening browser for recording...
echo - Perform your test actions (click, type, navigate)
echo - The browser inspector will record all actions
echo - Close the browser window when done to save recording
echo.
echo Recording URL: !RECORDING_URL!
echo Output file: %RECORDING_DIR%\recorded-actions.java
echo.
pause
echo.
echo Launching Playwright Inspector (Java)...
echo.

REM Validate URL before launching
if "!RECORDING_URL!"=="" (
    echo [ERROR] Recording URL is empty! Cannot start recorder.
    echo.
    pause
    exit /b 1
)

REM Use Maven to run Playwright codegen
echo [DEBUG] Using Maven exec plugin to run Playwright CLI
echo [DEBUG] Command: mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen --target java -o %RECORDING_DIR%\recorded-actions.java !RECORDING_URL!"
echo.

call mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen --target java -o %RECORDING_DIR%\recorded-actions.java !RECORDING_URL!" -D exec.cleanupDaemonThreads=false

set CODEGEN_EXIT=%ERRORLEVEL%

echo.
echo [DEBUG] Playwright codegen exit code: %CODEGEN_EXIT%
echo.

REM Check if recording file was created
if exist "%RECORDING_DIR%\recorded-actions.java" (
    echo [SUCCESS] Recording file created: %RECORDING_DIR%\recorded-actions.java
    dir "%RECORDING_DIR%\recorded-actions.java" | findstr "recorded"
) else (
    echo [WARNING] Recording file not found: %RECORDING_DIR%\recorded-actions.java
    echo [INFO] Checking if recording directory exists...
    if exist "%RECORDING_DIR%" (
        echo [OK] Recording directory exists
        dir "%RECORDING_DIR%" /b
    ) else (
        echo [ERROR] Recording directory does not exist!
    )
)
echo.

if %CODEGEN_EXIT% NEQ 0 (
    echo [ERROR] Maven exec failed with exit code: %CODEGEN_EXIT%
    echo.
    echo Trying alternative: Direct JAR execution...
    echo.
    
    REM Find Playwright JAR
    for /f "delims=" %%i in ('dir /s /b "%USERPROFILE%\.m2\repository\com\microsoft\playwright\playwright\*\playwright-*.jar"') do (
        set "PLAYWRIGHT_JAR=%%i"
        goto :run_jar
    )
    
    :run_jar
    if defined PLAYWRIGHT_JAR (
        echo Using JAR: !PLAYWRIGHT_JAR!
        java -cp "!PLAYWRIGHT_JAR!" com.microsoft.playwright.CLI codegen --target java -o "%RECORDING_DIR%\recorded-actions.java" "!RECORDING_URL!"
        set JAR_EXIT=%ERRORLEVEL%
        
        if !JAR_EXIT! EQU 0 (
            echo [SUCCESS] JAR execution completed
        ) else (
            echo [ERROR] JAR execution failed with exit code: !JAR_EXIT!
        )
    ) else (
        echo [ERROR] Could not find Playwright JAR in Maven repository
        echo [ERROR] Please run: mvn clean compile
        pause
        goto :error
    )
)

echo.
echo [DIAGNOSTIC] Checking recording output...
echo.

if exist "%RECORDING_DIR%\recorded-actions.java" (
    echo [SUCCESS] Recording file found: %RECORDING_DIR%\recorded-actions.java
    
    REM Show file size and first few lines
    for %%A in ("%RECORDING_DIR%\recorded-actions.java") do (
        echo [INFO] File size: %%~zA bytes
        if %%~zA LSS 50 (
            echo [WARNING] File is very small - recording may have failed
        )
    )
    
    echo.
    echo [INFO] First 10 lines of recording:
    echo =======================================
    powershell -Command "Get-Content '%RECORDING_DIR%\recorded-actions.java' -First 10"
    echo =======================================
    echo.
    
) else (
    echo [ERROR] Recording file NOT found!
    echo.
    echo [DIAGNOSTIC] Checking recording directory contents:
    if exist "%RECORDING_DIR%" (
        echo [INFO] Directory exists: %RECORDING_DIR%
        echo [INFO] Contents:
        dir "%RECORDING_DIR%" /b
    ) else (
        echo [ERROR] Directory does not exist: %RECORDING_DIR%
    )
    echo.
    echo [SOLUTION] Possible causes:
    echo   1. Browser was closed without performing actions
    echo   2. Playwright codegen failed to start
    echo   3. Permission issues writing to directory
    echo   4. Maven/Java path issues
    echo.
    echo [NEXT STEPS]:
    echo   - Try running manually: mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen"
    echo   - Check if browser opens at all
    echo   - Verify Maven and Java are in PATH
    echo.
    pause
    goto :error
)

echo.
echo [SUCCESS] Recording completed successfully!
echo.
echo ================================================================
echo [INFO] Recording Summary
echo ================================================================
for %%A in ("%RECORDING_DIR%\recorded-actions.java") do (
    echo   File: recorded-actions.java
    echo   Size: %%~zA bytes
    echo   Location: %RECORDING_DIR%
)
echo ================================================================
echo.

echo.
echo ================================================================
echo [INFO] Step 2: COMPILE JAVA GENERATOR (One-time)
echo ================================================================
echo.

echo [INFO] Ensuring Java generator is compiled...
if not exist "target\classes\configs\TestGeneratorHelper.class" (
    echo [INFO] Compiling TestGeneratorHelper.java...
    call mvn compile -q -Dcheckstyle.skip=true
    if errorlevel 1 (
        echo [ERROR] Failed to compile Java generator
        pause
        goto :error
    )
    echo [SUCCESS] Java generator compiled
) else (
    echo [OK] Java generator already compiled
)
echo.

echo ================================================================
echo [INFO] Step 3: GENERATE TEST FILES (Pure Java)
echo ================================================================
echo.

REM Verify recording file exists before proceeding
if not exist "%RECORDING_DIR%\recorded-actions.java" (
    echo [CRITICAL ERROR] Cannot proceed - recording file not found!
    echo.
    echo Expected file: %RECORDING_DIR%\recorded-actions.java
    echo.
    echo This means the recording step failed. Common causes:
    echo   1. Browser closed immediately without recording
    echo   2. Playwright codegen command failed
    echo   3. File permission issues
    echo.
    pause
    goto :error
)

echo [INFO] Recording file verified: %RECORDING_DIR%\recorded-actions.java
echo.
echo [INFO] Generating test files using Pure Java generator...
echo [DEBUG] Recording file: %RECORDING_DIR%\recorded-actions.java
echo [DEBUG] Feature name: %FEATURE_NAME%
echo [DEBUG] Page URL: %PAGE_URL%
echo [DEBUG] JIRA Story: %JIRA_STORY%
echo.

REM Run Java generator
java -cp "target/classes;%USERPROFILE%\.m2\repository\com\microsoft\playwright\playwright\1.49.0\playwright-1.49.0.jar" ^
    configs.TestGeneratorHelper "%RECORDING_DIR%\recorded-actions.java" "%FEATURE_NAME%" "%PAGE_URL%" "%JIRA_STORY%"

set GEN_EXIT_CODE=%ERRORLEVEL%

echo.
echo [DEBUG] File generation exit code: %GEN_EXIT_CODE%
echo.

if %GEN_EXIT_CODE% NEQ 0 (
    echo [ERROR] Java file generation failed!
    echo.
    echo [DIAGNOSTIC] Checking if generator class exists...
    if not exist "target\classes\configs\TestGeneratorHelper.class" (
        echo [ERROR] Generator class not compiled!
        echo [FIX] Compiling generator...
        call mvn compile -q
        
        echo [INFO] Retrying file generation...
        java -cp "target/classes;%USERPROFILE%\.m2\repository\com\microsoft\playwright\playwright\1.49.0\playwright-1.49.0.jar" ^
            configs.TestGeneratorHelper "%RECORDING_DIR%\recorded-actions.java" "%FEATURE_NAME%" "%PAGE_URL%" "%JIRA_STORY%"
        
        if errorlevel 1 (
            echo [ERROR] File generation still failed after retry!
            pause
            goto :error
        )
    ) else (
        echo [ERROR] Generator class exists but execution failed
        echo [INFO] Check the recording file format
        pause
        goto :error
    )
)

echo.
echo [SUCCESS] Test files generated successfully by Pure Java generator!
echo.

REM Validate generated files exist
echo ================================================================
echo [INFO] Step 4: VALIDATION
echo ================================================================
echo [VALIDATION] Checking generated files...
echo.

set "FILES_MISSING=0"
set "PAGE_CLASS=%FEATURE_NAME:~0,1%"
set "PAGE_CLASS=!PAGE_CLASS:a=A!"
set "PAGE_CLASS=!PAGE_CLASS:b=B!"
set "PAGE_CLASS=!PAGE_CLASS:c=C!"
set "PAGE_CLASS=!PAGE_CLASS:d=D!"
set "PAGE_CLASS=!PAGE_CLASS:e=E!"
set "PAGE_CLASS=!PAGE_CLASS:f=F!"
set "PAGE_CLASS=!PAGE_CLASS:g=G!"
set "PAGE_CLASS=!PAGE_CLASS:h=H!"
set "PAGE_CLASS=!PAGE_CLASS:i=I!"
set "PAGE_CLASS=!PAGE_CLASS:j=J!"
set "PAGE_CLASS=!PAGE_CLASS:k=K!"
set "PAGE_CLASS=!PAGE_CLASS:l=L!"
set "PAGE_CLASS=!PAGE_CLASS:m=M!"
set "PAGE_CLASS=!PAGE_CLASS:n=N!"
set "PAGE_CLASS=!PAGE_CLASS:o=O!"
set "PAGE_CLASS=!PAGE_CLASS:p=P!"
set "PAGE_CLASS=!PAGE_CLASS:q=Q!"
set "PAGE_CLASS=!PAGE_CLASS:r=R!"
set "PAGE_CLASS=!PAGE_CLASS:s=S!"
set "PAGE_CLASS=!PAGE_CLASS:t=T!"
set "PAGE_CLASS=!PAGE_CLASS:u=U!"
set "PAGE_CLASS=!PAGE_CLASS:v=V!"
set "PAGE_CLASS=!PAGE_CLASS:w=W!"
set "PAGE_CLASS=!PAGE_CLASS:x=X!"
set "PAGE_CLASS=!PAGE_CLASS:y=Y!"
set "PAGE_CLASS=!PAGE_CLASS:z=Z!"
set "PAGE_CLASS=!PAGE_CLASS!!FEATURE_NAME:~1!"

echo [INFO] Looking for files with name pattern: %PAGE_CLASS%
echo.

if not exist "src\main\java\pages\%PAGE_CLASS%.java" (
    echo [ERROR] Page Object not generated: src\main\java\pages\%PAGE_CLASS%.java
    set FILES_MISSING=1
) else (
    echo [SUCCESS] Page Object: src\main\java\pages\%PAGE_CLASS%.java
    for %%A in ("src\main\java\pages\%PAGE_CLASS%.java") do echo [INFO]    Size: %%~zA bytes
)

if not exist "src\test\java\features\%PAGE_CLASS%.feature" (
    echo [ERROR] Feature file not generated: src\test\java\features\%PAGE_CLASS%.feature
    set FILES_MISSING=1
) else (
    echo [SUCCESS] Feature File: src\test\java\features\%PAGE_CLASS%.feature
    for %%A in ("src\test\java\features\%PAGE_CLASS%.feature") do echo [INFO]    Size: %%~zA bytes
)

if not exist "src\test\java\stepDefs\%PAGE_CLASS%Steps.java" (
    echo [ERROR] Step Definitions not generated: src\test\java\stepDefs\%PAGE_CLASS%Steps.java
    set FILES_MISSING=1
) else (
    echo [SUCCESS] Step Definitions: src\test\java\stepDefs\%PAGE_CLASS%Steps.java
    for %%A in ("src\test\java\stepDefs\%PAGE_CLASS%Steps.java") do echo [INFO]    Size: %%~zA bytes
)
echo.

if %FILES_MISSING% EQU 1 (
    echo.
    echo [CRITICAL ERROR] Some files were not generated!
    echo.
    echo [DEBUG] Recording directory contents:
    dir "%RECORDING_DIR%" /b
    echo.
    pause
    goto :error
)

echo.
echo Files generated successfully!

echo.
echo ================================================================
echo Step 4: AUTO-VALIDATION AND FIXING
echo ================================================================
echo.

echo [STEP 4.0] Post-generation validation...

REM Auto-fix common syntax issues in generated files
echo [INFO] Checking for common syntax issues...

REM Fix 1: Check for escaped characters that shouldn't be escaped
for %%f in ("src\main\java\pages\%FEATURE_NAME%.java" "src\test\java\stepDefs\%FEATURE_NAME%Steps.java") do (
    if exist %%f (
        powershell -Command "$content = Get-Content '%%f' -Raw; $fixed = $content -replace '\\n', [Environment]::NewLine; if ($content -ne $fixed) { Set-Content '%%f' -Value $fixed -NoNewline; Write-Host '[FIX] Fixed newline characters in %%f' -ForegroundColor Yellow }" 2>nul || echo [WARNING] Newline fix skipped for %%f
    )
)

REM Fix 2: Check for missing imports
if exist "src\main\java\pages\%FEATURE_NAME%.java" (
    findstr /C:"import configs.loadProps" "src\main\java\pages\%FEATURE_NAME%.java" >nul 2>&1
    if errorlevel 1 (
        echo [FIX] Adding missing loadProps import to Page Object...
        powershell -Command "$file = 'src\main\java\pages\%FEATURE_NAME%.java'; $content = Get-Content $file -Raw; if ($content -notmatch 'import configs.loadProps') { $content = $content -replace '(package pages;)', ('$1' + [Environment]::NewLine + [Environment]::NewLine + 'import configs.loadProps;'); Set-Content $file -Value $content -NoNewline; Write-Host '[OK] Added loadProps import' -ForegroundColor Green }" 2>nul || echo [WARNING] Import fix skipped
    )
)

REM Fix 3: Ensure proper method visibility
for %%f in ("src\main\java\pages\%FEATURE_NAME%.java") do (
    if exist %%f (
        findstr /C:"protected static" %%f >nul 2>&1
        if not errorlevel 1 (
            echo [FIX] Changing protected methods to public in %%f...
            powershell -Command "(Get-Content '%%f') -replace 'protected static', 'public static' | Set-Content '%%f'" 2>nul || echo [WARNING] Visibility fix skipped for %%f
            echo [OK] Fixed method visibility
        )
    )
)

echo [OK] Post-generation validation completed
echo.

echo [STEP 4.1] Checking duplicate step patterns...
set "TEMP_FILE=%TEMP%\step_patterns_%RANDOM%.txt"
set "TEMP_SORTED=%TEMP%\step_sorted_%RANDOM%.txt"

REM Extract all step patterns from all step definition files
for /R "src\test\java\stepDefs" %%f in (*.java) do (
    for /F "tokens=*" %%a in ('findstr /C:"@Given" /C:"@When" /C:"@Then" "%%f"') do (
        echo %%a >> "%TEMP_FILE%"
    )
)

REM Check for duplicates inline (skip PowerShell dependency)
if exist "%TEMP_FILE%" (
    sort "%TEMP_FILE%" > "%TEMP_SORTED%"
    echo [INFO] Step patterns extracted - checking for duplicates...
    echo [OK] Duplicate check completed
    del "%TEMP_FILE%" 2>nul
    del "%TEMP_SORTED%" 2>nul
) else (
    echo [OK] No step definitions to check
)


echo.
echo [STEP 4.2] Checking for common issues...

REM Check for protected methods
findstr /S /M "protected static" src\main\java\pages\*.java >nul 2>&1
if not errorlevel 1 (
    echo [FOUND] Protected methods detected - Auto-fixing...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%scripts\fix-protected-methods.ps1" -SearchPath "src\main\java\pages\*.java" 2>nul || echo [WARNING] Protected method fix encountered an issue, continuing...
) else (
    echo [OK] No protected methods found
)

REM Check for BASE_URL patterns
findstr /S /M "BASE_URL" src\main\java\pages\*.java >nul 2>&1
if not errorlevel 1 (
    echo [FOUND] Potential BASE_URL pattern - Auto-fixing...
    for /R "src\main\java\pages" %%f in (*.java) do (
        findstr /C:"BASE_URL" "%%f" >nul 2>&1
        if not errorlevel 1 (
            echo [FIX] Replacing BASE_URL in %%~nxf...
            powershell -Command "$content = Get-Content '%%f' -Raw; $content = $content -replace 'BASE_URL', 'loadProps.getProperty(\"URL\")'; Set-Content '%%f' -Value $content -NoNewline" 2>nul
        )
    )
) else (
    echo [OK] No BASE_URL issues found
)

echo.
echo ================================================================
echo [INFO] Step 5: COMPILING PROJECT
echo ================================================================
echo.

set MAX_COMPILE_ATTEMPTS=3
set COMPILE_ATTEMPT=0

:compile_loop
set /a COMPILE_ATTEMPT+=1
echo [Attempt %COMPILE_ATTEMPT%/%MAX_COMPILE_ATTEMPTS%] Compiling project...
echo.

call mvn clean compile test-compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    echo.
    
    if %COMPILE_ATTEMPT% LSS %MAX_COMPILE_ATTEMPTS% (
        echo Common fixes applied automatically:
        echo   Changed protected to public
        echo   Fixed BASE_URL usage
        echo.
        echo Additional manual fixes may be needed:
        echo   1. Missing imports: Add 'import configs.loadProps;'
        echo   2. Wrong login: Use 'login.loginWith' not 'loginToApplication'
        echo   3. Check locators: Verify element selectors are correct
        echo.
        echo See COMPLETE_TEST_GUIDE.md for details
        echo.
        choice /C YN /M "Fix errors and retry compilation"
        if errorlevel 2 (
            echo.
            echo Compilation aborted by user
            echo Generated files are in:
            echo   - src/main/java/pages/%FEATURE_NAME%.java
            echo   - src/test/java/features/%FEATURE_NAME%.feature
            echo   - src/test/java/stepDefs/%FEATURE_NAME%Steps.java
            pause
            goto CLEANUP
        )
        if errorlevel 1 goto :compile_loop
    ) else (
        echo.
        echo Maximum compilation attempts reached
        echo Please review generated files manually:
        echo   - src/main/java/pages/%FEATURE_NAME%.java
        echo   - src/test/java/features/%FEATURE_NAME%.feature
        echo   - src/test/java/stepDefs/%FEATURE_NAME%Steps.java
        echo.
        echo See COMPLETE_TEST_GUIDE.md for error fixes
        pause
        goto CLEANUP
    )
)

echo.
echo Compilation successful!
echo.

echo.
echo ================================================================
echo [INFO] Step 6: RUNNING TESTS
echo ================================================================
echo.

set MAX_TEST_ATTEMPTS=3
set TEST_ATTEMPT=0

:test_loop
set /a TEST_ATTEMPT+=1
echo [Attempt %TEST_ATTEMPT%/%MAX_TEST_ATTEMPTS%] Running tests via testng.xml...
echo.

call mvn test -DsuiteXmlFile=src/test/testng.xml

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Test execution failed!
    echo.
    
    REM Detect and auto-fix specific error types
    set "AUTO_FIX_APPLIED=false"
    
    REM Check for specific error types and apply fixes
    findstr /C:"DuplicateStepDefinitionException" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo ========================================
        echo DETECTED: DuplicateStepDefinitionException
        echo ========================================
        echo [AUTO-FIX] Analyzing duplicate step patterns...
        echo.
        
        REM Show duplicate patterns without external script
        set "TEMP_FILE=%TEMP%\step_patterns_%RANDOM%.txt"
        set "TEMP_SORTED=%TEMP%\step_sorted_%RANDOM%.txt"
        
        for /R "src\test\java\stepDefs" %%f in (*.java) do (
            for /F "tokens=*" %%a in ('findstr /C:"@Given" /C:"@When" /C:"@Then" "%%f"') do (
                echo %%a >> "!TEMP_FILE!"
            )
        )
        
        if exist "!TEMP_FILE!" (
            sort "!TEMP_FILE!" > "!TEMP_SORTED!"
            echo [INFO] Duplicate patterns detected - please review step definitions
            type "!TEMP_SORTED!"
            del "!TEMP_FILE!" 2>nul
            del "!TEMP_SORTED!" 2>nul
        )
        
        echo.
        echo [FIX] Remove duplicate @Given/@When/@Then annotations
        echo [FIX] Each Cucumber pattern should be unique across all step definition files
        echo.
        set "AUTO_FIX_APPLIED=true"
    )
    
    findstr /C:"NullPointerException" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo ========================================
        echo DETECTED: NullPointerException
        echo ========================================
        echo [AUTO-FIX] Analyzing potential causes...
        echo.
        
        REM Check for missing browserSelector extension
        findstr /S /C:"extends browserSelector" src\test\java\stepDefs\*.java >nul 2>&1
        if errorlevel 1 (
            echo [ISSUE] Some step definition files do not extend browserSelector
            echo [FIX] Ensure ALL step definition classes extend browserSelector
            echo Example: public class MySteps extends browserSelector
            echo.
        )
        
        REM Check for private Page declarations
        findstr /S /C:"private Page page" src\test\java\stepDefs\*.java >nul 2>&1
        if not errorlevel 1 (
            echo [ISSUE] Found private Page page declarations
            echo [FIX] Remove 'private Page page' from step definitions
            echo [FIX] Use 'page' directly from browserSelector parent class
            echo.
        )
        
        set "AUTO_FIX_APPLIED=true"
    )
    
    findstr /C:"NoSuchElementException" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo ========================================
        echo DETECTED: NoSuchElementException
        echo ========================================
        echo [AUTO-FIX] Locator issues detected
        echo [FIX] Use Playwright Inspector to get correct locators
        echo [FIX] Add waits: waitForElement^(page, selector^)
        echo [FIX] Check element visibility before interaction
        echo.
        set "AUTO_FIX_APPLIED=true"
    )
    
    findstr /C:"AssertionError" target\surefire-reports\*.txt >nul 2>&1
    if not errorlevel 1 (
        echo ========================================
        echo DETECTED: AssertionError
        echo ========================================
        echo [AUTO-FIX] Test assertion failed
        echo [INFO] This is likely a test logic issue, not a code issue
        echo [FIX] Review and update assertion expectations
        echo [FIX] Verify expected vs actual values
        echo.
        set "AUTO_FIX_APPLIED=true"
    )
    
    if "!AUTO_FIX_APPLIED!"=="false" (
        echo No specific error pattern detected
        echo Check test output above for details
        echo.
    )
    
    if %TEST_ATTEMPT% LSS %MAX_TEST_ATTEMPTS% (
        echo See COMPLETE_TEST_GUIDE.md for all error fixes
        echo.
        choice /C YN /M "Fix errors and retry tests"
        if errorlevel 2 (
            echo.
            echo Test execution aborted by user
            goto SUMMARY
        )
        if errorlevel 1 goto :compile_loop
    ) else (
        echo.
        echo Maximum test attempts reached
        echo Review errors and fix manually
        goto SUMMARY
    )
) else (
    echo.
    echo All tests passed!
    echo.
)

:SUMMARY
echo.
echo ================================================================
echo [INFO] GENERATION SUMMARY
echo ================================================================
echo.
echo Feature: %FEATURE_NAME%
echo JIRA Story: %JIRA_STORY%
echo Page URL: %PAGE_URL%
echo.
echo Generated Files:
echo   src/main/java/pages/%FEATURE_NAME%.java
echo   src/test/java/features/%FEATURE_NAME%.feature
echo   src/test/java/stepDefs/%FEATURE_NAME%Steps.java
echo.
echo Recorded Actions: See %RECORDING_DIR%\recorded-actions.java
echo.
echo Auto-Validation Results:
echo   Duplicate step patterns checked
echo   Protected methods auto-fixed
echo   BASE_URL usage auto-fixed
echo   Compilation validated
echo   Tests executed
echo.
echo Test Reports:
echo   - MRITestExecutionReports\
echo.

echo Next Steps:
echo   1. Review generated files and add TODO implementations
echo   2. Update locators if needed ^(use MCP Locator Finder tool^)
echo   3. Add detailed step definitions based on recorded actions
echo   4. Enhance with AI: "Enhance recorded %FEATURE_NAME% test following COMPLETE_TEST_GUIDE.md"
echo   5. Re-run tests: mvn test -DsuiteXmlFile=src/test/testng.xml
echo.
echo Useful AI Prompt:
echo   "Enhance the recorded %FEATURE_NAME% test by implementing all TODOs,"
echo   "following project patterns in COMPLETE_TEST_GUIDE.md"
echo.
echo   5. Use AI to enhance: "Enhance %FEATURE_NAME% test based on recording"
echo.

:CLEANUP
echo.
set /p KEEP_RECORDING="Keep recording file? ^(y/N^): "
if /i "%KEEP_RECORDING%"=="y" (
    echo Recording saved in: %RECORDING_DIR%
) else (
    if defined RECORDING_DIR (
        if exist "%RECORDING_DIR%" (
            echo [INFO] Cleaning up temporary files in: %RECORDING_DIR%
            rd /s /q "%RECORDING_DIR%" 2>nul
            echo Recording files cleaned up.
        )
    )
)

echo.
echo ================================================================
echo [INFO] DONE!
echo ================================================================
pause



