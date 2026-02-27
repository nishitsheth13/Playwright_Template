package configs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility to update URL and credentials in configuration files.
 */
public class ConfigUpdater {
    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir")
            + "/src/test/resources/configurations.properties";
    private static final String JIRA_CONFIG_FILE_PATH = System.getProperty("user.dir")
            + "/src/test/resources/jiraConfigurations.properties";

    /**
     * Update the URL in the main configuration file and optionally update
     * credentials.
     * 
     * @param newUrl   The new URL to set
     * @param username Optional username to update (null to skip)
     * @param password Optional password to update (null to skip)
     * @return true if all updates succeed, false otherwise
     */
    public static boolean updateUrlAndCredentials(String newUrl, String username, String password) {
        boolean urlUpdated = setProperty(CONFIG_FILE_PATH, "URL", newUrl);
        boolean userUpdated = true;
        boolean passUpdated = true;
        if (username != null) {
            userUpdated = setProperty(CONFIG_FILE_PATH, "Username", username);
        }
        if (password != null) {
            passUpdated = setProperty(CONFIG_FILE_PATH, "Password", password);
        }
        return urlUpdated && userUpdated && passUpdated;
    }

    /**
     * Update a property in the given properties file.
     */
    private static boolean setProperty(String filePath, String key, String value) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
            properties.setProperty(key, value);
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                properties.store(outputStream, "Updated on: " + java.time.LocalDateTime.now());
            }
            System.out.println("✅ Property '" + key + "' updated in " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error updating property '" + key + "' in " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Update a property in the JIRA configuration file.
     */
    public static boolean updateJiraProperty(String key, String value) {
        return setProperty(JIRA_CONFIG_FILE_PATH, key, value);
    }
}
