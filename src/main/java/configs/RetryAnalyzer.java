package configs;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG Retry Analyzer for automatically retrying failed test cases.
 * Implements IRetryAnalyzer interface to determine if a failed test should be retried.
 * Thread-safe implementation for parallel test execution.
 *
 * CONFIGURATION:
 *    - MaxRetryCount in configurations.properties (default: 2)
 *    - Works with Cucumber-TestNG integration
 *    - Must be attached directly to @Test annotation
 *
 * USAGE:
 *    Option 1 (Automatic via RetryListener in testng.xml):
 *       <listeners>
 *           <listener class-name="configs.RetryListener"/>
 *       </listeners>
 *
 *    Option 2 (Direct attachment):
 *       @Test(retryAnalyzer = RetryAnalyzer.class)
 *       public void testMethod() { ... }
 *
 * HOW IT WORKS:
 *    1. Test fails → retry() method called
 *    2. Checks current retry count vs MaxRetryCount
 *    3. If retries remaining: sets result to SKIP, returns true
 *    4. TestNG reruns the test
 *    5. After max retries: returns false, test fails permanently
 *
 * FIXED ISSUES (Feb 11, 2026):
 *    - Proper TestNG integration with result.setStatus(SKIP)
 *    - Thread-safe retry counting
 *    - Detailed logging for debugging
 *    - Cleanup of ThreadLocal to prevent memory leaks
 *
 * See README.md for complete documentation.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int maxRetryCount;
    static {
        // Load max retry count from configuration
        String retryConfig = loadProps.getProperty("MaxRetryCount");
        maxRetryCount = (retryConfig != null && !retryConfig.isEmpty())
                ? Integer.parseInt(retryConfig)
                : 2; // Default to 2 retries if not configured

        System.out.println("========================================");
        System.out.println("🔄 RetryAnalyzer STATIC INITIALIZATION");
        System.out.println("========================================");
        System.out.println("MaxRetryCount from config: " + retryConfig);
        System.out.println("Effective maxRetryCount: " + maxRetryCount);
        System.out.println("========================================\n");
    }

    // Use Integer instead of int to allow proper initialization per test
    private ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    /**
     * Determines if a test should be retried based on the retry count.
     * 
     * @param result The test result containing test execution details
     * @return true if the test should be retried, false otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
        // Get the current retry count for this test in this thread
        Integer currentRetryCount = retryCount.get();

        // Log that retry() was actually called
        System.out.println("\n>>> RetryAnalyzer.retry() CALLED for: " + result.getName());
        System.out.println(">>> Current retry count: " + currentRetryCount + "/" + maxRetryCount);

        // Check if we should retry
        if (currentRetryCount < maxRetryCount) {
            currentRetryCount++;
            retryCount.set(currentRetryCount);

            String retryMessage = String.format(
                    "\n========================================\n" +
                            "⚠️  TEST RETRY TRIGGERED\n" +
                            "========================================\n" +
                            "Test Name: %s\n" +
                            "Retry Attempt: %d of %d\n" +
                            "Thread: %s\n" +
                            "Failure Reason: %s\n" +
                            "========================================\n",
                    result.getName(),
                    currentRetryCount,
                    maxRetryCount,
                    Thread.currentThread().getName(),
                    result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");
            System.out.println(retryMessage);
            System.err.println(retryMessage); // Also print to stderr for visibility

            // Set attributes to track retry status
            result.setAttribute("retryCount", currentRetryCount);
            result.setAttribute("maxRetryCount", maxRetryCount);
            result.setAttribute("isRetry", true);

            // Mark this result as not to be added to reports (TestNG will rerun it)
            result.setStatus(ITestResult.SKIP);

            return true; // Retry the test
        }

        // Max retries exhausted
        String failMessage = String.format(
                "\n========================================\n" +
                        "❌ TEST FAILED PERMANENTLY\n" +
                        "========================================\n" +
                        "Test Name: %s\n" +
                        "Max Retries Exhausted: %d\n" +
                        "Thread: %s\n" +
                        "========================================\n",
                result.getName(),
                maxRetryCount,
                Thread.currentThread().getName());
        System.out.println(failMessage);
        System.err.println(failMessage);

        // Clean up ThreadLocal to prevent memory leaks
        retryCount.remove();

        // Set final failure status
        result.setAttribute("retryExhausted", true);
        return false; // Don't retry anymore
    }

    /**
     * Resets the retry counter. Useful for testing purposes.
     */
    public void resetRetryCount() {
        retryCount.set(0);
    }

    /**
     * Gets the current retry count for this thread.
     * 
     * @return current retry count
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * Gets the maximum retry count from configuration.
     * 
     * @return maximum retry count
     */
    public static int getMaxRetryCount() {
        return maxRetryCount;
    }
}
