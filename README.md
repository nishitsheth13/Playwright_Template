# 🚀 Playwright Test Automation Framework

Complete BDD framework with Playwright Java, Cucumber, and TestNG.

---

## 📖 Complete Documentation

**👉 [PLAYWRIGHT_AUTOMATION_COMPLETE.md](PLAYWRIGHT_AUTOMATION_COMPLETE.md) - SINGLE SOURCE OF TRUTH**

Everything you need in one comprehensive guide:
- ✅ All 4 Test Generation Methods
- ✅ Code Reusability Checks & Validation
- ✅ Intelligent Naming System
- ✅ Dynamic Locator Strategies (9 helper methods)
- ✅ Complete Troubleshooting Guide
- ✅ Before & After Comparisons
- ✅ Best Practices & Quick Reference

---

## ⚡ Quick Start

### Fastest Way - Automated Recording
```bash
playwright-automation.bat
```
**NO Node.js required!** Record browser actions → Auto-generate tests in 5-10 minutes.

### Unified CLI Menu
```bash
generate-test.bat
```
Choose from:
1. 🎥 **Record & Auto-Generate** (Fastest - Pure Java)
2. 🤖 **AI-Assisted Interactive** (JIRA support - Requires Node.js)
3. ✅ **Validate & Run Tests** (Check existing tests)

---

## 📋 Prerequisites

**For Recording:** Java 17+, Maven 3.6+  
**For AI CLI:** Node.js 18+, npm

---

## 🎯 What This Framework Does

1. **Records** browser actions using Playwright Inspector
2. **Auto-generates** Page Objects, Features, and Step Definitions with intelligent naming
3. **Validates** code reusability and detects existing implementations
4. **Auto-fixes** duplicates, protected methods, and syntax issues
5. **Compiles & runs** tests with automatic retries
6. **Generates** comprehensive HTML reports with screenshots

---

## 🔧 Key Features

- ✅ **Pure Java Recording** - No Node.js required for recording
- ✅ **Intelligent Naming** - Descriptive element and method names
- ✅ **Code Reusability** - Detects and suggests existing code reuse
- ✅ **Priority Locators** - Stable selector strategies with dynamic ID detection
- ✅ **Auto-Validation** - Comprehensive pre/post-generation checks
- ✅ **Retry Mechanism** - Automatic retry for flaky tests
- ✅ **JIRA Integration** - Optional story-based test generation
- ✅ **Detailed Reporting** - Extent Reports with full traceability
- ✅ **Auto-Fix** - Resolves compilation errors
- ✅ **Retry Mechanism** - Handles flaky tests
- ✅ **Rich Reports** - Extent Reports with screenshots
- ✅ **JIRA Integration** - AI CLI with story import
- ✅ **Dynamic Locators** - 9 helper methods for stable locators
- ✅ **MCP Server** - AI integration for IntelliJ & VS Code

---

## 🧪 Running Tests

```bash
# Full workflow (record + generate + compile + test)
record-and-generate.bat

# Manual compile
mvn clean compile

# Manual test execution
mvn test -DsuiteXmlFile=src/test/testng.xml

# Run specific feature
mvn test -Dcucumber.options="src/test/java/features/Login.feature"
```

---

## 📊 Viewing Reports

After test execution:
- **Extent Reports**: `MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/`
---

## 📚 Complete Documentation

👉 **[PLAYWRIGHT_AUTOMATION_COMPLETE.md](PLAYWRIGHT_AUTOMATION_COMPLETE.md)**

For detailed step-by-step guides, troubleshooting, and advanced features.

---

## 🧪 Running Tests

```bash
# Full workflow (record + generate + compile + test)
playwright-automation.bat

# Compile project
mvn clean compile

# Run all tests
mvn test

# Run specific feature
mvn test -Dcucumber.filter.tags=@Login

# Run TestNG suite
mvn test -DsuiteXmlFile=src/test/testng.xml
```

---

## 📊 View Reports

