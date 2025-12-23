# Playwright Java Automation Framework

> **MRI Energy Test Automation**  
> Production-ready BDD framework with Playwright + Cucumber + TestNG + ExtentReports

---

## ⚡ AI-Powered Test Generation! 🤖

**Create complete test suites in 5 minutes with comprehensive verification**

### Quick Start
```bash
# 1. Navigate to project root
cd c:\Users\nishit.sheth\IdeaProjects\Playwright_Template

# 2. Setup (one-time)
setup-mcp.bat          # Windows
./setup-mcp.sh         # Mac/Linux

# 3. Generate test (EASY - just double-click!)
generate-test.bat      # Windows - Includes auto-validation!
./generate-test.sh     # Mac/Linux

# OR use command line:
node automation-cli.js

# 4. Build & Run (auto-done by generate-test.bat)
mvn clean compile test

# 📖 Complete Guide: COMPLETE_TEST_GUIDE.md
```

### Or use AI Chat
```
"Using MCP server, create login test with username, password fields and login button.
Add verification: Functional, UI, Performance (<3s), Logging.
Test valid and invalid scenarios."
```

**📘 Complete Guide:** [AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md) ← **Everything in ONE file!**

**🔧 Best Practices:** [TEST_GENERATION_BEST_PRACTICES.md](TEST_GENERATION_BEST_PRACTICES.md) ← **⚠️ MUST READ before generating tests!**

---

## 🚀 Traditional Approach

```bash
# Compile
mvn clean compile test-compile

# Run tests
mvn test

# Run with tag
mvn test -Dcucumber.filter.tags="@Smoke"
```

**📘 [Complete Framework Guide](AUTOMATION_FRAMEWORK_GUIDE.md)** ← Read this for full documentation

---

## 📦 What's Included

### 🤖 AI-Powered Features
✅ **MCP Server** - Intelligent code generation  
✅ **Interactive CLI** - Wizard for test creation  
✅ **Verification Options** - Functional, UI, UX, Performance, Logging  
✅ **GitHub Copilot Integration** - AI chat support  
✅ **Claude Desktop Support** - Alternative AI integration  
✅ **Auto-generation** - Page Objects, Features, Step Definitions

### 🔧 Framework Features
✅ **Playwright** - Modern browser automation  
✅ **Cucumber** - BDD with Gherkin syntax  
✅ **TestNG** - Powerful test execution  
✅ **Page Object Model** - Maintainable code structure  
✅ **ExtentReports** - Beautiful HTML reports with screenshots  
✅ **Auto-retry** - Retry failed tests automatically  
✅ **Centralized Config** - Easy configuration management  

---

## 📚 Documentation

- **[AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md)** - Complete AI guide with 19 auto-fix patterns
- **[PROMPT_TEMPLATES.md](PROMPT_TEMPLATES.md)** - Quick-start templates (Copy-paste ready)
- **[AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md)** - Framework reference (Structure, configuration, manual coding)

---

## 🎯 Create Your First Test

### Option 1: Interactive CLI (Easiest)
```bash
node automation-cli.js
```

### Option 2: AI Chat
```
Using MCP server, create [feature] test with [elements].
Add verification: Functional, UI, Performance (<3s), Logging.
Test [scenarios].
```

### Option 3: Manual Coding
See [AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md)

---
✅ **AI Integration** - Works with GitHub Copilot, Claude  
✅ **5 MCP Tools** - Generate page objects, features, steps  
✅ **90% Time Savings** - 5 minutes vs 2 hours per test suite  

### 🔧 Core Framework
✅ **BDD with Cucumber** - Gherkin feature files  
✅ **Page Object Model** - Maintainable architecture  
✅ **Automatic Retry** - Handles flaky tests  
✅ **Centralized Timeouts** - No hardcoded waits  
✅ **Rich HTML Reports** - ExtentReports with screenshots  
✅ **JIRA Integration** - Auto bug creation  
✅ **Multi-browser** - Chrome, Firefox, Edge, WebKit

---

## 📂 Project Structure

```
Playwright_Template/
├── 🤖 AI Tools
│   ├── automation-cli.js              # Main CLI (all features in ONE file)
│   ├── AI_AUTOMATION_COMPLETE_GUIDE.md  # Complete guide (all-in-one)
│   ├── setup-mcp.bat/.sh              # One-time setup
│   ├── generate-test.bat/.sh          # Quick launcher
│   └── mcp-server/                    # MCP server
│
├── 📚 Documentation (minimal)
│   ├── QUICK_START.md                 # Quick reference
│   └── AUTOMATION_FRAMEWORK_GUIDE.md  # Framework methods
│
├── src/main/java/
│   ├── configs/        # Framework utilities
│   └── pages/          # Page objects (POM)
├── src/test/java/
│   ├── features/       # Cucumber feature files
│   ├── stepDefs/       # Step definitions
│   ├── hooks/          # Test setup/teardown
│   ├── listener/       # TestNG listeners
│   └── runner/         # Test runner
├── src/test/resources/
│   └── configurations.properties  # Main config
└── pom.xml             # Maven dependencies

⚠️ POLICY: Keep structure minimal (8 support files max)
   Add features to existing files, not new files.
```

