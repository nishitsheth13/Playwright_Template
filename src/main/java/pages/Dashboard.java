package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import static configs.utils.*;

/**
 * Page Object for Dashboard
 * Auto-generated from Playwright recording by Pure Java Generator
 * @story AUTO-GEN
 */
public class Dashboard extends BasePage {
    private static final String PAGE_PATH = "";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_2 = "label=Username";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_3 = "label=Username";

    // Password - Priority 4: Label/Name ⭐⭐
    private static final String PASSWORD_4 = "label=Password";

    // SignIn - Priority 6: CSS ⭐
    private static final String SIGN_IN_5 = "text=Sign In";

    // DashboardsDashboards - Priority 6: CSS ⭐
    private static final String DASHBOARDS_DASHBOARDS_6 = "text=Dashboards Dashboards";

    // SelectNewPath - Priority 4: Label/Name ⭐⭐
    private static final String SELECT_NEW_PATH_8 = "label=Select New Path";

    // Edit - Priority 6: CSS ⭐
    private static final String EDIT_9 = "text=Edit";

    // Edit - Priority 6: CSS ⭐
    private static final String EDIT_10 = "text=Edit";

    // Yes - Priority 6: CSS ⭐
    private static final String YES_11 = "text=Yes";

    // SelectNewPath - Priority 4: Label/Name ⭐⭐
    private static final String SELECT_NEW_PATH_12 = "label=Select New Path";

    public static void navigateTo(Page page) {
        page.navigate(loadProps.getProperty("URL") + PAGE_PATH);
        System.out.println("✅ Navigated to Dashboard page: " + loadProps.getProperty("URL") + PAGE_PATH);
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
     * user clicks on dashboards dashboards
     * Element: DashboardsDashboards (text=Dashboards Dashboards)
     */
    public static void clickDashboardsDashboards(Page page) {
        System.out.println("🖱️ user clicks on dashboards dashboards: " + DASHBOARDS_DASHBOARDS_6);
        clickOnElement(DASHBOARDS_DASHBOARDS_6);
    }

    /**
     * user clicks on select new path
     * Element: SelectNewPath (label=Select New Path)
     */
    public static void clickSelectNewPath(Page page) {
        System.out.println("🖱️ user clicks on select new path: " + SELECT_NEW_PATH_8);
        clickOnElement(SELECT_NEW_PATH_8);
    }

    /**
     * user clicks on edit
     * Element: Edit (text=Edit)
     */
    public static void clickEdit(Page page) {
        System.out.println("🖱️ user clicks on edit: " + EDIT_9);
        clickOnElement(EDIT_9);
    }

    /**
     * user clicks on yes
     * Element: Yes (text=Yes)
     */
    public static void clickYes(Page page) {
        System.out.println("🖱️ user clicks on yes: " + YES_11);
        clickOnElement(YES_11);
    }

}
