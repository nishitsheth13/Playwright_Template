package stepDefs;
import configs.browserSelector;
import configs.loadProps;
import configs.utils;
import io.cucumber.java.en.*;
import pages.Accessgroup;
import pages.Login;

/**
 * Step Definitions for Accessgroup
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * @story AUTO-GEN
 */
public class AccessgroupSteps extends browserSelector {

    @Given("user navigates to Accessgroup page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Accessgroup page");
        Accessgroup.navigateToAccessgroup(page);
    }

    // ═══════════════════════════════════════════════════════════════
    // ℹ️  LOGIN STEPS FROM RECORDING - GENERATED BELOW
    // ═══════════════════════════════════════════════════════════════
    // NOTE: If LoginSteps.java already has matching steps, you may
    // remove duplicates, but ALL steps from the recording are generated.
    // ═══════════════════════════════════════════════════════════════


    @When("user clicks on setup")
    public void clickSetup() {
        System.out.println("📍 Step: user clicks on setup");
        Accessgroup.clickSetup(page);
    }

    @When("user clicks on security")
    public void clickSecurity() {
        System.out.println("📍 Step: user clicks on security");
        Accessgroup.clickSecurity(page);
    }

    @When("user clicks on access groups")
    public void clickAccessGroups() {
        System.out.println("📍 Step: user clicks on access groups");
        Accessgroup.clickAccessGroups(page);
    }

    @When("user clicks on add")
    public void clickAdd() {
        System.out.println("📍 Step: user clicks on add");
        Accessgroup.clickAdd(page);
    }

    @And("user enters {string} into pleaseenteraname")
    public void enterPleaseEnterAName(String text) {
        System.out.println("📍 Step: Entering text into PleaseEnterAName: '" + text + "'");
        Accessgroup.enterPleaseEnterAName(page, text);
    }

    @When("user clicks on txtsearchboxvalue")
    public void clickTxtsearchboxvalue() {
        System.out.println("📍 Step: user clicks on txtsearchboxvalue");
        Accessgroup.clickTxtsearchboxvalue(page);
    }

    @And("user enters {string} into txtsearchboxvalue")
    public void searchTxtsearchboxvalue(String text) {
        System.out.println("📍 Step: Entering text into Txtsearchboxvalue: '" + text + "'");
        Accessgroup.searchTxtsearchboxvalue(page, text);
    }

    @When("user clicks on btnsearch")
    public void clickBtnsearch() {
        System.out.println("📍 Step: user clicks on btnsearch");
        Accessgroup.clickBtnsearch(page);
    }

    @Then("mri energy automation root company should be visible")
    public void verifyMriEnergyAutomationRootCompany() {
        System.out.println("📍 Verify Step: mri energy automation root company should be visible");
        Accessgroup.verifyMriEnergyAutomationRootCompany(page);
    }

    @When("user clicks on mri energy automation root company")
    public void clickMriEnergyAutomationRootCompany() {
        System.out.println("📍 Step: user clicks on mri energy automation root company");
        Accessgroup.clickMriEnergyAutomationRootCompany(page);
    }

    @When("user clicks on save")
    public void clickSave() {
        System.out.println("📍 Step: user clicks on save");
        Accessgroup.clickSave(page);
    }

    @When("user clicks on clear filter")
    public void clickClearFilter() {
        System.out.println("📍 Step: user clicks on clear filter");
        Accessgroup.clickClearFilter(page);
    }

    @And("user enters {string} into search")
    public void search(String text) {
        System.out.println("📍 Step: Entering text into Search: '" + text + "'");
        Accessgroup.search(page, text);
    }

    @Then("automationtest should be visible")
    public void verifyAutomationtest() {
        System.out.println("📍 Verify Step: automationtest should be visible");
        Accessgroup.verifyAutomationtest(page);
    }


}
