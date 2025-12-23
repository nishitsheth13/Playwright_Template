# 🚀 Quick Reference - Create New Test

> **3 Easy Options to Generate Tests**

---

## Option 1: Interactive CLI (Recommended for Beginners)

```bash
node automation-cli.js
```

### Steps:
1. Select: **"Generate Complete Test Suite"**
2. Enter test name: `LoginTest`
3. Add elements:
   ```
   Element 1: Username field (type)
   Element 2: Password field (type)
   Element 3: Login button (click)
   ```
4. Add scenarios:
   ```
   Scenario 1: Valid login
     Steps: Given/When/Then...
   Scenario 2: Invalid credentials
     Steps: Given/When/Then...
   ```
5. **Enable verification:**
   ```
   Functional verification? y
   UI verification? y
   UX verification? y
   Performance verification? y
   Performance threshold: 3 seconds
   Logging? y
   ```
6. Confirm → Code generated!
7. Compile: `mvn clean compile`
8. Run: `mvn test`

---

## Option 2: AI Chat (GitHub Copilot or Claude)

### Template:
```
Using MCP server, create [FEATURE_NAME] test:

Elements:
- [element name] (click/type/select) - xpath: [locator]
- [element name] (click/type/select) - xpath: [locator]

Verification:
- Functional: Assert [what to verify]
- UI: Verify [element states]
- UX: Check [transitions]
- Performance: < [X] seconds
- Logging: Yes

Scenarios:
1. [Scenario name] - [description]
2. [Scenario name] - [description]
```

### Example:
```
Using MCP server, create User Login test:

Elements:
- Username field (type) - xpath: //input[@id='username']
- Password field (type) - xpath: //input[@id='password']
- Login button (click) - xpath: //button[@id='login']
- Dashboard title (verify) - xpath: //h1[@class='dashboard']

Verification:
- Functional: Assert login success, verify URL contains '/dashboard'
- UI: Verify button enabled/disabled states, error messages
- UX: Check smooth page transition, no broken elements
- Performance: < 3 seconds
- Logging: Yes

Scenarios:
1. Valid login - User enters correct credentials and reaches dashboard
2. Invalid credentials - Error message appears, stays on login page
3. Empty fields - Validation messages appear for required fields
```

Then:
```bash
mvn clean compile test
```

---

## Option 3: Manual Coding

See [AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md) for manual coding workflow.

---

## 📦 What Gets Generated

For each test, AI creates:

### 1. Page Object (`src/main/java/pages/YourPage.java`)
```java
public class YourPage extends BasePage {
    private static final Logger log = Logger.getLogger(YourPage.class.getName());
    
    protected static void yourMethod(String locator) {
        log.info("Action starting...");
        clickOnElement(locator);  // Uses YOUR existing methods!
        log.info("✓ Action completed");
    }
    
    protected static void verifyPageLoaded(String expectedUrl) {
        Assert.assertTrue(isUrlContains(expectedUrl));
    }
}
```

### 2. Feature File (`src/test/java/features/your.feature`)
```gherkin
@Functional @Smoke @PerformanceTest
Scenario: Your scenario
  Given User is on page
  When User performs action
  Then Result should be verified
  And Performance should be acceptable
```

### 3. Step Definitions (`src/test/java/stepDefs/yourSteps.java`)
```java
public class YourSteps {
    @Given("User is on page")
    public void userIsOnPage() {
        log.info("📋 Step starting...");
        yourPage.navigateTo("/page");
        Assert.assertTrue(yourPage.isPageLoaded());
        log.info("✓ Step completed");
    }
}
```

---

## 🎯 Verification Options

When generating tests, select what to verify:

✅ **Functional** - Business logic, assertions, data validation  
✅ **UI** - Element visibility, button states, error messages  
✅ **UX** - Page transitions, smooth experience, accessibility  
✅ **Performance** - Response times, load times, thresholds  
✅ **Logging** - Detailed step logs, verification results, timings  

---

## ⚡ Performance Thresholds

| Action | Threshold |
|--------|-----------|
| Page Load | < 3s |
| Login | < 3s |
| Form Submit | < 2s |
| Search | < 2s |
| API Call | < 1s |
| Click | < 1s |

---

## 📋 Common Tags

Use in feature files:
```gherkin
@Smoke              # Critical tests
@Functional         # Business logic
@Regression         # Full suite
@PerformanceTest    # Performance validation
@UITest             # UI verification
@NegativeTest       # Error scenarios
@Priority=0         # High priority
```

Run with tags:
```bash
mvn test -Dcucumber.filter.tags="@Smoke"
mvn test -Dcucumber.filter.tags="@Smoke and @Functional"
```

---

## 🔧 Commands

```bash
# Setup (one-time)
setup-mcp.bat          # Windows
./setup-mcp.sh         # Mac/Linux

# Generate
node automation-cli.js

# Build
mvn clean compile

# Test
mvn test
mvn test -Dcucumber.filter.tags="@Smoke"

# Reports
# Open: MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/*.html
```

---

## 📚 Full Documentation

- **[AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md)** - Complete AI guide
- **[AUTOMATION_FRAMEWORK_GUIDE.md](AUTOMATION_FRAMEWORK_GUIDE.md)** - Framework reference
- **[README.md](README.md)** - Project overview

---

## ✅ Quick Checklist

Before creating test:
- [ ] Setup completed (`setup-mcp.bat`)
- [ ] Node.js installed (check: `node --version`)
- [ ] Maven working (check: `mvn --version`)

After generating test:
- [ ] Code reviewed
- [ ] Compiled successfully (`mvn clean compile`)
- [ ] Tests run (`mvn test`)
- [ ] Reports checked

---

**Ready? Run `node automation-cli.js` now! 🚀**

*For detailed information, see [AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md)*
