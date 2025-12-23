@echo off
REM Quick Launch Script for AI Test Generator
REM This ensures you're in the correct directory

cd /d "%~dp0"
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
)
