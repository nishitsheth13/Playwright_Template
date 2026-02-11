package stepDefs;
import configs.browserSelector;
import configs.loadProps;
import io.cucumber.java.en.*;
import pages.Navigation;
import pages.Login;

/**
 * Step Definitions for Navigation
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from Login.java
 */
public class NavigationSteps extends browserSelector {

    @Given("user navigates to Navigation page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Navigation page");
        Navigation.navigateToNavigation(page);
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

    @When("user clicks on div")
    public void clickDiv() {
        System.out.println("📍 Step: user clicks on div");
        Navigation.clickDiv(page);
    }

    @When("user clicks on setup setup")
    public void clickSetupSetup() {
        System.out.println("📍 Step: user clicks on setup setup");
        Navigation.clickSetupSetup(page);
    }

    @When("user clicks on configure tree")
    public void clickConfigureTree() {
        System.out.println("📍 Step: user clicks on configure tree");
        Navigation.clickConfigureTree(page);
    }

    @When("user clicks on logout logout")
    public void clickLogoutLogout() {
        System.out.println("📍 Step: user clicks on logout logout");
        Navigation.clickLogoutLogout(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // TODO: Add verification logic
    }

    @Then("user presses key on username")
    public void userPressesKeyOnUsername() {
        System.out.println("📍 Step: User presses key on username");
        // This step is typically handled by the enter username method
    }


}
