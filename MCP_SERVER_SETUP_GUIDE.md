# 🚀 MCP Server Installation Guide for IntelliJ Platform

## ✅ Current Status

### Installation Check Results:
- ✅ **MCP Server Files**: Present in `mcp-server/` directory
- ✅ **Dependencies Installed**: All npm packages are installed
  - @modelcontextprotocol/sdk@1.25.1
  - zod@3.25.76
  - typescript@5.9.3
  - @types/node@20.19.27
- ✅ **TypeScript Compiled**: `dist/` folder contains compiled JavaScript
- ✅ **Server Executable**: `dist/index.js` is ready to run
- ✅ **IntelliJ MCP Support**: Enabled in IntelliJ IDEA 2025.3

### IntelliJ MCP Configuration Status:
Located in: `C:\Users\nishit.sheth\AppData\Roaming\JetBrains\IntelliJIdea2025.3\options\`

1. **mcpServer.xml**: MCP is enabled
   - `enableMcpServer: true`
   - `enableBraveMode: true`

2. **McpToolsStoreService.xml**: Playwright server registered
   - Server ID: "playwright"
   - Status: enabled

3. **llm.mcpServers.xml**: No custom commands configured yet

## 🔧 How to Register Your Custom MCP Server

### Method 1: Via IntelliJ UI (Recommended)

1. **Open IntelliJ Settings**:
   - Press `Ctrl + Alt + S` (Windows) or `Cmd + ,` (Mac)
   - Navigate to: `Tools` → `AI Assistant` → `MCP Servers`

2. **Add New MCP Server**:
   - Click `+` to add a new server
   - Enter Server Name: `Playwright Automation`
   - Command: `node`
   - Arguments: `C:\Users\nishit.sheth\IdeaProjects\Playwright_Template\mcp-server\dist\index.js`
   - Environment Variables (optional):
     - `PROJECT_ROOT`: `C:\Users\nishit.sheth\IdeaProjects\Playwright_Template`

3. **Enable the Server**:
   - Check the box next to your new server
   - Click `Apply` and `OK`

### Method 2: Manual Configuration (Advanced)

Update the file: `C:\Users\nishit.sheth\AppData\Roaming\JetBrains\IntelliJIdea2025.3\options\llm.mcpServers.xml`

```xml
<application>
  <component name="McpApplicationServerCommands" modifiable="true" autoEnableExternalChanges="false">
    <commands>
      <command>
        <option name="id" value="playwright-automation" />
        <option name="name" value="Playwright Automation" />
        <option name="command" value="node" />
        <option name="args">
          <list>
            <option value="C:\Users\nishit.sheth\IdeaProjects\Playwright_Template\mcp-server\dist\index.js" />
          </list>
        </option>
        <option name="env">
          <map>
            <entry key="PROJECT_ROOT" value="C:\Users\nishit.sheth\IdeaProjects\Playwright_Template" />
          </map>
        </option>
      </command>
    </commands>
    <urls />
  </component>
</application>
```

**Important**: Restart IntelliJ IDEA after manual configuration changes.

## 🧪 Testing Your MCP Server

### Test 1: Verify Server Starts
```powershell
cd C:\Users\nishit.sheth\IdeaProjects\Playwright_Template\mcp-server
node dist/index.js
```

Expected output: `🚀 Playwright Automation MCP Server running on stdio`

Press `Ctrl+C` to stop the server.

### Test 2: Verify in IntelliJ
1. Open GitHub Copilot Chat in IntelliJ
2. Check if "Playwright Automation" appears in available tools/servers
3. Try a prompt like: "List available test automation tools"

## 📋 Available MCP Server Tools

Your MCP server provides these tools to AI assistants:

1. **list-pages**: List all Page Object Model files
2. **read-page**: Read content of a specific page object
3. **list-features**: List all Cucumber feature files
4. **read-feature**: Read content of a specific feature file
5. **list-stepdefs**: List all step definition files
6. **read-stepdef**: Read content of a specific step definition
7. **generate-page**: Generate a new Page Object
8. **generate-feature**: Generate a new Cucumber feature
9. **generate-stepdef**: Generate new step definitions
10. **analyze-framework**: Analyze framework structure and patterns

## 🔍 Troubleshooting

### Issue 1: Server Not Showing in IntelliJ
**Solution**:
- Verify IntelliJ version is 2025.3 or later
- Check GitHub Copilot plugin is installed and active
- Restart IntelliJ IDEA
- Check Settings → Tools → AI Assistant → MCP Servers

### Issue 2: Node.js Not Found
**Solution**:
```powershell
# Check Node.js installation
node -v
npm -v

# If not installed, download from: https://nodejs.org/
```

### Issue 3: Server Fails to Start
**Solution**:
```powershell
# Rebuild the server
cd mcp-server
npm run build

# Check for errors
node dist/index.js
```

### Issue 4: TypeScript Compilation Errors
**Solution**:
```powershell
cd mcp-server
npm install
npm run build
```

## 🎯 Using MCP Server with GitHub Copilot

### Example Prompts:

1. **Generate a new page object**:
   ```
   @playwright-automation Create a LoginPage with username, password fields and login button
   ```

2. **Generate feature file**:
   ```
   @playwright-automation Create a feature file for user login scenario
   ```

3. **Analyze existing code**:
   ```
   @playwright-automation What page objects are available in the framework?
   ```

4. **Generate step definitions**:
   ```
   @playwright-automation Generate step definitions for the login feature
   ```

## 🔄 Updating the MCP Server

When you make changes to `mcp-server/src/index.ts`:

```powershell
cd C:\Users\nishit.sheth\IdeaProjects\Playwright_Template\mcp-server
npm run build
```

IntelliJ will automatically pick up the changes on next use.

## 📚 Additional Resources

- **MCP Documentation**: https://modelcontextprotocol.io/
- **IntelliJ AI Assistant**: https://www.jetbrains.com/help/idea/ai-assistant.html
- **Project README**: [README.md](README.md)
- **Master Test Guide**: [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md)

## ✨ Next Steps

1. ✅ Register your MCP server in IntelliJ (see Method 1 above)
2. 🧪 Test the server with GitHub Copilot Chat
3. 🚀 Start generating test automation code with AI assistance
4. 📖 Review available prompts in AI_PROMPT_TEMPLATES.md (if exists)

---

**Last Updated**: December 26, 2025
**Server Version**: 1.0.0
**IntelliJ Version**: 2025.3

