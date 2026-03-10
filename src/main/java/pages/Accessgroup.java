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
 * Page Object for Accessgroup
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
public class Accessgroup extends BasePage {
    private static final Logger log = Logger.getLogger(Accessgroup.class.getName());
    private static final String PAGE_PATH = "";

    /* --------------------
       Locators for Accessgroup
       -----------------------*/
    
    public static Locator setup() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Setup"));
    }

    public static Locator security() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Security"));
    }

    public static Locator accessgroups() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Access Groups"));
    }

    public static Locator add() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add"));
    }

    public static Locator pleaseenteranameField() {
        return page.getByPlaceholder("Please enter a name.");
    }

    public static Locator txtsearchboxvalue() {
        return page.locator("#txtSearchBoxValue");
    }

    public static Locator btnsearchButton() {
        return page.locator("#btnSearch");
    }

    public static Locator mrienergyautomationrootcompany() {
        return page.getByText("MRI_Energy_Automation_Root_Company");
    }

    public static Locator save() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
    }

    public static Locator clearfilter() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clear Filter"));
    }

    public static Locator searchField() {
        return page.getByPlaceholder("Search");
    }

    public static Locator automationtest() {
        return page.getByText("AutomationTest");
    }

    public static Locator logoutButton() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout"));
    }

    /**
     * Navigate to Accessgroup page
     * Uses common navigateToUrl method from BasePage
     * @param page Playwright Page instance
     */
    public static void navigateToAccessgroup(Page page) {
        // Fail-safe: Check if page is initialized
        if (BasePage.page == null) {
            throw new IllegalStateException("❌ Browser not initialized. Ensure hooks are running: " +
                "1) Check TestRunner @CucumberOptions glue includes 'hooks' " +
                "2) Verify hooks.java @Before method calls browserSelector.setUp()");
        }
        log.info("🌐 Navigating to Accessgroup page");
        String fullUrl = loadProps.getProperty("URL") + PAGE_PATH;
        navigateToUrl(fullUrl);
        log.info("✅ Navigation completed");
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
     * user clicks on security
     * Element: Security (role=link,name=Security)
     * Uses Locator method
     */
    public static void clickSecurity(Page page) {
        log.info("🖱️ user clicks on security");
        clickOnElement(security()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on security completed");
    }

    /**
     * user clicks on access groups
     * Element: AccessGroups (role=link,name=Access Groups)
     * Uses Locator method
     */
    public static void clickAccessGroups(Page page) {
        log.info("🖱️ user clicks on access groups");
        clickOnElement(accessgroups()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on access groups completed");
    }

    /**
     * user clicks on add
     * Element: Add (role=button,name=Add)
     * Uses Locator method
     */
    public static void clickAdd(Page page) {
        log.info("🖱️ user clicks on add");
        clickOnElement(add()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on add completed");
    }

    /**
     * user enters text into please enter aname
     * Element: PleaseEnterAName (placeholder=Please enter a name.)
     * Uses Locator method
     */
    public static void enterPleaseEnterAName(Page page, String text) {
        log.info("⌨️ user enters text into please enter aname: " + text);
        enterText(pleaseenteranameField(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into please enter aname completed");
    }

    /**
     * user clicks on txtsearchboxvalue
     * Element: Txtsearchboxvalue (#txtSearchBoxValue)
     * Uses Locator method
     */
    public static void clickTxtsearchboxvalue(Page page) {
        log.info("🖱️ user clicks on txtsearchboxvalue");
        clickOnElement(txtsearchboxvalue()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on txtsearchboxvalue completed");
    }

    /**
     * user enters text into txtsearchboxvalue
     * Element: Txtsearchboxvalue (#txtSearchBoxValue)
     * Uses Locator method
     */
    public static void searchTxtsearchboxvalue(Page page, String text) {
        log.info("⌨️ user enters text into txtsearchboxvalue: " + text);
        enterText(txtsearchboxvalue(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into txtsearchboxvalue completed");
    }

    /**
     * user clicks on btnsearch
     * Element: Btnsearch (#btnSearch)
     * Uses Locator method
     */
    public static void clickBtnsearch(Page page) {
        log.info("🖱️ user clicks on btnsearch");
        clickOnElement(btnsearchButton()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on btnsearch completed");
    }

    /**
     * mri energy automation root company should be visible
     * Element: MriEnergyAutomationRootCompany (text=MRI_Energy_Automation_Root_Company)
     * Uses Locator method
     */
    public static void verifyMriEnergyAutomationRootCompany(Page page) {
        log.info("🔍 Verifying: mri energy automation root company should be visible");
        // Use Playwright assertions for reliable visibility check with auto-waiting
        PlaywrightAssertions.assertThat(mrienergyautomationrootcompany()).isVisible();
        TimeoutConfig.waitShort();
        log.info("✅ Verification passed: mri energy automation root company should be visible");
    }

    /**
     * user clicks on mri energy automation root company
     * Element: MriEnergyAutomationRootCompany (text=MRI_Energy_Automation_Root_Company)
     * Uses Locator method
     */
    public static void clickMriEnergyAutomationRootCompany(Page page) {
        log.info("🖱️ user clicks on mri energy automation root company");
        clickOnElement(mrienergyautomationrootcompany()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on mri energy automation root company completed");
    }

    /**
     * user clicks on save
     * Element: Save (role=button,name=Save)
     * Uses Locator method
     */
    public static void clickSave(Page page) {
        log.info("🖱️ user clicks on save");
        clickOnElement(save()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on save completed");
    }

    /**
     * user clicks on clear filter
     * Element: ClearFilter (role=button,name=Clear Filter)
     * Uses Locator method
     */
    public static void clickClearFilter(Page page) {
        log.info("🖱️ user clicks on clear filter");
        clickOnElement(clearfilter()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on clear filter completed");
    }

    /**
     * user enters text into search
     * Element: Search (placeholder=Search)
     * Uses Locator method
     */
    public static void search(Page page, String text) {
        log.info("⌨️ user enters text into search: " + text);
        enterText(searchField(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into search completed");
    }

    /**
     * automationtest should be visible
     * Element: Automationtest (text=AutomationTest)
     * Uses Locator method
     */
    public static void verifyAutomationtest(Page page) {
        log.info("🔍 Verifying: automationtest should be visible");
        // Use Playwright assertions for reliable visibility check with auto-waiting
        PlaywrightAssertions.assertThat(automationtest()).isVisible();
        TimeoutConfig.waitShort();
        log.info("✅ Verification passed: automationtest should be visible");
    }

}
