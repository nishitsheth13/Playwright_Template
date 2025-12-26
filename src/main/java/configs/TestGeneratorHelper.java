package configs;

import configs.jira.jiraClient;
import configs.jira.jiraClient.JiraStory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
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
        
        try {
            // Parse recording file
            List<RecordedAction> actions = parseRecording(recordingFile);
            System.out.println("[INFO] Extracted " + actions.size() + " actions from recording");
            
            // Generate class name (capitalize first letter)
            String className = featureName.substring(0, 1).toUpperCase() + featureName.substring(1);
            
            // Generate files
            generatePageObject(className, pageUrl, jiraStory, actions);
            generateFeatureFile(className, jiraStory, actions);
            generateStepDefinitions(className, jiraStory, actions);
            
            System.out.println("[SUCCESS] All files generated successfully!");
            System.out.println("[INFO] Page Object: src/main/java/pages/" + className + ".java");
            System.out.println("[INFO] Feature File: src/test/java/features/" + className + ".feature");
            System.out.println("[INFO] Step Definitions: src/test/java/stepDefs/" + className + "Steps.java");
            
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate test files: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Parse Playwright recording file and extract actions.
     * Supports both old API (page.click) and modern API (page.locator().click())
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
                System.out.println("[DEBUG] Found locator click: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "click", matcher.group(1), null));
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
                System.out.println("[DEBUG] Found locator fill: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "fill", matcher.group(1), matcher.group(2)));
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
                System.out.println("[DEBUG] Found locator select: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "select", matcher.group(1), matcher.group(2)));
                continue;
            }
            
            // MODERN API - locator().check()
            matcher = locatorCheckPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found locator check: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "check", matcher.group(1), null));
                continue;
            }
            
            // MODERN API - locator().press()
            matcher = locatorPressPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found locator press: " + matcher.group(1) + " - " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "press", matcher.group(1), matcher.group(2)));
                continue;
            }
            
            // OLD API fallbacks
            matcher = clickPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found old API click: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "click", matcher.group(1), null));
                continue;
            }
            
            matcher = fillPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found old API fill: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "fill", matcher.group(1), matcher.group(2)));
                continue;
            }
            
            matcher = selectPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found old API select: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "select", matcher.group(1), matcher.group(2)));
                continue;
            }
            
            matcher = checkPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found old API check: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "check", matcher.group(1), null));
                continue;
            }
            
            matcher = pressPattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found old API press: " + matcher.group(1) + " - " + matcher.group(2));
                actions.add(new RecordedAction(actionId++, "press", matcher.group(1), matcher.group(2)));
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
     */
    private static void generatePageObject(String className, String pageUrl, String jiraStory, 
                                           List<RecordedAction> actions) throws IOException {
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
        for (RecordedAction action : actions) {
            if (action.selector != null) {
                sb.append("    // ").append(action.readableName).append("\n");
                sb.append("    private static final String ").append(action.elementName)
                  .append(" = \"").append(escapeJavaString(action.selector)).append("\";\n");
                sb.append("\n");
            }
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
     */
    private static void generateFeatureFile(String className, String jiraStory, 
                                           List<RecordedAction> actions) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append("@").append(jiraStory).append(" @").append(className).append("\n");
        sb.append("Feature: ").append(className).append(" Test\n");
        sb.append("  Auto-generated from Playwright recording\n");
        sb.append("\n");
        sb.append("  Scenario: Complete ").append(className).append(" workflow\n");
        sb.append("    Given user navigates to ").append(className).append(" page\n");
        
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type)) continue;
            
            switch (action.type) {
                case "click":
                    sb.append("    When ").append(action.stepText).append("\n");
                    break;
                case "fill":
                    sb.append("    And user enters \"{string}\" into ").append(action.readableName.toLowerCase()).append("\n");
                    break;
                case "select":
                    sb.append("    And user selects \"{string}\" from ").append(action.readableName.toLowerCase()).append("\n");
                    break;
                case "check":
                    sb.append("    And ").append(action.stepText).append("\n");
                    break;
                case "press":
                    sb.append("    And ").append(action.stepText).append("\n");
                    break;
            }
        }
        
        sb.append("    Then page should be updated\n");
        
        Files.write(Paths.get("src/test/java/features/" + className + ".feature"), sb.toString().getBytes());
    }
    
    /**
     * Generate Step Definitions from recorded actions.
     */
    private static void generateStepDefinitions(String className, String jiraStory, 
                                               List<RecordedAction> actions) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package stepDefs;\n");
        sb.append("import configs.browserSelector;\n");
        sb.append("import io.cucumber.java.en.*;\n");
        sb.append("import pages.").append(className).append(";\n");
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Step Definitions for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording by Pure Java Generator\n");
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
        
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type)) continue;
            
            // Generate camelCase method name for step definition
            String stepMethodName = action.methodName.substring(0, 1).toLowerCase() + action.methodName.substring(1);

            switch (action.type) {
                case "click":
                    sb.append("    @When(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "fill":
                    String fillStepText = "user enters {string} into " + action.readableName.toLowerCase();
                    sb.append("    @And(\"").append(fillStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String text) {\n");
                    sb.append("        System.out.println(\"📍 Step: Entering text into ").append(action.readableName).append(": '\" + text + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page, text);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "select":
                    String selectStepText = "user selects {string} from " + action.readableName.toLowerCase();
                    sb.append("    @And(\"").append(selectStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String option) {\n");
                    sb.append("        System.out.println(\"📍 Step: Selecting option from ").append(action.readableName).append(": '\" + option + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page, option);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "check":
                    sb.append("    @And(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;
                    
                case "press":
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
