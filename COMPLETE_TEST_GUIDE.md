# 🔧 Complete Test Generation Guide

## 📝 Purpose
**SINGLE SOURCE OF TRUTH** - Everything you need to generate, validate, and run tests successfully.

---

## � **IMPORTANT: Node.js Now Optional!**

### **Pure Java Implementation for Recording**
The recording feature (Option 1) now uses **Pure Java** - no Node.js installation needed!

**What Changed:**
- ✅ Recording uses unified TestGeneratorHelper (`TestGeneratorHelper.java`)
- ✅ Only Maven + Java JDK required for Options 1 & 3
- ✅ Node.js only needed for Option 2 (AI Interactive CLI with JIRA)
- ✅ Faster execution, simpler setup, better error messages

**Benefits:**
- **Setup Time:** Reduced from ~10min to ~2min
- **Dependencies:** Maven only (instead of Maven + Node.js)
- **Error Handling:** Clear Java stack traces
- **Maintenance:** Single language codebase

---

## �🎯 Quick Start Options

### 🚀 UNIFIED CLI (RECOMMENDED - All Options in One Place)
```bash
# Single entry point for all test generation methods
generate-test.bat
```
**Interactive menu with 3 options:**
1. 🎥 **Record & Auto-Generate** (Fastest - 5-10 min)
2. 🤖 **AI-Assisted Interactive** (Full-featured CLI with JIRA support)
3. ✅ **Validate & Run Tests** (Check and run existing tests)

**Why use Unified CLI?**
- ✅ Single command for everything
- ✅ Smart menu guides you to best option
- ✅ All features accessible: Recording, AI/JIRA, Interactive, Validation
- ✅ Consistent experience across all methods
- ✅ Auto-fixes common issues in all modes
- ✅ Built-in validation and testing

---

### 🎥 Option 1: Record & Auto-Generate (Inside CLI) - **NO Node.js Required!**
```bash
generate-test.bat  # Choose option 1 from menu
```
OR direct access:
```bash
generate-test.bat record
# OR
record-and-generate.bat
```
**Requirements:**
- ✅ Maven 3.6+
- ✅ Java JDK 17+
- ❌ Node.js NOT required (uses Pure Java generator)

**What it does:**
1. Opens Playwright Inspector to record your actions
2. Automatically extracts locators using Pure Java parser
3. Generates Page Object, Feature file, and Step Definitions (Java-based)
4. **Auto-validates & fixes common issues:**
   - ✅ Checks for duplicate step patterns
   - ✅ Auto-fixes protected methods to public
   - ✅ Auto-fixes BASE_URL() to getProperty("URL")
   - ✅ Multiple compilation attempts with guided fixes
   - ✅ Detects specific errors (DuplicateStepDefinitionException, NullPointerException)
5. Compiles and validates generated code (with retry logic)
6. **Runs tests automatically** via testng.xml
7. **Provides detailed error guidance** if issues occur
8. Ready to run immediately!

**Perfect for:**
- ⚡ Fastest test creation (5-10 minutes start to finish)
- 🎯 New test cases from scratch
- ✅ Accurate locators from real page interactions
- 🔄 Quick POC or demos
- 👥 Teams new to automation
- 📱 Testing dynamic web apps with complex locators
- 🛠️ Auto-fixing common code issues

**Limitations:**
- Only generates verification TODOs for business-specific logic
- All framework actions fully implemented
- Needs AI or manual for complex business validations

**Smart Generation Features:**
- ✅ Extracts ALL recorded actions (click, fill, select, check, press, navigate)
- ✅ Generates locator constant for EVERY element
- ✅ Creates dedicated method for EVERY action using utils.java
- ✅ Produces complete feature steps for ALL interactions
- ✅ Maps EVERY step to implemented page object method
- ✅ Only TODO for business-specific verification logic

**Auto-Fix Features:**
- ✅ Duplicate step pattern detection (prevents DuplicateStepDefinitionException)
- ✅ Protected method auto-fix (changes to public)
- ✅ BASE_URL() auto-fix (changes to getProperty("URL"))
- ✅ Retry compilation up to 3 times with guided fixes
- ✅ Retry test execution up to 3 times
- ✅ Specific error detection and guidance

