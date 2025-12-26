# 🚀 Playwright Test Automation Framework

Complete BDD framework with Playwright Java, Cucumber, and TestNG.

## 📖 Documentation

**👉 [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md) - SINGLE SOURCE OF TRUTH**

Everything in one place with TODO checklists:
- ✅ Pure Java Recording (NO Node.js needed)
- ✅ AI-Assisted Generation (Requires Node.js)
- ✅ Recorder Troubleshooting (with TODO steps)
- ✅ Dynamic Locator Strategies (9 helper methods)
- ✅ Complete validation checklists
- ✅ All fixes and solutions

---

## ⚡ Quick Start

### Fastest Way - Automated Recording
```bash
record-and-generate.bat
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

### For Recording:
- Java 17+
- Maven 3.6+

### For AI Interactive CLI:
- Node.js 18+
- npm install
- JIRA credentials configured (optional)

---

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
   - Syntax issues
4. **Compiles & runs tests** automatically
5. **Retries flaky tests** (configurable)
6. **Generates reports** (Extent Reports with screenshots)

---

## 📊 Project Structure

```
src/
├── main/java/
│   ├── configs/          # Framework configuration
│   │   ├── base.java
│   │   ├── utils.java (9 dynamic locator helpers)
│   │   ├── TestGeneratorHelper.java
│   │   ├── RetryAnalyzer.java
│   │   └── BasePage.java
│   └── pages/            # Page Objects (auto-generated)
│       └── {Feature}.java
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

---

## 🔧 Key Features

- ✅ **Pure Java Recording** - No Node.js for recording
- ✅ **Modern Playwright API** - Locator API support
- ✅ **Intelligent Parser** - Extracts all recorded actions
- ✅ **Auto-Validation** - Checks duplicates, syntax
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
- **Screenshots**: `MRITestExecutionReports/Version*/screenShots/`
- **Recordings**: `MRITestExecutionReports/Version*/recordings/`

---

## 🔍 Validation

Ensure all recorded steps are covered:
```bash
powershell -ExecutionPolicy Bypass -File validate-coverage.ps1 -FeatureName "Login"
```

---

## 🆘 Common Issues

All troubleshooting with TODO checklists in **[COMPLETE_GUIDE.md](COMPLETE_GUIDE.md)**:
- ❌ Steps not saved in recording file → **FIXED (Dec 26, 2025)** - See "Recorder Troubleshooting"
- ❌ getByLabel() actions not parsed → **FIXED (Dec 26, 2025)**
- ❌ Recording file created but empty → Troubleshooting section has TODO checklist
- ❌ Duplicate step definitions → Auto-validation handles this
- ❌ Dynamic IDs causing failures → Use dynamic locator helpers (9 methods in utils.java)
- ❌ Elements not found → See Dynamic Locators section

---

## 📚 Documentation Structure

**[COMPLETE_GUIDE.md](COMPLETE_GUIDE.md)** - Single comprehensive guide with TODO checklists  
**README.md** (this file) - Quick reference and entry point

---

**🎉 Ready to start? Run `record-and-generate.bat` and follow the prompts!**

**📖 For complete step-by-step TODO checklists, see [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md)**

---

**Last Updated**: December 26, 2025  
**Framework Version**: 2.0
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
