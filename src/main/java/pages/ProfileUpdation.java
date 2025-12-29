package pages;
import com.microsoft.playwright.Page;
import configs.loadProps;
import static configs.utils.*;

/**
 * Page Object for ProfileUpdation
 * Auto-generated from Playwright recording by Pure Java Generator
 * @story AUTO-GEN
 */
public class ProfileUpdation extends BasePage {
    private static final String PAGE_PATH = "";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_2 = "label=Username";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_3 = "label=Username";

    // Username - Priority 4: Label/Name ⭐⭐
    private static final String USERNAME_4 = "label=Username";

    // Password - Priority 4: Label/Name ⭐⭐
    private static final String PASSWORD_5 = "label=Password";

    // SignIn - Priority 6: CSS ⭐
    private static final String SIGN_IN_6 = "text=Sign In";

    // FirstName - Priority 4: Label/Name ⭐⭐
    private static final String FIRST_NAME_7 = "label=First Name";

    // FirstName - Priority 4: Label/Name ⭐⭐
    private static final String FIRST_NAME_8 = "label=First Name";

    // LastName - Priority 4: Label/Name ⭐⭐
    private static final String LAST_NAME_9 = "label=Last Name";

    // LastName - Priority 4: Label/Name ⭐⭐
    private static final String LAST_NAME_10 = "label=Last Name";

    // MobilePhoneNumber - Priority 4: Label/Name ⭐⭐
    private static final String MOBILE_PHONE_NUMBER_11 = "label=Mobile Phone Number";

    // MobilePhoneNumber - Priority 4: Label/Name ⭐⭐
    private static final String MOBILE_PHONE_NUMBER_12 = "label=Mobile Phone Number";

    // B9ddca9b7382468eA006273c27d735b5 - Priority 6: CSS ⭐
    private static final String B9DDCA9B7382468E_A006273C27D735B5_13 = "#b9ddca9b-7382-468e-a006-273c27d735b5";

    // Number25 - Priority 6: CSS ⭐
    private static final String NUMBER25_14 = "text=25";

    // Add - Priority 6: CSS ⭐
    private static final String ADD_15 = "text=Add";

    // Save - Priority 6: CSS ⭐
    private static final String SAVE_16 = "text=Save";

    // LogoutLogout - Priority 6: CSS ⭐
    private static final String LOGOUT_LOGOUT_17 = "text=Logout Logout";

    public static void navigateTo(Page page) {
        page.navigate(loadProps.getProperty("URL") + PAGE_PATH);
        System.out.println("✅ Navigated to ProfileUpdation page: " + loadProps.getProperty("URL") + PAGE_PATH);
    }

    /**
     * user clicks on username
     * Element: Username (label=Username)
     */
    public static void clickUsername(Page page) {
        System.out.println("🖱️ user clicks on username: " + USERNAME_2);
        clickOnElement(USERNAME_2);
    }

    /**
     * user enters text into username
     * Element: Username (label=Username)
     */
    public static void enterUsername(Page page, String text) {
        System.out.println("⌨️ user enters text into username: " + USERNAME_3 + " = '" + text + "'");
        enterText(USERNAME_3, text);
    }

    /**
     * user presses key on username
     * Element: Username (label=Username)
     */
    public static void pressKeyOnUsername(Page page) {
        System.out.println("⌨️ user presses key on username: " + USERNAME_4 + " - Key: Tab");
        page.locator(USERNAME_4).press("Tab");
    }

    /**
     * user enters text into password
     * Element: Password (label=Password)
     */
    public static void enterPassword(Page page, String text) {
        System.out.println("⌨️ user enters text into password: " + PASSWORD_5 + " = '" + text + "'");
        enterText(PASSWORD_5, text);
    }

    /**
     * user clicks on sign in
     * Element: SignIn (text=Sign In)
     */
    public static void clickSignIn(Page page) {
        System.out.println("🖱️ user clicks on sign in: " + SIGN_IN_6);
        clickOnElement(SIGN_IN_6);
    }

    /**
     * user clicks on first name
     * Element: FirstName (label=First Name)
     */
    public static void clickFirstName(Page page) {
        System.out.println("🖱️ user clicks on first name: " + FIRST_NAME_7);
        clickOnElement(FIRST_NAME_7);
    }

    /**
     * user enters text into first name
     * Element: FirstName (label=First Name)
     */
    public static void enterFirstName(Page page, String text) {
        System.out.println("⌨️ user enters text into first name: " + FIRST_NAME_8 + " = '" + text + "'");
        enterText(FIRST_NAME_8, text);
    }

    /**
     * user clicks on last name
     * Element: LastName (label=Last Name)
     */
    public static void clickLastName(Page page) {
        System.out.println("🖱️ user clicks on last name: " + LAST_NAME_9);
        clickOnElement(LAST_NAME_9);
    }

    /**
     * user enters text into last name
     * Element: LastName (label=Last Name)
     */
    public static void enterLastName(Page page, String text) {
        System.out.println("⌨️ user enters text into last name: " + LAST_NAME_10 + " = '" + text + "'");
        enterText(LAST_NAME_10, text);
    }

    /**
     * user clicks on mobile phone number
     * Element: MobilePhoneNumber (label=Mobile Phone Number)
     */
    public static void clickMobilePhoneNumber(Page page) {
        System.out.println("🖱️ user clicks on mobile phone number: " + MOBILE_PHONE_NUMBER_11);
        clickOnElement(MOBILE_PHONE_NUMBER_11);
    }

    /**
     * user enters text into mobile phone number
     * Element: MobilePhoneNumber (label=Mobile Phone Number)
     */
    public static void enterMobilePhoneNumber(Page page, String text) {
        System.out.println("⌨️ user enters text into mobile phone number: " + MOBILE_PHONE_NUMBER_12 + " = '" + text + "'");
        enterText(MOBILE_PHONE_NUMBER_12, text);
    }

    /**
     * user clicks on b9ddca9b7382468e a006273c27d735b5
     * Element: B9ddca9b7382468eA006273c27d735b5 (#b9ddca9b-7382-468e-a006-273c27d735b5)
     */
    public static void clickB9ddca9b7382468eA006273c27d735b5(Page page) {
        System.out.println("🖱️ user clicks on b9ddca9b7382468e a006273c27d735b5: " + B9DDCA9B7382468E_A006273C27D735B5_13);
        clickOnElement(B9DDCA9B7382468E_A006273C27D735B5_13);
    }

    /**
     * user clicks on number25
     * Element: Number25 (text=25)
     */
    public static void clickNumber25(Page page) {
        System.out.println("🖱️ user clicks on number25: " + NUMBER25_14);
        clickOnElement(NUMBER25_14);
    }

    /**
     * user clicks on add
     * Element: Add (text=Add)
     */
    public static void clickAdd(Page page) {
        System.out.println("🖱️ user clicks on add: " + ADD_15);
        clickOnElement(ADD_15);
    }

    /**
     * user clicks on save
     * Element: Save (text=Save)
     */
    public static void clickSave(Page page) {
        System.out.println("🖱️ user clicks on save: " + SAVE_16);
        clickOnElement(SAVE_16);
    }

    /**
     * user clicks on logout logout
     * Element: LogoutLogout (text=Logout Logout)
     */
    public static void clickLogoutLogout(Page page) {
        System.out.println("🖱️ user clicks on logout logout: " + LOGOUT_LOGOUT_17);
        clickOnElement(LOGOUT_LOGOUT_17);
    }

}
