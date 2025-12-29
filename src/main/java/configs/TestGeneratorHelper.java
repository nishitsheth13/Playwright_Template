package configs;

import configs.jira.jiraClient;
import configs.jira.jiraClient.JiraStory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unified Test Generator Helper - Single class for all test generation needs.
 * 
 * Features:
 * 1. Parse Playwright recordings and generate test files (replaces TestFileGenerator)
 * 2. JIRA story integration and AI-assisted generation
 * 3. Test requirement management and validation
 * 
 * This consolidates all test generation functionality into one helper class.
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * LOCATOR STRATEGY & CODE REUSABILITY (Applied to ALL generation methods)
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * LOCATOR PRIORITY ORDER (optimizeSelector method):
 *   1. Static ID          → //input[@id='Username'] (highest priority)
 *   2. Relative XPath     → //div[@class='form']//input
 *   3. Absolute XPath     → /html/body/div/input
 *   4. Label or Names     → label=Username, @name='submit'
 *   5. Class name         → .btn-primary, @class='container'
 *   6. CSS Selectors      → div > button (lowest priority)
 * 
 * DYNAMIC ID DETECTION (isDynamicId method):
 *   - Detects GUIDs: [a-f0-9]{8}-[a-f0-9]{4}-...
 *   - Detects Timestamps: \d{10,13}
 *   - Detects Random Hashes: [a-zA-Z0-9]{16,}
 *   - Warns and downgrades dynamic IDs to lower priority
 * 
 * CODE REUSABILITY (Code Reuse Detection Helpers):
 *   - pageObjectExists()           → Checks if page object already exists
 *   - detectExistingLogin()        → Finds existing login.java methods
 *   - containsLoginPattern()       → Detects login/auth patterns in actions
 *   - hasConfiguredCredentials()   → Checks configurations.properties for test data
 *   - Automatically imports existing classes (e.g., login.java)
 *   - Skips regeneration of existing page objects
 *   - Provides tips for manual integration
 * 
 * USAGE:
 *   All test generation methods (Recording, JIRA, Manual) use these features:
 *   - Recording-based: generateFromRecording() → parseRecording() → optimizeSelector()
 *   - JIRA-based: generateFromJiraStory() → [uses same file generation methods]
 *   - File generation: generatePageObject(), generateFeatureFile(), generateStepDefinitions()
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class TestGeneratorHelper {

    // ========================================================================
    // RECORDING-BASED GENERATION (from TestFileGenerator)
    // ========================================================================
    
    /**
     * Action extracted from Playwright recording.
     */
    private static class RecordedAction {
        String type;
        String selector;
        String value;
        String methodName;
        String stepText;
        String elementName;
        String readableName;

        RecordedAction(int id, String type, String selector, String value) {
            this.type = type;
            this.selector = selector;
            this.value = value;
            this.readableName = extractReadableName(selector);
            this.elementName = generateElementName(readableName, id);
            this.methodName = generateMethodName(type, readableName, id);
            this.stepText = generateStepText(type, readableName);
        }

        /**
         * Extract a readable name from the selector.
         * Examples:
         *   text=Sign In -> SignIn
         *   text=Save -> Save
         *   #username -> Username
         *   input[placeholder="Email"] -> Email
         *   button:has-text("Submit") -> Submit
         *   role=button[name="Login"] -> Login
         */
        private String extractReadableName(String selector) {
            if (selector == null || selector.isEmpty()) {
                return "Element";
            }

            String name = selector;

            // Extract from text= locator (Playwright modern)
            if (selector.startsWith("text=")) {
                name = selector.substring(5).trim();
            }
            // Extract from placeholder= locator
            else if (selector.startsWith("placeholder=")) {
                name = selector.substring(12).trim();
            }
            // Extract from label= locator  
            else if (selector.startsWith("label=")) {
                name = selector.substring(6).trim();
            }
            // Extract from ID selector
            else if (selector.startsWith("#")) {
                name = selector.substring(1).trim();
                // Remove common suffixes from IDs
                name = name.replaceAll("(?i)[-_](btn|button|input|field|link|txt|id)$", "");
            }
            // Extract from role=button[name="..."] pattern
            else if (selector.contains("role=") && selector.contains("name=")) {
                int start = selector.indexOf("name=") + 5;
                if (selector.charAt(start) == '"') start++;
                int end = selector.indexOf("\"", start);
                if (end == -1) end = selector.indexOf("]", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from :has-text("...") pattern
            else if (selector.contains(":has-text(")) {
                int start = selector.indexOf(":has-text(") + 11;
                int end = selector.indexOf(")", start);
                if (end > start) {
                    name = selector.substring(start, end).replace("\"", "").replace("'", "");
                }
            }
            // Extract from getByRole patterns (modern Playwright)
            else if (selector.contains("getByRole")) {
                if (selector.contains("name=\"")) {
                    int start = selector.indexOf("name=\"") + 6;
                    int end = selector.indexOf("\"", start);
                    if (end > start) {
                        name = selector.substring(start, end);
                    }
                }
            }
            // Extract from name attribute
            else if (selector.contains("name=\"")) {
                int start = selector.indexOf("name=\"") + 6;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from placeholder attribute
            else if (selector.contains("placeholder=\"")) {
                int start = selector.indexOf("placeholder=\"") + 13;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from aria-label
            else if (selector.contains("aria-label=\"")) {
                int start = selector.indexOf("aria-label=\"") + 12;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from title attribute
            else if (selector.contains("title=\"")) {
                int start = selector.indexOf("title=\"") + 7;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from value attribute (for buttons with value)
            else if (selector.contains("value=\"")) {
                int start = selector.indexOf("value=\"") + 7;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from class name (last class)
            else if (selector.startsWith(".")) {
                name = selector.substring(1).split("\\.")[0];
                // Convert kebab-case or camelCase class names
                name = name.replaceAll("-", " ").replaceAll("_", " ");
            }
            // Extract from data-testid or data-test-id
            else if (selector.contains("data-testid=\"") || selector.contains("data-test-id=\"")) {
                String pattern = selector.contains("data-testid=\"") ? "data-testid=\"" : "data-test-id=\"";
                int start = selector.indexOf(pattern) + pattern.length();
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                    name = name.replaceAll("-", " ").replaceAll("_", " ");
                }
            }

            // Clean up the name
            name = name.trim()
                       .replaceAll("[^a-zA-Z0-9\\s]", " ")  // Remove special chars
                       .replaceAll("\\s+", " ")              // Normalize spaces
                       .trim();

            // Remove common button/input suffixes for cleaner names
            name = name.replaceAll("(?i)\\s+(button|btn|input|field|link|checkbox|radio)$", "");

            // Convert to PascalCase
            String[] words = name.split("\\s+");
            StringBuilder pascalCase = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    pascalCase.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        pascalCase.append(word.substring(1).toLowerCase());
                    }
                }
            }

            String result = pascalCase.toString();

            // If result is empty or just numbers, provide a more descriptive default
            if (result.isEmpty()) {
                return "Element";
            } else if (result.matches("^\\d+$")) {
                return "Number" + result;
            }

            return result;
        }

        /**
         * Generate element constant name (e.g., SIGN_IN_BUTTON, SAVE_BUTTON)
         */
        private String generateElementName(String readableName, int id) {
            // Convert PascalCase to UPPER_SNAKE_CASE
            String snakeCase = readableName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
            return snakeCase + "_" + id;
        }

        /**
         * Generate method name based on action type and readable name.
         * Creates descriptive, verb-based method names following Java conventions.
         */
        private String generateMethodName(String type, String readableName, int id) {
            switch (type) {
                case "click":
                    // Use "click" for buttons/links, makes sense contextually
                    return "click" + readableName;

                case "fill":
                    // Use "enter" for text fields to match natural language
                    // Special handling for common field names
                    if (readableName.toLowerCase().contains("search")) {
                        return "search" + readableName.replace("Search", "");
                    } else if (readableName.toLowerCase().contains("email")) {
                        return "enterEmail";
                    } else if (readableName.toLowerCase().contains("password")) {
                        return "enterPassword";
                    } else if (readableName.toLowerCase().contains("username")) {
                        return "enterUsername";
                    }
                    return "enter" + readableName;

                case "select":
                    // Use "select" for dropdowns
                    return "select" + readableName;

                case "check":
                    // Use "check" for checkboxes, "toggle" for switches
                    if (readableName.toLowerCase().contains("toggle") ||
                        readableName.toLowerCase().contains("switch")) {
                        return "toggle" + readableName.replace("Toggle", "").replace("Switch", "");
                    }
                    return "check" + readableName;

                case "press":
                    // Use "pressKeyOn" for keyboard actions
                    return "pressKeyOn" + readableName;

                case "navigate":
                    return "navigateTo";

                default:
                    // Fallback with camelCase
                    String camelCase = readableName.substring(0, 1).toLowerCase() +
                                      (readableName.length() > 1 ? readableName.substring(1) : "");
                    return camelCase + "Action";
            }
        }
        
        /**
         * Generate human-readable step text.
         */
        private String generateStepText(String type, String readableName) {
            String lowerName = readableName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

            switch (type) {
                case "click": return "user clicks on " + lowerName;
                case "fill": return "user enters text into " + lowerName;
                case "select": return "user selects option from " + lowerName;
                case "check": return "user checks " + lowerName;
                case "press": return "user presses key on " + lowerName;
                case "navigate": return "user navigates to page";
                default: return "user performs action on " + lowerName;
            }
        }
    }
    
    /**
     * Generate test files from Playwright recording.
     * Main entry point for recording-based generation.
     * 
     * TODO: MANDATORY PRE-GENERATION CHECKS:
     * 1. Check if page object exists (avoid overwriting custom code)
     * 2. Verify existing login patterns (reuse existing steps)
     * 3. Check configurations.properties for test data
     * 4. Validate all selectors follow priority order
     * 5. Warn about dynamic IDs before generation
     * 
     * @param recordingFile Path to recorded-actions.java file
     * @param featureName Name of the feature (e.g., "login", "profile")
     * @param pageUrl Page URL or path
     * @param jiraStory JIRA story ID
     * @return true if successful, false otherwise
     */
    public static boolean generateFromRecording(String recordingFile, String featureName, 
                                                  String pageUrl, String jiraStory) {
        System.out.println("[INFO] Pure Java Test File Generator");
        System.out.println("[INFO] Recording file: " + recordingFile);
        System.out.println("[INFO] Feature name: " + featureName);
        System.out.println("[INFO] Page URL: " + pageUrl);
        System.out.println("[INFO] JIRA Story: " + jiraStory);
        
        // MANDATORY - Run pre-generation validation checks
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          CODE REUSABILITY & VALIDATION CHECKS                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        String className = featureName.substring(0, 1).toUpperCase() + featureName.substring(1);
        boolean hasExistingPageObject = false;
        boolean hasExistingLogin = false;
        boolean hasConfiguredData = false;
        
        // Check 1: Existing page object
        System.out.println("🔍 [CHECK 1] Scanning for existing page objects...");
        if (pageObjectExists(className)) {
            hasExistingPageObject = true;
            System.out.println("✅ FOUND: Page object " + className + ".java already exists!");
            System.out.println("   📁 Location: src/main/java/pages/" + className + ".java");
            System.out.println("   ⚠️  ACTION: Will SKIP generation to avoid overwriting custom code");
            System.out.println("   💡 TIP: Review existing methods before manually integrating new actions\n");
        } else {
            System.out.println("⚠️  NOT FOUND: Page object doesn't exist - will create new " + className + ".java\n");
        }
        
        // Check 2: Existing login patterns
        System.out.println("🔍 [CHECK 2] Detecting existing login/authentication code...");
        String existingLogin = detectExistingLogin();
        if (existingLogin != null) {
            hasExistingLogin = true;
            System.out.println("✅ FOUND: Existing login class: " + existingLogin + ".java");
            System.out.println("   📁 Location: src/main/java/pages/" + existingLogin + ".java");
            System.out.println("   📝 REUSE INSTRUCTIONS:");
            System.out.println("      1. Import in Step Definitions: import pages." + existingLogin + ";");
            System.out.println("      2. Call login methods: " + existingLogin + ".enterValidUsernameFromConfiguration(page);");
            System.out.println("      3. Call login methods: " + existingLogin + ".enterValidPasswordFromConfiguration(page);");
            System.out.println("      4. Call login methods: " + existingLogin + ".clickSignIn(page);");
            System.out.println("   💡 TIP: Avoid regenerating login steps - reuse existing validated methods!\n");
        } else {
            System.out.println("⚠️  NOT FOUND: No existing login patterns detected\n");
        }
        
        // Check 3: Configuration test data
        System.out.println("🔍 [CHECK 3] Checking for configured test credentials...");
        if (hasConfiguredCredentials()) {
            hasConfiguredData = true;
            System.out.println("✅ FOUND: Test credentials configured in configurations.properties");
            System.out.println("   📁 Location: src/test/resources/configurations.properties");
            System.out.println("   📝 USAGE INSTRUCTIONS:");
            System.out.println("      1. In Page Objects: loadProps.getProperty(\"Username\")");
            System.out.println("      2. In Step Defs: Call methods like enterValidUsernameFromConfiguration()");
            System.out.println("      3. In Features: Reference as 'valid credentials from configuration'");
            System.out.println("   💡 TIP: Use configuration data instead of hardcoded values!\n");
        } else {
            System.out.println("⚠️  NOT FOUND: No test credentials in configurations.properties");
            System.out.println("   💡 TIP: Add Username and Password properties for data-driven testing\n");
        }
        
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        try {
            // Parse recording file
            List<RecordedAction> actions = parseRecording(recordingFile);
            System.out.println("[INFO] Extracted " + actions.size() + " actions from recording");
            
            // TODO: Check 4 & 5 - Validate selectors and dynamic IDs
            int dynamicIdCount = 0;
            for (RecordedAction action : actions) {
                if (action.selector != null && action.selector.contains("@id=")) {
                    String id = action.selector.replaceAll(".*@id=['\"]([^'\"]+)['\"].*", "$1");
                    if (isDynamicId(id)) {
                        dynamicIdCount++;
                        System.out.println("⚠️ [CHECK 5] Dynamic ID detected: " + id + " (will be downgraded)");
                    }
                }
            }
            if (dynamicIdCount > 0) {
                System.out.println("⚠️ [TODO] Found " + dynamicIdCount + " dynamic IDs - consider using relative XPath instead\n");
            } else {
                System.out.println("✅ [CHECK 4 & 5] No dynamic IDs detected - selectors are stable\n");
            }
            
            // Generate files
            generatePageObject(className, pageUrl, jiraStory, actions);
            generateFeatureFile(className, jiraStory, actions);
            generateStepDefinitions(className, jiraStory, actions);
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║              GENERATION COMPLETE - NEXT STEPS                  ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("✅ [SUCCESS] All files generated successfully!\n");
            
            System.out.println("📂 GENERATED FILES:");
            System.out.println("   1. Page Object:      src/main/java/pages/" + className + ".java");
            System.out.println("   2. Feature File:     src/test/java/features/" + className + ".feature");
            System.out.println("   3. Step Definitions: src/test/java/stepDefs/" + className + "Steps.java\n");
            
            // Integration instructions
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("📝 INTEGRATION CHECKLIST:");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            if (hasExistingLogin) {
                System.out.println("🔄 [CRITICAL: REUSE EXISTING LOGIN - ACTION REQUIRED!]");
                System.out.println("   ⚠️  Login pattern detected - DO NOT use generated login steps!");
                System.out.println("   ✅ Existing login class found: " + existingLogin + ".java");
                System.out.println("   📝 MANDATORY STEPS:");
                System.out.println("   ✓ Step 1: Open " + className + "Steps.java");
                System.out.println("   ✓ Step 2: Find the LOGIN STEPS section (marked with TODO)");
                System.out.println("   ✓ Step 3: Replace generated code with existing login methods");
                System.out.println("   ✓ Step 4: Update " + className + ".feature to use these steps:\n");
                System.out.println("      When User enters valid username from configuration");
                System.out.println("      And User enters valid password from configuration");
                System.out.println("      And User clicks on Sign In button\n");
                System.out.println("   💡 WHY? Existing login is tested, uses config data, and maintains consistency!\n");
            }
            
            if (hasConfiguredData) {
                System.out.println("📋 [USE CONFIGURED TEST DATA]");
                System.out.println("   ✓ Test credentials found in configurations.properties");
                System.out.println("   ✓ Available properties: Username, Password (check config file for more)");
                System.out.println("   ✓ Usage: loadProps.getProperty(\"Username\")");
                System.out.println("   ✓ Already implemented in: enterValidUsernameFromConfiguration() methods\n");
            }
            
            System.out.println("🔨 [COMPILE PROJECT]");
            System.out.println("   ✓ Run: mvn clean compile");
            System.out.println("   ✓ Or:  generate-test.bat → Option 3 (Validate & Run)\n");
            
            System.out.println("🧪 [RUN TESTS]");
            System.out.println("   ✓ Run specific feature: mvn test -Dcucumber.filter.tags=@" + className);
            System.out.println("   ✓ Run all tests: mvn test");
            System.out.println("   ✓ Or use: generate-test.bat → Option 3 (Validate & Run)\n");
            
            System.out.println("📊 [VIEW REPORTS]");
            System.out.println("   ✓ Location: MRITestExecutionReports/Version*/extentReports/");
            System.out.println("   ✓ Open latest: HTML report in testNGExtentReports/html/\n");
            
            System.out.println("💡 [VERIFICATION TIPS]");
            System.out.println("   ✓ Review generated locators in " + className + ".java");
            System.out.println("   ✓ Check for dynamic IDs warnings above");
            System.out.println("   ✓ Verify steps match recorded actions in .feature file");
            System.out.println("   ✓ Ensure step definitions import correct page objects");
            System.out.println("   ✓ Test manually before CI/CD integration\n");
            
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate test files: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Checks if an ID is dynamic (contains GUIDs, timestamps, or random strings).
     * Dynamic IDs should be avoided as they change between sessions.
     * 
     * @param id The ID to check
     * @return true if the ID appears to be dynamic
     */
    private static boolean isDynamicId(String id) {
        if (id == null || id.isEmpty()) return false;
        
        // Check for GUID patterns (e.g., b0f53fd4-e8a9-4e88-87af-4456e7b35a2e)
        if (id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            return true;
        }
        
        // Check for random hash patterns (long strings of random characters)
        if (id.length() > 20 && id.matches("[a-zA-Z0-9]{20,}")) {
            return true;
        }
        
        // Check for timestamp patterns
        if (id.matches(".*\\d{13,}.*")) { // Unix timestamp
            return true;
        }
        
        // Check for random suffixes like _1234567890
        if (id.matches(".*[_-]\\d{8,}$")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets priority comment for a selector (used in generated code).
     * TODO: MANDATORY - All generated locators must show their priority level
     * 
     * @param selector The selector to analyze
     * @return Priority comment string
     */
    private static String getPriorityComment(String selector) {
        if (selector == null || selector.isEmpty()) return "Unknown Priority";
        
        // Priority 1: Static ID
        if (selector.contains("@id=") || selector.contains("[@id='")) {
            String id = selector.replaceAll(".*@id=['\"]([^'\"]+)['\"].*", "$1");
            if (isDynamicId(id)) {
                return "Priority 3: XPath (Dynamic ID downgraded)";
            }
            return "Priority 1: Static ID ⭐⭐⭐⭐⭐";
        }
        
        // Priority 2: Relative XPath (contains //)
        if (selector.startsWith("//") && !selector.matches("^/html/.*")) {
            return "Priority 2: Relative XPath ⭐⭐⭐⭐";
        }
        
        // Priority 3: Absolute XPath
        if (selector.startsWith("/html")) {
            return "Priority 3: Absolute XPath ⭐⭐⭐";
        }
        
        // Priority 4: Label or Names
        if (selector.startsWith("label=") || selector.contains("@name=")) {
            return "Priority 4: Label/Name ⭐⭐";
        }
        
        // Priority 5: Class name
        if (selector.contains("@class=") || selector.matches(".*\\[@class.*")) {
            return "Priority 5: Class name ⭐";
        }
        
        // Priority 6: CSS
        if (selector.startsWith("css=") || (!selector.startsWith("//") && !selector.startsWith("label="))) {
            return "Priority 6: CSS ⭐";
        }
        
        return "Unknown Priority";
    }
    
    /**
     * Optimizes a selector by preferring stable locators.
     * Priority Order:
     * 1. Static ID (highest priority)
     * 2. Relative XPath
     * 3. Absolute XPath
     * 4. Label or Names
     * 5. Class name
     * 6. CSS (lowest priority)
     * 
     * @param selector The original selector
     * @return Optimized selector following priority order
     */
    private static String optimizeSelector(String selector) {
        if (selector == null || selector.isEmpty()) return selector;
        
        System.out.println("[DEBUG] Optimizing selector: " + selector);
        
        // Common field name to ID mappings (for label conversion)
        Map<String, String> labelToId = new HashMap<>();
        labelToId.put("Username", "Username");
        labelToId.put("Password", "Password");
        labelToId.put("First Name", "FirstName");
        labelToId.put("Last Name", "LastName");
        labelToId.put("Mobile Phone Number", "MobilePhoneNumber");
        labelToId.put("Email", "Email");
        labelToId.put("Phone", "Phone");
        labelToId.put("Address", "Address");
        labelToId.put("City", "City");
        labelToId.put("State", "State");
        labelToId.put("Zip Code", "ZipCode");
        labelToId.put("Country", "Country");
        
        // ========== PRIORITY 1: STATIC ID ==========
        // Check for ID selectors in various formats
        String extractedId = null;
        
        // Format: #idValue
        if (selector.startsWith("#")) {
            extractedId = selector.substring(1).split("[\\s\\[\\]\\.\\:]")[0];
        }
        // Format: id='value' or id="value"
        else if (selector.contains("id=")) {
            Pattern idPattern = Pattern.compile("id=['\"]([^'\"]+)['\"]?");
            Matcher matcher = idPattern.matcher(selector);
            if (matcher.find()) {
                extractedId = matcher.group(1);
            }
        }
        // Format: [@id='value'] in XPath
        else if (selector.contains("[@id=") || selector.contains("[@id =")) {
            Pattern xpathIdPattern = Pattern.compile("@id\\s*=\\s*['\"]([^'\"]+)['\"]");
            Matcher matcher = xpathIdPattern.matcher(selector);
            if (matcher.find()) {
                extractedId = matcher.group(1);
            }
        }
        
        // If ID found, check if it's static
        if (extractedId != null && !extractedId.isEmpty()) {
            if (isDynamicId(extractedId)) {
                System.out.println("[PRIORITY 1 - SKIP] Dynamic ID detected: " + extractedId + " - Moving to lower priority");
            } else {
                System.out.println("[✓ PRIORITY 1] Static ID found: " + extractedId);
                return "//input[@id='" + extractedId + "'] | //button[@id='" + extractedId + "'] | " +
                       "//textarea[@id='" + extractedId + "'] | //select[@id='" + extractedId + "'] | " +
                       "//*[@id='" + extractedId + "']";
            }
        }
        
        // ========== PRIORITY 2: RELATIVE XPATH ==========
        // Already in relative XPath format (starts with //)
        if (selector.startsWith("//") || selector.startsWith("(//")) {
            System.out.println("[✓ PRIORITY 2] Relative XPath detected");
            return selector;
        }
        
        // ========== PRIORITY 3: ABSOLUTE XPATH ==========
        // Absolute XPath (starts with single /)
        if (selector.startsWith("/") && !selector.startsWith("//")) {
            System.out.println("[✓ PRIORITY 3] Absolute XPath detected");
            return selector;
        }
        
        // ========== PRIORITY 4: LABEL OR NAMES ==========
        // Handle label= locator - try to convert to ID (Priority 1) or XPath
        if (selector.startsWith("label=")) {
            String labelText = selector.substring(6).trim();
            System.out.println("[PRIORITY 4] Label selector detected: " + labelText);
            
            // Try to upgrade to Priority 1 (Static ID) if mapping exists
            String fieldId = labelToId.get(labelText);
            if (fieldId != null) {
                System.out.println("[✓ PRIORITY 4 → PRIORITY 1] Label mapped to static ID: " + fieldId);
                return "//input[@id='" + fieldId + "'] | //textarea[@id='" + fieldId + "'] | //select[@id='" + fieldId + "']";
            }
            
            // Convert to relative XPath (Priority 2)
            System.out.println("[✓ PRIORITY 4 → PRIORITY 2] Converting label to relative XPath");
            return "//label[normalize-space(text())='" + labelText + "']/..//input | " +
                   "//label[normalize-space(text())='" + labelText + "']/..//textarea | " +
                   "//label[normalize-space(text())='" + labelText + "']/..//select";
        }
        
        // Handle name attribute
        if (selector.startsWith("name=") || (selector.contains("name=") && !selector.contains("text="))) {
            Pattern namePattern = Pattern.compile("name=['\"]([^'\"]+)['\"]?");
            Matcher matcher = namePattern.matcher(selector);
            if (matcher.find()) {
                String name = matcher.group(1);
                System.out.println("[✓ PRIORITY 4] Name attribute found: " + name);
                return "//input[@name='" + name + "'] | //button[@name='" + name + "'] | " +
                       "//textarea[@name='" + name + "'] | //select[@name='" + name + "'] | " +
                       "//*[@name='" + name + "']";
            }
        }
        
        // Handle placeholder (treat as name-like selector)
        if (selector.startsWith("placeholder=")) {
            String placeholder = selector.substring(12).trim();
            System.out.println("[✓ PRIORITY 4] Placeholder attribute found: " + placeholder);
            return "//input[@placeholder='" + placeholder + "'] | //textarea[@placeholder='" + placeholder + "']";
        }
        
        // Handle text= locator (treat as name-like)
        if (selector.startsWith("text=")) {
            String text = selector.substring(5).trim();
            System.out.println("[✓ PRIORITY 4] Text selector found: " + text);
            return "//button[normalize-space(text())='" + text + "'] | " +
                   "//a[normalize-space(text())='" + text + "'] | " +
                   "//*[normalize-space(text())='" + text + "']";
        }
        
        // ========== PRIORITY 5: CLASS NAME ==========
        // CSS class selector: .className
        if (selector.startsWith(".")) {
            String className = selector.substring(1).split("[\\s\\[\\]\\.\\:]")[0];
            System.out.println("[✓ PRIORITY 5] Class name found: " + className);
            return "//*[contains(@class, '" + className + "')]";
        }
        
        // Attribute selector with class
        if (selector.contains("class=") && !selector.startsWith("//")) {
            Pattern classPattern = Pattern.compile("class=['\"]([^'\"]+)['\"]?");
            Matcher matcher = classPattern.matcher(selector);
            if (matcher.find()) {
                String className = matcher.group(1).split("\\s+")[0]; // Get first class
                System.out.println("[✓ PRIORITY 5] Class attribute found: " + className);
                return "//*[contains(@class, '" + className + "')]";
            }
        }
        
        // ========== PRIORITY 6: CSS SELECTORS ==========
        // Generic CSS selectors (lowest priority)
        if (!selector.startsWith("//") && !selector.startsWith("/") && 
            (selector.contains(">") || selector.contains("+") || selector.contains("~") || 
             selector.contains("[") || selector.contains(":"))) {
            System.out.println("[✓ PRIORITY 6] CSS selector detected (consider upgrading to higher priority)");
        }
        
        System.out.println("[INFO] Selector used as-is (no optimization applied)");
        return selector; // Return original if no optimization found
    }
    
    /**
     * Parse Playwright recording file and extract actions.
     * Supports both old API (page.click) and modern API (page.locator().click())
     * 
     * LOCATOR OPTIMIZATION: Applies optimizeSelector() to prioritize:
     * 1. Static ID, 2. Relative XPath, 3. Absolute XPath, 4. Label/Names, 5. Class, 6. CSS
     * 
     * DYNAMIC ID DETECTION: Identifies and warns about GUID/timestamp IDs using isDynamicId()
     */
    private static List<RecordedAction> parseRecording(String recordingFile) throws IOException {
        List<RecordedAction> actions = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(recordingFile)));
        
        // Patterns for MODERN Playwright Locator API (primary)
        Pattern locatorClickPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern locatorFillPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern locatorSelectPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.selectOption\\(\"([^\"]+)\"\\)");
        Pattern locatorCheckPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.check\\(");
        Pattern locatorPressPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.press\\(\"([^\"]+)\"\\)");
        
        // Modern getBy* API patterns
        Pattern getByRoleClickPattern = Pattern.compile("page\\.getByRole\\(AriaRole\\.\\w+,\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)\\)\\.click\\(");
        Pattern getByTextClickPattern = Pattern.compile("page\\.getByText\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern getByPlaceholderFillPattern = Pattern.compile("page\\.getByPlaceholder\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelClickPattern = Pattern.compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern getByLabelFillPattern = Pattern.compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelPressPattern = Pattern.compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.press\\(\"([^\"]+)\"\\)");
        
        // Patterns for OLD Playwright API (fallback)
        Pattern clickPattern = Pattern.compile("page\\.click\\(\"([^\"]+)\"\\)");
        Pattern fillPattern = Pattern.compile("page\\.fill\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern selectPattern = Pattern.compile("page\\.selectOption\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern checkPattern = Pattern.compile("page\\.check\\(\"([^\"]+)\"\\)");
        Pattern pressPattern = Pattern.compile("page\\.press\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern navigatePattern = Pattern.compile("page\\.navigate\\(\"([^\"]+)\"\\)");
        
        String[] lines = content.split("\\r?\\n");
        int actionId = 1;
        
        System.out.println("[DEBUG] Parsing recording file with " + lines.length + " lines");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("import") || line.startsWith("package")) {
                continue;
            }
            
            Matcher matcher;
            
            // Check for navigate
            matcher = navigatePattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found navigate: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "navigate", null, matcher.group(1)));
                continue;
            }
            
            // MODERN API - locator().click()
            matcher = locatorClickPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator click: " + matcher.group(1) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "click", selector, null));
                continue;
            }
            
            // MODERN API - getByRole().click()
            matcher = getByRoleClickPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByRole click: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "click", "text=" + matcher.group(1), null));
                continue;
            }
            
            // MODERN API - getByText().click()
            matcher = getByTextClickPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByText click: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "click", "text=" + matcher.group(1), null));
                continue;
            }
            
            // MODERN API - locator().fill()
            matcher = locatorFillPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator fill: " + matcher.group(1) + " = " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "fill", selector, matcher.group(2)));
                continue;
            }
            
            // MODERN API - getByPlaceholder().fill()
            matcher = getByPlaceholderFillPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByPlaceholder fill: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "fill", "placeholder=" + matcher.group(1), matcher.group(2)));
                continue;
            }
            
            // MODERN API - getByLabel().click()
            matcher = getByLabelClickPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByLabel click: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "click", "label=" + matcher.group(1), null));
                continue;
            }
            
            // MODERN API - getByLabel().fill()
            matcher = getByLabelFillPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByLabel fill: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "fill", "label=" + matcher.group(1), matcher.group(2)));
                continue;
            }
            
            // MODERN API - getByLabel().press()
            matcher = getByLabelPressPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found getByLabel press: " + matcher.group(1) + " - " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "press", "label=" + matcher.group(1), matcher.group(2)));
                continue;
            }
            
            // MODERN API - locator().selectOption()
            matcher = locatorSelectPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator select: " + matcher.group(1) + " = " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "select", selector, matcher.group(2)));
                continue;
            }
            
            // MODERN API - locator().check()
            matcher = locatorCheckPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator check: " + matcher.group(1) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "check", selector, null));
                continue;
            }
            
            // MODERN API - locator().press()
            matcher = locatorPressPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator press: " + matcher.group(1) + " - " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "press", selector, matcher.group(2)));
                continue;
            }
            
            // OLD API fallbacks
            matcher = clickPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API click: " + matcher.group(1) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "click", selector, null));
                continue;
            }
            
            matcher = fillPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API fill: " + matcher.group(1) + " = " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "fill", selector, matcher.group(2)));
                continue;
            }
            
            matcher = selectPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API select: " + matcher.group(1) + " = " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "select", selector, matcher.group(2)));
                continue;
            }
            
            matcher = checkPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API check: " + matcher.group(1) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "check", selector, null));
                continue;
            }
            
            matcher = pressPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API press: " + matcher.group(1) + " - " + matcher.group(2) + 
                    (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "press", selector, matcher.group(2)));
                continue;
            }
        }
        
        // If no actions found, add a default navigate action
        if (actions.isEmpty()) {
            System.out.println("[WARN] No actions found in recording, adding default navigation");
            System.out.println("[WARN] Check if recording file contains actual Playwright actions");
            actions.add(new RecordedAction(1, "navigate", null, ""));
        } else {
            System.out.println("[SUCCESS] Extracted " + actions.size() + " actions from recording");
        }
        
        return actions;
    }
    
    /**
     * Generate Page Object from recorded actions.
     * 
     * CODE REUSABILITY: Checks pageObjectExists() to avoid overwriting existing classes
     * LOCATOR OPTIMIZATION: All selectors optimized via optimizeSelector() before generation
     * SKIP LOGIC: Returns early if page object exists, preserving custom implementations
     */
    private static void generatePageObject(String className, String pageUrl, String jiraStory, 
                                           List<RecordedAction> actions) throws IOException {
        
        // Check if page object already exists
        if (pageObjectExists(className)) {
            System.out.println("[REUSE] Page object already exists: " + className + ".java");
            System.out.println("[INFO] Skipping page object generation - using existing implementation");
            System.out.println("[TIP] To add new methods, manually edit: src/main/java/pages/" + className + ".java");
            return; // Skip generation, use existing
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("package pages;\n");
        sb.append("import com.microsoft.playwright.Page;\n");
        sb.append("import configs.loadProps;\n");
        sb.append("import static configs.utils.*;\n");
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Page Object for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording by Pure Java Generator\n");
        sb.append(" * @story ").append(jiraStory).append("\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append(" extends BasePage {\n");
        sb.append("    private static final String PAGE_PATH = \"").append(pageUrl).append("\";\n");
        sb.append("\n");
        
        // Generate locator constants with descriptive names
        // TODO: MANDATORY - Verify all selectors follow priority order
        int staticIdCount = 0;
        int labelSelectorCount = 0;
        int textSelectorCount = 0;
        
        // DEDUPLICATION: Track unique locators and methods
        Set<String> generatedLocators = new HashSet<>();
        Set<String> generatedMethods = new HashSet<>();
        Set<String> locatorConstants = new HashSet<>();
        
        for (RecordedAction action : actions) {
            if (action.selector != null) {
                // Skip duplicate locators (same selector string)
                if (generatedLocators.contains(action.selector)) {
                    System.out.println("[SKIP DUPLICATE] Locator already exists: " + action.selector);
                    continue;
                }
                
                // Skip duplicate locator constant names
                if (locatorConstants.contains(action.elementName)) {
                    System.out.println("[SKIP DUPLICATE] Locator constant already defined: " + action.elementName);
                    continue;
                }
                
                generatedLocators.add(action.selector);
                locatorConstants.add(action.elementName);
                
                // Count selector types for validation
                if (action.selector.contains("@id=") && !isDynamicId(action.selector)) {
                    staticIdCount++;
                } else if (action.selector.startsWith("label=")) {
                    labelSelectorCount++;
                    System.out.println("⚠️ [WARNING] Using label= selector (Priority 4): " + action.selector);
                    System.out.println("   TODO: Verify static ID is not available for " + action.readableName);
                } else if (action.selector.startsWith("text=")) {
                    textSelectorCount++;
                }
                
                // TODO: Add comment showing priority level used
                String priorityComment = getPriorityComment(action.selector);
                sb.append("    // ").append(action.readableName).append(" - ").append(priorityComment).append("\n");
                sb.append("    private static final String ").append(action.elementName)
                  .append(" = \"").append(escapeJavaString(action.selector)).append("\";\n");
                sb.append("\n");
            }
        }
        
        // TODO: Log summary of locator types used
        System.out.println("[LOCATOR SUMMARY] Static IDs: " + staticIdCount + 
                          ", Labels: " + labelSelectorCount + 
                          ", Text: " + textSelectorCount);
        if (labelSelectorCount > 0) {
            System.out.println("⚠️ [TODO] Review " + labelSelectorCount + 
                              " label= selectors - consider using XPath with static IDs");
        }

        // Generate navigate method
        sb.append("    public static void navigateTo(Page page) {\n");
        sb.append("        page.navigate(loadProps.getProperty(\"URL\") + PAGE_PATH);\n");
        sb.append("        System.out.println(\"✅ Navigated to ").append(className).append(" page: \" + loadProps.getProperty(\"URL\") + PAGE_PATH);\n");
        sb.append("    }\n");
        sb.append("\n");
        
        // Generate methods for each action with descriptive names
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type)) continue;
            
            // DEDUPLICATION: Skip duplicate methods
            if (generatedMethods.contains(action.methodName)) {
                System.out.println("[SKIP DUPLICATE] Method already exists: " + action.methodName + "()");
                continue;
            }
            generatedMethods.add(action.methodName);
            
            sb.append("    /**\n");
            sb.append("     * ").append(action.stepText).append("\n");
            sb.append("     * Element: ").append(action.readableName).append(" (").append(action.selector).append(")\n");
            sb.append("     */\n");

            switch (action.type) {
                case "click":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        System.out.println(\"🖱️ ").append(action.stepText).append(": \" + ").append(action.elementName).append(");\n");
                    sb.append("        clickOnElement(").append(action.elementName).append(");\n");
                    sb.append("    }\n");
                    break;
                    
                case "fill":
                    sb.append("    public static void ").append(action.methodName).append("(Page page, String text) {\n");
                    sb.append("        System.out.println(\"⌨️ ").append(action.stepText).append(": \" + ").append(action.elementName).append(" + \" = '\" + text + \"'\");\n");
                    sb.append("        enterText(").append(action.elementName).append(", text);\n");
                    sb.append("    }\n");
                    break;
                    
                case "select":
                    sb.append("    public static void ").append(action.methodName).append("(Page page, String option) {\n");
                    sb.append("        System.out.println(\"🔽 ").append(action.stepText).append(": \" + ").append(action.elementName).append(" + \" = '\" + option + \"'\");\n");
                    sb.append("        selectDropDownValueByText(").append(action.elementName).append(", option);\n");
                    sb.append("    }\n");
                    break;
                    
                case "check":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        System.out.println(\"☑️ ").append(action.stepText).append(": \" + ").append(action.elementName).append(");\n");
                    sb.append("        clickOnElement(").append(action.elementName).append(");\n");
                    sb.append("    }\n");
                    break;
                    
                case "press":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        System.out.println(\"⌨️ ").append(action.stepText).append(": \" + ").append(action.elementName).append(" + \" - Key: ").append(escapeJavaString(action.value)).append("\");\n");
                    sb.append("        page.locator(").append(action.elementName).append(").press(\"")
                      .append(escapeJavaString(action.value)).append("\");\n");
                    sb.append("    }\n");
                    break;
            }
            sb.append("\n");
        }
        
        sb.append("}\n");
        
        Files.write(Paths.get("src/main/java/pages/" + className + ".java"), sb.toString().getBytes());
    }
    
    /**
     * Generate Feature file from recorded actions.
     * 
     * SCENARIO OUTLINE: Includes actual recorded data in Examples table
     * CODE REUSABILITY: Detects existing login patterns via containsLoginPattern()
     *                   Suggests using existing steps from detectExistingLogin()
     *                   Checks hasConfiguredCredentials() for test data reuse
     */
    private static void generateFeatureFile(String className, String jiraStory, 
                                           List<RecordedAction> actions) throws IOException {
        
        boolean hasConfiguredData = hasConfiguredCredentials();
        boolean hasLoginPattern = containsLoginPattern(actions);
        String existingLoginClass = detectExistingLogin();
        boolean shouldReuseLogin = hasLoginPattern && existingLoginClass != null;
        
        if (hasLoginPattern && existingLoginClass != null) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║         FEATURE FILE - LOGIN REUSE RECOMMENDATION              ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("🔄 [REUSE TIP] Login pattern detected in feature");
            System.out.println("   📝 Consider using existing login steps from " + existingLoginClass + "Steps.java:\n");
            System.out.println("   RECOMMENDED STEPS:");
            System.out.println("      Given User navigates to the MRI Energy login page");
            System.out.println("      When User enters valid username from configuration");
            System.out.println("      And User enters valid password from configuration");
            System.out.println("      And User clicks on Sign In button");
            System.out.println("      Then User should be successfully logged in\n");
            System.out.println("   💡 TIP: Reference existing step definitions for consistency");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        }
        
        if (hasConfiguredData) {
            System.out.println("📋 [DATA TIP] Configured test data available");
            System.out.println("   ✓ Use parameterized steps like 'user enters valid username from configuration'");
            System.out.println("   ✓ Avoid hardcoding credentials in feature files");
            System.out.println("   ✓ Reference: src/test/resources/configurations.properties\n");
        }
        
        StringBuilder sb = new StringBuilder();
        List<String> exampleColumns = new ArrayList<>();
        List<String> exampleValues = new ArrayList<>();
        
        sb.append("@").append(jiraStory).append(" @").append(className).append("\n");
        sb.append("Feature: ").append(className).append(" Test\n");
        sb.append("  Auto-generated from Playwright recording\n");
        sb.append("\n");
        
        // Use Scenario instead of Scenario Outline if login reuse is enabled (no Examples table needed)
        if (shouldReuseLogin && hasLoginPattern) {
            sb.append("  Scenario: Complete ").append(className).append(" workflow with existing login\n");
        } else {
            sb.append("  Scenario Outline: Complete ").append(className).append(" workflow\n");
        }
        sb.append("    Given user navigates to ").append(className).append(" page\n");
        
        // DEDUPLICATION: Track generated steps in feature file
        Set<String> generatedFeatureSteps = new HashSet<>();
        boolean hasGeneratedLoginSteps = false;
        
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type)) continue;
            
            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);
            
            // If login reuse enabled and this is login action, replace with existing login steps
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══\n");
                sb.append("    When User enters valid username from configuration\n");
                sb.append("    And User enters valid password from configuration\n");
                sb.append("    And User clicks on Sign In button\n");
                sb.append("    # ═══════════════════════════════════════════════\n");
                hasGeneratedLoginSteps = true;
                continue; // Skip generating the hardcoded login step
            }
            
            String featureStep = "";
            
            switch (action.type) {
                case "click":
                    featureStep = "When " + action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);
                    
                    sb.append("    ").append(featureStep).append("\n");
                    break;
                case "fill":
                    // Skip login-related fill actions if login reuse is enabled
                    if (shouldReuseLogin && isLoginRelatedAction(action)) {
                        System.out.println("[SKIP LOGIN FIELD] Not adding to feature file (using existing login): " + action.readableName);
                        continue;
                    }
                    
                    String columnName = action.readableName.toLowerCase().replace(" ", "");
                    featureStep = "And user enters \"<" + columnName + ">\" into " + action.readableName.toLowerCase();
                    
                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);
                    
                    sb.append("    ").append(featureStep).append("\n");
                    exampleColumns.add(columnName);
                    exampleValues.add(action.value != null ? action.value : "");
                    break;
                case "select":
                    String selectColumn = action.readableName.toLowerCase().replace(" ", "");
                    featureStep = "And user selects \"<" + selectColumn + ">\" from " + action.readableName.toLowerCase();
                    
                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);
                    
                    sb.append("    ").append(featureStep).append("\n");
                    exampleColumns.add(selectColumn);
                    exampleValues.add(action.value != null ? action.value : "");
                    break;
                case "check":
                    featureStep = "And " + action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);
                    
                    sb.append("    ").append(featureStep).append("\n");
                    break;
                case "press":
                    featureStep = "And " + action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);
                    
                    sb.append("    ").append(featureStep).append("\n");
                    break;
            }
        }
        
        sb.append("    Then page should be updated\n");
        
        // Add Examples table with actual recorded data
        if (!exampleColumns.isEmpty()) {
            sb.append("\n    Examples:\n");
            sb.append("      | ");
            sb.append(String.join(" | ", exampleColumns));
            sb.append(" |\n");
            sb.append("      | ");
            sb.append(String.join(" | ", exampleValues));
            sb.append(" |\n");
        }
        
        Files.write(Paths.get("src/test/java/features/" + className + ".feature"), sb.toString().getBytes());
    }
    
    /**
     * Generate Step Definitions from recorded actions.
     * 
     * CODE REUSABILITY: 
     *   - Detects login patterns via containsLoginPattern()
     *   - Imports existing classes via detectExistingLogin()
     *   - Adds documentation about reused methods
     *   - Provides tips for integrating with existing login steps
     */
    private static void generateStepDefinitions(String className, String jiraStory, 
                                               List<RecordedAction> actions) throws IOException {
        
        // Detect if we need to import existing page objects for reuse
        boolean hasLoginPattern = containsLoginPattern(actions);
        String existingLoginClass = detectExistingLogin();
        boolean shouldReuseLogin = hasLoginPattern && existingLoginClass != null;
        
        if (shouldReuseLogin) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║            CODE REUSE OPPORTUNITY DETECTED                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("🔄 [LOGIN REUSE] Detected login pattern in recorded actions");
            System.out.println("   ✅ Existing login class: " + existingLoginClass + ".java");
            System.out.println("   📁 Location: src/main/java/pages/" + existingLoginClass + ".java\n");
            
            System.out.println("📝 MANUAL INTEGRATION STEPS:");
            System.out.println("   1. Open generated file: src/test/java/stepDefs/" + className + "Steps.java");
            System.out.println("   2. Locate login-related step definitions (look for 'username', 'password', 'signin')");
            System.out.println("   3. Replace with existing login methods:\n");
            System.out.println("      INSTEAD OF:");
            System.out.println("        @When(\"user enters text into username\")");
            System.out.println("        public void enterUsername(String text) {");
            System.out.println("            " + className + ".enterUsername(page, text);");
            System.out.println("        }\n");
            System.out.println("      USE THIS:");
            System.out.println("        @When(\"user enters valid username from configuration\")");
            System.out.println("        public void enterValidUsername() {");
            System.out.println("            " + existingLoginClass + ".enterValidUsernameFromConfiguration(page);");
            System.out.println("        }\n");
            
            System.out.println("   4. Update Feature file: src/test/java/features/" + className + ".feature");
            System.out.println("      Change step text to match existing login steps\n");
            
            System.out.println("💡 BENEFITS:");
            System.out.println("   ✓ Reuses tested and validated login methods");
            System.out.println("   ✓ Uses configured credentials from configurations.properties");
            System.out.println("   ✓ Consistent login behavior across all tests");
            System.out.println("   ✓ Less code duplication and maintenance\n");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("package stepDefs;\n");
        sb.append("import configs.browserSelector;\n");
        sb.append("import io.cucumber.java.en.*;\n");
        sb.append("import pages.").append(className).append(";\n");
        
        // Import existing login class if detected and different from current class
        if (shouldReuseLogin && !className.equals(existingLoginClass)) {
            sb.append("import pages.").append(existingLoginClass).append(";\n");
        }
        
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Step Definitions for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording by Pure Java Generator\n");
        if (shouldReuseLogin && !className.equals(existingLoginClass)) {
            sb.append(" * Reuses existing login methods from ").append(existingLoginClass).append(".java\n");
        }
        sb.append(" * @story ").append(jiraStory).append("\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append("Steps extends browserSelector {\n");
        sb.append("\n");
        sb.append("    @Given(\"user navigates to ").append(className).append(" page\")\n");
        sb.append("    public void navigateTo() {\n");
        sb.append("        System.out.println(\"📍 Step: Navigating to ").append(className).append(" page\");\n");
        sb.append("        ").append(className).append(".navigateTo(page);\n");
        sb.append("    }\n");
        sb.append("\n");
        
        // DEDUPLICATION: Track generated step definitions
        Set<String> generatedSteps = new HashSet<>();
        Set<String> generatedStepMethods = new HashSet<>();
        
        // Track if we're generating login steps
        boolean hasGeneratedLoginSteps = false;
        
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type)) continue;
            
            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);
            
            // Generate camelCase method name for step definition
            String stepMethodName = action.methodName.substring(0, 1).toLowerCase() + action.methodName.substring(1);

            // DEDUPLICATION: Skip duplicate step methods
            if (generatedStepMethods.contains(stepMethodName)) {
                System.out.println("[SKIP DUPLICATE] Step definition method already exists: " + stepMethodName + "()");
                continue;
            }

            // If login reuse is enabled and this is a login action, skip generating step and add comment
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // 🔄 LOGIN STEPS - REUSE EXISTING METHODS\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // TODO: The following login steps were detected in your recording.\n");
                sb.append("    // Instead of using the generated page object methods, use existing login methods:\n");
                sb.append("    //\n");
                sb.append("    // RECOMMENDED APPROACH:\n");
                sb.append("    //   @When(\"User enters valid username from configuration\")\n");
                sb.append("    //   public void enterValidUsername() {\n");
                sb.append("    //       ").append(existingLoginClass).append(".enterValidUsernameFromConfiguration(page);\n");
                sb.append("    //   }\n");
                sb.append("    //\n");
                sb.append("    //   @And(\"User enters valid password from configuration\")\n");
                sb.append("    //   public void enterValidPassword() {\n");
                sb.append("    //       ").append(existingLoginClass).append(".enterValidPasswordFromConfiguration(page);\n");
                sb.append("    //   }\n");
                sb.append("    //\n");
                sb.append("    //   @And(\"User clicks on Sign In button\")\n");
                sb.append("    //   public void clickSignIn() {\n");
                sb.append("    //       ").append(existingLoginClass).append(".clickSignIn(page);\n");
                sb.append("    //   }\n");
                sb.append("    //\n");
                sb.append("    // Update your feature file to use:\n");
                sb.append("    //   When User enters valid username from configuration\n");
                sb.append("    //   And User enters valid password from configuration\n");
                sb.append("    //   And User clicks on Sign In button\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("\n");
                hasGeneratedLoginSteps = true;
            }
            
            // Mark step method as generated
            generatedStepMethods.add(stepMethodName);

            switch (action.type) {
                case "click":
                    String clickStepText = action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate step annotations
                    if (generatedSteps.contains(clickStepText)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists: " + clickStepText);
                        continue;
                    }
                    generatedSteps.add(clickStepText);
                    
                    sb.append("    @When(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "fill":
                    String fillStepText = "user enters {string} into " + action.readableName.toLowerCase();
                    
                    // DEDUPLICATION: Skip duplicate step annotations
                    if (generatedSteps.contains(fillStepText)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists: " + fillStepText);
                        continue;
                    }
                    generatedSteps.add(fillStepText);
                    
                    sb.append("    @And(\"").append(fillStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String text) {\n");
                    sb.append("        System.out.println(\"📍 Step: Entering text into ").append(action.readableName).append(": '\" + text + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page, text);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "select":
                    String selectStepText = "user selects {string} from " + action.readableName.toLowerCase();
                    
                    // DEDUPLICATION: Skip duplicate step annotations
                    if (generatedSteps.contains(selectStepText)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists: " + selectStepText);
                        continue;
                    }
                    generatedSteps.add(selectStepText);
                    
                    sb.append("    @And(\"").append(selectStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String option) {\n");
                    sb.append("        System.out.println(\"📍 Step: Selecting option from ").append(action.readableName).append(": '\" + option + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page, option);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "check":
                    String checkStepText = action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate step annotations
                    if (generatedSteps.contains(checkStepText)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists: " + checkStepText);
                        continue;
                    }
                    generatedSteps.add(checkStepText);
                    
                    sb.append("    @And(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "press":
                    String pressStepText = action.stepText;
                    
                    // DEDUPLICATION: Skip duplicate step annotations
                    if (generatedSteps.contains(pressStepText)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists: " + pressStepText);
                        continue;
                    }
                    generatedSteps.add(pressStepText);
                    
                    sb.append("    @And(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
            }
        }
        
        sb.append("    @Then(\"page should be updated\")\n");
        sb.append("    public void verifyPageUpdated() {\n");
        sb.append("        System.out.println(\"📍 Step: Verifying page is updated\");\n");
        sb.append("        // TODO: Add verification logic\n");
        sb.append("    }\n");
        sb.append("}\n");
        
        Files.write(Paths.get("src/test/java/stepDefs/" + className + "Steps.java"), sb.toString().getBytes());
    }
    
    /**
     * Escape special characters for Java strings.
     */
    private static String escapeJavaString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // ========================================================================
    // CODE REUSE DETECTION HELPERS
    // ========================================================================
    
    /**
     * Check if an action is login-related.
     */
    private static boolean isLoginRelatedAction(RecordedAction action) {
        if (action == null) return false;
        
        String selector = action.selector != null ? action.selector.toLowerCase() : "";
        String readableName = action.readableName != null ? action.readableName.toLowerCase() : "";
        String value = action.value != null ? action.value.toLowerCase() : "";
        
        return selector.contains("username") || selector.contains("password") || 
               selector.contains("login") || selector.contains("signin") || selector.contains("sign-in") ||
               readableName.contains("username") || readableName.contains("password") ||
               readableName.contains("login") || readableName.contains("signin") ||
               value.contains("username") || value.contains("password");
    }

    /**
     * Check if a page object already exists.
     * @param className The page object class name
     * @return true if the page object exists
     */
    private static boolean pageObjectExists(String className) {
        try {
            Path pageObjectPath = Paths.get("src/main/java/pages/" + className + ".java");
            return Files.exists(pageObjectPath);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Detect if existing page objects have login methods.
     * Returns the class name that contains login functionality.
     * @return Class name with login methods, or null if not found
     */
    private static String detectExistingLogin() {
        try {
            Path pagesDir = Paths.get("src/main/java/pages");
            if (!Files.exists(pagesDir)) return null;
            
            // Check for login.java first (common naming convention)
            Path loginPage = pagesDir.resolve("login.java");
            if (Files.exists(loginPage)) {
                String content = new String(Files.readAllBytes(loginPage));
                // Verify it has login methods
                if (content.contains("enterValidUsername") || 
                    content.contains("enterUsername") ||
                    content.contains("login")) {
                    System.out.println("[REUSE] Found existing login.java with login methods");
                    return "login";
                }
            }
            
            // Search all page objects for login patterns
            Files.list(pagesDir)
                 .filter(path -> path.toString().endsWith(".java"))
                 .forEach(path -> {
                     try {
                         String content = new String(Files.readAllBytes(path));
                         if (content.toLowerCase().contains("login") && 
                             (content.contains("enterUsername") || content.contains("enterPassword"))) {
                             String className = path.getFileName().toString().replace(".java", "");
                             System.out.println("[REUSE] Found login methods in: " + className);
                         }
                     } catch (IOException e) {
                         // Ignore
                     }
                 });
                 
            return "login"; // Default to login class name
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Detect if recorded actions contain login patterns.
     * @param actions List of recorded actions
     * @return true if login pattern detected
     */
    private static boolean containsLoginPattern(List<RecordedAction> actions) {
        for (RecordedAction action : actions) {
            String selector = action.selector != null ? action.selector.toLowerCase() : "";
            String value = action.value != null ? action.value.toLowerCase() : "";
            
            if (selector.contains("username") || selector.contains("password") ||
                selector.contains("login") || selector.contains("signin") ||
                value.contains("username") || value.contains("password")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if configurations.properties has test credentials.
     * @return true if credentials are configured
     */
    private static boolean hasConfiguredCredentials() {
        try {
            Path configPath = Paths.get("src/test/resources/configurations.properties");
            if (!Files.exists(configPath)) return false;
            
            String content = new String(Files.readAllBytes(configPath));
            return content.contains("username") || content.contains("Username") ||
                   content.contains("password") || content.contains("Password");
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // JIRA-BASED GENERATION (original TestGeneratorHelper functionality)
    // ========================================================================

    /**
     * Test requirement data structure for programmatic test generation.
     */
    public static class TestRequirement {
        public String testName;
        public String description;
        public List<PageElement> elements;
        public List<Scenario> scenarios;
        public VerificationOptions verification;
        public String jiraKey;

        public TestRequirement(String testName, String description) {
            this.testName = testName;
            this.description = description;
            this.elements = new ArrayList<>();
            this.scenarios = new ArrayList<>();
            this.verification = new VerificationOptions();
        }

        public TestRequirement addElement(String name, String action, String description) {
            this.elements.add(new PageElement(name, action, description));
            return this;
        }

        public TestRequirement addScenario(String name, String... steps) {
            this.scenarios.add(new Scenario(name, steps));
            return this;
        }

        public TestRequirement withVerification(boolean functional, boolean ui, boolean performance) {
            this.verification.functional = functional;
            this.verification.ui = ui;
            this.verification.performance = performance;
            return this;
        }

        public TestRequirement fromJira(String jiraKey) {
            this.jiraKey = jiraKey;
            return this;
        }
    }

    /**
     * Page element definition for test generation.
     */
    public static class PageElement {
        public String name;
        public String action;
        public String description;

        public PageElement(String name, String action, String description) {
            this.name = name;
            this.action = action;
            this.description = description;
        }
    }

    /**
     * Test scenario definition.
     */
    public static class Scenario {
        public String name;
        public List<String> steps;

        public Scenario(String name, String... steps) {
            this.name = name;
            this.steps = new ArrayList<>();
            for (String step : steps) {
                this.steps.add(step);
            }
        }
    }

    /**
     * Verification options for test generation.
     */
    public static class VerificationOptions {
        public boolean functional = false;
        public boolean ui = false;
        public boolean performance = false;
        public boolean logging = false;
        public int performanceThreshold = 3000;
    }

    /**
     * Generates test requirements from a JIRA story.
     * Auto-detects UI elements, generates comprehensive scenarios,
     * and suggests verification options based on story analysis.
     * 
     * @param issueKey JIRA issue key (e.g., "ECS-123")
     * @return TestRequirement with story details, or null if failed
     */
    public static TestRequirement generateFromJiraStory(String issueKey) {
        return generateFromJiraStory(issueKey, true);
    }

    /**
     * Generates test requirements from a JIRA story.
     * 
     * @param issueKey JIRA issue key (e.g., "ECS-123")
     * @param autoDetect If true, automatically detects elements and scenarios
     * @return TestRequirement with story details, or null if failed
     */
    public static TestRequirement generateFromJiraStory(String issueKey, boolean autoDetect) {
        System.out.println("🔍 Fetching JIRA story: " + issueKey);
        
        JiraStory story = jiraClient.getJiraStory(issueKey);
        
        if (story == null) {
            System.err.println("❌ Failed to fetch JIRA story");
            return null;
        }

        story.printDetails();

        // Convert story to test requirement
        String testName = sanitizeTestName(story.summary);
        String description = story.summary + "\n\nJIRA Story: " + story.key +
                "\nGenerated from: " + story.issueType +
                "\nPriority: " + story.priority;

        TestRequirement requirement = new TestRequirement(testName, description);
        requirement.fromJira(story.key);

        if (autoDetect) {
            // Auto-detect UI elements
            System.out.println("🤖 AI-Analyzing story for UI elements and test aspects...");
            String storyText = story.summary + "\n" + story.description + "\n" +
                    String.join("\n", story.acceptanceCriteria);
            
            List<PageElement> detectedElements = detectUIElements(storyText, story.issueType);
            requirement.elements.addAll(detectedElements);
            
            System.out.println("✅ Auto-detected " + detectedElements.size() + " UI elements:");
            for (PageElement el : detectedElements) {
                System.out.println("  - " + el.name + " (" + el.action + ")");
            }
            
            // Auto-suggest verification options
            suggestVerificationOptions(requirement, story.issueType, story.priority, storyText);
            System.out.println("✅ Suggested verification:");
            System.out.println("  - Functional: " + (requirement.verification.functional ? "✓" : "✗"));
            System.out.println("  - UI: " + (requirement.verification.ui ? "✓" : "✗"));
            System.out.println("  - Performance: " + (requirement.verification.performance ? "✓ (<" + 
                (requirement.verification.performanceThreshold/1000) + "s)" : "✗"));
            System.out.println("  - Logging: " + (requirement.verification.logging ? "✓" : "✗"));
        }

        // Convert acceptance criteria to comprehensive scenarios
        if (!story.acceptanceCriteria.isEmpty()) {
            System.out.println("📖 Converting " + story.acceptanceCriteria.size() + 
                " acceptance criteria to comprehensive scenarios...");
            
            for (int i = 0; i < story.acceptanceCriteria.size(); i++) {
                String criterion = story.acceptanceCriteria.get(i);
                String scenarioName = "Verify " + (criterion.length() > 50 ? 
                    criterion.substring(0, 50) + "..." : criterion);
                
                // Generate detailed steps
                String[] steps = generateDetailedSteps(criterion, requirement.elements, story.issueType);
                requirement.addScenario(scenarioName, steps);
            }
            
            // Add edge case scenarios
            if (autoDetect) {
                List<Scenario> edgeCases = generateEdgeCaseScenarios(story.issueType, 
                    story.summary, requirement.elements);
                requirement.scenarios.addAll(edgeCases);
                
                System.out.println("✅ Generated " + requirement.scenarios.size() + " total scenarios:");
                System.out.println("  - " + story.acceptanceCriteria.size() + " from acceptance criteria");
                System.out.println("  - " + edgeCases.size() + " edge case scenarios");
            } else {
                System.out.println("✅ Generated " + requirement.scenarios.size() + " scenarios");
            }
        } else {
            System.out.println("⚠️ No acceptance criteria found. Generating default scenarios...");
            List<Scenario> defaultScenarios = generateDefaultScenarios(story.issueType, 
                story.summary, requirement.elements);
            requirement.scenarios.addAll(defaultScenarios);
            System.out.println("✅ Generated " + defaultScenarios.size() + " default scenarios");
        }

        return requirement;
    }

    /**
     * Detect UI elements from story text using keyword analysis.
     */
    private static List<PageElement> detectUIElements(String storyText, String issueType) {
        List<PageElement> elements = new ArrayList<>();
        String textLower = storyText.toLowerCase();
        
        // Define element patterns
        Map<String, PageElement> patterns = new HashMap<>();
        
        // Input fields
        if (textLower.matches(".*(username|user name|userid|user id).*")) {
            patterns.put("username", new PageElement("Username Field", "type", "Enter username"));
        }
        if (textLower.matches(".*(password|pwd).*")) {
            patterns.put("password", new PageElement("Password Field", "type", "Enter password"));
        }
        if (textLower.matches(".*(email|e-mail).*")) {
            patterns.put("email", new PageElement("Email Field", "type", "Enter email address"));
        }
        if (textLower.matches(".*(first name|firstname).*")) {
            patterns.put("firstname", new PageElement("First Name Field", "type", "Enter first name"));
        }
        if (textLower.matches(".*(last name|lastname).*")) {
            patterns.put("lastname", new PageElement("Last Name Field", "type", "Enter last name"));
        }
        if (textLower.matches(".*(phone|telephone|mobile).*")) {
            patterns.put("phone", new PageElement("Phone Field", "type", "Enter phone number"));
        }
        if (textLower.matches(".*(address).*")) {
            patterns.put("address", new PageElement("Address Field", "type", "Enter address"));
        }
        if (textLower.matches(".*(search).*")) {
            patterns.put("search", new PageElement("Search Field", "type", "Enter search query"));
        }
        
        // Buttons
        if (textLower.matches(".*(login button|sign in|signin).*")) {
            patterns.put("login", new PageElement("Login Button", "click", "Click login button"));
        }
        if (textLower.matches(".*(submit button|submit form).*")) {
            patterns.put("submit", new PageElement("Submit Button", "click", "Submit form"));
        }
        if (textLower.matches(".*(save button|save).*")) {
            patterns.put("save", new PageElement("Save Button", "click", "Save changes"));
        }
        if (textLower.matches(".*(cancel button|cancel).*")) {
            patterns.put("cancel", new PageElement("Cancel Button", "click", "Cancel action"));
        }
        if (textLower.matches(".*(delete button|remove).*")) {
            patterns.put("delete", new PageElement("Delete Button", "click", "Delete item"));
        }
        if (textLower.matches(".*(register button|signup|sign up).*")) {
            patterns.put("register", new PageElement("Register Button", "click", "Register account"));
        }
        
        // Checkboxes
        if (textLower.matches(".*(checkbox|check box|remember me).*")) {
            patterns.put("checkbox", new PageElement("Checkbox", "click", "Toggle checkbox"));
        }
        
        // Dropdowns
        if (textLower.matches(".*(dropdown|drop down|select|combo).*")) {
            patterns.put("dropdown", new PageElement("Dropdown", "select", "Select from dropdown"));
        }
        
        elements.addAll(patterns.values());
        
        // Add default elements if nothing detected
        if (elements.isEmpty()) {
            if (issueType.matches("(?i).*(story|feature).*")) {
                elements.add(new PageElement("Main Action Button", "click", "Primary action button"));
                elements.add(new PageElement("Input Field", "type", "Primary input field"));
            } else if (issueType.matches("(?i).*(bug|defect).*")) {
                elements.add(new PageElement("Affected Element", "click", "Element with bug"));
            }
        }
        
        return elements;
    }

    /**
     * Generate detailed test steps from acceptance criterion.
     */
    private static String[] generateDetailedSteps(String criterion, List<PageElement> elements, 
                                                   String issueType) {
        List<String> steps = new ArrayList<>();
        String criterionLower = criterion.toLowerCase();
        
        // Check if criterion already has Given/When/Then structure
        if (criterionLower.matches(".*(given|when|then).*")) {
            String[] lines = criterion.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.matches("(?i)^(given|when|then|and).*")) {
                    steps.add(trimmed);
                }
            }
        }
        
        // Generate comprehensive steps if none extracted
        if (steps.isEmpty()) {
            steps.add("Given user is on the application page");
            steps.add("And the page is fully loaded");
            
            // Add element interactions
            if (!elements.isEmpty()) {
                for (PageElement element : elements) {
                    if (element.action.equals("type")) {
                        steps.add("When user enters valid data in " + element.name);
                    } else if (element.action.equals("click")) {
                        steps.add("And user clicks on " + element.name);
                    } else if (element.action.equals("select")) {
                        steps.add("And user selects value from " + element.name);
                    }
                }
            } else {
                steps.add("When user performs actions for \"" + criterion + "\"");
            }
            
            // Add verification
            String expectedResult = criterion.contains("should") ? 
                criterion.substring(criterion.toLowerCase().indexOf("should") + 6).trim() : 
                "complete successfully";
            steps.add("Then the system should " + expectedResult);
            steps.add("And no errors should be displayed");
        }
        
        return steps.toArray(new String[0]);
    }

    /**
     * Generate edge case scenarios based on story type.
     */
    private static List<Scenario> generateEdgeCaseScenarios(String issueType, String summary, 
                                                             List<PageElement> elements) {
        List<Scenario> scenarios = new ArrayList<>();
        
        // Negative test for input fields
        boolean hasInputFields = elements.stream().anyMatch(el -> el.action.equals("type"));
        
        if (hasInputFields) {
            scenarios.add(new Scenario("Verify validation with empty fields",
                "Given user is on the application page",
                "When user leaves required fields empty",
                "And user attempts to proceed",
                "Then appropriate validation messages should be displayed",
                "And the action should not proceed"
            ));
            
            scenarios.add(new Scenario("Verify validation with invalid data",
                "Given user is on the application page",
                "When user enters invalid data in fields",
                "And user attempts to proceed",
                "Then validation errors should be displayed",
                "And invalid fields should be highlighted"
            ));
        }
        
        // UI responsiveness scenario
        if (issueType.matches("(?i).*(story|feature).*")) {
            scenarios.add(new Scenario("Verify UI responsiveness",
                "Given user is on the application page",
                "When the page loads",
                "Then all elements should be visible",
                "And the layout should be proper",
                "And no visual glitches should occur"
            ));
        }
        
        // Error handling scenario
        scenarios.add(new Scenario("Verify error handling",
            "Given user is on the application page",
            "When an error condition occurs",
            "Then appropriate error message should be displayed",
            "And user should be able to recover",
            "And application should remain stable"
        ));
        
        return scenarios;
    }

    /**
     * Generate default scenarios when no acceptance criteria exist.
     */
    private static List<Scenario> generateDefaultScenarios(String issueType, String summary, 
                                                            List<PageElement> elements) {
        List<Scenario> scenarios = new ArrayList<>();
        
        // Happy path
        scenarios.add(new Scenario("Verify " + summary + " - Happy Path",
            "Given user is on the application page",
            "And all prerequisites are met",
            "When user performs the main action",
            "Then the action should complete successfully",
            "And expected result should be displayed"
        ));
        
        // Element-specific scenario
        if (!elements.isEmpty()) {
            List<String> steps = new ArrayList<>();
            steps.add("Given user is on the application page");
            for (PageElement el : elements) {
                steps.add("When user interacts with " + el.name);
            }
            steps.add("Then all elements should respond correctly");
            steps.add("And no errors should occur");
            
            scenarios.add(new Scenario("Verify all UI elements are functional", 
                steps.toArray(new String[0])));
        }
        
        return scenarios;
    }

    /**
     * Suggest verification options based on story type and priority.
     */
    private static void suggestVerificationOptions(TestRequirement requirement, String issueType, 
                                                    String priority, String storyText) {
        String textLower = storyText.toLowerCase();
        
        // Always enable functional
        requirement.verification.functional = true;
        
        // UI verification for UI-heavy stories
        if (textLower.matches(".*(ui|user interface|layout|design|button|field|form).*") ||
            issueType.matches("(?i).*(feature|story).*")) {
            requirement.verification.ui = true;
        }
        
        // Performance for high priority or performance-related
        if (priority.matches("(?i).*(high|critical|blocker).*") ||
            textLower.matches(".*(performance|speed|fast|slow|timeout|load time).*")) {
            requirement.verification.performance = true;
            requirement.verification.performanceThreshold = 
                priority.matches("(?i).*(critical|blocker).*") ? 2000 : 3000;
        }
        
        // Logging for bugs and complex features
        if (issueType.matches("(?i).*(bug|defect).*") || 
            priority.matches("(?i).*(high|critical|blocker).*")) {
            requirement.verification.logging = true;
        }
    }

    /**
     * Sanitizes a story summary to create a valid test class name.
     * 
     * @param summary Story summary text
     * @return Sanitized class name
     */
    private static String sanitizeTestName(String summary) {
        return Arrays.stream(summary.replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .split("\\s+"))
                .map(word -> word.isEmpty() ? "" : 
                    Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(""));
    }

    /**
     * Validates test file structure for a given test.
     * Checks if Page Object, Feature, and Step Definition files exist.
     * 
     * @param testName Name of the test
     * @return ValidationResult with details
     */
    public static ValidationResult validateTestStructure(String testName) {
        ValidationResult result = new ValidationResult(testName);
        
        String projectRoot = System.getProperty("user.dir");
        
        // Check Page Object
        Path pageFile = Paths.get(projectRoot, "src", "main", "java", "pages", testName + ".java");
        result.pageObjectExists = Files.exists(pageFile);
        result.pageObjectPath = pageFile.toString();
        
        // Check Feature file
        Path featureFile = Paths.get(projectRoot, "src", "test", "java", "features", 
                testName.toLowerCase() + ".feature");
        result.featureExists = Files.exists(featureFile);
        result.featurePath = featureFile.toString();
        
        // Check Step Definitions
        Path stepDefFile = Paths.get(projectRoot, "src", "test", "java", "stepDefs", 
                testName + "Steps.java");
        result.stepDefExists = Files.exists(stepDefFile);
        result.stepDefPath = stepDefFile.toString();
        
        result.isValid = result.pageObjectExists && result.featureExists && result.stepDefExists;
        
        return result;
    }

    /**
     * Validation result for test structure.
     */
    public static class ValidationResult {
        public String testName;
        public boolean isValid;
        public boolean pageObjectExists;
        public String pageObjectPath;
        public boolean featureExists;
        public String featurePath;
        public boolean stepDefExists;
        public String stepDefPath;
        public List<String> missingFiles;

        public ValidationResult(String testName) {
            this.testName = testName;
            this.missingFiles = new ArrayList<>();
        }

        public void printReport() {
            System.out.println("\n📊 Test Structure Validation Report");
            System.out.println("═══════════════════════════════════");
            System.out.println("Test: " + testName);
            System.out.println("Status: " + (isValid ? "✅ VALID" : "❌ INCOMPLETE"));
            System.out.println();
            
            System.out.println("Files:");
            printFileStatus("Page Object", pageObjectExists, pageObjectPath);
            printFileStatus("Feature", featureExists, featurePath);
            printFileStatus("Step Definitions", stepDefExists, stepDefPath);
            
            if (!isValid) {
                System.out.println("\n⚠️ Missing Files:");
                if (!pageObjectExists) missingFiles.add(pageObjectPath);
                if (!featureExists) missingFiles.add(featurePath);
                if (!stepDefExists) missingFiles.add(stepDefPath);
                
                missingFiles.forEach(file -> System.out.println("  - " + file));
            }
            System.out.println();
        }

        private void printFileStatus(String type, boolean exists, String path) {
            String status = exists ? "✅" : "❌";
            System.out.println(String.format("  %s %-20s %s", status, type + ":", 
                    path.substring(path.lastIndexOf("src"))));
        }
    }

    /**
     * Analyzes framework configuration and returns key settings.
     * 
     * @return FrameworkInfo with configuration details
     */
    public static FrameworkInfo analyzeFramework() {
        FrameworkInfo info = new FrameworkInfo();
        
        info.baseUrl = loadProps.getProperty("URL");
        info.browser = loadProps.getProperty("Browser");
        info.headless = Boolean.parseBoolean(loadProps.getProperty("Headless"));
        info.recordingEnabled = Boolean.parseBoolean(loadProps.getProperty("Record"));
        info.screenshotEnabled = Boolean.parseBoolean(loadProps.getProperty("TakeScreenShots"));
        info.defaultTimeout = Integer.parseInt(loadProps.getProperty("Timeout"));
        info.retryCount = Integer.parseInt(loadProps.getProperty("RetryCount"));
        
        // JIRA configuration
        info.jiraEnabled = loadProps.getJIRAConfig("JIRA_BASE_URL") != null;
        info.jiraBaseUrl = loadProps.getJIRAConfig("JIRA_BASE_URL");
        info.jiraProjectKey = loadProps.getJIRAConfig("PROJECT_KEY");
        
        return info;
    }

    /**
     * Framework configuration information.
     */
    public static class FrameworkInfo {
        public String baseUrl;
        public String browser;
        public boolean headless;
        public boolean recordingEnabled;
        public boolean screenshotEnabled;
        public int defaultTimeout;
        public int retryCount;
        public boolean jiraEnabled;
        public String jiraBaseUrl;
        public String jiraProjectKey;

        public void printSummary() {
            System.out.println("\n⚙️ Framework Configuration");
            System.out.println("═══════════════════════════");
            System.out.println("Base URL: " + baseUrl);
            System.out.println("Browser: " + browser);
            System.out.println("Headless: " + headless);
            System.out.println("Recording: " + (recordingEnabled ? "✅ Enabled" : "❌ Disabled"));
            System.out.println("Screenshots: " + (screenshotEnabled ? "✅ Enabled" : "❌ Disabled"));
            System.out.println("Timeout: " + defaultTimeout + "ms");
            System.out.println("Retry Count: " + retryCount);
            
            if (jiraEnabled) {
                System.out.println("\n🎫 JIRA Integration");
                System.out.println("JIRA URL: " + jiraBaseUrl);
                System.out.println("Project: " + jiraProjectKey);
            }
            System.out.println();
        }
    }

    /**
     * Compiles the Maven project and returns success status.
     * 
     * @return true if compilation succeeded, false otherwise
     */
    public static boolean compileProject() {
        System.out.println("🔨 Compiling project...");
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(isWindows() ? "mvn.cmd" : "mvn", "clean", "compile");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✅ Compilation successful");
                return true;
            } else {
                System.err.println("❌ Compilation failed with exit code: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error during compilation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Runs tests for a specific feature.
     * 
     * @param featureName Name of the feature to test
     * @return true if tests passed, false otherwise
     */
    public static boolean runTests(String featureName) {
        System.out.println("🧪 Running tests for: " + featureName);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(isWindows() ? "mvn.cmd" : "mvn", "test", 
                    "-Dcucumber.filter.tags=@" + featureName);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✅ Tests passed");
                return true;
            } else {
                System.err.println("❌ Tests failed");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error running tests: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the latest HTML report path.
     * 
     * @return Path to the latest HTML report, or null if not found
     */
    public static String getLatestReport() {
        String reportDir = System.getProperty("user.dir") + "/MRITestExecutionReports/";
        
        try {
            Path latestVersionDir = Files.list(Paths.get(reportDir))
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("Version"))
                    .max((p1, p2) -> p1.getFileName().toString()
                            .compareTo(p2.getFileName().toString()))
                    .orElse(null);
            
            if (latestVersionDir != null) {
                Path htmlReportDir = latestVersionDir.resolve("extentReports/testNGExtentReports/html");
                
                if (Files.exists(htmlReportDir)) {
                    Path latestHtml = Files.list(htmlReportDir)
                            .filter(p -> p.toString().endsWith(".html"))
                            .max((p1, p2) -> p1.getFileName().toString()
                                    .compareTo(p2.getFileName().toString()))
                            .orElse(null);
                    
                    if (latestHtml != null) {
                        return latestHtml.toAbsolutePath().toString();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error finding report: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Opens the latest test report in the default browser.
     */
    public static void openLatestReport() {
        String reportPath = getLatestReport();
        
        if (reportPath != null) {
            System.out.println("📊 Opening report: " + reportPath);
            
            try {
                if (isWindows()) {
                    new ProcessBuilder("cmd", "/c", "start", reportPath).start();
                } else if (isMac()) {
                    new ProcessBuilder("open", reportPath).start();
                } else {
                    new ProcessBuilder("xdg-open", reportPath).start();
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to open report: " + e.getMessage());
                System.out.println("📁 Report location: " + reportPath);
            }
        } else {
            System.err.println("❌ No report found");
        }
    }

    /**
     * Checks if running on Windows.
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Checks if running on Mac.
     */
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Command-line interface for test generation.
     * Supports two modes:
     * 1. Recording mode (4 args): recordingFile featureName pageUrl jiraStory
     * 2. JIRA mode (1 arg): jiraStoryId
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            // Recording mode: Generate from Playwright recording
            String recordingFile = args[0];
            String featureName = args[1];
            String pageUrl = args[2];
            String jiraStory = args[3];
            
            boolean success = generateFromRecording(recordingFile, featureName, pageUrl, jiraStory);
            System.exit(success ? 0 : 1);
            
        } else if (args.length == 1) {
            // JIRA mode: Generate from JIRA story
            String jiraStoryId = args[0];
            
            System.out.println("🤖 Test Generator Helper - JIRA Mode");
            System.out.println("═══════════════════════════════════════\n");
            
            TestRequirement req = generateFromJiraStory(jiraStoryId);
            if (req != null) {
                System.out.println("\n✅ Test requirement generated successfully!");
                System.out.println("   Test Name: " + req.testName);
                System.out.println("   Elements: " + req.elements.size());
                System.out.println("   Scenarios: " + req.scenarios.size());
                System.exit(0);
            } else {
                System.err.println("\n❌ Failed to generate test requirement from JIRA");
                System.exit(1);
            }
            
        } else if (args.length == 0) {
            // Interactive example mode
            System.out.println("🤖 Test Generator Helper - Example Usage\n");

            System.out.println("═══ Example 1: Analyze Framework ═══");
            FrameworkInfo info = analyzeFramework();
            info.printSummary();

            System.out.println("═══ Example 2: Validate Test Structure ═══");
            ValidationResult validation = validateTestStructure("login");
            validation.printReport();

            System.out.println("═══ Example 3: Open Latest Report ═══");
            openLatestReport();
            
        } else {
            // Show usage
            System.err.println("❌ Invalid arguments!");
            System.err.println("\nUsage:");
            System.err.println("  Recording Mode:");
            System.err.println("    java TestGeneratorHelper <recordingFile> <featureName> <pageUrl> <jiraStory>");
            System.err.println();
            System.err.println("  JIRA Mode:");
            System.err.println("    java TestGeneratorHelper <jiraStoryId>");
            System.err.println();
            System.err.println("  Interactive Mode:");
            System.err.println("    java TestGeneratorHelper");
            System.exit(1);
        }
    }
}
