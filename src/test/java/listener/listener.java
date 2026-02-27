package listener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import configs.AITestFramework;
import configs.loadProps;
import configs.testNGExtentReporter;
import configs.utils;
import configs.jira.jiraClient;

public class listener extends utils implements ITestListener {
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    /**
     * Tracks one ExtentTest node per logical test (keyed by display name).
     * Retries of the same test reuse the same node so only one row appears
     * in the report and it reflects the final outcome.
     */
    private final ConcurrentHashMap<String, ExtentTest> testNodes = new ConcurrentHashMap<>();
    ExtentReports extent = testNGExtentReporter.extentReportGenerator();
    ExtentTest test;

    String passCommentText = loadProps.getProperty("PassComment") + loadProps.getProperty("Version");
    Boolean isJiraEnabled = Boolean.parseBoolean(loadProps.getProperty("JIRA_Integration"));

    public listener() throws IOException {
    }

    public void onStart(ITestContext context) {
        System.out.println("📊 Starting Test Suite: " + context.getName());
        System.out.println(
                "📁 Reports will be generated in: " + System.getProperty("user.dir") + "/MRITestExecutionReports/");

        // Initialize AI Test Framework
        System.out.println("🤖 Initializing AI Test Framework...");
        AITestFramework.initialize();
    }

    public void onTestStart(ITestResult result) {
        Object[] params = result.getParameters();
        String displayName = (params != null && params.length > 0)
                ? params[0].toString().replace("\"", "")
                : result.getMethod().getMethodName();

        // Reuse the existing node for retries so only ONE row appears in the report.
        // The node's final status is overwritten to PASS/FAIL when the definitive
        // attempt completes (see onTestSuccess / onTestFailure).
        test = testNodes.computeIfAbsent(displayName, name -> extent.createTest(name));
        extentTest.set(test);
        testNGExtentReporter.test = test;
    }

    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Test Passed");

        // Record execution in AI Framework
        long duration = result.getEndMillis() - result.getStartMillis();
        AITestFramework.TestExecutionRecord record = new AITestFramework.TestExecutionRecord(result.getName(), "PASS",
                duration);
        AITestFramework.recordExecution(record);

        String issueKey = getDynamicIssueKey(result);
        if (issueKey != null && isJiraEnabled) {
            // If JIRA integration is enabled, log the result in JIRA
            jiraClient.updateIssueWithTestResult(
                    issueKey,
                    "Automation Test Passed - " + result.getMethod().getMethodName(),
                    passCommentText,
                    null,
                    false);
        }

