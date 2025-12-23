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
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ListResourcesRequestSchema,
  ReadResourceRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import { z } from 'zod';
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
const server = new Server(
  {
    name: 'playwright-automation-mcp-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
      resources: {},
    },
  }
);

/**
 * Utility: Read file content
 */
async function readFileContent(filePath: string): Promise<string> {
  try {
    return await fs.readFile(filePath, 'utf-8');
  } catch (error) {
    throw new Error(`Failed to read file ${filePath}: ${error}`);
  }
}

/**
 * Utility: Write file content
 */
async function writeFileContent(filePath: string, content: string): Promise<void> {
  try {
    await fs.mkdir(path.dirname(filePath), { recursive: true });
    await fs.writeFile(filePath, content, 'utf-8');
  } catch (error) {
    throw new Error(`Failed to write file ${filePath}: ${error}`);
  }
}

/**
 * Utility: List files in directory
 */
async function listFiles(dirPath: string, extension?: string): Promise<string[]> {
  try {
    const files = await fs.readdir(dirPath);
    if (extension) {
      return files.filter(f => f.endsWith(extension));
    }
    return files;
  } catch (error) {
    return [];
  }
}

/**
 * Utility: Analyze existing page objects
 */
async function analyzePageObjects(): Promise<any> {
  const pageFiles = await listFiles(PAGES_DIR, '.java');
  const pages = [];
  
  for (const file of pageFiles) {
    if (file === 'BasePage.java') continue;
    
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
async function analyzeFeatures(): Promise<any> {
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
 */
async function generatePageObject(args: any): Promise<string> {
  const { pageName, elements, description } = args;
  
  const className = pageName.charAt(0).toUpperCase() + pageName.slice(1);
  const fileName = `${className}.java`;
  const filePath = path.join(PAGES_DIR, fileName);
  
  // Generate element methods
  const elementMethods = elements.map((el: any) => {
    const methodName = el.name.replace(/\s+/g, '');
    const action = el.action || 'click';
    
    return `    /**
     * ${el.description || `Interact with ${el.name}`}
     * @param locator The element locator
     */
    protected static void ${methodName}(String locator) {
        System.out.println("🎯 ${action.charAt(0).toUpperCase() + action.slice(1)}ing on: ${el.name}");
        ${action === 'click' ? 'clickElement(locator);' : 
          action === 'type' ? 'fillText(locator, text);' :
          action === 'select' ? 'selectDropdown(locator, value);' :
          'interactWithElement(locator);'}
        TimeoutConfig.shortWait();
        System.out.println("✅ ${el.name} action completed");
    }`;
  }).join('\n\n');
  
  const pageContent = `package pages;

import com.microsoft.playwright.Page;
import configs.TimeoutConfig;

/**
 * ${description || `${className} Page Object`}
 * 
 * This page object handles interactions with the ${className} page.
 * All methods follow the Page Object Model pattern and extend BasePage.
 * 
 * @author Automation Team (AI Generated)
 * @version 1.0
 */
public class ${className} extends BasePage {
    
    /**
     * Constructor
     */
    public ${className}() {
        super();
    }
    
    // ============================================================
    // PAGE INTERACTIONS
    // ============================================================
    
${elementMethods}
    
}`;
  
  await writeFileContent(filePath, pageContent);
  return `✅ Page Object generated: ${fileName}\n📁 Location: ${filePath}\n\nNext steps:\n1. Review the generated code\n2. Create corresponding feature file using 'generate-feature' tool\n3. Generate step definitions using 'generate-step-definitions' tool`;
}

/**
 * Tool: Generate Feature File
 */
async function generateFeatureFile(args: any): Promise<string> {
  const { featureName, scenarios, description } = args;
  
  const fileName = `${featureName}.feature`;
  const filePath = path.join(FEATURES_DIR, fileName);
  
  // Generate scenarios
  const scenarioContent = scenarios.map((scenario: any, index: number) => {
    const steps = scenario.steps.map((step: string) => `    ${step}`).join('\n');
    
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
async function generateStepDefinitions(args: any): Promise<string> {
  const { featureName, pageName, steps } = args;
  
  const className = `${featureName}Steps`;
  const fileName = `${className}.java`;
  const filePath = path.join(STEPDEFS_DIR, fileName);
  
  const pageClass = pageName.charAt(0).toUpperCase() + pageName.slice(1);
  
  // Generate step methods
  const stepMethods = steps.map((step: any) => {
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
async function analyzeFramework(): Promise<string> {
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
async function generateCompleteTestSuite(args: any): Promise<string> {
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
  const steps = scenarios.flatMap((s: any) => 
    s.steps.map((step: string) => ({
      type: step.startsWith('Given') ? 'Given' : 
            step.startsWith('When') ? 'When' : 'Then',
      text: step.replace(/^(Given|When|Then|And)\s+/, ''),
      regex: step.replace(/^(Given|When|Then|And)\s+/, ''),
      methodName: step.replace(/[^a-zA-Z0-9]/g, '').toLowerCase().substring(0, 50)
    }))
  );
  
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
    
    let result: string;
    
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
  } catch (error) {
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