---

## ⚙️ Key Configuration

**File:** `src/test/resources/configurations.properties`

```properties
# Application
URL=https://your-app-url.com
Username=admin
Password=your-password

# Browser
Browser=chrome              # chrome | firefox | edge | webkit
Headless_Mode=false

# Retry & Timeout
MaxRetryCount=2             # Retry failed tests
DefaultTimeout=30000        # 30 seconds
PageLoadTimeout=60000       # 60 seconds

# Reporting
Screenshots_Mode=true
Recording_Mode=true
JIRA_Integration=True
```

---

## 🏃 Running Tests

```bash
# All tests
mvn test

# Specific tags
mvn test -Dcucumber.filter.tags="@Smoke"
mvn test -Dcucumber.filter.tags="@Functional"
mvn test -Dcucumber.filter.tags="@Priority=0"

# Multiple tags
mvn test -Dcucumber.filter.tags="@Smoke and @Functional"

# TestNG XML
mvn test -DsuiteXmlFile=src/test/testng.xml

# Headless mode
mvn test -DHeadless_Mode=true

# Different browser
mvn test -DBrowser=firefox
```

---

## 📊 Test Reports

Reports are auto-generated in:
```
MRITestExecutionReports/{Version_Date}/extentReports/
├── testNGExtentReports/spark/spark_{timestamp}.html
└── cucumberExtentReports/index.html
```

**Features:**
- ✅ Rich HTML reports with charts
- ✅ Screenshots for failed tests
- ✅ Retry attempts logged
- ✅ Test duration tracking
- ✅ Environment details

---

## 🎯 Creating New Tests

### 1. Create Feature File
`src/test/java/features/yourfeature.feature`
```gherkin
@Smoke
Feature: Your Feature

  Scenario: Your test scenario
    Given User navigates to page
    When User performs action
    Then User verifies result
```

### 2. Create Page Object
`src/main/java/pages/YourPage.java`
```java
public class YourPage extends BasePage {
    private static final String BTN_SUBMIT = "//button[@id='submit']";
    
    public static void clickSubmit() {
        clickOnElement(BTN_SUBMIT);
    }
}
```

### 3. Create Step Definitions
`src/test/java/stepDefs/YourSteps.java`
```java
public class YourSteps extends browserSelector {
    @When("User performs action")
    public void userPerformsAction() {
        YourPage.clickSubmit();
    }
}
```

### 4. Compile & Run
```bash
mvn clean compile test-compile
mvn test -Dcucumber.filter.tags="@Smoke"
```

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 22 |
| Browser Automation | Playwright | 1.45.0 |
| BDD Framework | Cucumber | 7.18.1 |
| Test Runner | TestNG | 7.11.0 |
| Build Tool | Maven | 3.x |
| Reporting | ExtentReports | 5.1.0 |

---

## ✅ Best Practices

### Page Objects
- ✅ Extend `BasePage`
- ✅ Use locator prefixes (`TXT_`, `BTN_`, `LBL_`)
- ✅ Use `TimeoutConfig` for waits
- ✅ Add emoji logs (🌐 📝 ✅ ❌)
- ❌ No assertions in page objects

### Step Definitions
- ✅ Keep thin (call page methods)
- ✅ Put assertions here
- ❌ No business logic

### Feature Files
- ✅ Clear, business-readable
- ✅ Add appropriate tags
- ✅ Use Background for common steps

---

## 🔧 Troubleshooting

### Tests fail due to timing
```properties
# Increase timeouts in configurations.properties
DefaultTimeout=40000
PageLoadTimeout=80000
```

### Flaky tests
```properties
# Increase retry count
MaxRetryCount=3
```

### Compilation errors
```bash
mvn clean compile test-compile
```

### Reports not generating
1. Check `Screenshots_Mode=true`
2. Verify listener in `testng.xml`
3. Check write permissions

---

## 📚 Documentation

