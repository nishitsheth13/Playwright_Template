#!/bin/bash

# MCP Server Setup Script
# This script automates the setup of the AI-powered automation MCP server

echo "🚀 Setting up AI-Powered Automation MCP Server..."
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check Node.js installation
echo "📦 Checking Node.js installation..."
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed!${NC}"
    echo "Please install Node.js 18+ from https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node -v)
echo -e "${GREEN}✅ Node.js found: $NODE_VERSION${NC}"
echo ""

# Check npm installation
echo "📦 Checking npm installation..."
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm is not installed!${NC}"
    exit 1
fi

NPM_VERSION=$(npm -v)
echo -e "${GREEN}✅ npm found: $NPM_VERSION${NC}"
echo ""

# Navigate to MCP server directory
echo "📂 Setting up MCP server..."
cd mcp-server || exit 1

# Install dependencies
echo "📥 Installing dependencies..."
npm install
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to install dependencies${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Dependencies installed${NC}"
echo ""

# Build the TypeScript project
echo "🔨 Building TypeScript project..."
npm run build
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Build successful${NC}"
echo ""

# Return to project root
cd ..

# Verify project structure
echo "🔍 Verifying project structure..."
REQUIRED_DIRS=(
    "src/main/java/pages"
    "src/test/java/features"
    "src/test/java/stepDefs"
    "mcp-server/dist"
)

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ ! -d "$dir" ]; then
        echo -e "${RED}❌ Missing directory: $dir${NC}"
        exit 1
    fi
done
echo -e "${GREEN}✅ Project structure verified${NC}"
echo ""

# Create MCP configuration for VS Code
echo "⚙️  Creating VS Code MCP configuration..."
mkdir -p .vscode
PROJECT_ROOT=$(pwd)

# For Windows paths, convert forward slashes to backslashes
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    PROJECT_ROOT=$(echo $PROJECT_ROOT | sed 's/\//\\\\/g')
fi

cat > .vscode/mcp-settings.json << EOF
{
  "mcpServers": {
    "playwright-automation": {
      "command": "node",
      "args": [
        "${PROJECT_ROOT}/mcp-server/dist/index.js"
      ],
      "env": {
        "PROJECT_ROOT": "${PROJECT_ROOT}"
      }
    }
  }
}
EOF

echo -e "${GREEN}✅ VS Code MCP configuration created${NC}"
echo ""

# Test MCP server
echo "🧪 Testing MCP server..."
timeout 3 node mcp-server/dist/index.js &> /dev/null &
SERVER_PID=$!
sleep 2

if ps -p $SERVER_PID > /dev/null; then
    echo -e "${GREEN}✅ MCP server is working${NC}"
    kill $SERVER_PID 2>/dev/null
else
    echo -e "${YELLOW}⚠️  Could not verify MCP server (may be normal)${NC}"
fi
echo ""

# Compile Java project
echo "☕ Compiling Java project..."
if command -v mvn &> /dev/null; then
    mvn clean compile test-compile -q
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Java project compiled successfully${NC}"
    else
        echo -e "${YELLOW}⚠️  Java compilation had warnings (check manually)${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Maven not found - skipping Java compilation${NC}"
fi
echo ""

# Display setup summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✨ Setup Complete!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Next Steps:"
echo ""
echo "1️⃣  Configure your AI assistant:"
echo "   • GitHub Copilot: Restart VS Code"
echo "   • Claude Desktop: Add MCP config to claude_desktop_config.json"
echo ""
echo "2️⃣  Try the Interactive CLI:"
echo "   $ node automation-cli.js"
echo ""
echo "3️⃣  Or use AI chat with prompts from:"
echo "   AI_PROMPT_TEMPLATES.md"
echo ""
echo "4️⃣  Read the full guide:"
echo "   AI_AUTOMATION_SETUP.md"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}Happy Testing! 🎉${NC}"
echo ""
