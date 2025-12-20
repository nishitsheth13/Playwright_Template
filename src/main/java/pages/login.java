package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import configs.browserSelector;
import configs.loadProps;
import configs.utils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Page Object class for Login functionality.
 * Contains locators and methods for login/logout operations.
 */
public class login extends utils {

    // User credentials from configuration
    public static final String U_name = loadProps.getProperty("Username");
    public static final String P_word = loadProps.getProperty("Password");
    public static final String URL = loadProps.getProperty("URL");
    public static final String InValid_Password = generateRandomText(8);

    // Page Locators
    public static final String username = "//input[@id='Username']";
    public static final String password = "//input[@id='Password']";
    public static final String SignInButton = "//button[@name='button']";
    public static final String UserIcon = "//span[@class='menu_user_title']";
    public static final String logoutButton = "//*[@id=\"Logout\"]/li[1]/a/div/span";

    /**
     * Performs login with valid credentials.
     * Checks if already logged in before attempting login.
     * 
     * @throws IOException if any IO error occurs
     */
    @Test
    public static void login() throws IOException {
        if (page.isVisible(username)) {
            System.out.println("🔐 Attempting to login...");
            enterText(username, U_name);
            enterText(password, P_word);
            clickOnElement(SignInButton);
            
            // Wait for navigation after login
            page.waitForLoadState();
            System.out.println("✅ User logged in successfully");
        } else {
            System.out.println("ℹ️ User already logged in");
        }
    }

    /**
     * Performs login with invalid credentials for negative testing.
     */
    @Test
    public static void Failed_login() {
        System.out.println("🔐 Attempting login with invalid credentials...");
        enterText(username, U_name);
        enterText(password, InValid_Password);
        clickOnElement(SignInButton);
        System.out.println("⚠️ Login attempt with invalid password: " + InValid_Password);
    }

    /**
     * Performs logout operation.
     * If logout button not found, navigates to login page.
     */
    public static void logout() {
        try {
            if (isElementPresent(logoutButton)) {
                clickOnElement(logoutButton);
                System.out.println("✅ User logged out successfully");
            } else {
                page.navigate(URL);
                System.out.println("ℹ️ Navigated to login page");
            }
        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            page.navigate(URL);
        }
    }


    public static void Invoice() throws IOException {
        login();
        page.navigate("https://uksestdevtest02.ukest.lan/MRIEnergy/AdvancedWeb/invoices");
        page.locator("a").filter(new Locator.FilterOptions().setHasText("Invoice Validation")).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Invoices")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.locator("#a0b29b05-8658-4c16-8406-a66284d88e7e").click();
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Portfolio")).fill("mri");
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Portfolio")).press("Escape");
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Portfolio")).fill("mri_Energy_Root_Portfolio");
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("MRI_Energy_Root_Portfolio")).click();
        page.locator("#b6869c81-3018-4b08-bd05-82751e796a72").click();
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Show All")).fill("MRI_Energy_Account");
        page.getByText("MRI_Energy_Account").click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Invoice Number*")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Invoice Number*")).fill("5002");
        page.locator("[id=\"\\31 4bcd024-b659-4adf-bdb9-f4c62f05eee9\"]").click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Go to the previous period")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Go to the previous period")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Go to the previous period")).click();
        page.getByLabel("Wednesday, 1 January").getByText("1").click();
        page.locator("#e8b667f0-9b0a-4ee2-9f68-f758bc518189").click();
        page.getByText("30").click();
        page.locator("[id=\"\\36 c32a64d-f55d-4043-bbab-bfd7503c4aeb\"]").click();
        page.getByLabel("Friday, 4 April").getByText("4").click();
        page.locator("[id=\"\\34 d125b2a-89e1-4f9d-9992-5a926457f4cb\"]").click();
        page.getByLabel("Friday, 4 April").getByText("4").click();
        page.locator(".col-4 > span > .k-clear-value").click();
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Currency*")).fill("gbp");
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("British Pound (GBP)")).click();
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Net Total ( £ )")).click();
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Net Total ( £ )")).fill("1500");
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Tax Total ( £ )")).click();
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Tax Total ( £ )")).fill("0200");
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Invoice Total ( £ )")).click();
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Invoice Total ( £ )")).fill("01700");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Internal Reference")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Internal Reference")).fill("Automation_Test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Attachments")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select files...")).setInputFiles(Paths.get("TenantAccountBillMeterModelIssue.webm"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Attachments")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").first().click();
        page.locator("div:nth-child(18) > button").click();
        page.locator("#ddChargeDescription").click();
        page.locator("#bc207946-5e3b-415c-a562-1128f2f8dbe4").click();
        page.getByText("Unit Charge 1 Cost").click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).nth(2).click();
        page.getByText("All Charges").click();
        page.getByRole(AriaRole.COMBOBOX).fill("Specific");
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Specific Meter Charges")).click();
        page.getByText("Please select").click();
        page.getByRole(AriaRole.GRIDCELL, new Page.GetByRoleOptions().setName("Open")).getByLabel("Open").click();
        page.getByLabel("Please select").getByText("100000000001").click();
        page.getByRole(AriaRole.GRIDCELL, new Page.GetByRoleOptions().setName("0").setExact(true)).click();
        page.getByRole(AriaRole.SPINBUTTON).click();
        page.getByRole(AriaRole.SPINBUTTON).fill("10");
        page.getByRole(AriaRole.GRIDCELL, new Page.GetByRoleOptions().setName("£ 2,820.00")).click();
        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName("Specific Meter Charges 100000000001 Electric Meter (Energy_SN01) 17.5 100 1,200")).getByRole(AriaRole.BUTTON).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save and Validate")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Revalidate")).click();

    }

    @AfterTest
    public void closeBrowser() throws Exception {

        browserSelector.closeBrowser();

    }


    @BeforeTest
    @Parameters("Browsers")
    public void launchBrowser(String Browser) throws Exception {
        browserSelector.LaunchBrowser(Browser);
    }

}
