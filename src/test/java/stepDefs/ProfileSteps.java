package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.Profile;

/**
 * Step Definitions for Profile
 * Auto-generated from Playwright recording by Pure Java Generator
 * @story AUTO-GEN
 */
public class ProfileSteps extends browserSelector {

    @Given("user navigates to Profile page")
    public void navigateTo() {
        Profile.navigateTo(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        // TODO: Add verification logic
    }
}
