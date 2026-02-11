package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import configs.TimeoutConfig;
import java.util.logging.Logger;

/**
 * Page Object for Profile
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
public class Profile extends BasePage {
    private static final Logger log = Logger.getLogger(Profile.class.getName());
    private static final String PAGE_PATH = "/MRIEnergy";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME = "label=Username";

    // Password - Priority 4: Label/Name ⭐⭐
    private static final String PASSWORD = "label=Password";

    // SignIn - Priority 6: CSS ⭐
    private static final String SIGN_IN = "text=Sign In";

    // MobilePhoneNumber - Priority 4: Label/Name ⭐⭐
    private static final String MOBILE_PHONE_NUMBER = "label=Mobile Phone Number";

    // Save - Priority 6: CSS ⭐
    private static final String SAVE = "text=Save";

    // LogoutLogout - Priority 6: CSS ⭐
    private static final String LOGOUT_LOGOUT = "text=Logout Logout";

    /**
     * Navigate to Profile page
     * Uses common navigateToUrl method from BasePage
     * @param page Playwright Page instance
     */
    public static void navigateToProfile(Page page) {
        log.info("🌐 Navigating to Profile page");
        String fullUrl = loadProps.getProperty("URL") + PAGE_PATH;
        navigateToUrl(fullUrl);
        log.info("✅ Navigation completed");
    }

    /**
     * user clicks on username
     * Element: Username (label=Username)
     * Uses common method from utils.java
     */
    public static void clickUsername(Page page) {
        log.info("🖱️ user clicks on username");
        clickOnElement(USERNAME); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on username completed");
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
     * user clicks on mobile phone number
     * Element: MobilePhoneNumber (label=Mobile Phone Number)
     * Uses common method from utils.java
     */
    public static void clickMobilePhoneNumber(Page page) {
        log.info("🖱️ user clicks on mobile phone number");
        clickOnElement(MOBILE_PHONE_NUMBER); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on mobile phone number completed");
    }

    /**
     * user enters text into mobile phone number
     * Element: MobilePhoneNumber (label=Mobile Phone Number)
     * Uses common method from utils.java
     */
    public static void enterMobilePhoneNumber(Page page, String text) {
        log.info("⌨️ user enters text into mobile phone number: " + text);
        enterText(MOBILE_PHONE_NUMBER, text); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user enters text into mobile phone number completed");
    }

    /**
     * user clicks on save
     * Element: Save (text=Save)
     * Uses common method from utils.java
     */
    public static void clickSave(Page page) {
        log.info("🖱️ user clicks on save");
        clickOnElement(SAVE); // Common method from utils
        TimeoutConfig.waitShort(); // Auto-fixed timeout method
        log.info("✅ user clicks on save completed");
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