        // Cleanup: Close browser after test success
        try {
            configs.browserSelector.tearDown();
        } catch (Exception e) {
            System.err.println("⚠️ Error during tearDown: " + e.getMessage());
        }
    }

    public void onTestFailure(ITestResult result) {
        try {
            // Ensure extentTest is initialized
            if (extentTest.get() == null) {
                System.err.println("⚠️ ExtentTest not initialized in onTestStart, initializing now...");
                onTestStart(result);
            }

            // Check if this test will be retried
            Boolean isRetry = (Boolean) result.getAttribute("isRetry");
            boolean willRetry = (isRetry != null && isRetry);

            if (willRetry) {
                // Intermediate retry — append a brief note to the SAME node (not a new entry).
                // The final attempt will overwrite the node status to PASS or FAIL.
                Integer attempt = (Integer) result.getAttribute("retryCount");
                int attemptNum = (attempt != null) ? attempt : 1;
                System.out.println(
                        "🔄 Test failed but will be retried (attempt " + attemptNum + "): " + result.getName());
                extentTest.get().log(Status.WARNING,
                        "Retry attempt " + attemptNum + " failed – retrying…");
                // Don't record to JIRA or AI framework yet; don't take screenshot.
                return;
            }

            // Check if retry was exhausted
            Boolean retryExhausted = (Boolean) result.getAttribute("retryExhausted");
            if (retryExhausted != null && retryExhausted) {
                System.out.println("❌ Test failed permanently after retries: " + result.getName());
            }

            extentTest.get().log(Status.FAIL, "Test Failed");
            extentTest.get().fail(result.getThrowable());

            // Record execution in AI Framework
            long duration = result.getEndMillis() - result.getStartMillis();
            AITestFramework.TestExecutionRecord record = new AITestFramework.TestExecutionRecord(result.getName(),
                    "FAIL", duration);
            record.failureReason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown";
            AITestFramework.recordExecution(record);

            // Check if page is available (should be set by browserSelector.setUp())
            System.out.println("🔍 Screenshot Debug - Checking page instance...");
            System.out.println("   - Page from listener context: " + (page != null ? "NOT NULL" : "NULL"));

            if (page == null) {
                System.err.println("⚠️ Page instance is null - attempting multiple retrieval strategies");

                // Strategy 1: Get from test instance field
                Object obj = result.getInstance();
                Class<?> cl = result.getTestClass().getRealClass();
                System.out.println("   - Test class: " + cl.getName());

                try {
                    java.lang.reflect.Field pageField = cl.getDeclaredField("page");
                    pageField.setAccessible(true); // CRITICAL: Make field accessible even if private/protected
                    page = (com.microsoft.playwright.Page) pageField.get(obj);
                    System.out.println("✅ Retrieved page instance from test class field");
                } catch (NoSuchFieldException e) {
                    System.out.println("   - No 'page' field in test class, trying parent class...");

                    // Strategy 2: Get from parent class (e.g., browserSelector extends base)
                    try {
                        java.lang.reflect.Field pageField = cl.getSuperclass().getDeclaredField("page");
                        pageField.setAccessible(true);
                        page = (com.microsoft.playwright.Page) pageField.get(obj);
                        System.out.println("✅ Retrieved page instance from parent class field");
                    } catch (Exception ex) {
                        System.out.println("   - No 'page' field in parent class, trying static base.page...");

                        // Strategy 3: Access static page from base class directly
                        try {
                            Class<?> baseClass = Class.forName("configs.base");
                            java.lang.reflect.Field staticPageField = baseClass.getDeclaredField("page");
                            staticPageField.setAccessible(true);
                            page = (com.microsoft.playwright.Page) staticPageField.get(null); // null for static field
                            System.out.println("✅ Retrieved static page instance from base class");
                        } catch (Exception ex2) {
                            System.err.println("❌ All retrieval strategies failed: " + ex2.getMessage());
                            page = null;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error accessing page field: " + e.getMessage());
                    e.printStackTrace();
                    page = null;
                }
            }

            // Final diagnostic check
            if (page != null) {
                System.out.println("✅ Page instance is available");

                // CRITICAL FIX: Update the static base.page field so getScreenShotPath() can
                // access it
                // The listener's local 'page' variable won't help utils.getScreenShotPath()
                // because that method accesses the static 'page' field from base class
                try {
                    Class<?> baseClass = Class.forName("configs.base");
                    java.lang.reflect.Field staticPageField = baseClass.getDeclaredField("page");
                    staticPageField.setAccessible(true);
                    staticPageField.set(null, page); // Set the static field
                    System.out.println("✅ Updated static base.page field for screenshot capture");
                } catch (Exception e) {
                    System.err.println("⚠️ Warning: Could not update static base.page field: " + e.getMessage());
                    // Continue anyway - the local page variable might work
                }

                try {
                    boolean isClosed = page.isClosed();
                    System.out.println("   - Page closed status: " + isClosed);
                    if (isClosed) {
                        System.err.println("❌ CRITICAL: Page is already closed! Cannot take screenshot.");
                        page = null; // Set to null so we don't try to use closed page
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error checking page status: " + e.getMessage());
                    page = null;
                }
            } else {
                System.err.println("❌ FATAL: Could not retrieve page instance - all strategies failed");
            }

            // Take screenshot for JIRA (only if page is available)
            File screenshotFile = null;
            if (page != null) {
                try {
                    System.out.println("📸 Attempting to capture failure screenshot...");
                    String screenshotPath = getScreenShotPath(result.getName());

                    if (screenshotPath != null) {
                        screenshotFile = new File(screenshotPath);

                        // Wait a moment for the filesystem to flush the write
                        Thread.sleep(500);

                        if (screenshotFile.exists() && screenshotFile.length() > 0) {
                            // Embed as Base64 so the image renders in the report regardless
                            // of where the report HTML file is opened (avoids broken Windows
                            // absolute-path img src issues).
                            byte[] imageBytes = java.nio.file.Files.readAllBytes(screenshotFile.toPath());
                            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                            extentTest.get().addScreenCaptureFromBase64String(base64, "Failure Screenshot");

                            System.out.println("✅ Screenshot captured and embedded in report:");
                            System.out.println("   - Path: " + screenshotFile.getAbsolutePath());
                            System.out.println("   - Size: " + screenshotFile.length() + " bytes");
                        } else {
                            System.err.println("❌ Screenshot file not created or empty!");
                            System.err.println("   - Expected path: " + screenshotPath);
                            System.err.println("   - File exists: " + screenshotFile.exists());
                            screenshotFile = null;
                        }
                    } else {
                        System.out.println("ℹ️  Screenshot path is null (check logs above for reason)");
                        Boolean screenshotsEnabled = Boolean.parseBoolean(
                                loadProps.getProperty(loadProps.PropKeys.SCREENSHOTS_MODE));
                        if (!screenshotsEnabled) {
                            extentTest.get()
                                    .info("📝 Note: Screenshot not attached (Screenshots_Mode=false in configuration)");
                        } else {
                            extentTest.get()
                                    .warning("⚠️ Screenshot could not be captured (see console logs for details)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Exception during screenshot capture:");
                    System.err.println("   - Error: " + e.getMessage());
                    e.printStackTrace();
                    extentTest.get().warning("⚠️ Screenshot capture failed: " + e.getMessage());
                    screenshotFile = null;
                }
            } else {
                System.err.println("❌ Cannot take screenshot - page instance is null (browser might have closed)");
                extentTest.get()
                        .warning("⚠️ Screenshot unavailable - browser was closed before screenshot could be taken");
            }

            // Jira bug handling
            String issueKey = getDynamicIssueKey(result);
            String summary = "Automation Test Failed - " + result.getMethod().getMethodName();
            String description = result.getThrowable() != null
                    ? result.getThrowable().toString().replace("\"", "'").replace("\n", "\\n")
                    : "No stack trace";

            System.out.println("🎫 JIRA Integration for failed test:");
            System.out.println("   - Issue Key: " + (issueKey != null ? issueKey : "NOT MAPPED"));
            System.out.println("   - JIRA Enabled: " + isJiraEnabled);
            System.out.println("   - Screenshot File: " + (screenshotFile != null ? screenshotFile.getName() : "NULL"));

            if (isJiraEnabled) {
                if (issueKey != null) {
                    // If JIRA integration is enabled, log the result in JIRA
                    jiraClient.updateIssueWithTestResult(
                            issueKey,
                            summary,
                            description,
                            screenshotFile,
                            true);
                } else {
                    jiraClient.createBugInJIRA(summary, description, screenshotFile);
                    System.out.println("⚠️ No JIRA IssueKey provided or mapped. Creating New Jira Issue");
                }
            }

            // CRITICAL: Close browser AFTER screenshot capture
            // This ensures screenshot is taken while browser is still open
            try {
                configs.browserSelector.tearDown();
            } catch (Exception e) {
                System.err.println("⚠️ Error during tearDown: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error in onTestFailure: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void onTestSkipped(ITestResult result) {
        try {
            // Ensure extentTest is initialized
            if (extentTest.get() == null) {
                System.err.println("⚠️ ExtentTest not initialized for skipped test, initializing now...");
                onTestStart(result);
            }

            // Check if this is a retry (test was skipped due to retry mechanism)
            if (result.wasRetried()) {
                System.out.println("🔄 Test was retried and skipped: " + result.getName());
                // Don't log skipped retries to avoid cluttering the report
                return;
            }

            extentTest.get().skip(result.getTestName());
            extentTest.get().log(Status.SKIP, "Test Skipped: " +
                    (result.getThrowable() != null ? result.getThrowable().getMessage() : "No reason provided"));

            // Try to capture screenshot if the browser page is still open.
            // Embeds as Base64 (same approach as onTestFailure) to avoid broken
            // absolute-path img src in the report HTML.
            if (page != null) {
                try {
                    String screenshotPath = getScreenShotPath(result.getName());
                    if (screenshotPath != null) {
                        java.io.File ssFile = new java.io.File(screenshotPath);
                        Thread.sleep(300); // let filesystem flush the write
                        if (ssFile.exists() && ssFile.length() > 0) {
                            byte[] imageBytes = java.nio.file.Files.readAllBytes(ssFile.toPath());
                            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                            extentTest.get().addScreenCaptureFromBase64String(base64, "Skip Screenshot");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Could not capture screenshot for skipped test: " + e.getMessage());
                }
            }

            // Cleanup: Close browser for skipped tests
            try {
                configs.browserSelector.tearDown();
            } catch (Exception e) {
                System.err.println("⚠️ Error during tearDown: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error in onTestSkipped: " + e.getMessage());
        }

    }

    public void onFinish(ITestContext context) {
        System.out.println("\n========================================");
        System.out.println("📊 Finishing Test Suite: " + context.getName());
        System.out.println("========================================");
        System.out.println("✅ Passed Tests: " + context.getPassedTests().size());
        System.out.println("❌ Failed Tests: " + context.getFailedTests().size());
        System.out.println("⏭️ Skipped Tests: " + context.getSkippedTests().size());
        System.out.println("========================================\n");

        // Flush extent reports
        try {
            if (extent != null) {
                System.out.println("📝 Flushing Extent Reports...");
                extent.flush();
                System.out.println("✅ Extent Reports flushed successfully");

                // Display report locations
                String version = loadProps.getProperty("Version").replaceAll("[()-+.^:,. ]", "");
                String baseReportPath = System.getProperty("user.dir") + "/MRITestExecutionReports/" + version
                        + "/extentReports/testNGExtentReports/";
                System.out.println("\n📊 Extent Reports Generated:");
                System.out.println("   📄 HTML Report: " + baseReportPath + "html/");
                System.out.println("   ⚡ Spark Report: " + baseReportPath + "spark/");
                System.out.println("\n💡 Open the HTML or Spark report to view test results\n");
            } else {
                System.err.println("⚠️ WARNING: Extent Reports was null - attempting to re-initialize");
                try {
                    extent = testNGExtentReporter.extentReportGenerator();
                    extent.flush();
                    System.out.println("✅ Extent Reports re-initialized and flushed");
                } catch (Exception e) {
                    System.err.println("❌ ERROR: Failed to re-initialize Extent Reports: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR flushing Extent Reports: " + e.getMessage());
            e.printStackTrace();
        }

        // Generate and display AI Framework health report
        try {
            System.out.println("\n" + AITestFramework.generateHealthReport());
        } catch (Exception e) {
            System.err.println("⚠️ Error generating AI health report: " + e.getMessage());
        }

        // Check for flaky tests and alert
        try {
            java.util.List<AITestFramework.TestHealthStatus> flakyTests = AITestFramework.getFlakyTests();
            if (!flakyTests.isEmpty()) {
                System.err.println("\n⚠️ ALERT: " + flakyTests.size() + " flaky tests detected!");
                for (AITestFramework.TestHealthStatus test : flakyTests) {
                    System.err.println("  - " + test.testName + " (Success: " +
                            String.format("%.1f%%", test.successRate) + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error checking for flaky tests: " + e.getMessage());
        }

        // Shutdown AI framework
        try {
            AITestFramework.shutdown();
        } catch (Exception e) {
            System.err.println("⚠️ Error shutting down AI framework: " + e.getMessage());
        }

        System.out.println("\n========================================");
        System.out.println("🏁 Test Execution Complete");
        System.out.println("========================================\n");
    }

    // ... existing code ...
    private String getDynamicIssueKey(ITestResult result) {
        // Option 1: Check parameters for specific issue tags (e.g., from Cucumber
        // scenarios)
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
            case "Failed_login",
                    "Verify User is not able to access the MRI Energy application With InValid Credentials" ->
                "ECS-7";
            case "Verify User is able to access the MRI Energy application with Valid Credentials", "login" -> "ECS-8";
            default -> null; // fallback to config
        };
    }
    // ... existing code ...
    // Leave other events if not needed
}
