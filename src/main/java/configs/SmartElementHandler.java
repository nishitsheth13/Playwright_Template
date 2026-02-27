package configs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.testng.Assert;

/**
 * Smart Element Handler - Utility for handling multiple elements with same name/text
 *
 * PROBLEM: Pages often have multiple "Add", "Save", "Delete" buttons
 * SOLUTION: Smart strategies to identify and interact with the correct element
 *
 * @author Auto-Generated
 * @version 1.0
 * @since Feb 26, 2026
 *
 * USAGE EXAMPLES:
 *
 * 1. Click button within specific section:
 *    SmartElementHandler.clickButtonInContext(page, "Add", "#users-section");
 *
 * 2. Click button with priority-based strategy:
 *    SmartElementHandler.clickButtonSmart(page, "add-user-btn", "Add User");
 *
 * 3. Click nth button with validation:
 *    SmartElementHandler.clickNthButton(page, "Save", 1);
 *
 * 4. Get element count for validation:
 *    int count = SmartElementHandler.getButtonCount(page, "Delete");
 */
public class SmartElementHandler {

    /**
     * Click button by text within a specific context/section.
     * ⭐ RECOMMENDED for pages with multiple sections
     *
     * @param page            Playwright page object
     * @param buttonText      Text on the button
     * @param contextSelector CSS selector for the container/section
     *
     * @example
     * clickButtonInContext(page, "Add", "#users-section");
     * clickButtonInContext(page, "Save", "form[name='userForm']");
     */
    public static void clickButtonInContext(Page page, String buttonText, String contextSelector) {
        System.out.println("🔍 Looking for '" + buttonText + "' button in context: " + contextSelector);

        Locator context = page.locator(contextSelector);
        if (context.count() == 0) {
            Assert.fail("❌ Context not found: " + contextSelector);
        }

        Locator button = context.locator("button, [role='button'], input[type='submit'], input[type='button']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        int count = button.count();
        System.out.println("📊 Found " + count + " '" + buttonText + "' button(s) in context");

        if (count == 0) {
            Assert.fail("❌ No '" + buttonText + "' button found in context: " + contextSelector);
        }

        button.first().click();
        System.out.println("✅ Clicked '" + buttonText + "' button in context");
        TimeoutConfig.waitShort();
    }

    /**
     * Click button using priority-based strategy: ID > data-testid > aria-label > text
     * ⭐ RECOMMENDED for production use
     *
     * @param page       Playwright page object
     * @param buttonId   ID or data-testid of the button
     * @param buttonText Fallback text on the button
     *
     * @example
     * clickButtonSmart(page, "add-user-btn", "Add User");
     */
    public static void clickButtonSmart(Page page, String buttonId, String buttonText) {
        Locator button = null;
        String method = "";

        System.out.println("🔍 Smart search for button ID: '" + buttonId + "', Text: '" + buttonText + "'");

        // Priority 1: Try by ID
        if (page.locator("#" + buttonId).count() > 0) {
            button = page.locator("#" + buttonId);
            method = "ID (#" + buttonId + ")";
        }
        // Priority 2: Try by data-testid
        else if (page.locator("[data-testid='" + buttonId + "']").count() > 0) {
            button = page.locator("[data-testid='" + buttonId + "']");
            method = "data-testid";
        }
        // Priority 3: Try by name attribute
        else if (page.locator("[name='" + buttonId + "']").count() > 0) {
            button = page.locator("[name='" + buttonId + "']");
            method = "name attribute";
        }
        // Priority 4: Try by aria-label
        else if (page.locator("[aria-label='" + buttonText + "']").count() > 0) {
            button = page.locator("[aria-label='" + buttonText + "']");
            method = "aria-label";
        }
        // Priority 5: Try by text (least stable)
        else {
            button = page.locator("button, [role='button'], input[type='submit'], input[type='button']")
                .filter(new Locator.FilterOptions().setHasText(buttonText));
            method = "text content";
        }

        if (button.count() == 0) {
            Assert.fail("❌ Button not found with ID: '" + buttonId + "' or text: '" + buttonText + "'");
        }

        System.out.println("✅ Found button using: " + method);
        button.first().click();
        System.out.println("✅ Clicked button successfully");
        TimeoutConfig.waitShort();
    }

    /**
     * Click the nth button with specific text (0-based index).
     * ⚠️ USE WITH CAUTION - position-based selectors are fragile
     *
     * @param page       Playwright page object
     * @param buttonText Text on the button
     * @param index      Index of the button (0-based)
     *
     * @example
     * clickNthButton(page, "Save", 0); // Click first Save button
     * clickNthButton(page, "Delete", 1); // Click second Delete button
     */
    public static void clickNthButton(Page page, String buttonText, int index) {
        System.out.println("🔍 Looking for button #" + (index + 1) + " with text: '" + buttonText + "'");

        Locator buttons = page.locator("button, [role='button'], input[type='submit'], input[type='button']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        int count = buttons.count();
        System.out.println("📊 Found " + count + " '" + buttonText + "' button(s)");

        if (count == 0) {
            Assert.fail("❌ No '" + buttonText + "' buttons found");
        }

        if (index >= count) {
            Assert.fail("❌ Requested button at index " + index +
                       " but only " + count + " button(s) found");
        }

        buttons.nth(index).click();
        System.out.println("✅ Clicked '" + buttonText + "' button at index " + index);
        TimeoutConfig.waitShort();
    }

    /**
     * Click first visible button with specific text.
     * Useful when multiple buttons exist but only one is visible.
     *
     * @param page       Playwright page object
     * @param buttonText Text on the button
     *
     * @example
     * clickFirstVisibleButton(page, "Add");
     */
    public static void clickFirstVisibleButton(Page page, String buttonText) {
        System.out.println("🔍 Looking for first visible '" + buttonText + "' button");

        Locator buttons = page.locator("button, [role='button'], input[type='submit'], input[type='button']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        int totalCount = buttons.count();
        System.out.println("📊 Found " + totalCount + " '" + buttonText + "' button(s) total");

        if (totalCount == 0) {
            Assert.fail("❌ No '" + buttonText + "' buttons found");
        }

        // Find first visible button
        Locator visibleButton = null;
        for (int i = 0; i < totalCount; i++) {
            if (buttons.nth(i).isVisible()) {
                visibleButton = buttons.nth(i);
                System.out.println("✅ Found visible button at index " + i);
                break;
            }
        }

        if (visibleButton == null) {
            Assert.fail("❌ No visible '" + buttonText + "' buttons found (total: " + totalCount + ")");
        }

        visibleButton.click();
        System.out.println("✅ Clicked first visible '" + buttonText + "' button");
        TimeoutConfig.waitShort();
    }

    /**
     * Get count of buttons with specific text.
     * Useful for validation before clicking.
     *
     * @param page       Playwright page object
     * @param buttonText Text on the button
     * @return Number of matching buttons
     *
     * @example
     * int count = getButtonCount(page, "Delete");
     * Assert.assertTrue(count > 0, "Delete button should exist");
     */
    public static int getButtonCount(Page page, String buttonText) {
        Locator buttons = page.locator("button, [role='button'], input[type='submit'], input[type='button']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        int count = buttons.count();
        System.out.println("📊 Found " + count + " '" + buttonText + "' button(s)");
        return count;
    }

    /**
     * Click button in a specific table row.
     * ⭐ RECOMMENDED for tables with action buttons
     *
     * @param page       Playwright page object
     * @param rowText    Text that identifies the row (e.g., user name, ID)
     * @param buttonText Text on the button
     *
     * @example
     * clickButtonInRow(page, "John Doe", "Edit");
     * clickButtonInRow(page, "Product-123", "Delete");
     */
    public static void clickButtonInRow(Page page, String rowText, String buttonText) {
        System.out.println("🔍 Looking for '" + buttonText + "' button in row containing: '" + rowText + "'");

        Locator row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(rowText));

        int rowCount = row.count();
        if (rowCount == 0) {
            Assert.fail("❌ No row found containing text: '" + rowText + "'");
        }

        System.out.println("📊 Found " + rowCount + " row(s) containing text");

        Locator button = row.first().locator("button, [role='button'], a")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        if (button.count() == 0) {
            Assert.fail("❌ No '" + buttonText + "' button found in row");
        }

        button.first().click();
        System.out.println("✅ Clicked '" + buttonText + "' button in row");
        TimeoutConfig.waitShort();
    }

    /**
     * Click button near a specific element (within same parent).
     * Useful when button is adjacent to a label or input field.
     *
     * @param page            Playwright page object
     * @param referenceSelector CSS selector for reference element
     * @param buttonText      Text on the button
     *
     * @example
     * clickButtonNearElement(page, "#username", "Clear");
     * clickButtonNearElement(page, "input[name='email']", "Verify");
     */
    public static void clickButtonNearElement(Page page, String referenceSelector, String buttonText) {
        System.out.println("🔍 Looking for '" + buttonText + "' button near: " + referenceSelector);

        Locator reference = page.locator(referenceSelector);
        if (reference.count() == 0) {
            Assert.fail("❌ Reference element not found: " + referenceSelector);
        }

        // Get parent container of reference element
        Locator parent = reference.locator("xpath=..");

        Locator button = parent.locator("button, [role='button']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        if (button.count() == 0) {
            Assert.fail("❌ No '" + buttonText + "' button found near reference element");
        }

        button.first().click();
        System.out.println("✅ Clicked '" + buttonText + "' button near reference element");
        TimeoutConfig.waitShort();
    }

    /**
     * Validate button exists and is enabled before clicking.
     * ⭐ RECOMMENDED for robust test automation
     *
     * @param page       Playwright page object
     * @param buttonText Text on the button
     * @param shouldExist Expected existence (true = should exist, false = should not exist)
     * @return true if validation passed
     *
     * @example
     * validateButtonState(page, "Submit", true); // Should exist
     * validateButtonState(page, "Delete", false); // Should NOT exist
     */
    public static boolean validateButtonState(Page page, String buttonText, boolean shouldExist) {
        Locator button = page.locator("button, [role='button'], input[type='submit']")
            .filter(new Locator.FilterOptions().setHasText(buttonText));

        int count = button.count();
        System.out.println("📊 Found " + count + " '" + buttonText + "' button(s)");

        if (shouldExist) {
            Assert.assertTrue(count > 0, "Expected '" + buttonText + "' button to exist");
            Assert.assertTrue(button.first().isVisible(), "'" + buttonText + "' button should be visible");
            Assert.assertTrue(button.first().isEnabled(), "'" + buttonText + "' button should be enabled");
            System.out.println("✅ Button validation passed: exists, visible, enabled");
            return true;
        } else {
            Assert.assertTrue(count == 0, "Expected '" + buttonText + "' button NOT to exist");
            System.out.println("✅ Button validation passed: does not exist");
            return true;
        }
    }

    /**
     * Click button with retry logic for flaky elements.
     * Useful for buttons that may take time to become clickable.
     *
     * @param page       Playwright page object
     * @param buttonText Text on the button
     * @param maxRetries Maximum number of retry attempts
     *
     * @example
     * clickButtonWithRetry(page, "Submit", 3);
     */
    public static void clickButtonWithRetry(Page page, String buttonText, int maxRetries) {
        System.out.println("🔍 Attempting to click '" + buttonText + "' button with retry");

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("📍 Attempt " + attempt + " of " + maxRetries);

                Locator button = page.locator("button, [role='button'], input[type='submit']")
                    .filter(new Locator.FilterOptions().setHasText(buttonText));

                if (button.count() > 0 && button.first().isVisible() && button.first().isEnabled()) {
                    button.first().click();
                    System.out.println("✅ Successfully clicked '" + buttonText + "' button on attempt " + attempt);
                    TimeoutConfig.waitShort();
                    return;
                }

                System.out.println("⚠️ Button not ready, waiting...");
                TimeoutConfig.waitShort();

            } catch (Exception e) {
                System.out.println("⚠️ Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) {
                    Assert.fail("❌ Failed to click '" + buttonText + "' button after " + maxRetries + " attempts");
                }
                TimeoutConfig.waitShort();
            }
        }
    }
}

