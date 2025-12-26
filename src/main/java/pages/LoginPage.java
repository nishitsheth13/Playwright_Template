package pages;

import com.microsoft.playwright.Locator;
import configs.TimeoutConfig;

// Note: BasePage is in the same package, so explicit import is not required
// The 'page' field is inherited from BasePage which extends utils

/**
 * Page Object Model for Login Page.
 * Contains locators, actions, and verifications for login functionality.
 * Follows framework conventions with protected static methods and extends BasePage.
 * 
 * @author Automation Team
 * @version 1.0
 */
public class LoginPage extends BasePage {

    // ============================================================
    // LOCATORS
    // ============================================================
    
    private static final String USERNAME_INPUT = "//input[@id='Username']";
    private static final String PASSWORD_INPUT = "//input[@id='Password']";
    private static final String LOGIN_BUTTON = "//button[@name='button']";
    private static final String REMEMBER_ME_CHECKBOX = "//input[@id='RememberLogin' and @type='checkbox']";
    private static final String FORGOT_USERNAME_LINK = "//a[contains(text(), 'Forgot Username')]";
    private static final String FORGOT_PASSWORD_LINK = "//a[contains(text(), 'Forgot Password')]";
    private static final String ERROR_MESSAGE = "//div[contains(@class, 'alert') or contains(@class, 'error')]";
    private static final String LOGIN_LOGO = "//img[contains(@src, 'logo') or contains(@alt, 'MRI')]";
    private static final String USER_ICON = "//span[@class='menu_user_title']";

    // ============================================================
    // ELEMENT GETTERS
    // ============================================================
    
    /**
     * Gets the username input field locator.
     * @return Locator for username input
     */
    protected static Locator getUsernameInput() {
        return page.locator(USERNAME_INPUT);
    }

    /**
     * Gets the password input field locator.
     * @return Locator for password input
     */
    protected static Locator getPasswordInput() {
        return page.locator(PASSWORD_INPUT);
    }

    /**
     * Gets the login button locator.
     * @return Locator for login button
     */
    protected static Locator getLoginButton() {
        return page.locator(LOGIN_BUTTON);
    }

    /**
     * Gets the remember me checkbox locator.
     * @return Locator for remember me checkbox
     */
    protected static Locator getRememberMeCheckbox() {
        return page.locator(REMEMBER_ME_CHECKBOX);
    }

    /**
     * Gets the forgot username link locator.
     * @return Locator for forgot username link
     */
    protected static Locator getForgotUsernameLink() {
        return page.locator(FORGOT_USERNAME_LINK);
    }

    /**
     * Gets the forgot password link locator.
     * @return Locator for forgot password link
     */
    protected static Locator getForgotPasswordLink() {
        return page.locator(FORGOT_PASSWORD_LINK);
    }

    /**
     * Gets the error message locator.
     * @return Locator for error message
     */
    protected static Locator getErrorMessage() {
        return page.locator(ERROR_MESSAGE);
    }

    /**
     * Gets the login logo locator.
     * @return Locator for login logo
     */
    protected static Locator getLoginLogo() {
        return page.locator(LOGIN_LOGO);
    }

    /**
     * Gets the user icon locator (visible after successful login).
     * @return Locator for user icon
     */
    protected static Locator getUserIcon() {
        return page.locator(USER_ICON);
    }

    // ============================================================
    // ACTION METHODS
    // ============================================================
    
    /**
     * Enters username in the username field.
     * @param username The username to enter
     */
    public static void enterUsername(String username) {
        System.out.println("🔹 Entering username: " + maskUsername(username));
        getUsernameInput().clear();
        getUsernameInput().fill(username);
        System.out.println("✅ Username entered");
    }

    /**
     * Enters password in the password field.
     * @param password The password to enter
     */
    public static void enterPassword(String password) {
        System.out.println("🔹 Entering password (masked for security)");
        getPasswordInput().clear();
        getPasswordInput().fill(password);
        System.out.println("✅ Password entered");
    }

