package configs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Smart Locator Strategy - Automatically finds elements using multiple fallback strategies
 * <p>
 * Priority Order (Most Stable → Least Stable):
 * 1. data-testid attribute (most stable for test automation)
 * 2. id attribute (stable if not dynamic)
 * 3. name attribute (common for form elements)
 * 4. placeholder text (good for inputs)
 * 5. aria-label (accessibility attribute)
 * 6. role + name combination (Playwright recommended)
 * 7. text content (for buttons, links)
 * 8. CSS class (less stable, but useful)
 * 9. XPath (last resort, most fragile)
 * <p>
 * Usage:
 * - SmartLocatorStrategy.findElement(page, strategies...)
 * - Returns the first visible element found
 * - Logs which strategy succeeded for debugging
 *
 * @author Automation Framework
 * @version 2.0 (Performance Optimized)
 */
public class SmartLocatorStrategy {

    private static final Logger log = Logger.getLogger(SmartLocatorStrategy.class.getName());
    private static final int SHORT_TIMEOUT = 500; // Reduced to 500ms for faster execution
    private static final Map<String, String> STRATEGY_CACHE = new ConcurrentHashMap<>(); // Cache successful strategies
    private static boolean ENABLE_CACHE = true; // Can be disabled for debugging

    /**
     * Find element using multiple fallback strategies with caching
     *
     * @param page       Playwright page instance
     * @param strategies Array of locator strategies to try in order
     * @return Locator for the found element
     * @throws RuntimeException if element not found with any strategy
     */
    public static Locator findElement(Page page, String... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new IllegalArgumentException("At least one strategy must be provided");
        }

        // Generate cache key from all strategies
        String cacheKey = String.join("|", strategies);

        // Check cache first for performance
        if (ENABLE_CACHE && STRATEGY_CACHE.containsKey(cacheKey)) {
            String cachedStrategy = STRATEGY_CACHE.get(cacheKey);
            try {
                Locator locator = resolveLocator(page, cachedStrategy);
                // Quick check without wait - element should be there
                if (locator.count() > 0) {
                    log.fine("🚀 Cache hit! Using strategy: " + cachedStrategy);
                    return locator.first();
                }
            } catch (Exception e) {
                // Cache miss or stale, continue with normal flow
                STRATEGY_CACHE.remove(cacheKey);
            }
        }

