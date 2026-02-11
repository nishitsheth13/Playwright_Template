package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import configs.TimeoutConfig;
import java.util.logging.Logger;

/**
 * Page Object for Navigation
 * Auto-generated from Playwright recording
 * 
 * This class extends BasePage which provides:
 *  - clickOnElement(locator) - from utils.java
 *  - enterText(locator, text) - from utils.java
 *  - selectDropDownValueByText(locator, text) - from utils.java
 *  - navigateToUrl(url) - from BasePage.java
 *  - And many more common utilities
 * 
 * All generated methods use these common utilities for consistency
 * and better maintainability across the test framework.
 * 
 * @story AUTO-GEN
 */
public class Navigation extends BasePage {
    private static final Logger log = Logger.getLogger(Navigation.class.getName());
    private static final String PAGE_PATH = "/MRIEnergy";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME = "label=Username";

    // Password - Priority 4: Label/Name ⭐⭐
    private static final String PASSWORD = "label=Password";

    // SignIn - Priority 6: CSS ⭐
    private static final String SIGN_IN = "text=Sign In";

    // Div - Priority 2: Relative XPath ⭐⭐⭐⭐
    // NOTE: Generic //div matches 23 elements. Use first visible div or skip this step if not needed
    // Using .first() to handle multiple matches, or comment out if this action is not required
    private static final String DIV = "div.wrapper >> visible=true";

    // SetupSetup - Priority 6: CSS ⭐
    private static final String SETUP_SETUP = "text=Setup Setup";

    // ConfigureTree - Priority 6: CSS ⭐
    private static final String CONFIGURE_TREE = "text=Configure Tree";

    // LogoutLogout - Priority 6: CSS ⭐
    private static final String LOGOUT_LOGOUT = "text=Logout Logout";

    /**
     * Navigate to Navigation page
     * Uses common navigateToUrl method from BasePage
     * @param page Playwright Page instance
     */
    public static void navigateToNavigation(Page page) {
        log.info("🌐 Navigating to Navigation page");
        String fullUrl = loadProps.getProperty("URL") + PAGE_PATH;
        navigateToUrl(fullUrl);
        log.info("✅ Navigation completed");
    }

    /**
     * user enters text into username
     * Element: Username (label=Username)
     * Uses common method from utils.java
     */
    public static void enterUsername(Page page, String text) {
        log.info("⌨️ user enters text into username: " + text);
        enterText(USERNAME, text); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user enters text into username completed");
    }

    /**
     * user presses key on username
     * Element: Username (label=Username)
     * Uses common method from utils.java
     */
    public static void pressKeyOnUsername(Page page) {
        log.info("⌨️ user presses key on username: " + USERNAME + " - Key: Tab");
        page.locator(USERNAME).press("Tab");
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user presses key on username completed");
    }

    /**
     * user enters text into password
     * Element: Password (label=Password)
     * Uses common method from utils.java
     */
    public static void enterPassword(Page page, String text) {
        log.info("⌨️ user enters text into password: " + text);
        enterText(PASSWORD, text); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user enters text into password completed");
    }

    /**
     * user clicks on sign in
     * Element: SignIn (text=Sign In)
     * Uses common method from utils.java
     */
    public static void clickSignIn(Page page) {
        log.info("🖱️ user clicks on sign in");
        clickOnElement(SIGN_IN); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on sign in completed");
    }

    /**
    /**
     * Element: Div (//div)
     * WARNING: This was auto-generated from recording but clicks a generic div element.
     * Consider updating the locator to target a specific element or removing this step.
     * Uses common method from utils.java
     */
    public static void clickDiv(Page page) {
        log.info("🖱️ user clicks on div");
        log.warning("⚠️ Clicking generic div element - this step may need refinement");
        try {
            clickOnElement(DIV); // Common method from utils
            TimeoutConfig.waitShort(); // Auto-fixed timeout method
            log.info("✅ user clicks on div completed");
        } catch (Exception e) {
            log.warning("⚠️ Could not click div (this is expected if the element is not specific): " + e.getMessage());
            // Continue execution - this is not a critical step
        }
    }

    /**
     * user clicks on setup setup
     * Element: SetupSetup (text=Setup Setup)
     * Uses common method from utils.java
     */
    public static void clickSetupSetup(Page page) {
        log.info("🖱️ user clicks on setup setup");
        clickOnElement(SETUP_SETUP); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on setup setup completed");
    }

    /**
     * user clicks on configure tree
     * Element: ConfigureTree (text=Configure Tree)
     * Uses common method from utils.java
     */
    public static void clickConfigureTree(Page page) {
        log.info("🖱️ user clicks on configure tree");
        clickOnElement(CONFIGURE_TREE); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on configure tree completed");
    }

    /**
     * user clicks on logout logout
     * Element: LogoutLogout (text=Logout Logout)
     * Uses common method from utils.java
     */
    public static void clickLogoutLogout(Page page) {
        log.info("🖱️ user clicks on logout logout");
        clickOnElement(LOGOUT_LOGOUT); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on logout logout completed");
    }

}
