package configs;

/**
 * Centralized timeout configuration utility.
 * Manages all timeout values used across the framework.
 * 
 * All timeout values are loaded from configurations.properties file.
 * Default values are provided as fallback if properties are not configured.
 */
public class TimeoutConfig {
    
    // Timeout values in milliseconds
    private static final int DEFAULT_TIMEOUT;
    private static final int PAGE_LOAD_TIMEOUT;
    private static final int ELEMENT_WAIT_TIMEOUT;
    private static final int IMPLICIT_WAIT_TIMEOUT;
    
    static {
        // Load timeout configurations from properties file
        DEFAULT_TIMEOUT = getTimeoutFromConfig("DefaultTimeout", 60000);
        PAGE_LOAD_TIMEOUT = getTimeoutFromConfig("PageLoadTimeout", 60000);
        ELEMENT_WAIT_TIMEOUT = getTimeoutFromConfig("ElementWaitTimeout", 10000);
        IMPLICIT_WAIT_TIMEOUT = getTimeoutFromConfig("ImplicitWaitTimeout", 5000);
        
        System.out.println("⏱️ Timeout Configuration Initialized:");
        System.out.println("   • Default Timeout: " + DEFAULT_TIMEOUT + "ms");
        System.out.println("   • Page Load Timeout: " + PAGE_LOAD_TIMEOUT + "ms");
        System.out.println("   • Element Wait Timeout: " + ELEMENT_WAIT_TIMEOUT + "ms");
        System.out.println("   • Implicit Wait Timeout: " + IMPLICIT_WAIT_TIMEOUT + "ms");
    }
    
    /**
     * Helper method to read timeout from configuration with fallback.
     */
    private static int getTimeoutFromConfig(String key, int defaultValue) {
        String value = loadProps.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Invalid timeout value for " + key + ": " + value);
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // Getter methods
    
    /**
     * Gets the default timeout for general operations.
     * @return default timeout in milliseconds
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }
    
    /**
     * Gets the timeout for page load operations.
     * @return page load timeout in milliseconds
     */
    public static int getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }
    
    /**
     * Gets the timeout for waiting for elements.
     * @return element wait timeout in milliseconds
     */
    public static int getElementWaitTimeout() {
        return ELEMENT_WAIT_TIMEOUT;
    }
    
    /**
     * Gets the implicit wait timeout.
     * @return implicit wait timeout in milliseconds
     */
    public static int getImplicitWaitTimeout() {
        return IMPLICIT_WAIT_TIMEOUT;
    }
    
    // Conversion utilities
    
    /**
     * Converts milliseconds to seconds.
     * @param milliseconds time in milliseconds
     * @return time in seconds
     */
    public static int toSeconds(int milliseconds) {
        return milliseconds / 1000;
    }
    
    /**
     * Gets default timeout in seconds.
     * @return default timeout in seconds
     */
    public static int getDefaultTimeoutInSeconds() {
        return toSeconds(DEFAULT_TIMEOUT);
    }
    
    /**
     * Gets page load timeout in seconds.
     * @return page load timeout in seconds
     */
    public static int getPageLoadTimeoutInSeconds() {
        return toSeconds(PAGE_LOAD_TIMEOUT);
    }
    
    /**
     * Gets element wait timeout in seconds.
     * @return element wait timeout in seconds
     */
    public static int getElementWaitTimeoutInSeconds() {
        return toSeconds(ELEMENT_WAIT_TIMEOUT);
    }
    
    // Custom wait utilities
    
    /**
     * Wait for a short duration (500ms).
     */
    public static void shortWait() {
        waitFor(500);
    }
    
    /**
     * Wait for a medium duration (1000ms).
     */
    public static void mediumWait() {
        waitFor(1000);
    }
    
    /**
     * Wait for a long duration (2000ms).
     */
    public static void longWait() {
        waitFor(2000);
    }
    
    /**
     * Generic wait method.
     * @param milliseconds time to wait in milliseconds
     */
    public static void waitFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠️ Wait interrupted: " + e.getMessage());
        }
    }
}
