package stepDefs;
import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.ProfileUpdation;
import pages.login;

/**
 * Step Definitions for ProfileUpdation
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class ProfileUpdationSteps extends browserSelector {

    @Given("user navigates to ProfileUpdation page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to ProfileUpdation page");
        ProfileUpdation.navigateTo(page);
    }

    @When("user clicks on username")
    public void clickUsername() {
        System.out.println("📍 Step: user clicks on username");
        ProfileUpdation.clickUsername(page);
    }

    @And("user enters {string} into username")
    public void enterUsername(String text) {
        System.out.println("📍 Step: Entering text into Username: '" + text + "'");
        ProfileUpdation.enterUsername(page, text);
    }

    @And("user presses key on username")
    public void pressKeyOnUsername() {
        System.out.println("📍 Step: user presses key on username");
        ProfileUpdation.pressKeyOnUsername(page);
    }

    @And("user enters {string} into password")
    public void enterPassword(String text) {
        System.out.println("📍 Step: Entering text into Password: '" + text + "'");
        ProfileUpdation.enterPassword(page, text);
    }

    @When("user clicks on sign in")
    public void clickSignIn() {
        System.out.println("📍 Step: user clicks on sign in");
        ProfileUpdation.clickSignIn(page);
    }

    @When("user clicks on first name")
    public void clickFirstName() {
        System.out.println("📍 Step: user clicks on first name");
        ProfileUpdation.clickFirstName(page);
    }

    @And("user enters {string} into firstname")
    public void enterFirstName(String text) {
        System.out.println("📍 Step: Entering text into FirstName: '" + text + "'");
        ProfileUpdation.enterFirstName(page, text);
    }

    @When("user clicks on last name")
    public void clickLastName() {
        System.out.println("📍 Step: user clicks on last name");
        ProfileUpdation.clickLastName(page);
    }

    @And("user enters {string} into lastname")
    public void enterLastName(String text) {
        System.out.println("📍 Step: Entering text into LastName: '" + text + "'");
        ProfileUpdation.enterLastName(page, text);
    }

    @When("user clicks on mobile phone number")
    public void clickMobilePhoneNumber() {
        System.out.println("📍 Step: user clicks on mobile phone number");
        ProfileUpdation.clickMobilePhoneNumber(page);
    }

    @And("user enters {string} into mobilephonenumber")
    public void enterMobilePhoneNumber(String text) {
        System.out.println("📍 Step: Entering text into MobilePhoneNumber: '" + text + "'");
        ProfileUpdation.enterMobilePhoneNumber(page, text);
    }

    @When("user clicks on b9ddca9b7382468e a006273c27d735b5")
    public void clickB9ddca9b7382468eA006273c27d735b5() {
        System.out.println("📍 Step: user clicks on b9ddca9b7382468e a006273c27d735b5");
        ProfileUpdation.clickB9ddca9b7382468eA006273c27d735b5(page);
    }

    @When("user clicks on number25")
    public void clickNumber25() {
        System.out.println("📍 Step: user clicks on number25");
        ProfileUpdation.clickNumber25(page);
    }

    @When("user clicks on add")
    public void clickAdd() {
        System.out.println("📍 Step: user clicks on add");
        ProfileUpdation.clickAdd(page);
    }

    @When("user clicks on save")
    public void clickSave() {
        System.out.println("📍 Step: user clicks on save");
        ProfileUpdation.clickSave(page);
    }

    @When("user clicks on logout logout")
    public void clickLogoutLogout() {
        System.out.println("📍 Step: user clicks on logout logout");
        ProfileUpdation.clickLogoutLogout(page);
    }

    @Then("page should be updated")
    public void verifyPageUpdated() {
        System.out.println("📍 Step: Verifying page is updated");
        // TODO: Add verification logic
    }
}
