# 🔧 Complete Test Generation Guide

## 📝 Purpose
**SINGLE SOURCE OF TRUTH** - Everything you need to generate, validate, and run tests successfully.

---

## 🎯 CRITICAL REMINDERS - Read This First!

### 📁 Folder Structure (NEVER Change This!)

```
src/
├── main/java/
│   ├── configs/                    # ⚠️ Framework code - DON'T modify
│   │   ├── base.java
│   │   ├── browserSelector.java    # Step definitions extend this
│   │   ├── utils.java              # USE these methods!
│   │   ├── loadProps.java          # For getProperty("URL")
│   │   ├── BasePage.java           # Page objects extend this
│   │   ├── RetryAnalyzer.java
│   │   ├── RetryListener.java
│   │   └── TimeoutConfig.java
│   └── pages/                      # ✅ CREATE page objects here
│       ├── BasePage.java
│       ├── login.java
│       └── YourPage.java           # Your new page objects
│
└── test/
    ├── java/
    │   ├── features/               # ✅ CREATE .feature files here
    │   │   ├── login.feature
    │   │   └── YourTest.feature    # Your new feature files
    │   ├── stepDefs/               # ✅ CREATE step definitions here
    │   │   ├── loginSteps.java
    │   │   └── YourSteps.java      # Your new step definitions
    │   ├── hooks/                  # ⚠️ DON'T modify
    │   │   └── hooks.java
    │   ├── listener/               # ⚠️ DON'T modify
    │   │   └── listener.java
    │   └── runner/                 # ⚠️ DON'T modify
    │       └── testRunner.java
    └── resources/
        ├── configurations.properties  # ⚠️ Read only
        ├── testng.xml                # ⚠️ DON'T modify
        └── extent-config.xml         # ⚠️ DON'T modify
```

### 📝 File Creation Rules

**✅ ALWAYS CREATE:**
1. **Page Object** → `src/main/java/pages/YourPage.java`
2. **Feature File** → `src/test/java/features/YourTest.feature`
3. **Step Definition** → `src/test/java/stepDefs/YourSteps.java`

**❌ NEVER CREATE:**
- Extra MD files (use COMPLETE_TEST_GUIDE.md only)
- Extra BAT files (use generate-test.bat only)
- Files outside standard folders
- Duplicate utilities (use utils.java methods!)

### 🔑 Key Patterns (MANDATORY!)

**Every Page Object Must:**
```java
package pages;                      // ✅ Correct package
import com.microsoft.playwright.Page;
import configs.loadProps;           // ✅ MUST import this!

public class YourPage extends BasePage {  // ✅ Extend BasePage
    private static final String LOCATOR = "selector";
    
    public static void method(Page page) {  // ✅ public static
        clickOnElement(LOCATOR);     // ✅ Use utils method!
    }
}
```

**Every Step Definition Must:**
```java
package stepDefs;                   // ✅ Correct package
import configs.browserSelector;     // ✅ MUST import this!
import io.cucumber.java.en.*;

public class YourSteps extends browserSelector {  // ✅ Extend this!
    // ✅ NO 'private Page page' - inherited!
    
    @Given("step text")             // ✅ ONE annotation only!
    public void step() {
        YourPage.method(page);       // ✅ Use page from parent
    }
}
```

### 🚫 NEVER DO These!

1. ❌ Create multiple MD/BAT files → Use existing ones
2. ❌ Multiple @Given/@When/@Then on same method → Causes DuplicateStepDefinitionException
3. ❌ Create custom click/type methods → Use utils.java methods
4. ❌ Use `protected static` → Always use `public static`
5. ❌ Use `BASE_URL()` → Use `getProperty("URL")`
6. ❌ Hardcode test data → Use `loadProps.getProperty()` from configurations.properties
7. ❌ Skip compilation → Always compile before running tests
8. ❌ Modify testng.xml/hooks/runner → Framework files are read-only

---

## ⚡ QUICK REFERENCE (Print & Keep Visible!)

### One-Line Command
```bash
generate-test.bat validate  # Run validation only (skip generation)
generate-test.bat           # Generate new test + auto-validate
```

