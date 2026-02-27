#!/usr/bin/env node
/**
 * AI-Powered Playwright Automation MCP Server
 *
 * This MCP server enables AI assistants to generate Playwright automation scripts
 * by understanding the existing framework structure and providing intelligent
 * code generation capabilities.
 *
 * @author Automation Team
 * @version 1.0.0
 */
import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { CallToolRequestSchema, ListResourcesRequestSchema, ListToolsRequestSchema, ReadResourceRequestSchema, } from '@modelcontextprotocol/sdk/types.js';
import * as fs from 'fs/promises';
import * as path from 'path';
import { fileURLToPath } from 'url';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
// Project paths
const PROJECT_ROOT = path.resolve(__dirname, '../..');
const PAGES_DIR = path.join(PROJECT_ROOT, 'src/main/java/pages');
const FEATURES_DIR = path.join(PROJECT_ROOT, 'src/test/java/features');
const STEPDEFS_DIR = path.join(PROJECT_ROOT, 'src/test/java/stepDefs');
/**
 * Framework structure information
 */
const FRAMEWORK_INFO = {
    name: "Playwright-Cucumber-TestNG Framework",
    version: "1.0.0",
    structure: {
        pages: "Page Object Model - All page objects extend BasePage.java",
        features: "Cucumber BDD feature files with Gherkin syntax",
        stepDefs: "Step definitions implementing feature file steps",
        configs: "Configuration classes for browser, retry, reporting",
        hooks: "Before/After hooks for test setup and teardown"
    },
    patterns: {
        page: "Extend BasePage, use protected static methods, include comprehensive JavaDoc",
        feature: "Use Given-When-Then structure, include @tags for categorization",
        stepDef: "Implement @Given, @When, @Then annotations, use PicoContainer for DI"
    }
};
/**
 * MCP Server instance
 */
const server = new Server({
    name: 'playwright-automation-mcp-server',
    version: '1.0.0',
}, {
    capabilities: {
        tools: {},
        resources: {},
    },
});
/**
 * Utility: Read file content
 */
async function readFileContent(filePath) {
    try {
        return await fs.readFile(filePath, 'utf-8');
    }
    catch (error) {
        throw new Error(`Failed to read file ${filePath}: ${error}`);
    }
}
/**
 * Utility: Write file content
 */
async function writeFileContent(filePath, content) {
    try {
        await fs.mkdir(path.dirname(filePath), { recursive: true });
        await fs.writeFile(filePath, content, 'utf-8');
    }
    catch (error) {
        throw new Error(`Failed to write file ${filePath}: ${error}`);
    }
}
/**
 * Utility: List files in directory
 */
async function listFiles(dirPath, extension) {
    try {
        const files = await fs.readdir(dirPath);
        if (extension) {
            return files.filter(f => f.endsWith(extension));
        }
        return files;
    }
    catch (error) {
        return [];
    }
}
/**
 * Utility: Analyze existing page objects
 */
async function analyzePageObjects() {
    const pageFiles = await listFiles(PAGES_DIR, '.java');
    const pages = [];
    for (const file of pageFiles) {
        if (file === 'BasePage.java')
            continue;
        const content = await readFileContent(path.join(PAGES_DIR, file));
        const className = file.replace('.java', '');
        // Extract methods (simple regex-based)
        const methodMatches = content.matchAll(/protected static \w+\s+(\w+)\s*\([^)]*\)/g);
        const methods = Array.from(methodMatches, m => m[1]);
        pages.push({
            name: className,
            file,
            methodCount: methods.length,
            methods: methods.slice(0, 5) // First 5 methods as sample
        });
    }
    return pages;
}
/**
 * Utility: Analyze existing features
 */
async function analyzeFeatures() {
    const featureFiles = await listFiles(FEATURES_DIR, '.feature');
    const features = [];
    for (const file of featureFiles) {
        const content = await readFileContent(path.join(FEATURES_DIR, file));
        const featureName = file.replace('.feature', '');
        // Extract scenarios
        const scenarioMatches = content.matchAll(/Scenario:(.+)/g);
        const scenarios = Array.from(scenarioMatches, m => m[1].trim());
        features.push({
            name: featureName,
            file,
            scenarioCount: scenarios.length,
            scenarios: scenarios.slice(0, 3)
        });
    }
    return features;
}
/**
 * Tool: Generate Page Object
 * Uses Playwright Locator pattern: locator methods + action methods.
 * Follows the same pattern as Treecomponent.java and Login.java.
 */
