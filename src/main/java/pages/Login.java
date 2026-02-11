package pages;

import java.util.logging.Logger;
import org.testng.Assert;
import configs.SmartLocatorStrategy;
import configs.TimeoutConfig;

public class Login extends BasePage {
    private static final Logger log = Logger.getLogger(Login.class.getName());

    private static final String[] USERNAME_LOCATORS = SmartLocatorStrategy.generateInputStrategies("username", "text");
    private static final String[] PASSWORD_LOCATORS = SmartLocatorStrategy.generateInputStrategies("password", "password");
    private static final String[] SIGNIN_BUTTON_LOCATORS = SmartLocatorStrategy.generateButtonStrategies("Sign In");

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
        enterTextSmart(text, USERNAME_LOCATORS);
        TimeoutConfig.waitShort();
    }

    public static void PasswordField(String text) {
        enterTextSmart(text, PASSWORD_LOCATORS);
        TimeoutConfig.waitShort();
    }

    public static void SignInButton() {
        clickElementSmart(SIGNIN_BUTTON_LOCATORS);
        TimeoutConfig.waitShort();
    }

    protected static void verifyPageLoaded(String expectedUrlPart) {
        Assert.assertTrue(isUrlContains(expectedUrlPart), "Page URL verification failed");
    }
}