### Common Methods (Use These, Don't Reinvent!)
```java
// From utils.java - ALWAYS use these!
clickOnElement(locator)
enterText(locator, text)
clearAndEnterText(locator, text)
isElementPresent(locator)
selectDropDownValueByText(locator, text)

// From loadProps.java - ALWAYS use for test data!
loadProps.getProperty("URL")           // Base URL
loadProps.getProperty("Username")      // Test username
loadProps.getProperty("Password")      // Test password
loadProps.getProperty("Browser")       // Browser type
// Add any custom test data to configurations.properties
```

### Test Data Management

**✅ ALWAYS use configurations.properties for test data:**

Available test data in `src/test/resources/configurations.properties`:
- `URL` - Application base URL
- `Username` - Test user username
- `Password` - Test user password
- `Browser` - Browser type (chrome/firefox/edge)
- `Headless_Mode` - Run in headless mode (true/false)
- `DefaultTimeout`, `ElementWaitTimeout`, `PageLoadTimeout` - Timeouts
- `Screenshots_Mode`, `Recording_Mode` - Reporting options

**❌ NEVER hardcode test data in page objects or step definitions!**

```java
// ❌ WRONG - Hardcoded
page.navigate("https://example.com");
enterText(username, "testuser");

// ✅ CORRECT - From configuration
page.navigate(loadProps.getProperty("URL"));
enterText(username, loadProps.getProperty("Username"));
```

**Adding new test data:**
1. Add property to `configurations.properties`:
   ```properties
   NewTestData=value
   ```
2. Use in code:
   ```java
   String data = loadProps.getProperty("NewTestData");
   ```

### Quick Templates

**Page Object:**
```java
package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;  // ⚠️ MUST HAVE!

public class YourPage extends BasePage {
    private static final String LOCATOR = "selector";
    
    public static void navigate(Page page) {
        // ✅ ALWAYS use getProperty for URL!
        page.navigate(loadProps.getProperty("URL") + "/path");
    }
    
    public static void performAction(Page page) {
        clickOnElement(LOCATOR);  // ✅ Use utils method!
    }
    
    public static void login(Page page) {
        // ✅ ALWAYS use getProperty for test data!
        enterText("input#username", loadProps.getProperty("Username"));
        enterText("input#password", loadProps.getProperty("Password"));
        clickOnElement("button#login");
    }
}
```

**Step Definition:**
```java
package stepDefs;
import configs.browserSelector;  // ⚠️ MUST HAVE!
import io.cucumber.java.en.*;

public class YourSteps extends browserSelector {  // ⚠️ Extend this!
    @Given("step text")  // ⚠️ ONE annotation per method!
    public void step() {
        YourPage.method(page);  // page inherited
    }
}
```

### 🚨 Never Do These!
- ❌ Multiple @Given/@When/@Then on same method
- ❌ Create custom click/type methods (use utils.java!)
- ❌ Use `protected static` (always `public static`)
- ❌ Skip compilation before running tests
- ❌ Give up after first failure (keep fixing!)

---

## 🚀 MANDATORY WORKFLOW - Follow This Every Time!

### ⚠️ CRITICAL: Use This Workflow For EVERY New Test Generation

```
1. ANALYZE → 2. REUSE → 3. COMPILE → 4. FIX → 5. RUN → 6. REPEAT
```

**Quick Command:**
```bash
validate-and-run.bat  # Automated workflow script
```

### Step-by-Step Mandatory Process:

#### 1️⃣ ANALYZE PROJECT STRUCTURE (Before Writing Any Code)

**Check existing patterns:**
```bash
# Review existing page objects
dir src\main\java\pages\*.java

# Review existing step definitions  
dir src\test\java\stepDefs\*.java

# Review common utilities
type src\main\java\configs\utils.java | more
```

**Identify reusable components:**
- ✅ Check `utils.java` for common methods
- ✅ Check `BasePage.java` for inheritance pattern
- ✅ Check `browserSelector.java` for page access
- ✅ Review existing page objects for similar patterns
- ✅ Review existing step definitions for similar scenarios

#### 2️⃣ REUSE COMMON METHODS (Maximum Code Reusability)

**Available Common Methods in `utils.java`:**