### 🤖 Option 2: AI-Assisted (Inside CLI - RECOMMENDED for Enterprise) - **Requires Node.js**
```bash
generate-test.bat  # Choose option 2 from menu
```
OR direct access:
```bash
generate-test.bat cli
# OR
node automation-cli.js
```

**Requirements:**
- ✅ Maven 3.6+
- ✅ Java JDK 17+
- ✅ Node.js 18+ (for AI/JIRA features)
- ✅ MCP server setup: Run `setup-mcp.bat` once

**AI-Assisted menu options:**
1. 🎥 Record & Auto-Generate
2. 🎫 Generate from JIRA Story
3. ✨ AI-Guided Interactive (answer questions)
4. 📝 Update Existing Test
5. 🔧 Generate individual components (Page Object, Feature, Steps)
6. 🔍 Analyze Framework
7. 📖 Quick Start Tutorial

**Smart Generation Features:**
- ✅ Analyzes requirements completely
- ✅ Generates ALL identified actions (not just templates)
- ✅ Creates proper locator constants for ALL elements
- ✅ Implements ALL methods using utils.java
- ✅ Maps ALL scenarios to complete implementations
- ✅ Only TODO for business-specific verification logic

**Perfect for:**
- 📋 Tests with detailed JIRA requirements
- 🏢 Enterprise workflows with documentation
- ✅ Complete test coverage from acceptance criteria
- 📊 Traceability to requirements
- 🤝 Team collaboration with JIRA integration
- ✨ Interactive test design without JIRA

### ✅ Option 3: Validate & Run Tests - **NO Node.js Required!**
```bash
generate-test.bat  # Choose option 3 from menu
```
OR direct access:
```bash
generate-test.bat validate
```

**Requirements:**
- ✅ Maven 3.6+
- ✅ Java JDK 17+
- ❌ Node.js NOT required

**What it does:**
- ✅ Analyzes project structure
- ✅ Checks for common issues
- ✅ Auto-fixes protected methods and BASE_URL()
- ✅ Detects duplicate step patterns
- ✅ Compiles with retry (up to 3 attempts)
- ✅ Runs tests with retry (up to 3 attempts)
- ✅ Generates reports

**Perfect for:**
- 🔍 Checking existing tests
- 🛠️ Auto-fixing common issues
- 📊 Running test suite with reports
- ♻️ Retry logic for flaky builds

---

### 📝 Option 4: Manual Coding (For Advanced Users)
Create files following the structure below.

**Perfect for:**
- 🧩 Complex business logic
- 🔀 Custom validation scenarios
- 🎨 Full control over implementation
- 📚 Learning framework internals
- ⚙️ Advanced customization needs

---

## 💡 Quick Decision Guide

**Start with Unified CLI:**
```bash
generate-test.bat  # Shows menu with all options
```

**Choose Recording (Option 1) if:**
- Need tests fast (< 30 minutes)
- Have access to running application
- Creating simple user workflows
- Prototyping or POC

**Choose AI/JIRA (Option 2) if:**
- Have detailed JIRA stories
- Need complete test coverage
- Working in enterprise environment
- Want JIRA integration/traceability
- Want interactive guided generation

**Choose Validation (Option 3) if:**
- Already have tests
- Need to check/fix existing code
- Want to run test suite
- Need compilation retry logic

**Choose Manual (Option 4) if:**
- Need complex logic/validation
- Customizing framework behavior
- Learning or training
- Have specific technical requirements

---

## 🔄 Complete Workflows

### Workflow 1: Unified CLI → Choose Method → Auto-Validate
```
1. generate-test.bat
2. Choose option from menu:
   - Option 1: Recording → Perform actions → Auto-generate + validate
   - Option 2: AI/JIRA → Provide details → AI generates + validates
   - Option 3: Validation → Check existing → Auto-fix + compile + run
3. All options include:
   ✓ Duplicate step pattern check
   ✓ Auto-fix protected methods
   ✓ Auto-fix BASE_URL() usage
   ✓ Compile with retry (up to 3 attempts)
   ✓ Run tests with retry (up to 3 attempts)
4. Review reports and enhance if needed
```

