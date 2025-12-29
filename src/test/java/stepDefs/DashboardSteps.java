package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.Dashboard;
import pages.login;

/**
 * Step Definitions for Dashboard
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class DashboardSteps extends browserSelector {

    @Given("user navigates to Dashboard page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Dashboard page");
        Dashboard.navigateTo(page);
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔄 LOGIN STEPS - USING EXISTING METHODS
    // ═══════════════════════════════════════════════════════════════
    @When("User enters valid username from configuration")
    public void enterValidUsername() {
        System.out.println("📍 Step: Entering valid username from configuration");
        login.enterValidUsername();
    }

    @And("User enters valid password from configuration")
    public void enterValidPassword() {
        System.out.println("📍 Step: Entering valid password from configuration");
        login.enterValidPassword();
    }

    @And("User clicks on Sign In button")
    public void clickSignInButton() {
        System.out.println("📍 Step: Clicking Sign In button");
        login.clickSignInButton();
    }
    // ═══════════════════════════════════════════════════════════════

    @When("user clicks on dashboards dashboards")
    public void clickDashboardsDashboards() {
        System.out.println("📍 Step: user clicks on dashboards dashboards");
        Dashboard.clickDashboardsDashboards(page);
    }

    @When("user clicks on select new path")
    public void clickSelectNewPath() {
        System.out.println("📍 Step: user clicks on select new path");
        Dashboard.clickSelectNewPath(page);
    }

    @When("user clicks on edit")
    public void clickEdit() {
        System.out.println("📍 Step: user clicks on edit");
        Dashboard.clickEdit(page);
    }

    @When("user clicks on yes")
    public void clickYes() {
        System.out.println("📍 Step: user clicks on yes");
        Dashboard.clickYes(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // TODO: Add verification logic
    }
}
