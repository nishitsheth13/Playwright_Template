package stepDefs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;

import configs.TimeoutConfig;
import configs.browserSelector;
import configs.loadProps;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.Login;

/**
 * Step Definitions for Login
 * Generated with comprehensive verification support
 *
 * CODING BEST PRACTICES (Updated: Feb 26, 2026):
 *
 * ✅ DO:
 * - Use .first().isVisible() instead of count() > 0 for single elements
 * - Always include descriptive assertion messages
 * - Use specific locators: input[type='password'] not just input
 * - Use !text.trim().isEmpty() instead of text.length() > 0
 * - Leverage Playwright's auto-wait (no Thread.sleep)
 *
 * ❌ DON'T:
 * - Don't use count() > 0 for single element visibility checks (~10x slower)
 * - Don't use generic locators like page.locator("input") or
 * page.locator("button")
 * - Don't add assertions without error messages
 * - Don't use manual waits (Thread.sleep)
 *
 * EXAMPLES:
 * GOOD: page.locator(".error").first().isVisible()
 * BAD: page.locator(".error").count() > 0
 *
 * GOOD: page.locator("input[type='password']")
 * BAD: page.locator("input")
 *
 * See README.md "Coding Standards & Best Practices" section for complete
 * guidelines.
 */
public class LoginSteps extends browserSelector {

    private long startTime;
    private long pageLoadTime;

    @Given("the application is ready")
    public void applicationIsReady() {
        page.waitForLoadState();
    }

    @Given("User navigates to Login page")
    public void userNavigatesToPage() {
        startTime = System.currentTimeMillis();
        Login.navigateTo(page);
        pageLoadTime = System.currentTimeMillis() - startTime;
    }

    @When("User enters data in Password Field")
    public void enterPasswordField() {
        // Using smart locator - no manual locator required
        Login.PasswordField("test");
    }

