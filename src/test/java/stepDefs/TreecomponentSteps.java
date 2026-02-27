package stepDefs;

import configs.browserSelector;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.Login;
import pages.Treecomponent;

/**
 * Step Definitions for Treecomponent
 * Auto-generated from Playwright recording by Pure Java Generator
 * Reuses existing login methods from login.java
 * 
 * @story AUTO-GEN
 */
public class TreecomponentSteps extends browserSelector {

    @Given("user navigates to Treecomponent page")
    public void navigateTo() {
        System.out.println("📍 Step: Navigating to Treecomponent page");
        Treecomponent.navigateToTreecomponent(page);
    }


    @When("user clicks on setup")
    public void clickSetup() {
        System.out.println("📍 Step: user clicks on setup");
        Treecomponent.clickSetup(page);
    }

    @When("user clicks on security")
    public void clickSecurity() {
        System.out.println("📍 Step: user clicks on security");
        Treecomponent.clickSecurity(page);
    }

    @When("user clicks on access groups")
    public void clickAccessGroups() {
        System.out.println("📍 Step: user clicks on access groups");
        Treecomponent.clickAccessGroups(page);
    }

    @When("user clicks on add")
    public void clickAdd() {
        System.out.println("📍 Step: user clicks on add");
        Treecomponent.clickAdd(page);
    }

    @When("user clicks on txtsearchboxvalue")
    public void clickTxtsearchboxvalue() {
        System.out.println("📍 Step: user clicks on txtsearchboxvalue");
        Treecomponent.clickTxtsearchboxvalue(page);
    }

    @And("user enters {string} into txtsearchboxvalue")
    public void searchTxtsearchboxvalue(String text) {
        System.out.println("📍 Step: Entering text into Txtsearchboxvalue: '" + text + "'");
        Treecomponent.searchTxtsearchboxvalue(page, text);
    }

    @When("user clicks on btnsearch")
    public void clickBtnsearch() {
        System.out.println("📍 Step: user clicks on btnsearch");
        Treecomponent.clickBtnsearch(page);
    }

    @Then("mri energy automation root company should be visible")
    public void verifyMriEnergyAutomationRootCompany() {
        System.out.println("📍 Verify Step: mri energy automation root company should be visible");
        Treecomponent.verifyMriEnergyAutomationRootCompany(page);
    }

    @When("user clicks on mri energy automation root company")
    public void clickMriEnergyAutomationRootCompany() {
        System.out.println("📍 Step: user clicks on mri energy automation root company");
        Treecomponent.clickMriEnergyAutomationRootCompany(page);
    }

    @When("user clicks on open")
    public void clickOpen() {
        System.out.println("📍 Step: user clicks on open");
        Treecomponent.clickOpen(page);
    }

    @When("user clicks on tree details")
    public void clickTreeDetails() {
        System.out.println("📍 Step: user clicks on tree details");
        Treecomponent.clickTreeDetails(page);
    }

    @When("user clicks on clear")
    public void clickClear() {
        System.out.println("📍 Step: user clicks on clear");
        Treecomponent.clickClear(page);
    }

    @And("user enters {string} into selectfilter")
    public void enterSelectFilter(String text) {
        System.out.println("📍 Step: Entering text into SelectFilter: '" + text + "'");
        Treecomponent.enterSelectFilter(page, text);
    }

    @When("user clicks on btnfilter")
    public void clickBtnfilter() {
        System.out.println("📍 Step: user clicks on btnfilter");
        Treecomponent.clickBtnfilter(page);
    }

    @Then("list filters should be visible")
    public void verifyListFilters() {
        System.out.println("📍 Verify Step: list filters should be visible");
        Treecomponent.verifyListFilters(page);
    }

    @When("user clicks on uxname")
    public void clickUxname() {
        System.out.println("📍 Step: user clicks on uxname");
        Treecomponent.clickUxname(page);
    }

    @And("user enters {string} into uxname")
    public void enterUxname(String text) {
        System.out.println("📍 Step: Entering text into Uxname: '" + text + "'");
        Treecomponent.enterUxname(page, text);
    }

    @And("user selects {string} from uxcolumn")
    public void selectUxcolumn(String option) {
        System.out.println("📍 Step: Selecting option from Uxcolumn: '" + option + "'");
        Treecomponent.selectUxcolumn(page, option);
    }

    @When("user clicks on uxfcvaluetextbox")
    public void clickUxfcvaluetextbox() {
        System.out.println("📍 Step: user clicks on uxfcvaluetextbox");
        Treecomponent.clickUxfcvaluetextbox(page);
    }

    @And("user enters {string} into uxfcvaluetextbox")
    public void enterUxfcvaluetextbox(String text) {
        System.out.println("📍 Step: Entering text into Uxfcvaluetextbox: '" + text + "'");
        Treecomponent.enterUxfcvaluetextbox(page, text);
    }

    @When("user clicks on add to filter")
    public void clickAddToFilter() {
        System.out.println("📍 Step: user clicks on add to filter");
        Treecomponent.clickAddToFilter(page);
    }

    @When("user clicks on search")
    public void clickSearch() {
        System.out.println("📍 Step: user clicks on search");
        Treecomponent.clickSearch(page);
    }

    @When("user clicks on close")
    public void clickClose() {
        System.out.println("📍 Step: user clicks on close");
        Treecomponent.clickClose(page);
    }

    @When("user clicks on save")
    public void clickSave() {
        System.out.println("📍 Step: user clicks on save");
        Treecomponent.clickSave(page);
    }

    @When("user clicks on new treefilter")
    public void clickNewTreefilter() {
        System.out.println("📍 Step: user clicks on new treefilter");
        Treecomponent.clickNewTreefilter(page);
    }

}
