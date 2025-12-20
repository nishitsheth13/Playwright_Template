package configs;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG Annotation Transformer to automatically apply RetryAnalyzer to all test methods.
 * This eliminates the need to add retryAnalyzer attribute to each @Test annotation.
 * 
 * Configuration: Add this listener to testng.xml:
 * <listeners>
 *     <listener class-name="configs.RetryListener"/>
 * </listeners>
 */
public class RetryListener implements IAnnotationTransformer {
    
    /**
     * Transforms test annotations to add retry analyzer automatically.
     * 
     * @param annotation The test annotation to transform
     * @param testClass The test class
     * @param testConstructor The test constructor
     * @param testMethod The test method
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void transform(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {
        
        // Set retry analyzer for all test methods if not already set
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
            
            if (testMethod != null) {
                System.out.println("🔄 RetryAnalyzer attached to: " + testMethod.getName());
            }
        }
    }
}
