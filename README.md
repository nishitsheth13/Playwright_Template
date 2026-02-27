# Playwright Test Automation Framework

## Overview

A comprehensive Java-based test automation framework using Playwright, Cucumber BDD, and TestNG for end-to-end testing of the MRI Energy application.

## 🤖 AI Prompt Templates & Enterprise Framework Automation

**🎯 NEW: PROMPT-103 - Upgrade ANY Existing Framework to Enterprise Level!**

We've created comprehensive AI prompt libraries to help you:

- ✅ **Analyze existing frameworks** and identify gaps
- ✅ **UPGRADE existing framework** to enterprise-grade (PROMPT-103) ⭐ NEW!
- ✅ **Setup NEW frameworks** (Playwright, Selenium, Cypress, RestAssured)
- ✅ **Generate tests** from recordings, JIRA stories, or AI assistance
- ✅ **Add NPM CLI automation** with recording, retry, AI generation
- ✅ **Migrate frameworks** (Selenium → Playwright, etc.)

**📚 Documentation Files:**

1. **[docs/AI_PROMPT_TEMPLATES.md](docs/AI_PROMPT_TEMPLATES.md)** - 21 Enterprise AI Prompts ⭐
   - **PROMPT-103: Upgrade Existing Framework** (NEW!)
     - Analyzes your current project
     - Adds CLI automation menu (recording, retry, JIRA, AI)
     - Adds smart locators, parallel execution, CI/CD
     - 100% backward compatible
     - Working in 2 hours!

   - PROMPT-101/102: Framework assessment
   - PROMPT-201-204: New framework setup
   - PROMPT-301-303: Test generation (recording/JIRA/AI)
   - PROMPT-401-402: Framework migration
   - PROMPT-501-503: Advanced features
   - PROMPT-601-602: Performance optimization
   - PROMPT-701-702: CI/CD & Docker
   - PROMPT-801-802: Troubleshooting

2. **[docs/STEP_DEFINITION_BEST_PRACTICES.md](docs/STEP_DEFINITION_BEST_PRACTICES.md)** - Code Quality Standards ⭐ NEW!
   - **Permanent fixes for common issues**
   - **NO** `:has-text()` pseudo-selectors (Java incompatible)
   - **Proper** string escaping in selectors
   - **Smart** locator priority (ID first strategy)
   - **Prevent** duplicate step definitions
   - **Handling multiple elements** with same name
   - **Code generation checklist**
   - **Quality enforcement rules**

3. **[docs/SMART_ELEMENT_HANDLER_GUIDE.md](docs/SMART_ELEMENT_HANDLER_GUIDE.md)** - Multiple Elements Guide ⭐ NEW!
   - **Problem:** Multiple "Add", "Save", "Delete" buttons
   - **Solution:** SmartElementHandler utility class
   - **9 strategies** for handling multiple elements
   - **Real-world examples** (User Management, E-commerce, Forms)
   - **Decision tree** for choosing right strategy
   - **Quick reference** with code samples

4. **[docs/MIGRATION_GUIDE.md](docs/MIGRATION_GUIDE.md)** - NPM Migration Guide
   - How to use NPM-based CLI
   - Cross-platform automation
   - Command reference

5. **[docs/AI_DOCUMENTATION_INDEX.md](docs/AI_DOCUMENTATION_INDEX.md)** - Navigation Helper
   - Quick start guide
   - File structure overview

**💡 Example Use Cases:**

**Scenario 1: Upgrade Your Existing Framework**

```
You have: Basic Playwright framework without CLI automation
You want: Enterprise features (recording, retry, AI generation, smart locators)

→ Open docs/AI_PROMPT_TEMPLATES.md
→ Use PROMPT-103: Upgrade Existing Framework
→ Paste your current code
→ Get complete upgrade in 2 hours
→ Result: CLI menu + recording + retry + AI + all advanced features
```

**Scenario 2: Create New Framework**

```
You have: Nothing
You want: Complete Playwright Java framework

→ Use PROMPT-201: Enterprise Playwright Java Framework
→ Get complete framework in 30 minutes
→ Includes CLI automation from day 1
```

**Scenario 3: Generate Tests from Recording**

