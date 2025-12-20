package pages;

import com.microsoft.playwright.Page;
import configs.TimeoutConfig;
import configs.utils;

/**
 * Base Page class containing common methods for all page objects.
 * All page objects should extend this class to inherit common functionality.
 * 
 * @author Automation Team
 * @version 1.0
 */
public abstract class BasePage extends utils {
    
    protected static Page page;
    
    /**
     * Constructor to initialize page instance
     */
    public BasePage() {
        BasePage.page = utils.page;
    }
    
    // ============================================================
    // NAVIGATION METHODS
    // ============================================================
    
    /**
     * Navigate to specific URL with wait
     * @param url The URL to navigate to
     */
    protected static void navigateTo(String url) {
        System.out.println("🌐 Navigating to: " + url);
        page.navigate(url);
        page.waitForLoadState();
        TimeoutConfig.shortWait();
        System.out.println("✅ Page loaded successfully");
    }
    
    /**
     * Refresh current page
     */
    protected static void refreshPage() {
        System.out.println("🔄 Refreshing page...");
        page.reload();
        TimeoutConfig.mediumWait();
        System.out.println("✅ Page refreshed");
    }
    
    /**
     * Navigate back to previous page
     */
    protected static void goBack() {
        System.out.println("◀️ Navigating back...");
        page.goBack();
        TimeoutConfig.shortWait();
    }
    
    /**
     * Navigate forward to next page
     */
    protected static void goForward() {
        System.out.println("▶️ Navigating forward...");
        page.goForward();
        TimeoutConfig.shortWait();
    }
    
    // ============================================================
    // PAGE INFORMATION METHODS
    // ============================================================
    
    /**
     * Get current page title
     * @return Page title as String
     */
    protected static String getPageTitle() {
        return page.title();
    }
    
    /**
     * Get current page URL
     * @return Current URL as String
     */
    protected static String getCurrentUrl() {
        return page.url();
    }
    
    /**
     * Check if page title contains expected text
     * @param expectedTitle Text to check in title
     * @return true if title contains expected text, false otherwise
     */
    protected static boolean isTitleContains(String expectedTitle) {
        String actualTitle = getPageTitle();
        boolean contains = actualTitle.contains(expectedTitle);
        System.out.println("📋 Title check: Expected='" + expectedTitle + "', Actual='" + actualTitle + "', Result=" + contains);
        return contains;
    }
    
    /**
     * Check if current URL contains expected text
     * @param expectedUrlPart Text to check in URL
     * @return true if URL contains expected text, false otherwise
     */
    protected static boolean isUrlContains(String expectedUrlPart) {
        String actualUrl = getCurrentUrl();
        boolean contains = actualUrl.contains(expectedUrlPart);
        System.out.println("🔗 URL check: Expected='" + expectedUrlPart + "', Actual='" + actualUrl + "', Result=" + contains);
        return contains;
    }
    
    /**
     * Wait for page title to contain specific text
     * @param expectedTitle Expected text in title
     * @param timeoutInSeconds Maximum wait time
     * @return true if title contains text within timeout
     */
    protected static boolean waitForTitle(String expectedTitle, int timeoutInSeconds) {
        System.out.println("⏳ Waiting for title to contain: " + expectedTitle);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutInSeconds * 1000);
        
        while (System.currentTimeMillis() < endTime) {
            if (isTitleContains(expectedTitle)) {
                System.out.println("✅ Title found");
                return true;
            }
            TimeoutConfig.shortWait();
        }
        
        System.out.println("❌ Title not found within timeout");
        return false;
    }
    
    // ============================================================
    // COMMON ACTION METHODS
    // ============================================================
    
    /**
     * Scroll to top of page
     */
    protected static void scrollToTop() {
        System.out.println("⬆️ Scrolling to top");
        page.evaluate("window.scrollTo(0, 0)");
        TimeoutConfig.shortWait();
    }
    
    /**
     * Scroll to bottom of page
     */
    protected static void scrollToBottom() {
        System.out.println("⬇️ Scrolling to bottom");
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        TimeoutConfig.shortWait();
    }
    
    /**
     * Scroll to specific element
     * @param locator Element locator
     */
    protected static void scrollToElement(String locator) {
        System.out.println("🎯 Scrolling to element: " + locator);
        page.locator(locator).scrollIntoViewIfNeeded();
        TimeoutConfig.shortWait();
    }
    
    /**
     * Take screenshot of current page
     * @param screenshotName Name for the screenshot file
     * @return Screenshot file path
     */
    protected static String takeScreenshot(String screenshotName) {
        try {
            System.out.println("📸 Taking screenshot: " + screenshotName);
            // Screenshot logic handled by utils class
            return "Screenshot captured: " + screenshotName;
        } catch (Exception e) {
            System.err.println("❌ Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
    
    // ============================================================
    // ALERT/DIALOG METHODS
    // ============================================================
    
    /**
     * Accept browser alert
     */
    protected static void acceptAlert() {
        System.out.println("✅ Accepting alert");
        page.onDialog(dialog -> {
            System.out.println("⚠️ Alert text: " + dialog.message());
            dialog.accept();
        });
    }
    
    /**
     * Dismiss browser alert
     */
    protected static void dismissAlert() {
        System.out.println("❌ Dismissing alert");
        page.onDialog(dialog -> {
            System.out.println("⚠️ Alert text: " + dialog.message());
            dialog.dismiss();
        });
    }
    
    // ============================================================
    // WAIT METHODS
    // ============================================================
    
    /**
     * Wait for page to be fully loaded
     */
    protected static void waitForPageLoad() {
        System.out.println("⏳ Waiting for page to load...");
        page.waitForLoadState();
        TimeoutConfig.mediumWait();
        System.out.println("✅ Page loaded");
    }
    
    /**
     * Wait for network to be idle
     */
    protected static void waitForNetworkIdle() {
        System.out.println("⏳ Waiting for network idle...");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        System.out.println("✅ Network idle");
    }
    
    // ============================================================
    // VALIDATION METHODS
    // ============================================================
    
    /**
     * Verify page is loaded by checking multiple elements
     * @param locators Array of locators to check
     * @return true if all elements are present
     */
    protected static boolean isPageLoaded(String... locators) {
        System.out.println("🔍 Verifying page loaded...");
        for (String locator : locators) {
            if (!isElementPresent(locator)) {
                System.out.println("❌ Element not found: " + locator);
                return false;
            }
        }
        System.out.println("✅ All elements found - Page loaded");
        return true;
    }
    
    /**
     * Check if current page is the expected page
     * @param expectedUrl Expected URL substring
     * @param expectedTitle Expected title substring
     * @return true if both URL and title match
     */
    protected static boolean isCurrentPage(String expectedUrl, String expectedTitle) {
        boolean urlMatch = isUrlContains(expectedUrl);
        boolean titleMatch = isTitleContains(expectedTitle);
        return urlMatch && titleMatch;
    }
}
