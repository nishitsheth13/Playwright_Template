package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;

/**
 * Page Object for Login
 * Auto-generated from Playwright recording
 * Uses common methods from BasePage and utils
 */
public class Login extends BasePage {
    private static final String PAGE_PATH = "";

    // Locators - Using semantic names without ID suffixes
    private static final String USERNAME = "#Username";
    private static final String PASSWORD = "#Password";
    private static final String SIGN_IN = "text=Sign In";
    private static final String LOGOUT_LOGOUT = "label=Logout";

    /**
     * Navigate to Login page
     * Uses common navigateToUrl method from BasePage
     */
    public static void navigateToLogin(Page page) {
        String fullUrl = loadProps.getProperty("URL") + PAGE_PATH;
        navigateToUrl(fullUrl);
    }

    /**
     * Enter username using common enterText method from utils
     */
    public static void enterUsername(Page page, String text) {
        System.out.println("⌨️ Entering username");
        enterText(USERNAME, text);
    }

    /**
     * Enter password using common enterText method from utils
     */
    public static void enterPassword(Page page, String text) {
        System.out.println("⌨️ Entering password");
        enterText(PASSWORD, text);
    }

    /**
     * Click Sign In button using common clickOnElement method from utils
     */
    public static void clickSignIn(Page page) {
        System.out.println("🖱️ Clicking Sign In button");
        clickOnElement(SIGN_IN);
    }

    /**
     * Click Logout button using common clickOnElement method from utils
     */
    public static void clickLogoutLogout(Page page) {
        System.out.println("🖱️ Clicking Logout button");
        clickOnElement(LOGOUT_LOGOUT);
    }

    /**
     * Complete login flow using config credentials
     * Reuses common methods for better maintainability
     */
    public static void performLogin(String username, String password) {
        enterUsername(page, username);
        enterPassword(page, password);
        clickSignIn(page);
        System.out.println("✅ Login completed");
    }
}
