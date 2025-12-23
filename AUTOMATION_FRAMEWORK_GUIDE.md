# Playwright Automation Framework Guide

> **Professional Test Automation with Playwright + Cucumber + TestNG**

---

## 📑 Table of Contents

1. [Overview](#-overview)
2. [Project Structure](#-project-structure)
3. [Prerequisites](#-prerequisites)
4. [Manual Test Creation](#-manual-test-creation)
5. [Test Execution](#-test-execution)
6. [Configuration Reference](#-configuration-reference)
7. [Best Practices](#-best-practices)
8. [Troubleshooting](#-troubleshooting)

---

## 🎯 Overview

### 🤖 Want AI-Generated Tests? 
**📘 See [AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md) - Create tests in 5 minutes with verification!**

### Framework Features
- **Playwright** for browser automation
- **Cucumber** for BDD scenarios
- **TestNG** for test execution
- **Page Object Model** for maintainability
- **Auto-retry** failed tests
- **ExtentReports** with screenshots & recordings
- **Centralized configuration** and timeouts

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

**Required:**
- Java JDK 22
- Maven 3.x
- IDE (IntelliJ IDEA / VS Code)

**Optional (for AI generation):**
- Node.js 18+ - See [AI_GUIDE.md](AI_GUIDE.md)

**Verify Setup:**
```bash
mvn clean compile test-compile
```

---

## 🔧 Traditional Workflow (Manual Coding)

**If you prefer manual coding instead of AI generation:**

### Step 1: Create Page Object

Create a new file in `src/main/java/pages/`:

```javManual Test Creation Workflow
import com.microsoft.playwright.Page;
import configs.TimeoutConfig;

public class LoginPage extends BasePage {
    
    public LoginPage() {
        super();
    }
    
    protected static void enterUsername(String locator, String username) {
        fillText(locator, username);
    }
    
    protected static void enterPassword(String locator, String password) {
        fillText(locator, password);
    }
    
    protected static void clickLoginButton(String locator) {
        clickElement(locator);
    }
}
```

### Step 2: Create Feature File

Create a new file in `src/test/java/features/`:

```gherkin
Feature: Login Functionality
  
  @Functional @Smoke
  Scenario: Valid login
    Given User is on login page
    When User enters username
    And User enters password
    And User clicks login button
    Then User should be logged in
```

### Step 3: Create Step Definitions

Create a new file in `src/test/java/stepDefs/`:

```java
package stepDefs;

import io.cucumber.java.en.*;
import pages.LoginPage;

public class LoginSteps {
    private LoginPage loginPage;
    
    public LoginSteps() {
        this.loginPage = new LoginPage();
    }
    
    @Given("User is on login page")
    public void userIsOnLoginPage() {
        loginPage.navigateTo("https://app.com/login");
    }
    
    @When("User enters username")
    public void userEntersUsername() {
        loginPage.enterUsername("//input[@id='username']", "testuser");
    }
    
    @When("User enters password")
    public void userEntersPassword() {
        loginPage.enterPassword("//input[@id='password']", "password123");
    }
    
    @When("User clicks login button")
    public void userClicksLoginButton() {
        loginPage.clickLoginButton("//button[@id='login']");
    }
    
    @Then("User should be logged in")
    public void userShouldBeLoggedIn() {
        // Add assertion
    }
}
```

**⚡ Tip:** Use AI generation instead - see [AI_GUIDE.md](AI_GUIDE.md) for 10x faster workflow!

### Step 4: Compile & Run

```bash
mvn clean compile test-compile
mvn test
```

**⚡ Faster Way:** Use AI generation - see [AI_GUIDE.md](AI_GUIDE.md

---

### **STEP 4: Generate Page Object with AI**

**Objective:** Create page class with MCP-captured locators and methods

**AI Prompt Template:**
```
"Create a Page Object class for [PAGE NAME] based on MCP observations:

MCP Captured Locators:
- [element 1]: [Id/XPath/CSS]
- [element 2]: [Id/XPath/CSS]
- [element 3]: [Id/XPath/CSS]

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
- Fun without compiling first
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

- Use descriptive names for page objects and methods
- Follow Gherkin pattern: Given → When → Then
- Always compile before running tests: `mvn clean compile`
- Use tags (@Smoke, @Regression) for test organization
- Review test reports after execution
- Keep page objects focused and maintainable
- UsCommon Issues

| Problem | Solution |
|---------|----------|
| **Build failure** | Run `mvn clean compile test-compile`, check error logs |
| **Tests not running** | Verify testng.xml configured, check test class path |
| **Tests failing** | Check reports in MRITestExecutionReports/, review screenshots |
| **Reports not generated** | Verify Screenshots_Mode=true, check directory permissions |
| **Retry not working** | Verify RetryListener in testng.xml, check MaxRetryCount setting |
| **Browser not launching** | Check Headless_Mode setting, verify browser drivers |
| **Timeouts** | Adjust timeout values in configurations.properties---

## 📚 Additional Resources

- **🤖 AI-Powered Testing:** [AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md)
- **📁 Reports:** `MRITestExecutionReports/`
- **⚙️ Config:** `src/test/resources/configurations.properties`

**Framework Version:** 3.0  
**Last Updated:** December 23, 2025  
**Status:** Production Ready ✅

---