    /**
     * Clicks the login button.
     */
    public static void clickLoginButton() {
        System.out.println("🔹 Clicking login button...");
        getLoginButton().click();
        page.waitForLoadState();
        TimeoutConfig.mediumWait();
        System.out.println("✅ Login button clicked");
    }

    /**
     * Checks the remember me checkbox.
     */
    public static void checkRememberMe() {
        System.out.println("🔹 Checking Remember Me checkbox...");
        if (!getRememberMeCheckbox().isChecked()) {
            getRememberMeCheckbox().click();
        }
        System.out.println("✅ Remember Me checked");
    }

    /**
     * Unchecks the remember me checkbox.
     */
    public static void uncheckRememberMe() {
        System.out.println("🔹 Unchecking Remember Me checkbox...");
        if (getRememberMeCheckbox().isChecked()) {
            getRememberMeCheckbox().click();
        }
        System.out.println("✅ Remember Me unchecked");
    }

    /**
     * Sets the remember me checkbox to a specific state.
     * @param checked true to check, false to uncheck
     */
    public static void setRememberMe(boolean checked) {
        System.out.println("🔹 Setting Remember Me to: " + checked);
        if (checked) {
            checkRememberMe();
        } else {
            uncheckRememberMe();
        }
    }

    /**
     * Clicks the forgot username link.
     */
    public static void clickForgotUsernameLink() {
        System.out.println("🔹 Clicking Forgot Username link...");
        getForgotUsernameLink().click();
        page.waitForLoadState();
        TimeoutConfig.mediumWait();
        System.out.println("✅ Forgot Username link clicked");
    }

    /**
     * Clicks the forgot password link.
     */
    public static void clickForgotPasswordLink() {
        System.out.println("🔹 Clicking Forgot Password link...");
        getForgotPasswordLink().click();
        page.waitForLoadState();
        TimeoutConfig.mediumWait();
        System.out.println("✅ Forgot Password link clicked");
    }

    // ============================================================
    // BUSINESS LOGIC METHODS
    // ============================================================
    
    /**
     * Performs login with provided credentials.
     * This is the main login method that combines all login actions.
     * 
     * @param username The username to login with
     * @param password The password to login with
     */
    public static void login(String username, String password) {
        System.out.println("🔐 Attempting login for user: " + maskUsername(username));
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        System.out.println("✅ Login process completed");
    }
    
