package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import configs.TimeoutConfig;
import org.testng.Assert;

/**
 * Page Object for Login functionality.
 * Uses Locator pattern and utils.java for all element interactions.
 */
public class Login extends BasePage {

    public static Locator usernameField() {
        return page.locator("input[type='email'], input[name='username'], input[name*='user' i]");
    }

    public static Locator passwordField() {
        return page.locator("input[type='password'], input[name='password']");
    }

    public static Locator signInButton() {
        return page.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions().setName("Sign In"));
    }

    public Login() {
        super();
    }

    public static void navigateTo(com.microsoft.playwright.Page page) {
        String url = configs.loadProps.getProperty(configs.loadProps.PropKeys.URL);
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("URL is not configured in properties file.");
        }
        navigateToUrl(url);
    }

    public static void UsernameField(String text) {
        enterText(usernameField(), text);
        TimeoutConfig.waitShort();
    }

    public static void PasswordField(String text) {
        enterText(passwordField(), text);
        TimeoutConfig.waitShort();
    }

    public static void SignInButton() {
        clickOnElement(signInButton());
        TimeoutConfig.waitShort();
    }

    protected static void verifyPageLoaded(String expectedUrlPart) {
        Assert.assertTrue(isUrlContains(expectedUrlPart), "Page URL verification failed");
    }
}