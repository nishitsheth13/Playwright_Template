# Playwright MCP Automation Guide

> **Professional Test Automation with MCP Server & AI**  
> Create and maintain tests through browser interaction and AI assistance

---

## 📑 Table of Contents

1. [Overview](#-overview)
2. [Project Structure](#-project-structure)
3. [Prerequisites](#-prerequisites)
4. [MCP + AI Workflow](#-mcp--ai-workflow)
5. [Step-by-Step Guide](#-step-by-step-guide)
6. [Test Execution](#-test-execution)
7. [Configuration Reference](#-configuration-reference)

---

## 🎯 Overview

This framework enables test automation through:
- **MCP Browser Server**: Capture application behavior and element locators
- **AI Assistant**: Generate test files based on MCP observations
- **Automated Execution**: TestNG-based execution with retry and reporting
- **No Manual Coding**: Let MCP and AI handle the implementation

**Workflow:** Explore → Capture → Instruct AI → Compile → Execute → Verify

---

## 📂 Project Structure

```
Playwright_Template/
│
├── src/
│   ├── main/java/
│   │   ├── configs/
│   │   │   ├── base.java              # Base configuration
│   │   │   ├── browserSelector.java   # Browser management
│   │   │   ├── TimeoutConfig.java     # Centralized timeouts
│   │   │   ├── RetryAnalyzer.java     # Auto-retry failed tests
│   │   │   ├── RetryListener.java     # Apply retry globally
│   │   │   └── utils.java             # Common utilities
│   │   │
│   │   └── pages/                     # Page Object Model
│   │       ├── BasePage.java          # Base for all pages
│   │       ├── login.java
│   │       ├── myaccount.java
│   │       └── portfolio.java
│   │
│   └── test/
│       ├── java/
│       │   ├── features/              # Cucumber Feature Files
│       │   │   ├── login.feature
│       │   │   ├── myaccount.feature
│       │   │   └── portfolio.feature
│       │   │
│       │   ├── stepDefs/              # Step Definitions
│       │   │   ├── login.java
│       │   │   ├── myaccountSteps.java
│       │   │   └── portfolioSteps.java
│       │   │
│       │   ├── hooks/                 # Setup/Teardown
│       │   │   └── hooks.java
│       │   │
│       │   ├── listener/              # TestNG Listeners
│       │   │   └── listener.java
│       │   │
│       │   └── runner/                # Test Runner
│       │       └── testRunner.java
│       │
│       └── resources/
│           ├── configurations.properties
│           ├── extent-config.xml
│           └── testng.xml
│
├── MRITestExecutionReports/          # Generated reports
├── pom.xml                            # Maven dependencies
└── AUTOMATION_FRAMEWORK_GUIDE.md      # This file
```

---

## ✅ Prerequisites

**Required Software:**
- Java JDK 22
- Maven 3.x
- IDE (IntelliJ IDEA / VS Code)
- Playwright MCP Server
- AI Assistant (GitHub Copilot / ChatGPT)

**Verification:**
```bash
mvn clean compile test-compile
# Expected: BUILD SUCCESS
```

---

## 🔄 MCP + AI Workflow

### **Complete Process Flow**

```
┌─────────────────────────────────────────────────────────────────┐
│  PHASE 1: EXPLORATION (MCP Browser)                             │
├─────────────────────────────────────────────────────────────────┤
│  1. Start MCP Browser                                           │
│  2. Navigate to application                                     │
│  3. Interact with elements (click, type, navigate)              │
│  4. Capture snapshots and locators                              │
│  5. Document behavior and flows                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  PHASE 2: GENERATION (AI Assistant)                             │
├─────────────────────────────────────────────────────────────────┤
│  6. Provide MCP observations to AI                              │
│  7. AI generates Feature file (Gherkin)                         │
│  8. AI generates Page Object (with MCP locators)                │
│  9. AI generates Step Definitions (connects feature to page)    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  PHASE 3: VALIDATION & EXECUTION                                │
├─────────────────────────────────────────────────────────────────┤
│  10. Compile project: mvn clean compile test-compile            │
│  11. Execute tests: mvn test -DsuiteXmlFile=testng.xml          │
│  12. Verify reports in MRITestExecutionReports/                 │
│  13. Close MCP browser                                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📖 Step-by-Step Guide

### **STEP 1: Launch MCP Browser**

**Objective:** Start Playwright MCP server for application exploration

**Actions:**
1. Activate MCP browser tools in your IDE
2. Ensure browser window opens successfully
3. Verify MCP tools are available

**MCP Tools Available:**
- `browser_navigate` - Navigate to URLs
- `browser_snapshot` - Capture page structure
- `browser_click` - Click elements
- `browser_type` - Enter text
- `browser_fill_form` - Fill forms

---

### **STEP 2: Explore Application with MCP**

**Objective:** Capture complete user journey and element information

**Process:**

1. **Navigate to Application**
   ```
   Tool: browser_navigate
   URL: https://your-application.com/page
   ```

2. **Take Snapshots**
   - Capture initial page state
   - Identify all interactive elements
   - Note element attributes (ID, XPath, CSS)

3. **Interact with Elements**
   - Click buttons, links, menus
   - Fill forms and input fields
   - Navigate between pages
   - Verify outcomes

4. **Document Observations**
   - Page URLs
   - Element locators (XPath/CSS selectors)
   - User actions sequence
   - Expected vs actual behavior
   - Success/error messages

**Example MCP Session:**
```
1. Navigate: https://app.com/login
2. Snapshot: Identify username → //input[@id='Username']
3. Type: Enter "admin"
4. Snapshot: Identify password → //input[@id='Password']
5. Type: Enter password
6. Snapshot: Identify button → //button[@name='button']
7. Click: Sign In button
8. Snapshot: Verify dashboard loaded
```

---

### **STEP 3: Generate Feature File with AI**

**Objective:** Create Gherkin feature file based on MCP observations

**AI Prompt Template:**
```
"Create a Cucumber feature file for [FUNCTIONALITY] based on MCP observations:

MCP Observations:
- Page URL: [url]
- User Flow: [describe steps observed]
- Element Locators:
  - [element name]: [XPath/CSS from MCP]
  - [element name]: [XPath/CSS from MCP]

Requirements:
- File: src/test/java/features/[name].feature
- Tags: @Smoke @Functional @Priority=0
- Include Background section
- Business-readable language
- Cover positive and negative scenarios"
```

**AI Output Location:** `src/test/java/features/[name].feature`

**What AI Generates:**
- Feature declaration with description
- Background (common setup steps)
- Multiple scenarios (happy path, error cases)
- Given-When-Then structure
- Appropriate tags for execution control

---

### **STEP 4: Generate Page Object with AI**

**Objective:** Create page class with MCP-captured locators and methods

**AI Prompt Template:**
```
"Create a Page Object class for [PAGE NAME] based on MCP observations:

MCP Captured Locators:
- [element 1]: [XPath/CSS]
- [element 2]: [XPath/CSS]
- [element 3]: [XPath/CSS]

Requirements:
- File: src/main/java/pages/[PageName].java
- Extend BasePage
- Use TimeoutConfig for waits
- Follow naming conventions:
  - Locators: TXT_*, BTN_*, LBL_*, DDL_*
  - Methods: camelCase (click*, enter*, get*, is*)
- Include sections:
  - LOCATORS (from MCP)
  - NAVIGATION methods
  - ACTIONS methods
  - VALIDATIONS methods
- Add emoji logs (🌐 📝 🖱️ ✅)
- No assertions in page methods (return boolean)"
```

**AI Output Location:** `src/main/java/pages/[PageName].java`

**What AI Generates:**
- Class extending BasePage
- Static final String locators
- Navigation method (navigateToPage)
- Action methods (enter*, click*, select*)
- Validation methods (is*, get*)
- Proper imports and structure

---

### **STEP 5: Generate Step Definitions with AI**

**Objective:** Connect Gherkin steps to page object methods

**AI Prompt Template:**
```
"Create Step Definitions for [FEATURE NAME]:

Requirements:
- File: src/test/java/stepDefs/[Name]Steps.java
- Connect feature file: [feature file name]
- Use page object: [PageName].java
- Extend browserSelector
- Map each Gherkin step to page method
- Add assertions in @Then steps
- Use TestNG Assert
- Match exact step text from feature file"
```

**AI Output Location:** `src/test/java/stepDefs/[Name]Steps.java`

**What AI Generates:**
- Class extending browserSelector
- @Given, @When, @Then, @And methods
- Method calls to page objects
- Assertions for verification steps
- Proper parameter handling
- Error messages for failures

---

### **STEP 6: Compile Project**

**Objective:** Verify all generated code compiles successfully

**Command:**
```bash
mvn clean compile test-compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Compiling XX source files
[INFO] Compiling XX test sources
```

**If Compilation Fails:**

**Ask AI to Fix:**
```
"Fix compilation error in [file]:

Error message:
[paste error from console]

Context:
- Feature file: [name]
- Page object: [name]
- Step definitions: [name]"
```

**Common Issues AI Can Fix:**
- Missing imports
- Method name mismatches
- Incorrect locator syntax
- Parameter type errors
- Package declaration issues

---

### **STEP 7: Execute Tests**

**Objective:** Run tests using TestNG and verify results

**Primary Execution (Recommended):**
```bash
mvn test -DsuiteXmlFile=src/test/testng.xml -Dcucumber.filter.tags="@Smoke"
```

**Why TestNG XML:**
- ✅ Loads RetryAnalyzer automatically
- ✅ Applies all listeners (reporting, retry)
- ✅ Ensures consistent execution
- ✅ Works in all environments

**Alternative Execution:**
```bash
# Smoke tests
mvn test -Dcucumber.filter.tags="@Smoke"

# Headless mode
mvn test -DsuiteXmlFile=src/test/testng.xml -DHeadless_Mode=true

# Different browser
mvn test -DsuiteXmlFile=src/test/testng.xml -DBrowser=firefox

# Specific feature
mvn test -Dcucumber.features="src/test/java/features/[name].feature"
```

---

### **STEP 8: Verify Reports**

**Objective:** Confirm test execution and review results

**Report Location:**
```
MRITestExecutionReports/
└── Version20252Build019/
    └── extentReports/
        └── testNGExtentReports/
            └── spark/
                └── spark_{timestamp}.html
```

**Verification Checklist:**
- [ ] All scenarios executed
- [ ] PASS/FAIL status correct
- [ ] Screenshots attached (for failures)
- [ ] Test duration recorded
- [ ] Retry attempts logged (if any)
- [ ] Browser/environment info present

**If Tests Fail - Ask AI:**
```
"Test failed with error:
[paste error/screenshot]

Fix the following:
- Page object: [name]
- Step definitions: [name]
- Expected: [behavior]
- Actual: [behavior]"
```

---

### **STEP 9: Update Existing Tests with AI**

**Objective:** Maintain tests when application changes

**Scenario 1: Update Locators**
```
"Application updated. Update locators in [PageName]:

Old locator: //button[@name='button']
New locator: //button[@id='signin-btn']

MCP verified: new locator works correctly"
```

**Scenario 2: Add New Scenario**
```
"Add new scenario to [feature file]:

Functionality: [describe]
MCP observations: [new locators and flow]

Update:
- Feature file with new scenario
- Page object with new methods
- Step definitions with new steps"
```

**Scenario 3: Add Validations**
```
"Add validation methods to [PageName]:

New MCP locators:
- Success message: //div[@class='success']
- Error badge: //span[@class='error-badge']

Add methods:
- isSuccessDisplayed()
- getSuccessMessage()
- isErrorPresent()

Update step definitions to use new validations"
```

---

### **STEP 10: Close MCP Browser**

**Objective:** Properly cleanup MCP session

**When to Close:**
- ✅ After all tests execute successfully
- ✅ After reports are verified
- ✅ Never during active test execution

**Command:**
```
MCP Tool: browser_close
```

---

## 🏃 Test Execution

### **Primary Method (TestNG XML)**

```bash
# Recommended for all environments
mvn test -DsuiteXmlFile=src/test/testng.xml
```

**With Options:**
```bash
# Smoke tests
mvn test -DsuiteXmlFile=src/test/testng.xml -Dcucumber.filter.tags="@Smoke"

# Headless mode
mvn test -DsuiteXmlFile=src/test/testng.xml -DHeadless_Mode=true

# Specific browser
mvn test -DsuiteXmlFile=src/test/testng.xml -DBrowser=firefox

# Combined
mvn test -DsuiteXmlFile=src/test/testng.xml \
  -Dcucumber.filter.tags="@Smoke" \
  -DHeadless_Mode=true \
  -DBrowser=chrome
```

### **Execution Options**

| Command | Purpose |
|---------|---------|
| `-DsuiteXmlFile=src/test/testng.xml` | Use TestNG configuration (REQUIRED) |
| `-Dcucumber.filter.tags="@Smoke"` | Run smoke tests only |
| `-Dcucumber.filter.tags="@Priority=0"` | Run critical tests |
| `-DHeadless_Mode=true` | Run without browser UI |
| `-DBrowser=chrome\|firefox\|edge` | Specify browser |
| `-Dcucumber.features="path/to/file.feature"` | Run specific feature |

### **Environment Overrides**

```bash
# Different environment
mvn test -DsuiteXmlFile=src/test/testng.xml -DURL="https://staging.app.com"

# Different credentials
mvn test -DsuiteXmlFile=src/test/testng.xml -DUsername="testuser"

# Disable retry
mvn test -DsuiteXmlFile=src/test/testng.xml -DMaxRetryCount=0
```

---

## ⚙️ Configuration Reference

### **configurations.properties**

Location: `src/test/resources/configurations.properties`

```properties
# Application
URL=https://your-app.com
Username=admin
Password=yourpassword

# Browser
Browser=chrome
Headless_Mode=false

# Retry & Timeout
MaxRetryCount=2
DefaultTimeout=30000
PageLoadTimeout=60000
ElementWaitTimeout=10000

# Reporting
Screenshots_Mode=true
Recording_Mode=true
Remove_Old_Reports=true
Version_Record=3
```

### **testng.xml**

Location: `src/test/testng.xml`

```xml
<suite name="Test Suite">
    <listeners>
        <listener class-name="listener.listener"/>
        <listener class-name="configs.RetryListener"/>
    </listeners>
    <test name="Test Execution">
        <classes>
            <class name="runner.testRunner"/>
        </classes>
    </test>
</suite>
```

**Key Components:**
- `listener.listener` - ExtentReports generation
- `configs.RetryListener` - Automatic retry on failure
- `runner.testRunner` - Cucumber test runner

---

## 🎯 AI Prompt Templates

### **Quick Reference**

| Task | Prompt |
|------|--------|
| **Feature File** | "Create feature for [page] with scenarios [list]. Use MCP locators: [list]" |
| **Page Object** | "Create [PageName] extending BasePage. MCP locators: [list]. Methods: [list]" |
| **Step Definitions** | "Create steps for [feature]. Connect to [PageName]. Add assertions" |
| **Update Locator** | "Update [element] in [PageName] from [old] to [new]. MCP verified" |
| **Add Scenario** | "Add scenario to [feature]: [description]. MCP locators: [list]" |
| **Fix Error** | "Fix error in [file]: [error message]. Expected: [behavior]" |
| **Add Validation** | "Add methods to [PageName]: [list]. MCP locators: [list]" |

---

## 📊 Best Practices

### **DO ✅**

**MCP Exploration:**
- Take snapshots at every state change
- Document complete user journeys
- Verify locators work in MCP before AI generation
- Capture both success and error scenarios

**AI Interaction:**
- Provide complete context (MCP observations + requirements)
- Request adherence to project conventions
- Ask AI to use TimeoutConfig and BasePage
- Review generated code before compiling

**Test Execution:**
- Always use TestNG XML for consistency
- Run smoke tests before full regression
- Verify reports after each execution
- Fix failures immediately

### **DON'T ❌**

**MCP Usage:**
- Close browser before test execution completes
- Skip capturing element locators
- Assume locators without MCP verification

**AI Usage:**
- Give vague prompts without context
- Accept code without review
- Skip compilation verification
- Ignore framework conventions

**Test Execution:**
- Run without compiling first
- Ignore test failures
- Skip report verification
- Commit untested code

---

## 🆘 Troubleshooting

### **MCP Issues**

| Problem | Solution |
|---------|----------|
| Browser not starting | Verify MCP server running, check browser tools activated |
| Locators not working | Re-capture from fresh snapshot, verify XPath syntax |
| Snapshot incomplete | Refresh page, wait for load, take new snapshot |

### **Compilation Issues**

| Problem | Solution |
|---------|----------|
| Build failure | Run `mvn clean compile test-compile`, check errors |
| Missing imports | Ask AI: "Fix imports in [file]" |
| Method not found | Verify page object has method, check spelling |
| Syntax errors | Ask AI: "Fix syntax error: [error message]" |

### **Execution Issues**

| Problem | Solution |
|---------|----------|
| Tests not running | Verify testng.xml configured, check tags match |
| Tests failing | Check reports for screenshots, ask AI to fix |
| Reports not generated | Verify Screenshots_Mode=true, check write permissions |
| Retry not working | Verify RetryListener in testng.xml, check MaxRetryCount |

---

## 📞 Support

**Framework Version:** 3.0  
**Last Updated:** December 20, 2025  
**Status:** Production Ready ✅

**For Issues:**
1. Check Troubleshooting section
2. Review report screenshots
3. Ask AI to fix with error context
4. Contact automation team

---

**End of Guide**
