package pages;

import com.microsoft.playwright.Page;
import configs.loadProps;

/**
 * Page Object Model for My Account Page
 * Handles access group impersonation and switching functionality
 * 
 * @author QA Automation Team
 * @version 1.0.0
 * @story ECS-14 - Enable Impersonate Access Group functionality
 */
public class MyAccountPage extends BasePage {
    
    // ===== Locators =====
    private static final String IMPERSONATE_LABEL = "//label[contains(text(),'Impersonate Access Group')]";
    private static final String ACCESS_GROUP_DROPDOWN = "select#impersonateAccessGroupDropdown";
    private static final String CURRENT_ACCESS_GROUP_LABEL = "span#currentAccessGroup";
    private static final String CLEAR_BUTTON = "button#clearImpersonation";
    private static final String MY_ACCOUNT_HEADER = "h1.page-title";
    private static final String ACCESS_GROUP_SECTION = "div#accessGroupSection";
    private static final String CONFIRMATION_MESSAGE = "div.notification-message";
    
    /**
     * Navigate to My Account page
     * @param page Playwright page instance
     */
    public static void navigateToMyAccount(Page page) {
        page.navigate(loadProps.getProperty("URL") + "/my-account");
        page.waitForLoadState();
    }
    
    /**
     * Verify impersonate label is displayed
     * @param page Playwright page instance
     * @return true if impersonate label is visible
     */
    public static boolean isImpersonateLabelDisplayed(Page page) {
        return page.isVisible(IMPERSONATE_LABEL);
    }
    
    /**
     * Verify access group dropdown is displayed
     * @param page Playwright page instance
     * @return true if dropdown is visible
     */
    public static boolean isAccessGroupDropdownDisplayed(Page page) {
        return page.isVisible(ACCESS_GROUP_DROPDOWN);
    }
    
    /**
     * Select access group from dropdown
     * @param page Playwright page instance
     * @param accessGroupName Access group name to select
     */
    public static void selectAccessGroupToImpersonate(Page page, String accessGroupName) {
        page.selectOption(ACCESS_GROUP_DROPDOWN, accessGroupName);
        page.waitForTimeout(1000);
    }
    
    /**
     * Get currently selected access group name
     * @param page Playwright page instance
     * @return Current access group name
     */
    public static String getCurrentAccessGroupName(Page page) {
        return page.textContent(CURRENT_ACCESS_GROUP_LABEL);
    }
    
    /**
     * Verify clear button is displayed
     * @param page Playwright page instance
     * @return true if clear button is visible
     */
    public static boolean isClearButtonDisplayed(Page page) {
        return page.isVisible(CLEAR_BUTTON);
    }
    
    /**
     * Click clear button to reset to original access group
     * @param page Playwright page instance
     */
    public static void clickClearButton(Page page) {
        page.click(CLEAR_BUTTON);
        page.waitForTimeout(1000);
    }
    
    /**
     * Verify current access group label is displayed
     * @param page Playwright page instance
     * @return true if current access group label is visible
     */
    public static boolean isCurrentAccessGroupLabelDisplayed(Page page) {
        return page.isVisible(CURRENT_ACCESS_GROUP_LABEL);
    }
    
    /**
     * Get all available access groups from dropdown
     * @param page Playwright page instance
     * @return Array of access group names
     */
    public static String[] getAvailableAccessGroups(Page page) {
        return page.locator(ACCESS_GROUP_DROPDOWN + " option")
                .allTextContents()
                .toArray(new String[0]);
    }
    
    /**
     * Verify access group dropdown is NOT displayed
     * @param page Playwright page instance
     * @return true if dropdown is not visible
     */
    public static boolean isAccessGroupDropdownHidden(Page page) {
        return !page.isVisible(ACCESS_GROUP_DROPDOWN);
    }
    
    /**
     * Verify clear button is NOT displayed
     * @param page Playwright page instance
     * @return true if clear button is not visible
     */
    public static boolean isClearButtonHidden(Page page) {
        return !page.isVisible(CLEAR_BUTTON);
    }
    
    /**
     * Get confirmation message text
     * @param page Playwright page instance
     * @return Confirmation message text
     */
    public static String getConfirmationMessage(Page page) {
        page.waitForSelector(CONFIRMATION_MESSAGE, new Page.WaitForSelectorOptions().setTimeout(5000));
        return page.textContent(CONFIRMATION_MESSAGE);
    }
    
    /**
     * Verify My Account page is loaded
     * @param page Playwright page instance
     * @return true if page header is visible
     */
    public static boolean isMyAccountPageLoaded(Page page) {
        return page.isVisible(MY_ACCOUNT_HEADER);
    }
    
    /**
     * Verify access group section is visible
     * @param page Playwright page instance
     * @return true if access group section is visible
     */
    public static boolean isAccessGroupSectionVisible(Page page) {
        return page.isVisible(ACCESS_GROUP_SECTION);
    }
    
    /**
     * Switch to different access group and verify
     * @param page Playwright page instance
     * @param accessGroupName Access group to switch to
     * @return true if successfully switched
     */
    public static boolean switchAccessGroup(Page page, String accessGroupName) {
        selectAccessGroupToImpersonate(page, accessGroupName);
        String currentGroup = getCurrentAccessGroupName(page);
        return currentGroup.equals(accessGroupName);
    }
    
    /**
     * Reset to original access group
     * @param page Playwright page instance
     */
    public static void resetToOriginalAccessGroup(Page page) {
        if (isClearButtonDisplayed(page)) {
            clickClearButton(page);
        }
    }
}
