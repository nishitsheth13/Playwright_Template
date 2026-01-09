package runner;


import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = { "src/test/java/features/" }, glue = { "stepDefs", "hooks" }, plugin = {
                "pretty",
                "json:target/json-report/cucumber.json",
                "html:target/cucumber-reports/cucumber.html",
                "junit:target/cucumber-reports/cucumber.xml",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
}, monochrome = true, publish = true)
public class TestRunner extends AbstractTestNGCucumberTests {

        // Override this method to enable parallel execution if needed
        // @Override
        // @DataProvider(parallel = true)
        // public Object[][] scenarios() {
        // return super.scenarios();
        // }

}