```
You have: Playwright recording of user actions
You want: Complete test suite (Page Object + Feature + Steps)

→ Run: playwright codegen https://yoursite.com
→ Copy recording
→ Use PROMPT-301: Recording to Test Suite Converter
→ Get 3 production-ready files in 5 minutes
```

**🌍 Works Across Frameworks:**
Even if your new project uses a different framework (not Playwright), these templates adapt to:

- Screenplay Pattern (Serenity BDD)
- Behavior-Driven Development (Cucumber)
- Data-Driven Testing (TestNG, Pytest)
- Keyword-Driven Testing
- Hybrid frameworks

**📖 Start Here:**

1. **Quick Start?** → Check [AI_QUICK_REFERENCE.md](AI_QUICK_REFERENCE.md)
2. **Need Full Workflow?** → Follow [AI_WORKFLOW_EXAMPLES.md](AI_WORKFLOW_EXAMPLES.md)
3. **Just Want Prompts?** → Copy from [AI_PROMPT_TEMPLATES.md](AI_PROMPT_TEMPLATES.md)

---

## Features

- **Playwright Integration**: Fast and reliable browser automation
- **BDD with Cucumber**: Gherkin syntax for readable test scenarios
- **TestNG**: Advanced test configuration and parallel execution
- **Page Object Model**: Maintainable and reusable code structure
- **Smart Locator Strategy**: Prioritizes ID attributes over labels for reliable element identification
- **Auto-Fix Test Generator**: Generates page objects, feature files, and step definitions from recordings
  - **Automatic Cleanup**: Removes unused template files after successful generation
- **Extent Reports**: Comprehensive HTML and Spark reports with screenshots on failure
- **Retry Mechanism**: Automatic retry for failed tests
- **Recording Support**: Visual test execution recording in AVI format
- **AI-Enhanced Framework**: Smart test data generation and optimization suggestions

## Project Structure

```

Playwright_Template/
├── src/
│   ├── main/java/
│   │   ├── configs/          # Framework configuration and utilities
│   │   │   ├── base.java     # Base test setup
│   │   │   ├── AITestFramework.java  # AI-enhanced testing features
│   │   │   └── TestGeneratorHelper.java  # Auto-generate tests from recordings
│   │   └── pages/            # Page Object Model classes
│   └── test/
│       ├── java/
│       │   ├── features/     # Cucumber feature files
│       │   ├── stepDefs/     # Step definition classes
│       │   ├── runner/       # TestNG runner configuration
│       │   └── hooks/        # Cucumber hooks
│       └── resources/
│           ├── configurations.properties      # Test configuration
│           ├── cucumber.properties           # Cucumber settings
│           ├── extent.properties             # Extent report configuration
│           └── testng.xml                    # TestNG suite configuration
├── MRITestExecutionReports/  # Test execution reports and recordings
├── pom.xml                   # Maven dependencies
└── README.md                 # This file

```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Playwright browsers (auto-installed on first run)

## Installation

1. Clone the repository
2. Install dependencies:

   ```bash
   mvn clean install
   ```

## Quick Start

### Prerequisites

1. Install Node.js dependencies (one-time setup):

   ```bash
   npm install
   ```

### Option 1: Interactive Menu (Recommended)

Launch the interactive CLI menu to access all features:

```bash
npm start
```

Or:

```bash
node automation-cli.js
```

This displays a menu with options for:

- 🎥 Record & Auto-Generate Tests
- 🔄 Retry from Existing Recording  
- 📋 Generate from JIRA Story
- 🤖 AI Interactive Generation
- ⚙️ Setup & Installation
- 🧹 Clean & Utilities

### Option 2: Direct Commands (NPM Scripts)

Execute specific actions directly:

```bash
# Record new test and generate files
npm run record

# Retry generation from existing recording
npm run retry

# Generate from JIRA story (interactive)
npm run jira

# AI-guided interactive generation
npm run ai-generate

# Setup MCP server (one-time)
npm run setup

# Install all dependencies
npm run install-deps

# Clean build artifacts
npm run clean

# View tutorial/help
npm run help
```

### Option 3: Maven Test Execution

Run your tests:

```bash
# Run all tests
npm test
# or
mvn clean test

# Run specific test suite
npm run test:suite

# Run tests by tag
npm run test:tag @smoke

# Run tests in parallel
npm run test:parallel

# Generate reports
npm run report
```

### Workflow Example

**Complete test generation in 3 steps:**

```bash
# Step 1: Record your test actions
npm run record

# Step 2: Review generated files
# - Page Object: src/main/java/pages/*.java
# - Feature: src/test/java/features/*.feature  
# - Steps: src/test/java/stepDefs/*Steps.java

# Step 3: Run tests
npm test
```

## Cross-Platform Compatibility

✅ **Windows, macOS, Linux** - All NPM commands work identically across platforms

No platform-specific files (.bat, .sh, .ps1) required. Pure Node.js + Maven solution.

## Configuration

### Test Credentials

Edit `src/test/resources/configurations.properties`:

```properties
USERNAME=your_username
PASSWORD=your_password
URL=https://your-test-url.com
```

### TestNG Configuration

Edit `src/test/resources/testng.xml` for:

- Test suites
- Parallel execution
- Retry configuration
- Listeners

### Extent Reports

Configure in `src/test/resources/extent.properties`:

- Report theme
- Screenshot capture on failure
- Report title and document name

## Smart Locator Strategy

The framework prioritizes locators in this order:

1. **ID attributes** (Priority 1) - Most stable
2. **Data-test attributes** (Priority 2)
3. **Name attributes** (Priority 3)
4. **Label text** (Priority 4)
5. **Button text/role** (Priority 5)

This ensures maximum test stability and reduces maintenance.

## Features

### Auto-Fix Test Generator

- Validates selectors during generation
- Reuses existing page objects and login methods
- Prevents duplicate step definitions
- Smart handling of common actions (login, logout)
- Auto-detects existing test data configuration
- **Automatic cleanup**: Removes unused template files after successful generation
  - Keeps your workspace clean
  - Only deletes files that are not referenced in the codebase
  - Non-blocking: Cleanup errors don't affect test generation

### Retry Mechanism

Failed tests are automatically retried based on configuration in TestNG.

### Extent Reports

- HTML reports with detailed test execution
- Spark reports with charts and statistics
- Screenshots automatically attached on failure
- Execution history and trends

### Recording Support

Test executions can be recorded as video:

- AVI format recordings
- Saved in `MRITestExecutionReports/*/recordings/`
- Configurable via properties

## Common Methods

Framework includes reusable methods in `CommonSteps.java`:

- Login/Logout operations
- Navigation actions
- Common UI interactions

Prevents duplicate step definitions across multiple feature files.

## AI-Enhanced Features

The `AITestFramework` provides:

- Smart test data generation
- Form data auto-population
- Optimization suggestions
- Test health monitoring
- Coverage reports

## Troubleshooting

### Issue: Locator Not Found

- Check if element exists on page
- Verify smart locator priority (ID > Label)
- Review generated page object for warnings

### Issue: Duplicate Step Definitions

- Framework automatically reuses common steps
- Check `CommonSteps.java` for existing methods
- Avoid creating duplicate logout/login steps

### Issue: Retry Not Working

- Verify TestNG retry listener is configured
- Check `testng.xml` for retry count settings
- Review console output for retry attempts

### Issue: Reports Not Generated

- Verify extent.properties configuration
- Check file permissions in reports directory
- Ensure test execution completes successfully

## Reports Location

After test execution:

```
MRITestExecutionReports/
└── Version{Year}Build{Build}/
    ├── extentReports/
    │   └── testNGExtentReports/
    │       ├── html/         # HTML reports
    │       └── spark/        # Spark reports
    └── recordings/           # Video recordings
```

## Best Practices

1. **Reuse existing methods**: Check `CommonSteps.java` before creating new step definitions
2. **Use configuration data**: Reference `configurations.properties` instead of hardcoding values
3. **Follow naming conventions**: Feature names should match page/functionality
4. **Smart locators**: Let the framework use ID attributes when available
5. **Review warnings**: Check console output for locator optimization suggestions

## Support

For issues or questions, refer to the console output during test generation - it provides detailed validation and debugging information.

## License

Internal use - MRI Energy Test Automation

---
**Last Updated**: February 2026