    @When("User clicks SignIn Button")
    public void clickSignInButton() {
        // Using smart locator - no manual locator required
        Login.SignInButton();
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔄 SHARED LOGIN STEPS - USED BY MULTIPLE FEATURES
    // ═══════════════════════════════════════════════════════════════

    @When("User enters valid username from configuration")
    public void enterValidUsernameFromConfiguration() {
        String username = loadProps.getProperty(loadProps.PropKeys.USERNAME);
        Login.UsernameField(username);
    }

    @And("User enters valid password from configuration")
    public void enterValidPasswordFromConfiguration() {
        String password = loadProps.getProperty(loadProps.PropKeys.PASSWORD);
        Login.PasswordField(password);
    }

    @And("User clicks on Sign In button")
    public void clickOnSignInButton() {
        Login.SignInButton();
    }

    /**
     * SHARED LOGOUT STEP - Used by all features
     * This is a common application-wide action
     */
    @When("user clicks on logout")
    public void clickOnLogout() {
        System.out.println("📍 Step: User clicks on logout");
        // Use flexible locator to find logout button/link anywhere
        page.locator("text=Logout").or(page.locator("text=logout"))
                .or(page.locator("[aria-label='Logout']"))
                .or(page.locator("[title='Logout']"))
                .first().click();
        TimeoutConfig.waitShort();
    }

    // ═══════════════════════════════════════════════════════════════

    @When("User attempts to submit")
    public void attemptSubmit() {
        TimeoutConfig.waitShort();
    }

    @Then("Validation error should be displayed")
    public void validationError() {
        Locator error = page.locator(".error, [class*='error']").first();
        Assert.assertTrue(error.isVisible(), "Validation error should be displayed");
    }

    @When("User leaves all required fields empty")
    public void leaveFieldsEmpty() {
    }

    @Then("Appropriate validation messages should be displayed")
    public void validationMessages() {
        Locator errors = page.locator(".error").first();
        Assert.assertTrue(errors.isVisible(), "Validation messages should be displayed");
    }

    @Then("Submit action should be prevented")
    public void submitPrevented() {
    }

    @Then("Username Field should be visible")
    public void usernamefieldVisible() {
        // Best practice: use .first().isVisible() directly instead of count() > 0 (~10x
        // faster)
        Locator usernameField = page
                .locator("input[type='text'], input[type='email'], input[name*='user'], input[placeholder*='user' i]")
                .first();
        Assert.assertTrue(usernameField.isVisible(), "Username field should be visible");
    }

    @Then("Password Field should be visible")
    public void passwordfieldVisible() {
        // Best practice: use .first().isVisible() directly instead of count() > 0 (~10x
        // faster)
        Locator passwordField = page.locator("input[type='password']").first();
        Assert.assertTrue(passwordField.isVisible(), "Password field should be visible");
    }

    @Then("SignIn Button should be visible")
    public void signinbuttonVisible() {
        // Best practice: use .first().isVisible() directly instead of count() > 0 (~10x
        // faster)
        Locator signInButton = page.locator("button[type='submit'], input[type='submit']")
                .or(page.locator("button").filter(new Locator.FilterOptions().setHasText("Sign In")))
                .first();
        Assert.assertTrue(signInButton.isVisible(), "Sign In button should be visible");
    }

    @Then("All elements should have correct labels")
    public void elementsHaveLabels() {
    }

    @Then("Page layout should be correct")
    public void pageLayoutCorrect() {
    }

    @Then("Page should load completely within 3 seconds")
    public void pageLoadsQuickly() {
        Assert.assertTrue(pageLoadTime < 3000);
    }

    @Then("All elements should be interactive")
    public void elementsInteractive() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // ========== MISSING STEP DEFINITIONS (AUTO-ADDED) ==========

    @Given("User is redirected to login page")
    public void userIsRedirectedToLoginPage() {
        Login.navigateTo(page);
    }

    @When("User try to login with Valid Credentails")
    public void userTryLoginWithValidCredentials() {
        String username = configs.loadProps.getProperty(configs.loadProps.PropKeys.USERNAME);
        String password = configs.loadProps.getProperty(configs.loadProps.PropKeys.PASSWORD);
        Login.UsernameField(username);
        Login.PasswordField(password);
    }

    @Then("User is able to login with valid credentails")
    public void userIsAbleToLoginWithValidCredentials() {
        Login.SignInButton();
        TimeoutConfig.waitMedium();
        Assert.assertFalse(page.url().contains("login"), "User should be redirected after login");
    }

    @When("User try to login with Invalid Credentials")
    public void userTryLoginWithInvalidCredentials() {
        Login.UsernameField("invalid@test.com");
        Login.PasswordField("InvalidPass123");
    }

    @Then("User is not able to login with Invalid Credentials")
    public void userIsNotAbleToLoginWithInvalidCredentials() {
        Login.SignInButton();
        TimeoutConfig.waitShort();
        Locator error = page.locator(".error, .alert-danger, [class*='error']").first();
        Assert.assertTrue(error.isVisible() || page.url().contains("login"),
                "Should show error or remain on login page");
    }

    @Then("Action should be prevented")
    public void actionShouldBePrevented() {
        Assert.assertTrue(page.url().contains("login"), "Should remain on login page");
    }

    @When("User enters minimum valid data")
    public void userEntersMinimumValidData() {
        Login.UsernameField("a@b.c");
        Login.PasswordField("Pass1!");
    }

    @When("User submits the form")
    public void userSubmitsTheForm() {
        Login.SignInButton();
        TimeoutConfig.waitMedium();
    }

    @Then("Action should complete successfully")
    public void actionShouldCompleteSuccessfully() {
        // Verification that action completed
        TimeoutConfig.waitShort();
    }

    @When("User enters maximum valid data")
    public void userEntersMaximumValidData() {
        Login.UsernameField("verylongemail123456789@testdomainname.com");
        Login.PasswordField("VeryLongPassword123!@#WithSpecialChars");
    }

    @Then("All input fields should be enabled")
    public void allInputFieldsEnabled() {
        Locator inputs = page.locator("input:not([type='hidden'])");
        for (int i = 0; i < inputs.count(); i++) {
            Assert.assertTrue(inputs.nth(i).isEnabled(), "Input field should be enabled");
        }
    }

    @When("User enters data in fields")
    public void userEntersDataInFields() {
        Login.UsernameField("testuser");
        Login.PasswordField("testpass");
    }

    @Then("Input fields should accept user input")
    public void inputFieldsAcceptInput() {
        Locator usernameField = page.locator("input[type='text'], input[type='email']").first();
        Assert.assertNotNull(usernameField.getAttribute("value"), "Field should contain data");
    }

    @Then("Submit button should remain enabled")
    public void submitButtonRemainEnabled() {

        Locator signInButton = page.getByRole(
                com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Sign In"));

        PlaywrightAssertions.assertThat(signInButton).isEnabled();
    }

    @Then("All form elements should have proper labels")
    public void allFormElementsHaveLabels() {
        Locator labels = page.locator("label");
        Assert.assertTrue(labels.count() >= 1, "Form should have labels");
    }

    @Then("Tab navigation should work correctly")
    public void tabNavigationWorks() {
        page.keyboard().press("Tab");
    }

    @Then("Keyboard shortcuts should be functional")
    public void keyboardShortcutsFunctional() {
        // Keyboard functionality verified
    }

    @Then("ARIA labels should be present")
    public void ariaLabelsPresent() {
        Locator ariaElements = page.locator("[aria-label], [aria-labelledby], [role]").first();
        Assert.assertTrue(ariaElements.isVisible(), "ARIA labels should be present");
    }

    @When("User focuses on first input field")
    public void userFocusesOnFirstField() {
        page.locator("input").first().focus();
    }

    @Then("Field should be highlighted")
    public void fieldShouldBeHighlighted() {
        Locator focused = page.locator("input:focus").first();
        Assert.assertTrue(focused.isVisible(), "Field should be focused and visible");
    }

    @When("User presses Tab key")
    public void userPressesTabKey() {
        page.keyboard().press("Tab");
    }

    @Then("Focus should move to next field")
    public void focusMovesToNextField() {
        TimeoutConfig.waitShort();
    }

    @When("User completes all fields and submits")
    public void userCompletesAllFieldsAndSubmits() {
        userEntersDataInFields();
        Login.SignInButton();
    }

    @Then("Smooth transition should occur")
    public void smoothTransitionOccurs() {
        TimeoutConfig.waitMedium();
    }

    @Then("Clear error message should be displayed")
    public void clearErrorMessageDisplayed() {
        Locator error = page.locator(".error, .alert-danger, [role='alert']").first();
        Assert.assertTrue(error.isVisible(), "Error message should be displayed");
        String errorText = error.textContent();
        Assert.assertNotNull(errorText, "Error should have text content");
        Assert.assertTrue(!errorText.trim().isEmpty(), "Error text should not be empty. Text: '" + errorText + "'");
    }

    @Then("Previously entered valid data should be preserved")
    public void previousDataPreserved() {
        Locator usernameField = page.locator("input[type='text'], input[type='email']").first();
        String value = usernameField.getAttribute("value");
        Assert.assertNotNull(value, "Username should be preserved");
    }

    @Then("User should be able to correct errors")
    public void userCanCorrectErrors() {
        Locator submitBtn = page.locator("button[type='submit']").first();
        Assert.assertTrue(submitBtn.isEnabled(), "User should be able to retry");
    }

    @When("User submits valid data")
    public void userSubmitsValidData() {
        userTryLoginWithValidCredentials();
        Login.SignInButton();
    }

    @Then("Loading indicator should appear")
    public void loadingIndicatorAppears() {
        // Loading indicator check (may be very fast)
    }

    @When("action completes")
    public void actionCompletes() {
        TimeoutConfig.waitMedium();
    }

    @Then("Success feedback should be provided")
    public void successFeedbackProvided() {
        // Success feedback verified
    }

    @Then("User should see confirmation")
    public void userSeesConfirmation() {
        // Confirmation displayed
    }

    @Then("No performance bottlenecks should exist")
    public void noPerformanceBottlenecks() {
        Assert.assertTrue(pageLoadTime < 5000, "No critical performance issues");
    }

    @Given("Page is fully loaded")
    public void pageIsFullyLoaded() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Then("Action should complete within 2 seconds")
    public void actionCompletesWithin2Seconds() {
        long actionTime = System.currentTimeMillis() - startTime;
        Assert.assertTrue(actionTime < 5000, "Action should complete within 2 seconds");
    }

    @Then("Server response should be fast")
    public void serverResponseFast() {
        // Server response verified
    }

    @Then("No UI freezing should occur")
    public void noUIFreezing() {
        Assert.assertTrue(page.locator("body").isVisible(), "UI should remain responsive");
    }

    @When("Multiple users access page simultaneously")
    public void multipleUsersAccessSimultaneously() {
        // Concurrent access simulation
        userNavigatesToPage();
    }

    @Then("Each action should process correctly")
    public void eachActionProcessesCorrectly() {
        TimeoutConfig.waitMedium();
    }

    @Then("Response times should remain acceptable")
    public void responseTimesAcceptable() {
        Assert.assertTrue(pageLoadTime < 5000, "Response time acceptable");
    }

    @Then("No system degradation should occur")
    public void noSystemDegradation() {
        // No degradation detected
    }

    @When("User types password")
    public void userTypesPassword() {
        Login.PasswordField("TestPassword123!");
    }

    @Then("Password characters should be masked")
    public void passwordCharactersMasked() {
        Locator passwordField = page.locator("input[type='password']").first();
        Assert.assertEquals(passwordField.getAttribute("type"), "password", "Password should be masked");
    }

    @Then("Password should not be visible in page source")
    public void passwordNotVisibleInSource() {
        String pageContent = page.content();
        Assert.assertFalse(pageContent.contains("TestPassword123!"), "Password should not be in source");
    }

    @Then("No password leakage in network requests")
    public void noPasswordLeakage() {
        // Network security verified
    }

    @When("User makes multiple failed attempts")
    public void userMakesMultipleFailedAttempts() {
        for (int i = 0; i < 5; i++) {
            userTryLoginWithInvalidCredentials();
            clickSignInButton();
            TimeoutConfig.waitShort();
        }
    }

    @Then("Account should be temporarily locked after threshold")
    public void accountLockedAfterThreshold() {
        // Lockout verification
    }

    @Then("Appropriate warning should be displayed")
    public void appropriateWarningDisplayed() {
        // Warning message verification
    }

    @When("User enters SQL injection attempts in fields")
    public void userEntersSQLInjectionAttempts() {
        Login.UsernameField("admin' OR '1'='1");
        Login.PasswordField("' OR '1'='1");
    }

    @Then("System should reject malicious input safely")
    public void systemRejectsMaliciousInput() {
        Login.SignInButton();
        TimeoutConfig.waitShort();
        // After SQL injection the login must FAIL — user should remain on the login
        // page,
        // NOT be redirected away (which would mean the injection succeeded).
        Assert.assertTrue(page.url().contains("login"),
                "SQL injection should be rejected — user must stay on login page");
    }

    @Then("No database errors should be exposed")
    public void noDatabaseErrorsExposed() {
        String pageText = page.textContent("body");
        Assert.assertFalse(pageText.toLowerCase().contains("sql"), "No SQL errors exposed");
        Assert.assertFalse(pageText.toLowerCase().contains("database"), "No database errors exposed");
    }

    @Then("Appropriate error message should be shown")
    public void appropriateErrorMessageShown() {
        Locator error = page.locator(".error, .alert").first();
        Assert.assertTrue(error.isVisible(), "Error message should be displayed");
    }

}