```java
// Element Interactions
clickOnElement(String element)           // Click with visibility check
enterText(String element, String text)    // Clear and enter text
clearAndEnterText(String element, String text)
isElementPresent(String element)         // Check element visibility
isElementEnabled(String element)
isElementVisible(String element)
selectDropDownValueByText(String element, String text)

// Waits
waitForElement(String element, int timeout)
waitForPageLoad()

// Assertions
verifyElementText(String element, String expectedText)
verifyElementVisible(String element)

// Text Operations
getText(String element)
getAttributeValue(String element, String attribute)
```

**❌ DON'T create new methods for these operations:**
```java
// ❌ WRONG - Reinventing the wheel
public static void clickButton(String locator) {
    page.locator(locator).click();
}
```

**✅ DO reuse existing utility methods:**
```java
// ✅ CORRECT - Using common method
public static void clickSubmitButton(Page page) {
    clickOnElement(SUBMIT_BUTTON);  // From utils.java
}
```

#### 3️⃣ COMPILE PROJECT (Catch Errors Early)

```bash
# Always compile BEFORE running tests
mvn clean compile test-compile

# If compilation fails, check:
# 1. Missing imports
# 2. Wrong access modifiers (protected vs public)
# 3. Wrong method names
# 4. Missing class extensions
```

#### 4️⃣ FIX COMPILATION ERRORS (Use This Guide)

See the error tables below for quick fixes.

#### 5️⃣ RUN TESTS (Always Execute testng.xml)

```bash
# MANDATORY: Run full test suite
mvn test -DsuiteXmlFile=src/test/testng.xml

# For specific tags (optional)
mvn test -DsuiteXmlFile=src/test/testng.xml -Dcucumber.filter.tags="@YourTag"
```

#### 6️⃣ REPEAT IF NEEDED (Fix Until All Tests Pass)

- If tests fail, analyze error
- Fix the issue
- Recompile (Step 3)
- Run tests again (Step 5)
- **Repeat until all tests pass**

---

## 📋 PROJECT STRUCTURE ANALYSIS CHECKLIST

Before creating ANY new test, verify:

- [ ] Reviewed existing page objects in `src/main/java/pages/`
- [ ] Identified reusable methods in `utils.java`
- [ ] Checked `BasePage.java` for inheritance pattern
- [ ] Reviewed `browserSelector.java` for page/browser access
- [ ] Checked existing step definitions for similar patterns
- [ ] Identified common locator strategies used
- [ ] Reviewed existing feature files for Gherkin patterns
- [ ] Checked `loadProps.java` for property access patterns
- [ ] Verified all required imports

---

## ✅ Mandatory Checklist for New Page Objects

### 1. **Required Imports**
Always include these imports in page object classes:

```java
package pages;

import com.microsoft.playwright.Page;
import configs.loadProps;  // ⚠️ MUST INCLUDE for URL configuration
```

**❌ Common Error:**
```
cannot find symbol: variable loadProps
```

**✅ Fix:** Always import `configs.loadProps` in page objects

---

### 2. **Method Access Modifiers**
Use `public static` for methods that will be called from step definitions.

**❌ Wrong:**
```java
protected static void navigateToPage(Page page) {
    // method code
}
```

**✅ Correct:**
```java
public static void navigateToPage(Page page) {
    // method code
}
```

**❌ Common Error:**
```
navigateToPage(Page) has protected access in PageClass
```

**✅ Fix:** Replace `protected static` with `public static` for all methods accessed from step definitions

**Quick Fix Command:**
```powershell
(Get-Content 'path\to\PageObject.java') -replace 'protected static', 'public static' | Set-Content 'path\to\PageObject.java'
```

---

### 3. **Loading Properties / URLs**
Use the correct method to access configuration properties.

**❌ Wrong:**
```java
page.navigate(loadProps.BASE_URL() + "/my-account");
```

**✅ Correct:**
```java
page.navigate(loadProps.getProperty("URL") + "/my-account");
```

**❌ Common Error:**
```
cannot find symbol: method BASE_URL()
```

**✅ Fix:** Use `loadProps.getProperty("URL")` instead of `loadProps.BASE_URL()`

---

### 4. **Login Method Calls**
Use the correct login method from the login class.

**❌ Wrong:**
```java
login.loginToApplication(page, username, password);
```

**✅ Correct:**
```java
login.loginWith(username, password);
```

**❌ Common Error:**
```
cannot find symbol: method loginToApplication(Page,String,String)
```

