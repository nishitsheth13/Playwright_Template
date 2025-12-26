# 🚀 Playwright Test Automation Framework - Complete Guide

**SINGLE SOURCE OF TRUTH - Everything you need in one place**

Last Updated: December 26, 2025

---

## 📑 Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Method 1: CLI Recording](#method-1-cli-recording-recommended)
4. [Method 2: AI Prompt Enhancement](#method-2-ai-prompt-enhancement)
5. [Method 3: AI Interactive CLI with JIRA](#method-3-ai-interactive-cli-with-jira)
6. [Method 4: Manual Coding](#method-4-manual-coding)
7. [Dynamic Locator Strategies](#dynamic-locator-strategies)
8. [MCP Server Setup](#mcp-server-setup-ai-integration)
9. [Retry Analyzer](#retry-analyzer-configuration)
10. [Troubleshooting](#troubleshooting)
11. [Quick Reference](#quick-reference)

---

## ⚡ Quick Start

### Choose Your Method

| Method | Best For | Time | Node.js |
|--------|----------|------|---------|
| **CLI Recording** | Fast generation | 5-10 min | ❌ NO |
| **AI Enhancement** | Code refinement | 10-15 min | ❌ NO |
| **AI JIRA CLI** | JIRA integration | 15-20 min | ✅ YES |
| **Manual** | Full control | 15-30 min | ❌ NO |

### Unified CLI Menu
```bash
generate-test.bat
```
Choose from:
1. 🎥 Record & Auto-Generate (Fastest)
2. 🤖 AI-Assisted Interactive (JIRA)
3. ✅ Validate & Run Tests

### Direct Recording
```bash
record-and-generate.bat
```

---

## 📋 Prerequisites

### For Recording (Methods 1 & 4):
- [ ] Java 17+: `java --version`
- [ ] Maven 3.6+: `mvn --version`
- [ ] Git initialized: `git status`
- [ ] Config updated: `src/test/resources/configurations.properties`

### For AI CLI (Methods 2 & 3):
- [ ] Node.js 18+: `node --version`
- [ ] npm: `npm --version`
- [ ] Dependencies: `cd mcp-server && npm install`

### Configuration
`src/test/resources/configurations.properties`:
```properties
URL=https://your-app-url.com
MaxRetryCount=2
```

---

## 🤖 METHOD 1: CLI Recording (Recommended)

### Step-by-Step TODO

#### Phase 1: Recording Setup
- [ ] Open terminal in project root
- [ ] Run: `record-and-generate.bat` OR `generate-test.bat` → Option 1
- [ ] Enter feature name (e.g., "Login", "Checkout")
- [ ] Choose URL mode:
  - Option 1: Config URL + path (enter `/login`)
  - Option 2: Full custom URL
- [ ] Enter JIRA story ID (optional, press Enter for AUTO-GEN)

#### Phase 2: Recording Actions
- [ ] Wait for Playwright Inspector to open
- [ ] Perform all test actions:
  - Click buttons/links
  - Fill input fields
  - Select dropdown options
  - Check/uncheck boxes
  - Navigate pages
  - Upload files (if needed)
- [ ] Close browser when done (triggers auto-generation)

#### Phase 3: Auto-Validation
**Script automatically:**
- [ ] ✅ Compiles TestGeneratorHelper
- [ ] ✅ Generates test files
- [ ] ✅ Auto-fixes syntax issues
- [ ] ✅ Checks duplicates
- [ ] ✅ Fixes protected methods
- [ ] ✅ Fixes BASE_URL references

Watch console for:
```
[SUCCESS] Extracted X actions from recording
[DEBUG] Found locator click: button#submit
[DEBUG] Found locator fill: input[name='username']
```

#### Phase 4: Review Generated Files
- [ ] Page Object: `src/main/java/pages/{Feature}.java`
- [ ] Feature: `src/test/java/features/{Feature}.feature`
- [ ] Steps: `src/test/java/stepDefs/{Feature}Steps.java`

#### Phase 5: Validate Coverage
- [ ] Count recorded actions in `temp_recording_*/recorded-actions.java`
- [ ] Count locators in Page Object (should match)
- [ ] Count methods in Page Object (should be actions + 1 for navigateTo)
- [ ] Count steps in Feature file
- [ ] Count step definitions

**Run validation script:**
```powershell
powershell -ExecutionPolicy Bypass -File validate-coverage.ps1 -FeatureName "Login"
```

#### Phase 6: Compilation & Testing
- [ ] Watch for automatic compilation
- [ ] Review test execution results
- [ ] Check for retry messages if tests fail

#### Phase 7: Review Results
- [ ] Check console output for pass/fail
- [ ] Review reports in `MRITestExecutionReports/`
- [ ] Check screenshots if failures occurred
- [ ] Commit changes: `git add . && git commit -m "Add tests for {Feature}"`

### Success Criteria
- [ ] ✅ All recorded actions extracted
- [ ] ✅ Locators match recorded actions
- [ ] ✅ Page Object methods for all actions
- [ ] ✅ Feature steps match actions
- [ ] ✅ Step definitions call Page Object methods
- [ ] ✅ Project compiles without errors
- [ ] ✅ Tests run successfully

---

## 🧠 METHOD 2: AI Prompt Enhancement

### Prerequisites
- [ ] Generated test files exist (from METHOD 1)
- [ ] GitHub Copilot or AI assistant available

### Comprehensive Enhancement Prompt

```
I have Playwright Java test files that need enhancement. Please analyze and improve:

FILES TO ENHANCE:
1. Page Object: src/main/java/pages/{Feature}.java
2. Feature: src/test/java/features/{Feature}.feature
3. Steps: src/test/java/stepDefs/{Feature}Steps.java

REQUIRED ENHANCEMENTS:
1. Add comprehensive assertions
2. Add explicit waits where needed
3. Add error handling
4. Add meaningful scenario descriptions
5. Improve locator strategies (prefer data-testid > id > css)
6. Add data-driven test examples
7. Add negative test scenarios
8. Add logging statements
9. Verify Page Object Model best practices
10. Ensure step definitions are reusable

FRAMEWORK UTILS:
- clickOnElement(page, locator)
- enterText(page, locator, text)
- selectDropDownValueByText(page, locator, text)
- getText(page, locator): String
- waitForElement(page, locator, timeout)
- isElementVisible(page, locator): boolean

PROVIDE:
1. Enhanced Page Object
2. Enhanced Feature file (positive + negative scenarios)
3. Enhanced Step Definitions
4. List of additional utility methods needed

CONSTRAINTS:
- Use existing BasePage and utils methods
- Follow @Given/@When/@Then annotations
- Keep existing file structure
- Maintain testng.xml configuration
```

### Quick Fix Prompts

**Fix Duplicates:**
```
Analyze stepDefs/ folder and consolidate duplicate @Given/@When/@Then patterns
into reusable steps. Update all feature files accordingly.
```

**Improve Locators:**
```
Improve locators in {Feature}.java:
1. Replace brittle CSS with robust alternatives
2. Prefer data-testid > id > text > css
3. Add comments explaining strategies
```

**Add Assertions:**
```
Add assertions to {Feature}Steps.java:
1. Verify page navigation
2. Verify element visibility before interactions
3. Verify success messages
4. Add explicit waits
Use: isElementVisible(), getText(), waitForElement()
```

---

## 🤖 METHOD 3: AI Interactive CLI with JIRA

**⚠️ REQUIRES NODE.JS**

### Prerequisites
- [ ] Node.js 18+: `node --version`
- [ ] npm: `npm --version`
- [ ] JIRA configured: `src/test/resources/jiraConfigurations.properties`
```properties
JIRA_URL=https://your-domain.atlassian.net
JIRA_USERNAME=your-email@company.com
JIRA_API_TOKEN=your-api-token
```
- [ ] Dependencies: `npm install`

### Steps
1. [ ] Run: `generate-test.bat` → Option 2 OR `node automation-cli.js`
2. [ ] Choose: "Generate from JIRA" or "Generate from prompt"
3. [ ] If JIRA: Enter story ID (e.g., PROJ-123)
4. [ ] Review generated files
5. [ ] Use CLI commands: `/refine`, `/addscenario`, `/fixlocator`, `/validate`
6. [ ] CLI auto-compiles and runs tests

---

## ✍️ METHOD 4: Manual Coding

### Step 1: Create Page Object
File: `src/main/java/pages/{Feature}.java`

```java
package pages;
import com.microsoft.playwright.Page;
import configs.BasePage;
import static configs.utils.*;

public class Feature extends BasePage {
    // Locators
    private static final String ELEMENT_NAME = "selector";
    
    // Navigation
    public static void navigateTo(Page page, String path) {
        page.navigate(getProperty("URL") + path);
    }
    
    // Actions
    public static void performAction(Page page) {
        clickOnElement(page, ELEMENT_NAME);
    }
    
    // Verifications
    public static boolean isElementDisplayed(Page page) {
        return isElementVisible(page, ELEMENT_NAME);
    }
}
```

### Step 2: Create Feature File
File: `src/test/java/features/{Feature}.feature`

```gherkin
Feature: Feature Name
  Description
  
  Scenario: Scenario description
    Given user navigates to "/path"
    When user performs action
    Then user should see expected result
```

### Step 3: Create Step Definitions
File: `src/test/java/stepDefs/{Feature}Steps.java`

```java
package stepDefs;
import io.cucumber.java.en.*;
import com.microsoft.playwright.Page;
import pages.Feature;
import hooks.hooks;

public class FeatureSteps {
    private Page page;
    
    public FeatureSteps() {
        this.page = hooks.getPage();
    }
    
    @Given("user navigates to {string}")
    public void navigateToPage(String path) {
        Feature.navigateTo(page, path);
    }
    
    @When("user performs action")
    public void performAction() {
        Feature.performAction(page);
    }
    
    @Then("user should see expected result")
    public void verifyResult() {
        assert Feature.isElementDisplayed(page);
    }
}
```

### Step 4: Compile & Test
```bash
mvn clean compile
mvn test -DsuiteXmlFile=src/test/testng.xml
```

---

## 🎯 Dynamic Locator Strategies

### Problem: Random/Dynamic IDs
```html
<button id="c5e5f7ef-1180-49e8-a0a3-bfde5d637c1e">Submit</button>
```
**❌ DON'T use random IDs!**

### Solution: 9 Helper Methods

#### 1. Find by Text
```java
// HTML: <button id="random">Submit</button>
String btn = utils.findByText("button", "Submit");
// Result: xpath=//button[text()='Submit']
```

#### 2. Find by Partial Text
```java
// HTML: <div id="random">Welcome John</div>
String msg = utils.findByPartialText("div", "Welcome");
// Result: xpath=//div[contains(text(),'Welcome')]
```

#### 3. Find by Attribute
```java
// HTML: <button id="random" class="btn-primary">Click</button>
String btn = utils.findByAttribute("button", "class", "btn-primary");
// Result: xpath=//button[@class='btn-primary']

// With data-testid
String btn = utils.findByAttribute("button", "data-testid", "submit");
```

#### 4. Find by Partial Attribute
```java
// HTML: <button class="btn btn-primary btn-lg">Submit</button>
String btn = utils.findByPartialAttribute("button", "class", "btn-primary");
// Result: xpath=//button[contains(@class,'btn-primary')]
```

#### 5. Find by Label
```java
// HTML: <label>Username</label><input id="random">
String field = utils.findByLabel("Username");
// Result: xpath=//label[text()='Username']/following-sibling::input
```

#### 6. Find by Placeholder
```java
// HTML: <input id="random" placeholder="Enter username">
String input = utils.findByPlaceholder("Enter username");
// Result: xpath=//input[@placeholder='Enter username']
```

#### 7. Find by Parent-Child
```java
// HTML: <div class="login-form"><input id="random"></div>
String field = utils.findByParentChild("div[@class='login-form']", "input");
// Result: xpath=//div[@class='login-form']//input
```

#### 8. Find Button
```java
// HTML: <button id="random" aria-label="Submit">Submit</button>
String btn = utils.findButton("Submit");
// Result: xpath=//button[text()='Submit' or @aria-label='Submit' or @value='Submit']
```

#### 9. Find by Relative Position
```java
// HTML: <div class="header">Header</div><button id="random1">First</button>
String first = utils.findByRelativePosition("div[@class='header']", "following-sibling", "button", 1);
// Result: xpath=//div[@class='header']/following-sibling::button[1]
```

### Priority Order
1. ✅ **data-testid** (if available)
2. ✅ **Stable CSS class**
3. ✅ **Text content**
4. ✅ **Placeholder**
5. ✅ **Label relationship**
6. ✅ **ARIA attributes**
7. ✅ **Parent-child**
8. ⚠️ **Relative position** (last resort)

### Complete Example
```java
public class LoginWithDynamicIDs extends BasePage {
    // ✅ GOOD - Stable locators
    private static final String USERNAME = utils.findByPlaceholder("Enter username");
    private static final String PASSWORD = utils.findByPlaceholder("Enter password");
    private static final String LOGIN_BTN = utils.findButton("Login");
    private static final String WELCOME = utils.findByPartialText("div", "Welcome");
    
    public static void login(String user, String pass) {
        utils.enterText(USERNAME, user);
        utils.enterText(PASSWORD, pass);
        utils.clickOnElement(LOGIN_BTN);
    }
    
    public static void verifyLogin(String expectedUser) {
        String welcomeText = utils.getText(WELCOME);
        Assert.assertTrue(welcomeText.contains(expectedUser));
    }
}
```

---

## 🤖 MCP Server Setup (AI Integration)

### Installation Status
- ✅ MCP Server Files: `mcp-server/` directory
- ✅ Dependencies: @modelcontextprotocol/sdk, zod, typescript
- ✅ Compiled: `dist/index.js` ready
- ✅ IntelliJ Support: 2025.3+
- ✅ VS Code Config: `.vscode/mcp-settings.json`

### IntelliJ Configuration

#### Method 1: Via UI (Recommended)
1. **Open Settings**: `Ctrl + Alt + S` (Windows) or `Cmd + ,` (Mac)
2. **Navigate**: `Tools` → `AI Assistant` → `MCP Servers`
3. **Add Server**:
   - Name: `Playwright Automation`
   - Command: `node`
   - Arguments: `C:\Users\nishit.sheth\IdeaProjects\Playwright_Template\mcp-server\dist\index.js`
   - Env (optional): `PROJECT_ROOT=C:\Users\nishit.sheth\IdeaProjects\Playwright_Template`
4. **Enable**: Check the box and click Apply

#### Method 2: Manual XML
File: `C:\Users\nishit.sheth\AppData\Roaming\JetBrains\IntelliJIdea2025.3\options\llm.mcpServers.xml`

```xml
<application>
  <component name="McpApplicationServerCommands">
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
      </command>
    </commands>
  </component>
</application>
```

Restart IntelliJ after manual changes.

### Testing MCP Server

#### Test 1: Verify Server Starts
```powershell
cd mcp-server
node dist/index.js
```
Expected: `🚀 Playwright Automation MCP Server running on stdio`

#### Test 2: Verify in IntelliJ
1. Open GitHub Copilot Chat
2. Check for "Playwright Automation" in available tools
3. Try: "List available test automation tools"

### Available Tools
1. **list-pages**: List all Page Objects
2. **read-page**: Read page object content
3. **list-features**: List feature files
4. **read-feature**: Read feature content
5. **list-stepdefs**: List step definitions
6. **read-stepdef**: Read step definition
7. **generate-page**: Generate Page Object
8. **generate-feature**: Generate feature file
9. **generate-stepdef**: Generate step definitions
10. **analyze-framework**: Analyze framework structure

### Example Prompts
```
@playwright-automation Create a LoginPage with username, password and login button
@playwright-automation Create feature file for user login scenario
@playwright-automation What page objects are available?
@playwright-automation Generate step definitions for login feature
```

### Updating Server
```powershell
cd mcp-server
npm run build
```

---

## 🔄 Retry Analyzer Configuration

### Setup
File: `src/test/resources/configurations.properties`
```properties
MaxRetryCount=2
```

File: `src/test/testng.xml`
```xml
<listeners>
    <listener class-name="configs.RetryListener"/>
</listeners>
```

### How It Works
1. Test fails → Retry Analyzer triggers
2. Console shows:
```
========================================
⚠️ TEST RETRY TRIGGERED
========================================
Test: LoginTest.verifyLogin
Retry: 1 of 2
========================================
```
3. Retries up to MaxRetryCount
4. If still fails → marks as failure

---

## 🔍 Troubleshooting

### Recording Doesn't Extract Actions
- [ ] Check `temp_recording_*/recorded-actions.java` has code
- [ ] Look for `[WARN] No actions found` in console
- [ ] Verify browser actions were performed
- [ ] Check debug logs for `[DEBUG] Found locator...`
- [ ] Verify TestGeneratorHelper compiled

### Duplicate Step Definitions
```bash
powershell -ExecutionPolicy Bypass -File scripts\check-duplicates.ps1
```
- [ ] Review reported duplicates
- [ ] Consolidate patterns
- [ ] Recompile: `mvn clean compile`

### Tests Fail with NullPointerException
- [ ] Check `hooks.java` initializes page
- [ ] Verify `page = hooks.getPage()` in step definitions
- [ ] Check Page Object methods receive page parameter

### Elements Not Found
- [ ] Add explicit waits:
```java
waitForElement(page, ELEMENT, 5000);
clickOnElement(page, ELEMENT);
```
- [ ] Verify locator with Playwright Inspector
- [ ] Check if element is in iframe
- [ ] Verify page loaded completely

### Protected Method Errors
Auto-fix runs, but if persists:
```bash
powershell -ExecutionPolicy Bypass -File scripts\fix-protected-methods.ps1
```

### BASE_URL() Not Recognized
Auto-fix runs, but if persists:
```bash
powershell -ExecutionPolicy Bypass -File scripts\fix-base-url.ps1
```

---

## 📌 Quick Reference

### Commands

| Task | Command |
|------|---------|
| **Full Workflow** | `record-and-generate.bat` |
| **Unified Menu** | `generate-test.bat` |
| **Compile** | `mvn clean compile` |
| **Run Tests** | `mvn test -DsuiteXmlFile=src/test/testng.xml` |
| **Specific Feature** | `mvn test -Dcucumber.options="src/test/java/features/Login.feature"` |
| **Clean Rebuild** | `mvn clean install` |
| **Check Duplicates** | `powershell -ExecutionPolicy Bypass -File scripts\check-duplicates.ps1` |
| **Validation** | `powershell -ExecutionPolicy Bypass -File validate-coverage.ps1 -FeatureName "Login"` |

### Reports Location
- **Extent Reports**: `MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/`
- **Cucumber**: `target/cucumber-reports/cucumber.html`
- **Screenshots**: `MRITestExecutionReports/Version*/screenShots/`
- **Recordings**: `MRITestExecutionReports/Version*/recordings/`

### Key Files
- **Base Classes**: `src/main/java/configs/base.java`, `BasePage.java`
- **Utilities**: `src/main/java/configs/utils.java`
- **Test Generator**: `src/main/java/configs/TestGeneratorHelper.java`
- **Hooks**: `src/test/java/hooks/hooks.java`
- **Config**: `src/test/resources/configurations.properties`

---

## 🎯 Final Success Criteria

Complete TODO before "DONE":
- [ ] ✅ All recorded actions covered
- [ ] ✅ Validation shows 100% coverage
- [ ] ✅ Compiles without errors
- [ ] ✅ Tests execute successfully
- [ ] ✅ Retry mechanism working
- [ ] ✅ Reports generated correctly
- [ ] ✅ No duplicate step patterns
- [ ] ✅ Locators follow best practices
- [ ] ✅ Step defs call Page Object methods
- [ ] ✅ Feature files proper Gherkin
- [ ] ✅ Code committed to git

---

## 🎉 Ready to Start!

```bash
# Fastest way - 5-10 minutes
record-and-generate.bat
```

**Follow the prompts and check off each TODO item as you complete it!**

---

**Last Updated**: December 26, 2025  
**Framework Version**: 2.0  
**Documentation**: Single consolidated guide

