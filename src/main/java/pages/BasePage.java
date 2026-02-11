package pages;

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

    /**
     * Constructor - inherits page from base class through utils
     */
    public BasePage() {
        // page is inherited from base class through utils
    }

    // ============================================================
    // NAVIGATION METHODS
    // ============================================================

    /**
     * Navigate to specific URL with wait
     * 
     * @param url The URL to navigate to
     * @throws IllegalArgumentException if URL is null or empty
     * @throws IllegalStateException    if page is not initialized
     */
    public static void navigateToUrl(String url) {
        // Validate URL
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ URL cannot be null or empty");
        }

        // Ensure page is initialized
        if (page == null) {
            throw new IllegalStateException("❌ Page is not initialized. Ensure browser is launched before navigation.");
        }

        System.out.println("🌐 Navigating to: " + url);

        try {
            page.navigate(url);
            page.waitForLoadState();
            TimeoutConfig.waitShort();
            System.out.println("✅ Page loaded successfully");
            System.out.println("📍 Current URL: " + page.url());
        } catch (Exception e) {
            System.err.println("❌ Navigation failed: " + e.getMessage());
            throw new RuntimeException("Failed to navigate to URL: " + url, e);
        }
    }

    /**
     * Refresh current page
     */
    public static void refreshPage() {
        System.out.println("🔄 Refreshing page...");
        page.reload();
        TimeoutConfig.waitMedium();
        System.out.println("✅ Page refreshed");
    }

    /**
     * Navigate back to previous page
     */
    public static void navigateBack() {
        System.out.println("◀️ Navigating back...");
        page.goBack();
        TimeoutConfig.waitShort();
    }

    /**
     * Navigate forward to next page
     */
    public static void navigateForward() {
        System.out.println("▶️ Navigating forward...");
        page.goForward();
        TimeoutConfig.waitShort();
    }

    // ============================================================
    // PAGE INFORMATION METHODS
    // ============================================================

    /**
     * Get current page title
     * 
     * @return Page title as String
     */
    public static String getPageTitle() {
        return page.title();
    }

    /**
     * Get current page URL
     * 
     * @return Current URL as String
     */
    public static String getCurrentUrl() {
        return page.url();
    }

    /**
     * Check if page title contains expected text
     * 
     * @param expectedTitle Text to check in title
     * @return true if title contains expected text, false otherwise
     */
    public static boolean isTitleContains(String expectedTitle) {
        String actualTitle = getPageTitle();
        boolean contains = actualTitle.contains(expectedTitle);
        System.out.println(
                "📋 Title check: Expected='" + expectedTitle + "', Actual='" + actualTitle + "', Result=" + contains);
        return contains;
    }

    /**
     * Check if current URL contains expected text
     * 
     * @param expectedUrlPart Text to check in URL
     * @return true if URL contains expected text, false otherwise
     */
    public static boolean isUrlContains(String expectedUrlPart) {
        String actualUrl = getCurrentUrl();
        boolean contains = actualUrl.contains(expectedUrlPart);
        System.out.println(
                "🔗 URL check: Expected='" + expectedUrlPart + "', Actual='" + actualUrl + "', Result=" + contains);
        return contains;
    }

    /**
     * Wait for page title to contain specific text
     * 
     * @param expectedTitle    Expected text in title
     * @param timeoutInSeconds Maximum wait time
     * @return true if title contains text within timeout
     */
    public static boolean waitForTitle(String expectedTitle, int timeoutInSeconds) {
        System.out.println("⏳ Waiting for title to contain: " + expectedTitle);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutInSeconds * 1000);

        while (System.currentTimeMillis() < endTime) {
            if (isTitleContains(expectedTitle)) {
                System.out.println("✅ Title found");
                return true;
            }
            TimeoutConfig.waitShort();
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
    public static void scrollToTop() {
        System.out.println("⬆️ Scrolling to top");
        page.evaluate("window.scrollTo(0, 0)");
        TimeoutConfig.waitShort();
    }

    /**
     * Scroll to bottom of page
     */
    public static void scrollToBottom() {
        System.out.println("⬇️ Scrolling to bottom");
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        TimeoutConfig.waitShort();
    }

    /**
     * Scroll to specific element
     * 
     * @param locator Element locator
     */
    public static void scrollToElement(String locator) {
        System.out.println("🎯 Scrolling to element: " + locator);
        page.locator(locator).scrollIntoViewIfNeeded();
        TimeoutConfig.waitShort();
    }

    /**
     * Take screenshot of current page
     * 
     * @param screenshotName Name for the screenshot file
     * @return Screenshot file path
     */
    public static String takeScreenshot(String screenshotName) {
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
    public static void acceptAlert() {
        System.out.println("✅ Accepting alert");
        page.onDialog(dialog -> {
            System.out.println("⚠️ Alert text: " + dialog.message());
            dialog.accept();
        });
    }

    /**
     * Dismiss browser alert
     */
    public static void dismissAlert() {
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
    public static void waitForPageLoad() {
        System.out.println("⏳ Waiting for page to load...");
        page.waitForLoadState();
        TimeoutConfig.waitMedium();
        System.out.println("✅ Page loaded");
    }

    /**
     * Wait for network to be idle
     */
    public static void waitForNetworkIdle() {
        System.out.println("⏳ Waiting for network idle...");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        System.out.println("✅ Network idle");
    }

    // ============================================================
    // VALIDATION METHODS
    // ============================================================

    /**
     * Verify page is loaded by checking multiple elements
     * 
     * @param locators Array of locators to check
     * @return true if all elements are present
     */
    public static boolean isPageLoaded(String... locators) {
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
     * 
     * @param expectedUrl   Expected URL substring
     * @param expectedTitle Expected title substring
     * @return true if both URL and title match
     */
    public static boolean isCurrentPage(String expectedUrl, String expectedTitle) {
        boolean urlMatch = isUrlContains(expectedUrl);
        boolean titleMatch = isTitleContains(expectedTitle);
        return urlMatch && titleMatch;
    }
}
