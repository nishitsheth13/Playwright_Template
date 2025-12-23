# 🤖 AI-Powered Test Automation Guide

> **Generate complete test suites in 5 minutes**

---

## 🚀 Quick Start

### Setup (One-Time)
```bash
# Windows
setup-mcp.bat

# Mac/Linux  
chmod +x setup-mcp.sh && ./setup-mcp.sh
```

### Generate Test
```bash
# Interactive CLI
node automation-cli.js

# Or use AI Chat
"Using MCP server, create a login test with username, password fields and login button"
```

### Run
```bash
mvn clean compile test
```

---

## 🎯 Three Ways to Generate

### 1. Interactive CLI (Easiest)
```bash
node automation-cli.js
```
- Follow prompts for test name, elements, scenarios
- All files generated automatically
- Best for beginners

### 2. GitHub Copilot Chat
```
Using MCP server, create [feature] test with [elements]. Test [scenarios].
```
- Already configured in `.vscode/mcp-settings.json`
- AI uses MCP tools automatically
- Best for regular use

### 3. Claude Desktop
Add to config: `%APPDATA%\Claude\claude_desktop_config.json` (Windows) or `~/Library/Application Support/Claude/claude_desktop_config.json` (Mac)
```json
{
  "mcpServers": {
    "playwright-automation": {
      "command": "node",
      "args": ["C:/path/to/project/mcp-server/dist/index.js"],
      "env": {"PROJECT_ROOT": "C:/path/to/project"}
    }
  }
}
```

---

## 📝 Prompt Examples

### Login Test
```
Using MCP server, create login test:
- Username field (type)
- Password field (type)
- Login button (click)

Test valid login and invalid credentials
```

### Search Test
```
Create search test with search bar, search button, and results container.
Test valid search and no results scenarios.
```

### Form Test
```
Create form test with name, email fields and submit button.
Test valid submission and validation errors.
```

### Multi-Page Flow
```
Create checkout flow: Cart → Shipping → Payment → Confirmation.
Test complete purchase with all steps.
```

---

## 🛠️ MCP Tools Available

AI automatically uses these 5 tools:
1. `analyze-framework` - Show existing tests
2. `generate-page-object` - Create Page Object
3. `generate-feature` - Create Feature file
4. `generate-step-definitions` - Create Step Definitions
5. `generate-complete-test-suite` - Create everything ⭐

---

## 📦 What Gets Generated

For each test:

**Page Object (*.java):**
```java
public class Login extends BasePage {
    protected static void enterUsername(String locator, String text) {
        fillText(locator, text);
    }
}
```

**Feature File (*.feature):**
```gherkin
@Functional @Smoke
Scenario: Valid login
  Given User is on login page
  When User enters credentials
  Then User should be logged in
```

**Step Definitions (*Steps.java):**
```java
@Given("User is on login page")
public void userIsOnLoginPage() {
    loginPage.navigateTo("/login");
}
```

---

## 🐛 Troubleshooting

**Setup fails:**
```bash
cd mcp-server
npm install
npm run build
```

**CLI not working:**
```bash
node --version  # Must be 18+
node automation-cli.js
```

**Compilation errors:**
```bash
mvn clean compile test-compile
```

**AI not using MCP:**
- Say "Using MCP server" in prompts
- Restart VS Code/Claude
- Verify setup completed

---

## 💡 Best Practices

✅ Use descriptive names ("loginButton" not "btn1")  
✅ Follow Gherkin: Given → When → Then  
✅ Start simple, add complexity later  
✅ Review generated code before committing  
✅ Use tags: @Smoke, @Regression, @Functional

---

## 📊 Time Comparison

| Task | Manual | AI-Generated |
|------|--------|--------------|
| Page Object | 30 min | 1 min |
| Feature File | 15 min | 30 sec |
| Step Definitions | 30 min | 1 min |
| Debug & Fix | 20 min | 1 min |
| **Total** | **~2 hours** | **~5 minutes** |

**90% faster!**

---

## 📚 Commands Reference

```bash
# Setup
setup-mcp.bat / ./setup-mcp.sh

# Generate
node automation-cli.js

# Build & Test
mvn clean compile
mvn test
mvn test -Dcucumber.filter.tags="@Smoke"
```

**Files:**
- Page Objects: `src/main/java/pages/`
- Features: `src/test/java/features/`
- Step Defs: `src/test/java/stepDefs/`
- Reports: `MRITestExecutionReports/`

---

## 🎯 Next Steps

1. ✅ Run setup: `setup-mcp.bat`
2. ✅ Generate test: `node automation-cli.js`
3. ✅ Compile: `mvn clean compile`
4. ✅ Run: `mvn test`
5. ✅ Create more tests for your app

---

**Framework Details:** [AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md)

**Happy Testing! 🚀**
