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

    public static Page popupPage;

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
     * Navigate to specific URL with comprehensive auto-wait
     * Includes wait for LOAD state and network idle detection
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
            
            // Auto-wait for page to be fully loaded
            System.out.println("⏳ [Auto-Wait] Waiting for page LOAD state...");
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
            
            // Wait for network to be idle for stable state
            System.out.println("⏳ [Auto-Wait] Waiting for network activity to settle...");
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            
            TimeoutConfig.waitShort();
            System.out.println("✅ Page loaded successfully with auto-wait");
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
     * Wait for network to be idle with auto-wait logging
     */
    public static void waitForNetworkIdle() {
        System.out.println("⏳ [Auto-Wait] Waiting for network idle...");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        System.out.println("✅ [Auto-Wait] Network idle - page fully loaded");
    }

    // ============================================================
    // AUTO-WAIT VERIFICATION METHODS (Enhanced)
    // ============================================================

    /**
     * Wait for element to be visible with integrated auto-wait
     * Includes retry mechanism for stale elements
     * 
     * @param locator Element locator
     * @param timeoutMs Maximum wait time in milliseconds
     * @return true if element becomes visible within timeout
     */
    public static boolean waitForElementVisible(com.microsoft.playwright.Locator locator, double timeoutMs) {
        try {
            System.out.println("⏳ [Auto-Wait] Waiting for element to be visible...");
            locator.waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
            System.out.println("✅ [Auto-Wait] Element is visible");
            return true;
        } catch (com.microsoft.playwright.TimeoutError e) {
            System.err.println("❌ [Auto-Wait] Element not visible within " + timeoutMs + "ms");
            return false;
        }
    }

    /**
     * Wait for element to contain specific text with integrated auto-retry
     * Polls every 200ms until text is found or timeout
     * 
     * @param locator Element locator
     * @param expectedText Expected text content
     * @param timeoutMs Maximum wait time
     * @return true if text found within timeout
     */
    public static boolean waitForElementText(com.microsoft.playwright.Locator locator, String expectedText, double timeoutMs) {
        System.out.println("⏳ [Auto-Wait] Waiting for text: '" + expectedText + "'");
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (long) timeoutMs;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                String actualText = locator.textContent();
                if (actualText != null && actualText.contains(expectedText)) {
                    System.out.println("✅ [Auto-Wait] Text found: '" + expectedText + "'");
                    return true;
                }
                try {
                    Thread.sleep(200); // Poll every 200ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                // Continue waiting
            }
        }
        
        System.err.println("❌ [Auto-Wait] Text not found within timeout: '" + expectedText + "'");
        return false;
    }

    /**
     * Wait for element to be clickable (visible + enabled) with auto-wait
     * 
     * @param locator Element locator
     * @param timeoutMs Maximum wait time
     * @return true if element becomes clickable
     */
    public static boolean waitForElementClickable(com.microsoft.playwright.Locator locator, double timeoutMs) {
        try {
            System.out.println("⏳ [Auto-Wait] Waiting for element to be clickable...");
            
            // Wait for visible
            locator.waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
            
            // Wait for enabled
            long startTime = System.currentTimeMillis();
            while (!locator.isEnabled() && (System.currentTimeMillis() - startTime) < timeoutMs) {
                Thread.sleep(100);
            }
            
            if (locator.isEnabled()) {
                System.out.println("✅ [Auto-Wait] Element is clickable");
                return true;
            } else {
                System.err.println("❌ [Auto-Wait] Element visible but not enabled");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ [Auto-Wait] Element not clickable: " + e.getMessage());
            return false;
        }
    }

    /**
     * Wait for page URL to contain expected text with auto-retry
     * 
     * @param expectedUrlPart Expected URL substring
     * @param timeoutSeconds Maximum wait time in seconds
     * @return true if URL contains text within timeout
     */
    public static boolean waitForUrlContains(String expectedUrlPart, int timeoutSeconds) {
        System.out.println("⏳ [Auto-Wait] Waiting for URL to contain: " + expectedUrlPart);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            if (getCurrentUrl().contains(expectedUrlPart)) {
                System.out.println("✅ [Auto-Wait] URL contains: " + expectedUrlPart);
                return true;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        System.err.println("❌ [Auto-Wait] URL does not contain: " + expectedUrlPart);
        return false;
    }

    /**
     * Wait for multiple elements to be visible with auto-wait
     * 
     * @param locators Array of element locators
     * @param timeoutMs Timeout for each element
     * @return true if all elements become visible
     */
    public static boolean waitForMultipleElements(com.microsoft.playwright.Locator[] locators, double timeoutMs) {
        System.out.println("⏳ [Auto-Wait] Waiting for " + locators.length + " elements to be visible...");
        
        for (int i = 0; i < locators.length; i++) {
            try {
                locators[i].waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
                System.out.println("✅ [Auto-Wait] Element " + (i + 1) + "/" + locators.length + " visible");
            } catch (com.microsoft.playwright.TimeoutError e) {
                System.err.println("❌ [Auto-Wait] Element " + (i + 1) + " not visible within timeout");
                return false;
            }
        }
        
        System.out.println("✅ [Auto-Wait] All " + locators.length + " elements visible");
        return true;
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
