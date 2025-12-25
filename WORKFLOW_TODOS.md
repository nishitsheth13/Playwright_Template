# 🚀 Test Automation Workflow - Complete TODO Checklist

## 📋 Quick Start Guide

Choose your workflow based on your needs:
- **Option 1**: 🎥 Record & Auto-Generate (Fastest - 5-10 min)
- **Option 2**: 🤖 AI-Assisted Interactive (Full-featured)
- **Option 3**: ✅ Validate & Run Tests (Check existing)

---

## 🎥 OPTION 1: Record & Auto-Generate Workflow

**Best for**: Quick test creation from recorded browser actions

### TODO Checklist:

- [ ] **STEP 1: Prepare Environment**
  - [ ] Ensure Maven is installed: `mvn --version`
  - [ ] Ensure Java JDK installed (17 or higher)
  - [ ] Node.js is OPTIONAL (only needed for Option 2 - AI Interactive CLI)
  - [ ] Verify `configurations.properties` has correct base URL
  - [ ] Location: `src\test\resources\configurations.properties`
  - [ ] Check URL format: `URL=https\://your-domain.com/base-path`

- [ ] **STEP 2: Launch Recording**
  - [ ] Run: `generate-test.bat` or double-click `record-and-generate.bat`
  - [ ] Choose Option 1 from menu
  - [ ] Enter feature name (e.g., "Login", "Profile", "Dashboard")

- [ ] **STEP 3: Choose URL Mode**
  - [ ] **Mode 1** - Use config URL + path:
    - [ ] Enter path to append (e.g., `/login`, `/dashboard`)
    - [ ] System will use: `config_url + your_path`
  - [ ] **Mode 2** - Custom full URL:
    - [ ] Enter complete URL including `https://`
    - [ ] Example: `https://custom-site.com/full-path`

- [ ] **STEP 4: Optional JIRA Integration**
  - [ ] Enter JIRA Story ID if needed
  - [ ] Press Enter to skip (will use "AUTO-GEN")

- [ ] **STEP 5: Browser Recording**
  - [ ] Wait for browser to open automatically
  - [ ] Browser will navigate to your target URL
  - [ ] Perform actions you want to test:
    - [ ] Click buttons
    - [ ] Fill forms
    - [ ] Navigate pages
    - [ ] Verify elements
  - [ ] Close browser when done

- [ ] **STEP 6: Auto-Generation Process**
  - [ ] System extracts actions from recording using Pure Java
  - [ ] Generates 3 files automatically:
    - [ ] Page Object: `src\main\java\pages\{feature}Page.java`
    - [ ] Feature File: `src\test\java\features\{feature}.feature`
    - [ ] Step Definitions: `src\test\java\stepDefs\{feature}Steps.java`
  - [ ] NO Node.js required - Pure Java generator

- [ ] **STEP 7: Auto-Compilation**
  - [ ] System runs Maven compile (max 3 attempts)
  - [ ] Smart auto-fix runs between attempts
  - [ ] Fixes applied:
    - [ ] File name matches class name
    - [ ] Missing imports added
    - [ ] Syntax errors corrected
    - [ ] Code cleanup performed

- [ ] **STEP 8: Validation**
  - [ ] Review generated files
  - [ ] Check compilation output
  - [ ] Note any manual fixes needed
  - [ ] Review code review warnings

- [ ] **STEP 9: Test Execution**
  - [ ] Choose "Yes" to run tests immediately
  - [ ] Or run later: `mvn test` or use Option 3

---

## 🤖 OPTION 2: AI-Assisted Interactive Workflow

**Best for**: Complex tests with JIRA integration or detailed requirements

### TODO Checklist:

- [ ] **STEP 1: Setup MCP Server**
  - [ ] Run once: `setup-mcp.bat`
  - [ ] Verifies Node.js installation
  - [ ] Installs MCP dependencies
  - [ ] No need to repeat unless updating

