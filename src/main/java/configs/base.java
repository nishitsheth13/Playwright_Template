package configs;

import com.microsoft.playwright.Page;

/**
 * Base class that holds the shared Playwright Page instance.
 * All page objects and test classes extend this to access the browser page.
 * 
 * @author Nishit Sheth
 * @version 1.0
 */
public class base {

    /**
     * Static Page instance shared across all tests.
     * Note: For parallel execution, consider using ThreadLocal<Page>
     */
    public static Page page;

    /**
     * Protected constructor to prevent instantiation
     */
    protected base() {
        // Prevent instantiation
    }
}