async function generatePageObject(args) {
    const { pageName, elements, description, verification } = args;
    const className = pageName.charAt(0).toUpperCase() + pageName.slice(1);
    const fileName = `${className}.java`;
    const filePath = path.join(PAGES_DIR, fileName);
    const enableLogging = verification?.logging ?? false;
    const enableAssertions = verification?.functional ?? false;
    const enablePerformance = verification?.performance ?? false;
    // ── helpers ────────────────────────────────────────────────────────────────
    const toPascal = (name) => name.replace(/[^a-zA-Z0-9\s]/g, '').trim().split(/\s+/)
        .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join('');
    const toCamel = (name) => {
        const p = toPascal(name);
        return p.charAt(0).toLowerCase() + p.slice(1);
    };
    const smartLocator = (el) => {
        const n = el.name.toLowerCase();
        const act = (el.action || 'click').toLowerCase();
        const label = el.name.replace(/\s+(field|button|btn|dropdown|select|input|checkbox|link|text|area|textbox)$/i, '').trim();
        const cid = label.toLowerCase().replace(/[^a-z0-9]/g, '');
        if (n.includes('password') || n.includes('pass'))
            return "page.locator(\"input[type='password'], input[name*='password' i], input[id*='password' i]\")";
        if (n.includes('email') || n.includes('e-mail'))
            return "page.locator(\"input[type='email'], input[name*='email' i], input[id*='email' i]\")";
        if (n.includes('username') || n.includes('user name'))
            return "page.locator(\"input[type='email'], input[name*='user' i], input[id*='user' i]\")";
        if (n.includes('search'))
            return `page.locator("input[name*='search' i], input[id*='search' i], input[placeholder*='search' i]")`;
        if (n.includes('checkbox') || (n.includes('check') && !n.includes('search')))
            return `page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("${label}"))`;
        if (act === 'select' || n.includes('dropdown') || n.includes('select') || n.includes('combo'))
            return `page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("${label}"))`;
        if (n.includes('link') || n.includes('menu') || n.includes('nav'))
            return `page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("${label}"))`;
        if (act === 'type' || n.includes('field') || n.includes('input') || n.includes('text') || n.includes('name'))
            return `page.locator("input[name*='${cid}' i], input[id*='${cid}' i], input[placeholder*='${label}' i], textarea[name*='${cid}' i]")`;
        // Default: button
        return `page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("${label}"))`;
    };
    // ── generate locator + action methods ─────────────────────────────────────
    const seenLocators = new Set();
    const elementMethods = elements.map((el) => {
        const locatorId = toCamel(el.name); // "usernameField"
        const pascalId = toPascal(el.name); // "UsernameField"
        const act = (el.action || 'click').toLowerCase();
        const locExpr = smartLocator(el);
        let block = '';
        if (!seenLocators.has(locatorId)) {
            seenLocators.add(locatorId);
            block += `    /** Locator for ${el.name} */\n`;
            block += `    public static Locator ${locatorId}() {\n`;
            block += `        return ${locExpr};\n`;
            block += `    }\n\n`;
        }
        const prefix = act === 'type' ? 'enter' : act === 'select' ? 'select' : 'click';
        const methodName = `${prefix}${pascalId}`;
        const sigParam = act === 'type' ? 'Page page, String text' : act === 'select' ? 'Page page, String option' : 'Page page';
        block += `    /**\n     * ${el.description || el.name} - ${act} action\n     * @param page Playwright Page instance\n     */\n`;
        block += `    public static void ${methodName}(${sigParam}) {\n`;
        if (enableLogging) {
            block += `        log.info("${act === 'type' ? '⌨️' : '🖱️'} ${el.name}");\n`;
        }
        else {
            block += `        System.out.println("📍 Step: ${prefix} ${el.name}");\n`;
        }
        if (enablePerformance)
            block += `        long startTime = System.currentTimeMillis();\n`;
        if (act === 'click')
            block += `        clickOnElement(${locatorId}());\n`;
        else if (act === 'type')
            block += `        enterText(${locatorId}(), text);\n`;
        else if (act === 'select')
            block += `        selectDropDownValueByText(${locatorId}(), option);\n`;
        if (enablePerformance) {
            block += `        long duration = System.currentTimeMillis() - startTime;\n`;
            block += `        log.info("⏱️ Action completed in " + duration + "ms");\n`;
        }
        block += `        TimeoutConfig.waitShort();\n`;
        if (enableLogging)
            block += `        log.info("✅ ${el.name} completed");\n`;
        block += `    }`;
        return block;
    }).join('\n\n');
    // ── verification methods ───────────────────────────────────────────────────
    let verificationMethods = '';
    if (enableAssertions) {
        verificationMethods += `\n\n    /** Verify page loaded via URL check */\n`;
        verificationMethods += `    public static void verifyPageLoaded(String expectedUrlPart) {\n`;
        verificationMethods += `        log.info("🔍 Verifying page loaded");\n`;
        verificationMethods += `        Assert.assertTrue(isUrlContains(expectedUrlPart), "Page URL verification failed");\n`;
        verificationMethods += `        log.info("✓ Page verified");\n`;
        verificationMethods += `    }`;
    }
    // Include Logger only when it is actually referenced (logging, performance timing, assertions)
    const useLogger = enableLogging || enablePerformance || enableAssertions;
    let imports = '';
    if (enableAssertions)
        imports += `\nimport org.testng.Assert;`;
    if (useLogger)
        imports += `\nimport java.util.logging.Logger;`;
    const loggerDeclaration = useLogger
        ? `\n    private static final Logger log = Logger.getLogger(${className}.class.getName());`
        : '';
    const pageContent = `package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import configs.TimeoutConfig;${imports}

/**
 * ${description || `${className} Page Object`}
 * Auto-generated by MCP Server - uses Playwright Locator pattern.
 * Extends BasePage to inherit common utilities.
 */
public class ${className} extends BasePage {${loggerDeclaration}
    private static final String PAGE_PATH = "";

    /* --------------------
       Locators for ${className}
       -----------------------*/

${elementMethods}${verificationMethods}

}
`;
    await writeFileContent(filePath, pageContent);
    return `✅ Page Object generated: ${fileName}\n📁 Location: ${filePath}\n\nNext steps:\n1. Review the generated code\n2. Create corresponding feature file using 'generate-feature' tool\n3. Generate step definitions using 'generate-step-definitions' tool`;
}
/**
 * Tool: Generate Feature File
 */
