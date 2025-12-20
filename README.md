```md
# Playwright with Java - MRI Energy Automation Project

This project contains automation test scripts for **MRI Energy Automation**, built using **Java + Playwright + JUnit + TestNG**.
It enables web application testing across different browsers and includes detailed reporting with screenshots for failed
scenarios.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Introduction to Framework](#introduction-to-framework)
3. [Framework Structure](#framework-structure)
4. [Running Tests](#running-tests)
5. [Generating Reports](#generating-reports)
6. [Creating New Test Scripts](#creating-new-test-scripts)
7. [Recent Improvements](#recent-improvements)
8. [Test Data](#test-data)
9. [POM File](#pom-file)
10. [Contact](#contact)

---

## Getting Started

In order to run the automation test scripts, ensure that the following software is installed:

1. **Java JDK** (1.8 or higher)
    - [Download Java](https://www.oracle.com/in/java/technologies/downloads/)

2. **Integrated Development Environment (IDE)**:
    - Recommended: IntelliJ IDEA or Eclipse
        - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
        - [Eclipse IDE](https://www.eclipse.org/downloads/)
    - If you choose **Eclipse**, install **Cucumber** and **TestNG** plugins from **Help > Eclipse Marketplace**.

3. **Playwright CLI**: Install globally for browser management
   ```bash
   npm install -g playwright
   ```

4. **Browser Installations**:
    - Install Chrome, Firefox, and Edge browsers to perform cross-browser testing.

### Project Setup

To set up the project:

1. **Clone the repository** or **download the Zip** file and import it into your IDE:
   ```bash
   git clone https://github.com/your-repo-url/playwright-java-project.git
   ```

2. Navigate to the project folder and install dependencies:
   ```bash
   mvn clean install
   ```

3. Install Playwright browsers:
   ```bash
   npx playwright install
   ```

---

## Introduction to Framework

This test automation framework is built using **Java**, **Playwright**, and **JUnit**, following the **Page Object
Model (POM)** design pattern. It also integrates with **Extent Reports** for generating detailed test execution reports.

The project uses **Playwright** for browser automation, allowing cross-browser support. The **POM design pattern**
centralizes UI elements, improving code reusability and maintainability.

---

## Framework Structure

The project structure is as follows:

- **/src/main/java**: Contains important Java classes like `Base`, `BrowserSelector`, and `Utils`, along with page
  object classes.
- **/src/test/java**: Contains feature files, test runner classes, and step definitions.
- **/src/test/resources**: Stores configuration files.
- **/MRITestExecutionReports/Versions/ExtentReport.html**: Contains the generated reports after execution.
- **POM.xml**: Contains Maven dependencies and plugins for Playwright, JUnit, Extent Reports, and more.

---

## Running Tests

### Run Tests Using Maven:

To run all the tests, execute the following command:

```bash
mvn test
```

### Running Specific Browser Tests:

Specify the browser for test execution by using Maven profiles:

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