        List<String> failedStrategies = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < strategies.length; i++) {
            String strategy = strategies[i];
            try {
                Locator locator = resolveLocator(page, strategy);

                // Fast check: if element count > 0, it exists
                if (locator.count() > 0) {
                    // Only wait for visibility if element exists
                    try {
                        locator.first().waitFor(new Locator.WaitForOptions()
                                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                                .setTimeout(SHORT_TIMEOUT));

                        if (locator.first().isVisible()) {
                            long duration = System.currentTimeMillis() - startTime;
                            log.info(String.format("✅ Element found using strategy #%d: %s (took %dms)",
                                    (i + 1), strategy, duration));

                            // Cache successful strategy
                            if (ENABLE_CACHE) {
                                STRATEGY_CACHE.put(cacheKey, strategy);
                            }
                            return locator.first();
                        }
                    } catch (TimeoutError e) {
                        // Element exists but not visible, continue
                        failedStrategies.add(strategy + " (not visible)");
                    }
                } else {
                    failedStrategies.add(strategy + " (not found)");
                }
            } catch (TimeoutError e) {
                failedStrategies.add(strategy + " (timeout)");
                log.fine("⏭️ Strategy timeout: " + strategy);
            } catch (Exception e) {
                failedStrategies.add(strategy + " (error: " + e.getMessage() + ")");
                log.fine("⏭️ Strategy error: " + strategy + " - " + e.getMessage());
            }
        }

        // All strategies failed
        long totalDuration = System.currentTimeMillis() - startTime;
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("❌ Element not found with any of ").append(strategies.length)
                .append(" strategies (took ").append(totalDuration).append("ms).\n")
                .append("Failed strategies:\n");

        for (String failed : failedStrategies) {
            errorMsg.append("  • ").append(failed).append("\n");
        }

        log.severe(errorMsg.toString());
        throw new RuntimeException(errorMsg.toString());
    }

    /**
     * Clear the strategy cache (useful for testing or when DOM changes significantly)
     */
    public static void clearCache() {
        STRATEGY_CACHE.clear();
        log.info("🗑️ Strategy cache cleared");
    }

    /**
     * Enable or disable strategy caching
     *
     * @param enable true to enable caching, false to disable
     */
    public static void setCacheEnabled(boolean enable) {
        ENABLE_CACHE = enable;
        log.info("📦 Strategy caching " + (enable ? "enabled" : "disabled"));
    }

    /**
     * Check if element exists using any of the provided strategies (fast, no wait)
     *
     * @param page       Playwright page instance
     * @param strategies Array of locator strategies to try
     * @return true if element found and visible, false otherwise
     */
    public static boolean elementExists(Page page, String... strategies) {
        for (String strategy : strategies) {
            try {
                Locator locator = resolveLocator(page, strategy);
                // Quick check - no wait, just check if visible immediately
                if (locator.count() > 0 && locator.first().isVisible()) {
                    return true;
                }
            } catch (Exception e) {
                // Continue to next strategy
            }
        }
        return false;
    }

    /**
     * Resolve locator string to Playwright Locator
     * Supports multiple locator types with prefixes
     *
     * @param page     Playwright page instance
     * @param strategy Locator strategy string
     * @return Playwright Locator
     */
    private static Locator resolveLocator(Page page, String strategy) {
        if (strategy == null || strategy.trim().isEmpty()) {
            throw new IllegalArgumentException("Strategy cannot be null or empty");
        }

        // data-testid (highest priority)
        if (strategy.startsWith("data-testid=")) {
            String testId = strategy.substring(12);
            return page.locator("[data-testid=\"" + testId + "\"]");
        }

        // ID selector
        if (strategy.startsWith("id=")) {
            String id = strategy.substring(3);
            return page.locator("#" + id);
        }

        // Name attribute
        if (strategy.startsWith("name=")) {
            String name = strategy.substring(5);
            return page.locator("[name=\"" + name + "\"]");
        }

        // Placeholder (exact match)
        if (strategy.startsWith("placeholder=")) {
            String placeholder = strategy.substring(12);
            return page.locator("[placeholder=\"" + placeholder + "\"]");
        }

        // Placeholder (contains - case insensitive)
        if (strategy.startsWith("placeholder*=")) {
            String placeholder = strategy.substring(13);
            return page.locator("[placeholder*=\"" + placeholder + "\" i]");
        }

        // Aria-label
        if (strategy.startsWith("aria-label=")) {
            String label = strategy.substring(11);
            return page.getByLabel(label);
        }

        // Role + name combination (Playwright recommended)
        if (strategy.startsWith("role=")) {
            String[] parts = strategy.substring(5).split(",name=");
            String roleName = parts[0].toUpperCase().replace("-", "_");

            try {
                com.microsoft.playwright.options.AriaRole role =
                        com.microsoft.playwright.options.AriaRole.valueOf(roleName);

                if (parts.length > 1) {
                    return page.getByRole(role, new Page.GetByRoleOptions().setName(parts[1]));
                } else {
                    return page.getByRole(role);
                }
            } catch (IllegalArgumentException e) {
                log.warning("⚠️ Invalid ARIA role: " + roleName);
                throw e;
            }
        }

        // Text content (exact match)
        if (strategy.startsWith("text=")) {
            String text = strategy.substring(5);
            return page.getByText(text, new Page.GetByTextOptions().setExact(true));
        }

        // Text content (contains)
        if (strategy.startsWith("text*=")) {
            String text = strategy.substring(6);
            return page.getByText(text);
        }

        // Class name
        if (strategy.startsWith("class=")) {
            String className = strategy.substring(6);
            // Handle class selector properly
            if (!className.startsWith(".")) {
                className = "." + className;
            }
            return page.locator(className);
        }

        // XPath
        if (strategy.startsWith("xpath=")) {
            String xpath = strategy.substring(6);
            return page.locator(xpath);
        }

        // Default: treat as CSS selector
        return page.locator(strategy);
    }

    /**
     * Generate smart locator strategies for common input fields
     * Automatically creates fallback strategies based on field type
     *
     * @param fieldName Field name or label
     * @param fieldType Type of field (text, password, email, etc.)
     * @return Array of locator strategies
     */
    public static String[] generateInputStrategies(String fieldName, String fieldType) {
        String lowerName = fieldName.toLowerCase();
        List<String> strategies = new ArrayList<>();

        // data-testid (best practice)
        strategies.add("data-testid=" + lowerName.replace(" ", "-"));

        // ID
        strategies.add("id=" + lowerName.replace(" ", ""));
        strategies.add("id=" + lowerName.replace(" ", "-"));
        strategies.add("id=" + lowerName.replace(" ", "_"));

        // Name
        strategies.add("name=" + lowerName.replace(" ", ""));
        strategies.add("name=" + lowerName.replace(" ", "-"));
        strategies.add("name=" + lowerName.replace(" ", "_"));

        // Placeholder
        strategies.add("placeholder*=" + fieldName);
        strategies.add("placeholder*=Enter " + fieldName);
        strategies.add("placeholder*=" + fieldName.split(" ")[0]); // First word

        // Aria-label
        strategies.add("aria-label=" + fieldName);

        // Role + name
        strategies.add("role=textbox,name=" + fieldName);

        // Type-specific selectors
        if (fieldType != null && !fieldType.isEmpty()) {
            strategies.add("input[type=\"" + fieldType + "\"]");

            // Autocomplete attribute
            if (fieldType.equals("password")) {
                strategies.add("input[autocomplete=\"current-password\"]");
                strategies.add("input[autocomplete=\"new-password\"]");
            } else if (lowerName.contains("username") || lowerName.contains("email")) {
                strategies.add("input[autocomplete=\"username\"]");
                strategies.add("input[autocomplete=\"email\"]");
            }
        }

        // XPath fallbacks
        strategies.add("xpath=//input[contains(@placeholder, \"" + fieldName + "\")]");
        strategies.add("xpath=//input[contains(@name, \"" + lowerName.replace(" ", "") + "\")]");
        strategies.add("xpath=//label[contains(text(), \"" + fieldName + "\")]//following::input[1]");

        return strategies.toArray(new String[0]);
    }

    /**
     * Generate smart locator strategies for buttons
     *
     * @param buttonText Button text or label
     * @return Array of locator strategies
     */
    public static String[] generateButtonStrategies(String buttonText) {
        String lowerText = buttonText.toLowerCase();
        List<String> strategies = new ArrayList<>();

        // data-testid
        strategies.add("data-testid=" + lowerText.replace(" ", "-") + "-button");
        strategies.add("data-testid=" + lowerText.replace(" ", "-") + "-btn");

        // ID
        strategies.add("id=" + lowerText.replace(" ", "") + "Btn");
        strategies.add("id=" + lowerText.replace(" ", "") + "Button");
        strategies.add("id=" + lowerText.replace(" ", "-") + "-btn");

        // Name
        strategies.add("name=" + lowerText.replace(" ", ""));

        // Role + name (Playwright recommended)
        strategies.add("role=button,name=" + buttonText);

        // Text content
        strategies.add("text=" + buttonText);
        strategies.add("text*=" + buttonText);

        // Button with type
        strategies.add("button[type=\"submit\"]");

        // Class-based (common patterns)
        strategies.add("class=btn-" + lowerText.replace(" ", "-"));
        strategies.add("class=" + lowerText.replace(" ", "-") + "-button");

        // XPath fallbacks
        strategies.add("xpath=//button[contains(text(), \"" + buttonText + "\")]");
        strategies.add("xpath=//button[contains(@value, \"" + buttonText + "\")]");
        strategies.add("xpath=//input[@type=\"button\" and contains(@value, \"" + buttonText + "\")]");
        strategies.add("xpath=//input[@type=\"submit\" and contains(@value, \"" + buttonText + "\")]");

        return strategies.toArray(new String[0]);
    }

    /**
     * Generate smart locator strategies for links
     *
     * @param linkText Link text
     * @return Array of locator strategies
     */
    public static String[] generateLinkStrategies(String linkText) {
        List<String> strategies = new ArrayList<>();

        // data-testid
        strategies.add("data-testid=" + linkText.toLowerCase().replace(" ", "-") + "-link");

        // Role + name
        strategies.add("role=link,name=" + linkText);

        // Text content
        strategies.add("text=" + linkText);
        strategies.add("text*=" + linkText);

        // XPath
        strategies.add("xpath=//a[contains(text(), \"" + linkText + "\")]");
        strategies.add("xpath=//a[contains(@href, \"" + linkText.toLowerCase() + "\")]");

        return strategies.toArray(new String[0]);
    }
}
