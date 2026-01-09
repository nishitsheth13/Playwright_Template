package hooks;

import configs.browserSelector;
import configs.AITestFramework;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Scenario;

/**
 * Cucumber hooks for test setup and teardown.
 * Manages browser lifecycle and AI framework for BDD tests.
 * Note: Cucumber hooks should NOT be static - each scenario gets its own instance.
 */
public class hooks extends browserSelector {

    private long scenarioStartTime;

    /**
     * Initialize AI Framework once before all scenarios
     */
    @BeforeAll
    public static void initializeAIFramework() {
        System.out.println("🤖 Initializing AI Test Framework...");
        AITestFramework.initialize();
    }

    /**
     * Cucumber Before hook - runs before each scenario
     * @param scenario Current scenario being executed
     */
    @Before
    public void beforeScenario(Scenario scenario) throws Exception {
        scenarioStartTime = System.currentTimeMillis();
        System.out.println("🎬 Starting scenario: " + scenario.getName());
        browserSelector.setUp();
    }

    /**
     * Cucumber After hook - runs after each scenario
     * @param scenario Current scenario that was executed
     */
    @After
    public void afterScenario(Scenario scenario) throws Exception {
        long duration = System.currentTimeMillis() - scenarioStartTime;
        String status = scenario.isFailed() ? "FAIL" : "PASS";

        System.out.println("🏁 Finishing scenario: " + scenario.getName() + " - Status: " + status + " (" + duration + "ms)");

        // Record execution in AI framework
        AITestFramework.TestExecutionRecord record =
            new AITestFramework.TestExecutionRecord(scenario.getName(), status, duration);
        if (scenario.isFailed()) {
            record.failureReason = "Scenario failed";
        }
        AITestFramework.recordExecution(record);

        browserSelector.tearDown();
    }

    /**
     * Generate health report after all scenarios
     */
    @AfterAll
    public static void generateHealthReport() {
        System.out.println("\n" + AITestFramework.generateHealthReport());
        AITestFramework.shutdown();
    }

}





