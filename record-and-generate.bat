@echo off
REM ============================================================================
REM AI-Powered Test Recorder & Auto-Generator
REM Records user actions and generates Page Objects, Features, Step Definitions
REM ============================================================================

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  🎥 PLAYWRIGHT RECORDER - Auto-Generate Test Scripts          ║
echo ╔════════════════════════════════════════════════════════════════╗
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found. Please install Maven first.
    pause
    exit /b 1
)

REM Check if Node.js is installed
where node >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js not found. Please install Node.js first.
    pause
    exit /b 1
)

REM Get test details from user or environment variables
echo.
echo 📝 Enter test details:
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
        set PAGE_URL=
    ) else (
        echo.
        echo Enter ONLY the path to append to config URL
        echo Examples: /start-page, /login, /profile, or press Enter for /
        set /p PAGE_URL="Path only: "
        set CUSTOM_URL=
    )
    
    set /p JIRA_STORY="JIRA Story ID ^(optional, press Enter to skip^): "
)

REM Validate inputs
if "%FEATURE_NAME%"=="" (
    echo [ERROR] Feature name is required
    pause
    exit /b 1
)

if "%PAGE_URL%"=="" if "%CUSTOM_URL%"=="" (
    set PAGE_URL=/
)

if "%JIRA_STORY%"=="" (
    set JIRA_STORY=AUTO-GEN
)

REM Create output directory for recording
set RECORDING_DIR=temp_recording_%RANDOM%
mkdir %RECORDING_DIR% 2>nul

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

REM Determine recording URL
if not "%CUSTOM_URL%"=="" (
    set "RECORDING_URL=%CUSTOM_URL%"
    echo Recording URL: %RECORDING_URL% ^(custom URL^)
) else (
    REM Get base URL from configurations.properties using PowerShell
    set "BASE_URL="
    for /f "usebackq delims=" %%a in (`powershell -NoProfile -Command "$props = Get-Content 'src\test\resources\configurations.properties'; $urlLine = $props ^| Where-Object { $_ -match '^URL=' }; if ($urlLine) { ($urlLine -split '=', 2)[1] -replace '\\:', ':' }"`) do set "BASE_URL=%%a"
    
    if "%BASE_URL%"=="" (
        echo [WARNING] Could not read URL from configurations.properties
        set "BASE_URL=http://localhost:8080"
    )
    
    set "RECORDING_URL=%BASE_URL%%PAGE_URL%"
    echo Recording URL: %RECORDING_URL% ^(from config + path^)
    echo Base URL read: %BASE_URL%
)

echo Output: %RECORDING_DIR%\recorded-actions.java
echo.
echo [INFO] Installing Playwright if needed...
call npx -y playwright install chromium >nul 2>&1

echo [INFO] Opening browser for recording...
echo [INFO] Perform your actions in the browser
echo [INFO] Close the browser window when done recording
echo.

REM Use npx playwright codegen directly (more reliable than Maven)
call npx playwright codegen --target java -o "%RECORDING_DIR%\recorded-actions.java" "%RECORDING_URL%"

if errorlevel 1 (
    echo [WARN] npx execution had issues, trying Maven method...
    echo.
    call mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" ^
        -Dexec.args="codegen --target java %RECORDING_URL% --output %RECORDING_DIR%\recorded-actions.java"
)

if not exist "%RECORDING_DIR%\recorded-actions.java" (
    echo.
    echo [INFO] No recording file generated. Trying alternative method...
    echo.
    
    REM Alternative: Use npx playwright codegen with correct URL
    call npx playwright codegen --target java "%RECORDING_URL%" > "%RECORDING_DIR%\recorded-actions.java" 2>&1
)

if not exist "%RECORDING_DIR%\recorded-actions.java" (
    echo [ERROR] Recording failed. Please ensure Playwright is installed.
    pause
    if defined RECORDING_DIR (
        if exist "%RECORDING_DIR%" (
            rd /s /q "%RECORDING_DIR%" 2>nul
        )
    )
    exit /b 1
)

echo.
echo ✅ Recording completed successfully!
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo 🤖 Step 2: AI ANALYSIS OF RECORDED ACTIONS
echo ════════════════════════════════════════════════════════════════
echo.

