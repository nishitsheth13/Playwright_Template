package configs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for loading and managing properties from configuration files.
 * Provides centralized access to configuration values used throughout the
 * framework.
 *
 * ⚠️ IMPORTANT: Always use the property key constants (PropKeys class) to avoid
 * case-sensitivity issues!
 * ✅ GOOD: loadProps.getProperty(PropKeys.USERNAME)
 * ❌ BAD: loadProps.getProperty("username") or getProperty("userName")
 */
public class loadProps {

    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir")
            + "/src/test/resources/configurations.properties";
    private static final String JIRA_CONFIG_FILE_PATH = System.getProperty("user.dir")
            + "/src/test/resources/jiraConfigurations.properties";

    /**
     * Retrieves a property value from the main configuration file.
     * ⚠️ WARNING: Use PropKeys constants to avoid case-sensitivity issues!
     *
     * @param key The property key to retrieve (use PropKeys constants)
     * @return The property value, or null if not found or error occurs
     */
    public static String getProperty(String key) {
        Properties prop = new Properties();

        try (FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            prop.load(input);
            String value = prop.getProperty(key);

            // Warn if property not found - might be case-sensitivity issue
            if (value == null) {
                System.err.println("⚠️ WARNING: Property '" + key + "' not found in configuration!");
                System.err.println("💡 TIP: Check case-sensitivity. Use loadProps.PropKeys constants.");
                System.err.println("📋 Available properties: " + prop.keySet());
            }

            return value;
        } catch (IOException e) {
            System.err.println("❌ Error loading property '" + key + "' from configuration file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Safe property retrieval with default value.
     * Returns default value if property is missing or null.
     *
     * @param key          The property key (use PropKeys constants)
     * @param defaultValue Value to return if property not found
     * @return The property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Validates that a required property exists and is not empty.
     * Throws exception if property is missing.
     *
     * @param key The property key to validate
     * @return The property value
     * @throws IllegalStateException if property is missing or empty
     */
    public static String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                    "❌ REQUIRED property '" + key + "' is missing or empty in configuration! " +
                            "Check src/test/resources/configurations.properties");
        }
        return value;
    }

    /**
     * Property key constants to ensure case-sensitivity consistency.
     * Always use these constants instead of hardcoded strings!
     */
    public static final class PropKeys {
        // Authentication
        public static final String USERNAME = "Username";
        public static final String PASSWORD = "Password";

        // Browser Configuration
        public static final String BROWSER = "Browser";
        public static final String URL = "URL";
        public static final String HEADLESS_MODE = "Headless_Mode";

        // Test Execution Settings
        public static final String RECORDING_MODE = "Recording_Mode";
        public static final String SCREENSHOTS_MODE = "Screenshots_Mode";
        public static final String TRACE_LOG = "TraceLog";

        // Timeouts
        public static final String DEFAULT_TIMEOUT = "DefaultTimeout";
        public static final String ELEMENT_WAIT_TIMEOUT = "ElementWaitTimeout";
        public static final String IMPLICIT_WAIT_TIMEOUT = "ImplicitWaitTimeout";
        public static final String PAGE_LOAD_TIMEOUT = "PageLoadTimeout";

        // Reporting
        public static final String VERSION = "Version";
        public static final String VERSION_RECORD = "Version_Record";
        public static final String REMOVE_OLD_REPORTS = "Remove_Old_Reports";

        // JIRA Integration
        public static final String JIRA_INTEGRATION = "JIRA_Integration";
        public static final String PASS_COMMENT = "PassComment";
        public static final String FAILED_COMMENT = "FailedComment";

        // Retry Configuration
        public static final String MAX_RETRY_COUNT = "MaxRetryCount";
        public static final String RETRY_FAILED_TESTS_ONLY = "RetryFailedTestsOnly";

        private PropKeys() {
            // Prevent instantiation
        }
    }

    /**
     * Updates a property value in the configuration file.
     * 
     * @param key   The property key to update
     * @param value The new value to set
     * @return true if update was successful, false otherwise
     */
    public static boolean setProperty(String key, String value) {
        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);

            String originalValue = properties.getProperty(key);
            if (originalValue != null) {
                System.out.println("📝 Updating property '" + key + "': " + originalValue + " → " + value);
            } else {
                System.out.println("📝 Adding new property '" + key + "': " + value);
            }

            properties.setProperty(key, value);

            try (FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE_PATH)) {
                properties.store(outputStream, "Updated on: " + java.time.LocalDateTime.now());
            }

            System.out.println("✅ Property updated successfully!");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error updating property '" + key + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a property value from the JIRA configuration file.
     * 
     * @param key The JIRA property key to retrieve
     * @return The property value, or null if not found or error occurs
     */
    public static String getJIRAConfig(String key) {
        Properties prop = new Properties();

        try (FileInputStream input = new FileInputStream(JIRA_CONFIG_FILE_PATH)) {
            prop.load(input);
            return prop.getProperty(key);
        } catch (IOException e) {
            System.err.println("❌ Error loading JIRA property '" + key + "': " + e.getMessage());
            return null;
        }
    }
}