**✅ Fix:** Use `login.loginWith(username, password)` without passing the page object

---

## 📋 Standard Page Object Template

```java
package pages;

import com.microsoft.playwright.Page;
import configs.loadProps;

/**
 * Page Object Model for [Page Name]
 * [Description of functionality]
 * 
 * @author QA Automation Team
 * @version 1.0.0
 * @story [JIRA-ID] - [Story Title]
 */
public class PageName extends BasePage {
    
    // ===== Locators =====
    private static final String ELEMENT_LOCATOR = "css#selector";
    
    /**
     * Navigate to page
     * @param page Playwright page instance
     */
    public static void navigateToPage(Page page) {
        page.navigate(loadProps.getProperty("URL") + "/page-path");
        page.waitForLoadState();
    }
    
    /**
     * Method description
     * @param page Playwright page instance
     * @param param Parameter description
     */
    public static void methodName(Page page, String param) {
        // Method implementation with minimal logging
        page.fill(ELEMENT_LOCATOR, param);
    }
    
    /**
     * Verification method
     * @param page Playwright page instance
     * @return true if condition met
     */
    public static boolean isElementVisible(Page page) {
        return page.isVisible(ELEMENT_LOCATOR);
    }
}
```

---

## 📋 Standard Step Definition Template

```java
package stepDefs;

import com.microsoft.playwright.Page;
import configs.browserSelector;
import io.cucumber.java.en.*;
import org.testng.Assert;
import pages.login;
import pages.YourPageObject;

/**
 * Step Definitions for [Feature Name]
 * 
 * @author QA Automation Team
 * @version 1.0.0
 * @story [JIRA-ID] - [Story Title]
 */
public class YourFeatureSteps extends browserSelector {  // ✅ Extend browserSelector, NOT base
    
    // ✅ No need to declare 'page' - inherited from browserSelector
    
    @Given("user is logged into the system")
    public void userIsLoggedIn() {
        // ✅ page is available from parent class
        login.loginWith("admin", "password123");
    }
    
    @When("user navigates to page")
    public void userNavigatesToPage() {
        YourPageObject.navigateToPage(page);
    }
    
    @Then("element should be visible")
    public void elementShouldBeVisible() {
        Assert.assertTrue(YourPageObject.isElementVisible(page),
                "Element is not visible");
    }
    
    // ⚠️ CRITICAL: Never use multiple annotations on same method!
    // ❌ WRONG:
    // @Given("user logs in")
    // @When("user logs in")
    // public void login() { }
    
    // ✅ CORRECT: Create separate methods or use private helper
    @Given("user logs in as {string}")
    public void userLogsInGiven(String userName) {
        performLogin(userName);
    }
    
    @When("user logs in as {string}")
    public void userLogsInWhen(String userName) {
        performLogin(userName);
    }
    
    private void performLogin(String userName) {
        login.loginWith(userName, "password123");
    }
}
```

---

## 🔍 Locator Best Practices

### Priority Order:
1. **CSS Selectors** (Fastest, most reliable)
   ```java
   private static final String BUTTON = "button#submitBtn";
   private static final String INPUT = "input[name='username']";
   ```

2. **XPath** (When CSS is not sufficient)
   ```java
   private static final String LABEL = "//label[contains(text(),'Username')]";
   ```

3. **Playwright Role Selectors** (For accessibility)
   ```java
   page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit"));
   ```

### Minimize Logging:
**❌ Too much logging:**
```java
System.out.println("🔹 Clicking button...");
page.click(BUTTON);
System.out.println("✅ Button clicked");
```

**✅ Minimal logging:**
```java
page.click(BUTTON);  // Let test reports handle logging
```

---

## 🚀 Compilation & Execution Checklist

### Before Running Tests:

1. **Compile Main Classes**
   ```bash
   mvn clean compile -DskipTests
   ```
   
2. **Compile Test Classes**
   ```bash
   mvn test-compile
   ```
   
3. **Run Specific Tests**
   ```bash
   mvn test -Dcucumber.filter.tags="@ECS-14"
   ```
   
4. **Run Full Test Suite**
   ```bash
   mvn test -DsuiteXmlFile=src/test/testng.xml
   ```

---

## 🐛 Common Compilation Errors & Quick Fixes