    /**
     * Masks username for logging purposes (shows first 2 chars + ***)
     * @param username The username to mask
     * @return Masked username for logging
     */
    private static String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.substring(0, 2) + "***";
    }

    /**
     * Performs login with Remember Me option enabled.
     * 
     * @param username The username to login with
     * @param password The password to login with
     */
    public static void loginWithRememberMe(String username, String password) {
        System.out.println("🔐 Attempting login with Remember Me enabled");
        enterUsername(username);
        enterPassword(password);
        checkRememberMe();
        clickLoginButton();
        System.out.println("✅ Login with Remember Me completed");
    }

    // ============================================================
    // VERIFICATION METHODS
    // ============================================================
    
    /**
     * Checks if the login page is displayed.
     * @return true if login page is visible
     */
    public static boolean isLoginPageDisplayed() {
        System.out.println("🔍 Checking if login page is displayed");
        try {
            boolean isDisplayed = getUsernameInput().isVisible() && 
                                 getPasswordInput().isVisible() && 
                                 getLoginButton().isVisible();
            System.out.println("✅ Login page displayed: " + isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            System.out.println("❌ Login page not displayed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if login was successful by verifying user icon is visible.
     * @return true if login was successful
     */
    public static boolean isLoginSuccessful() {
        System.out.println("🔍 Verifying login success");
        try {
            TimeoutConfig.mediumWait();
            boolean isSuccess = getUserIcon().isVisible();
            System.out.println("✅ Login successful: " + isSuccess);
            return isSuccess;
        } catch (Exception e) {
            System.out.println("❌ Login verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if error message is displayed.
     * @return true if error message is visible
     */
    public static boolean isErrorMessageDisplayed() {
        System.out.println("🔍 Checking for error message");
        try {
            boolean isDisplayed = getErrorMessage().isVisible();
            System.out.println("⚠️ Error message displayed: " + isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            System.out.println("✅ No error message displayed");
            return false;
        }
    }

    /**
     * Gets the error message text.
     * @return The error message text, or empty string if not visible
     */
    public static String getErrorMessageText() {
        System.out.println("🔍 Getting error message text");
        try {
            if (isErrorMessageDisplayed()) {
                String errorText = getErrorMessage().innerText();
                System.out.println("⚠️ Error message: " + errorText);
                return errorText;
            }
            return "";
        } catch (Exception e) {
            System.out.println("❌ Could not get error message: " + e.getMessage());
            return "";
        }
    }

    /**
     * Checks if remember me checkbox is checked.
     * @return true if remember me is checked
     */
    public static boolean isRememberMeChecked() {
        System.out.println("🔍 Checking Remember Me status");
        try {
            boolean isChecked = getRememberMeCheckbox().isChecked();
            System.out.println("✅ Remember Me checked: " + isChecked);
            return isChecked;
        } catch (Exception e) {
            System.out.println("❌ Could not check Remember Me status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if forgot username link is visible.
     * @return true if forgot username link is visible
     */
    public static boolean isForgotUsernameLinkVisible() {
        System.out.println("🔍 Checking Forgot Username link visibility");
        try {
            boolean isVisible = getForgotUsernameLink().isVisible();
            System.out.println("✅ Forgot Username link visible: " + isVisible);
            return isVisible;
        } catch (Exception e) {
            System.out.println("❌ Forgot Username link not visible");
            return false;
        }
    }

    /**
     * Checks if forgot password link is visible.
     * @return true if forgot password link is visible
     */
    public static boolean isForgotPasswordLinkVisible() {
        System.out.println("🔍 Checking Forgot Password link visibility");
        try {
            boolean isVisible = getForgotPasswordLink().isVisible();
            System.out.println("✅ Forgot Password link visible: " + isVisible);
            return isVisible;
        } catch (Exception e) {
            System.out.println("❌ Forgot Password link not visible");
            return false;
        }
    }

    /**
     * Checks if login logo is visible.
     * @return true if logo is visible
     */
    public static boolean isLogoVisible() {
        System.out.println("🔍 Checking logo visibility");
        try {
            boolean isVisible = getLoginLogo().isVisible();
            System.out.println("✅ Logo visible: " + isVisible);
            return isVisible;
        } catch (Exception e) {
            System.out.println("❌ Logo not visible");
            return false;
        }
    }

    /**
     * Checks if password field masks the input.
     * @return true if password is masked
     */
    public static boolean isPasswordMasked() {
        System.out.println("🔍 Checking if password is masked");
        try {
            String fieldType = getPasswordInput().getAttribute("type");
            boolean isMasked = "password".equalsIgnoreCase(fieldType);
            System.out.println("✅ Password masked: " + isMasked);
            return isMasked;
        } catch (Exception e) {
            System.out.println("❌ Could not verify password masking: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================
    
    /**
     * Clears the login form (username and password fields).
     */
    public static void clearLoginForm() {
        System.out.println("🔹 Clearing login form...");
        getUsernameInput().clear();
        getPasswordInput().clear();
        System.out.println("✅ Login form cleared");
    }

    /**
     * Waits for the login page to be fully loaded.
     */
    public static void waitForLoginPageLoad() {
        System.out.println("⏳ Waiting for login page to load...");
        waitForPageLoad();
        TimeoutConfig.mediumWait();
        if (isLoginPageDisplayed()) {
            System.out.println("✅ Login page loaded successfully");
        } else {
            System.out.println("⚠️ Login page may not be fully loaded");
        }
    }
}
