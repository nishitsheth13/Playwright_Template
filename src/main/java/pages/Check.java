package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import static configs.utils.*;

/**
 * Page Object for Check
 * Auto-generated from Playwright recording by Pure Java Generator
 * @story AUTO-GEN
 */
public class Check extends BasePage {
    private static final String PAGE_PATH = "";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_2 = "label=Username";

    // Password - Priority 4: Label/Name ⭐⭐
    private static final String PASSWORD_4 = "label=Password";

    // SignIn - Priority 6: CSS ⭐
    private static final String SIGN_IN_5 = "text=Sign In";

    // SetupSetup - Priority 6: CSS ⭐
    private static final String SETUP_SETUP_6 = "text=Setup Setup";

    // SetupTreeConfigureTree - Priority 6: CSS ⭐
    private static final String SETUP_TREE_CONFIGURE_TREE_7 = "text=Setup > Tree > Configure Tree";

    // LogoutLogout - Priority 6: CSS ⭐
    private static final String LOGOUT_LOGOUT_8 = "text=Logout Logout";

    public static void navigateTo(Page page) {
        page.navigate(loadProps.getProperty("URL") + PAGE_PATH);
        System.out.println("✅ Navigated to Check page: " + loadProps.getProperty("URL") + PAGE_PATH);
    }

    /**
     * user enters text into username
     * Element: Username (label=Username)
     */
    public static void enterUsername(Page page, String text) {
        System.out.println("⌨️ user enters text into username: " + USERNAME_2 + " = '" + text + "'");
        enterText(USERNAME_2, text);
    }

    /**
     * user presses key on username
     * Element: Username (label=Username)
     */
    public static void pressKeyOnUsername(Page page) {
        System.out.println("⌨️ user presses key on username: " + USERNAME_3 + " - Key: Tab");
        page.locator(USERNAME_3).press("Tab");
    }

    /**
     * user enters text into password
     * Element: Password (label=Password)
     */
    public static void enterPassword(Page page, String text) {
        System.out.println("⌨️ user enters text into password: " + PASSWORD_4 + " = '" + text + "'");
        enterText(PASSWORD_4, text);
    }

    /**
     * user clicks on sign in
     * Element: SignIn (text=Sign In)
     */
    public static void clickSignIn(Page page) {
        System.out.println("🖱️ user clicks on sign in: " + SIGN_IN_5);
        clickOnElement(SIGN_IN_5);
    }

    /**
     * user clicks on setup setup
     * Element: SetupSetup (text=Setup Setup)
     */
    public static void clickSetupSetup(Page page) {
        System.out.println("🖱️ user clicks on setup setup: " + SETUP_SETUP_6);
        clickOnElement(SETUP_SETUP_6);
    }

    /**
     * user clicks on setup tree configure tree
     * Element: SetupTreeConfigureTree (text=Setup > Tree > Configure Tree)
     */
    public static void clickSetupTreeConfigureTree(Page page) {
        System.out.println("🖱️ user clicks on setup tree configure tree: " + SETUP_TREE_CONFIGURE_TREE_7);
        clickOnElement(SETUP_TREE_CONFIGURE_TREE_7);
    }

    /**
     * user clicks on logout logout
     * Element: LogoutLogout (text=Logout Logout)
     */
    public static void clickLogoutLogout(Page page) {
        System.out.println("🖱️ user clicks on logout logout: " + LOGOUT_LOGOUT_8);
        clickOnElement(LOGOUT_LOGOUT_8);
    }

}