REM Extract locators and actions from recording - Create temp PowerShell script
echo [INFO] Analyzing recorded actions intelligently...
set "PS_SCRIPT=%TEMP%\extract_locators_%RANDOM%.ps1"
(
echo $recording = Get-Content '%RECORDING_DIR%\recorded-actions.java' -Raw
echo $actions = @^(^)
echo $lines = $recording -split "`n"
echo $counter = 1
echo.
echo foreach ^($line in $lines^) {
echo     # Match various Playwright actions with their selectors
echo     if ^($line -match 'page\.click\^("^([^^"]+^)"\^)'^) {
echo         $selector = $matches[1]
echo         $actions += @{
echo             id = $counter++
echo             action = 'click'
echo             selector = $selector
echo             methodName = 'clickElement' + $counter
echo             stepText = 'user clicks on element'
echo         }
echo     }
echo     elseif ^($line -match 'page\.fill\^("^([^^"]+^)", *"^([^^"]+^)"\^)'^) {
echo         $selector = $matches[1]
echo         $value = $matches[2]
echo         $actions += @{
echo             id = $counter++
echo             action = 'fill'
echo             selector = $selector
echo             value = $value
echo             methodName = 'fillElement' + $counter
echo             stepText = 'user enters text into element'
echo         }
echo     }
echo     elseif ^($line -match 'page\.selectOption\^("^([^^"]+^)", *"^([^^"]+^)"\^)'^) {
echo         $selector = $matches[1]
echo         $value = $matches[2]
echo         $actions += @{
echo             id = $counter++
echo             action = 'select'
echo             selector = $selector
echo             value = $value
echo             methodName = 'selectOption' + $counter
echo             stepText = 'user selects option from dropdown'
echo         }
echo     }
echo     elseif ^($line -match 'page\.check\^("^([^^"]+^)"\^)'^) {
echo         $selector = $matches[1]
echo         $actions += @{
echo             id = $counter++
echo             action = 'check'
echo             selector = $selector
echo             methodName = 'checkElement' + $counter
echo             stepText = 'user checks checkbox'
echo         }
echo     }
echo     elseif ^($line -match 'page\.press\^("^([^^"]+^)", *"^([^^"]+^)"\^)'^) {
echo         $selector = $matches[1]
echo         $key = $matches[2]
echo         $actions += @{
echo             id = $counter++
echo             action = 'press'
echo             selector = $selector
echo             value = $key
echo             methodName = 'pressKey' + $counter
echo             stepText = 'user presses key on element'
echo         }
echo     }
echo     elseif ^($line -match 'page\.navigate\^("^([^^"]+^)"\^)'^) {
echo         $url = $matches[1]
echo         $actions += @{
echo             id = $counter++
echo             action = 'navigate'
echo             url = $url
echo             methodName = 'navigateTo'
echo             stepText = 'user navigates to page'
echo         }
echo     }
echo }
echo.
echo # Save as JSON with proper formatting
echo $json = $actions ^| ConvertTo-Json -Depth 10
echo $json ^| Out-File '%RECORDING_DIR%\actions.json' -Encoding utf8
echo Write-Host "[INFO] Extracted $^($actions.Count^) actions from recording" -ForegroundColor Cyan
echo.
echo # Display summary
echo Write-Host "Actions breakdown:" -ForegroundColor Yellow
echo $actions ^| Group-Object action ^| ForEach-Object { Write-Host "  - $^($_.Name^): $^($_.Count^)" -ForegroundColor Cyan }
) > "%PS_SCRIPT%"

REM Execute PowerShell script with error handling
echo [INFO] Running smart action extraction...
powershell -ExecutionPolicy Bypass -File "%PS_SCRIPT%" >"%TEMP%\ps_output_%RANDOM%.log" 2>&1
set PS_EXIT_CODE=%ERRORLEVEL%

