# 🚀 Playwright Test Automation Framework - Complete Guide

**SINGLE SOURCE OF TRUTH - Everything you need in one place**

Last Updated: December 26, 2025

---

## 📑 Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Method 1: CLI Recording](#method-1-cli-recording-recommended)
4. [Intelligent Naming System](#intelligent-naming-system)
5. [Method 2: AI Prompt Enhancement](#method-2-ai-prompt-enhancement)
6. [Method 3: AI Interactive CLI with JIRA](#method-3-ai-interactive-cli-with-jira)
7. [Method 4: Manual Coding](#method-4-manual-coding)
8. [Dynamic Locator Strategies](#dynamic-locator-strategies)
9. [MCP Server Setup](#mcp-server-setup-ai-integration)
10. [Retry Analyzer](#retry-analyzer-configuration)
11. [Troubleshooting](#troubleshooting)
12. [Quick Reference](#quick-reference)

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

## 🎨 INTELLIGENT NAMING SYSTEM

**🎯 The recorder now generates descriptive, professional-quality code automatically!**

### Overview

All recorded tests use **intelligent naming conventions** to create maintainable, self-documenting code:
- **Element names** extracted from selectors (not ELEMENT_1, ELEMENT_2)
- **Method names** describe actions semantically (clickSignIn, enterUsername)
- **Feature steps** use natural, readable language
- **Comprehensive logging** with emoji indicators

### Name Extraction Examples

| Selector Type | Example | Generated Constant | Method Name |
|--------------|---------|-------------------|-------------|
| **Text locator** | `text=Sign In` | `SIGN_IN_1` | `clickSignIn()` |
| **ID selector** | `#username` | `USERNAME_1` | `enterUsername()` |
| **Placeholder** | `placeholder=Email` | `EMAIL_1` | `enterEmail()` |
| **Aria-label** | `aria-label="Submit"` | `SUBMIT_1` | `clickSubmit()` |
| **Data-testid** | `data-testid="login-btn"` | `LOGIN_BTN_1` | `clickLoginBtn()` |
| **Role + Name** | `role=button[name="Save"]` | `SAVE_1` | `clickSave()` |
| **Has-text** | `button:has-text("Cancel")` | `CANCEL_1` | `clickCancel()` |

### Generated Code Structure

#### ✅ Page Object (Before vs After)

**❌ OLD (Generic):**
```java
private static final String ELEMENT_1 = "text=Sign In";
public static void clickElement1(Page page) {
    clickOnElement(ELEMENT_1);
}
```

**✅ NEW (Descriptive):**
```java
// Sign In
private static final String SIGN_IN_1 = "text=Sign In";

/**
 * user clicks on sign in
 * Element: SignIn (text=Sign In)
 */
public static void clickSignIn(Page page) {
    System.out.println("🖱️ user clicks on sign in: " + SIGN_IN_1);
    clickOnElement(SIGN_IN_1);
}
```

#### ✅ Feature File

**❌ OLD:** `When user clicks on element 1`  
**✅ NEW:** `When user clicks on sign in`

#### ✅ Step Definitions

**❌ OLD:**
```java
@When("user clicks on element 1")
public void clickElement1() {
    Profile.clickElement1(page);
}
```

**✅ NEW:**
```java
@When("user clicks on sign in")
public void clickSignIn() {
    System.out.println("📍 Step: user clicks on sign in");
    Profile.clickSignIn(page);
}
```

### Naming Conventions

#### 1. **Element Constants** (UPPER_SNAKE_CASE)
```java
SIGN_IN_1           // Click action on "Sign In"
USERNAME_INPUT_2    // Text field with ID "username"
SAVE_BUTTON_3       // Button with text "Save"
COUNTRY_SELECT_4    // Dropdown for country selection
```

#### 2. **Method Names** (camelCase with verb prefix)

| Action Type | Prefix | Example |
|-------------|--------|---------|
| Click | `click` | `clickSignIn()`, `clickSave()` |
| Fill | `enter` | `enterUsername()`, `enterPassword()` |
| Search | `search` | `searchProducts()` |
| Select | `select` | `selectCountry()` |
| Check | `check` | `checkRememberMe()` |
| Toggle | `toggle` | `toggleDarkMode()` |

**Special Recognition:**
- Email fields → `enterEmail()` (not `enterEmailAddress()`)
- Password fields → `enterPassword()` (not `enterPasswordInput()`)
- Username fields → `enterUsername()` (not `enterUsernameField()`)
- Search boxes → `searchProducts()` (not `enterSearch()`)

#### 3. **Feature Steps** (Natural language)
```gherkin
When user clicks on sign in
When user enters text into username
When user selects option from country dropdown
When user checks remember me checkbox
Then page should be updated
```

### Supported Selector Types

#### ✅ Modern Playwright (Preferred)
```java
text=Button Text                    // Text content
placeholder=Enter email             // Placeholder attribute
role=button[name="Submit"]          // ARIA role + name
button:has-text("Save")            // Element with text
#element-id                         // ID selector
```

#### ✅ Traditional Selectors
```java
input[name="username"]              // Name attribute
input[placeholder="Email"]          // Placeholder
button[aria-label="Close"]          // Aria-label
a[title="Help"]                     // Title attribute
input[data-testid="login"]          // Test ID
.btn-primary                        // CSS class
```

### Smart Features

#### Auto-Cleanup
- ✅ Removes suffixes: "button", "btn", "input", "field", "link"
- ✅ Removes from IDs: "-btn", "-button", "-input", "-field"
- ✅ Converts kebab-case → PascalCase
- ✅ Handles numbers: "25" → `Number25`

#### Context-Aware Naming
```java
// Detects button type
"Sign In" button    → clickSignIn()
"Save" button       → clickSave()
"Cancel" link       → clickCancel()

// Detects input type
"Email" field       → enterEmail()
"Search" box        → searchProducts()
"Password" field    → enterPassword()

// Detects interaction
Checkbox           → checkTerms()
Toggle switch      → toggleDarkMode()
Dropdown          → selectCountry()
```

#### Comprehensive Logging

**Step Level (📍):**
```
📍 Step: user clicks on sign in
📍 Step: user enters text into username
```

**Action Level:**
```
🖱️ user clicks on sign in: text=Sign In
⌨️ user enters text into username: #username = 'john.doe'
🔽 user selects option from country: #country = 'USA'
☑️ user checks remember me: #remember
✅ Clicked on element: text=Sign In
```

### Best Practices

#### ✅ DO: Use Descriptive HTML
```html
<button>Sign In</button>                              ✅ Generates: clickSignIn()
<input placeholder="Email Address" />                ✅ Generates: enterEmail()
<button data-testid="submit-form">Submit</button>   ✅ Generates: clickSubmitForm()
```

#### ❌ AVOID: Non-Descriptive HTML
```html
<button>→</button>              ❌ Generates: clickElement1()
<button>OK</button>             ❌ Too generic
<input id="txt1" />            ❌ No meaningful name
```

#### 💡 Enhancement Tips
1. **After Recording:** Review generated names
2. **Refine if Needed:** Manually update for clarity
3. **Add Attributes:** Use `aria-label` or `data-testid` for complex elements
4. **Group Actions:** Combine related steps into higher-level methods

### Troubleshooting

**Q: Names are still generic (e.g., "Element1")**  
**A:** Selector has no identifiable text/attributes  
**Fix:** Add `aria-label`, `placeholder`, or `data-testid` to HTML

**Q: Method names are too long**  
**A:** Element has long descriptive text  
**Fix:** Manually refactor to shorter aliases after generation

**Q: Duplicate method names**  
**A:** Multiple elements with same text  
**Fix:** Framework adds ID suffixes automatically (`_1`, `_2`); refine if needed

### Example Workflow

```bash
# 1. Record actions
.\generate-test.bat → Option 1

# 2. Enter feature name
Feature Name: Login

# 3. Record in browser:
# - Click "Sign In" button
# - Enter username
# - Enter password
# - Click "Submit"

# 4. Generated Page Object:
# SIGN_IN_1, USERNAME_2, PASSWORD_3, SUBMIT_4
# clickSignIn(), enterUsername(), enterPassword(), clickSubmit()

# 5. Generated Feature:
# When user clicks on sign in
# When user enters text into username
# When user enters text into password
# When user clicks on submit
```

### Benefits

✅ **Self-Documenting** - Code explains itself  
✅ **Maintainable** - Easy to find and update  
✅ **Readable** - Non-technical people can read features  
✅ **Professional** - Looks hand-crafted, not auto-generated  
✅ **Debuggable** - Comprehensive logging pinpoints issues  
✅ **Future-Proof** - All recordings use this convention automatically  

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

### Maven Warnings (sun.misc.Unsafe)
**Symptom:** Warnings about `sun.misc.Unsafe::staticFieldBase` during Maven builds

**✅ Already Fixed!** The framework includes:
- `.mvn/jvm.config` - Suppresses warnings globally
- `pom.xml` - Configured with proper JVM arguments
- Clean console output during builds

**If warnings still appear:**
```bash
# Verify .mvn/jvm.config exists
cat .mvn/jvm.config

# Should contain:
--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.misc=ALL-UNNAMED
```

**Note:** These are Maven 3.9.x + Java 17+ compatibility warnings from Guice dependency. They don't affect functionality.

---

## 🎬 Recorder Troubleshooting

### Understanding Two Recording Systems

#### 1. Video Recording (recoder.java)
**Purpose:** Screen capture during test execution  
**Output:** AVI files in `MRITestExecutionReports/{version}/recordings/`  
**When Used:** During test execution (not action recording)

#### 2. Action Recording (Playwright CLI Codegen)
**Purpose:** Capture user interactions and generate code  
**Output:** `recorded-actions.java` in temp_recording_* folders  
**When Used:** During `record-and-generate.bat` session

### TODO: Recording Workflow
- [ ] Run `record-and-generate.bat`
- [ ] Playwright codegen starts: `mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI`
- [ ] Perform actions in browser
- [ ] Close browser to trigger parsing
- [ ] TestGeneratorHelper parses `recorded-actions.java`
- [ ] 3 test files generated (Page Object, Feature, Steps)

### TODO: Common Issues Resolution

#### Issue 1: Steps Not Saved (FIXED Dec 26, 2025)
**Symptoms:**
- [ ] Check if `recorded-actions.java` is empty or has no actions
- [ ] Check for "[WARN] No actions found in recording" message
- [ ] Check if generated test files only have navigation

**Resolution:**
- [x] ✅ Added missing `getByLabel().click()` pattern
- [x] ✅ Added missing `getByLabel().fill()` pattern
- [x] ✅ Added missing `getByLabel().press()` pattern

**Supported APIs:**
```java
// Modern API (All Working)
page.locator("selector").click()
page.getByRole(AriaRole.BUTTON, options).click()
page.getByText("text").click()
page.getByLabel("label").click() // ✅ FIXED
page.getByLabel("label").fill("text") // ✅ FIXED
page.getByPlaceholder("placeholder").fill("text")
```

#### Issue 2: Recording File Not Created
**TODO Checklist:**
- [ ] Check Playwright installed: `mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"`
- [ ] Test codegen command: `mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen --help"`
- [ ] Verify temp_recording_* folder created: `dir /b | findstr temp_recording`
- [ ] Ensure you perform actions before closing browser

#### Issue 3: Duplicate Elements
**Expected Behavior:** Each interaction gets its own constant:
- USERNAME_2 (click to focus)
- USERNAME_3 (fill with text)
- USERNAME_4 (press Tab)

**TODO:** Manually merge if needed after generation

#### Issue 4: Dynamic/Random IDs
**TODO:** Use dynamic locator helpers from utils.java:
```java
// Replace this:
clickOnElement("#c5e5f7ef-1180-49e8-a0a3-bfde5d637c1e");

// With this:
String selector = findByText("button", "Save");
clickOnElement(selector);
```

### TODO: Testing Recorder Manually
```batch
# Test parsing
mvn exec:java -D exec.mainClass="configs.TestGeneratorHelper" -D exec.args="path\to\recorded-actions.java featureName pageUrl JIRA-123"

# Example
mvn exec:java -D exec.mainClass="configs.TestGeneratorHelper" -D exec.args="temp_recording_123\recorded-actions.java login /login JIRA-456"
```

**Expected Output Checklist:**
- [ ] "[DEBUG] Found navigate: URL"
- [ ] "[DEBUG] Found getByLabel click: Username"
- [ ] "[DEBUG] Found getByLabel fill: Username = admin"
- [ ] "[SUCCESS] Extracted X actions" where X > 0
- [ ] "All files generated successfully!"

### TODO: Verification After Recording
- [ ] `temp_recording_*/recorded-actions.java` exists
- [ ] File size > 0 bytes
- [ ] File contains Playwright code (not empty)
- [ ] Parser logs show "[DEBUG] Found..." messages
- [ ] Extracted actions count > 0
- [ ] 3 test files generated
- [ ] Generated files compile without errors

### TODO: Adding New Parsing Patterns
If Playwright API changes:
- [ ] Add pattern: `Pattern getByNewMethodPattern = Pattern.compile(...)`
- [ ] Add parsing logic with debug output
- [ ] Update extractReadableName method
- [ ] Recompile: `mvn clean compile`
- [ ] Test with sample recording

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

### Reports Location
- **Extent Reports**: `MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/`
- **Cucumber**: `target/cucumber-reports/cucumber.html`
- **Screenshots**: `MRITestExecutionReports/Version*/screenShots/`
- **Recordings**: `MRITestExecutionReports/Version*/recordings/`

### Key Files
- **Base Classes**: `src/main/java/configs/base.java`, `BasePage.java`
- **Utilities**: `src/main/java/configs/utils.java` (9 dynamic locator helpers)
- **Test Generator**: `src/main/java/configs/TestGeneratorHelper.java`
- **Hooks**: `src/test/java/hooks/hooks.java`
- **Config**: `src/test/resources/configurations.properties`

---

## 🎯 Final Success Criteria

Complete TODO before "DONE":
- [ ] ✅ All recorded actions covered
- [ ] ✅ Recording file created with actions
- [ ] ✅ Parser extracted all actions successfully
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
**Latest Fix**: getByLabel() parsing patterns added