async function generateFeatureFile(args) {
    const { featureName, scenarios, description } = args;
    const fileName = `${featureName}.feature`;
    const filePath = path.join(FEATURES_DIR, fileName);
    // Generate scenarios
    const scenarioContent = scenarios.map((scenario, index) => {
        const steps = scenario.steps.map((step) => `    ${step}`).join('\n');
        return `  @Functional @Priority=${index}
  Scenario: ${scenario.name}
${steps}`;
    }).join('\n\n');
    const featureContent = `Feature: ${description || featureName}
  As a user
  I want to test ${featureName} functionality
  So that I can ensure it works correctly

  Background:
    Given the application is ready

${scenarioContent}
`;
    await writeFileContent(filePath, featureContent);
    return `✅ Feature file generated: ${fileName}\n📁 Location: ${filePath}\n\nNext step: Generate step definitions using 'generate-step-definitions' tool`;
}
/**
 * Tool: Generate Step Definitions
 */
async function generateStepDefinitions(args) {
    const { featureName, pageName, steps } = args;
    const className = `${featureName}Steps`;
    const fileName = `${className}.java`;
    const filePath = path.join(STEPDEFS_DIR, fileName);
    const pageClass = pageName.charAt(0).toUpperCase() + pageName.slice(1);
    // Generate step methods
    const stepMethods = steps.map((step) => {
        const annotation = step.type; // Given, When, Then
        const regex = step.regex || step.text.replace(/"/g, '\\"');
        const methodName = step.methodName || step.text.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
        return `    @${annotation}("${regex}")
    public void ${methodName}() {
        System.out.println("📋 Step: ${step.text}");
        // TODO: Implement step logic using ${pageClass} page object
        ${step.implementation || '// Add implementation here'}
    }`;
    }).join('\n\n');
    const stepDefContent = `package stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import pages.${pageClass};

/**
 * Step Definitions for ${featureName}
 * 
 * This class contains step definition implementations for ${featureName} feature file.
 * Uses PicoContainer for dependency injection.
 * 
 * @author Automation Team (AI Generated)
 * @version 1.0
 */
public class ${className} {
    
    private ${pageClass} ${pageName}Page;
    
    /**
     * Constructor with DI
     */
    public ${className}() {
        this.${pageName}Page = new ${pageClass}();
    }
    
    // ============================================================
    // STEP DEFINITIONS
    // ============================================================
    
${stepMethods}
    
}`;
    await writeFileContent(filePath, stepDefContent);
    return `✅ Step Definitions generated: ${fileName}\n📁 Location: ${filePath}\n\nNext steps:\n1. Implement the TODO sections\n2. Run tests using Maven: mvn clean test\n3. Check reports in MRITestExecutionReports/`;
}
/**
 * Tool: Analyze Framework Structure
 */
async function analyzeFramework() {
    const pages = await analyzePageObjects();
    const features = await analyzeFeatures();
    const analysis = {
        framework: FRAMEWORK_INFO,
        currentState: {
            pageObjects: pages,
            featureFiles: features,
            totalPages: pages.length,
            totalFeatures: features.length
        },
        recommendations: [
            "Use 'generate-page-object' to create new page objects",
            "Use 'generate-feature' to create new feature files",
            "Use 'generate-step-definitions' to implement step definitions",
            "Follow the existing naming conventions and patterns"
        ]
    };
    return JSON.stringify(analysis, null, 2);
}
/**
 * Tool: Generate Complete Test Suite
 */
async function generateCompleteTestSuite(args) {
    const { testName, pageElements, scenarios, description } = args;
    const results = [];
    // 1. Generate Page Object
    const pageResult = await generatePageObject({
        pageName: testName,
        elements: pageElements,
        description: `Page object for ${testName} functionality`
    });
    results.push(pageResult);
    // 2. Generate Feature File
    const featureResult = await generateFeatureFile({
        featureName: testName,
        scenarios: scenarios,
        description: description
    });
    results.push(featureResult);
    // 3. Generate Step Definitions
    const steps = scenarios.flatMap((s) => s.steps.map((step) => ({
        type: step.startsWith('Given') ? 'Given' :
            step.startsWith('When') ? 'When' : 'Then',
        text: step.replace(/^(Given|When|Then|And)\s+/, ''),
        regex: step.replace(/^(Given|When|Then|And)\s+/, ''),
        methodName: step.replace(/[^a-zA-Z0-9]/g, '').toLowerCase().substring(0, 50)
    })));
    const stepDefResult = await generateStepDefinitions({
        featureName: testName,
        pageName: testName,
        steps: steps
    });
    results.push(stepDefResult);
    return `🎉 Complete test suite generated for: ${testName}\n\n${results.join('\n\n---\n\n')}`;
}
/**
 * Register Tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
        tools: [
            {
                name: 'analyze-framework',
                description: 'Analyzes the existing framework structure, page objects, and features. Use this first to understand what exists.',
                inputSchema: {
                    type: 'object',
                    properties: {},
                    required: []
                }
            },
            {
                name: 'generate-page-object',
                description: 'Generates a new Page Object class with specified elements and interactions',
                inputSchema: {
                    type: 'object',
                    properties: {
                        pageName: {
                            type: 'string',
                            description: 'Name of the page (e.g., "dashboard", "checkout")'
                        },
                        elements: {
                            type: 'array',
                            description: 'List of page elements to interact with',
                            items: {
                                type: 'object',
                                properties: {
                                    name: { type: 'string', description: 'Element name' },
                                    action: { type: 'string', description: 'Action type: click, type, select' },
                                    description: { type: 'string', description: 'Element description' }
                                }
                            }
                        },
                        description: {
                            type: 'string',
                            description: 'Page description'
                        }
                    },
                    required: ['pageName', 'elements']
                }
            },
            {
                name: 'generate-feature',
                description: 'Generates a Cucumber feature file with scenarios',
                inputSchema: {
                    type: 'object',
                    properties: {
                        featureName: {
                            type: 'string',
                            description: 'Feature name'
                        },
                        scenarios: {
                            type: 'array',
                            description: 'List of test scenarios',
                            items: {
                                type: 'object',
                                properties: {
                                    name: { type: 'string', description: 'Scenario name' },
                                    steps: { type: 'array', items: { type: 'string' }, description: 'Gherkin steps' }
                                }
                            }
                        },
                        description: {
                            type: 'string',
                            description: 'Feature description'
                        }
                    },
                    required: ['featureName', 'scenarios']
                }
            },
            {
                name: 'generate-step-definitions',
                description: 'Generates step definition class for a feature file',
                inputSchema: {
                    type: 'object',
                    properties: {
                        featureName: {
                            type: 'string',
                            description: 'Feature name'
                        },
                        pageName: {
                            type: 'string',
                            description: 'Page object name to use'
                        },
                        steps: {
                            type: 'array',
                            description: 'List of steps to implement',
                            items: {
                                type: 'object',
                                properties: {
                                    type: { type: 'string', description: 'Given, When, or Then' },
                                    text: { type: 'string', description: 'Step text' },
                                    regex: { type: 'string', description: 'Step regex pattern' },
                                    implementation: { type: 'string', description: 'Implementation code' }
                                }
                            }
                        }
                    },
                    required: ['featureName', 'pageName', 'steps']
                }
            },
            {
                name: 'generate-complete-test-suite',
                description: 'Generates a complete test suite: Page Object + Feature File + Step Definitions in one go',
                inputSchema: {
                    type: 'object',
                    properties: {
                        testName: {
                            type: 'string',
                            description: 'Name of the test suite (e.g., "UserRegistration", "ProductSearch")'
                        },
                        description: {
                            type: 'string',
                            description: 'Test suite description'
                        },
                        pageElements: {
                            type: 'array',
                            description: 'Page elements to interact with',
                            items: {
                                type: 'object',
                                properties: {
                                    name: { type: 'string' },
                                    action: { type: 'string' },
                                    description: { type: 'string' }
                                }
                            }
                        },
                        scenarios: {
                            type: 'array',
                            description: 'Test scenarios',
                            items: {
                                type: 'object',
                                properties: {
                                    name: { type: 'string' },
                                    steps: { type: 'array', items: { type: 'string' } }
                                }
                            }
                        }
                    },
                    required: ['testName', 'pageElements', 'scenarios']
                }
            }
        ]
    };
});
/**
 * Handle Tool Calls
 */
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    try {
        const { name, arguments: args } = request.params;
        let result;
        switch (name) {
            case 'analyze-framework':
                result = await analyzeFramework();
                break;
            case 'generate-page-object':
                result = await generatePageObject(args);
                break;
            case 'generate-feature':
                result = await generateFeatureFile(args);
                break;
            case 'generate-step-definitions':
                result = await generateStepDefinitions(args);
                break;
            case 'generate-complete-test-suite':
                result = await generateCompleteTestSuite(args);
                break;
            default:
                throw new Error(`Unknown tool: ${name}`);
        }
        return {
            content: [
                {
                    type: 'text',
                    text: result
                }
            ]
        };
    }
    catch (error) {
        return {
            content: [
                {
                    type: 'text',
                    text: `Error: ${error instanceof Error ? error.message : String(error)}`
                }
            ],
            isError: true
        };
    }
});
/**
 * Register Resources (Framework Documentation)
 */