if %PS_EXIT_CODE% NEQ 0 (
    echo.
    echo [WARNING] Smart extraction encountered an error. Using fallback method...
    echo.
    
    REM Fallback: Simple extraction using basic pattern matching
    set "FALLBACK_SCRIPT=%TEMP%\fallback_extract_%RANDOM%.ps1"
    (
        echo $recording = Get-Content '%RECORDING_DIR%\recorded-actions.java' -Raw
        echo $actions = @^(^)
        echo $counter = 1
        echo.
        echo # Simple extraction - find click and fill patterns
        echo $recording -split "`n" ^| ForEach-Object {
        echo     $line = $_
        echo     if ^($line -match 'page\.click'^^) {
        echo         if ^($line -match '\"^([^^\"]+^)\"'^^) {
        echo             $actions += @{
        echo                 id = $counter++
        echo                 action = 'click'
        echo                 selector = $matches[1]
        echo                 methodName = 'clickElement' + $counter
        echo                 stepText = 'user clicks element'
        echo             }
        echo         }
        echo     }
        echo     elseif ^($line -match 'page\.fill'^^) {
        echo         if ^($line -match '\"^([^^\"]+^)\".*\"^([^^\"]+^)\"'^^) {
        echo             $actions += @{
        echo                 id = $counter++
        echo                 action = 'fill'
        echo                 selector = $matches[1]
        echo                 value = $matches[2]
        echo                 methodName = 'fillElement' + $counter
        echo                 stepText = 'user fills element'
        echo             }
        echo         }
        echo     }
        echo }
        echo.
        echo if ^($actions.Count -eq 0^^) {
        echo     $actions += @{
        echo         id = 1
        echo         action = 'navigate'
        echo         url = '%PAGE_URL%'
        echo         methodName = 'navigateTo'
        echo         stepText = 'user navigates'
        echo     }
        echo }
        echo.
        echo $json = $actions ^| ConvertTo-Json -Depth 10
        echo $json ^| Out-File '%RECORDING_DIR%\actions.json' -Encoding utf8
        echo Write-Host "[INFO] Extracted $^($actions.Count^^) actions using fallback method" -ForegroundColor Yellow
    ) > "%FALLBACK_SCRIPT%"
    
    powershell -ExecutionPolicy Bypass -File "%FALLBACK_SCRIPT%" 2>nul
    del "%FALLBACK_SCRIPT%" 2>nul
    
    echo [INFO] Fallback extraction completed
)

del "%PS_SCRIPT%" 2>nul

REM Validate that actions.json was created
if not exist "%RECORDING_DIR%\actions.json" (
    echo.
    echo [WARNING] No actions extracted. Creating minimal structure...
    (
        echo [
        echo   ^{
        echo     "id": 1,
        echo     "action": "navigate",
        echo     "url": "%PAGE_URL%",
        echo     "methodName": "navigateTo",
        echo     "stepText": "user navigates to page"
        echo   ^}
        echo ]
    ) > "%RECORDING_DIR%\actions.json"
    echo [INFO] Created minimal actions structure
)

echo.
echo ════════════════════════════════════════════════════════════════
echo 📄 Step 3: GENERATE TEST FILES
echo ════════════════════════════════════════════════════════════════
echo.

