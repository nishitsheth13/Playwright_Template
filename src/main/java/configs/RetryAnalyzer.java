package configs;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG Retry Analyzer for automatically retrying failed test cases.
 * Implements IRetryAnalyzer interface to determine if a failed test should be retried.
 * Thread-safe implementation for parallel test execution.
 *
 * Usage: Add @Test(retryAnalyzer = RetryAnalyzer.class) to test methods
 * Or configure globally via listener in testng.xml
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int maxRetryCount;
    private final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    static {
        // Load max retry count from configuration
        String retryConfig = loadProps.getProperty("MaxRetryCount");
        maxRetryCount = (retryConfig != null && !retryConfig.isEmpty()) 
                        ? Integer.parseInt(retryConfig) 
                        : 2; // Default to 2 retries if not configured
        
        System.out.println("🔄 RetryAnalyzer initialized with maxRetryCount: " + maxRetryCount);
    }
    
    /**
     * Determines if a test should be retried based on the retry count.
     * 
     * @param result The test result containing test execution details
     * @return true if the test should be retried, false otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
        int currentRetryCount = retryCount.get();

        if (currentRetryCount < maxRetryCount) {
            currentRetryCount++;
            retryCount.set(currentRetryCount);

            String retryMessage = String.format(
                    "⚠️ Test Failed: %s | Retry attempt: %d of %d | Thread: %s",
                result.getName(),
                    currentRetryCount,
                    maxRetryCount,
                    Thread.currentThread().getName()
            );
            System.out.println(retryMessage);

            result.setAttribute("retryCount", currentRetryCount);
            result.setAttribute("maxRetryCount", maxRetryCount);
            result.setAttribute("isRetry", true);

            return true;
        }

        String failMessage = String.format(
                "❌ Test Failed permanently: %s | Max retries (%d) exhausted | Thread: %s",
                result.getName(),
                maxRetryCount,
                Thread.currentThread().getName()
        );
        System.out.println(failMessage);

        // Clean up ThreadLocal to prevent memory leaks
        retryCount.remove();
        return false;
    }
    
    /**
     * Resets the retry counter. Useful for testing purposes.
     */
    public void resetRetryCount() {
        retryCount.set(0);
    }
    
    /**
     * Gets the current retry count for this thread.
     * @return current retry count
     */
    public int getRetryCount() {
        return retryCount.get();
    }
    
    /**
     * Gets the maximum retry count from configuration.
     * @return maximum retry count
     */
    public static int getMaxRetryCount() {
        return maxRetryCount;
    }
}
