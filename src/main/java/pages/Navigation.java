package pages;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import configs.loadProps;
import configs.TimeoutConfig;
import java.util.logging.Logger;
import pages.Login;

/**
 * Page Object for Navigation
 * Auto-generated from Playwright recording
 * 
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
    private static final String PAGE_PATH = "";

    /* --------------------
       Locators for Navigation
       -----------------------*/
    
    public static Locator page() {
        return page.locator("page");
    }

    public static Locator mrinavigationbarexpand() {
        return page.locator(".mri-navigation-bar__expand-button");
    }

    public static Locator setup() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Setup"));
    }

    public static Locator tree() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Tree").setExact(true));
    }

    public static Locator configuretree() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Configure Tree"));
    }

    /**
     * NOTE: Similar locator exists in Accessgroup.java: logoutButton()
     * Consider reusing Accessgroup.logoutButton() if applicable.
     */
    public static Locator logoutButton() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout"));
    }

    /**
     * Navigate to Navigation page
     * Uses common navigateToUrl method from BasePage
     * @param page Playwright Page instance
     */
    public static void navigateToNavigation(Page page) {
        // Fail-safe: Check if page is initialized
        if (BasePage.page == null) {
            throw new IllegalStateException("❌ Browser not initialized. Ensure hooks are running: " +
                "1) Check TestRunner @CucumberOptions glue includes 'hooks' " +
                "2) Verify hooks.java @Before method calls browserSelector.setUp()");
        }
        log.info("🌐 Navigating to Navigation page");
        String fullUrl = loadProps.getProperty("URL") + PAGE_PATH;
        navigateToUrl(fullUrl);
        log.info("✅ Navigation completed");
    }

    /**
     * page should be visible
     * Element: Page (page)
     * Uses Locator method
     */
    public static void verifyPage(Page page) {
        log.info("🔍 Verifying: page should be visible");
        // Use Playwright assertions for reliable visibility check with auto-waiting
        PlaywrightAssertions.assertThat(page()).isVisible();
        TimeoutConfig.waitShort();
        log.info("✅ Verification passed: page should be visible");
    }

    /**
     * user clicks on mri navigation bar expand
     * Element: MriNavigationBarExpand (.mri-navigation-bar__expand-button)
     * Uses Locator method
     */
    public static void clickMriNavigationBarExpand(Page page) {
        log.info("🖱️ user clicks on mri navigation bar expand");
        clickOnElement(mrinavigationbarexpand()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on mri navigation bar expand completed");
    }

    /**
     * user clicks on setup
     * Element: Setup (role=link,name=Setup)
     * Uses Locator method
     */
    public static void clickSetup(Page page) {
        log.info("🖱️ user clicks on setup");
        clickOnElement(setup()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on setup completed");
    }

    /**
     * user clicks on tree
     * Element: Tree (role=link,name=Tree)
     * Uses Locator method
     */
    public static void clickTree(Page page) {
        log.info("🖱️ user clicks on tree");
        clickOnElement(tree()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on tree completed");
    }

    /**
     * user clicks on configure tree
     * Element: ConfigureTree (role=link,name=Configure Tree)
     * Uses Locator method
     */
    public static void clickConfigureTree(Page page) {
        log.info("🖱️ user clicks on configure tree");
        clickOnElement(configuretree()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on configure tree completed");
    }

}