After test execution, reports are in:
- **Extent HTML:** `MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/`
- **Cucumber HTML:** `target/cucumber-reports/cucumber.html`
- **Screenshots:** `MRITestExecutionReports/Version*/screenShots/`

---

**Last Updated:** December 29, 2025
- Node.js 18+
- npm install
- JIRA credentials configured

## 🎯 What This Framework Does

1. **Records browser actions** using Playwright Inspector
2. **Auto-generates tests:**
   - Page Objects with locator constants
   - Cucumber Feature files (Gherkin)
   - Step Definitions
3. **Auto-validates & fixes:**
   - Duplicate step patterns
   - Protected methods
   - BASE_URL references
4. **Compiles & runs tests** automatically
5. **Retries flaky tests** (configurable)
6. **Generates reports** (Extent Reports)

## 📊 Project Structure

```
src/
├── main/java/
│   ├── configs/          # Framework configuration
│   │   ├── base.java
│   │   ├── utils.java
│   │   ├── TestGeneratorHelper.java
│   │   ├── RetryAnalyzer.java
│   │   └── ...
│   └── pages/            # Page Objects (auto-generated)
│       ├── login.java
│       └── ...
└── test/
    ├── java/
    │   ├── features/     # Cucumber feature files
    │   ├── stepDefs/     # Step definitions
    │   ├── hooks/        # Test hooks
    │   └── runner/       # TestNG runner
    └── resources/
        ├── configurations.properties
        └── testng.xml
```

## 🔧 Key Features

- ✅ **Pure Java Recording** - No Node.js dependencies for recording
- ✅ **Modern Playwright API** - Locator API support (page.locator(), getByRole(), etc.)
- ✅ **Intelligent Parser** - Extracts all recorded actions automatically
- ✅ **Auto-Validation** - Checks duplicate steps, syntax issues
- ✅ **Auto-Fix** - Resolves common compilation errors
- ✅ **Retry Mechanism** - Handles flaky tests automatically (configurable retries)
- ✅ **Rich Reports** - Extent Reports with screenshots and recordings
- ✅ **JIRA Integration** - AI CLI with story import (requires Node.js)
- ✅ **Page Object Model** - Maintainable test structure
- ✅ **BDD Support** - Cucumber with Gherkin syntax

## 📖 Complete Guide

**👉 See [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md) for:**
- Step-by-step TODO checklists for each method
- Validation scripts to ensure 100% coverage
- AI enhancement prompts
- Troubleshooting solutions
- Best practices

## 🆘 Common Issues & Solutions

All troubleshooting in [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md):
- ❌ Recording doesn't extract actions → Check debug logs
- ❌ Duplicate step definitions → Run duplicate checker
- ❌ NullPointerException → Verify hooks initialization
- ❌ Elements not found → Add explicit waits
- ❌ Protected method errors → Auto-fixed by script

## 🎉 Ready to Start?

```bash
# Fastest way - record and generate in 5-10 minutes
record-and-generate.bat
```

Follow the on-screen prompts and refer to [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md) for detailed checklists!

## 🧪 Running Tests

```bash
# Full workflow (record + generate + compile + test)
record-and-generate.bat

# Manual compile
mvn clean compile

# Manual test execution
mvn test -DsuiteXmlFile=src/test/testng.xml

# Run specific feature
mvn test -Dcucumber.options="src/test/java/features/Login.feature"

# Clean rebuild
mvn clean install
```

## 📊 Viewing Reports

After test execution, reports are in:
- **Extent Reports:** `MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/`
- **Cucumber Reports:** `target/cucumber-reports/cucumber.html`
- **Screenshots:** `MRITestExecutionReports/Version*/screenShots/`
- **Recordings:** `MRITestExecutionReports/Version*/recordings/`

## 🔄 Validation

Ensure all recorded steps are covered:
```bash
powershell -ExecutionPolicy Bypass -File validate-coverage.ps1 -FeatureName "Login"
```

See [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md) Phase 4.1 for complete validation checklist.

---

**📖 For complete documentation with TODO checklists, see [MASTER_TEST_GUIDE.md](MASTER_TEST_GUIDE.md)**
