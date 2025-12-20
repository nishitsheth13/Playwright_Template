package configs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for loading and managing properties from configuration files.
 * Provides centralized access to configuration values used throughout the framework.
 */
public class loadProps {

    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/configurations.properties";
    private static final String JIRA_CONFIG_FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/jiraConfigurations.properties";

    /**
     * Retrieves a property value from the main configuration file.
     * 
     * @param key The property key to retrieve
     * @return The property value, or null if not found or error occurs
     */
    public static String getProperty(String key) {
        Properties prop = new Properties();
        
        try (FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            prop.load(input);
            return prop.getProperty(key);
        } catch (IOException e) {
            System.err.println("❌ Error loading property '" + key + "' from configuration file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates a property value in the configuration file.
     * 
     * @param key The property key to update
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