| Error | Fix |
|-------|-----|
| `cannot find symbol: variable loadProps` | Add `import configs.loadProps;` |
| `cannot find symbol: method BASE_URL()` | Use `loadProps.getProperty("URL")` |
| `has protected access in PageClass` | Change `protected static` to `public static` |
| `cannot find symbol: method loginToApplication` | Use `login.loginWith(username, password)` |
| `cannot find symbol: class PageName` | Ensure page object is in `pages` package |
| `package pages does not exist` | Ensure `package pages;` is first line |
| `DuplicateStepDefinitionException` | **NEVER use multiple annotations on same method!** Create separate methods |

### ⚠️ CRITICAL: Cucumber Duplicate Step Definitions

**❌ WRONG - This will cause DuplicateStepDefinitionException:**
```java
@Given("user logs in as {string}")
@When("user logs in as {string}")
public void userLogsIn(String userName) {
    // This creates TWO step definitions!
}
```

**✅ CORRECT - Create separate methods:**
```java
@Given("user logs in as {string}")
public void userLogsInGiven(String userName) {
    login.loginWith(userName, "password123");
}

@When("user logs in as {string}")  
public void userLogsInWhen(String userName) {
    login.loginWith(userName, "password123");
}
```

**Better Alternative - Use a common private method:**
```java
@Given("user logs in as {string}")
public void userLogsInGiven(String userName) {
    performLogin(userName);
}

@When("user logs in as {string}")  
public void userLogsInWhen(String userName) {
    performLogin(userName);
}

private void performLogin(String userName) {
    login.loginWith(userName, "password123");
}
```

---

## 📦 Required Dependencies Check

Ensure `pom.xml` contains:
- ✅ Playwright dependencies
- ✅ Cucumber dependencies
- ✅ TestNG dependencies
- ✅ ExtentReports dependencies

---

## 🎯 Test Generation Workflow

1. ✅ Fetch JIRA story details
2. ✅ Analyze requirements and plan scenarios
3. ✅ Create page objects with proper imports
4. ✅ Use `public static` for all methods
5. ✅ Use correct loadProps syntax
6. ✅ Create feature file with comprehensive scenarios
7. ✅ Create step definitions with correct method calls
8. ✅ Compile main classes (`mvn compile`)
9. ✅ Compile test classes (`mvn test-compile`)
10. ✅ Run tests (`mvn test`)
11. ✅ Validate test execution and reports

---

## 📊 Post-Generation Validation

After generating tests, always verify:

```bash
# 1. Compile check
mvn clean compile test-compile

# 2. Syntax validation
mvn validate

# 3. Run specific test
mvn test -Dcucumber.filter.tags="@YourTag"

# 4. Check reports
# Navigate to: MRITestExecutionReports/Version*/extentReports/
```

---

## 🔄 Quick Reference Commands

```bash
# Clean build
mvn clean

# Compile only
mvn compile -DskipTests

# Compile tests
mvn test-compile

# Run all tests
mvn test

# Run with testng.xml
mvn test -DsuiteXmlFile=src/test/testng.xml

# Run specific tag
mvn test -Dcucumber.filter.tags="@Smoke"

# Fix protected to public (PowerShell)
(Get-Content 'file.java') -replace 'protected static', 'public static' | Set-Content 'file.java'
```

---

## ⚠️ NEVER FORGET

1. **Always import `configs.loadProps`** in page objects
2. **Always use `public static`** for methods called from step definitions
3. **Always use `loadProps.getProperty("URL")`** not `BASE_URL()`
4. **Always use `login.loginWith()`** not `loginToApplication()`
5. **Always compile before running**: `mvn compile test-compile`
6. **Always run testng.xml**: `mvn test -DsuiteXmlFile=src/test/testng.xml`
7. **Always use minimal logging** in page objects
8. **Always add proper JavaDoc** comments
9. **Always extend BasePage** for page objects
10. **Always validate with compilation** after generating files

---

---

## 📋 Quick Checklist

