package pages;

import com.microsoft.playwright.Page;
import configs.loadProps;

/**
 * Page Object Model for User Configuration Page
 * Handles user setup and impersonation configuration functionality
 * 
 * @author QA Automation Team
 * @version 1.0.0
 * @story ECS-14 - Enable Impersonate Access Group functionality
 */
public class UserConfigurationPage extends BasePage {
    
    // ===== Locators =====
    private static final String ACCESS_TYPE_DROPDOWN = "select#accessType";
    private static final String ACCESS_GROUP_DROPDOWN = "select#accessGroup";
    private static final String IMPERSONATE_CHECKBOX = "input[name='impersonateAccessGroup']";
    private static final String USER_NAME_INPUT = "input#userName";
    private static final String EMAIL_INPUT = "input#email";
    private static final String SAVE_USER_BUTTON = "button[type='submit']";
    private static final String SUCCESS_MESSAGE = "div.alert-success";
    private static final String ACCESS_GROUP_LABEL = "label[for='accessGroup']";
    
    /**
     * Navigate to User Configuration page
     * @param page Playwright page instance
     */
    public static void navigateToUserConfiguration(Page page) {
        page.navigate(loadProps.getProperty("URL") + "/setup/security/user-configuration");
        page.waitForLoadState();
    }
    
    /**
     * Set access type to Access Group Security
     * @param page Playwright page instance
     */
    public static void selectAccessTypeAsAccessGroupSecurity(Page page) {
        page.selectOption(ACCESS_TYPE_DROPDOWN, "Access Group Security");
        page.waitForTimeout(500);
    }
    
    /**
     * Select access group from dropdown
     * @param page Playwright page instance
     * @param accessGroupName Name of the access group to select
     */
    public static void selectAccessGroup(Page page, String accessGroupName) {
        page.selectOption(ACCESS_GROUP_DROPDOWN, accessGroupName);
    }
    
    /**
     * Enable impersonate access group checkbox
     * @param page Playwright page instance
     */
    public static void enableImpersonateAccessGroup(Page page) {
        if (!page.isChecked(IMPERSONATE_CHECKBOX)) {
            page.check(IMPERSONATE_CHECKBOX);
        }
    }
    
    /**
     * Disable impersonate access group checkbox
     * @param page Playwright page instance
     */
    public static void disableImpersonateAccessGroup(Page page) {
        if (page.isChecked(IMPERSONATE_CHECKBOX)) {
            page.uncheck(IMPERSONATE_CHECKBOX);
        }
    }
    
    /**
     * Enter user name
     * @param page Playwright page instance
     * @param userName User name to enter
     */
    public static void enterUserName(Page page, String userName) {
        page.fill(USER_NAME_INPUT, userName);
    }
    
    /**
     * Enter user email
     * @param page Playwright page instance
     * @param email Email to enter
     */
    public static void enterEmail(Page page, String email) {
        page.fill(EMAIL_INPUT, email);
    }
    
    /**
     * Click save user button
     * @param page Playwright page instance
     */
    public static void clickSaveUser(Page page) {
        page.click(SAVE_USER_BUTTON);
    }
    
    /**
     * Verify impersonate checkbox is visible
     * @param page Playwright page instance
     * @return true if checkbox is visible
     */
    public static boolean isImpersonateCheckboxVisible(Page page) {
        return page.isVisible(IMPERSONATE_CHECKBOX);
    }
    
    /**
     * Verify impersonate checkbox is hidden
     * @param page Playwright page instance
     * @return true if checkbox is not visible
     */
    public static boolean isImpersonateCheckboxHidden(Page page) {
        return !page.isVisible(IMPERSONATE_CHECKBOX);
    }
    
    /**
     * Verify impersonate checkbox is checked
     * @param page Playwright page instance
     * @return true if checkbox is checked
     */
    public static boolean isImpersonateCheckboxChecked(Page page) {
        return page.isChecked(IMPERSONATE_CHECKBOX);
    }
    
    /**
     * Verify success message is displayed
     * @param page Playwright page instance
     * @return true if success message is visible
     */
    public static boolean isSuccessMessageDisplayed(Page page) {
        return page.isVisible(SUCCESS_MESSAGE);
    }
    
    /**
     * Get success message text
     * @param page Playwright page instance
     * @return Success message text
     */
    public static String getSuccessMessageText(Page page) {
        return page.textContent(SUCCESS_MESSAGE);
    }
    
    /**
     * Create new user with impersonate access group enabled
     * @param page Playwright page instance
     * @param userName User name
     * @param email User email
     * @param accessGroup Access group name
     */
    public static void createUserWithImpersonation(Page page, String userName, String email, String accessGroup) {
        enterUserName(page, userName);
        enterEmail(page, email);
        selectAccessTypeAsAccessGroupSecurity(page);
        selectAccessGroup(page, accessGroup);
        enableImpersonateAccessGroup(page);
        clickSaveUser(page);
    }
    
    /**
     * Verify access group field is enabled
     * @param page Playwright page instance
     * @return true if access group dropdown is enabled
     */
    public static boolean isAccessGroupFieldEnabled(Page page) {
        return page.isEnabled(ACCESS_GROUP_DROPDOWN);
    }
}
