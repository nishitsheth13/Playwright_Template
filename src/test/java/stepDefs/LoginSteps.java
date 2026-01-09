package stepDefs;

import configs.browserSelector;
import configs.loadProps;
import io.cucumber.java.en.*;
import pages.Login;

/**
 * Step Definitions for Login
 * Using existing login methods for better code reuse
 */
public class LoginSteps extends browserSelector {

    @Given("user navigates to Login page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Login page");
        Login.navigateToLogin(page);
    }

    @When("User enters valid username from configuration")
    public void enterValidUsername() {
        System.out.println("📍 Step: Entering valid username from configuration");
        Login.enterUsername(page, loadProps.getProperty("Username"));
    }

    @And("User enters valid password from configuration")
    public void enterValidPassword() {
        System.out.println("📍 Step: Entering valid password from configuration");
        Login.enterPassword(page, loadProps.getProperty("Password"));
    }

    @And("User clicks on Sign In button")
    public void clickSignInButton() {
        System.out.println("📍 Step: Clicking Sign In button");
        Login.clickSignIn(page);
    }

    @Then("user should be logged in successfully")
    public void userLoggedIn() {
        System.out.println("📍 Step: Verifying user is logged in");
        // Add verification logic here
        // Example: Assert.assertTrue(page.locator("text=Dashboard").isVisible());
    }

    @When("user clicks on logout logout")
    public void clickLogoutLogout() {
        System.out.println("📍 Step: user clicks on logout logout");
        Login.clickLogoutLogout(page);
    }

    @Then("page should be updated")
    public void pageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // Add verification logic here
    }
}