REM Generate files using a temporary JavaScript file with smart action parsing
echo [INFO] Generating intelligent test files from recorded actions...
set "GEN_SCRIPT=%TEMP%\generate_files_%RANDOM%.js"
(
echo const fs = require('fs'^);
echo const path = require('path'^);
echo const featureName = process.argv[2];
echo const pageUrl = process.argv[3];
echo const jiraStory = process.argv[4];
echo const recordingDir = process.argv[5];
echo.
echo // Load recorded actions
echo let actions = [];
echo try {
echo   const content = fs.readFileSync^(path.join^(recordingDir, 'actions.json'^), 'utf8'^);
echo   actions = JSON.parse^(content^);
echo   if ^(!Array.isArray^(actions^)^) actions = [actions];
echo   console.log^(`[INFO] Loaded ${actions.length} actions from recording`^);
echo } catch^(e^) {
echo   console.log^('[WARN] No actions.json found, generating basic template'^);
echo }
echo.
echo const className = featureName.charAt^(0^).toUpperCase^(^) + featureName.slice^(1^);
echo.
echo // Generate locator constants from actions
echo function generateLocators^(^) {
echo   if ^(actions.length === 0^) return [];
echo   const locators = [];
echo   actions.forEach^(^(action, idx^) =^> {
echo     if ^(action.selector^) {
echo       const name = `ELEMENT_${idx + 1}`;
echo       locators.push^(`    private static final String ${name} = "${action.selector}";`^);
echo     }
echo   }^);
echo   return locators;
echo }
echo.
echo // Generate page object methods from actions
echo function generateMethods^(^) {
echo   if ^(actions.length === 0^) {
echo     return ['    public static void navigateTo^(Page page^) {',
echo             '        page.navigate^(loadProps.getProperty^("URL"^) + PAGE_PATH^);',
echo             '    }'];
echo   }
echo   const methods = ['    public static void navigateTo^(Page page^) {',
echo                     '        page.navigate^(loadProps.getProperty^("URL"^) + PAGE_PATH^);',
echo                     '    }', ''];
echo   actions.forEach^(^(action, idx^) =^> {
echo     const locatorVar = `ELEMENT_${idx + 1}`;
echo     methods.push^(`    // TODO: Implement ${action.action} action`^);
echo     switch^(action.action^) {
echo       case 'click':
echo         methods.push^(`    public static void ${action.methodName}^(Page page^) {`^);
echo         methods.push^(`        clickOnElement^(${locatorVar}^);`^);
echo         methods.push^(`    }`, ''^);
echo         break;
echo       case 'fill':
echo         methods.push^(`    public static void ${action.methodName}^(Page page, String text^) {`^);
echo         methods.push^(`        enterText^(${locatorVar}, text^);`^);
echo         methods.push^(`    }`, ''^);
echo         break;
echo       case 'select':
echo         methods.push^(`    public static void ${action.methodName}^(Page page, String option^) {`^);
echo         methods.push^(`        selectDropDownValueByText^(${locatorVar}, option^);`^);
echo         methods.push^(`    }`, ''^);
echo         break;
echo       case 'check':
echo         methods.push^(`    public static void ${action.methodName}^(Page page^) {`^);
echo         methods.push^(`        clickOnElement^(${locatorVar}^);`^);
echo         methods.push^(`    }`, ''^);
echo         break;
echo       case 'press':
echo         methods.push^(`    public static void ${action.methodName}^(Page page^) {`^);
echo         methods.push^(`        page.locator^(${locatorVar}^).press^("${action.value}"^);`^);
echo         methods.push^(`    }`, ''^);
echo         break;
echo     }
echo   }^);
echo   return methods;
echo }
echo.
echo // Generate Page Object
echo const locators = generateLocators^(^);
echo const methods = generateMethods^(^);
echo const pageObject = [
echo   'package pages;',
echo   'import com.microsoft.playwright.Page;',
echo   'import configs.loadProps;',
echo   'import static configs.utils.*;',
echo   '',
echo   '/**',
echo   ' * Page Object for ' + className,
echo   ' * Auto-generated from Playwright recording',
echo   ' * @story ' + jiraStory,
echo   ' */',
echo   'public class ' + className + ' extends BasePage {',
echo   '    private static final String PAGE_PATH = "' + pageUrl + '";',
echo   '',
echo   ...locators,
echo   '',
echo   ...methods,
echo   '}'
echo ].join^('\n'^);
echo.
echo // Generate step texts from actions
echo function generateStepTexts^(^) {
echo   if ^(actions.length === 0^) {
echo     return ['    Given user navigates to ' + className + ' page'];
echo   }
echo   const steps = ['    Given user navigates to ' + className + ' page'];
echo   actions.forEach^(^(action, idx^) =^> {
echo     switch^(action.action^) {
echo       case 'click':
echo         steps.push^(`    When user clicks on element ${idx + 1}`^);
echo         break;
echo       case 'fill':
echo         steps.push^(`    And user enters "{string}" into element ${idx + 1}`^);
echo         break;
echo       case 'select':
echo         steps.push^(`    And user selects "{string}" from dropdown ${idx + 1}`^);
echo         break;
echo       case 'check':
echo         steps.push^(`    And user checks checkbox ${idx + 1}`^);
echo         break;
echo       case 'press':
echo         steps.push^(`    And user presses key on element ${idx + 1}`^);
echo         break;
echo     }
echo   }^);
echo   steps.push^('    Then page should be updated'^);
echo   return steps;
echo }
echo.
echo // Generate Feature File
echo const stepTexts = generateStepTexts^(^);
echo const featureFile = [
echo   '@' + jiraStory + ' @' + className,
echo   'Feature: ' + className + ' Test',
echo   '  ',
echo   '  Scenario: Complete ' + className + ' workflow',
echo   ...stepTexts
echo ].join^('\n'^);
echo.
echo // Generate step definitions from actions
echo function generateStepDefs^(^) {
echo   if ^(actions.length === 0^) {
echo     return ['    @Given^("user navigates to ' + className + ' page"^)',
echo             '    public void navigateTo^(^) {',
echo             '        ' + className + '.navigateTo^(page^);',
echo             '    }'];
echo   }
echo   const steps = ['    @Given^("user navigates to ' + className + ' page"^)',
echo                   '    public void navigateTo^(^) {',
echo                   '        ' + className + '.navigateTo^(page^);',
echo                   '    }', ''];
echo   actions.forEach^(^(action, idx^) =^> {
echo     steps.push^(`    // TODO: Implement step for ${action.action} action`^);
echo     switch^(action.action^) {
echo       case 'click':
echo         steps.push^(`    @When^("user clicks on element ${idx + 1}"^)`^);
echo         steps.push^(`    public void clickElement${idx + 1}^(^) {`^);
echo         steps.push^(`        ${className}.${action.methodName}^(page^);`^);
echo         steps.push^(`    }`, ''^);
echo         break;
echo       case 'fill':
echo         steps.push^(`    @And^("user enters {string} into element ${idx + 1}"^)`^);
echo         steps.push^(`    public void fillElement${idx + 1}^(String text^) {`^);
echo         steps.push^(`        ${className}.${action.methodName}^(page, text^);`^);
echo         steps.push^(`    }`, ''^);
echo         break;
echo       case 'select':
echo         steps.push^(`    @And^("user selects {string} from dropdown ${idx + 1}"^)`^);
echo         steps.push^(`    public void selectOption${idx + 1}^(String option^) {`^);
echo         steps.push^(`        ${className}.${action.methodName}^(page, option^);`^);
echo         steps.push^(`    }`, ''^);
echo         break;
echo       case 'check':
echo         steps.push^(`    @And^("user checks checkbox ${idx + 1}"^)`^);
echo         steps.push^(`    public void checkElement${idx + 1}^(^) {`^);
echo         steps.push^(`        ${className}.${action.methodName}^(page^);`^);
echo         steps.push^(`    }`, ''^);
echo         break;
echo       case 'press':
echo         steps.push^(`    @And^("user presses key on element ${idx + 1}"^)`^);
echo         steps.push^(`    public void pressKey${idx + 1}^(^) {`^);
echo         steps.push^(`        ${className}.${action.methodName}^(page^);`^);
echo         steps.push^(`    }`, ''^);
echo         break;
echo     }
echo   }^);
echo   steps.push^('    @Then^("page should be updated"^)'^);
echo   steps.push^('    public void verifyPageUpdated^(^) {'^);
echo   steps.push^('        // TODO: Add verification logic'^);
echo   steps.push^('    }'^);
echo   return steps;
echo }
echo.
echo // Generate Step Definitions
echo const stepMethods = generateStepDefs^(^);
echo const stepDefs = [
echo   'package stepDefs;',
echo   'import configs.browserSelector;',
echo   'import io.cucumber.java.en.*;',
echo   'import pages.' + className + ';',
echo   '',
echo   '/**',
echo   ' * Step Definitions for ' + className,
echo   ' * Auto-generated from Playwright recording',
echo   ' * @story ' + jiraStory,
echo   ' */',
echo   'public class ' + className + 'Steps extends browserSelector {',
echo   '',
echo   ...stepMethods,
echo   '}'
echo ].join^('\n'^);
echo.
echo.
echo // Write files
echo fs.writeFileSync^('src/main/java/pages/' + className + '.java', pageObject^);
echo fs.writeFileSync^('src/test/java/features/' + className + '.feature', featureFile^);
echo fs.writeFileSync^('src/test/java/stepDefs/' + className + 'Steps.java', stepDefs^);
echo.
echo console.log^('[SUCCESS] Generated test files with ' + actions.length + ' recorded actions'^);
echo console.log^('[INFO] Page Object: src/main/java/pages/' + className + '.java'^);
echo console.log^('[INFO] Feature File: src/test/java/features/' + className + '.feature'^);
echo console.log^('[INFO] Step Definitions: src/test/java/stepDefs/' + className + 'Steps.java'^);
) > "%GEN_SCRIPT%"