- [ ] **STEP 2: Launch CLI**
  - [ ] Run: `generate-test.bat`
  - [ ] Choose Option 2 from menu
  - [ ] Wait for MCP server to start
  - [ ] Server runs on port 3000

- [ ] **STEP 3: Choose Input Method**
  - [ ] **Method A**: Manual Input
    - [ ] Answer AI questions about test
    - [ ] Provide feature details
    - [ ] Describe user flows
    - [ ] Specify assertions needed
  - [ ] **Method B**: JIRA Story
    - [ ] Enter JIRA Story ID
    - [ ] System fetches story details
    - [ ] AI extracts test requirements
    - [ ] Generates test from acceptance criteria

- [ ] **STEP 4: AI Generation**
  - [ ] AI analyzes requirements
  - [ ] Generates Page Object with methods
  - [ ] Creates Feature file with scenarios
  - [ ] Writes Step Definitions
  - [ ] Files saved to respective folders

- [ ] **STEP 5: Auto-Compilation Loop**
  - [ ] System compiles generated code
  - [ ] Up to 5 auto-fix attempts
  - [ ] Each attempt:
    - [ ] Compiles with Maven
    - [ ] Analyzes errors
    - [ ] Applies smart fixes
    - [ ] Retries compilation

- [ ] **STEP 6: Error Handling**
  - [ ] Review compilation errors if any remain
  - [ ] Check syntax corrections applied
  - [ ] Review code cleanup actions
  - [ ] Check code review warnings

- [ ] **STEP 7: Test Validation**
  - [ ] Review generated test logic
  - [ ] Verify step definitions match feature
  - [ ] Check page object methods
  - [ ] Ensure proper inheritance

- [ ] **STEP 8: Test Execution**
  - [ ] CLI offers to run tests
  - [ ] Choose "Yes" to execute
  - [ ] View test results
  - [ ] Check extent reports

---

## ✅ OPTION 3: Validate & Run Tests Workflow

**Best for**: Running existing tests, checking compilation, fixing errors

### TODO Checklist:

- [ ] **STEP 1: Pre-Validation Checks**
  - [ ] Verify project structure:
    - [ ] `src\main\java\configs\utils.java` exists
    - [ ] `src\main\java\configs\browserSelector.java` exists
    - [ ] `src\main\java\pages\BasePage.java` exists
  - [ ] Check test files exist in:
    - [ ] `src\test\java\features\` (feature files)
    - [ ] `src\test\java\stepDefs\` (step definitions)
    - [ ] `src\main\java\pages\` (page objects)

- [ ] **STEP 2: Launch Validation**
  - [ ] Run: `generate-test.bat`
  - [ ] Choose Option 3 from menu
  - [ ] System starts structure analysis

- [ ] **PHASE 1: Structure Analysis**
  - [ ] System checks file existence
  - [ ] Validates class inheritance:
    - [ ] Page objects extend `BasePage`
    - [ ] Step definitions extend `browserSelector`
  - [ ] Verifies import statements
  - [ ] Reviews generated report

- [ ] **PHASE 2: Known Issues Check**
  - [ ] Checks for missing `loadProps` imports
  - [ ] Scans for duplicate step definitions
  - [ ] Detects protected methods (auto-fixes to public)
  - [ ] Reviews findings

- [ ] **PHASE 3: Compilation (3 Attempts)**
  - [ ] **Attempt 1**:
    - [ ] Runs `mvn clean compile test-compile`
    - [ ] If fails → triggers smart auto-fix
    - [ ] Applies all fix patterns
  - [ ] **Attempt 2** (if needed):
    - [ ] Recompiles after auto-fix
    - [ ] Runs smart fix again if needed
    - [ ] Checks for remaining errors
  - [ ] **Attempt 3** (if needed):
    - [ ] Final compilation attempt
    - [ ] Applies fallback basic fixes:
      - [ ] File name to match class name
      - [ ] Add missing imports
      - [ ] Fix capitalizations
    - [ ] Reports final status

- [ ] **PHASE 4: Smart Auto-Fix Report**
  - [ ] Review fix summary:
    - [ ] Files scanned
    - [ ] Files modified
    - [ ] Total fixes applied
    - [ ] Syntax issues fixed
    - [ ] Cleanup issues fixed
  - [ ] Review code review warnings:
    - [ ] Null comparison issues
    - [ ] Thread.sleep() usage
    - [ ] System.out.println detected
    - [ ] Other best practice violations

- [ ] **PHASE 5: Test Execution**
  - [ ] If compilation successful:
    - [ ] System offers to run tests
    - [ ] Choose execution mode:
      - [ ] All tests: `mvn test`
      - [ ] Specific feature: `mvn test -Dcucumber.options="--tags @{tag}"`
  - [ ] Wait for test execution
  - [ ] Review test output

- [ ] **PHASE 6: Report Analysis**
  - [ ] Check extent reports:
    - [ ] HTML report: `MRITestExecutionReports\Version*\extentReports\testNGExtentReports\html\`
    - [ ] Spark report: `MRITestExecutionReports\Version*\extentReports\testNGExtentReports\spark\`
  - [ ] Check Cucumber reports:
    - [ ] `target\cucumber-reports\cucumber.html`
  - [ ] Review test status:
    - [ ] Passed tests
    - [ ] Failed tests
    - [ ] Skipped tests
  - [ ] Check screenshots (if failures)
  - [ ] Check recordings (if enabled)

- [ ] **PHASE 7: Failure Analysis** (if needed)
  - [ ] Review error messages
  - [ ] Check stack traces
  - [ ] Verify selectors in page objects
  - [ ] Check timing issues (waits needed?)
  - [ ] Verify test data
  - [ ] Run smart fix again if compilation issues

---

## 🔧 Common Issues & Solutions TODO

### Issue 1: "{ was unexpected at this time"
- [ ] Check batch file for unescaped special characters
- [ ] Verify curly braces are escaped: `^{` and `^}`
- [ ] Check PowerShell code blocks in batch files

### Issue 2: "Maven not found"
- [ ] Install Maven from https://maven.apache.org/
- [ ] Add to PATH: `C:\Program Files\Apache\maven\bin`
- [ ] Verify: `mvn --version`

### Issue 3: "Node.js not found" (Option 2 only)
- [ ] Node.js only needed for AI Interactive CLI (Option 2)
- [ ] Recording (Option 1) uses Pure Java - no Node.js needed
- [ ] Install Node.js from https://nodejs.org/ if using Option 2
- [ ] Version 18+ required for AI features
- [ ] Verify: `node --version`

### Issue 4: Compilation errors persist
- [ ] Run smart auto-fix manually: `powershell -File scripts\smart-compiler-fix.ps1`
- [ ] Review fix report
- [ ] Check code review warnings
- [ ] Apply manual fixes for complex issues

### Issue 5: Browser doesn't open for recording
- [ ] Check Playwright installation: `mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="--help"`
- [ ] Install browsers: `mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"`
- [ ] Verify URL extraction works

### Issue 6: Tests fail to find elements
- [ ] Check selectors in page objects
- [ ] Add explicit waits: `page.waitForSelector()`
- [ ] Verify URL in configurations.properties
- [ ] Check if page loaded completely

### Issue 7: JIRA integration not working
- [ ] Verify JIRA credentials in `src\test\resources\jiraConfigurations.properties`
- [ ] Check JIRA Story ID format
- [ ] Ensure network connectivity
- [ ] Verify JIRA API permissions

---

## 📊 Post-Execution TODO

After successful test creation/execution:

- [ ] **Code Review**
  - [ ] Review generated code quality
  - [ ] Check naming conventions
  - [ ] Verify proper imports
  - [ ] Review method signatures