server.setRequestHandler(ListResourcesRequestSchema, async () => {
    return {
        resources: [
            {
                uri: 'framework://info',
                name: 'Framework Information',
                mimeType: 'application/json',
                description: 'Complete framework structure and patterns'
            },
            {
                uri: 'framework://basepage',
                name: 'BasePage Template',
                mimeType: 'text/plain',
                description: 'BasePage.java content for reference'
            }
        ]
    };
});
server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
    const { uri } = request.params;
    if (uri === 'framework://info') {
        return {
            contents: [
                {
                    uri,
                    mimeType: 'application/json',
                    text: JSON.stringify(FRAMEWORK_INFO, null, 2)
                }
            ]
        };
    }
    if (uri === 'framework://basepage') {
        const basePagePath = path.join(PAGES_DIR, 'BasePage.java');
        const content = await readFileContent(basePagePath);
        return {
            contents: [
                {
                    uri,
                    mimeType: 'text/plain',
                    text: content
                }
            ]
        };
    }
    throw new Error(`Unknown resource: ${uri}`);
});
/**
 * Start Server
 */
async function main() {
    const transport = new StdioServerTransport();
    await server.connect(transport);
    console.error('🚀 Playwright Automation MCP Server running on stdio');
}
main().catch((error) => {
    console.error('❌ Server error:', error);
    process.exit(1);
});
//# sourceMappingURL=index.js.map