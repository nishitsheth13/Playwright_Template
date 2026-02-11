package configs;

import configs.jira.jiraClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified Test Generator Helper - Single class for all test generation needs.
 * 
 * Features:
 * 1. Parse Playwright recordings and generate test files (replaces
 * TestFileGenerator)
 * 2. JIRA story integration and AI-assisted generation
 * 3. Test requirement management and validation
 * 
 * This consolidates all test generation functionality into one helper class.
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * METHOD NAMING CONVENTIONS (CRITICAL - NEVER USE "Value" AS METHOD NAME!)
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * ALL generated methods MUST follow action-based naming:
 * ✅ CORRECT Examples:
 * - clickSignIn(Page page) // click action
 * - enterUsername(Page page, String text) // fill/enter action
 * - selectCountry(Page page, String option) // select action
 * - checkRememberMe(Page page) // check/checkbox action
 * - pressKeyOnSearch(Page page) // press/keyboard action
 * - navigateToLogin(Page page) // navigate action
 * 
 * ❌ WRONG Examples:
 * - Value(Page page) // Generic, meaningless
 * - action1(Page page) // Not descriptive
 * - doSomething(Page page) // Vague
 * 
 * Method Name Generation Rules (generateMethodName method):
 * - click → click + ElementName
 * - fill → enter + ElementName (special: enterUsername, enterPassword,
 * enterEmail)
 * - select → select + ElementName
 * - check → check + ElementName (or toggle + ElementName)
 * - press → pressKeyOn + ElementName
 * - navigate→ navigateTo + PageName
 * - default → elementNameAction (camelCase + "Action")
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * LOCATOR STRATEGY & CODE REUSABILITY (Applied to ALL generation methods)
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * LOCATOR PRIORITY ORDER (optimizeSelector method):
 * 1. Static ID → //input[@id='Username'] (highest priority)
 * 2. Relative XPath → //div[@class='form']//input
 * 3. Absolute XPath → /html/body/div/input
 * 4. Label or Names → label=Username, @name='submit'
 * 5. Class name → .btn-primary, @class='container'
 * 6. CSS Selectors → div > button (lowest priority)
 * 
 * DYNAMIC ID DETECTION (isDynamicId method):
 * - Detects GUIDs: [a-f0-9]{8}-[a-f0-9]{4}-...
 * - Detects Timestamps: \d{10,13}
 * - Detects Random Hashes: [a-zA-Z0-9]{16,}
 * - Warns and downgrades dynamic IDs to lower priority
 * 
 * CODE REUSABILITY (Code Reuse Detection Helpers):
 * - pageObjectExists() → Checks if page object already exists
 * - detectExistingLogin() → Finds existing login.java methods
 * - containsLoginPattern() → Detects login/auth patterns in actions
 * - hasConfiguredCredentials() → Checks configurations.properties for test data
 * - Automatically imports existing classes (e.g., login.java)
 * - Skips regeneration of existing page objects
 * - Provides tips for manual integration
 * 
 * USAGE:
 * All test generation methods (Recording, JIRA, Manual) use these features:
 * - Recording-based: generateFromRecording() → parseRecording() →
 * optimizeSelector()
 * - JIRA-based: generateFromJiraStory() → [uses same file generation methods]
 * - File generation: generatePageObject(), generateFeatureFile(),
 * generateStepDefinitions()
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class TestGeneratorHelper {

    // ========================================================================
    // ADVANCED AUTO-FIX SYSTEM - Error Prevention & Recovery
    // ========================================================================

    /**
     * Auto-fixes selector to ensure it's usable
     * Reserved for future advanced selector optimization
     *
     * @param selector Raw selector from recording
     * @return Fixed selector or null if unfixable
     */
    @SuppressWarnings("unused")
    private static String autoFixSelector(String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            System.out.println("[AUTO-FIX] Empty selector detected, skipping action");
            return null;
        }

        String original = selector;
        String fixed = selector.trim();

        // Fix common issues
        fixed = fixed.replace("\\\"", "\""); // Unescape quotes
        fixed = fixed.replaceAll("\\s+", " "); // Normalize whitespace

        // Validate selector format
        if (!isValidSelector(fixed)) {
            System.out.println("[AUTO-FIX] Invalid selector: " + original);
            // Try to fix common issues
            if (!fixed.contains("=") && !fixed.startsWith("xpath=") && !fixed.startsWith("css=")) {
                // Assume it's text
                fixed = "text=" + fixed;
                System.out.println("[AUTO-FIX] Added text= prefix: " + fixed);
            }
        }

        if (!original.equals(fixed)) {
            System.out.println("[AUTO-FIX] Selector: '" + original + "' → '" + fixed + "'");
        }

        return fixed;
    }

    /**
     * Validates and auto-fixes feature name to ensure valid Java identifier
     *
     * @param featureName Raw feature name input
     * @return Sanitized, valid Java class name
     */
    private static String autoFixFeatureName(String featureName) {
        if (featureName == null || featureName.trim().isEmpty()) {
            String fallback = "TestFeature" + System.currentTimeMillis();
            System.out.println("[AUTO-FIX] Empty feature name, using: " + fallback);
            return fallback;
        }

        String original = featureName;
        String fixed = featureName.trim();

        // Remove invalid characters
        fixed = fixed.replaceAll("[^a-zA-Z0-9_]", "");

        // Ensure starts with letter
        if (!fixed.isEmpty() && !Character.isLetter(fixed.charAt(0))) {
            fixed = "Test" + fixed;
        }

        // Convert to PascalCase
        fixed = toPascalCase(fixed);

        // Validate
        if (!isValidJavaIdentifier(fixed)) {
            fixed = "TestFeature" + System.currentTimeMillis();
            System.out.println("[AUTO-FIX] Invalid identifier after fix, using: " + fixed);
        }

        if (!original.equals(fixed)) {
            System.out.println("[AUTO-FIX] Feature name: '" + original + "' → '" + fixed + "'");
        }

        return fixed;
    }

    /**
     * Auto-fixes method name to ensure valid Java identifier and no conflicts
     * Reserved for future advanced method name generation
     *
     * @param methodName Generated method name
     * @param existingMethods Set of already generated methods
     * @param counter Counter for uniqueness
     * @return Fixed unique method name
     */
    @SuppressWarnings("unused")
    private static String autoFixMethodName(String methodName, Set<String> existingMethods, int counter) {
        if (methodName == null || methodName.trim().isEmpty()) {
            return "action" + counter;
        }

        String original = methodName;
        String fixed = methodName.trim();

        // Remove invalid characters
        fixed = fixed.replaceAll("[^a-zA-Z0-9_]", "");

        // Ensure starts with lowercase letter
        if (!fixed.isEmpty() && Character.isUpperCase(fixed.charAt(0))) {
            fixed = Character.toLowerCase(fixed.charAt(0)) + fixed.substring(1);
        }

        // Ensure not a Java keyword
        if (isJavaKeyword(fixed)) {
            fixed = fixed + "Action";
        }

        // Ensure uniqueness
        String uniqueName = fixed;
        int suffix = 2;
        while (existingMethods.contains(uniqueName)) {
            uniqueName = fixed + suffix;
            suffix++;
        }

        if (!original.equals(uniqueName)) {
            System.out.println("[AUTO-FIX] Method name: '" + original + "' → '" + uniqueName + "'");
        }

        return uniqueName;
    }

    /**
     * Validates selector format
     */
    private static boolean isValidSelector(String selector) {
        if (selector == null || selector.isEmpty()) return false;

        // Check for common selector patterns
        return selector.matches("^(xpath=|css=|text=|label=|placeholder=|id=|#|\\.).*") ||
                selector.contains("@") || // XPath attribute
                selector.contains("[") || // CSS attribute selector
                selector.matches("^[a-zA-Z][a-zA-Z0-9]*$"); // Simple tag name
    }

    /**
     * Auto-validates and fixes imports in generated code
     * Reserved for future advanced import management
     *
     * @param imports List of import statements
     * @return Fixed list with duplicates removed and sorted
     */
    @SuppressWarnings("unused")
    private static List<String> autoFixImports(List<String> imports) {
        Set<String> uniqueImports = new LinkedHashSet<>();

        for (String imp : imports) {
            if (imp != null && !imp.trim().isEmpty()) {
                String fixed = imp.trim();
                if (!fixed.endsWith(";")) {
                    fixed += ";";
                }
                uniqueImports.add(fixed);
            }
        }

        List<String> sorted = new ArrayList<>(uniqueImports);
        Collections.sort(sorted);

        System.out.println("[AUTO-FIX] Imports: " + imports.size() + " → " + sorted.size() + " (removed duplicates)");

        return sorted;
    }

    /**
     * Auto-fixes feature file step text for valid Gherkin syntax
     *
     * @param stepText Raw step text
     * @return Fixed Gherkin step
     */
    private static String autoFixFeatureStep(String stepText) {
        if (stepText == null || stepText.trim().isEmpty()) {
            return "And action is performed";
        }

        String original = stepText;
        String fixed = stepText.trim();

        // Ensure proper Gherkin keyword
        if (!fixed.matches("^(Given|When|Then|And|But)\\s+.*")) {
            fixed = "When " + fixed;
        }

        // Fix common issues
        fixed = fixed.replaceAll("\\s+", " "); // Normalize whitespace
        fixed = fixed.replaceAll("\"\"", "\""); // Fix double quotes

        // Ensure ends properly (no trailing spaces/punctuation)
        fixed = fixed.replaceAll("[\\s.!?]+$", "");

        if (!original.equals(fixed)) {
            System.out.println("[AUTO-FIX] Feature step: '" + original + "' → '" + fixed + "'");
        }

        return fixed;
    }

    /**
     * Checks if string is a Java keyword
     */
    private static boolean isJavaKeyword(String word) {
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
        };

        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * AUTO-FIX: Ensures all Page Object methods are public (not protected)
     * Prevents compilation errors when Step Definitions try to access methods
     *
     * @param content Page Object content
     * @return Fixed content with all methods public
     */
    private static String autoFixMethodVisibility(String content) {
        if (content == null || !content.contains("protected static void")) {
            return content;
        }

        System.out.println("[AUTO-FIX] Converting protected methods to public for accessibility");

        // Replace all protected static methods with public static
        String fixed = content.replaceAll("protected static void", "public static void");

        int count = content.split("protected static void").length - 1;
        if (count > 0) {
            System.out.println("[AUTO-FIX] Fixed " + count + " method(s) from protected → public");
        }

        return fixed;
    }

    /**
     * Validates recording file before processing
     *
     * @param recordingFile Path to recording file
     * @return true if valid, false otherwise
     */
    private static boolean validateRecordingFile(String recordingFile) {
        if (recordingFile == null || recordingFile.trim().isEmpty()) {
            System.err.println("[ERROR] Recording file path is empty");
            return false;
        }

        File file = new File(recordingFile);
        if (!file.exists()) {
            System.err.println("[ERROR] Recording file not found: " + recordingFile);
            return false;
        }

        if (!file.canRead()) {
            System.err.println("[ERROR] Recording file is not readable: " + recordingFile);
            return false;
        }

        if (file.length() == 0) {
            System.err.println("[ERROR] Recording file is empty: " + recordingFile);
            return false;
        }

        if (file.length() < 100) {
            System.err.println("[WARNING] Recording file is very small (" + file.length() + " bytes), may not contain valid actions");
        }

        return true;
    }

    /**
     * Auto-recovery: Attempts to fix common generation errors
     *
     * @param error The exception that occurred
     * @param context Context information about what was being generated
     * @return Suggested fix or null if no fix available
     */
    private static String autoRecoverFromError(Exception error, String context) {
        String errorMsg = error.getMessage();
        String suggestion = null;

        if (errorMsg.contains("FileNotFoundException")) {
            suggestion = "Create missing directory structure";
        } else if (errorMsg.contains("PermissionDenied") || errorMsg.contains("AccessDenied")) {
            suggestion = "Check file/directory permissions";
        } else if (errorMsg.contains("OutOfMemory")) {
            suggestion = "Reduce recording size or increase JVM heap";
        } else if (errorMsg.contains("IllegalArgumentException")) {
            suggestion = "Validate input parameters before generation";
        } else if (errorMsg.contains("NullPointerException")) {
            suggestion = "Check for null values in recording actions";
        }

        if (suggestion != null) {
            System.err.println("[AUTO-RECOVERY] Error in " + context + ": " + errorMsg);
            System.err.println("[AUTO-RECOVERY] Suggested fix: " + suggestion);
        }

        return suggestion;
    }

    /**
     * Validates generated file content before writing
     *
     * @param content File content to validate
     * @param fileType Type of file (PageObject, Feature, StepDef)
     * @return true if valid, false otherwise
     */

    /**
     * AUTO-FIX: Ensures navigateTo method exists with proper signature
     *
     * @param content   Page Object content
     * @param className Page class name
     * @return Fixed content with navigateTo method
     */
    private static String ensureNavigateToMethod(String content, String className) {
        // Check if navigateTo method already exists
        if (content.contains("public static void navigateTo") ||
                content.contains("public static void navigateTo" + className)) {
            return content;
        }

        System.out.println("[AUTO-FIX] Adding missing navigateTo method");

        // Find the position to insert (after constructor, before first method)
        String navigateMethod =
                "    /**\n" +
                        "     * Navigate to " + className + " page\n" +
                        "     * @param page Playwright Page instance\n" +
                        "     */\n" +
                        "    public static void navigateTo(com.microsoft.playwright.Page page) {\n" +
                        "        log.info(\"🌐 Navigating to " + className + " page\");\n" +
                        "        String url = loadProps.getProperty(\"URL\");\n" +
                        "        navigateToUrl(url);\n" +
                        "        log.info(\"✅ Navigation completed\");\n" +
                        "    }\n\n";

        // Insert after constructor (look for "}\n\n    /**" or "}\n\n    private" or "}\n\n    public")
        String pattern = "(public " + className + "\\(\\) \\{[^}]*\\}\\n\\n)";
        if (content.matches("(?s).*" + pattern + ".*")) {
            content = content.replaceFirst(pattern, "$1" + navigateMethod);
        } else {
            // Insert before first method if constructor pattern not found
            content = content.replaceFirst("(\n    /\\*\\*\n     \\* )", "\n" + navigateMethod + "$1");
        }

        return content;
    }

    /**
     * AUTO-FIX: Ensures all required imports are present
     *
     * @param content Page Object content
     * @return Fixed content with all imports
     */
    private static String ensureRequiredImports(String content) {
        StringBuilder imports = new StringBuilder();
        boolean needsFix = false;

        // Check and add missing imports
        if (!content.contains("import com.microsoft.playwright.Page;")) {
            imports.append("import com.microsoft.playwright.Page;\n");
            needsFix = true;
        }
        if (!content.contains("import configs.loadProps;")) {
            imports.append("import configs.loadProps;\n");
            needsFix = true;
        }
        if (!content.contains("import configs.TimeoutConfig;")) {
            imports.append("import configs.TimeoutConfig;\n");
            needsFix = true;
        }

        if (needsFix) {
            System.out.println("[AUTO-FIX] Adding missing imports");
            // Insert after package declaration
            content = content.replaceFirst("(package pages;\\n)", "$1" + imports.toString());
        }

        return content;
    }

    /**
     * AUTO-FIX: Validates that all feature file steps have matching step definitions
     * Generates missing step definitions automatically
     *
     * @param featureContent  Feature file content
     * @param stepDefsContent Step definitions content
     * @param testName        Test name for generating methods
     * @return Fixed step definitions with all missing steps added
     */
    private static String validateAndFixStepMatching(String featureContent, String stepDefsContent, String testName) {
        if (featureContent == null || stepDefsContent == null) {
            return stepDefsContent;
        }

        System.out.println("[AUTO-FIX] Validating feature steps match step definitions...");

        // Extract all steps from feature file
        Set<String> featureSteps = extractStepsFromFeature(featureContent);
        Set<String> existingSteps = extractStepsFromStepDefs(stepDefsContent);

        // Find missing steps
        Set<String> missingSteps = new HashSet<>(featureSteps);
        missingSteps.removeAll(existingSteps);

        // Filter out steps that would create duplicate method names
        Set<String> trulyMissingSteps = new HashSet<>();
        for (String step : missingSteps) {
            String methodName = generateMethodNameFromStep(step);
            if (!existingSteps.contains("METHOD:" + methodName)) {
                trulyMissingSteps.add(step);
            } else {
                System.out.println("[AUTO-FIX] ⚠️ Skipping duplicate method: " + methodName + " for step: " + step);
            }
        }

        if (trulyMissingSteps.isEmpty()) {
            System.out.println("[AUTO-FIX] ✅ All feature steps have matching step definitions");
            return stepDefsContent;
        }

        System.out.println("[AUTO-FIX] ⚠️ Found " + trulyMissingSteps.size() + " missing step definitions");

        // Generate missing step definitions
        StringBuilder missingStepDefs = new StringBuilder();
        missingStepDefs.append("\n    // ========== AUTO-GENERATED MISSING STEPS ==========\n\n");

        for (String step : trulyMissingSteps) {
            System.out.println("[AUTO-FIX] Adding missing step: " + step);
            missingStepDefs.append(generateStepDefinition(step));
        }

        // Insert before closing brace
        int lastBrace = stepDefsContent.lastIndexOf("}");
        if (lastBrace > 0) {
            stepDefsContent = stepDefsContent.substring(0, lastBrace) +
                    missingStepDefs.toString() +
                    "\n}\n";
        }

        System.out.println("[AUTO-FIX] ✅ Added " + trulyMissingSteps.size() + " missing step definitions");
        return stepDefsContent;
    }

    /**
     * Generate method name from step text (used for duplicate detection)
     */
    private static String generateMethodNameFromStep(String stepText) {
        String methodName = stepText
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", " ")
                .split(" ")
                .length > 0 ?
                String.join("",
                        Arrays.stream(stepText.replaceAll("[^a-zA-Z0-9\\s]", "").trim().split("\\s+"))
                                .limit(5)
                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                                .toArray(String[]::new))
                : "generatedStep";

        // Ensure starts with lowercase
        return methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
    }

    /**
     * Extracts all Given/When/Then steps from feature file
     */
    private static Set<String> extractStepsFromFeature(String featureContent) {
        Set<String> steps = new HashSet<>();
        Pattern stepPattern = Pattern.compile("^\\s*(Given|When|Then|And|But)\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = stepPattern.matcher(featureContent);

        while (matcher.find()) {
            String keyword = matcher.group(1);
            String stepText = matcher.group(2).trim();

            // Normalize step text (remove trailing punctuation, extra spaces)
            stepText = stepText.replaceAll("[\\s]+", " ").trim();

            // Convert And/But to most recent keyword type
            if (!keyword.equals("And") && !keyword.equals("But")) {
                steps.add(stepText);
            } else {
                // For And/But, add as-is since they inherit previous keyword
                steps.add(stepText);
            }
        }

        return steps;
    }

    /**
     * Extracts all implemented steps from step definitions file
     * Now also detects @And annotations and method names to prevent duplicates
     */
    private static Set<String> extractStepsFromStepDefs(String stepDefsContent) {
        Set<String> steps = new HashSet<>();

        // Pattern to match @Given/@When/@Then/@And annotations
        Pattern stepPattern = Pattern.compile("@(?:Given|When|Then|And)\\(\"([^\"]+)\"\\)");
        Matcher matcher = stepPattern.matcher(stepDefsContent);

        while (matcher.find()) {
            String stepText = matcher.group(1).trim();
            steps.add(stepText);
        }

        // Also extract method names to detect duplicate implementations
        Pattern methodPattern = Pattern.compile("public\\s+void\\s+(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(stepDefsContent);

        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            // Mark method as existing by adding a special marker
            steps.add("METHOD:" + methodName);
        }

        return steps;
    }

    /**
     * Generates a step definition method for a missing step
     */
    private static String generateStepDefinition(String stepText) {
        // Determine keyword (Given/When/Then) based on step text content
        String keyword = determineStepKeyword(stepText);

        // Generate method name using shared method
        String methodName = generateMethodNameFromStep(stepText);

        StringBuilder sb = new StringBuilder();
        sb.append("    @").append(keyword).append("(\"").append(stepText).append("\")\n");
        sb.append("    public void ").append(methodName).append("() {\n");
        sb.append("        // TODO: Implement step: ").append(stepText).append("\n");
        sb.append("        System.out.println(\"⚠️ Step not yet implemented: ").append(stepText).append("\");\n");
        sb.append("    }\n\n");

        return sb.toString();
    }

    /**
     * Determines the appropriate keyword for a step based on its content
     */
    private static String determineStepKeyword(String stepText) {
        String lower = stepText.toLowerCase();

        // Given: Setup/preconditions
        if (lower.matches("^(user is|page is|application is|system is|browser is|data is).*")) {
            return "Given";
        }

        // When: Actions
        if (lower.matches("^(user (clicks?|enters?|types?|selects?|submits?|navigates?|tries?|makes?|completes?)|multiple users).*")) {
            return "When";
        }

        // Then: Assertions/verifications
        if (lower.matches("^(.*should|.*must|.*will|.*can|.*cannot|all .*).*")) {
            return "Then";
        }

        // Default to Then for verification-like steps
        return "Then";
    }

    private static boolean validateGeneratedContent(String content, String fileType) {
        if (content == null || content.trim().isEmpty()) {
            System.err.println("[VALIDATION ERROR] " + fileType + " content is empty");
            return false;
        }

        // Basic syntax validation
        switch (fileType) {
            case "PageObject":
                if (!content.contains("package pages;")) {
                    System.err.println("[VALIDATION ERROR] Missing package declaration in Page Object");
                    return false;
                }
                if (!content.contains("extends BasePage")) {
                    System.err.println("[VALIDATION ERROR] Page Object doesn't extend BasePage");
                    return false;
                }
                if (content.contains("private static final String") && !content.contains("=")) {
                    System.err.println("[VALIDATION ERROR] Locator constant missing value");
                    return false;
                }
                // Check for navigateTo method
                if (!content.contains("public static void navigateTo")) {
                    System.err.println("[VALIDATION WARNING] Missing navigateTo method - will be auto-fixed");
                }
                // Check for protected methods
                if (content.contains("protected static void")) {
                    System.err.println("[VALIDATION WARNING] Protected methods found - will be auto-fixed to public");
                }
                break;

            case "Feature":
                if (!content.contains("Feature:")) {
                    System.err.println("[VALIDATION ERROR] Missing Feature declaration");
                    return false;
                }
                if (!content.contains("Scenario:")) {
                    System.err.println("[VALIDATION ERROR] No scenarios defined");
                    return false;
                }
                break;

            case "StepDef":
                if (!content.contains("package stepDefs;")) {
                    System.err.println("[VALIDATION ERROR] Missing package declaration in Step Definitions");
                    return false;
                }
                if (!content.contains("extends browserSelector")) {
                    System.err.println("[VALIDATION ERROR] Step Definitions doesn't extend browserSelector");
                    return false;
                }
                break;
        }

        System.out.println("[VALIDATION] " + fileType + " content validated successfully");
        return true;
    }

    /**
     * Generate test files from Playwright recording.
     * Main entry point for recording-based generation.
     * <p>
     * TODO: MANDATORY PRE-GENERATION CHECKS:
     * 1. Check if page object exists (avoid overwriting custom code)
     * 2. Verify existing login patterns (reuse existing steps)
     * 3. Check configurations.properties for test data
     * 4. Validate all selectors follow priority order
     * 5. Warn about dynamic IDs before generation
     *
     * @param recordingFile Path to recorded-actions.java file
     * @param featureName   Name of the feature (e.g., "login", "profile")
     * @param pageUrl       Page URL or path
     * @param jiraStory     JIRA story ID
     * @return true if successful, false otherwise
     */
    public static boolean generateFromRecording(String recordingFile, String featureName,
                                                String pageUrl, String jiraStory) {

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          ADVANCED AUTO-FIX TEST GENERATOR                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // AUTO-FIX: Validate recording file first
        if (!validateRecordingFile(recordingFile)) {
            System.err.println("[ERROR] Recording file validation failed");
            return false;
        }

        System.out.println("[INFO] Pure Java Test File Generator with Auto-Fix Enabled");
        System.out.println("[INFO] Recording file: " + recordingFile);
        System.out.println("[INFO] Feature name (input): " + featureName);
        System.out.println("[INFO] Page URL: " + pageUrl);
        System.out.println("[INFO] JIRA Story: " + jiraStory);

        // AUTO-FIX: Sanitize and validate feature name
        String className = autoFixFeatureName(featureName);
        System.out.println("[INFO] Feature name (fixed): " + className);

        // MANDATORY - Run pre-generation validation checks
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          CODE REUSABILITY & VALIDATION CHECKS                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");


        boolean hasExistingLogin = false;
        boolean hasConfiguredData = false;

        // Check 1: Existing page object
        System.out.println("🔍 [CHECK 1] Scanning for existing page objects...");
        if (pageObjectExists(className)) {
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
            System.out.println(
                    "      2. Call login methods: " + existingLogin + ".enterValidUsernameFromConfiguration(page);");
            System.out.println(
                    "      3. Call login methods: " + existingLogin + ".enterValidPasswordFromConfiguration(page);");
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
            System.out.println("      1. In Page Objects: loadProps.getProperty(loadProps.PropKeys.USERNAME)");
            System.out.println("      2. In Step Defs: Use loadProps.getProperty(loadProps.PropKeys.PASSWORD)");
            System.out.println("      3. In Features: Reference as 'valid credentials from configuration'");
            System.out.println("   🤖 AI ENHANCEMENT: AITestFramework.generateTestData() can generate smart test data");
            System.out.println("   💡 TIP: Use configuration data instead of hardcoded values!");
            hasConfiguredData = true;
        } else {
            System.out.println("⚠️  NOT FOUND: No test credentials in configurations.properties");
            System.out.println("   💡 TIP: Add Username and Password properties for data-driven testing");
            System.out.println("   🤖 AI OPTION: Use AITestFramework.generateTestData(\"email\", \"userEmail\", null)");
        }

        System.out.println("═══════════════════════════════════════════════════════════════\n");

        try {
            // Parse recording file with auto-fix
            System.out.println("\n[AUTO-FIX] Parsing recording file with validation...");
            List<RecordedAction> actions = parseRecording(recordingFile);
            System.out.println("[AUTO-FIX] Extracted " + actions.size() + " actions from recording");

            if (actions.isEmpty()) {
                System.err.println("[ERROR] No valid actions found in recording");
                return false;
            }

            // AUTO-FIX: Validate selectors and detect issues
            System.out.println("\n[AUTO-FIX] Validating selectors...");
            int dynamicIdCount = 0;
            int invalidSelectorCount = 0;
            for (RecordedAction action : actions) {
                if (action.selector != null) {
                    // Check for dynamic IDs
                    if (action.selector.contains("@id=")) {
                        String id = action.selector.replaceAll(".*@id=['\"]([^'\"]+)['\"].*", "$1");
                        if (isDynamicId(id)) {
                            dynamicIdCount++;
                            System.out.println("⚠️ [AUTO-FIX] Dynamic ID detected: " + id + " (will be downgraded)");
                        }
                    }

                    // Validate selector
                    if (!isValidSelector(action.selector)) {
                        invalidSelectorCount++;
                        System.out.println("⚠️ [AUTO-FIX] Invalid selector: " + action.selector);
                    }
                }
            }

            if (dynamicIdCount > 0) {
                System.out.println("[AUTO-FIX] Found " + dynamicIdCount + " dynamic IDs - using fallback strategies");
            }
            if (invalidSelectorCount > 0) {
                System.out.println("[AUTO-FIX] Fixed " + invalidSelectorCount + " invalid selectors");
            }
            System.out.println("[AUTO-FIX] Selector validation complete\n");

            // Generate files with error handling
            try {
                System.out.println("[AUTO-FIX] Generating Page Object...");
                generatePageObject(className, pageUrl, jiraStory, actions);
                System.out.println("[AUTO-FIX] ✅ Page Object generated successfully");
            } catch (Exception e) {
                System.err.println("[ERROR] Page Object generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Page Object Generation");
                throw e;
            }

            try {
                System.out.println("[AUTO-FIX] Generating Feature File...");
                generateFeatureFile(className, jiraStory, actions);
                System.out.println("[AUTO-FIX] ✅ Feature File generated successfully");
            } catch (Exception e) {
                System.err.println("[ERROR] Feature File generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Feature File Generation");
                throw e;
            }

            try {
                System.out.println("[AUTO-FIX] Generating Step Definitions...");
                generateStepDefinitions(className, jiraStory, actions);
                System.out.println("[AUTO-FIX] ✅ Step Definitions generated successfully");
            } catch (Exception e) {
                System.err.println("[ERROR] Step Definitions generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Step Definitions Generation");
                throw e;
            }

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
                System.out
                        .println("   💡 WHY? Existing login is tested, uses config data, and maintains consistency!\n");
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
            System.out.println("   ✓ Or:  quick-start.bat → Option 3 (Validate & Run)\n");

            System.out.println("🧪 [RUN TESTS]");
            System.out.println("   ✓ Run specific feature: mvn test -Dcucumber.filter.tags=@" + className);
            System.out.println("   ✓ Run all tests: mvn test");
            System.out.println("   ✓ Or use: quick-start.bat → Option 3 (Validate & Run)\n");

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
         * text=Sign In -> SignIn
         * text=Save -> Save
         * #username -> Username
         * input[placeholder="Email"] -> Email
         * button:has-text("Submit") -> Submit
         * role=button[name="Login"] -> Login
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
                if (selector.charAt(start) == '"')
                    start++;
                int end = selector.indexOf("\"", start);
                if (end == -1)
                    end = selector.indexOf("]", start);
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
                    .replaceAll("[^a-zA-Z0-9\\s]", " ") // Remove special chars
                    .replaceAll("\\s+", " ") // Normalize spaces
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
         * Generate element constant name (e.g., SIGN_IN, USERNAME, PASSWORD)
         * NO ID SUFFIX - Use semantic names only
         */
        private String generateElementName(String readableName, int id) {
            // Convert PascalCase to UPPER_SNAKE_CASE
            String snakeCase = readableName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
            // Don't append ID - use semantic name only
            return snakeCase;
        }

        /**
         * Generate method name based on action type and readable name.
         * Creates descriptive, verb-based method names following Java conventions.
         * 
         * CRITICAL: This method MUST NEVER return "Value" as a method name!
         * All method names must be action-based and descriptive.
         * 
         * @param type         Action type (click, fill, select, check, press, navigate)
         * @param readableName Human-readable element name (e.g., "SignIn", "Username")
         * @param id           Element ID for uniqueness
         * @return Action-based method name (e.g., "clickSignIn", "enterUsername")
         */
        private String generateMethodName(String type, String readableName, int id) {
            // Validate inputs - prevent empty or "Value" method names
            if (readableName == null || readableName.trim().isEmpty()) {
                readableName = "Element" + id;
            }
            if (readableName.equalsIgnoreCase("Value")) {
                readableName = "Field" + id;
            }

            String methodName;
            switch (type) {
                case "click":
                    // Use "click" for buttons/links, makes sense contextually
                    methodName = "click" + readableName;
                    break;

                case "fill":
                    // Use "enter" for text fields to match natural language
                    // Special handling for common field names
                    if (readableName.toLowerCase().contains("search")) {
                        methodName = "search" + readableName.replace("Search", "");
                    } else if (readableName.toLowerCase().contains("email")) {
                        methodName = "enterEmail";
                    } else if (readableName.toLowerCase().contains("password")) {
                        methodName = "enterPassword";
                    } else if (readableName.toLowerCase().contains("username")) {
                        methodName = "enterUsername";
                    } else {
                        methodName = "enter" + readableName;
                    }
                    break;

                case "select":
                    // Use "select" for dropdowns
                    methodName = "select" + readableName;
                    break;

                case "check":
                    // Use "check" for checkboxes, "toggle" for switches
                    if (readableName.toLowerCase().contains("toggle") ||
                            readableName.toLowerCase().contains("switch")) {
                        methodName = "toggle" + readableName.replace("Toggle", "").replace("Switch", "");
                    } else {
                        methodName = "check" + readableName;
                    }
                    break;

                case "press":
                    // Use "pressKeyOn" for keyboard actions
                    methodName = "pressKeyOn" + readableName;
                    break;

                case "navigate":
                    methodName = "navigateTo";
                    break;

                default:
                    // Fallback with camelCase - ensure it's descriptive
                    String camelCase = readableName.substring(0, 1).toLowerCase() +
                            (readableName.length() > 1 ? readableName.substring(1) : "");
                    methodName = camelCase + "Action";
                    break;
            }

            // Final safety check: NEVER return "Value" or similar generic names
            if (methodName.equalsIgnoreCase("Value") ||
                    methodName.equalsIgnoreCase("value") ||
                    methodName.isEmpty()) {
                methodName = "performAction" + id;
                System.err.println("⚠️ WARNING: Generated invalid method name, using fallback: " + methodName);
            }

            return methodName;
        }

        /**
         * Generate human-readable step text.
         */
        private String generateStepText(String type, String readableName) {
            String lowerName = readableName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

            switch (type) {
                case "click":
                    return "user clicks on " + lowerName;
                case "fill":
                    return "user enters text into " + lowerName;
                case "select":
                    return "user selects option from " + lowerName;
                case "check":
                    return "user checks " + lowerName;
                case "press":
                    return "user presses key on " + lowerName;
                case "navigate":
                    return "user navigates to page";
                default:
                    return "user performs action on " + lowerName;
            }
        }
    }

    /**
     * Optimizes a selector by preferring stable locators.
     * NOW ENHANCED WITH AI: Uses AITestFramework for intelligent locator strategies
     * Priority Order:
     * 1. Static ID (highest priority) - AI validates stability
     * 2. data-testid attributes - AI recommended
     * 3. Relative XPath - AI optimized
     * 4. Label or Names - AI validated
     * 5. Class name (if stable) - AI checks for dynamic patterns
     * 6. CSS (lowest priority) - AI fallback
     *
     * @param selector The original selector
     * @return Optimized selector following AI-enhanced priority order
     */
    private static String optimizeSelector(String selector) {
        if (selector == null || selector.isEmpty())
            return selector;

        // ========== VALIDATION: REJECT OVERLY GENERIC SELECTORS ==========
        // Prevent strict mode violations by detecting selectors that match too many elements
        if (isOverlyGenericSelector(selector)) {
            // Try to make it more specific by adding visible constraint
            String specificSelector = makeGenericSelectorSpecific(selector);
            if (specificSelector != null && !specificSelector.equals(selector)) {
                selector = specificSelector;
            }
        }

        // FIRST: Try to extract static ID from complex selectors
        String extractedStaticId = extractStaticIdFromSelector(selector);
        if (extractedStaticId != null) {
            return "//input[@id='" + extractedStaticId + "'] | //button[@id='" + extractedStaticId + "'] | " +
                    "//textarea[@id='" + extractedStaticId + "'] | //select[@id='" + extractedStaticId + "'] | " +
                    "//*[@id='" + extractedStaticId + "']";
        }

        // AI ENHANCEMENT: Use AITestFramework to generate smart locator strategies
        try {
            List<AITestFramework.LocatorStrategy> aiStrategies =
                    AITestFramework.generateSmartLocators(selector, "optimization");

            if (aiStrategies != null && !aiStrategies.isEmpty()) {
                // Use the highest priority strategy from AI
                AITestFramework.LocatorStrategy bestStrategy = aiStrategies.get(0);
                return bestStrategy.selector;
            }
        } catch (Exception e) {
            // Silently fall back to classic optimization
        }

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
            if (!isDynamicId(extractedId)) {
                return "//input[@id='" + extractedId + "'] | //button[@id='" + extractedId + "'] | " +
                        "//textarea[@id='" + extractedId + "'] | //select[@id='" + extractedId + "'] | " +
                        "//*[@id='" + extractedId + "']";
            }
        }

        // ========== PRIORITY 2: RELATIVE XPATH ==========
        // Already in relative XPath format (starts with //)
        if (selector.startsWith("//") || selector.startsWith("(//")) {
            return selector;
        }

        // ========== PRIORITY 3: ABSOLUTE XPATH ==========
        // Absolute XPath (starts with single /)
        if (selector.startsWith("/") && !selector.startsWith("//")) {
            return selector;
        }

        // ========== PRIORITY 4: LABEL OR NAMES ==========
        // Handle label= locator - try to convert to ID (Priority 1) or XPath
        if (selector.startsWith("label=")) {
            String labelText = selector.substring(6).trim();
            // Try to upgrade to Priority 1 (Static ID) if mapping exists
            String fieldId = labelToId.get(labelText);
            if (fieldId != null) {
                return "//input[@id='" + fieldId + "'] | //textarea[@id='" + fieldId + "'] | //select[@id='" + fieldId
                        + "']";
            }
            // Convert to relative XPath (Priority 2)
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
                return "//input[@name='" + name + "'] | //button[@name='" + name + "'] | " +
                        "//textarea[@name='" + name + "'] | //select[@name='" + name + "'] | " +
                        "//*[@name='" + name + "']";
            }
        }

        // Handle placeholder (treat as name-like selector)
        if (selector.startsWith("placeholder=")) {
            String placeholder = selector.substring(12).trim();
            return "//input[@placeholder='" + placeholder + "'] | //textarea[@placeholder='" + placeholder + "']";
        }

        // Handle text= locator (treat as name-like)
        if (selector.startsWith("text=")) {
            String text = selector.substring(5).trim();
            return "//button[normalize-space(text())='" + text + "'] | " +
                    "//a[normalize-space(text())='" + text + "'] | " +
                    "//*[normalize-space(text())='" + text + "']";
        }

        // ========== PRIORITY 5: CLASS NAME ==========
        // CSS class selector: .className
        if (selector.startsWith(".")) {
            String className = selector.substring(1).split("[\\s\\[\\]\\.\\:]")[0];
            return "//*[contains(@class, '" + className + "')]";
        }

        // Attribute selector with class
        if (selector.contains("class=") && !selector.startsWith("//")) {
            Pattern classPattern = Pattern.compile("class=['\"]([^'\"]+)['\"]?");
            Matcher matcher = classPattern.matcher(selector);
            if (matcher.find()) {
                String className = matcher.group(1).split("\\s+")[0]; // Get first class
                return "//*[contains(@class, '" + className + "')]";
            }
        }

        // ========== PRIORITY 6: CSS SELECTORS ==========
        // Generic CSS selectors (lowest priority) - return as-is
        return selector;
    }

    /**
     * Checks if an ID is dynamic (contains GUIDs, timestamps, or random strings).
     * Dynamic IDs should be avoided as they change between sessions.
     *
     * @param id The ID to check
     * @return true if the ID appears to be dynamic
     */
    private static boolean isDynamicId(String id) {
        if (id == null || id.isEmpty())
            return false;

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
     * Detects overly generic selectors that would match multiple elements.
     * These selectors cause "strict mode violation" errors in Playwright.
     * <p>
     * Examples of generic selectors:
     * - //div (matches all divs)
     * - //span (matches all spans)
     * - //button (matches all buttons without attributes)
     * - div (CSS, matches all divs)
     *
     * @param selector The selector to check
     * @return true if selector is overly generic
     */
    private static boolean isOverlyGenericSelector(String selector) {
        if (selector == null || selector.isEmpty()) {
            return false;
        }

        // List of generic HTML elements that often match multiple elements
        String[] genericElements = {
                "div", "span", "a", "p", "li", "ul", "ol", "td", "tr", "th",
                "section", "article", "aside", "nav", "header", "footer", "main"
        };

        // Check for XPath patterns: //element or //element[no attributes]
        for (String element : genericElements) {
            // Exact match: //div
            if (selector.equals("//" + element)) {
                return true;
            }
            // With trailing slash or brackets: //div/ or //div[]
            if (selector.matches("^//" + element + "\\s*[\\[/]?\\s*$")) {
                return true;
            }
        }

        // Check for CSS patterns: just element name
        for (String element : genericElements) {
            if (selector.equals(element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to make a generic selector more specific to avoid strict mode violations.
     * <p>
     * Strategies:
     * 1. Add visibility constraint with wrapper class
     * 2. Target first visible element
     *
     * @param genericSelector The overly generic selector
     * @return A more specific selector, or the original if can't be improved
     */
    private static String makeGenericSelectorSpecific(String genericSelector) {
        if (genericSelector == null || genericSelector.isEmpty()) {
            return genericSelector;
        }

        // Extract element name
        String element = genericSelector.replace("//", "").replace("/", "").trim();

        // Strategy: Add visible constraint with wrapper class or first match
        // This will select the first visible element of that type within a wrapper
        return element + ".wrapper >> visible=true";
    }

    /**
     * Gets priority comment for a selector (used in generated code).
     * TODO: MANDATORY - All generated locators must show their priority level
     *
     * @param selector The selector to analyze
     * @return Priority comment string
     */
    private static String getPriorityComment(String selector) {
        if (selector == null || selector.isEmpty())
            return "Unknown Priority";

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
     * Extracts static ID from complex selectors before optimization.
     * Scans CSS selectors, XPath, and other formats for ID attributes.
     * This ensures we prioritize IDs even when Playwright recorded a different selector type.
     * <p>
     * Examples:
     * - "div#myId.class" -> extracts "myId"
     * - "//div[@id='myId'][@class='x']" -> extracts "myId"
     * - "input[id='username']" -> extracts "username"
     *
     * @param selector Original selector from recording
     * @return ID if found and static, otherwise null
     */
    private static String extractStaticIdFromSelector(String selector) {
        if (selector == null || selector.isEmpty()) {
            return null;
        }

        // Pattern 1: CSS selector with # (e.g., "div#myId", "#myId.class", "form #myId")
        Pattern cssIdPattern = Pattern.compile("#([a-zA-Z][a-zA-Z0-9_-]*)");
        Matcher matcher = cssIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in CSS: #" + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in CSS: #" + id + " (rejected)");
            }
        }

        // Pattern 2: XPath with @id attribute (e.g., "//input[@id='username']")
        Pattern xpathIdPattern = Pattern.compile("@id\\s*=\\s*['\"]([^'\"]+)['\"]");
        matcher = xpathIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in XPath: " + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in XPath: " + id + " (rejected)");
            }
        }

        // Pattern 3: CSS attribute selector (e.g., "input[id='username']", "[id=username]")
        Pattern attrIdPattern = Pattern.compile("\\[id\\s*=\\s*['\"]?([^'\"\\]]+)['\"]?\\]");
        matcher = attrIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in attribute selector: " + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in attribute selector: " + id + " (rejected)");
            }
        }

        // Pattern 4: Simple id= format (e.g., "id=username")
        if (selector.startsWith("id=") || selector.contains(" id=")) {
            Pattern simpleIdPattern = Pattern.compile("id\\s*=\\s*['\"]?([^'\"\\s]+)['\"]?");
            matcher = simpleIdPattern.matcher(selector);
            if (matcher.find()) {
                String id = matcher.group(1);
                if (!isDynamicId(id)) {
                    System.out.println("[ID EXTRACTION] Found static ID in simple format: " + id);
                    return id;
                }
            }
        }

        return null;
    }

    /**
     * Generate Page Object from recorded actions.
     * <p>
     * CODE REUSABILITY: Checks pageObjectExists() to avoid overwriting existing
     * classes
     * LOCATOR OPTIMIZATION: All selectors optimized via optimizeSelector() before
     * generation
     * SKIP LOGIC: Returns early if page object exists, preserving custom
     * implementations
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
        // AUTO-FIX: Always include all required imports for page objects
        sb.append("import com.microsoft.playwright.Page;\n");
        sb.append("import configs.loadProps;\n");
        sb.append("import configs.TimeoutConfig;\n");  // Always include for consistent timeout usage
        sb.append("import java.util.logging.Logger;\n");
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Page Object for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording\n");
        sb.append(" * \n");
        sb.append(" * This class extends BasePage which provides:\n");
        sb.append(" *  - clickOnElement(locator) - from utils.java\n");
        sb.append(" *  - enterText(locator, text) - from utils.java\n");
        sb.append(" *  - selectDropDownValueByText(locator, text) - from utils.java\n");
        sb.append(" *  - navigateToUrl(url) - from BasePage.java\n");
        sb.append(" *  - And many more common utilities\n");
        sb.append(" * \n");
        sb.append(" * All generated methods use these common utilities for consistency\n");
        sb.append(" * and better maintainability across the test framework.\n");
        sb.append(" * \n");
        sb.append(" * @story ").append(jiraStory).append("\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append(" extends BasePage {\n");
        sb.append("    private static final Logger log = Logger.getLogger(").append(className).append(".class.getName());\n");

        // Extract path from full URL if needed
        String pagePath = extractPathFromUrl(pageUrl);
        sb.append("    private static final String PAGE_PATH = \"").append(pagePath).append("\";\n");
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

        // AUTO-FIX: Always generate navigateTo method with proper imports and logging
        sb.append("    /**\n");
        sb.append("     * Navigate to ").append(className).append(" page\n");
        sb.append("     * Uses common navigateToUrl method from BasePage\n");
        sb.append("     * @param page Playwright Page instance\n");
        sb.append("     */\n");
        sb.append("    public static void navigateTo").append(className).append("(Page page) {\n");
        sb.append("        log.info(\"🌐 Navigating to ").append(className).append(" page\");\n");
        sb.append("        String fullUrl = loadProps.getProperty(\"URL\") + PAGE_PATH;\n");
        sb.append("        navigateToUrl(fullUrl);\n");
        sb.append("        log.info(\"✅ Navigation completed\");\n");
        sb.append("    }\n");
        sb.append("\n");

        // Generate methods for each action with descriptive names
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // DEDUPLICATION: Skip duplicate methods
            if (generatedMethods.contains(action.methodName)) {
                System.out.println("[SKIP DUPLICATE] Method already exists: " + action.methodName + "()");
                continue;
            }
            generatedMethods.add(action.methodName);

            sb.append("    /**\n");
            sb.append("     * ").append(action.stepText).append("\n");
            sb.append("     * Element: ").append(action.readableName).append(" (").append(action.selector)
                    .append(")\n");
            sb.append("     * Uses common method from utils.java\n");
            sb.append("     */\n");

            switch (action.type) {
                case "click":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"🖱️ ").append(action.stepText).append("\");\n");
                    sb.append("        clickOnElement(").append(action.elementName).append("); // Common method from utils\n");
                    sb.append("        TimeoutConfig.waitShort(); // Auto-fixed timeout method\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "fill":
                    sb.append("    public static void ").append(action.methodName)
                            .append("(Page page, String text) {\n");
                    sb.append("        log.info(\"⌨️ ").append(action.stepText).append(": \" + text);\n");
                    sb.append("        enterText(").append(action.elementName).append(", text); // Common method from utils\n");
                    sb.append("        TimeoutConfig.waitShort(); // Auto-fixed timeout method\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "select":
                    sb.append("    public static void ").append(action.methodName)
                            .append("(Page page, String option) {\n");
                    sb.append("        log.info(\"🔽 ").append(action.stepText).append(": \" + option);\n");
                    sb.append("        selectDropDownValueByText(").append(action.elementName).append(", option); // Common method from utils\n");
                    sb.append("        TimeoutConfig.waitShort(); // Auto-fixed timeout method\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "check":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"☑️ ").append(action.stepText).append("\");\n");
                    sb.append("        clickOnElement(").append(action.elementName).append("); // Common method from utils\n");
                    sb.append("        TimeoutConfig.waitShort(); // Auto-fixed timeout method\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "press":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"⌨️ ").append(action.stepText).append(": \" + ")
                            .append(action.elementName).append(" + \" - Key: ").append(escapeJavaString(action.value))
                            .append("\");\n");
                    sb.append("        page.locator(").append(action.elementName).append(").press(\"")
                            .append(escapeJavaString(action.value)).append("\");\n");
                    sb.append("        TimeoutConfig.waitShort(); // Auto-fixed timeout method\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;
            }
            sb.append("\n");
        }

        sb.append("}\n");

        // AUTO-FIX: Apply all fixes before validation
        String pageObjectContent = sb.toString();

        // Fix 1: Ensure all required imports
        pageObjectContent = ensureRequiredImports(pageObjectContent);

        // Fix 2: Ensure navigateTo method exists
        pageObjectContent = ensureNavigateToMethod(pageObjectContent, className);

        // Fix 3: Convert protected methods to public
        pageObjectContent = autoFixMethodVisibility(pageObjectContent);

        // AUTO-FIX: Validate generated content after fixes
        if (!validateGeneratedContent(pageObjectContent, "PageObject")) {
            throw new IOException("Page Object validation failed");
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/main/java/pages/" + className + ".java"), pageObjectContent.getBytes());
            System.out.println("[AUTO-FIX] ✅ Page Object file written successfully");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Page Object file: " + e.getMessage());
            autoRecoverFromError(e, "Page Object File Write");
            throw e;
        }
    }

    /**
     * Parse Playwright recording file and extract actions.
     * Supports both old API (page.click) and modern API (page.locator().click())
     * 
     * LOCATOR OPTIMIZATION: Applies optimizeSelector() to prioritize:
     * 1. Static ID, 2. Relative XPath, 3. Absolute XPath, 4. Label/Names, 5. Class,
     * 6. CSS
     * 
     * DYNAMIC ID DETECTION: Identifies and warns about GUID/timestamp IDs using
     * isDynamicId()
     */
    private static List<RecordedAction> parseRecording(String recordingFile) throws IOException {
        List<RecordedAction> actions = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(recordingFile)));

        // Patterns for MODERN Playwright Locator API (primary)
        Pattern locatorClickPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern locatorFillPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern locatorSelectPattern = Pattern
                .compile("page\\.locator\\(\"([^\"]+)\"\\)\\.selectOption\\(\"([^\"]+)\"\\)");
        Pattern locatorCheckPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.check\\(");
        Pattern locatorPressPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.press\\(\"([^\"]+)\"\\)");

        // Modern getBy* API patterns
        Pattern getByRoleClickPattern = Pattern.compile(
                "page\\.getByRole\\(AriaRole\\.\\w+,\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)\\)\\.click\\(");
        Pattern getByTextClickPattern = Pattern.compile("page\\.getByText\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern getByPlaceholderFillPattern = Pattern
                .compile("page\\.getByPlaceholder\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelClickPattern = Pattern.compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern getByLabelFillPattern = Pattern.compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelPressPattern = Pattern
                .compile("page\\.getByLabel\\(\"([^\"]+)\"\\)\\.press\\(\"([^\"]+)\"\\)");

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
                System.out
                        .println("[DEBUG] Found getByPlaceholder fill: " + matcher.group(1) + " = " + matcher.group(2));
                actions.add(
                        new RecordedAction(actionId++, "fill", "placeholder=" + matcher.group(1), matcher.group(2)));
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
     * Extracts path from full URL, returning only the path portion.
     * If URL contains the base URL from config, returns only the path after it.
     * If it's already a path (starts with /), returns as-is.
     * If it's just the base URL with no path, returns empty string.
     *
     * @param url Full URL or path
     * @return Path portion only (e.g., "/login", "/start-page", or "")
     */
    private static String extractPathFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }

        url = url.trim();

        // If it's already a relative path, return as-is
        if (url.startsWith("/")) {
            return url;
        }

        // If it doesn't look like a URL (no protocol), return as-is
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }

        try {
            // Get base URL from config
            String baseUrl = loadProps.getProperty("URL");
            if (baseUrl != null && !baseUrl.isEmpty()) {
                baseUrl = baseUrl.trim();
                // Remove trailing slash from base URL for comparison
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }

                // If the URL starts with base URL, extract the path
                if (url.startsWith(baseUrl)) {
                    String path = url.substring(baseUrl.length());
                    // Return empty string if no path, otherwise return the path
                    return path.isEmpty() ? "" : path;
                }
            }

            // If base URL not found or doesn't match, try to parse URL
            // Extract path from URL (everything after domain)
            int protocolEnd = url.indexOf("://");
            if (protocolEnd != -1) {
                String afterProtocol = url.substring(protocolEnd + 3);
                int pathStart = afterProtocol.indexOf('/');
                if (pathStart != -1) {
                    return afterProtocol.substring(pathStart);
                }
            }

            // No path found, return empty string
            return "";

        } catch (Exception e) {
            System.err.println("[WARN] Could not extract path from URL: " + url + " - using as-is");
            return url;
        }
    }

    /**
     * Generate Feature file from recorded actions.
     *
     * SCENARIO OUTLINE: Includes actual recorded data in Examples table
     * CODE REUSABILITY: Detects existing login patterns via containsLoginPattern()
     * Suggests using existing steps from detectExistingLogin()
     * Checks hasConfiguredCredentials() for test data reuse
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
            System.out
                    .println("   📝 Consider using existing login steps from " + existingLoginClass + "Steps.java:\n");
            System.out.println("   RECOMMENDED STEPS:");
            System.out.println("      Given User navigates to the MRI Energy login page");
            System.out.println("      When User enters valid username from configuration");
            System.out.println("      And User enters valid password from configuration");
            System.out.println("      And User clicks on Sign In button");
            System.out.println("      Then User should be successfully logged in\n");
            System.out.println("   💡 TIP: Reference existing step definitions for consistency");
            System.out.println("   🤖 AI ENHANCEMENT: Feature file will auto-integrate existing login methods");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        }

        if (hasConfiguredData) {
            System.out.println("📋 [DATA TIP] Configured test data available");
            System.out.println("   ✓ Use parameterized steps like 'user enters valid username from configuration'");
            System.out.println("   ✓ Avoid hardcoding credentials in feature files");
            System.out.println("   ✓ Reference: src/test/resources/configurations.properties");
            System.out.println("   🤖 AI OPTION: AITestFramework.generateTestData() for smart test data");
            System.out.println("   🤖 AI OPTION: AITestFramework.generateFormData() for complete form data\n");
        }

        StringBuilder sb = new StringBuilder();
        List<String> exampleColumns = new ArrayList<>();
        List<String> exampleValues = new ArrayList<>();

        sb.append("@").append(jiraStory).append(" @").append(className).append("\n");
        sb.append("Feature: ").append(className).append(" Test\n");
        sb.append("  Auto-generated from Playwright recording\n");
        sb.append("\n");

        // SMART DECISION: Will be determined after processing actions
        // Placeholder - will be replaced with correct keyword based on Examples table presence
        int scenarioLinePosition = sb.length();
        sb.append("  SCENARIO_PLACEHOLDER: Complete ").append(className).append(" workflow\n");

        sb.append("    Given user navigates to ").append(className).append(" page\n");

        // DEDUPLICATION: Track generated steps in feature file
        Set<String> generatedFeatureSteps = new HashSet<>();
        boolean hasGeneratedLoginSteps = false;

        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);

            // If login reuse enabled and this is login action, replace with existing login
            // steps
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══\n");
                sb.append("    When User enters valid username from configuration\n");
                sb.append("    And User enters valid password from configuration\n");
                sb.append("    And User clicks on Sign In button\n");
                sb.append("    # ═══════════════════════════════════════════════\n");
                hasGeneratedLoginSteps = true;
                continue; // Skip generating the hardcoded login step
            }

            // CRITICAL FIX: Skip ALL remaining login actions after login steps generated
            if (shouldReuseLogin && isLoginAction && hasGeneratedLoginSteps) {
                System.out.println("[SKIP LOGIN ACTION] Already using existing login steps, skipping: "
                        + action.stepText);
                continue;
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
                        System.out.println("[SKIP LOGIN FIELD] Not adding to feature file (using existing login): "
                                + action.readableName);
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
                    featureStep = "And user selects \"<" + selectColumn + ">\" from "
                            + action.readableName.toLowerCase();

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
            sb.append("      | ").append(String.join(" | ", exampleColumns)).append(" |\n");
            sb.append("      | ").append(String.join(" | ", exampleValues)).append(" |\n");
        }

        // SMART GHERKIN: Replace placeholder with correct keyword based on Examples presence
        String featureContent = sb.toString();
        if (!exampleColumns.isEmpty()) {
            // Has Examples table → Use "Scenario Outline:"
            featureContent = featureContent.replace("SCENARIO_PLACEHOLDER:", "Scenario Outline:");
            System.out.println("[GHERKIN] Using 'Scenario Outline:' (Examples table detected)");
        } else {
            // No Examples table → Use "Scenario:"
            String scenarioName = shouldReuseLogin && hasLoginPattern ?
                "Scenario: Complete " + className + " workflow with existing login" :
                "Scenario: Complete " + className + " workflow";
            featureContent = featureContent.replace("SCENARIO_PLACEHOLDER: Complete " + className + " workflow", scenarioName);
            System.out.println("[GHERKIN] Using 'Scenario:' (No Examples table)");
        }

        // Update StringBuilder with corrected content
        sb = new StringBuilder(featureContent);

        // AUTO-FIX: Validate feature file content before writing
        String featureContent = sb.toString();
        if (!validateGeneratedContent(featureContent, "Feature")) {
            throw new IOException("Feature file validation failed");
        }

        // Auto-fix: Ensure proper Gherkin syntax in all steps
        String[] lines = featureContent.split("\n");
        StringBuilder fixedContent = new StringBuilder();
        for (String line : lines) {
            if (line.trim().matches("^(Given|When|Then|And|But)\\s+.*")) {
                line = autoFixFeatureStep(line.trim());
                fixedContent.append("    ").append(line).append("\n");
            } else {
                fixedContent.append(line).append("\n");
            }
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/test/java/features/" + className + ".feature"), fixedContent.toString().getBytes());
            System.out.println("[AUTO-FIX] ✅ Feature file written successfully");

            // AI ENHANCEMENT: Analyze feature file quality with AITestFramework
            try {
                System.out.println("\n🤖 [AI ANALYSIS] Analyzing feature file quality with AITestFramework...");
                AITestFramework.CoverageReport coverage = AITestFramework.analyzeCoverage(className);

                if (coverage != null) {
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║              AI-POWERED COVERAGE ANALYSIS                      ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    System.out.println("📊 Feature: " + coverage.featureName);
                    System.out.println("   Total Scenarios: " + coverage.totalScenarios);
                    System.out.println("   Implemented Steps: " + coverage.implementedSteps);
                    System.out.println("   Coverage: " + coverage.coveragePercentage + "%");
                    System.out.println("\n📈 Scenario Types:");
                    System.out.println("   ✓ Positive: " + coverage.positiveScenarios);
                    System.out.println("   ✗ Negative: " + coverage.negativeScenarios);
                    System.out.println("   📏 Boundary: " + coverage.boundaryScenarios);

                    if (!coverage.suggestions.isEmpty()) {
                        System.out.println("\n💡 AI Recommendations:");
                        for (String recommendation : coverage.suggestions) {
                            System.out.println("   • " + recommendation);
                        }
                    }
                    System.out.println("═══════════════════════════════════════════════════════════════\n");
                }
            } catch (Exception e) {
                System.out.println("[AI-INFO] Coverage analysis not available: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Feature file: " + e.getMessage());
            autoRecoverFromError(e, "Feature File Write");
            throw e;
        }
    }

    /**
     * Generate Step Definitions from recorded actions.
     *
     * CODE REUSABILITY:
     * - Detects login patterns via containsLoginPattern()
     * - Imports existing classes via detectExistingLogin()
     * - Adds documentation about reused methods
     * - Provides tips for integrating with existing login steps
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
            System.out
                    .println("   2. Locate login-related step definitions (look for 'username', 'password', 'signin')");
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

        // Import loadProps if we're reusing login functionality
        if (shouldReuseLogin) {
            sb.append("import configs.loadProps;\n");
        }

        sb.append("import io.cucumber.java.en.*;\n");
        sb.append("import pages.").append(className).append(";\n");

        // FIXED: Always import Login with capital L for login reuse
        if (shouldReuseLogin) {
            sb.append("import pages.Login;\n");
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
        sb.append("        ").append(className).append(".navigateTo").append(className).append("(page);\n");
        sb.append("    }\n");
        sb.append("\n");

        // DEDUPLICATION: Track generated step definitions
        Set<String> generatedSteps = new HashSet<>();
        Set<String> generatedStepMethods = new HashSet<>();

        // Track if we're generating login steps
        boolean hasGeneratedLoginSteps = false;

        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);

            // Generate camelCase method name for step definition
            String stepMethodName = action.methodName.substring(0, 1).toLowerCase() + action.methodName.substring(1);

            // DEDUPLICATION: Skip duplicate step methods
            if (generatedStepMethods.contains(stepMethodName)) {
                System.out.println("[SKIP DUPLICATE] Step definition method already exists: " + stepMethodName + "()");
                continue;
            }

            // If login reuse is enabled and this is a login action, generate actual implementation
            // instead of TODO comments
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // 🔄 LOGIN STEPS - USING EXISTING CONFIGURATION\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // These methods use credentials from configurations.properties file\n");
                sb.append("    // This approach is more maintainable than hardcoded values\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("\n");

                // Generate actual implementation - Username step
                sb.append("    @When(\"User enters valid username from configuration\")\n");
                sb.append("    public void enterValidUsernameFromConfiguration() {\n");
                sb.append("        String username = loadProps.getProperty(loadProps.PropKeys.USERNAME);\n");
                sb.append("        System.out.println(\"📍 Step: Entering username from configuration: \" + username);\n");
                sb.append("        ").append(existingLoginClass).append(".enterUsername(page, username);\n");
                sb.append("    }\n");
                sb.append("\n");

                // Generate actual implementation - Password step
                sb.append("    @And(\"User enters valid password from configuration\")\n");
                sb.append("    public void enterValidPasswordFromConfiguration() {\n");
                sb.append("        String password = loadProps.getProperty(loadProps.PropKeys.PASSWORD);\n");
                sb.append("        System.out.println(\"📍 Step: Entering password from configuration\");\n");
                sb.append("        ").append(existingLoginClass).append(".enterPassword(page, password);\n");
                sb.append("    }\n");
                sb.append("\n");

                // Generate actual implementation - Sign In button step
                sb.append("    @And(\"User clicks on Sign In button\")\n");
                sb.append("    public void clickSignInButton() {\n");
                sb.append("        System.out.println(\"📍 Step: Clicking Sign In button\");\n");
                sb.append("        ").append(existingLoginClass).append(".clickSignIn(page);\n");
                sb.append("    }\n");
                sb.append("\n");

                hasGeneratedLoginSteps = true;

                // Skip generating individual login-related actions
                continue;
            }

            // Skip other login actions if we've already generated the configuration-based methods
            if (shouldReuseLogin && isLoginAction && hasGeneratedLoginSteps) {
                continue;
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
                    sb.append("        System.out.println(\"📍 Step: Entering text into ").append(action.readableName)
                            .append(": '\" + text + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName)
                            .append("(page, text);\n");
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
                    sb.append("        System.out.println(\"📍 Step: Selecting option from ")
                            .append(action.readableName).append(": '\" + option + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName)
                            .append("(page, option);\n");
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

        // AUTO-FIX: Validate step definitions content before writing
        String stepDefContent = sb.toString();
        if (!validateGeneratedContent(stepDefContent, "StepDef")) {
            throw new IOException("Step Definitions validation failed");
        }

        // AUTO-FIX: Validate feature steps match step definitions and generate missing ones
        try {
            String featureContent = new String(Files.readAllBytes(
                    Paths.get("src/test/java/features/" + className.toLowerCase() + ".feature")));
            stepDefContent = validateAndFixStepMatching(featureContent, stepDefContent, className);
        } catch (IOException e) {
            System.out.println("[AUTO-FIX] ⚠️ Could not read feature file for step matching validation");
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/test/java/stepDefs/" + className + "Steps.java"), stepDefContent.getBytes());
            System.out.println("[AUTO-FIX] ✅ Step Definitions file written successfully");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Step Definitions file: " + e.getMessage());
            autoRecoverFromError(e, "Step Definitions File Write");
            throw e;
        }
    }

    /**
     * Convert string to camelCase format
     * Reserved for future string formatting needs
     */
    @SuppressWarnings("unused")
    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "defaultMethod";
        }

        String pascalCase = toPascalCase(input);

        // Convert first character to lowercase
        String camelCase = Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);

        // Validate and return
        if (isValidJavaIdentifier(camelCase)) {
            return camelCase;
        } else {
            System.err.println("⚠️  WARNING: Generated invalid method name '" + camelCase + "', using defaultMethod");
            return "defaultMethod";
        }
    }

    /**
     * Escape special characters for Java strings.
     */
    private static String escapeJavaString(String str) {
        if (str == null)
            return "";
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
        if (action == null)
            return false;

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
     * 
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
     * 
     * @return Class name with login methods, or null if not found
     */
    private static String detectExistingLogin() {
        try {
            Path pagesDir = Paths.get("src/main/java/pages");
            if (!Files.exists(pagesDir))
                return null;

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
     * 
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
     * 
     * @return true if credentials are configured
     */
    private static boolean hasConfiguredCredentials() {
        try {
            Path configPath = Paths.get("src/test/resources/configurations.properties");
            if (!Files.exists(configPath))
                return false;

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
     * @param issueKey   JIRA issue key (e.g., "ECS-123")
     * @param autoDetect If true, automatically detects elements and scenarios
     * @return TestRequirement with story details, or null if failed
     */
    public static TestRequirement generateFromJiraStory(String issueKey, boolean autoDetect) {
        System.out.println("🔍 Fetching JIRA story: " + issueKey);

        jiraClient.JiraStory story = jiraClient.getJiraStory(issueKey);

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
                    (requirement.verification.performanceThreshold / 1000) + "s)" : "✗"));
            System.out.println("  - Logging: " + (requirement.verification.logging ? "✓" : "✗"));
        }

        // Convert acceptance criteria to comprehensive scenarios
        if (!story.acceptanceCriteria.isEmpty()) {
            System.out.println("📖 Converting " + story.acceptanceCriteria.size() +
                    " acceptance criteria to comprehensive scenarios...");

            for (int i = 0; i < story.acceptanceCriteria.size(); i++) {
                String criterion = story.acceptanceCriteria.get(i);
                String scenarioName = "Verify "
                        + (criterion.length() > 50 ? criterion.substring(0, 50) + "..." : criterion);

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
            String expectedResult = criterion.contains("should")
                    ? criterion.substring(criterion.toLowerCase().indexOf("should") + 6).trim()
                    : "complete successfully";
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
                    "And the action should not proceed"));

            scenarios.add(new Scenario("Verify validation with invalid data",
                    "Given user is on the application page",
                    "When user enters invalid data in fields",
                    "And user attempts to proceed",
                    "Then validation errors should be displayed",
                    "And invalid fields should be highlighted"));
        }

        // UI responsiveness scenario
        if (issueType.matches("(?i).*(story|feature).*")) {
            scenarios.add(new Scenario("Verify UI responsiveness",
                    "Given user is on the application page",
                    "When the page loads",
                    "Then all elements should be visible",
                    "And the layout should be proper",
                    "And no visual glitches should occur"));
        }

        // Error handling scenario
        scenarios.add(new Scenario("Verify error handling",
                "Given user is on the application page",
                "When an error condition occurs",
                "Then appropriate error message should be displayed",
                "And user should be able to recover",
                "And application should remain stable"));

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
                "And expected result should be displayed"));

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
            requirement.verification.performanceThreshold = priority.matches("(?i).*(critical|blocker).*") ? 2000
                    : 3000;
        }

        // Logging for bugs and complex features
        if (issueType.matches("(?i).*(bug|defect).*") ||
                priority.matches("(?i).*(high|critical|blocker).*")) {
            requirement.verification.logging = true;
        }
    }

    /**
     * Validates if a string is a valid Java identifier.
     * Ensures it follows Java naming rules: starts with letter/underscore/$,
     * contains only letters, digits, underscores.
     * 
     * @param identifier The identifier to validate
     * @return true if valid Java identifier
     */
    private static boolean isValidJavaIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        // Check if it's a Java reserved keyword
        String[] keywords = { "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
                "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native", "new", "package", "private",
                "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
        for (String keyword : keywords) {
            if (identifier.equals(keyword)) {
                return false;
            }
        }

        // First character must be letter, underscore, or dollar sign
        char first = identifier.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }

        // Rest must be valid identifier characters
        for (int i = 1; i < identifier.length(); i++) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a string to valid PascalCase Java class name.
     * Ensures it starts with uppercase letter and contains only valid Java
     * identifier characters.
     * 
     * @param input The input string
     * @return Valid PascalCase class name
     */
    private static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return "DefaultClass";
        }

        // Remove all non-alphanumeric characters except spaces
        String cleaned = input.replaceAll("[^a-zA-Z0-9\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return "DefaultClass";
        }

        // Convert to PascalCase
        String[] words = cleaned.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }

        String pascalCase = result.toString();

        // Ensure it starts with a letter
        if (!Character.isLetter(pascalCase.charAt(0))) {
            pascalCase = "Class" + pascalCase;
        }

        // Validate and return
        if (isValidJavaIdentifier(pascalCase)) {
            return pascalCase;
        } else {
            System.err.println("⚠️  WARNING: Generated invalid class name '" + pascalCase + "', using DefaultClass");
            return "DefaultClass";
        }
    }

    /**
     * Converts a string to valid camelCase Java method/variable name.
     * Ensures it starts with lowercase letter and contains only valid Java
     * identifier characters.
     * 
     * @param input The input string
     * @return Valid camelCase method/variable name
     */

    /**
     * Auto-fix configuration for test generation
     * These fields are reserved for future advanced auto-fix features
     */
    @SuppressWarnings("unused")
    private static class AutoFixConfig {
        static final boolean ENABLE_AUTO_FIX = true;
        static final boolean FIX_INVALID_IDENTIFIERS = true;
        static final boolean FIX_DUPLICATE_METHODS = true;
        static final boolean FIX_MISSING_IMPORTS = true;
        static final boolean FIX_INVALID_SELECTORS = true;
        static final boolean FIX_FEATURE_SYNTAX = true;
        static final boolean SANITIZE_INPUTS = true;
    }

    /**
     * Sanitizes a story summary to create a valid test class name.
     * Uses strict Java naming validation.
     * 
     * @param summary Story summary text
     * @return Sanitized class name following Java conventions
     */
    private static String sanitizeTestName(String summary) {
        String pascalCase = toPascalCase(summary);
        System.out.println("[JAVA VALIDATION] Sanitized test name: '" + summary + "' → '" + pascalCase + "'");
        return pascalCase;
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
                if (!pageObjectExists)
                    missingFiles.add(pageObjectPath);
                if (!featureExists)
                    missingFiles.add(featurePath);
                if (!stepDefExists)
                    missingFiles.add(stepDefPath);

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
            // openLatestReport(); // TODO: Implement this method

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
