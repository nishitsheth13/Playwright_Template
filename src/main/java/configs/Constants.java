package configs;

/**
 * Central repository for all constants used across the framework.
 * This includes timeouts, paths, and configuration keys.
 * 
 * @author Nishit Sheth
 * @version 1.0
 */
public class Constants {
    
    // Timeouts (in milliseconds)
    public static final int DEFAULT_TIMEOUT = 20000;
    public static final int PAGE_LOAD_TIMEOUT = 30000;
    public static final int ELEMENT_TIMEOUT = 10000;
    
    // Paths
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String REPORTS_BASE_PATH = USER_DIR + "/MRITestExecutionReports/";
    public static final String CONFIG_PATH = USER_DIR + "/src/test/resources/configurations.properties";
    public static final String JIRA_CONFIG_PATH = USER_DIR + "/src/test/resources/jiraConfigurations.properties";
    public static final String ATTACHMENTS_PATH = USER_DIR + "/src/test/resources/attachments/";
    public static final String SCREENSHOTS_PATH = "/screenShots/";
    public static final String RECORDINGS_PATH = "/recordings/";
    
    // Configuration Keys
    public static final String BROWSER_KEY = "Browser";
    public static final String URL_KEY = "URL";
    public static final String USERNAME_KEY = "Username";
    public static final String PASSWORD_KEY = "Password";
    public static final String HEADLESS_MODE_KEY = "Headless_Mode";
    public static final String RECORDING_MODE_KEY = "Recording_Mode";
    public static final String SCREENSHOTS_MODE_KEY = "Screenshots_Mode";
    public static final String REMOVE_OLD_REPORTS_KEY = "Remove_Old_Reports";
    public static final String VERSION_KEY = "Version";
    public static final String VERSION_RECORD_KEY = "Version_Record";
    public static final String JIRA_INTEGRATION_KEY = "JIRA_Integration";
    
    // Browser Types
    public static final String CHROME = "chrome";
    public static final String EDGE = "edge";
    public static final String FIREFOX = "firefox";
    
    // Report Names
    public static final String CUCUMBER_REPORTS_FOLDER = "cucumberExtentReports";
    public static final String EXTENT_REPORTS_FOLDER = "extentReports";
    
    // Symbols for logging
    public static final String SUCCESS_SYMBOL = "✅";
    public static final String ERROR_SYMBOL = "❌";
    public static final String WARNING_SYMBOL = "⚠️";
    public static final String INFO_SYMBOL = "ℹ️";
    public static final String FOLDER_SYMBOL = "📁";
    public static final String FILE_SYMBOL = "📄";
    public static final String LOCK_SYMBOL = "🔐";
    public static final String LINK_SYMBOL = "🔗";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants class should not be instantiated");
    }
}