echo [INFO] Generating test files from extracted actions...
node "%GEN_SCRIPT%" "%FEATURE_NAME%" "%PAGE_URL%" "%JIRA_STORY%" "%RECORDING_DIR%" 2>"%TEMP%\gen_error_%RANDOM%.log"
set GEN_EXIT_CODE=%ERRORLEVEL%

if %GEN_EXIT_CODE% NEQ 0 (
    echo.
    echo [ERROR] File generation encountered an error.
    echo [INFO] Checking for common issues...
    
    REM Check if Node.js had syntax errors
    if exist "%TEMP%\gen_error_*.log" (
        echo [INFO] Error details found in temporary log
    )
    
    REM Validate JSON file exists and is valid
    if not exist "%RECORDING_DIR%\actions.json" (
        echo [ERROR] Actions JSON file not found!
        echo [FIX] Creating minimal JSON structure...
        (
            echo [^{"id":1,"action":"navigate","url":"%PAGE_URL%","methodName":"navigateTo","stepText":"user navigates"^}]
        ) > "%RECORDING_DIR%\actions.json"
    ) else (
        REM Try to validate JSON
        node -e "try^{require('./%RECORDING_DIR%/actions.json'^);console.log('[OK] JSON is valid'^);^}catch^(e^)^{console.log('[ERROR] Invalid JSON:',e.message^);^}" 2>nul
    )
    
    echo [INFO] Retrying file generation with error recovery...
    node "%GEN_SCRIPT%" "%FEATURE_NAME%" "%PAGE_URL%" "%JIRA_STORY%" "%RECORDING_DIR%" 2>nul
    
    if errorlevel 1 (
        echo [ERROR] File generation failed after retry!
        echo [INFO] Please check the recording and try again.
        del "%GEN_SCRIPT%" 2>nul
        goto :error
    )
)