### Workflow 2: Recording → Auto-Generate → AI Enhancement
```
1. generate-test.bat → Choose option 1
   OR record-and-generate.bat
2. Enter feature details (name, URL, JIRA)
3. Perform actions in browser → close when done
4. Auto-validation runs:
   ✓ Duplicate step pattern check
   ✓ Auto-fix protected methods
   ✓ Auto-fix BASE_URL() usage
   ✓ Compile with retry (up to 3 attempts)
   ✓ Run tests with retry (up to 3 attempts)
5. Files auto-generated with TODOs + validation passed
6. AI prompt: "Enhance recorded {Feature} test by implementing TODOs"
7. Re-run if needed: generate-test.bat validate
```

### Workflow 3: JIRA/AI → Validate → Run
```
1. generate-test.bat → Choose option 2
   OR node automation-cli.js
2. Select from AI menu:
   - Generate from JIRA Story (option 2)
   - AI-Guided Interactive (option 3)
3. AI analyzes → generates all files
4. Auto-validation runs
5. Fix errors if any (guided prompts)
6. Review reports
```

### Workflow 4: Validation Only
```
1. generate-test.bat → Choose option 3
   OR generate-test.bat validate
2. Auto-checks and fixes:
   - Project structure
   - Duplicate patterns
   - Protected methods
   - BASE_URL() usage
3. Compile (retry up to 3x)
4. Run tests (retry up to 3x)
5. Review reports
```

### Workflow 5: Manual Development
```
1. Create Page Object (extend BasePage)
2. Create Feature file (Gherkin syntax)
3. Create Step Definitions (extend browserSelector)
4. Run validation: generate-test.bat validate
5. Fix and iterate
```

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
- `JIRA_Integration` - Enable automated JIRA defect reporting (True/False)

**📋 JIRA Integration Behavior:**

```properties
JIRA_Integration=True  # Enables automated defect reporting AFTER test execution
```

**How JIRA Integration Works:**

1. ✅ Tests execute normally (login → run scenarios → assertions)
2. ✅ Tests pass/fail based on assertions
3. ✅ **AFTER test completes** → TestNG listener triggers
4. ✅ Listener checks if JIRA_Integration=True
5. ✅ If True → Adds pass/fail comment to JIRA issue
6. ✅ If test failed → Attaches screenshot to JIRA issue

**JIRA Integration does NOT:**

- ❌ Prevent test execution
- ❌ Skip login steps
- ❌ Interfere with test flow
- ❌ Create bugs during test execution

**JIRA Integration DOES:**

- ✅ Add comments to JIRA after test completes
- ✅ Attach screenshots for failed tests
- ✅ Track test results in JIRA automatically

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

**What causes DuplicateStepDefinitionException:**

1. ❌ Multiple @Given/@When/@Then annotations on same method
2. ❌ **SAME PATTERN TEXT in different @Given/@When/@Then** (e.g., `@Given("user logs in")` and `@When("user logs in")`)
3. ❌ Same pattern appearing multiple times across different step definition files

**RULE: Each pattern text can appear ONLY ONCE across ALL @Given/@When/@Then annotations in entire project**

**❌ WRONG - Multiple annotations on one method:**
```java
@Given("user logs in as {string}")
@When("user logs in as {string}")
public void userLogsIn(String userName) {
    // This creates TWO step definitions with same pattern!
}
```

**❌ WRONG - Same pattern in different methods:**

```java
@Given("user navigates to page")
public void userNavigatesToPage() {
    // First definition
}

@When("user navigates to page")
public void userNavigatesToPageAgain() {
    // Second definition - DUPLICATE PATTERN!
}
```

**✅ CORRECT - Different patterns or rename methods:**

```java
// Option 1: Different patterns
@Given("user is on the page")
public void userIsOnPage() {
    navigateToPage();
}

@When("user navigates to the page")
public void userNavigatesToPage() {
    navigateToPage();
}

// Option 2: Rename with Given/When suffix
@Given("user navigates to page")
public void userNavigatesToPageGiven() {
    performNavigation();
}

@When("user navigates to page")
public void userNavigatesToPageWhen() {
    performNavigation();
}

// Shared private helper
private void performNavigation() {
    // Actual implementation
}
```

**✅ Project Status (Last Verified: 2025-12-23):**

- ✅ **ZERO DUPLICATES** - All 105 step patterns are unique
- ✅ No pattern text appears in multiple @Given/@When/@Then annotations
- ✅ Breakdown:
   - ProfileSteps.java: 6 unique patterns
   - loginSteps.java: 32 unique patterns
   - ImpersonateAccessGroupSteps.java: 67 unique patterns
