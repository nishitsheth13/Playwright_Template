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
6. [Test Data](#test-data)
7. [POM File](#pom-file)
8. [Contact](#contact)

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