del "%GEN_SCRIPT%" 2>nul
del "%TEMP%\gen_error_*.log" 2>nul

REM Validate generated files exist
set "FILES_MISSING=0"
if not exist "src\main\java\pages\%FEATURE_NAME%.java" (
    echo [WARNING] Page Object not generated!
    set FILES_MISSING=1
)
if not exist "src\test\java\features\%FEATURE_NAME%.feature" (
    echo [WARNING] Feature file not generated!
    set FILES_MISSING=1
)
if not exist "src\test\java\stepDefs\%FEATURE_NAME%Steps.java" (
    echo [WARNING] Step Definitions not generated!
    set FILES_MISSING=1
)

if %FILES_MISSING% EQU 1 (
    echo.
    echo [ERROR] Some files were not generated. Check for errors above.
    goto :error
)

echo.
echo ✅ Files generated successfully!

echo.
echo ════════════════════════════════════════════════════════════════
echo ✅ Step 4: AUTO-VALIDATION AND FIXING
echo ════════════════════════════════════════════════════════════════
echo.

echo [STEP 4.0] Post-generation validation...

REM Auto-fix common syntax issues in generated files
echo [INFO] Checking for common syntax issues...

REM Fix 1: Check for escaped characters that shouldn't be escaped
for %%f in ("src\main\java\pages\%FEATURE_NAME%.java" "src\test\java\stepDefs\%FEATURE_NAME%Steps.java") do (
    if exist %%f (
        powershell -Command "$content = Get-Content '%%f' -Raw; $fixed = $content -replace '\\\\n', [Environment]::NewLine; if ($content -ne $fixed) { Set-Content '%%f' -Value $fixed -NoNewline; Write-Host '[FIX] Fixed newline characters in %%f' -ForegroundColor Yellow }"
    )
)

REM Fix 2: Check for missing imports
if exist "src\main\java\pages\%FEATURE_NAME%.java" (
    findstr /C:"import configs.loadProps" "src\main\java\pages\%FEATURE_NAME%.java" >nul 2>&1
    if errorlevel 1 (
        echo [FIX] Adding missing loadProps import to Page Object...
        powershell -Command "$file = 'src\main\java\pages\%FEATURE_NAME%.java'; $content = Get-Content $file -Raw; if ($content -notmatch 'import configs.loadProps') { $content = $content -replace '(package pages;)', ('$1' + [Environment]::NewLine + [Environment]::NewLine + 'import configs.loadProps;'); Set-Content $file -Value $content -NoNewline; Write-Host '[OK] Added loadProps import' -ForegroundColor Green }"
    )
)