### Before Writing Code
- [ ] Review `src\main\java\pages\` for existing page objects
- [ ] Review `src\test\java\stepDefs\` for existing step definitions
- [ ] Check `utils.java` for common methods
- [ ] Identify patterns: BasePage, browserSelector, public static

### While Writing Code
- [ ] Use `clickOnElement()`, `enterText()` from utils.java
- [ ] Extend BasePage for page objects
- [ ] Extend browserSelector for step definitions
- [ ] Use `public static` methods
- [ ] Import configs.loadProps
- [ ] ONE Cucumber annotation per method

### After Writing Code
- [ ] Run: `mvn clean compile test-compile`
- [ ] Fix any errors using error table above
- [ ] Run: `mvn test -DsuiteXmlFile=src/test/testng.xml`
- [ ] If failed, fix and repeat
- [ ] Check reports in MRITestExecutionReports/

---

## 🤖 AI Prompt Templates

### JIRA-Based (Recommended)
```
Generate test from JIRA story {ISSUE-KEY} with:
- All test parameters and scenarios
- Proper locators using MCP server
- Minimal logging
```

### Manual Generation
```
Using MCP server, create {feature} test with:
Elements: {list elements}
Scenarios: {list scenarios}
Verification: functional + UI + performance
```

### Update Existing
```
Update {feature} test to add:
- New scenarios: {list}
- Additional validation for {aspects}
```

---

## 🎯 Framework Overview

### Tech Stack
- **Playwright** - Browser automation
- **Cucumber BDD** - Gherkin feature files
- **TestNG** - Test execution & reporting
- **ExtentReports** - HTML/Spark reports with screenshots
- **Page Object Model** - Maintainable architecture

### Project Structure
```
src/
├── main/java/
│   ├── configs/          # Framework configuration
│   │   ├── browserSelector.java
│   │   ├── utils.java    # Common methods - USE THESE!
│   │   ├── loadProps.java
│   │   └── BasePage.java
│   └── pages/            # Page objects extend BasePage
│       ├── login.java
│       └── YourPage.java
└── test/
    ├── java/
    │   ├── stepDefs/     # Step definitions extend browserSelector
    │   ├── features/     # .feature files (Gherkin)
    │   └── runner/       # TestNG runner
    └── resources/
        ├── configurations.properties
        └── testng.xml    # Test suite configuration
```

---

## 🔧 Manual Test Creation

### 1. Create Page Object
```java
package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;

public class MyPage extends BasePage {
    private static final String BUTTON = "button#submit";
    
    public static void clickSubmit(Page page) {
        clickOnElement(BUTTON);  // From utils.java
    }
}
```

### 2. Create Feature File
```gherkin
@ECS-123 @Smoke
Feature: My Feature

  Scenario: Test scenario
    Given user is on page
    When user clicks submit
    Then page should update
```

### 3. Create Step Definitions
```java
package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;

public class MySteps extends browserSelector {
    @Given("user is on page")
    public void userIsOnPage() {
        MyPage.navigate(page);
    }
}
```

---

## 🛠️ Configuration Files

### configurations.properties
```properties
URL=https://your-app.com
BROWSER=chromium
HEADLESS=false
TIMEOUT=30000
```

### testng.xml
```xml
<suite name="Test Suite">
    <test name="Cucumber Tests">
        <classes>
            <class name="runner.testRunner"/>
        </classes>
    </test>
</suite>
```

---

## 🚨 Troubleshooting

### Tests Not Running
1. Check testng.xml path is correct
2. Ensure runner class exists
3. Verify @CucumberOptions annotation

### Page Not Found
- Check loadProps.getProperty("URL") is correct
- Verify configurations.properties exists

### Element Not Found
- Use MCP server to record correct locators
- Check element timing with waitForElement()
- Verify page is fully loaded

### Build Failures
- Run `mvn clean` first
- Check Java version compatibility
- Verify all dependencies in pom.xml

---

## 📊 Reports & Artifacts

### Report Locations
```
MRITestExecutionReports/
└── Version{YYYYMM}Build{NNN}/
    ├── extentReports/
    │   ├── html/          # HTML reports
    │   └── spark/         # Spark reports
    ├── screenshots/       # Test screenshots
    └── recordings/        # Video recordings
```

### View Reports
Open spark reports in browser:
```
MRITestExecutionReports/Version*/extentReports/spark/spark_*.html
```

---

## 📅 Last Updated
December 23, 2025

---

**📌 ONE FILE. EVERYTHING YOU NEED. Bookmark this!**
