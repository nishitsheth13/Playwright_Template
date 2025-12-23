package listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import configs.jira.jiraClient;
import configs.loadProps;
import configs.testNGExtentReporter;
import configs.utils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;

public class listener extends utils implements ITestListener {
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    ExtentReports extent = testNGExtentReporter.extentReportGenerator();
    ExtentTest test;

    String passCommentText = loadProps.getProperty("PassComment") + loadProps.getProperty("Version");
    Boolean isJiraEnabled = Boolean.parseBoolean(loadProps.getProperty("JIRA_Integration"));

    public listener() throws IOException {
    }

    public void onStart(ITestContext context) {
        System.out.println("📊 Starting Test Suite: " + context.getName());
        System.out.println("📁 Reports will be generated in: " + System.getProperty("user.dir") + "/MRITestExecutionReports/");
    }

    public void onTestStart(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            test = extent.createTest(params[0].toString().replace("\"", ""));
            extentTest.set(test);
        } else {
            test = extent.createTest(result.getMethod().getMethodName());
            extentTest.set(test);
        }

        testNGExtentReporter.test = test; // Update the static ref if you use it elsewhere
    }

    public void onTestSuccess(ITestResult result) {

        extentTest.get().log(Status.PASS, "Successful");
        String issueKey = getDynamicIssueKey(result);
        if (issueKey != null && isJiraEnabled) {
            // If JIRA integration is enabled, log the result in JIRA
            jiraClient.handleTestCaseResultSmart(
                    issueKey,
                    "Automation Test Passed - " + result.getMethod().getMethodName(),
                    passCommentText,
                    null,
                    false
            );
        }
    }

    public void onTestFailure(ITestResult result) {
        try {
            extentTest.get().log(Status.FAIL, "Test Failed");
            extentTest.get().fail(result.getThrowable());

            // Check if page is available (should be set by browserSelector.setUp())
            if (page == null) {
                System.err.println("⚠️ Page instance is null - attempting to retrieve from test class");
                // Get driver instance from test class as fallback
                Object obj = result.getInstance();
                Class<?> cl = result.getTestClass().getRealClass();
                try {
                    page = (com.microsoft.playwright.Page) cl.getDeclaredField("page").get(obj);
                    System.out.println("✅ Retrieved page instance from test class");
                } catch (NoSuchFieldException e) {
                    // Field not found, try to get page from parent class
                    try {
                        page = (com.microsoft.playwright.Page) cl.getSuperclass().getDeclaredField("page").get(obj);
                        System.out.println("✅ Retrieved page instance from parent class");
                    } catch (Exception ex) {
                        System.err.println("❌ Could not access page instance: " + ex.getMessage());
                        page = null;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error accessing page instance: " + e.getMessage());
                    page = null;
                }
            }

            // Take screenshot for JIRA (only if page is available)
            File screenshotFile = null;
            if (page != null) {
                try {
                    String screenshotPath = getScreenShotPath(result.getName());
                    screenshotFile = new File(screenshotPath);

                    // Add to Extent Report
                    extentTest.get().addScreenCaptureFromPath(screenshotFile.getAbsolutePath());

                    System.out.println("📸 Screenshot created for JIRA:");
                    System.out.println("   - Path: " + screenshotFile.getAbsolutePath());
                    System.out.println("   - Exists: " + screenshotFile.exists());
                    System.out.println("   - Size: " + (screenshotFile.exists() ? screenshotFile.length() + " bytes" : "N/A"));
                } catch (Exception e) {
                    System.err.println("❌ Failed to create screenshot: " + e.getMessage());
                    System.err.println("   Exception type: " + e.getClass().getName());
                    screenshotFile = null;
                }
            } else {
                System.err.println("⚠️ Cannot take screenshot - page instance is null (browser might have closed)");
            }

            // Jira bug handling
            String issueKey = getDynamicIssueKey(result);
            String summary = "Automation Test Failed - " + result.getMethod().getMethodName();
            String description = result.getThrowable() != null ?
                    result.getThrowable().toString().replace("\"", "'").replace("\n", "\\n") : "No stack trace";

            System.out.println("🎫 JIRA Integration for failed test:");
            System.out.println("   - Issue Key: " + (issueKey != null ? issueKey : "NOT MAPPED"));
            System.out.println("   - JIRA Enabled: " + isJiraEnabled);
            System.out.println("   - Screenshot File: " + (screenshotFile != null ? screenshotFile.getName() : "NULL"));

            if (isJiraEnabled) {
                if (issueKey != null) {
                    // If JIRA integration is enabled, log the result in JIRA
                    jiraClient.handleTestCaseResultSmart(
                            issueKey,
                            summary,
                            description,
                            screenshotFile,
                            true
                    );
                } else {
                    jiraClient.createBug(summary, description, screenshotFile);
                    System.out.println("⚠️ No JIRA IssueKey provided or mapped. Creating New Jira Issue");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in onTestFailure: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void onTestSkipped(ITestResult result) {
        try {
            // Check if this is a retry (test was skipped due to retry mechanism)
            if (result.wasRetried()) {
                System.out.println("🔄 Test was retried and skipped: " + result.getName());
                // Don't log skipped retries to avoid cluttering the report
                return;
            }

            extentTest.get().skip(result.getTestName());
            extentTest.get().log(Status.SKIP, "Test Skipped: " +
                    (result.getThrowable() != null ? result.getThrowable().getMessage() : "No reason provided"));

            // Try to add screenshot if available
            try {
                extentTest.get().addScreenCaptureFromPath(getScreenShotPath(result.getTestContext().getName()));
            } catch (Exception e) {
                // Screenshot might not be available for skipped tests, that's okay
                System.out.println("⚠️ Could not capture screenshot for skipped test: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error in onTestSkipped: " + e.getMessage());
        }

    }

    public void onFinish(ITestContext context) {
        System.out.println("📊 Finishing Test Suite: " + context.getName());
        System.out.println("✅ Passed Tests: " + context.getPassedTests().size());
        System.out.println("❌ Failed Tests: " + context.getFailedTests().size());
        System.out.println("⏭️ Skipped Tests: " + context.getSkippedTests().size());

        if (extent != null) {
            extent.flush();
            System.out.println("📝 Extent Report flushed successfully");
        } else {
            System.err.println("⚠️ Warning: Extent Reports was null, report may not be generated");
        }
    }


    // ... existing code ...
    private String getDynamicIssueKey(ITestResult result) {
        // Option 1: Check parameters for specific issue tags (e.g., from Cucumber scenarios)
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (Object param : parameters) {
                String paramStr = param.toString();
                if (paramStr.contains("Verify successful login with valid credentials from configuration")) {
                    return "ECS-8";
                }
                if (paramStr.contains("Verify all login page elements are visible")) {
                    return "ECS-7";
                }
            }
            // Fallback: if parameters exist but no specific tag matched, default to ECS-7
            // (Replaces the original 'else { methodName = "runScenario" }' logic)
            return null;
        }

        // Option 2: Map based on Method Name (if no parameters were present)
        String methodName = result.getMethod().getMethodName();
        return switch (methodName) {
            case "Failed_login", "Verify User is not able to access the MRI Energy application With InValid Credentials" ->
                    "ECS-7";
            case "Verify User is able to access the MRI Energy application with Valid Credentials", "login" ->
                    "ECS-8";
            default -> null; // fallback to config
        };
    }
// ... existing code ...
    // Leave other events if not needed
}
