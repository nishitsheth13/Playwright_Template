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
 * Page Object for Treecomponent
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
public class Treecomponent extends BasePage {
    private static final Logger log = Logger.getLogger(Treecomponent.class.getName());
    private static final String PAGE_PATH = "";

    /* --------------------
       Locators for Treecomponent
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

    public static Locator txtsearchboxvalue() {
        return page.locator("#txtSearchBoxValue");
    }

    public static Locator btnsearchButton() {
        return page.locator("#btnSearch");
    }

    public static Locator mrienergyautomationrootcompany() {
        return page.getByText("MRI_Energy_Automation_Root_Company");
    }

    public static Locator open() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Open"));
    }

    public static Locator treedetails() {
        return page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Tree Details"));
    }

    public static Locator clear() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clear"));
    }

    public static Locator selectfilterField() {
        return page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Select Filter"));
    }

    public static Locator treedetails2() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Tree Details"));
    }

    public static Locator btnfilterButton() {
        return page.locator("#btnFilter");
    }

    public static Locator listfilters() {
        return page.getByText("List Filters");
    }

    public static Locator uxname() {
        return page.locator("#uxName");
    }

    public static Locator uxcolumnDropdown() {
        return page.locator("#uxColumn");
    }

    public static Locator uxfcvaluetextbox() {
        return page.locator("#uxFCValueTextBox");
    }

    public static Locator addtofilter() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to Filter"));
    }

    public static Locator search() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search"));
    }

    public static Locator close() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close"));
    }

    public static Locator save() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
    }

    public static Locator newtreefilter() {
        return page.getByText("New TreeFilter");
    }

    public static Locator logoutButton() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout"));
    }

    /**
     * Navigate to Treecomponent page
     * Uses common navigateToUrl method from BasePage
     * @param page Playwright Page instance
     */
    public static void navigateToTreecomponent(Page page) {
        // Fail-safe: Check if page is initialized
        if (BasePage.page == null) {
            throw new IllegalStateException("❌ Browser not initialized. Ensure hooks are running: " +
                "1) Check TestRunner @CucumberOptions glue includes 'hooks' " +
                "2) Verify hooks.java @Before method calls browserSelector.setUp()");
        }
        log.info("🌐 Navigating to Treecomponent page");
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
     * user clicks on open
     * Element: Open (role=button,name=Open)
     * Uses Locator method
     */
    public static void clickOpen(Page page) {
        log.info("🖱️ user clicks on open");
        clickOnElement(open()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on open completed");
    }

    /**
     * user clicks on tree details
     * Element: TreeDetails (role=option,name=Tree Details)
     * Uses Locator method
     */
    public static void clickTreeDetails(Page page) {
        log.info("🖱️ user clicks on tree details");
        clickOnElement(treedetails()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on tree details completed");
    }

    /**
     * user clicks on clear
     * Element: Clear (role=button,name=Clear)
     * Uses Locator method
     */
    public static void clickClear(Page page) {
        log.info("🖱️ user clicks on clear");
        clickOnElement(clear()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on clear completed");
    }

    /**
     * user enters text into select filter
     * Element: SelectFilter (role=combobox,name=Select Filter)
     * Uses Locator method
     */
    public static void enterSelectFilter(Page page, String text) {
        log.info("⌨️ user enters text into select filter: " + text);
        enterText(selectfilterField(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into select filter completed");
    }

    /**
     * user clicks on btnfilter
     * Element: Btnfilter (#btnFilter)
     * Uses Locator method
     */
    public static void clickBtnfilter(Page page) {
        log.info("🖱️ user clicks on btnfilter");
        clickOnElement(btnfilterButton()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on btnfilter completed");
    }

    /**
     * list filters should be visible
     * Element: ListFilters (text=List Filters)
     * Uses Locator method
     */
    public static void verifyListFilters(Page page) {
        log.info("🔍 Verifying: list filters should be visible");
        // Use Playwright assertions for reliable visibility check with auto-waiting
        PlaywrightAssertions.assertThat(listfilters()).isVisible();
        TimeoutConfig.waitShort();
        log.info("✅ Verification passed: list filters should be visible");
    }

    /**
     * user clicks on uxname
     * Element: Uxname (#uxName)
     * Uses Locator method
     */
    public static void clickUxname(Page page) {
        log.info("🖱️ user clicks on uxname");
        clickOnElement(uxname()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on uxname completed");
    }

    /**
     * user enters text into uxname
     * Element: Uxname (#uxName)
     * Uses Locator method
     */
    public static void enterUxname(Page page, String text) {
        log.info("⌨️ user enters text into uxname: " + text);
        enterText(uxname(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into uxname completed");
    }

    /**
     * user selects option from uxcolumn
     * Element: Uxcolumn (#uxColumn)
     * Uses Locator method
     */
    public static void selectUxcolumn(Page page, String option) {
        log.info("🔽 user selects option from uxcolumn: " + option);
        selectDropDownValueByText(uxcolumnDropdown(), option); // Uses utils.selectDropDownValueByText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user selects option from uxcolumn completed");
    }

    /**
     * user clicks on uxfcvaluetextbox
     * Element: Uxfcvaluetextbox (#uxFCValueTextBox)
     * Uses Locator method
     */
    public static void clickUxfcvaluetextbox(Page page) {
        log.info("🖱️ user clicks on uxfcvaluetextbox");
        clickOnElement(uxfcvaluetextbox()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on uxfcvaluetextbox completed");
    }

    /**
     * user enters text into uxfcvaluetextbox
     * Element: Uxfcvaluetextbox (#uxFCValueTextBox)
     * Uses Locator method
     */
    public static void enterUxfcvaluetextbox(Page page, String text) {
        log.info("⌨️ user enters text into uxfcvaluetextbox: " + text);
        enterText(uxfcvaluetextbox(), text); // Uses utils.enterText(Locator, String)
        TimeoutConfig.waitShort();
        log.info("✅ user enters text into uxfcvaluetextbox completed");
    }

    /**
     * user clicks on add to filter
     * Element: AddToFilter (role=button,name=Add to Filter)
     * Uses Locator method
     */
    public static void clickAddToFilter(Page page) {
        log.info("🖱️ user clicks on add to filter");
        clickOnElement(addtofilter()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on add to filter completed");
    }

    /**
     * user clicks on search
     * Element: Search (role=button,name=Search)
     * Uses Locator method
     */
    public static void clickSearch(Page page) {
        log.info("🖱️ user clicks on search");
        clickOnElement(search()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on search completed");
    }

    /**
     * user clicks on close
     * Element: Close (role=button,name=Close)
     * Uses Locator method
     */
    public static void clickClose(Page page) {
        log.info("🖱️ user clicks on close");
        clickOnElement(close()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on close completed");
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
     * user clicks on new treefilter
     * Element: NewTreefilter (text=New TreeFilter)
     * Uses Locator method
     */
    public static void clickNewTreefilter(Page page) {
        log.info("🖱️ user clicks on new treefilter");
        clickOnElement(newtreefilter()); // Uses utils.clickOnElement(Locator)
        TimeoutConfig.waitShort();
        log.info("✅ user clicks on new treefilter completed");
    }

}
