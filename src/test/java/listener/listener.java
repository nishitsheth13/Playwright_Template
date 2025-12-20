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

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class listener extends utils implements ITestListener {
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();
    ExtentReports extent = testNGExtentReporter.extentReportGenerator();
    ExtentTest test;

    String passCommentText = loadProps.getProperty("PassComment") + loadProps.getProperty("Version");
    String failCommentText = loadProps.getProperty("FailedComment") + loadProps.getProperty("Version");
    Boolean isJiraEnabled = Boolean.parseBoolean(loadProps.getProperty("JIRA_Integration"));

    public listener() throws IOException {
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

            extentTest.get().addScreenCaptureFromPath(getScreenShotPath(result.getTestContext().getName()));
            extentTest.get().log(Status.FAIL, "Test Failed");
            extentTest.get().fail(result.getThrowable());
            // Get driver instance
            Object obj = result.getInstance();
            Class<?> cl = result.getTestClass().getRealClass();
            try {
                page = (com.microsoft.playwright.Page) cl.getDeclaredField("page").get(obj);
            } catch (NoSuchFieldException e) {
                // Field not found, try to get page from parent class (for classes extending browserSelector)
                try {
                    page = (com.microsoft.playwright.Page) cl.getSuperclass().getDeclaredField("page").get(obj);
                } catch (Exception ex) {
                    System.out.println("⚠️ Could not access page instance for screenshot: " + ex.getMessage());
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not access page instance: " + e.getMessage());
            }

            File screenshotFile = null;
            try {
                screenshotFile = new File(getScreenShotPath(result.getName()));
                extentTest.get().addScreenCaptureFromPath(screenshotFile.getAbsolutePath());
            } catch (IOException | AWTException e) {
                e.printStackTrace();
            }

            // Jira bug handling
            String issueKey = getDynamicIssueKey(result);
            String summary = "Automation Test Failed - " + result.getMethod().getMethodName();
            String description = result.getThrowable() != null ?
                    result.getThrowable().toString().replace("\"", "'").replace("\n", "\\n") : "No stack trace";
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void onTestSkipped(ITestResult result) {
        if (extent.isKeepLastRetryOnly()) {
            try {
                extentTest.get().addScreenCaptureFromPath(getScreenShotPath(result.getTestContext().getName()));
                extentTest.get().skip(result.getTestName());
                extentTest.get().log(Status.SKIP, "Test Skipped");
            } catch (IOException | AWTException e) {
                e.printStackTrace();
            }
        }

    }

    public void onFinish(ITestContext context) {
        extent.flush();
    }


    // ... existing code ...
    private String getDynamicIssueKey(ITestResult result) {
        // Option 1: Check parameters for specific issue tags (e.g., from Cucumber scenarios)
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (Object param : parameters) {
                String paramStr = param.toString();
                if (paramStr.contains("Verify User is not able to access the MRI Energy application With InValid Credentials")) {
                    return "ECS-7";
                }
                if (paramStr.contains("Verify User is able to access the MRI Energy application with Valid Credentials")) {
                    return "ECS-8";
                }
            }
            // Fallback: if parameters exist but no specific tag matched, default to ECS-7
            // (Replaces the original 'else { methodName = "runScenario" }' logic)
            return null;
        }

        // Option 2: Map based on Method Name (if no parameters were present)
        String methodName = result.getMethod().getMethodName();
        switch (methodName) {
            case "Failed_login":
            case "Verify User is not able to access the MRI Energy application With InValid Credentials":
                return "ECS-7";
            case "Verify User is able to access the MRI Energy application with Valid Credentials":
            case "login":
                return "ECS-8";
            default:
                return null; // fallback to config
        }
    }
// ... existing code ...
    // Leave other events if not needed
}
