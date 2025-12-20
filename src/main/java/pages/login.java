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
    public static final String logoutButton = "//a[contains(@href, 'Logout') or contains(text(), 'Logout') or @id='Logout']//span";
    public static final String rememberMeCheckbox = "//input[@id='RememberLogin' or @name='RememberLogin' or (@type='checkbox' and contains(@class, 'checkbox'))]";
    public static final String forgotUsernameLink = "//a[contains(text(), 'Forgot Username')]";
    public static final String forgotPasswordLink = "//a[contains(text(), 'Forgot Password')]";
    public static final String errorMessage = "//div[contains(@class, 'alert') or contains(@class, 'error')]";
    public static final String loginLogo = "//img[contains(@src, 'logo') or contains(@alt, 'MRI')]";
    public static final String versionInfo = "//div[contains(text(), 'Version')]";
    
    // Forgot Password Page Locators
    public static final String forgotPasswordEmailInput = "//input[@type='email' or contains(@id, 'Email')]";
    public static final String receiveViaEmailButton = "//button[contains(text(), 'Receive via Email')]";
    public static final String backToSignInButton = "//button[contains(text(), 'Back to sign in')]";
    public static final String forgotPasswordTitle = "//h4[contains(text(), 'Forgot Password')]";
    public static final String responseMessage = "//div[contains(@class, 'modal') or contains(@role, 'alert')]//div[last()]";

    /**
     * Navigates to the login page URL.
     */
    public static void navigateToLoginPage() {
        System.out.println("🌐 Navigating to login page...");
        page.navigate(URL);
        page.waitForLoadState();
        System.out.println("✅ Login page loaded");
    }

    /**
     * Enters username in the username field.
     * 
     * @param user The username to enter
     */
    public static void enterUsername(String user) {
        System.out.println("🔹 Entering username: " + user);
        clearAndEnterText(username, user);
        System.out.println("✅ Username entered");
    }

    /**
     * Enters password in the password field.
     * 
     * @param pass The password to enter
     */
    public static void enterPassword(String pass) {
        System.out.println("🔹 Entering password");
        clearAndEnterText(password, pass);
        System.out.println("✅ Password entered");
    }

    /**
     * Enters valid username from configuration.
     */
    public static void enterValidUsername() {
        enterUsername(U_name);
    }

    /**
     * Enters valid password from configuration.
     */
    public static void enterValidPassword() {
        enterPassword(P_word);
    }

    /**
     * Clicks the Sign In button.
     */
    public static void clickSignInButton() {
        System.out.println("🔹 Clicking Sign In button...");
        clickOnElement(SignInButton);
        page.waitForLoadState();
        System.out.println("✅ Sign In button clicked");
    }

    /**
     * Checks the Remember Me checkbox.
     */
    public static void checkRememberMe() {
        System.out.println("🔹 Checking Remember Me checkbox...");
        if (!page.locator(rememberMeCheckbox).isChecked()) {
            clickOnElement(rememberMeCheckbox);
        }
        System.out.println("✅ Remember Me checked");
    }

    /**
     * Verifies if Remember Me is enabled.
     * 
     * @return true if Remember Me is checked
     */
    public static boolean isRememberMeChecked() {
        page.locator(rememberMeCheckbox).waitFor(new Locator.WaitForOptions().setTimeout(10000));
        return page.locator(rememberMeCheckbox).isChecked();
    }

    /**
     * Clicks on Forgot Username link.
     */
    public static void clickForgotUsername() {
        System.out.println("🔹 Clicking Forgot Username link...");
        clickOnElement(forgotUsernameLink);
        page.waitForLoadState();
        System.out.println("✅ Forgot Username clicked");
    }

    /**
     * Clicks on Forgot Password link.
     */
    public static void clickForgotPassword() {
        System.out.println("🔹 Clicking Forgot Password link...");
        clickOnElement(forgotPasswordLink);
        page.waitForLoadState();
        System.out.println("✅ Forgot Password clicked");
    }

    /**
     * Checks if error message is displayed.
     * 
     * @return true if error message is visible
     */
    public static boolean isErrorMessageDisplayed() {
        return isElementPresent(errorMessage);
    }

    /**
     * Gets the error message text.
     * 
     * @return The error message text
     */
    public static String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            return getElementText(errorMessage);
        }
        return "";
    }

    /**
     * Verifies if user is on login page.
     * 
     * @return true if on login page
     */
    public static boolean isOnLoginPage() {
        return page.url().contains("Login") && isElementPresent(SignInButton);
    }

    /**
     * Verifies if user is logged in successfully.
     * 
     * @return true if logged in
     */
    public static boolean isUserLoggedIn() {
        page.waitForLoadState();
        return !isElementPresent(SignInButton) && !page.url().contains("Login");
    }

    /**
     * Verifies if username field is visible.
     * 
     * @return true if username field is visible
     */
    public static boolean isUsernameFieldVisible() {
        return isElementPresent(username);
    }

    /**
     * Verifies if password field is visible.
     * 
     * @return true if password field is visible
     */
    public static boolean isPasswordFieldVisible() {
        return isElementPresent(password);
    }

    /**
     * Verifies if Sign In button is visible.
     * 
     * @return true if Sign In button is visible
     */
    public static boolean isSignInButtonVisible() {
        return isElementPresent(SignInButton);
    }

    /**
     * Verifies if Remember Me checkbox is visible.
     * 
     * @return true if Remember Me checkbox is visible
     */
    public static boolean isRememberMeVisible() {
        return isElementPresent(rememberMeCheckbox);
    }

    /**
     * Verifies if Forgot Username link is visible.
     * 
     * @return true if Forgot Username link is visible
     */
    public static boolean isForgotUsernameVisible() {
        return isElementPresent(forgotUsernameLink);
    }

    /**
     * Verifies if Forgot Password link is visible.
     * 
     * @return true if Forgot Password link is visible
     */
    public static boolean isForgotPasswordVisible() {
        return isElementPresent(forgotPasswordLink);
    }

    // ============================================================
    // FORGOT PASSWORD FUNCTIONALITY
    // ============================================================

    /**
     * Clicks the Forgot Password link on login page.
     */
    public static void clickForgotPasswordLink() {
        System.out.println("🔹 Clicking Forgot Password link...");
        clickOnElement(forgotPasswordLink);
        page.waitForLoadState();
        System.out.println("✅ Navigated to Forgot Password page");
    }

    /**
     * Enters email in forgot password page.
     * 
     * @param email The email address to enter
     */
    public static void enterForgotPasswordEmail(String email) {
        System.out.println("📧 Entering email: " + email);
        clearAndEnterText(forgotPasswordEmailInput, email);
        System.out.println("✅ Email entered");
    }

    /**
     * Clicks the Receive via Email button.
     */
    public static void clickReceiveViaEmailButton() {
        System.out.println("🔹 Clicking Receive via Email button...");
        clickOnElement(receiveViaEmailButton);
        page.waitForTimeout(2000); // Wait for response
        System.out.println("✅ Receive via Email button clicked");
    }

    /**
     * Clicks the Back to sign in button.
     */
    public static void clickBackToSignInButton() {
        System.out.println("🔹 Clicking Back to sign in button...");
        clickOnElement(backToSignInButton);
        page.waitForLoadState();
        System.out.println("✅ Returned to login page");
    }

    /**
     * Checks if forgot password page is displayed.
     * 
     * @return true if forgot password page title is present
     */
    public static boolean isForgotPasswordPageDisplayed() {
        return isElementPresent(forgotPasswordTitle);
    }

    /**
     * Checks if email input field is displayed on forgot password page.
     * 
     * @return true if email input is present
     */
    public static boolean isEmailInputDisplayed() {
        return isElementPresent(forgotPasswordEmailInput);
    }

    /**
     * Checks if response message is displayed after submitting email.
     * 
     * @return true if response message is present
     */
    public static boolean isResponseMessageDisplayed() {
        return isElementPresent(responseMessage);
    }

    /**
     * Gets the response message text.
     * 
     * @return The response message text
     */
    public static String getResponseMessageText() {
        return getElementText(responseMessage);
    }

    /**
     * Verifies if logo is visible.
     * 
     * @return true if logo is visible
     */
    public static boolean isLogoVisible() {
        return isElementPresent(loginLogo);
    }

    /**
     * Gets the page title.
     * 
     * @return The page title
     */
    public static String getPageTitle() {
        return page.title();
    }

    /**
     * Gets version information from footer.
     * 
     * @return Version information text
     */
    public static String getVersionInfo() {
        if (isElementPresent(versionInfo)) {
            return getElementText(versionInfo);
        }
        return "";
    }

    /**
     * Verifies if password is masked.
     * 
     * @return true if password field type is password
     */
    public static boolean isPasswordMasked() {
        String fieldType = page.locator(password).getAttribute("type");
        return "password".equalsIgnoreCase(fieldType);
    }

    /**
     * Clears the username field.
     */
    public static void clearUsernameField() {
        System.out.println("🔹 Clearing username field...");
        page.locator(username).clear();
        System.out.println("✅ Username field cleared");
    }

    /**
     * Clears the password field.
     */
    public static void clearPasswordField() {
        System.out.println("🔹 Clearing password field...");
        page.locator(password).clear();
        System.out.println("✅ Password field cleared");
    }

    /**
     * Performs login with specific credentials.
     * 
     * @param user The username
     * @param pass The password
     */
    public static void loginWith(String user, String pass) {
        System.out.println("🔐 Attempting login with provided credentials...");
        enterUsername(user);
        enterPassword(pass);
        clickSignInButton();
    }

    /**
     * Performs login with valid credentials multiple times.
     * 
     * @param attempts Number of login attempts
     * @param invalidPassword The invalid password to use
     */
    public static void attemptLoginMultipleTimes(int attempts, String invalidPassword) {
        System.out.println("🔐 Attempting login " + attempts + " times with invalid password...");
        for (int i = 0; i < attempts; i++) {
            if (isOnLoginPage()) {
                enterValidUsername();
                enterPassword(invalidPassword);
                clickSignInButton();
                page.waitForTimeout(1000);
                System.out.println("⚠️ Login attempt " + (i + 1) + " failed");
            }
        }
    }

    /**
     * Performs login with valid credentials.
     * Checks if already logged in before attempting login.
     * 
     * @throws IOException if any IO error occurs
     */
    @Test
    public static void performLogin() throws IOException {
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
     */
    public static void logout() {
        try {
            if (isElementPresent(UserIcon)) {
                clickOnElement(UserIcon);
                page.waitForTimeout(2000);
            }
            
            String[] logoutSelectors = {
                "//a[contains(@href, 'Logout')]",
                "//span[contains(text(), 'Logout')]",
                "//*[@id='Logout']//span",
                "//li[contains(@class, 'logout')]//span",
                logoutButton
            };
            
            boolean loggedOut = false;
            for (String selector : logoutSelectors) {
                if (isElementPresent(selector)) {
                    page.locator(selector).scrollIntoViewIfNeeded();
                    page.waitForTimeout(500);
                    clickOnElement(selector);
                    page.waitForLoadState();
                    loggedOut = true;
                    break;
                }
            }
            
            if (!loggedOut) {
                page.navigate(URL);
            }
        } catch (Exception e) {
            page.navigate(URL);
        }
    }


    public static void Invoice() throws IOException {
        performLogin();
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
