package hooks;

import configs.browserSelector;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

/**
 * Cucumber hooks for test setup and teardown.
 * Manages browser lifecycle for BDD tests.
 */
public class hooks extends browserSelector {

    @BeforeAll
    public static void before() throws Exception {
        browserSelector.setUp();
    }

    @AfterAll
    public static void after() throws Exception {
        browserSelector.tearDown();
    }

    @BeforeClass
    @Parameters("Browsers")
    public static void launchBrowser(String Browser) throws Exception {
        browserSelector.LaunchBrowser(Browser);
    }

    @AfterClass
    public static void closeBrowser() throws Exception {
        browserSelector.closeBrowser();
    }

}
