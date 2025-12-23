@echo off
REM MCP Server Setup Script for Windows
REM This script automates the setup of the AI-powered automation MCP server

echo.
echo ================================
echo AI-Powered Automation MCP Setup
echo ================================
echo.

REM Check Node.js installation
echo [1/6] Checking Node.js installation...
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed!
    echo Please install Node.js 18+ from https://nodejs.org/
    pause
    exit /b 1
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
    exit /b 1
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
    exit /b 1
)

call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install dependencies
    cd ..
    pause
    exit /b 1
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
    exit /b 1
)
echo [OK] Build successful
cd ..
echo.

REM Verify project structure
echo [5/6] Verifying project structure...
if not exist "src\main\java\pages" (
    echo [ERROR] Missing directory: src\main\java\pages
    pause
    exit /b 1
)
if not exist "src\test\java\features" (
    echo [ERROR] Missing directory: src\test\java\features
    pause
    exit /b 1
)
if not exist "src\test\java\stepDefs" (
    echo [ERROR] Missing directory: src\test\java\stepDefs
    pause
    exit /b 1
)
if not exist "mcp-server\dist" (
    echo [ERROR] Missing directory: mcp-server\dist
    pause
    exit /b 1
)
echo [OK] Project structure verified
echo.

REM Create VS Code MCP configuration
echo [6/6] Creating VS Code MCP configuration...
if not exist ".vscode" mkdir .vscode

set PROJECT_ROOT=%CD%
set PROJECT_ROOT=%PROJECT_ROOT:\=\\%

(
echo {
echo   "mcpServers": {
echo     "playwright-automation": {
echo       "command": "node",
echo       "args": [
echo         "%PROJECT_ROOT%\\mcp-server\\dist\\index.js"
echo       ],
echo       "env": {
echo         "PROJECT_ROOT": "%PROJECT_ROOT%"
echo       }
echo     }
echo   }
echo }
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

REM Display summary
echo.
echo ============================================
echo           SETUP COMPLETE!
echo ============================================
echo.
echo Next Steps:
echo.
echo 1. Configure your AI assistant:
echo    * GitHub Copilot: Restart VS Code
echo    * Claude Desktop: Add MCP config to claude_desktop_config.json
echo.
echo 2. Try the Interactive CLI:
echo    ^> node automation-cli.js
echo.
echo 3. Or use AI chat with prompts from:
echo    AI_PROMPT_TEMPLATES.md
echo.
echo 4. Read the full guide:
echo    AI_AUTOMATION_SETUP.md
echo.
echo ============================================
echo.
echo Happy Testing!
echo.
pause