- ✅ Automated duplicate detection in generate-test.bat

**How to prevent duplicates:**

1. ✅ Each pattern text MUST be unique across ALL @Given/@When/@Then
2. ✅ NEVER use same pattern text with different annotation types:
   - ❌ WRONG: `@Given("user logs in")` and `@When("user logs in")`
   - ✅ RIGHT: `@Given("user is logged in")` and `@When("user logs in")`
3. ✅ If similar actions needed, use different wording OR suffix method names
4. ✅ Always create private helper methods for shared implementation logic
5. ✅ Run generate-test.bat - automatically detects ALL duplicate patterns before compilation {
   MyPage.navigate(page);
   }

```

**✅ BEST PRACTICE - Use private helper methods:**
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

**🔍 Auto-Detection:**
Duplicate checking is built into generate-test.bat (Step 3)

```bash
generate-test.bat validate  # Includes automatic duplicate detection
```

**Prevention Checklist:**

- [ ] Never use same pattern text in multiple annotations
- [ ] Always rename methods with Given/When/Then suffix if patterns are similar
- [ ] Use private helper methods for shared logic
- [ ] Run `generate-test.bat validate` before every commit
- [ ] Review feature files to avoid duplicate step text

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
# Validate & Run (Full workflow with duplicate check built-in)
generate-test.bat validate

# Generate new test with auto-validation
generate-test.bat

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
- [ ] Run: `generate-test.bat validate` (includes duplicate check)

### While Writing Code
- [ ] Use `clickOnElement()`, `enterText()` from utils.java
- [ ] Extend BasePage for page objects
- [ ] Extend browserSelector for step definitions
- [ ] Use `public static` methods
- [ ] Import configs.loadProps
- [ ] ONE Cucumber annotation per method
- [ ] UNIQUE pattern text for each @Given/@When/@Then
- [ ] Use Given/When/Then suffix if patterns are similar

### After Writing Code

- [ ] Run: `generate-test.bat validate` (auto-checks duplicates, compiles, runs)
- [ ] Fix any errors using error table above
- [ ] If failed, fix and repeat
- [ ] Check reports in MRITestExecutionReports/

---

## 🤖 AI Prompt Templates - Work Everywhere!

### 🌟 Universal AI Prompts (Use with GitHub Copilot, Claude, ChatGPT, etc.)

**These prompts work in:**
- ✅ GitHub Copilot Chat (VS Code, IntelliJ)
- ✅ Claude Desktop (via MCP server)
- ✅ ChatGPT / Claude Web
- ✅ Any AI assistant with workspace access

---

### 🎥 Recording-Based Enhancement (After recording in CLI)

```
Enhance the recorded test for {FeatureName} by:

1. Read generated files:
   - src/main/java/pages/{FeatureName}.java
   - src/test/java/features/{FeatureName}.feature
   - src/test/java/stepDefs/{FeatureName}Steps.java
   - temp_recording_*/recorded-actions.java

2. Verify ALL actions are implemented:
   - Check that EVERY recorded action has a page object method
   - Verify EVERY method uses utils.java (clickOnElement, enterText, etc.)
   - Ensure ALL feature steps are mapped to step definitions
   - Confirm ALL step definitions call page object methods

3. Implement ONLY remaining TODOs (verification logic):
   - Add business-specific assertions
   - Verify expected error messages
   - Check database state if needed
   - Validate business rules

4. Follow project patterns from COMPLETE_TEST_GUIDE.md:
   - Keep Page Objects extending BasePage
   - Keep Step Definitions extending browserSelector
   - ONE Cucumber annotation per method (no duplicates!)
   - Use loadProps.getProperty() for test data

5. Compile and verify:
   mvn clean compile test-compile

6. Run tests:
   mvn test -DsuiteXmlFile=src/test/testng.xml

CRITICAL: All framework actions (click, fill, select) should already be 
implemented. Only business verification logic should need implementation.
```

---

### 🎫 JIRA-Based Generation (Full test from scratch)

```
Generate complete test from JIRA story {ISSUE-KEY} following COMPLETE_TEST_GUIDE.md:

STRICT WORKFLOW:
1. ANALYZE FIRST:
   - Review src/main/java/pages/ for existing patterns
   - Review src/main/java/configs/utils.java for methods
   - Check src/test/resources/configurations.properties for test data
   - Review COMPLETE_TEST_GUIDE.md for patterns

2. CREATE ONLY THESE 3 FILES (ALL FULLY IMPLEMENTED):
   - src/main/java/pages/{Feature}.java
   - src/test/java/features/{Feature}.feature
   - src/test/java/stepDefs/{Feature}Steps.java

3. MANDATORY PATTERNS (COMPLETE IMPLEMENTATION):
   - Page objects: 
     * Locator constant for EVERY element
     * Method for EVERY action using utils.java
     * Method for EVERY verification
     * extend BasePage, import configs.loadProps, public static methods
   
   - Step definitions: 
     * Method for EVERY feature step
     * ALL methods call page object methods
     * ALL methods fully implemented (no action TODOs)
     * extend browserSelector (NOT base!), NO private Page page
   
   - Feature file:
     * Step for EVERY user interaction
     * Step for EVERY verification
     * Complete scenarios
   
   - Use loadProps.getProperty("URL"), loadProps.getProperty("Username")
   - ONE Cucumber annotation per method (NO @Given + @When on same method!)
   - Each pattern text UNIQUE across ALL @Given/@When/@Then

4. IMPLEMENTATION RULES:
   - NEVER leave framework actions as TODO (click, fill, select, etc.)
   - ALWAYS implement using utils.java: clickOnElement(), enterText(), selectDropDownValueByText()
   - ONLY TODO for business-specific verification logic
   - ALL actions = IMPLEMENTED
   - ALL mappings = IMPLEMENTED
   - ALL verifications = IMPLEMENTED with assertions

5. COMPILE & VALIDATE:
   - Run: mvn clean compile test-compile
   - Fix ALL errors before proceeding
   - Run: mvn test -DsuiteXmlFile=src/test/testng.xml
   - Fix until ALL tests pass

6. DO NOT:
   - Create bugs/issues
   - Create extra MD/BAT files
   - Modify testng.xml, hooks, runner, configs
   - Hardcode test data (use configurations.properties)
   - Skip compilation
   - Use multiple annotations on same method
   - Leave framework actions as TODO

CRITICAL: Generate COMPLETE implementations, not templates with TODOs!
```

---

### ✨ Interactive Generation (No JIRA, describe manually)

```
Create {Feature} test following COMPLETE_TEST_GUIDE.md:

FEATURE DETAILS:
- Feature Name: {Name}
- Page URL: {URL path}
- Elements: {List elements with actions}
- Scenarios: {List test scenarios}
- Test Data: {Required from configurations.properties}

FOLLOW WORKFLOW:
1. Analyze existing patterns (pages/, stepDefs/, utils.java)
2. Create 3 files (Page Object, Feature, Steps)
3. Use mandatory patterns (extend BasePage/browserSelector, utils methods)
4. Use loadProps.getProperty() for all test data
5. ONE annotation per method, unique patterns only
6. Compile: mvn clean compile test-compile
7. Run: mvn test -DsuiteXmlFile=src/test/testng.xml
8. Fix until tests pass

DO NOT create bugs or extra files.
```

---

### 🔄 Update Existing Test

```
Update {Feature} test to add:

NEW REQUIREMENTS:
- Additional scenarios: {list}
- New elements: {list}
- Additional validations: {list}

FOLLOW WORKFLOW:
1. Read existing files:
   - src/main/java/pages/{Feature}.java
   - src/test/java/features/{Feature}.feature
   - src/test/java/stepDefs/{Feature}Steps.java

2. Update files following existing patterns:
   - Match locator naming (ELEMENT_X)
   - Match method naming conventions
   - Use same utils.java methods
   - Keep same class structure

3. CHECK FOR DUPLICATES:
   - NO duplicate step patterns
   - Each @Given/@When/@Then pattern must be UNIQUE
   - Use different wording if similar actions

4. Compile and test:
   mvn clean compile test-compile
   mvn test -DsuiteXmlFile=src/test/testng.xml
```

---

### 🛠️ Fix Compilation Errors

```
Fix compilation errors in {Feature} test:

COMMON ISSUES TO CHECK:
1. Missing imports: Add "import configs.loadProps;"
2. Wrong access: Change "protected static" to "public static"
3. Wrong method: Use "getProperty("URL")" not "BASE_URL()"
4. Duplicate steps: Check for same pattern in multiple @Given/@When/@Then
5. Missing extensions: Ensure Page Objects extend BasePage
6. Missing extensions: Ensure Step Defs extend browserSelector

STEPS:
1. Read error messages
2. Apply fixes from COMPLETE_TEST_GUIDE.md error table
3. Recompile: mvn clean compile test-compile
4. Repeat until successful
```

---

### 🧪 Fix Test Failures

```
Fix test failures in {Feature} test:

ANALYZE:
1. Read test execution logs/reports
2. Identify failure type:
   - DuplicateStepDefinitionException → Check for duplicate patterns
   - NullPointerException → Check browserSelector extension
   - Element not found → Check locators
   - Assertion failed → Check expected vs actual

FIX:
1. Apply solution from COMPLETE_TEST_GUIDE.md
2. Recompile if code changes
3. Rerun: mvn test -DsuiteXmlFile=src/test/testng.xml
4. Repeat until pass
```

---

### 📊 Generate Test Report Summary

```
Analyze test reports and create summary:

1. Read reports from:
   MRITestExecutionReports/Version*/extentReports/

2. Summarize:
   - Total tests run
   - Passed/Failed/Skipped
   - Execution time
   - Failed test details with errors
   - Screenshot analysis if available

3. Provide recommendations for fixes
```

---

### 🎯 Complete AI Workflow Example

```
# 1. Start with CLI
generate-test.bat

# 2. Choose recording option
# Record actions in browser

# 3. After generation, enhance with AI
"Enhance recorded Login test by implementing TODOs,
following COMPLETE_TEST_GUIDE.md patterns"

# 4. If compilation errors occur
"Fix compilation errors in Login test following
COMPLETE_TEST_GUIDE.md error guide"

# 5. If test failures occur
"Fix test failures in Login test by analyzing
reports and updating code"

# 6. Final validation
generate-test.bat validate
```

---

### ⚠️ CRITICAL: Universal Instructions (Include in EVERY AI request)

**ALWAYS include these rules:**

```
Generate test from JIRA story {ISSUE-KEY} following these STRICT rules:

1. ANALYZE FIRST:
   - Review src/main/java/pages/ for existing patterns
   - Review src/main/java/configs/utils.java for common methods
   - Check configurations.properties for test data

2. CREATE ONLY THESE FILES (no extras!):
   - Page Object: src/main/java/pages/YourPage.java
   - Feature File: src/test/java/features/YourTest.feature
   - Step Definitions: src/test/java/stepDefs/YourSteps.java

3. MANDATORY PATTERNS:
   - Page objects: extend BasePage, import configs.loadProps, use public static
   - Step definitions: extend browserSelector (NOT base!), NO private Page page
   - Use utils.java methods: clickOnElement(), enterText(), etc.
   - Use loadProps.getProperty("URL"), loadProps.getProperty("Username")
   - ONE Cucumber annotation per method (NO duplicates!)

4. COMPILE & VALIDATE:
   - Run: mvn clean compile test-compile
   - Fix ALL errors before proceeding
   - Run: mvn test -DsuiteXmlFile=src/test/testng.xml
   - Fix until ALL tests pass

5. DO NOT:
   - Create bugs/issues
   - Create extra MD/BAT files
   - Modify testng.xml, hooks, runner, or configs folder
   - Hardcode test data (use configurations.properties)
   - Skip compilation step
   - Use multiple @Given/@When/@Then on same method
```

### JIRA-Based (Copy This Exact Template)
```
Generate test from JIRA story {ISSUE-KEY} with:
- All test parameters and scenarios
- Proper locators using MCP server
- Minimal logging
- Use loadProps.getProperty() for all test data from configurations.properties
- Follow folder structure: pages/, features/, stepDefs/
- Compile and run testng.xml after creation
- DO NOT create bugs or issues
```

### Manual Generation
```
Using MCP server, create {feature} test with:
Elements: {list elements}
Scenarios: {list scenarios}
Verification: functional + UI + performance
Test Data: Use configurations.properties (loadProps.getProperty())
Follow: COMPLETE_TEST_GUIDE.md workflow
Compile: mvn clean compile test-compile
Run: mvn test -DsuiteXmlFile=src/test/testng.xml
DO NOT create bugs or issues
```

### Update Existing
```
Update {feature} test to add:
- New scenarios: {list}
- Additional validation for {aspects}
- Use existing patterns from COMPLETE_TEST_GUIDE.md
- Compile and test after changes
- DO NOT create bugs or issues
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