- [ ] **Test Refinement**
  - [ ] Add additional assertions
  - [ ] Enhance error messages
  - [ ] Add comments for clarity
  - [ ] Group related tests with tags

- [ ] **Documentation**
  - [ ] Document test purpose
  - [ ] Add setup/teardown notes
  - [ ] Document test data requirements
  - [ ] Note any special configurations

- [ ] **Version Control**
  - [ ] Commit generated files
  - [ ] Write meaningful commit message
  - [ ] Push to repository
  - [ ] Update team documentation

- [ ] **CI/CD Integration** (if applicable)
  - [ ] Add to build pipeline
  - [ ] Configure test triggers
  - [ ] Set up report notifications
  - [ ] Configure failure alerts

---

## 🎯 Best Practices TODO

Before starting any workflow:

- [ ] **Environment Setup**
  - [ ] Latest Maven installed
  - [ ] Java JDK 17+ installed
  - [ ] Node.js (v18+) - OPTIONAL, only for AI Interactive CLI (Option 2)
  - [ ] IDE configured (IntelliJ/Eclipse)
  - [ ] Git initialized and configured

- [ ] **Configuration Check**
  - [ ] Base URL correct in configurations.properties
  - [ ] Browser settings configured
  - [ ] Timeout values appropriate
  - [ ] Report paths configured

- [ ] **Project Structure**
  - [ ] Configs folder complete
  - [ ] Base classes present
  - [ ] Utils available
  - [ ] Hooks configured

- [ ] **Dependencies**
  - [ ] pom.xml up to date
  - [ ] All Maven dependencies resolved
  - [ ] Playwright version correct
  - [ ] Cucumber version compatible

---

## 📝 Daily Workflow TODO

### Morning Setup
- [ ] Pull latest code from repository
- [ ] Run `mvn clean install` to verify build
- [ ] Check for any dependency updates
- [ ] Review overnight test failures (if CI/CD)

### Test Creation
- [ ] Choose appropriate option (1, 2, or 3)
- [ ] Follow respective TODO checklist
- [ ] Review auto-fix reports
- [ ] Address code review warnings

### Before Commit
- [ ] Run all tests locally
- [ ] Check test reports
- [ ] Review code changes
- [ ] Ensure no compilation errors
- [ ] Clean up commented code

### End of Day
- [ ] Commit all working tests
- [ ] Update documentation if needed
- [ ] Note any pending issues
- [ ] Push changes to repository

---

## 🚨 Emergency Troubleshooting TODO

If everything breaks:

- [ ] **Hard Reset**
  - [ ] Run: `mvn clean`
  - [ ] Delete target folder manually
  - [ ] Run: `mvn clean install -U`
  - [ ] Restart IDE

- [ ] **Dependency Issues**
  - [ ] Check internet connectivity
  - [ ] Clear Maven cache: `rm -rf ~/.m2/repository`
  - [ ] Force update: `mvn clean install -U`
  - [ ] Verify pom.xml syntax

- [ ] **Compilation Hell**
  - [ ] Run smart fix: `powershell -File scripts\smart-compiler-fix.ps1 -verbose`
  - [ ] Review all errors manually
  - [ ] Check for circular dependencies
  - [ ] Verify class/file name matches

- [ ] **Test Execution Failures**
  - [ ] Check browser drivers installed
  - [ ] Verify network access to application
  - [ ] Check credentials if needed
  - [ ] Review timeout configurations

---

## ✅ Success Criteria

Your workflow is complete when:

- [ ] All files generated successfully
- [ ] Compilation passes with 0 errors
- [ ] Tests execute without failures
- [ ] Reports generated correctly
- [ ] Code review warnings addressed
- [ ] Code committed to repository
- [ ] Team notified of new tests

---

**Last Updated**: December 25, 2025
**Framework Version**: Playwright + Cucumber + Maven
**Maintained By**: AI Test Automation Team
