package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import static configs.utils.*;

/**
 * Page Object for Profile
 * Auto-generated from Playwright recording by Pure Java Generator
 * @story AUTO-GEN
 */
public class Profile extends BasePage {
    private static final String PAGE_PATH = "";


    public static void navigateTo(Page page) {
        page.navigate(loadProps.getProperty("URL") + PAGE_PATH);
    }

}