### ❌ Login Steps Not Performed / JIRA Bugs Created Instead

Tests Not Running / Steps Not Followed

**Symptoms:**

- Tests skip steps or run in wrong order
- Random bugs being created instead of tests
- Workflow not being followed

**Root Causes & Fixes:**

1. **AI Not Following Instructions**
   - ✅ Use the EXACT prompt template from "AI Prompt Templates" section above
   - ✅ Always include: "Follow COMPLETE_TEST_GUIDE.md workflow"
   - ✅ Always include: "DO NOT create bugs or issues during test generation"
   - ✅ Include all mandatory rules in every request

2. **Missing Workflow Steps**
   - ❌ Problem: Skipping ANALYZE step
   - ✅ Fix: Always start with "ANALYZE project structure first"

3. **Wrong File Locations**
   - ❌ Problem: Files created in wrong folders
   - ✅ Fix: Explicitly state folder paths in prompt
   - ✅ Example: "Create page object in src/main/java/pages/"

4. **Not Compiling Before Running**
   - ❌ Problem: Running tests without compilation
   - ✅ Fix: Add to prompt: "Compile: mvn clean compile test-compile"
   - ✅ Add to prompt: "Then run: mvn test -DsuiteXmlFile=src/test/testng.xml"

5. **Creating Extra Files**
   - ❌ Problem: AI creates bugs, extra docs, or unnecessary files
   - ✅ Fix: Explicitly state: "Create ONLY: PageObject.java, Feature.feature, Steps.java"
   - ✅ Add: "DO NOT create bugs, issues, or extra documentation during generation"

**Note:** JIRA_Integration=True is OK! It only adds comments to JIRA AFTER tests complete. It does NOT interfere with
test execution.

**Recommended Full Prompt:**

```
Generate test from JIRA story ECS-XXX following COMPLETE_TEST_GUIDE.md:

STRICT WORKFLOW:
1. ANALYZE: Review pages/, stepDefs/, utils.java, configurations.properties
2. CREATE ONLY:
   - src/main/java/pages/YourPage.java (extend BasePage, import loadProps)
   - src/test/java/features/YourTest.feature
   - src/test/java/stepDefs/YourSteps.java (extend browserSelector)
3. USE: utils.java methods, loadProps.getProperty() for test data
4. COMPILE: mvn clean compile test-compile (fix all errors)
5. RUN: mvn test -DsuiteXmlFile=src/test/testng.xml
6. REPEAT: Until all tests pass

DO NOT:
- Create bugs or issues during test generation
- Create extra MD/BAT files
- Modify testng.xml, hooks, runner
- Skip compilation
- Hardcode test data

Note: JIRA_Integration=True adds comments AFTER test execution completes

---

## 🎥 Test Recording & Auto-Generation

### Using record-and-generate.bat

**What it does:**
1. Opens Playwright Inspector browser
2. Records all your actions (clicks, typing, navigation)
3. Extracts locators automatically
4. Generates Page Object, Feature file, and Step Definitions
5. **Auto-validates & fixes common issues**
6. **Compiles with retry logic (up to 3 attempts)**
7. **Runs tests automatically (up to 3 attempts)**
8. Provides detailed error guidance
9. Ready to run immediately!

**Auto-Validation Features:**
- ✅ **Duplicate Step Pattern Detection**: Prevents DuplicateStepDefinitionException
- ✅ **Auto-Fix Protected Methods**: Changes `protected static` to `public static`
- ✅ **Auto-Fix BASE_URL()**: Changes to `getProperty("URL")`
- ✅ **Compilation Retry**: Up to 3 attempts with guided fixes
- ✅ **Test Execution Retry**: Up to 3 attempts with error detection
- ✅ **Specific Error Guidance**: Detects and provides fixes for common errors

**Usage:**
```bash
record-and-generate.bat
```

**Interactive Prompts:**
```
Feature Name: Login
Page URL: /login
JIRA Story: ECS-123
```

**Recording Process:**
1. Browser opens with Playwright Inspector
2. Perform your test actions:
   - Navigate to pages
   - Click buttons
   - Fill form fields
   - Select dropdowns
3. Inspector records everything automatically
4. Close browser when done
5. **Auto-validation runs immediately**:
   - Checks for duplicate step patterns
   - Auto-fixes protected methods
   - Auto-fixes BASE_URL() usage
   - Compiles code (with retry)
   - Runs tests (with retry)

**Output Files:**
- `src/main/java/pages/{FeatureName}.java` - Page Object with recorded locators
- `src/test/java/features/{FeatureName}.feature` - Feature file with scenarios
- `src/test/java/stepDefs/{FeatureName}Steps.java` - Step definitions (TODOs to implement)

**Error Handling:**
If compilation fails:
- Script will auto-retry up to 3 times
- Common issues are auto-fixed (protected methods, BASE_URL)
- Provides specific guidance for manual fixes
- Prompts to retry after manual fixes

If tests fail:
- Script will auto-retry up to 3 times
- Detects specific errors (DuplicateStepDefinitionException, NullPointerException)
- Provides targeted fix guidance
- Prompts to retry after fixes

**Enhancing Recorded Tests:**

After recording, use AI to implement TODO sections:

```
Enhance the recorded {FeatureName} test:

1. Read generated files and temp_recording_*/recorded-actions.java
2. Implement all TODO sections in:
   - Page Object action methods
   - Step definitions
   - Cucumber scenarios
3. Add proper assertions and verifications
4. Follow project patterns (utils.java, loadProps, BasePage)
5. Compile and test: mvn clean compile test-compile
```

**Benefits:**
- ⚡ Fastest test creation (minutes vs hours)
- ✅ Accurate locators from real page
- 🎯 No JIRA required for quick tests
- 🔄 Easy to re-record if page changes
- 🤖 AI enhances with proper patterns
- 🛠️ **Auto-fixes common code issues**
- ♻️ **Retry logic for compilation and tests**
- 📋 **Detailed error guidance**

**Tips:**
- Record one complete user workflow per session
- Keep recordings focused (one feature at a time)
- Review generated locators - MCP server can optimize them
- Use AI to add assertions and cleanup code
- If validation fails, follow the guided fix suggestions
- Check auto-fix results in generated files

---

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

## 🌟 AI-Powered Test Generation Everywhere!

### 🎯 One CLI, Multiple AI Options

**Unified Entry Point:**
```bash
generate-test.bat  # Shows menu with all options
```

**AI Works in ALL These Places:**

1. **🎥 Recording + AI Enhancement**
   - Record in CLI → AI implements TODOs
   - Use Copilot/Claude/ChatGPT to enhance

2. **🎫 JIRA + AI Generation**
   - CLI fetches JIRA → AI generates test
   - Or use AI chat with JIRA story

3. **✨ Interactive + AI Guidance**
   - Answer CLI questions → AI generates
   - Or describe to AI → AI generates

4. **🔍 Validation + AI Fixes**
   - CLI validates → AI fixes errors
   - Or ask AI to fix specific issues

### 🤖 AI Assistants Supported

- ✅ **GitHub Copilot** (VS Code, IntelliJ)
- ✅ **Claude Desktop** (via MCP server)
- ✅ **ChatGPT / Claude Web** (copy/paste prompts)
- ✅ **Any AI with file access**

### 📋 Universal AI Workflow

```
1. Start:    generate-test.bat
2. Choose:   Recording / JIRA / Interactive / Validation
3. Generate: Files created with patterns
4. Enhance:  Use AI prompt templates above
5. Validate: Automatic in CLI or manual
6. Fix:      AI-guided error resolution
7. Run:      Automatic or manual execution
```

### 💡 Pro Tips

- **Use CLI first** → Gets structure right
- **Use AI second** → Enhances implementation
- **Always validate** → Catches issues early
- **Iterate with AI** → Fix until tests pass
- **Reference guide** → Include COMPLETE_TEST_GUIDE.md in prompts

---

## 📅 Last Updated
December 23, 2025

---

**📌 ONE FILE. EVERYTHING YOU NEED. Bookmark this!**
