package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.Navigation;

/**
 * Step Definitions for Navigation
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class NavigationSteps extends browserSelector {

    @Given("user navigates to Navigation page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Navigation page");
        Navigation.navigateToNavigation(page);
    }

    @Then("page should be visible")
    public void verifyPage() {
        System.out.println("📍 Verify Step: page should be visible");
        Navigation.verifyPage(page);
    }

    @When("user clicks on mri navigation bar expand")
    public void clickMriNavigationBarExpand() {
        System.out.println("📍 Step: user clicks on mri navigation bar expand");
        Navigation.clickMriNavigationBarExpand(page);
    }

    @When("user clicks on tree")
    public void clickTree() {
        System.out.println("📍 Step: user clicks on tree");
        Navigation.clickTree(page);
    }

    @When("user clicks on configure tree")
    public void clickConfigureTree() {
        System.out.println("📍 Step: user clicks on configure tree");
        Navigation.clickConfigureTree(page);
    }


}
