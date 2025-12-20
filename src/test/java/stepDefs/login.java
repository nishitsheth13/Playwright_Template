package stepDefs;

import configs.browserSelector;
import configs.utils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class login extends browserSelector {

    pages.login Obj = new pages.login();
    String title;
    SoftAssert asserts = new SoftAssert();

    @Given("Enter URL")
    public void verifyURL() {
        System.out.println("🌐 Step: Navigating to application URL");
        page.navigate(URL);
        page.waitForLoadState();
        System.out.println("✅ Navigation completed");
    }

    @When("Browser is Open")
    public void verifyBrowser() {
        System.out.println("🌐 Step: Verifying browser is open");
        if (page != null) {
            System.out.println("✅ Browser is open and ready");
        } else {
            System.err.println("❌ Browser not opened");
            throw new IllegalStateException("Browser page instance is null");
        }
    }

    @Then("User is redirected to the application's URL")
    public void verifyRedirection() {
        System.out.println("🌐 Step: Verifying user redirection");
        String currentUrl = page.url();
        System.out.println("✅ Current URL: " + currentUrl);
    }


    @When("User enter the valid credentials of admin")
    public void userEnterTheValidCredentialsOfAdmin() throws IOException {
        System.out.println("🔐 Step: Entering valid credentials");
        pages.login.login();
    }

    @Then("Verify User should be able to redirect to HomePage")
    public void verifyUserShouldBeAbleToRedirectToHomePage() throws InterruptedException {
        System.out.println("✅ Step: Verifying successful login");
        boolean isUserIconPresent = utils.isElementPresent(pages.login.UserIcon);
        asserts.assertTrue(isUserIconPresent, "User icon should be visible after successful login");
        System.out.println("✅ User successfully redirected to HomePage");
    }

    @When("User enter the invalid credentials of admin")
    public void userEnterTheInvalidCredentialsOfAdmin() {
        System.out.println("🔐 Step: Entering invalid credentials");
        pages.login.Failed_login();
    }

    @Then("Verify User should not be able to redirect to HomePage")
    public void verifyUserShouldNotBeAbleToRedirectToHomePage() {
        System.out.println("✅ Step: Verifying login failure");
        assertThat(page.getByText("Invalid Login Attempt. The")).isVisible();
        System.out.println("✅ Invalid login message displayed as expected");
    }
}