REM Fix 3: Ensure proper method visibility
for %%f in ("src\main\java\pages\%FEATURE_NAME%.java") do (
    if exist %%f (
        findstr /C:"protected static" %%f >nul 2>&1
        if not errorlevel 1 (
            echo [FIX] Changing protected methods to public in %%f...
            powershell -Command "(Get-Content '%%f') -replace 'protected static', 'public static' | Set-Content '%%f'"
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
    for /F "tokens=*" %%a in ('findstr /C:"@Given" /C:"@When" /C:"@Then" "%%f" 2^>nul') do (
        echo %%a >> "%TEMP_FILE%"
    )
)

REM Check for duplicates
if exist "%TEMP_FILE%" (
    sort "%TEMP_FILE%" > "%TEMP_SORTED%"
    powershell -Command "$lines = Get-Content '%TEMP_SORTED%'; $patterns = @{}; foreach ($line in $lines) { if ($line -match '@(Given|When|Then)\(\"([^\"]+)\"\)') { $pattern = $matches[2]; if ($patterns.ContainsKey($pattern)) { $patterns[$pattern]++ } else { $patterns[$pattern] = 1 } } }; $dups = $patterns.GetEnumerator() | Where-Object { $_.Value -gt 1 }; if ($dups) { Write-Host '[ERROR] Duplicate step patterns found:' -ForegroundColor Red; $dups | ForEach-Object { Write-Host ('  - \"' + $_.Key + '\" (' + $_.Value + ' times)') -ForegroundColor Yellow }; exit 1 } else { Write-Host '[OK] No duplicate step patterns' -ForegroundColor Green; exit 0 }"
    set DUP_CHECK_RESULT=%ERRORLEVEL%
    
    if %DUP_CHECK_RESULT% NEQ 0 (
        echo.
        echo [ERROR] Fix duplicates by renaming methods with Given/When/Then suffix
        del "%TEMP_FILE%" 2>nul
        del "%TEMP_SORTED%" 2>nul
        pause
        goto CLEANUP
    )
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
    powershell -Command "(Get-ChildItem -Path 'src\main\java\pages\*.java' -Recurse) | ForEach-Object { (Get-Content $_.FullName) -replace 'protected static', 'public static' | Set-Content $_.FullName }"
    echo [OK] Fixed protected methods to public
) else (
    echo [OK] No protected methods found
)

REM Check for BASE_URL() usage
findstr /S /M "BASE_URL()" src\main\java\pages\*.java >nul 2>&1
if not errorlevel 1 (
    echo [FOUND] Incorrect BASE_URL() usage - Auto-fixing...
    powershell -Command "(Get-ChildItem -Path 'src\main\java\pages\*.java' -Recurse) | ForEach-Object { (Get-Content $_.FullName) -replace 'BASE_URL\(\)', 'getProperty(\"URL\")' | Set-Content $_.FullName }"
    echo [OK] Fixed BASE_URL() to getProperty("URL")
) else (
    echo [OK] No BASE_URL() issues found
)

echo.
echo ════════════════════════════════════════════════════════════════
echo 🔨 Step 5: COMPILING PROJECT
echo ════════════════════════════════════════════════════════════════
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
        echo   ✓ Changed protected to public
        echo   ✓ Fixed BASE_URL() usage
        echo.
        echo Additional manual fixes may be needed:
        echo   1. Missing imports: Add 'import configs.loadProps;'
        echo   2. Wrong login: Use 'login.loginWith()' not 'loginToApplication()'
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
echo ✅ Compilation successful!
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo 🧪 Step 6: RUNNING TESTS
echo ════════════════════════════════════════════════════════════════
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
    echo ✅ All tests passed!
    echo.
)

:SUMMARY
echo.
echo ════════════════════════════════════════════════════════════════
echo 📊 GENERATION SUMMARY
echo ════════════════════════════════════════════════════════════════
echo.
echo Feature: %FEATURE_NAME%
echo JIRA Story: %JIRA_STORY%
echo Page URL: %PAGE_URL%
echo.
echo Generated Files:
echo   ✅ src/main/java/pages/%FEATURE_NAME%.java
echo   ✅ src/test/java/features/%FEATURE_NAME%.feature
echo   ✅ src/test/java/stepDefs/%FEATURE_NAME%Steps.java
echo.
echo Recorded Actions: See %RECORDING_DIR%\recorded-actions.java
echo.
echo Auto-Validation Results:
echo   ✓ Duplicate step patterns checked
echo   ✓ Protected methods auto-fixed
echo   ✓ BASE_URL() usage auto-fixed
echo   ✓ Compilation validated
echo   ✓ Tests executed
echo.
echo Test Reports:
echo   - MRITestExecutionReports\
echo.

echo Next Steps:
echo   1. Review generated files and add TODO implementations
echo   2. Update locators if needed (use MCP Locator Finder tool)
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
echo ════════════════════════════════════════════════════════════════
echo 🎉 DONE!
echo ════════════════════════════════════════════════════════════════
pause
