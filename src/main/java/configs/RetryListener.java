package configs;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG Annotation Transformer to automatically apply RetryAnalyzer to all test methods,
 * including Cucumber scenarios.
 * This eliminates the need to add retryAnalyzer attribute to each @Test annotation.
 *
 * CONFIGURATION (Required in testng.xml):
 *    <listeners>
 *        <listener class-name="configs.RetryListener"/>
 *    </listeners>
 *
 * HOW IT WORKS:
 *    1. TestNG loads this listener before running tests
 *    2. For each test method, transform() is called
 *    3. Automatically attaches RetryAnalyzer to the @Test annotation
 *    4. Works with both regular TestNG tests and Cucumber scenarios
 *
 * CUCUMBER-TESTNG INTEGRATION:
 *    - Detects Cucumber scenario runner methods
 *    - Properly attaches retry analyzer to runScenario() method
 *    - Thread-safe for parallel test execution
 *
 * VERIFIED WORKING (Feb 11, 2026):
 *    - Retry mechanism confirmed functional with Cucumber
 *    - Console shows "RetryAnalyzer attached to: methodName"
 *    - Tests actually retry on failure (not just log messages)
 *
 * See README.md and RetryAnalyzer.java for complete documentation.
 */
public class RetryListener implements IAnnotationTransformer {

    public RetryListener() {
        System.out.println("\n========================================");
        System.out.println("🔄 RetryListener CONSTRUCTOR CALLED");
        System.out.println("========================================\n");
    }

    /**
     * Transforms test annotations to add retry analyzer automatically.
     * Works with both pure TestNG tests and Cucumber scenarios.
     *
     * @param annotation      The test annotation to transform
     * @param testClass       The test class
     * @param testConstructor The test constructor
     * @param testMethod      The test method
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void transform(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {

        System.out.println("\n>>> RetryListener.transform() CALLED");
        System.out.println(">>> Class: " + (testClass != null ? testClass.getName() : "null"));
        System.out.println(">>> Method: " + (testMethod != null ? testMethod.getName() : "null"));

        // Set retry analyzer for all test methods if not already set
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);

            // Log attachment for debugging
            String methodName = testMethod != null ? testMethod.getName() :
                    (testClass != null ? testClass.getSimpleName() : "Unknown");

            // Check if this is a Cucumber scenario
            boolean isCucumberScenario = testClass != null &&
                    testClass.getName().contains("TestRunner") ||
                    (testMethod != null && testMethod.getName().equals("runScenario"));

            if (isCucumberScenario) {
                System.out.println("🔄 RetryAnalyzer attached to Cucumber scenario runner: " + methodName);
            } else if (testMethod != null) {
                System.out.println("🔄 RetryAnalyzer attached to: " + methodName);
            }
        } else {
            System.out.println(">>> RetryAnalyzer ALREADY SET: " + annotation.getRetryAnalyzerClass().getName());
        }
    }
}
