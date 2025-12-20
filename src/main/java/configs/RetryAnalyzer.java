package configs;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG Retry Analyzer for automatically retrying failed test cases.
 * Implements IRetryAnalyzer interface to determine if a failed test should be retried.
 * 
 * Usage: Add @Test(retryAnalyzer = RetryAnalyzer.class) to test methods
 * Or configure globally via listener in testng.xml
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    
    private int retryCount = 0;
    private static int maxRetryCount;
    
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
        if (retryCount < maxRetryCount) {
            retryCount++;
            String retryMessage = String.format(
                "⚠️ Test Failed: %s | Retry attempt: %d of %d",
                result.getName(),
                retryCount, 
                maxRetryCount
            );
            System.out.println(retryMessage);
            
            result.setAttribute("retryCount", retryCount);
            result.setAttribute("maxRetryCount", maxRetryCount);
            
            return true;
        }
        
        System.out.println("❌ Test Failed permanently: " + result.getName() 
                         + " | Max retries (" + maxRetryCount + ") exhausted");
        return false;
    }
    
    /**
     * Resets the retry counter. Useful for testing purposes.
     */
    public void resetRetryCount() {
        this.retryCount = 0;
    }
    
    /**
     * Gets the current retry count.
     * @return current retry count
     */
    public int getRetryCount() {
        return retryCount;
    }
    
    /**
     * Gets the maximum retry count from configuration.
     * @return maximum retry count
     */
    public static int getMaxRetryCount() {
        return maxRetryCount;
    }
}
