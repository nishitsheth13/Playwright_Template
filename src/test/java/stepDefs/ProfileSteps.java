package stepDefs;
import configs.browserSelector;
import configs.loadProps;
import io.cucumber.java.en.*;
import pages.Profile;
import pages.Login;

/**
 * Step Definitions for Profile
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class ProfileSteps extends browserSelector {

    @Given("user navigates to Profile page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Profile page");
        Profile.navigateToProfile(page);
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔄 LOGIN STEPS - USING EXISTING CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    // These methods use credentials from configurations.properties file
    // This approach is more maintainable than hardcoded values
    // ═══════════════════════════════════════════════════════════════

    @When("User enters valid username from configuration")
    public void enterValidUsernameFromConfiguration() {
        String username = loadProps.getProperty(loadProps.PropKeys.USERNAME);
        System.out.println("📍 Step: Entering username from configuration: " + username);
        Login.UsernameField(username);
    }

    @And("User enters valid password from configuration")
    public void enterValidPasswordFromConfiguration() {
        String password = loadProps.getProperty(loadProps.PropKeys.PASSWORD);
        System.out.println("📍 Step: Entering password from configuration");
        Login.PasswordField(password);
    }

    @And("User clicks on Sign In button")
    public void clickSignInButton() {
        System.out.println("📍 Step: Clicking Sign In button");
        Login.SignInButton();
    }

    @When("user clicks on mobile phone number")
    public void clickMobilePhoneNumber() {
        System.out.println("📍 Step: user clicks on mobile phone number");
        Profile.clickMobilePhoneNumber(page);
    }

    @And("user enters {string} into mobilephonenumber")
    public void enterMobilePhoneNumber(String text) {
        System.out.println("📍 Step: Entering text into MobilePhoneNumber: '" + text + "'");
        Profile.enterMobilePhoneNumber(page, text);
    }

    @When("user clicks on save")
    public void clickSave() {
        System.out.println("📍 Step: user clicks on save");
        Profile.clickSave(page);
    }

    @When("user clicks on logout logout")
    public void clickLogoutLogout() {
        System.out.println("📍 Step: user clicks on logout logout");
        Profile.clickLogoutLogout(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // TODO: Add verification logic
    }


}
