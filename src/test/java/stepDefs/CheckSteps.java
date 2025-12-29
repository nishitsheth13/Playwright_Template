package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.Check;
import pages.login;

/**
 * Step Definitions for Check
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class CheckSteps extends browserSelector {

    @Given("user navigates to Check page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Check page");
        Check.navigateTo(page);
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔄 LOGIN STEPS - REUSE EXISTING METHODS
    // ═══════════════════════════════════════════════════════════════
    // TODO: The following login steps were detected in your recording.
    // Instead of using the generated page object methods, use existing login methods:
    //
    // RECOMMENDED APPROACH:
    //   @When("User enters valid username from configuration")
    //   public void enterValidUsername() {
    //       login.enterValidUsernameFromConfiguration(page);
    //   }
    //
    //   @And("User enters valid password from configuration")
    //   public void enterValidPassword() {
    //       login.enterValidPasswordFromConfiguration(page);
    //   }
    //
    //   @And("User clicks on Sign In button")
    //   public void clickSignIn() {
    //       login.clickSignIn(page);
    //   }
    //
    // Update your feature file to use:
    //   When User enters valid username from configuration
    //   And User enters valid password from configuration
    //   And User clicks on Sign In button
    // ═══════════════════════════════════════════════════════════════

    @And("user enters {string} into username")
    public void enterUsername(String text) {
        System.out.println("📍 Step: Entering text into Username: '" + text + "'");
        Check.enterUsername(page, text);
    }

    @And("user presses key on username")
    public void pressKeyOnUsername() {
        System.out.println("📍 Step: user presses key on username");
        Check.pressKeyOnUsername(page);
    }

    @And("user enters {string} into password")
    public void enterPassword(String text) {
        System.out.println("📍 Step: Entering text into Password: '" + text + "'");
        Check.enterPassword(page, text);
    }

    @When("user clicks on sign in")
    public void clickSignIn() {
        System.out.println("📍 Step: user clicks on sign in");
        Check.clickSignIn(page);
    }

    @When("user clicks on setup setup")
    public void clickSetupSetup() {
        System.out.println("📍 Step: user clicks on setup setup");
        Check.clickSetupSetup(page);
    }

    @When("user clicks on setup tree configure tree")
    public void clickSetupTreeConfigureTree() {
        System.out.println("📍 Step: user clicks on setup tree configure tree");
        Check.clickSetupTreeConfigureTree(page);
    }

    @When("user clicks on logout logout")
    public void clickLogoutLogout() {
        System.out.println("📍 Step: user clicks on logout logout");
        Check.clickLogoutLogout(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // TODO: Add verification logic
    }
}