- **[Complete Framework Guide](AUTOMATION_FRAMEWORK_GUIDE.md)** - Full documentation
- **[POM Best Practices](AUTOMATION_FRAMEWORK_GUIDE.md#-page-object-model-pom)** - Page object patterns
- **[Retry & Timeout](AUTOMATION_FRAMEWORK_GUIDE.md#-retry--timeout-management)** - Configuration guide
- **[Test Creation](AUTOMATION_FRAMEWORK_GUIDE.md#-test-creation-workflow)** - Step-by-step workflow

---

## 🎓 Quick Reference

### Essential Commands
```bash
mvn clean                                    # Clean project
mvn compile test-compile                     # Compile
mvn test                                     # Run all tests
mvn test -Dcucumber.filter.tags="@Smoke"    # Run with tag
```

### Common Methods
```java
// Navigation
navigateTo(url);

// Actions
clickOnElement(locator);
clearAndEnterText(locator, text);

// Validations
isElementPresent(locator);
getElementText(locator);

// Waits
TimeoutConfig.shortWait();
TimeoutConfig.mediumWait();
```

---

## 📞 Support

For detailed information, refer to:
- **[AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md)** - Complete guide
- **Configuration:** `src/test/resources/configurations.properties`
- **TestNG Suite:** `src/test/testng.xml`

---

**Framework Version:** 3.0  
**Status:** Production Ready ✅  
**Last Updated:** December 20, 2025

---

Made with ❤️ by Automation Team


```bash
mvn test -Pchromium
mvn test -Pfirefox
mvn test -Pwebkit
```

### Other Ways to Run the Tests:

1. Run the `testng.xml` file for single and cross-browser testing using TestNG.
2. Run Any Method from @Test annotation.
3. Execute tests using the TestRunner class.

---

## Creating New Test Scripts

Want to add new test scenarios? We've got you covered! 🎯

### 🎯 Option 1: Automated Generator (Recommended - No Coding!)

**Generate complete test scripts by just answering prompts!**

```bash
# Windows
generate_test_script.bat

# Mac/Linux
./generate_test_script.sh

# Or directly
python generate_test_script.py
```

The generator will ask simple questions and create:
- ✅ Feature file with scenarios
- ✅ Page Object with locators
- ✅ Step Definitions with implementations

**Time:** 5-10 minutes | **Coding Required:** None!

📖 **See [GENERATOR_GUIDE.md](GENERATOR_GUIDE.md)** for detailed walkthrough

---

### 📝 Option 2: Manual Creation

The framework provides a comprehensive guide for creating new test scripts manually.

📖 **See [SCRIPT_CREATION_GUIDE.md](SCRIPT_CREATION_GUIDE.md)** for:
- Step-by-step instructions with templates
- Feature file, Page Object, and Step Definition examples
- Best practices and coding guidelines
- Quick reference for common methods
- Real-world examples

### Framework Execution Flow
```
TestNG XML → Test Runner → Cucumber Features → Hooks → Step Definitions → Page Objects → Browser/Utils
                                                  ↓
                                            Listener (Reports & JIRA)
```

### Quick Template

**1. Create Feature File** (`src/test/java/features/yourFeature.feature`)
```gherkin
Feature: Your Feature Name

  @Priority=0
  Scenario: Your Scenario
    Given Precondition
    When Action
    Then Expected Result
```

**2. Create Page Object** (`src/main/java/pages/yourPage.java`)
```java
public class yourPage extends utils {
    public static final String ELEMENT = "xpath=//your/locator";
    
    public static void performAction() {
        clickOnElement(ELEMENT);
    }
}
```

**3. Create Step Definitions** (`src/test/java/stepDefs/yourSteps.java`)
```java
public class yourSteps extends browserSelector {
    @Given("Precondition")
    public void precondition() {
        yourPage.performAction();
    }
}
```

That's it! Your test will automatically integrate with the reporting and JIRA systems.

---

## Recent Improvements

This framework has been enhanced with production-grade improvements while maintaining the original structure:

✅ **Better Error Handling** - Comprehensive try-catch blocks with meaningful error messages  
✅ **Enhanced Logging** - Visual indicators (✅ ❌ ⚠️) for quick debugging  
✅ **Resource Management** - Automatic cleanup with try-with-resources  
✅ **Constants Management** - Centralized constants in `Constants.java`  
✅ **JavaDoc Documentation** - Complete API documentation for all methods  
✅ **Code Templates** - Ready-to-use templates for rapid script creation  
✅ **Improved JIRA Integration** - Better authentication and error handling  
✅ **Null Safety** - Validation checks prevent NullPointerException  

📖 **See [IMPROVEMENTS_SUMMARY.md](IMPROVEMENTS_SUMMARY.md)** for detailed changes and before/after comparisons.

---

## Generating Reports

After running the tests, reports will be automatically generated in the **/Reports/** folder. You can find:

- **HTML Report**
- **JSON Report**
- **Extent Spark Reports**

To view the Extent Report, open the `ExtentReport.html` file located at `/Reports/ExtentReport.html` in a web browser.

---

## Test Data

- [Describe how test data is managed if applicable]
- [Include any sample test data files or templates if necessary]

---

## POM File

The **POM.xml** file in the root directory includes all the necessary dependencies for Maven to manage the project.

### Important Maven Plugins:

- **maven-cucumber-reporting**
- **maven-compiler-plugin**
- **maven-surefire-plugin**

### Important Maven Dependencies:

- **webdrivermanager**
- **junit**
- **cucumber-java**
- **playwright**
- **extentreports**
- **extentreports-cucumber7-adapter**

---

## Contact

- **QA Engineer**: Nishit Sheth

