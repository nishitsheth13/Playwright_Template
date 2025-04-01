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
        System.out.println("Enter URL");
        page.navigate(URL);

    }

    @When("Browser is Open")
    public void verifyBrowser() {
        if (page != null) {
            System.out.println("Browser is open");
        } else {
            System.out.println("Browser not opened");
        }


    }

    @Then("User is redirected to the application's URL")
    public void verifyRedirection() {
        System.out.println("Verify User is redirected to URL");
    }


    @When("User enter the valid credentials of admin")
    public void userEnterTheValidCredentialsOfAdmin() throws IOException {
        pages.login.login();
    }

    @Then("Verify User should be able to redirect to HomePage")
    public void verifyUserShouldBeAbleToRedirectToHomePage() throws InterruptedException {
        asserts.assertTrue(utils.isElementPresent(String.valueOf(pages.login.UserIcon)));
        System.out.println("Verify User is redirected to HomePage");
    }

    @When("User enter the invalid credentials of admin")
    public void userEnterTheInvalidCredentialsOfAdmin() {
        pages.login.Failed_login();
    }

    @Then("Verify User should not be able to redirect to HomePage")
    public void verifyUserShouldNotBeAbleToRedirectToHomePage() {
        assertThat(page.getByText("Invalid Login Attempt. The")).isVisible();
    }
}
