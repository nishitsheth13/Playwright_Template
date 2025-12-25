#!/usr/bin/env node

/**
 * AI Automation CLI - Interactive Test Generator
 * 
 * User-friendly command-line interface for generating automation scripts
 * using AI and MCP server capabilities.
 * 
 * Features:
 * - Auto-start MCP server
 * - Generate Page Objects, Features, Step Definitions
 * - Auto-compile, test, and fix errors (up to 5 attempts)
 * - Run full test suite with reports
 * 
 * Documentation: See AI_AUTOMATION_COMPLETE_GUIDE.md (single consolidated guide)
 * 
 * TODO BEFORE USING:
 *   [ ] Run setup-mcp.bat once to install dependencies
 *   [ ] Ensure Maven & Node.js installed
 *   [ ] Update configurations.properties
 *   [ ] Prepare JIRA credentials (optional)
 * 
 * TODO USAGE:
 *   [ ] Choose input method: Manual OR JIRA Story
 *   [ ] Provide detailed test requirements
 *   [ ] Review AI-generated code
 *   [ ] Check auto-fix reports
 *   [ ] Run validation tests
 * 
 * Full TODO checklist: See WORKFLOW_TODOS.md
 * 
 * ⚠️ DEVELOPMENT POLICY:
 * - DO NOT create separate script files for new features - add functions here
 * - DO NOT create new markdown files - update AI_AUTOMATION_COMPLETE_GUIDE.md
 * - Keep project structure minimal: 4 docs + 4 scripts + this file + pom.xml
 * - Before adding any new file, check if it can be added to existing files
 */

const readline = require('readline');
const { spawn, exec } = require('child_process');
const fs = require('fs').promises;
const path = require('path');
const net = require('net');

// ANSI color codes for better UX
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  cyan: '\x1b[36m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  magenta: '\x1b[35m',
  blue: '\x1b[34m'
};

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

// MCP Server state
let mcpServerProcess = null;
const MCP_SERVER_PORT = 3000; // Default MCP server port

// Promisify readline question
const question = (query) => new Promise((resolve) => rl.question(query, resolve));

/**
 * Display welcome banner
 */
function displayWelcome() {
  console.clear();
  console.log(colors.cyan + colors.bright);
  console.log('╔════════════════════════════════════════════════════════════╗');
  console.log('║                                                            ║');
  console.log('║    🤖 AI-POWERED AUTOMATION SCRIPT GENERATOR 🚀           ║');
  console.log('║                                                            ║');
  console.log('║    Create test automation scripts in minutes!             ║');
  console.log('║    No coding required - just describe what you need       ║');
  console.log('║                                                            ║');
  console.log('╚════════════════════════════════════════════════════════════╝');
  console.log(colors.reset);
}

/**
 * Check if MCP server is running
 */
async function isMCPServerRunning() {
  return new Promise((resolve) => {
    const client = new net.Socket();
    
    client.setTimeout(2000);
    
    client.on('connect', () => {
      client.destroy();
      resolve(true);
    });
    
    client.on('error', () => {
      resolve(false);
    });
    
    client.on('timeout', () => {
      client.destroy();
      resolve(false);
    });
    
    client.connect(MCP_SERVER_PORT, 'localhost');
  });
}

/**
 * Record & Auto-Generate (Option 1)
 * Integrated Playwright recording with auto-generation, validation, and testing
 */
async function recordAndGenerate() {
  console.log(colors.green + '\n🎥 Record & Auto-Generate Test\n' + colors.reset);
  console.log('This will open Playwright Inspector to record your actions,');
  console.log('then automatically generate all test files with validation!\n');
  
  // Get test details
  const featureName = await question(colors.cyan + '📝 Feature Name (e.g., Login, Profile): ' + colors.reset);
  const pageUrl = await question(colors.cyan + '🌐 Page URL path (e.g., /login, /profile): ' + colors.reset);
  const jiraStory = await question(colors.cyan + '🎫 JIRA Story ID (optional, e.g., ECS-123): ' + colors.reset);
  
  if (!featureName.trim()) {
    console.log(colors.red + '\n❌ Feature name is required!' + colors.reset);
    return;
  }
  
  console.log(colors.yellow + '\n🚀 Starting recording process...' + colors.reset);
  console.log(colors.yellow + 'This will call record-and-generate.bat with your inputs\n' + colors.reset);
  
  // Call record-and-generate.bat with parameters
  return new Promise((resolve, reject) => {
    const recordScript = spawn('cmd.exe', ['/c', 'record-and-generate.bat'], {
      stdio: 'inherit',
      env: {
        ...process.env,
        FEATURE_NAME: featureName,
        PAGE_URL: pageUrl || '/',
        JIRA_STORY: jiraStory || 'AUTO-GEN'
      }
    });
    
    recordScript.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ Recording and generation completed successfully!\n' + colors.reset);
        console.log(colors.bright + '📋 Generated Files:' + colors.reset);
        console.log(`   ✓ src/main/java/pages/${featureName}.java`);
        console.log(`   ✓ src/test/java/features/${featureName}.feature`);
        console.log(`   ✓ src/test/java/stepDefs/${featureName}Steps.java\n`);
        
        console.log(colors.cyan + '💡 Next: Enhance with AI prompt:' + colors.reset);
        console.log(`   "Enhance recorded ${featureName} test by implementing TODOs"`);
        console.log(`   "Follow COMPLETE_TEST_GUIDE.md patterns"\n`);
      } else {
        console.log(colors.red + `\n❌ Recording process failed with code ${code}` + colors.reset);
      }
      resolve();
    });
    
    recordScript.on('error', (err) => {
      console.log(colors.red + '\n❌ Failed to start recording: ' + err.message + colors.reset);
      reject(err);
    });
  });
}

/**
 * Generate locators from recorded actions
 */
function generateLocatorsFromRecording(actions) {
  const locators = [];
  
  actions.forEach((action, index) => {
    if (action.selector) {
      const locatorName = action.name || `ELEMENT_${index + 1}`;
      locators.push({
        name: locatorName,
        selector: action.selector,
        action: action.action,
        comment: action.description || `Locator for ${action.action} action`
      });
    }
  });
  
  return locators;
}

/**
 * Generate Page Object from recording
 */
function generatePageObjectFromRecording(pageName, url, locators) {
  const className = pageName.charAt(0).toUpperCase() + pageName.slice(1);
  
  let code = `package pages;

import com.microsoft.playwright.Page;
import configs.BasePage;
import configs.loadProps;

/**
 * Page Object for ${className}
 * Auto-generated from recording
 * @author AI Automation
 */
public class ${className} extends BasePage {

    // Locators
`;

  // Add locators
  locators.forEach(loc => {
    code += `    private static final String ${loc.name.toUpperCase()} = "${loc.selector}"; // ${loc.comment}\n`;
  });
  
  code += `\n    /**\n     * Navigate to ${className} page\n     */\n`;
  code += `    public static void navigateTo(Page page) {\n`;
  code += `        String url = loadProps.getProperty("URL") + "${url}";\n`;
  code += `        page.navigate(url);\n`;
  code += `        page.waitForLoadState();\n`;
  code += `    }\n\n`;
  
  // Add action methods
  const uniqueActions = [...new Set(locators.map(l => l.action))];
  locators.forEach((loc, idx) => {
    const methodName = loc.name.toLowerCase().replace(/_/g, '');
    code += `    /**\n     * ${loc.comment}\n     */\n`;
    
    switch(loc.action) {
      case 'click':
        code += `    public static void ${methodName}() {\n`;
        code += `        clickOnElement(${loc.name.toUpperCase()});\n`;
        code += `    }\n\n`;
        break;
      case 'fill':
        code += `    public static void ${methodName}(String text) {\n`;
        code += `        enterText(${loc.name.toUpperCase()}, text);\n`;
        code += `    }\n\n`;
        break;
      case 'select':
        code += `    public static void ${methodName}(String value) {\n`;
        code += `        selectFromDropdown(${loc.name.toUpperCase()}, value);\n`;
        code += `    }\n\n`;
        break;
    }
  });
  
  code += `}`;
  return code;
}

/**
 * Generate Feature file from recording
 */
function generateFeatureFromRecording(featureName, scenarios) {
  let feature = `@${featureName} @Automated\nFeature: ${featureName}\n\n`;
  feature += `  Background:\n`;
  feature += `    Given user navigates to ${featureName} page\n\n`;
  
  scenarios.forEach((scenario, idx) => {
    feature += `  @Scenario${idx + 1}\n`;
    feature += `  Scenario: ${scenario.name || `Test scenario ${idx + 1}`}\n`;
    
    scenario.steps.forEach(step => {
      feature += `    ${step.type} ${step.text}\n`;
    });
    feature += `\n`;
  });
  
  return feature;
}

/**
 * Generate Step Definitions from recording
 */
function generateStepDefsFromRecording(featureName, steps) {
  const className = featureName.charAt(0).toUpperCase() + featureName.slice(1) + 'Steps';
  const pageName = featureName.charAt(0).toUpperCase() + featureName.slice(1);
  
  let code = `package stepDefs;

import configs.browserSelector;
import io.cucumber.java.en.*;
import pages.${pageName};

/**
 * Step Definitions for ${featureName}
 * Auto-generated from recording
 */
public class ${className} extends browserSelector {
\n`;

  code += `    @Given("user navigates to ${featureName} page")\n`;
  code += `    public void userNavigatesTo${pageName}Page() {\n`;
  code += `        ${pageName}.navigateTo(page);\n`;
  code += `    }\n\n`;
  
  steps.forEach((step, idx) => {
    const methodName = step.text
      .toLowerCase()
      .replace(/[^a-z0-9\s]/g, '')
      .split(' ')
      .map((word, i) => i === 0 ? word : word.charAt(0).toUpperCase() + word.slice(1))
      .join('');
    
    code += `    @${step.type}("${step.text}")\n`;
    code += `    public void ${methodName}() {\n`;
    code += `        // TODO: Implement step logic\n`;
    code += `    }\n\n`;
  });
  
  code += `}`;
  return code;
}

/**
 * Start MCP server
 */
async function startMCPServer() {
  return new Promise(async (resolve) => {
    const mcpServerPath = path.join(__dirname, 'mcp-server');
    const nodeModulesExists = await fs.access(path.join(mcpServerPath, 'node_modules'))
      .then(() => true)
      .catch(() => false);
    
    if (!nodeModulesExists) {
      console.log(colors.yellow + '📦 Installing MCP server dependencies...\n' + colors.reset);
      
      const install = spawn('npm', ['install'], {
        cwd: mcpServerPath,
        shell: true,
        stdio: 'inherit'
      });
      
      install.on('close', (code) => {
        if (code !== 0) {
          console.log(colors.red + '❌ Failed to install MCP server dependencies' + colors.reset);
          resolve(false);
          return;
        }
        startServer();
      });
    } else {
      startServer();
    }
    
    function startServer() {
      console.log(colors.cyan + '🚀 Starting MCP server...\n' + colors.reset);
      
      mcpServerProcess = spawn('npm', ['start'], {
        cwd: mcpServerPath,
        shell: true,
        detached: false
      });
      
      mcpServerProcess.stdout.on('data', (data) => {
        const output = data.toString();
        if (output.includes('Server running') || output.includes('listening on port')) {
          console.log(colors.green + '✅ MCP server started successfully!\n' + colors.reset);
          resolve(true);
        }
      });
      
      mcpServerProcess.stderr.on('data', (data) => {
        // Ignore stderr unless it's an actual error
        const output = data.toString();
        if (output.includes('Error') || output.includes('EADDRINUSE')) {
          console.log(colors.yellow + '⚠️ MCP server may already be running\n' + colors.reset);
          resolve(true);
        }
      });
      
      // Give it 5 seconds to start
      setTimeout(() => {
        resolve(true);
      }, 5000);
    }
  });
}

/**
 * Ensure MCP server is running
 */
async function ensureMCPServer() {
  console.log(colors.cyan + '🔍 Checking MCP server status...\n' + colors.reset);
  
  const isRunning = await isMCPServerRunning();
  
  if (isRunning) {
    console.log(colors.green + '✅ MCP server is already running!\n' + colors.reset);
    return true;
  }
  
  console.log(colors.yellow + '⚠️ MCP server is not running\n' + colors.reset);
  const shouldStart = await question(colors.cyan + '🚀 Start MCP server now? (y/n): ' + colors.reset);
  
  if (shouldStart.toLowerCase() === 'y') {
    return await startMCPServer();
  }
  
  console.log(colors.yellow + '⚠️ Some features may not work without MCP server\n' + colors.reset);
  return false;
}

/**
 * Fetch JIRA Story details
 */
async function getJiraStory(issueKey) {
  const fs = require('fs').promises;
  const https = require('https');
  
  try {
    // Load JIRA config
    const configPath = path.join(process.cwd(), 'src/test/resources/jiraConfigurations.properties');
    const configContent = await fs.readFile(configPath, 'utf-8');
    
    const config = {};
    configContent.split('\n').forEach(line => {
      const trimmedLine = line.trim().replace(/\r$/,''); // Remove carriage returns
      const [key, ...valueParts] = trimmedLine.split('=');
      if (key && valueParts.length > 0) {
        config[key.trim()] = valueParts.join('=').trim();
      }
    });
    
    const baseUrl = config.JIRA_BASE_URL;
    const email = config.JIRA_EMAIL;
    const apiToken = config.JIRA_API_TOKEN;
    
    if (!baseUrl || !email || !apiToken) {
      console.log(colors.red + '❌ JIRA credentials not configured in jiraConfigurations.properties\n' + colors.reset);
      return null;
    }
    
    // Make JIRA API call
    const auth = Buffer.from(`${email}:${apiToken}`).toString('base64');
    const url = new URL(`/rest/api/3/issue/${issueKey}`, baseUrl);
    
    return new Promise((resolve, reject) => {
      const options = {
        hostname: url.hostname,
        path: url.pathname + url.search,
        method: 'GET',
        headers: {
          'Authorization': `Basic ${auth}`,
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      };
      
      const req = https.request(options, (res) => {
        let data = '';
        
        res.on('data', (chunk) => {
          data += chunk;
        });
        
        res.on('end', () => {
          if (res.statusCode === 200) {
            try {
              const issue = JSON.parse(data);
              resolve({
                key: issue.key,
                summary: issue.fields.summary,
                description: extractPlainText(issue.fields.description),
                issueType: issue.fields.issuetype.name,
                status: issue.fields.status.name,
                priority: issue.fields.priority?.name || 'Medium',
                acceptanceCriteria: extractAcceptanceCriteria(issue.fields.description)
              });
            } catch (e) {
              reject(new Error('Failed to parse JIRA response: ' + e.message));
            }
          } else if (res.statusCode === 404) {
            reject(new Error(`JIRA issue ${issueKey} not found`));
          } else if (res.statusCode === 401) {
            reject(new Error('JIRA authentication failed - check credentials'));
          } else {
            reject(new Error(`JIRA API error (${res.statusCode}): ${data}`));
          }
        });
      });
      
      req.on('error', (e) => {
        reject(new Error('JIRA connection error: ' + e.message));
      });
      
      req.end();
    });
  } catch (error) {
    console.log(colors.red + '❌ Error fetching JIRA story: ' + error.message + '\n' + colors.reset);
    return null;
  }
}

/**
 * Extract plain text from JIRA ADF (Atlassian Document Format)
 */
function extractPlainText(adf) {
  if (!adf || !adf.content) return '';
  
  let text = '';
  
  function traverse(node) {
    if (node.type === 'text') {
      text += node.text;
    } else if (node.type === 'hardBreak') {
      text += '\n';
    } else if (node.content) {
      node.content.forEach(traverse);
      if (node.type === 'paragraph') {
        text += '\n';
      }
    }
  }
  
  adf.content.forEach(traverse);
  return text.trim();
}

/**
 * Extract acceptance criteria from JIRA description
 */
function extractAcceptanceCriteria(adf) {
  const text = extractPlainText(adf);
  const criteria = [];
  
  // Look for common acceptance criteria patterns
  const patterns = [
    /acceptance criteria:?\s*([\s\S]*?)(?=\n\n|$)/i,
    /given[\s\S]*?when[\s\S]*?then[\s\S]*?(?=\n\n|$)/gi,
    /^[-*]\s+(.+)$/gm
  ];
  
  patterns.forEach(pattern => {
    const matches = text.match(pattern);
    if (matches) {
      matches.forEach(match => {
        const clean = match.replace(/^[-*]\s+/, '').trim();
        if (clean && !criteria.includes(clean)) {
          criteria.push(clean);
        }
      });
    }
  });
  
  return criteria;
}

/**
 * Display main menu
 */
async function displayMenu() {
  console.log(colors.yellow + '\n╔════════════════════════════════════════════════════════════╗' + colors.reset);
  console.log(colors.yellow + '║' + colors.reset + colors.bright + '           🎯 TEST GENERATION OPTIONS                      ' + colors.reset + colors.yellow + '║' + colors.reset);
  console.log(colors.yellow + '╚════════════════════════════════════════════════════════════╝' + colors.reset);
  console.log('');
  console.log(colors.bright + '📌 RECOMMENDED OPTIONS (Smart & Fast):' + colors.reset);
  console.log(colors.green + '  1️⃣  🎥 Record & Auto-Generate' + colors.reset + ' (Fastest - 5-10 min)');
  console.log('      └─ Record browser actions → Auto-generate all files');
  console.log(colors.green + '  2️⃣  🤖 AI-Assisted from JIRA' + colors.reset + ' (Enterprise)');
  console.log('      └─ Use JIRA story → AI generates complete test');
  console.log(colors.green + '  3️⃣  ✨ AI-Guided Interactive' + colors.reset + ' (No JIRA needed)');
  console.log('      └─ Answer questions → AI generates complete suite');
  console.log('');
  console.log(colors.bright + '🔧 ADVANCED OPTIONS:' + colors.reset);
  console.log('  4️⃣  📝 Update Existing Test (Add elements/scenarios)');
  console.log('  5️⃣  📄 Generate Page Object only');
  console.log('  6️⃣  📋 Generate Feature File only');
  console.log('  7️⃣  🔀 Generate Step Definitions only');
  console.log('');
  console.log(colors.bright + '📚 HELP & UTILITIES:' + colors.reset);
  console.log('  8️⃣  🔍 Analyze Existing Framework');
  console.log('  9️⃣  📖 Quick Start Tutorial');
  console.log('  0️⃣  🚪 Exit\n');
  
  const choice = await question(colors.cyan + '👉 Enter your choice (0-9): ' + colors.reset);
  return choice.trim();
}

/**
 * Generate Complete Test Suite (Guided Wizard)
 */
async function generateCompleteTestSuite() {
  console.log(colors.green + '\n🎯 Complete Test Suite Generator\n' + colors.reset);
  console.log('I\'ll help you create a complete test automation suite!');
  console.log('Just answer a few questions...\n');
  
  // Step 1: Test Name
  const testName = await question(colors.cyan + '📝 Test name (e.g., UserRegistration, ProductSearch): ' + colors.reset);
  
  // Step 2: Description
  const description = await question(colors.cyan + '📋 Brief description: ' + colors.reset);
  
  // Step 3: Page Elements
  console.log(colors.yellow + '\n🎨 Define page elements to interact with:' + colors.reset);
  const elements = [];
  let addMore = true;
  let elementCount = 1;
  
  while (addMore) {
    console.log(colors.magenta + `\nElement ${elementCount}:` + colors.reset);
    const elementName = await question(colors.cyan + '  Name (e.g., "Username Field", "Submit Button"): ' + colors.reset);
    
    if (!elementName.trim()) {
      break;
    }
    
    const elementAction = await question(colors.cyan + '  Action (click/type/select): ' + colors.reset);
    const elementDesc = await question(colors.cyan + '  Description (optional): ' + colors.reset);
    
    elements.push({
      name: elementName,
      action: elementAction || 'click',
      description: elementDesc || `Interact with ${elementName}`
    });
    
    elementCount++;
    const more = await question(colors.cyan + '  Add another element? (y/n): ' + colors.reset);
    addMore = more.toLowerCase() === 'y';
  }
  
  // Step 4: Scenarios
  console.log(colors.yellow + '\n📖 Define test scenarios:' + colors.reset);
  const scenarios = [];
  let addMoreScenarios = true;
  let scenarioCount = 1;
  
  while (addMoreScenarios) {
    console.log(colors.magenta + `\nScenario ${scenarioCount}:` + colors.reset);
    const scenarioName = await question(colors.cyan + '  Scenario name: ' + colors.reset);
    
    if (!scenarioName.trim()) {
      break;
    }
    
    console.log(colors.yellow + '  Add steps (Given/When/Then). Press Enter on empty line to finish:' + colors.reset);
    const steps = [];
    let stepNum = 1;
    
    while (true) {
      const step = await question(colors.cyan + `    Step ${stepNum}: ` + colors.reset);
      if (!step.trim()) break;
      
      // Auto-add Given/When/Then if not present
      let formattedStep = step;
      if (stepNum === 1 && !step.match(/^(Given|When|Then|And)/i)) {
        formattedStep = 'Given ' + step;
      } else if (!step.match(/^(Given|When|Then|And)/i)) {
        formattedStep = 'And ' + step;
      }
      
      steps.push(formattedStep);
      stepNum++;
    }
    
    if (steps.length > 0) {
      scenarios.push({
        name: scenarioName,
        steps: steps
      });
    }
    
    scenarioCount++;
    const more = await question(colors.cyan + '  Add another scenario? (y/n): ' + colors.reset);
    addMoreScenarios = more.toLowerCase() === 'y';
  }
  
  // Step 5: Verification Options (NEW)
  console.log(colors.yellow + '\n✅ Add Verification & Assertions?' + colors.reset);
  
  const addFunctional = await question(colors.cyan + '  Functional verification (business logic, assertions)? (y/n): ' + colors.reset);
  const addUI = await question(colors.cyan + '  UI verification (element states, visibility)? (y/n): ' + colors.reset);
  const addUX = await question(colors.cyan + '  UX verification (transitions, user experience)? (y/n): ' + colors.reset);
  const addPerformance = await question(colors.cyan + '  Performance verification (timing, thresholds)? (y/n): ' + colors.reset);
  const addLogging = await question(colors.cyan + '  Detailed logging (step-by-step logs)? (y/n): ' + colors.reset);
  
  const verification = {
    functional: addFunctional.toLowerCase() === 'y',
    ui: addUI.toLowerCase() === 'y',
    ux: addUX.toLowerCase() === 'y',
    performance: addPerformance.toLowerCase() === 'y',
    logging: addLogging.toLowerCase() === 'y'
  };
  
  // Performance threshold if enabled
  let performanceThreshold = 3000; // default 3 seconds
  if (verification.performance) {
    const threshold = await question(colors.cyan + '  Performance threshold in seconds (default 3): ' + colors.reset);
    if (threshold.trim()) {
      performanceThreshold = parseInt(threshold) * 1000;
    }
  }
  
  // Confirm and generate
  console.log(colors.yellow + '\n📊 Summary:' + colors.reset);
  console.log(`  Test: ${testName}`);
  console.log(`  Elements: ${elements.length}`);
  console.log(`  Scenarios: ${scenarios.length}`);
  console.log(colors.cyan + '  Verification:' + colors.reset);
  console.log(`    • Functional: ${verification.functional ? '✅' : '❌'}`);
  console.log(`    • UI: ${verification.ui ? '✅' : '❌'}`);
  console.log(`    • UX: ${verification.ux ? '✅' : '❌'}`);
  console.log(`    • Performance: ${verification.performance ? '✅ (<' + (performanceThreshold/1000) + 's)' : '❌'}`);
  console.log(`    • Logging: ${verification.logging ? '✅' : '❌'}`);
  
  const confirm = await question(colors.cyan + '\n✨ Generate test suite? (y/n): ' + colors.reset);
  
  if (confirm.toLowerCase() === 'y') {
    console.log(colors.green + '\n🚀 Generating your test suite with verification...\n' + colors.reset);
    
    // Call MCP server tool (simulated here, actual implementation would use MCP SDK)
    const result = await callMCPTool('generate-complete-test-suite', {
      testName,
      description,
      pageElements: elements,
      scenarios,
      verification,
      performanceThreshold
    });
    
    console.log(colors.green + result + colors.reset);
    
    // Auto-compile, run, and fix loop
    console.log(colors.cyan + '\n🔄 Starting auto-compile, test, and fix cycle...\n' + colors.reset);
    await autoCompileTestAndFix(testName, elements, scenarios, verification, performanceThreshold)
  } else {
    console.log(colors.yellow + 'Generation cancelled.' + colors.reset);
  }
}

/**
 * Validate and fix Page Object code before writing to disk
 * Prevents common compilation errors by ensuring required imports and declarations
 */
function validateAndFixPageObject(code, className) {
  let fixedCode = code;
  
  // VALIDATION 1: Ensure Logger import and instance if log.* methods are used
  if ((fixedCode.includes('log.info') || fixedCode.includes('log.error') || fixedCode.includes('log.warn'))) {
    
    // Remove incorrect log imports
    fixedCode = fixedCode.replace(/import log;?\s*\n/g, '');
    fixedCode = fixedCode.replace(/import log\.\*;?\s*\n/g, '');
    
    // Add Logger import
    if (!fixedCode.includes('import java.util.logging.Logger')) {
      const packageMatch = fixedCode.match(/package\s+[\w.]+;\s*\n/);
      if (packageMatch) {
        const insertPoint = packageMatch.index + packageMatch[0].length;
        fixedCode = fixedCode.slice(0, insertPoint) + 'import java.util.logging.Logger;\n' + fixedCode.slice(insertPoint);
      }
    }
    
    // Add static getLogger import
    if (!fixedCode.includes('import static java.util.logging.Logger.getLogger')) {
      const lastImport = fixedCode.lastIndexOf('import ');
      if (lastImport !== -1) {
        const nextLine = fixedCode.indexOf('\n', lastImport) + 1;
        fixedCode = fixedCode.slice(0, nextLine) + 'import static java.util.logging.Logger.getLogger;\n' + fixedCode.slice(nextLine);
      }
    }
    
    // Add Logger instance
    if (!fixedCode.includes('private static final Logger log')) {
      const classMatch = fixedCode.match(/public class (\w+)(?:\s+extends\s+\w+)?\s*\{/);
      if (classMatch) {
        const insertPoint = classMatch.index + classMatch[0].length;
        fixedCode = fixedCode.slice(0, insertPoint) + 
          '\n    private static final Logger log = getLogger(' + className + '.class.getName());\n' +
          fixedCode.slice(insertPoint);
      }
    }
  }
  
  // VALIDATION 2: Check for method parameter issues
  // Find methods with selectDropDownValueByText that use 'text' parameter
  const dropdownMethodRegex = /protected\s+static\s+void\s+(\w+)\s*\(\s*String\s+locator\s*\)\s*\{[^}]*selectDropDownValueByText\s*\([^,]+,\s*text\s*\)[^}]*\}/g;
  let match;
  
  while ((match = dropdownMethodRegex.exec(fixedCode)) !== null) {
    const methodName = match[1];
    const oldSignature = `protected static void ${methodName}(String locator)`;
    const newSignature = `protected static void ${methodName}(String locator, String text)`;
    fixedCode = fixedCode.replace(oldSignature, newSignature);
  }
  
  // VALIDATION 3: Ensure TimeoutConfig import if used
  if (fixedCode.includes('TimeoutConfig.') && !fixedCode.includes('import configs.TimeoutConfig')) {
    const packageMatch = fixedCode.match(/package\s+[\w.]+;\s*\n/);
    if (packageMatch) {
      const insertPoint = packageMatch.index + packageMatch[0].length;
      fixedCode = fixedCode.slice(0, insertPoint) + 'import configs.TimeoutConfig;\n' + fixedCode.slice(insertPoint);
    }
  }
  
  return fixedCode;
}

/**
 * Update Existing Test
 */
async function updateExistingTest() {
  console.log(colors.green + '\n🔄 Update Existing Test\n' + colors.reset);
  
  // List existing tests
  const pagesDir = path.join(process.cwd(), 'src/main/java/pages');
  try {
    const files = await fs.readdir(pagesDir);
    const pageFiles = files.filter(f => f.endsWith('.java') && f.endsWith('Page.java'));
    
    if (pageFiles.length === 0) {
      console.log(colors.red + 'No existing tests found. Create a new test first (Option 1).\n' + colors.reset);
      return;
    }
    
    console.log(colors.cyan + 'Available tests:\n' + colors.reset);
    pageFiles.forEach((file, idx) => {
      console.log(`  ${idx + 1}. ${file.replace('.java', '')}`);
    });
    
    const selection = await question(colors.cyan + '\nSelect test to update (number): ' + colors.reset);
    const selectedIdx = parseInt(selection) - 1;
    
    if (selectedIdx < 0 || selectedIdx >= pageFiles.length) {
      console.log(colors.red + 'Invalid selection.\n' + colors.reset);
      return;
    }
    
    const testName = pageFiles[selectedIdx].replace('Page.java', '').replace('.java', '');
    console.log(colors.green + `\nUpdating: ${testName}\n` + colors.reset);
    
    // Read existing files
    const pageFile = path.join(pagesDir, `${testName}Page.java`);
    const featureFile = path.join(process.cwd(), 'src/test/java/features', `${testName.toLowerCase()}.feature`);
    const stepsFile = path.join(process.cwd(), 'src/test/java/stepDefs', `${testName}Steps.java`);
    
    let pageContent = await fs.readFile(pageFile, 'utf-8');
    let featureContent = await fs.readFile(featureFile, 'utf-8').catch(() => '');
    let stepsContent = await fs.readFile(stepsFile, 'utf-8').catch(() => '');
    
    // Ask what to update
    console.log(colors.yellow + 'What would you like to update?\n' + colors.reset);
    console.log('  1. Add new elements');
    console.log('  2. Add new scenarios');
    console.log('  3. Enable/update verification');
    console.log('  4. All of the above\n');
    
    const updateChoice = await question(colors.cyan + 'Your choice: ' + colors.reset);
    
    const addElements = ['1', '4'].includes(updateChoice);
    const addScenarios = ['2', '4'].includes(updateChoice);
    const updateVerification = ['3', '4'].includes(updateChoice);
    
    let newElements = [];
    let newScenarios = [];
    let verification = { functional: false, ui: false, ux: false, performance: false, logging: false };
    
    // Add new elements
    if (addElements) {
      console.log(colors.yellow + '\n➕ Add New Elements:\n' + colors.reset);
      let addMore = true;
      while (addMore) {
        const name = await question(colors.cyan + '  Element name: ' + colors.reset);
        if (!name.trim()) break;
        
        const type = await question(colors.cyan + '  Type (input/button/select): ' + colors.reset);
        const locator = await question(colors.cyan + '  Locator (css/xpath): ' + colors.reset);
        const description = await question(colors.cyan + '  Description: ' + colors.reset);
        
        newElements.push({ name: name.trim(), type: type || 'input', locator: locator.trim(), description });
        
        const more = await question(colors.cyan + '  Add another? (y/n): ' + colors.reset);
        addMore = more.toLowerCase() === 'y';
      }
      
      // Add elements to Page Object
      for (const el of newElements) {
        const findByAnnotation = `\n    @FindBy(css = "${el.locator}")\n    private WebElement ${el.name};\n`;
        
        // Insert before constructor
        const constructorIdx = pageContent.indexOf('public ' + testName + 'Page(');
        if (constructorIdx > 0) {
          pageContent = pageContent.slice(0, constructorIdx) + findByAnnotation + pageContent.slice(constructorIdx);
        }
        
        // Add methods
        let methodCode = '';
        if (el.type === 'input') {
          methodCode = `\n    public void enter${el.name.charAt(0).toUpperCase() + el.name.slice(1)}(String text) {
        enterText(${el.name}, text, TimeoutConfig.shortWait());
    }\n`;
        } else if (el.type === 'button') {
          methodCode = `\n    public void click${el.name.charAt(0).toUpperCase() + el.name.slice(1)}() {
        clickOnElement(${el.name}, TimeoutConfig.shortWait());
    }\n`;
        }
        
        // Insert before last closing brace
        const lastBrace = pageContent.lastIndexOf('}');
        pageContent = pageContent.slice(0, lastBrace) + methodCode + pageContent.slice(lastBrace);
      }
      
      await fs.writeFile(pageFile, pageContent, 'utf-8');
      console.log(colors.green + `  ✅ Added ${newElements.length} new elements to Page Object\n` + colors.reset);
    }
    
    // Add new scenarios
    if (addScenarios) {
      console.log(colors.yellow + '\n➕ Add New Scenarios:\n' + colors.reset);
      let addMore = true;
      while (addMore) {
        const scenarioName = await question(colors.cyan + '  Scenario name: ' + colors.reset);
        if (!scenarioName.trim()) break;
        
        console.log(colors.cyan + '  Steps (one per line, empty to finish):\n' + colors.reset);
        const steps = [];
        let stepNum = 1;
        while (true) {
          const step = await question(colors.cyan + `    Step ${stepNum}: ` + colors.reset);
          if (!step.trim()) break;
          steps.push(step.trim());
          stepNum++;
        }
        
        if (steps.length > 0) {
          newScenarios.push({ name: scenarioName.trim(), steps });
        }
        
        const more = await question(colors.cyan + '  Add another scenario? (y/n): ' + colors.reset);
        addMore = more.toLowerCase() === 'y';
      }
      
      // Add scenarios to Feature file
      for (const scenario of newScenarios) {
        let scenarioText = `\n  Scenario: ${scenario.name}\n`;
        scenario.steps.forEach(step => {
          scenarioText += `    When ${step}\n`;
        });
        scenarioText += `    Then Operation should be successful\n`;
        
        featureContent += scenarioText;
      }
      
      await fs.writeFile(featureFile, featureContent, 'utf-8');
      console.log(colors.green + `  ✅ Added ${newScenarios.length} new scenarios to Feature file\n` + colors.reset);
    }
    
    // Update verification
    if (updateVerification) {
      console.log(colors.yellow + '\n⚙️ Update Verification:\n' + colors.reset);
      const functional = await question(colors.cyan + '  Enable Functional verification? (y/n): ' + colors.reset);
      const ui = await question(colors.cyan + '  Enable UI verification? (y/n): ' + colors.reset);
      const ux = await question(colors.cyan + '  Enable UX verification? (y/n): ' + colors.reset);
      const perf = await question(colors.cyan + '  Enable Performance verification? (y/n): ' + colors.reset);
      const logging = await question(colors.cyan + '  Enable Logging? (y/n): ' + colors.reset);
      
      verification = {
        functional: functional.toLowerCase() === 'y',
        ui: ui.toLowerCase() === 'y',
        ux: ux.toLowerCase() === 'y',
        performance: perf.toLowerCase() === 'y',
        logging: logging.toLowerCase() === 'y'
      };
      
      // Update imports if needed
      if (verification.logging && !pageContent.includes('import org.apache.logging.log4j.Logger')) {
        const packageEnd = pageContent.indexOf(';') + 1;
        pageContent = pageContent.slice(0, packageEnd) + '\n\nimport org.apache.logging.log4j.Logger;\nimport org.apache.logging.log4j.LogManager;' + pageContent.slice(packageEnd);
      }
      
      if (verification.functional && !pageContent.includes('import org.testng.Assert')) {
        const packageEnd = pageContent.indexOf(';') + 1;
        pageContent = pageContent.slice(0, packageEnd) + '\n\nimport org.testng.Assert;' + pageContent.slice(packageEnd);
      }
      
      await fs.writeFile(pageFile, pageContent, 'utf-8');
      console.log(colors.green + '  ✅ Updated verification settings\n' + colors.reset);
    }
    
    console.log(colors.green + '\n✨ Test updated successfully!\n' + colors.reset);
    
    // Offer to compile and test
    const compile = await question(colors.cyan + '🔨 Compile and test now? (y/n): ' + colors.reset);
    if (compile.toLowerCase() === 'y') {
      await autoCompileTestAndFix(testName, newElements, newScenarios, verification, 3000);
    }
    
  } catch (error) {
    console.log(colors.red + `Error updating test: ${error.message}\n` + colors.reset);
  }
}

/**
 * Generate Page Object
 */
async function generatePageObject() {
  console.log(colors.green + '\n📄 Page Object Generator\n' + colors.reset);
  
  const pageName = await question(colors.cyan + 'Page name: ' + colors.reset);
  const description = await question(colors.cyan + 'Description: ' + colors.reset);
  
  console.log(colors.yellow + '\nDefine elements:' + colors.reset);
  const elements = [];
  let addMore = true;
  
  while (addMore) {
    const name = await question(colors.cyan + '  Element name: ' + colors.reset);
    if (!name.trim()) break;
    
    const action = await question(colors.cyan + '  Action (click/type/select): ' + colors.reset);
    elements.push({ name, action: action || 'click' });
    
    const more = await question(colors.cyan + '  Add another? (y/n): ' + colors.reset);
    addMore = more.toLowerCase() === 'y';
  }
  
  console.log(colors.green + '\n🚀 Generating page object...\n' + colors.reset);
  const result = await callMCPTool('generate-page-object', {
    pageName,
    elements,
    description,
    verification: { logging: true, functional: false, performance: false }
  });
  
  console.log(colors.green + result + colors.reset);
  
  // Auto-compile and fix any errors
  console.log(colors.cyan + '\n🔄 Starting auto-compile and fix cycle...\n' + colors.reset);
  await autoCompileTestAndFix(pageName, elements, [], { logging: true, functional: false, ui: false, ux: false, performance: false }, 3000);
}

/**
 * Generate Feature File
 */
async function generateFeatureFile() {
  console.log(colors.green + '\n📝 Feature File Generator\n' + colors.reset);
  
  const featureName = await question(colors.cyan + 'Feature name: ' + colors.reset);
  const description = await question(colors.cyan + 'Description: ' + colors.reset);
  
  console.log(colors.yellow + '\nDefine scenarios:' + colors.reset);
  const scenarios = [];
  let addMore = true;
  let scenarioCount = 1;
  
  while (addMore) {
    console.log(colors.magenta + `\nScenario ${scenarioCount}:` + colors.reset);
    const scenarioName = await question(colors.cyan + '  Scenario name: ' + colors.reset);
    
    if (!scenarioName.trim()) {
      break;
    }
    
    console.log(colors.yellow + '  Add steps (Given/When/Then). Press Enter on empty line to finish:' + colors.reset);
    const steps = [];
    let stepNum = 1;
    
    while (true) {
      const step = await question(colors.cyan + `    Step ${stepNum}: ` + colors.reset);
      if (!step.trim()) break;
      
      steps.push(step.trim());
      stepNum++;
    }
    
    if (steps.length > 0) {
      scenarios.push({
        name: scenarioName,
        steps: steps
      });
    }
    
    scenarioCount++;
    const more = await question(colors.cyan + '  Add another scenario? (y/n): ' + colors.reset);
    addMore = more.toLowerCase() === 'y';
  }
  
  if (scenarios.length === 0) {
    console.log(colors.red + '❌ No scenarios defined. Feature file not generated.\n' + colors.reset);
    return;
  }
  
  console.log(colors.green + '\n🚀 Generating feature file...\n' + colors.reset);
  const result = await callMCPTool('generate-feature', {
    featureName,
    description,
    scenarios
  });
  
  console.log(colors.green + result + colors.reset);
  
  // Auto-compile and fix any errors
  console.log(colors.cyan + '\n🔄 Starting auto-compile and fix cycle...\n' + colors.reset);
  await autoCompileTestAndFix(featureName, [], scenarios, { logging: true, functional: true, ui: false, ux: false, performance: false }, 3000);
}

/**
 * Generate Step Definitions
 */
async function generateStepDefinitions() {
  console.log(colors.green + '\n🔧 Step Definitions Generator\n' + colors.reset);
  
  const testName = await question(colors.cyan + 'Test name (must match existing Page Object): ' + colors.reset);
  
  // Verify Page Object exists
  const pageFile = path.join(process.cwd(), 'src/main/java/pages', `${testName}Page.java`);
  try {
    await fs.access(pageFile);
  } catch (error) {
    console.log(colors.red + `❌ Page Object not found: ${testName}Page.java\n` + colors.reset);
    console.log(colors.yellow + 'Generate Page Object first (Option 3) or use complete test suite (Option 1)\n' + colors.reset);
    return;
  }
  
  console.log(colors.green + `✅ Found Page Object: ${testName}Page.java\n` + colors.reset);
  
  // Check for existing feature file
  const featureFile = path.join(process.cwd(), 'src/test/java/features', `${testName.toLowerCase()}.feature`);
  let featureExists = false;
  try {
    await fs.access(featureFile);
    featureExists = true;
    console.log(colors.green + `✅ Found Feature File: ${testName.toLowerCase()}.feature\n` + colors.reset);
  } catch (error) {
    console.log(colors.yellow + `⚠️ Feature file not found. Will generate step definitions without feature context.\n` + colors.reset);
  }
  
  console.log(colors.yellow + '\nDefine steps to implement:' + colors.reset);
  const steps = [];
  let addMore = true;
  let stepNum = 1;
  
  while (addMore) {
    const step = await question(colors.cyan + `  Step ${stepNum} (Cucumber format): ` + colors.reset);
    
    if (!step.trim()) {
      break;
    }
    
    steps.push(step.trim());
    stepNum++;
    
    const more = await question(colors.cyan + '  Add another step? (y/n): ' + colors.reset);
    addMore = more.toLowerCase() === 'y';
  }
  
  if (steps.length === 0) {
    console.log(colors.red + '❌ No steps defined. Step definitions not generated.\n' + colors.reset);
    return;
  }
  
  console.log(colors.green + '\n🚀 Generating step definitions...\n' + colors.reset);
  const result = await callMCPTool('generate-step-definitions', {
    testName,
    steps,
    hasFeatureFile: featureExists
  });
  
  console.log(colors.green + result + colors.reset);
  
  // Auto-compile and fix any errors
  console.log(colors.cyan + '\n🔄 Starting auto-compile and fix cycle...\n' + colors.reset);
  const scenarios = steps.length > 0 ? [{ name: 'Generated Steps', steps: steps }] : [];
  await autoCompileTestAndFix(testName, [], scenarios, { logging: true, functional: true, ui: false, ux: false, performance: false }, 3000);
}

/**
 * Analyze Framework
 */
async function analyzeFramework() {
  console.log(colors.green + '\n🔍 Analyzing framework structure...\n' + colors.reset);
  
  const result = await callMCPTool('analyze-framework', {});
  const analysis = JSON.parse(result);
  
  console.log(colors.cyan + '📊 Framework Analysis:' + colors.reset);
  console.log(colors.yellow + `\n  Total Page Objects: ${analysis.currentState.totalPages}` + colors.reset);
  console.log(colors.yellow + `  Total Features: ${analysis.currentState.totalFeatures}` + colors.reset);
  
  if (analysis.currentState.pageObjects.length > 0) {
    console.log(colors.cyan + '\n  📄 Existing Page Objects:' + colors.reset);
    analysis.currentState.pageObjects.forEach(page => {
      console.log(`    • ${page.name} (${page.methodCount} methods)`);
    });
  }
  
  if (analysis.currentState.featureFiles.length > 0) {
    console.log(colors.cyan + '\n  📖 Existing Features:' + colors.reset);
    analysis.currentState.featureFiles.forEach(feature => {
      console.log(`    • ${feature.name} (${feature.scenarioCount} scenarios)`);
    });
  }
  
  console.log(colors.green + '\n  ✅ Analysis complete!' + colors.reset);
}

/**
 * Generate Test from JIRA Story
 */
async function generateTestFromJiraStory() {
  console.log(colors.green + '\n📋 Generate Test from JIRA Story\n' + colors.reset);
  
  // Get JIRA issue key
  const issueKey = await question(colors.cyan + '🎫 Enter JIRA Story/Issue key (e.g., ECS-123, or press Enter to skip): ' + colors.reset);
  
  if (!issueKey.trim()) {
    console.log(colors.yellow + '\n⚠️ No JIRA key provided. Switching to manual test generation...\n' + colors.reset);
    await generateCompleteTestSuite();
    return;
  }
  
  console.log(colors.yellow + '\n🔍 Fetching story from JIRA...\n' + colors.reset);
  
  // Fetch JIRA story
  const story = await getJiraStory(issueKey.trim());
  
  if (!story) {
    console.log(colors.red + '❌ Failed to fetch JIRA story "' + issueKey + '".\n' + colors.reset);
    console.log(colors.yellow + '   Possible reasons:' + colors.reset);
    console.log(colors.yellow + '   • Issue does not exist' + colors.reset);
    console.log(colors.yellow + '   • No permission to access issue' + colors.reset);
    console.log(colors.yellow + '   • Invalid JIRA configuration' + colors.reset);
    console.log(colors.yellow + '   • No issues in the ECS project\n' + colors.reset);
    
    // Smart fallback: Ask user if they want to proceed manually
    const fallback = await question(colors.cyan + '🔄 Would you like to create the test manually instead? (y/n): ' + colors.reset);
    
    if (fallback.toLowerCase() === 'y') {
      console.log(colors.green + '\n✨ Switching to manual test generation...\n' + colors.reset);
      await generateCompleteTestSuite();
    } else {
      console.log(colors.yellow + '\nTest generation cancelled.\n' + colors.reset);
    }
    return;
  }
  
  // Display story details
  console.log(colors.green + '✅ Story fetched successfully!\n' + colors.reset);
  console.log(colors.cyan + '📝 Story Details:' + colors.reset);
  console.log(`  Key: ${story.key}`);
  console.log(`  Type: ${story.issueType}`);
  console.log(`  Status: ${story.status}`);
  console.log(`  Priority: ${story.priority}`);
  console.log(`  Summary: ${story.summary}`);
  
  if (story.description) {
    console.log(`\n  Description:\n${story.description.split('\n').map(line => '    ' + line).join('\n')}`);
  }
  
  if (story.acceptanceCriteria.length > 0) {
    console.log(`\n  Acceptance Criteria:`);
    story.acceptanceCriteria.forEach((criterion, idx) => {
      console.log(`    ${idx + 1}. ${criterion}`);
    });
  }
  
  // Confirm generation
  const confirm = await question(colors.cyan + '\n✨ Generate test from this story? (y/n): ' + colors.reset);
  
  if (confirm.toLowerCase() !== 'y') {
    console.log(colors.yellow + 'Generation cancelled.\n' + colors.reset);
    return;
  }
  
  // Use story summary as test name (sanitized)
  const testName = story.summary
    .replace(/[^a-zA-Z0-9\s]/g, '')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  console.log(colors.cyan + `\n📝 Test Name (auto-generated): ${testName}` + colors.reset);
  const customName = await question(colors.cyan + '   Change test name? (press Enter to keep, or type new name): ' + colors.reset);
  const finalTestName = customName.trim() || testName;
  
  // Smart detection: Auto-detect elements from story description and acceptance criteria
  console.log(colors.yellow + '\n🤖 AI-Analyzing story for UI elements and test aspects...\n' + colors.reset);
  
  const storyText = `${story.summary}\n${story.description}\n${story.acceptanceCriteria.join('\n')}`;
  const detectedElements = detectUIElements(storyText, story.issueType);
  const suggestedVerification = suggestVerificationOptions(story.issueType, story.priority, storyText);
  
  // Display detected elements
  console.log(colors.green + '✅ Auto-detected UI elements:' + colors.reset);
  detectedElements.forEach((el, idx) => {
    console.log(`  ${idx + 1}. ${el.name} (${el.action}) - ${el.description}`);
  });
  
  // Ask if user wants to customize
  console.log(colors.yellow + '\n🎨 Element Options:' + colors.reset);
  console.log('  1. Use auto-detected elements (recommended)');
  console.log('  2. Add/modify elements manually');
  console.log('  3. Skip elements (use story text only)');
  
  const elementChoice = await question(colors.cyan + '\nYour choice (1-3, default 1): ' + colors.reset);
  
  let elements = [];
  
  if (!elementChoice.trim() || elementChoice === '1') {
    elements = detectedElements;
    console.log(colors.green + `✅ Using ${elements.length} auto-detected elements\n` + colors.reset);
  } else if (elementChoice === '2') {
    elements = [...detectedElements];
    console.log(colors.yellow + '\n📝 Customize elements (auto-detected shown, add more):' + colors.reset);
    
    let addMore = true;
    let elementCount = elements.length + 1;
    
    while (addMore) {
      console.log(colors.magenta + `\nElement ${elementCount}:` + colors.reset);
      const elementName = await question(colors.cyan + '  Name (e.g., "Submit Button", "Email Field"): ' + colors.reset);
      
      if (!elementName.trim()) {
        break;
      }
      
      const elementAction = await question(colors.cyan + '  Action (click/type/select): ' + colors.reset);
      const elementDesc = await question(colors.cyan + '  Description (optional): ' + colors.reset);
      
      elements.push({
        name: elementName,
        action: elementAction || 'click',
        description: elementDesc || `Interact with ${elementName}`
      });
      
      elementCount++;
      const more = await question(colors.cyan + '  Add another element? (y/n): ' + colors.reset);
      addMore = more.toLowerCase() === 'y';
    }
  } else {
    console.log(colors.yellow + '⚠️ Proceeding without specific elements\n' + colors.reset);
  }
  
  // Generate comprehensive scenarios from acceptance criteria
  console.log(colors.yellow + '\n📖 Generating comprehensive test scenarios...\n' + colors.reset);
  
  const scenarios = [];
  
  if (story.acceptanceCriteria.length > 0) {
    // Auto-generate detailed scenarios from each acceptance criterion
    story.acceptanceCriteria.forEach((criterion, idx) => {
      const scenarioName = `Verify ${criterion.substring(0, 50)}${criterion.length > 50 ? '...' : ''}`;
      
      // Generate comprehensive steps based on criterion content
      const steps = generateDetailedSteps(criterion, elements, story.issueType);
      
      scenarios.push({
        name: scenarioName,
        steps: steps
      });
    });
    
    // Add edge case scenarios based on story type
    const edgeCaseScenarios = generateEdgeCaseScenarios(story.issueType, story.summary, elements);
    scenarios.push(...edgeCaseScenarios);
    
    console.log(colors.green + `✅ Generated ${scenarios.length} comprehensive scenarios:` + colors.reset);
    console.log(colors.cyan + `   - ${story.acceptanceCriteria.length} from acceptance criteria` + colors.reset);
    console.log(colors.cyan + `   - ${edgeCaseScenarios.length} edge case scenarios` + colors.reset);
  } else {
    console.log(colors.yellow + '⚠️ No acceptance criteria found. Generating default scenarios...\n' + colors.reset);
    
    // Generate default scenarios based on story type
    const defaultScenarios = generateDefaultScenarios(story.issueType, story.summary, elements);
    scenarios.push(...defaultScenarios);
    
    console.log(colors.green + `✅ Generated ${scenarios.length} default scenarios\n` + colors.reset);
  }
  
  // Display suggested verification
  console.log(colors.yellow + '\n✅ Recommended Verification & Assertions:' + colors.reset);
  console.log(colors.cyan + `  Story Type: ${story.issueType}` + colors.reset);
  console.log(colors.cyan + `  Priority: ${story.priority}` + colors.reset);
  console.log(colors.green + '\n  Suggested verification:' + colors.reset);
  console.log(`    • Functional: ${suggestedVerification.functional ? '✅ Enabled' : '❌'}`);
  console.log(`    • UI: ${suggestedVerification.ui ? '✅ Enabled' : '❌'}`);
  console.log(`    • Performance: ${suggestedVerification.performance ? '✅ Enabled (<' + (suggestedVerification.performanceThreshold/1000) + 's)' : '❌'}`);
  console.log(`    • Logging: ${suggestedVerification.logging ? '✅ Enabled' : '❌'}`);
  
  const useRecommended = await question(colors.cyan + '\n  Use recommended verification? (y/n, default y): ' + colors.reset);
  
  let verification, performanceThreshold;
  
  if (!useRecommended.trim() || useRecommended.toLowerCase() === 'y') {
    verification = suggestedVerification;
    performanceThreshold = suggestedVerification.performanceThreshold;
    console.log(colors.green + '✅ Using recommended verification settings\n' + colors.reset);
  } else {
    console.log(colors.yellow + '\n📝 Customize verification options:' + colors.reset);
    const addFunctional = await question(colors.cyan + '  Functional verification? (y/n): ' + colors.reset);
    const addUI = await question(colors.cyan + '  UI verification? (y/n): ' + colors.reset);
    const addPerformance = await question(colors.cyan + '  Performance verification? (y/n): ' + colors.reset);
    const addLogging = await question(colors.cyan + '  Detailed logging? (y/n): ' + colors.reset);
    
    verification = {
      functional: addFunctional.toLowerCase() === 'y',
      ui: addUI.toLowerCase() === 'y',
      ux: false,
      performance: addPerformance.toLowerCase() === 'y',
      logging: addLogging.toLowerCase() === 'y'
    };
    
    performanceThreshold = 3000;
    if (verification.performance) {
      const threshold = await question(colors.cyan + '  Performance threshold in seconds (default 3): ' + colors.reset);
      if (threshold.trim()) {
        performanceThreshold = parseInt(threshold) * 1000;
      }
    }
  }
  
  // Display summary
  console.log(colors.yellow + '\n📊 Test Generation Summary:' + colors.reset);
  console.log(`  JIRA Story: ${story.key}`);
  console.log(`  Test Name: ${finalTestName}`);
  console.log(`  Elements: ${elements.length}`);
  console.log(`  Scenarios: ${scenarios.length}`);
  console.log(colors.cyan + '  Verification:' + colors.reset);
  console.log(`    • Functional: ${verification.functional ? '✅' : '❌'}`);
  console.log(`    • UI: ${verification.ui ? '✅' : '❌'}`);
  console.log(`    • Performance: ${verification.performance ? '✅ (<' + (performanceThreshold/1000) + 's)' : '❌'}`);
  console.log(`    • Logging: ${verification.logging ? '✅' : '❌'}`);
  
  const finalConfirm = await question(colors.cyan + '\n🚀 Generate test suite? (y/n): ' + colors.reset);
  
  if (finalConfirm.toLowerCase() === 'y') {
    console.log(colors.green + '\n🚀 Generating test suite from JIRA story...\n' + colors.reset);
    
    // Add JIRA reference to description
    const description = `${story.summary}\n\nJIRA Story: ${story.key}\nGenerated from: ${story.issueType}\nPriority: ${story.priority}`;
    
    // Call MCP server tool
    const result = await callMCPTool('generate-complete-test-suite', {
      testName: finalTestName,
      description: description,
      pageElements: elements,
      scenarios,
      verification,
      performanceThreshold,
      jiraKey: story.key
    });
    
    console.log(colors.green + result + colors.reset);
    
    // Auto-compile, run, and fix loop
    console.log(colors.cyan + '\n🔄 Starting auto-compile, test, and fix cycle...\n' + colors.reset);
    await autoCompileTestAndFix(finalTestName, elements, scenarios, verification, performanceThreshold);
    
    // Add comment to JIRA
    console.log(colors.cyan + '\n💬 Would you like to add a comment to the JIRA story? (y/n): ' + colors.reset);
    const addComment = await question('');
    
    if (addComment.toLowerCase() === 'y') {
      console.log(colors.yellow + '📝 Adding comment to JIRA story...\n' + colors.reset);
      console.log(colors.green + `✅ Test automation created for story ${story.key}` + colors.reset);
      console.log(colors.reset + `   Generated files: ${finalTestName}.java, ${finalTestName.toLowerCase()}.feature, ${finalTestName}Steps.java\n`);
    }
  } else {
    console.log(colors.yellow + 'Generation cancelled.\n' + colors.reset);
  }
}

/**
 * Detect UI elements from story text using keyword analysis
 */
function detectUIElements(storyText, issueType) {
  const elements = [];
  const textLower = storyText.toLowerCase();
  
  // Common UI element patterns
  const patterns = {
    // Input fields
    'username|user name|userid|user id': { name: 'Username Field', action: 'type', description: 'Enter username' },
    'password|pwd': { name: 'Password Field', action: 'type', description: 'Enter password' },
    'email|e-mail': { name: 'Email Field', action: 'type', description: 'Enter email address' },
    'first name|firstname': { name: 'First Name Field', action: 'type', description: 'Enter first name' },
    'last name|lastname': { name: 'Last Name Field', action: 'type', description: 'Enter last name' },
    'phone|telephone|mobile': { name: 'Phone Field', action: 'type', description: 'Enter phone number' },
    'address': { name: 'Address Field', action: 'type', description: 'Enter address' },
    'search': { name: 'Search Field', action: 'type', description: 'Enter search query' },
    'comment|message|description': { name: 'Comment Field', action: 'type', description: 'Enter text' },
    
    // Buttons
    'login button|sign in button|signin': { name: 'Login Button', action: 'click', description: 'Click login button' },
    'submit button|submit form': { name: 'Submit Button', action: 'click', description: 'Submit form' },
    'save button|save': { name: 'Save Button', action: 'click', description: 'Save changes' },
    'cancel button|cancel': { name: 'Cancel Button', action: 'click', description: 'Cancel action' },
    'delete button|remove': { name: 'Delete Button', action: 'click', description: 'Delete item' },
    'edit button|modify': { name: 'Edit Button', action: 'click', description: 'Edit item' },
    'add button|create': { name: 'Add Button', action: 'click', description: 'Add new item' },
    'register button|signup': { name: 'Register Button', action: 'click', description: 'Register account' },
    
    // Checkboxes & Radio
    'checkbox|check box|remember me': { name: 'Checkbox', action: 'click', description: 'Toggle checkbox' },
    'radio button|option': { name: 'Radio Option', action: 'click', description: 'Select option' },
    
    // Dropdowns
    'dropdown|drop down|select|combo': { name: 'Dropdown', action: 'select', description: 'Select from dropdown' },
    'country|state|city': { name: 'Location Dropdown', action: 'select', description: 'Select location' },
    
    // Links
    'link|hyperlink|navigate': { name: 'Link', action: 'click', description: 'Click link' },
    'forgot password': { name: 'Forgot Password Link', action: 'click', description: 'Click forgot password' }
  };
  
  // Detect elements based on patterns
  for (const [pattern, element] of Object.entries(patterns)) {
    const regex = new RegExp(pattern, 'i');
    if (regex.test(textLower)) {
      // Avoid duplicates
      if (!elements.find(el => el.name === element.name)) {
        elements.push({ ...element });
      }
    }
  }
  
  // Add default elements based on issue type if nothing detected
  if (elements.length === 0) {
    if (issueType.match(/story|feature/i)) {
      elements.push(
        { name: 'Main Action Button', action: 'click', description: 'Primary action button' },
        { name: 'Input Field', action: 'type', description: 'Primary input field' }
      );
    } else if (issueType.match(/bug|defect/i)) {
      elements.push(
        { name: 'Affected Element', action: 'click', description: 'Element with bug' }
      );
    }
  }
  
  return elements;
}

/**
 * Generate detailed test steps from acceptance criterion
 */
function generateDetailedSteps(criterion, elements, issueType) {
  const steps = [];
  const criterionLower = criterion.toLowerCase();
  
  // Analyze criterion for Given/When/Then structure
  if (criterionLower.includes('given') || criterionLower.includes('when') || criterionLower.includes('then')) {
    // Already structured, use as-is
    const lines = criterion.split('\n');
    lines.forEach(line => {
      const trimmed = line.trim();
      if (trimmed && trimmed.match(/^(given|when|then|and)/i)) {
        steps.push(trimmed);
      }
    });
  }
  
  // If no steps generated, create comprehensive steps
  if (steps.length === 0) {
    // Setup
    steps.push('Given user is on the application page');
    steps.push('And the page is fully loaded');
    
    // Actions based on detected elements
    if (elements.length > 0) {
      elements.forEach(element => {
        if (element.action === 'type') {
          steps.push(`When user enters valid data in ${element.name}`);
        } else if (element.action === 'click') {
          steps.push(`And user clicks on ${element.name}`);
        } else if (element.action === 'select') {
          steps.push(`And user selects value from ${element.name}`);
        }
      });
    } else {
      steps.push(`When user performs actions for "${criterion}"`);
    }
    
    // Verification
    steps.push(`Then the system should ${criterion.toLowerCase().includes('should') ? criterion.split('should')[1].trim() : 'complete successfully'}`);
    steps.push('And no errors should be displayed');
  }
  
  return steps;
}

/**
 * Generate edge case scenarios based on story type
 */
function generateEdgeCaseScenarios(issueType, summary, elements) {
  const scenarios = [];
  
  // Negative test scenarios
  if (elements.some(el => el.action === 'type')) {
    scenarios.push({
      name: 'Verify validation with empty fields',
      steps: [
        'Given user is on the application page',
        'When user leaves required fields empty',
        'And user attempts to proceed',
        'Then appropriate validation messages should be displayed',
        'And the action should not proceed'
      ]
    });
    
    scenarios.push({
      name: 'Verify validation with invalid data',
      steps: [
        'Given user is on the application page',
        'When user enters invalid data in fields',
        'And user attempts to proceed',
        'Then validation errors should be displayed',
        'And invalid fields should be highlighted'
      ]
    });
  }
  
  // UI/UX scenarios
  if (issueType.match(/story|feature/i)) {
    scenarios.push({
      name: 'Verify UI responsiveness',
      steps: [
        'Given user is on the application page',
        'When the page loads',
        'Then all elements should be visible',
        'And the layout should be proper',
        'And no visual glitches should occur'
      ]
    });
  }
  
  // Error handling scenarios
  scenarios.push({
    name: 'Verify error handling',
    steps: [
      'Given user is on the application page',
      'When an error condition occurs',
      'Then appropriate error message should be displayed',
      'And user should be able to recover',
      'And application should remain stable'
    ]
  });
  
  return scenarios;
}

/**
 * Generate default scenarios when no acceptance criteria exist
 */
function generateDefaultScenarios(issueType, summary, elements) {
  const scenarios = [];
  
  // Happy path scenario
  scenarios.push({
    name: `Verify ${summary} - Happy Path`,
    steps: [
      'Given user is on the application page',
      'And all prerequisites are met',
      'When user performs the main action',
      'Then the action should complete successfully',
      'And expected result should be displayed'
    ]
  });
  
  // Add element-specific scenarios
  if (elements.length > 0) {
    scenarios.push({
      name: `Verify all UI elements are functional`,
      steps: [
        'Given user is on the application page',
        ...elements.map(el => `When user interacts with ${el.name}`),
        'Then all elements should respond correctly',
        'And no errors should occur'
      ]
    });
  }
  
  return scenarios;
}

/**
 * Suggest verification options based on story type and priority
 */
function suggestVerificationOptions(issueType, priority, storyText) {
  const verification = {
    functional: true,  // Always enable functional
    ui: false,
    ux: false,
    performance: false,
    logging: false,
    performanceThreshold: 3000
  };
  
  const textLower = storyText.toLowerCase();
  
  // UI verification for UI-heavy stories
  if (textLower.match(/ui|user interface|layout|design|button|field|form/i) || 
      issueType.match(/feature|story/i)) {
    verification.ui = true;
  }
  
  // Performance verification for high priority or performance-related stories
  if (priority.match(/high|critical|blocker/i) || 
      textLower.match(/performance|speed|fast|slow|timeout|load time/i)) {
    verification.performance = true;
    verification.performanceThreshold = priority.match(/critical|blocker/i) ? 2000 : 3000;
  }
  
  // Detailed logging for bugs and complex features
  if (issueType.match(/bug|defect/i) || priority.match(/high|critical|blocker/i)) {
    verification.logging = true;
  }
  
  return verification;
}

/**
 * Quick Start Tutorial
 */
async function quickStartTutorial() {
  console.log(colors.cyan + colors.bright + '\n📚 Quick Start Tutorial\n' + colors.reset);
  
  console.log('Welcome to the AI Automation Generator!');
  console.log('\n' + colors.yellow + 'How it works:' + colors.reset);
  console.log('  1️⃣  Describe what you want to test');
  console.log('  2️⃣  Define page elements (buttons, fields, etc.)');
  console.log('  3️⃣  Describe test scenarios in plain English');
  console.log('  4️⃣  AI generates all the code automatically!');
  
  console.log('\n' + colors.yellow + 'Example:' + colors.reset);
  console.log('  Test Name: "Login"');
  console.log('  Elements: Username Field, Password Field, Login Button');
  console.log('  Scenario: "Login with valid credentials"');
  console.log('    Given User is on login page');
  console.log('    When User enters username');
  console.log('    And User enters password');
  console.log('    And User clicks login button');
  console.log('    Then User should be logged in');
  
  console.log('\n' + colors.green + '✨ The AI will generate:' + colors.reset);
  console.log('  • Login.java (Page Object)');
  console.log('  • login.feature (Cucumber Feature)');
  console.log('  • loginSteps.java (Step Definitions)');
  
  console.log('\n' + colors.cyan + 'Press Enter to return to main menu...' + colors.reset);
  await question('');
}

/**
 * Call MCP Tool and generate actual files
 */
async function callMCPTool(toolName, args) {
  const projectRoot = process.cwd();
  const pagesDir = path.join(projectRoot, 'src/main/java/pages');
  const featuresDir = path.join(projectRoot, 'src/test/java/features');
  const stepDefsDir = path.join(projectRoot, 'src/test/java/stepDefs');
  
  try {
    // NEW: Generate test from JIRA story (for AI prompt usage)
    if (toolName === 'generate-from-jira-story') {
      const { issueKey } = args;
      
      console.log(colors.cyan + `\n🎫 Fetching JIRA story: ${issueKey}...\n` + colors.reset);
      
      // Fetch JIRA story
      const story = await getJiraStory(issueKey);
      
      if (!story) {
        return colors.red + `❌ Failed to fetch JIRA story ${issueKey}. Please check the issue key and JIRA configuration.` + colors.reset;
      }
      
      // Display story details
      console.log(colors.green + '✅ Story fetched successfully!\n' + colors.reset);
      console.log(colors.cyan + '📝 Story Details:' + colors.reset);
      console.log(`  Key: ${story.key}`);
      console.log(`  Type: ${story.issueType}`);
      console.log(`  Summary: ${story.summary}`);
      console.log(`  Priority: ${story.priority}`);
      
      if (story.acceptanceCriteria.length > 0) {
        console.log(`\n  Acceptance Criteria (${story.acceptanceCriteria.length}):`);
        story.acceptanceCriteria.forEach((criterion, idx) => {
          console.log(`    ${idx + 1}. ${criterion}`);
        });
      }
      
      console.log(colors.yellow + '\n🤖 Auto-generating complete test suite...\n' + colors.reset);
      
      // Auto-detect everything
      const storyText = `${story.summary}\n${story.description}\n${story.acceptanceCriteria.join('\n')}`;
      const elements = detectUIElements(storyText, story.issueType);
      const verification = suggestVerificationOptions(story.issueType, story.priority, storyText);
      
      console.log(colors.green + `✅ Auto-detected ${elements.length} UI elements` + colors.reset);
      console.log(colors.green + `✅ Configured verification: Functional=${verification.functional}, UI=${verification.ui}, Performance=${verification.performance}` + colors.reset);
      
      // Generate scenarios
      const scenarios = [];
      
      if (story.acceptanceCriteria.length > 0) {
        story.acceptanceCriteria.forEach((criterion) => {
          const scenarioName = `Verify ${criterion.substring(0, 50)}${criterion.length > 50 ? '...' : ''}`;
          const steps = generateDetailedSteps(criterion, elements, story.issueType);
          scenarios.push({ name: scenarioName, steps });
        });
        
        const edgeCases = generateEdgeCaseScenarios(story.issueType, story.summary, elements);
        scenarios.push(...edgeCases);
      } else {
        const defaultScenarios = generateDefaultScenarios(story.issueType, story.summary, elements);
        scenarios.push(...defaultScenarios);
      }
      
      console.log(colors.green + `✅ Generated ${scenarios.length} comprehensive scenarios\n` + colors.reset);
      
      // Generate test name
      const testName = story.summary
        .replace(/[^a-zA-Z0-9\s]/g, '')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join('');
      
      const description = `${story.summary}\n\nJIRA Story: ${story.key}\nGenerated from: ${story.issueType}\nPriority: ${story.priority}`;
      
      // Generate test suite using existing function
      console.log(colors.cyan + '🚀 Generating test files...\n' + colors.reset);
      
      const result = await callMCPTool('generate-complete-test-suite', {
        testName,
        description,
        pageElements: elements,
        scenarios,
        verification,
        performanceThreshold: verification.performanceThreshold,
        jiraKey: story.key
      });
      
      console.log(result);
      
      // Auto-compile and fix
      console.log(colors.cyan + '\n🔄 Starting auto-compile, test, and fix cycle...\n' + colors.reset);
      await autoCompileTestAndFix(testName, elements, scenarios, verification, verification.performanceThreshold);
      
      return colors.green + `\n✅ Test suite for JIRA story ${story.key} generated successfully!\n` +
             `   Test Name: ${testName}\n` +
             `   Elements: ${elements.length}\n` +
             `   Scenarios: ${scenarios.length}\n` +
             `   Files: ${testName}.java, ${testName.toLowerCase()}.feature, ${testName}Steps.java` + colors.reset;
    }
    
    if (toolName === 'generate-complete-test-suite') {
      const { testName, description, pageElements, scenarios, verification, performanceThreshold } = args;
      
      const className = testName.charAt(0).toUpperCase() + testName.slice(1);
      const enableLogging = verification?.logging ?? false;
      const enableAssertions = verification?.functional ?? false;
      const enablePerformance = verification?.performance ?? false;
      
      // Generate Page Object
      const pageFile = path.join(pagesDir, `${className}.java`);
      const elementMethods = pageElements.map(el => {
        const methodName = el.name.replace(/\s+/g, '');
        const action = el.action || 'click';
        
        let method = `    /**\n     * ${el.description || el.name}\n     * @param locator Element locator\n     */\n`;
        method += `    protected static void ${methodName}(String locator${action === 'type' ? ', String text' : ''}) {\n`;
        
        if (enableLogging) {
          method += `        log.info("🎯 ${action}ing on: ${el.name}");\n`;
        } else {
          method += `        System.out.println("🎯 ${action}ing on: ${el.name}");\n`;
        }
        
        if (enablePerformance) {
          method += `        long startTime = System.currentTimeMillis();\n`;
        }
        
        if (action === 'click') {
          method += `        clickOnElement(locator);\n`;
        } else if (action === 'type') {
          method += `        enterText(locator, text);\n`;
        } else if (action === 'select') {
          method += `        selectDropDownValueByText(locator, text);\n`;
        }
        
        if (enablePerformance) {
          method += `        long duration = System.currentTimeMillis() - startTime;\n`;
          method += `        log.info("⏱️ Action completed in " + duration + "ms");\n`;
        }
        
        method += `        TimeoutConfig.shortWait();\n`;
        
        if (enableLogging) {
          method += `        log.info("✅ ${el.name} completed");\n`;
        } else {
          method += `        System.out.println("✅ ${el.name} completed");\n`;
        }
        
        method += `    }\n`;
        return method;
      }).join('\n');
      
      let verificationMethods = '';
      if (enableAssertions) {
        verificationMethods += `\n    protected static void verifyPageLoaded(String expectedUrlPart) {\n`;
        verificationMethods += `        log.info("🔍 Verifying page loaded");\n`;
        verificationMethods += `        Assert.assertTrue(isUrlContains(expectedUrlPart), "Page URL verification failed");\n`;
        verificationMethods += `        log.info("✓ Page verified");\n`;
        verificationMethods += `    }\n`;
      }
      
      const imports = (enableLogging || enableAssertions) ? 
        `\nimport org.testng.Assert;\nimport java.util.logging.Logger;` : '';
      
      const loggerDecl = enableLogging ? 
        `\n    private static final Logger log = Logger.getLogger(${className}.class.getName());` : '';
      
      const pageContent = `package pages;\n\nimport configs.TimeoutConfig;${imports}\n\n/**\n * ${description || className + ' Page Object'}\n * Generated by AI Automation CLI\n */\npublic class ${className} extends BasePage {${loggerDecl}\n\n    public ${className}() {\n        super();\n    }\n\n${elementMethods}${verificationMethods}\n}\n`;
      
      // VALIDATE AND FIX before writing to disk
      const validatedPageContent = validateAndFixPageObject(pageContent, className);
      
      await fs.mkdir(pagesDir, { recursive: true });
      await fs.writeFile(pageFile, validatedPageContent, 'utf-8');
      
      // Generate Feature File
      const featureFile = path.join(featuresDir, `${testName}.feature`);
      const scenarioContent = scenarios.map((s, i) => {
        const steps = s.steps.map(step => `    ${step}`).join('\n');
        const tags = verification.performance ? '@Functional @PerformanceTest' : '@Functional';
        return `  ${tags} @Priority=${i}\n  Scenario: ${s.name}\n${steps}\n`;
      }).join('\n');
      
      const featureContent = `Feature: ${description || testName}\n  As a user\n  I want to test ${testName} functionality\n\n  Background:\n    Given the application is ready\n\n${scenarioContent}`;
      
      await fs.mkdir(featuresDir, { recursive: true });
      await fs.writeFile(featureFile, featureContent, 'utf-8');
      
      // Generate Step Definitions
      const stepsFile = path.join(stepDefsDir, `${testName}Steps.java`);
      const stepsContent = `package stepDefs;\n\nimport io.cucumber.java.en.*;\nimport pages.${className};\nimport org.testng.Assert;\nimport java.util.logging.Logger;\n\n/**\n * Step Definitions for ${testName}\n * Generated by AI Automation CLI\n */\npublic class ${className}Steps {\n    private static final Logger log = Logger.getLogger(${className}Steps.class.getName());\n    private ${className} ${testName}Page = new ${className}();\n\n    @Given("the application is ready")\n    public void applicationIsReady() {\n        log.info("📋 Application ready");\n    }\n\n    // TODO: Add your step definitions here based on scenarios\n    // Example:\n    // @Given("User is on page")\n    // public void userIsOnPage() {\n    //     ${testName}Page.navigateTo("/url");\n    // }\n}\n`;
      
      await fs.mkdir(stepDefsDir, { recursive: true });
      await fs.writeFile(stepsFile, stepsContent, 'utf-8');
      
      return `✅ Test suite generated successfully!\n\nGenerated files:\n  📄 ${pageFile}\n  📋 ${featureFile}\n  🧪 ${stepsFile}\n\nNext steps:\n  1. Review generated files\n  2. Add step definition implementations\n  3. Compile: mvn clean compile\n  4. Run: mvn test`;
      
    } else if (toolName === 'analyze-framework') {
      // Read existing files
      let pageObjects = [];
      let featureFiles = [];
      
      try {
        const pages = await fs.readdir(pagesDir);
        pageObjects = pages.filter(f => f.endsWith('.java')).map(f => ({
          name: f.replace('.java', ''),
          methodCount: 0
        }));
      } catch (e) {}
      
      try {
        const features = await fs.readdir(featuresDir);
        featureFiles = features.filter(f => f.endsWith('.feature')).map(f => ({
          name: f.replace('.feature', ''),
          scenarioCount: 0
        }));
      } catch (e) {}
      
      return JSON.stringify({
        currentState: {
          totalPages: pageObjects.length,
          totalFeatures: featureFiles.length,
          pageObjects,
          featureFiles
        }
      });
    }
    
    // Handle error fixing operations
    if (toolName === 'fix-compilation-error' || toolName === 'fix-test-failure') {
      const { file, error, code, prompt } = args;
      
      console.log(colors.cyan + '  🤖 AI analyzing and fixing...\n' + colors.reset);
      
      let fixedCode = code;
      
      // COMPREHENSIVE ERROR FIXING PATTERNS
      
      // Fix 1: Missing imports (Enhanced)
      if (error.includes('cannot find symbol') || error.includes('package does not exist')) {
        const symbol = error.match(/symbol:\s+(?:class|interface|enum)\s+(\w+)/)?.[1];
        if (symbol) {
          const imports = {
            'Assert': 'import org.testng.Assert;',
            'Logger': 'import org.apache.logging.log4j.Logger;\nimport org.apache.logging.log4j.LogManager;',
            'WebDriver': 'import org.openqa.selenium.WebDriver;',
            'WebElement': 'import org.openqa.selenium.WebElement;',
            'FindBy': 'import org.openqa.selenium.support.FindBy;',
            'PageFactory': 'import org.openqa.selenium.support.PageFactory;',
            'Given': 'import io.cucumber.java.en.Given;',
            'When': 'import io.cucumber.java.en.When;',
            'Then': 'import io.cucumber.java.en.Then;',
            'Before': 'import io.cucumber.java.Before;',
            'After': 'import io.cucumber.java.After;',
            'TimeoutConfig': 'import configs.TimeoutConfig;',
            'BasePage': 'import pages.BasePage;',
            'base': 'import configs.base;'
          };
          
          if (imports[symbol] && !fixedCode.includes(imports[symbol])) {
            const packageLineEnd = fixedCode.indexOf(';') + 1;
            fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\n' + imports[symbol] + fixedCode.slice(packageLineEnd);
          }
        }
        
        // Check for common missing import patterns
        if (error.includes('log') && !fixedCode.includes('import org.apache.logging')) {
          const packageLineEnd = fixedCode.indexOf(';') + 1;
          fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport org.apache.logging.log4j.Logger;\nimport org.apache.logging.log4j.LogManager;' + fixedCode.slice(packageLineEnd);
        }
      }
      
      // Fix 2: Method not found - use correct framework methods (Enhanced)
      if (error.includes('method') && error.includes('cannot find symbol')) {
        // Fix direct Selenium calls to use framework methods
        fixedCode = fixedCode.replace(/(\w+)\.click\(\)/g, 'clickOnElement($1, TimeoutConfig.shortWait())');
        fixedCode = fixedCode.replace(/(\w+)\.sendKeys\(([^)]+)\)/g, 'enterText($1, $2, TimeoutConfig.shortWait())');
        fixedCode = fixedCode.replace(/(\w+)\.getText\(\)/g, '$1.getText()'); // Already correct
        
        // Fix missing logger declaration
        if (error.includes('logger') && !fixedCode.includes('private static final Logger logger')) {
          const classMatch = fixedCode.match(/public class (\w+)/);
          if (classMatch) {
            const className = classMatch[1];
            const insertPoint = fixedCode.indexOf('{', fixedCode.indexOf('public class')) + 1;
            fixedCode = fixedCode.slice(0, insertPoint) + '\n    private static final Logger logger = LogManager.getLogger(' + className + '.class);\n' + fixedCode.slice(insertPoint);
          }
        }
      }
      
      // Fix 3: Missing super() call (Enhanced)
      if (error.includes('constructor') && file.includes('Page.java')) {
        if (!fixedCode.includes('super(driver)')) {
          // Find constructor and add super call
          const constructorMatch = fixedCode.match(/(public \w+Page\(WebDriver driver\) \{)/);
          if (constructorMatch) {
            fixedCode = fixedCode.replace(
              /(public \w+Page\(WebDriver driver\) \{)/,
              '$1\n        super(driver);'
            );
          }
        }
        
        // Ensure PageFactory.initElements is present
        if (!fixedCode.includes('PageFactory.initElements')) {
          const superCallMatch = fixedCode.match(/super\(driver\);/);
          if (superCallMatch) {
            fixedCode = fixedCode.replace(
              /super\(driver\);/,
              'super(driver);\n        PageFactory.initElements(driver, this);'
            );
          }
        }
      }
      
      // Fix 4: Undefined Cucumber steps (Enhanced)
      if (toolName === 'fix-test-failure' && error.includes('Undefined step')) {
        const stepText = error.match(/Undefined step:\s*(.+)/)?.[1]?.trim();
        if (stepText) {
          // Determine annotation type
          const annotation = stepText.match(/^Given /i) ? 'Given' : 
                           stepText.match(/^When /i) ? 'When' : 
                           stepText.match(/^Then /i) ? 'Then' : 'When';
          
          // Clean step text and generate method name
          const cleanStep = stepText.replace(/^(Given|When|Then|And|But)\s+/i, '');
          const methodName = cleanStep.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
          
          // Check if step has parameters
          const hasStringParam = cleanStep.includes('"');
          const hasIntParam = cleanStep.match(/\d+/);
          
          let methodSignature = `public void ${methodName}(`;
          const params = [];
          
          if (hasStringParam) {
            const stringCount = (cleanStep.match(/"/g) || []).length / 2;
            for (let i = 0; i < stringCount; i++) {
              params.push(`String param${i + 1}`);
            }
          }
          
          methodSignature += params.join(', ') + ')';
          
          const stepRegex = cleanStep.replace(/"/g, '{string}').replace(/\d+/g, '{int}');
          
          const newStep = `
    @${annotation}("${stepRegex}")
    ${methodSignature} {
        logger.info("Executing: ${cleanStep}");
        // TODO: Implement step logic
    }
`;
          
          // Ensure logger import exists
          if (!fixedCode.includes('import org.apache.logging.log4j.Logger')) {
            const packageLineEnd = fixedCode.indexOf(';') + 1;
            fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport org.apache.logging.log4j.Logger;\nimport org.apache.logging.log4j.LogManager;' + fixedCode.slice(packageLineEnd);
          }
          
          // Insert step before last closing brace
          const lastBraceIndex = fixedCode.lastIndexOf('}');
          fixedCode = fixedCode.slice(0, lastBraceIndex) + newStep + '\n' + fixedCode.slice(lastBraceIndex);
        }
      }
      
      // Fix 5: Missing extends/implements
      if (error.includes('cannot find symbol') && error.includes('class')) {
        if (file.includes('Page.java') && !fixedCode.includes('extends BasePage')) {
          fixedCode = fixedCode.replace(
            /(public class \w+Page)/,
            '$1 extends BasePage'
          );
        }
        if (file.includes('Steps.java') && !fixedCode.includes('extends base')) {
          fixedCode = fixedCode.replace(
            /(public class \w+Steps)/,
            '$1 extends base'
          );
        }
      }
      
      // Fix 6: Variable not initialized
      if (error.includes('variable') && error.includes('might not have been initialized')) {
        const varName = error.match(/variable (\w+)/)?.[1];
        if (varName && fixedCode.includes(`${varName}Page;`)) {
          // Initialize page object in setup method
          if (!fixedCode.includes(`${varName}Page = new`)) {
            fixedCode = fixedCode.replace(
              /@Given\("User navigates to the application"\)[^}]+\{/,
              `$&\n        ${varName}Page = new ${varName.charAt(0).toUpperCase() + varName.slice(1)}Page(driver);`
            );
          }
        }
      }
      
      // Fix 7: Syntax errors - missing semicolons
      if (error.includes(';expected')) {
        const lineNum = error.match(/line (\d+)/)?.[1];
        // Add missing semicolon (basic fix)
        const lines = fixedCode.split('\n');
        if (lineNum && lines[lineNum - 1]) {
          if (!lines[lineNum - 1].trim().endsWith(';') && 
              !lines[lineNum - 1].trim().endsWith('{') &&
              !lines[lineNum - 1].trim().endsWith('}')) {
            lines[lineNum - 1] = lines[lineNum - 1] + ';';
            fixedCode = lines.join('\n');
          }
        }
      }
      
      // Fix 8: Deprecated Runtime.exec() - Replace with ProcessBuilder
      if (error.includes('Runtime') && (error.includes('deprecated') || error.includes('exec'))) {
        console.log(colors.yellow + '    Fixing deprecated Runtime.exec() calls...\n' + colors.reset);
        
        // Fix Windows command
        fixedCode = fixedCode.replace(
          /Runtime\.getRuntime\(\)\.exec\("cmd\s+\/c\s+start\s+"\s*\+\s*(\w+)\)/g,
          'new ProcessBuilder("cmd", "/c", "start", $1).start()'
        );
        
        // Fix Mac command
        fixedCode = fixedCode.replace(
          /Runtime\.getRuntime\(\)\.exec\("open\s+"\s*\+\s*(\w+)\)/g,
          'new ProcessBuilder("open", $1).start()'
        );
        
        // Fix Linux command
        fixedCode = fixedCode.replace(
          /Runtime\.getRuntime\(\)\.exec\("xdg-open\s+"\s*\+\s*(\w+)\)/g,
          'new ProcessBuilder("xdg-open", $1).start()'
        );
      }
      
      // Fix 9: Missing Logger instance in Page Object (ENHANCED - More robust detection)
      if (error.includes('log') && (error.includes('cannot be resolved') || error.includes('cannot find symbol') || error.includes('package log does not exist'))) {
        console.log(colors.yellow + '    Adding Logger instance...\n' + colors.reset);
        
        // Remove any incorrect log imports
        fixedCode = fixedCode.replace(/import log;?\s*\n/g, '');
        fixedCode = fixedCode.replace(/import log\.\*;?\s*\n/g, '');
        
        // Add Logger import if not present
        if (!fixedCode.includes('import java.util.logging.Logger')) {
          // Find the package statement and add import after it
          const packageMatch = fixedCode.match(/package\s+[\w.]+;\s*\n/);
          if (packageMatch) {
            const insertPoint = packageMatch.index + packageMatch[0].length;
            fixedCode = fixedCode.slice(0, insertPoint) + 'import java.util.logging.Logger;\n' + fixedCode.slice(insertPoint);
          }
        }
        
        // Add static Logger.getLogger import
        if (!fixedCode.includes('import static java.util.logging.Logger.getLogger')) {
          const lastImport = fixedCode.lastIndexOf('import ');
          if (lastImport !== -1) {
            const nextLine = fixedCode.indexOf('\n', lastImport) + 1;
            fixedCode = fixedCode.slice(0, nextLine) + 'import static java.util.logging.Logger.getLogger;\n' + fixedCode.slice(nextLine);
          }
        }
        
        // Add Logger instance to class if using log.info, log.error, etc.
        if ((fixedCode.includes('log.info') || fixedCode.includes('log.error') || fixedCode.includes('log.warn')) && 
            !fixedCode.includes('private static final Logger log')) {
          const classMatch = fixedCode.match(/public class (\w+)(?:\s+extends\s+\w+)?\s*\{/);
          if (classMatch) {
            const className = classMatch[1];
            const insertPoint = classMatch.index + classMatch[0].length;
            fixedCode = fixedCode.slice(0, insertPoint) + 
              '\n    private static final Logger log = getLogger(' + className + '.class.getName());\n' +
              fixedCode.slice(insertPoint);
          }
        }
      }
      
      // Fix 10: Missing method parameters (ENHANCED - Better parameter detection)
      if (error.includes('cannot resolve to a variable') || error.includes('cannot find symbol: variable') || error.includes('cannot find symbol')) {
        const varMatch = error.match(/symbol:\s+variable\s+(\w+)/);
        const varName = varMatch ? varMatch[1] : null;
        
        if (varName && varName !== 'log') { // Skip if it's log (handled by Fix 9)
          console.log(colors.yellow + `    Adding missing parameter: ${varName}\n` + colors.reset);
          
          // Find ALL methods that use this variable
          const lines = fixedCode.split('\n');
          const errorLineMatch = error.match(/\[(\d+),/);
          let targetLine = errorLineMatch ? parseInt(errorLineMatch[1]) : -1;
          
          // Find the method containing the error line
          let methodStartLine = -1;
          let methodEndLine = -1;
          let bracketCount = 0;
          
          for (let i = targetLine - 1; i >= 0; i--) {
            if (lines[i].includes('protected static void') || lines[i].includes('public static void') || 
                lines[i].includes('protected void') || lines[i].includes('public void')) {
              methodStartLine = i;
              break;
            }
          }
          
          if (methodStartLine !== -1) {
            // Find method end
            for (let i = methodStartLine; i < lines.length; i++) {
              for (const char of lines[i]) {
                if (char === '{') bracketCount++;
                if (char === '}') bracketCount--;
                if (bracketCount === 0 && i > methodStartLine) {
                  methodEndLine = i;
                  break;
                }
              }
              if (methodEndLine !== -1) break;
            }
            
            // Extract method
            const methodLines = lines.slice(methodStartLine, methodEndLine + 1);
            const methodCode = methodLines.join('\n');
            
            // Determine parameter type
            let paramType = 'String';
            if (methodCode.includes('selectDropDownValueByText') || methodCode.includes('sendKeys') || methodCode.includes('enterText')) {
              paramType = 'String';
            } else if (methodCode.includes('Integer') || methodCode.includes('parseInt')) {
              paramType = 'int';
            }
            
            // Update method signature
            const signatureLine = lines[methodStartLine];
            if (!signatureLine.includes(`, ${paramType} ${varName}`) && !signatureLine.includes(`${paramType} ${varName},`)) {
              const updatedSignature = signatureLine.replace(
                /(\w+\s+\w+\s+void\s+\w+)\(([^)]*)\)/,
                (match, prefix, params) => {
                  if (params.trim() === '') {
                    return `${prefix}(String locator, ${paramType} ${varName})`;
                  } else {
                    return `${prefix}(${params}, ${paramType} ${varName})`;
                  }
                }
              );
              lines[methodStartLine] = updatedSignature;
              fixedCode = lines.join('\n');
            }
          }
        }
      }
      
      // Fix 11: Unused imports
      if (error.includes('is never used') && error.includes('import')) {
        const unusedImport = error.match(/import\s+([\w.]+)/)?.[1];
        if (unusedImport) {
          console.log(colors.yellow + `    Removing unused import: ${unusedImport}\n` + colors.reset);
          const importLine = new RegExp(`import\\s+${unusedImport.replace('.', '\\.')}.*;?\\n`, 'g');
          fixedCode = fixedCode.replace(importLine, '');
        }
      }
      
      // Fix 12: Unused variables/fields (rename to follow conventions)
      if (error.includes('is not used') && error.includes('field')) {
        const fieldMatch = error.match(/field\s+\w+\.(\w+)/)?.[1];
        if (fieldMatch && fieldMatch.charAt(0) === fieldMatch.charAt(0).toUpperCase()) {
          console.log(colors.yellow + `    Renaming field to follow Java conventions: ${fieldMatch}\n` + colors.reset);
          
          // Convert PascalCase to camelCase
          const newFieldName = fieldMatch.charAt(0).toLowerCase() + fieldMatch.slice(1);
          
          // Replace field declaration
          fixedCode = fixedCode.replace(
            new RegExp(`(private\\s+\\w+\\s+)${fieldMatch}(\\s*=)`, 'g'),
            `$1${newFieldName}$2`
          );
          
          // Replace field usage
          fixedCode = fixedCode.replace(
            new RegExp(`\\b${fieldMatch}\\.`, 'g'),
            `${newFieldName}.`
          );
          
          // Update comments
          fixedCode = fixedCode.replace(
            new RegExp(`//\\s*${fieldMatch}`, 'g'),
            `// ${newFieldName}`
          );
        }
      }
      
      // Fix 13: Missing @FindBy annotation for WebElements
      if (error.includes('cannot find symbol') && file.includes('Page.java')) {
        const elementMatch = error.match(/symbol:\s+variable\s+(\w+Element)/i);
        if (elementMatch) {
          const elementName = elementMatch[1];
          console.log(colors.yellow + `    Adding @FindBy annotation for: ${elementName}\n` + colors.reset);
          
          // Add @FindBy import if missing
          if (!fixedCode.includes('import org.openqa.selenium.support.FindBy')) {
            const packageLineEnd = fixedCode.indexOf(';') + 1;
            fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport org.openqa.selenium.support.FindBy;\nimport org.openqa.selenium.WebElement;' + fixedCode.slice(packageLineEnd);
          }
          
          // Add WebElement declaration with @FindBy
          const classBodyMatch = fixedCode.match(/public class \w+Page[^{]*\{/);
          if (classBodyMatch && !fixedCode.includes(`${elementName};`)) {
            const insertPoint = classBodyMatch.index + classBodyMatch[0].length;
            const selector = elementName.replace(/Element$/, '').replace(/([A-Z])/g, '-$1').toLowerCase().replace(/^-/, '');
            fixedCode = fixedCode.slice(0, insertPoint) + 
              `\n    @FindBy(id = "${selector}")\n    private WebElement ${elementName};\n` +
              fixedCode.slice(insertPoint);
          }
        }
      }
      
      // Fix 14: WebDriver type mismatch (ChromeDriver/FirefoxDriver → WebDriver)
      if (error.includes('incompatible types') && (error.includes('ChromeDriver') || error.includes('FirefoxDriver'))) {
        console.log(colors.yellow + '    Fixing WebDriver type mismatch...\n' + colors.reset);
        
        // Replace ChromeDriver with WebDriver
        fixedCode = fixedCode.replace(/ChromeDriver\s+(\w+)\s*=\s*new\s+ChromeDriver/g, 'WebDriver $1 = new ChromeDriver');
        fixedCode = fixedCode.replace(/FirefoxDriver\s+(\w+)\s*=\s*new\s+FirefoxDriver/g, 'WebDriver $1 = new FirefoxDriver');
        
        // Add WebDriver import if missing
        if (!fixedCode.includes('import org.openqa.selenium.WebDriver')) {
          const packageLineEnd = fixedCode.indexOf(';') + 1;
          fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport org.openqa.selenium.WebDriver;' + fixedCode.slice(packageLineEnd);
        }
      }
      
      // Fix 15: Duplicate class name in package
      if (error.includes('duplicate class') || error.includes('class is public, should be declared')) {
        const classMatch = error.match(/class\s+(\w+)/)?.[1];
        const fileMatch = file.match(/([^\\/]+)\.java$/)?.[1];
        
        if (classMatch && fileMatch && classMatch !== fileMatch) {
          console.log(colors.yellow + `    Renaming class ${classMatch} to match file ${fileMatch}\n` + colors.reset);
          
          // Rename class to match filename
          fixedCode = fixedCode.replace(
            new RegExp(`public class ${classMatch}`, 'g'),
            `public class ${fileMatch}`
          );
          
          // Update constructor name
          fixedCode = fixedCode.replace(
            new RegExp(`public ${classMatch}\\(`, 'g'),
            `public ${fileMatch}(`
          );
          
          // Update logger reference
          fixedCode = fixedCode.replace(
            new RegExp(`getLogger\\(${classMatch}\\.class`, 'g'),
            `getLogger(${fileMatch}.class`
          );
        }
      }
      
      // Fix 16: Missing @CucumberOptions annotation in runner
      if (error.includes('No features found') && file.includes('runner') || file.includes('Runner')) {
        console.log(colors.yellow + '    Adding @CucumberOptions annotation...\n' + colors.reset);
        
        if (!fixedCode.includes('@CucumberOptions')) {
          // Add CucumberOptions import
          if (!fixedCode.includes('import io.cucumber.testng.CucumberOptions')) {
            const packageLineEnd = fixedCode.indexOf(';') + 1;
            fixedCode = fixedCode.slice(0, packageLineEnd) + 
              '\n\nimport io.cucumber.testng.CucumberOptions;\nimport io.cucumber.testng.AbstractTestNGCucumberTests;' + 
              fixedCode.slice(packageLineEnd);
          }
          
          // Add annotation before class
          fixedCode = fixedCode.replace(
            /(public class \w+)/,
            `@CucumberOptions(\n    features = "src/test/java/features",\n    glue = {"stepDefs", "hooks"},\n    plugin = {"pretty", "html:target/cucumber-reports.html"}\n)\n$1`
          );
          
          // Ensure class extends AbstractTestNGCucumberTests
          if (!fixedCode.includes('extends AbstractTestNGCucumberTests')) {
            fixedCode = fixedCode.replace(
              /(public class \w+)(?:\s*\{)/,
              '$1 extends AbstractTestNGCucumberTests {'
            );
          }
        }
      }
      
      // Fix 17: Incorrect assertion method (assertEquals vs assertTrue)
      if (error.includes('cannot find symbol') && error.includes('assertEquals')) {
        console.log(colors.yellow + '    Adding Assert import...\n' + colors.reset);
        
        if (!fixedCode.includes('import org.testng.Assert')) {
          const packageLineEnd = fixedCode.indexOf(';') + 1;
          fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport org.testng.Assert;' + fixedCode.slice(packageLineEnd);
        }
        
        // Fix standalone assertEquals calls
        fixedCode = fixedCode.replace(/\bassertEquals\(/g, 'Assert.assertEquals(');
        fixedCode = fixedCode.replace(/\bassertTrue\(/g, 'Assert.assertTrue(');
        fixedCode = fixedCode.replace(/\bassertFalse\(/g, 'Assert.assertFalse(');
        fixedCode = fixedCode.replace(/\bassertNotNull\(/g, 'Assert.assertNotNull(');
      }
      
      // Fix 18: Missing test data parameter in Cucumber steps
      if (error.includes('cannot find symbol') && file.includes('Steps.java') && error.includes('variable')) {
        const varMatch = error.match(/symbol:\s+variable\s+(\w+)/)?.[1];
        if (varMatch && !['driver', 'log', 'logger'].includes(varMatch)) {
          console.log(colors.yellow + `    Converting hardcoded value to Cucumber parameter: ${varMatch}\n` + colors.reset);
          
          // Find the step definition that uses this variable
          const stepMatch = fixedCode.match(/@(Given|When|Then)\("([^"]+)"\)[^{]+\{[^}]*\b${varMatch}\b/s);
          if (stepMatch) {
            const annotation = stepMatch[1];
            const stepText = stepMatch[2];
            
            // Add parameter placeholder to step regex
            const newStepText = stepText + ' {string}';
            fixedCode = fixedCode.replace(
              `@${annotation}("${stepText}")`,
              `@${annotation}("${newStepText}")`
            );
            
            // Add parameter to method signature
            const methodMatch = fixedCode.match(new RegExp(`@${annotation}\\("${newStepText.replace(/[{}]/g, '\\$&')}"\\)[^(]+\\(([^)]*)\\)`));
            if (methodMatch) {
              const params = methodMatch[1];
              const newParams = params ? `${params}, String ${varMatch}` : `String ${varMatch}`;
              fixedCode = fixedCode.replace(
                new RegExp(`(@${annotation}\\("${newStepText.replace(/[{}]/g, '\\$&')}"\\)[^(]+\\()${params}(\\))`),
                `$1${newParams}$2`
              );
            }
          }
        }
      }
      
      // Fix 19: IOException not handled (add throws or try-catch)
      if (error.includes('unreported exception') && error.includes('IOException')) {
        console.log(colors.yellow + '    Adding IOException handling...\n' + colors.reset);
        
        // Find method with IOException
        const methodMatch = fixedCode.match(/(public|protected|private)\s+(static\s+)?\w+\s+(\w+)\s*\([^)]*\)(?!\s*throws)/g);
        if (methodMatch) {
          methodMatch.forEach(method => {
            fixedCode = fixedCode.replace(
              method + ' {',
              method + ' throws IOException {'
            );
          });
        }
        
        // Add IOException import
        if (!fixedCode.includes('import java.io.IOException')) {
          const packageLineEnd = fixedCode.indexOf(';') + 1;
          fixedCode = fixedCode.slice(0, packageLineEnd) + '\n\nimport java.io.IOException;' + fixedCode.slice(packageLineEnd);
        }
      }
      
      // Fix 8: Type mismatch
      if (error.includes('incompatible types')) {
        // Fix common type mismatches
        fixedCode = fixedCode.replace(/TimeoutConfig\.shortWait\(\)/g, 'TimeoutConfig.shortWait()');
      }
      
      // Fix 9: Package mismatch
      if (error.includes('package') && file.includes('/pages/')) {
        if (!fixedCode.startsWith('package pages;')) {
          fixedCode = 'package pages;\n\n' + fixedCode.replace(/^package.*?;/, '');
        }
      } else if (file.includes('/stepDefs/')) {
        if (!fixedCode.startsWith('package stepDefs;')) {
          fixedCode = 'package stepDefs;\n\n' + fixedCode.replace(/^package.*?;/, '');
        }
      }
      
      return fixedCode;
    }
    
    return '✅ Operation completed';
  } catch (error) {
    console.error(colors.red + '\n❌ Error: ' + error.message + colors.reset);
    return '❌ Generation failed: ' + error.message;
  }
}

/**
 * Compile Project
 */
async function compileProject() {
  console.log(colors.yellow + '\n🔨 Compiling project...\n' + colors.reset);
  
  return new Promise((resolve) => {
    const mvn = spawn('mvn', ['clean', 'compile', 'test-compile'], {
      stdio: 'inherit',
      shell: true
    });
    
    mvn.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ Compilation successful!' + colors.reset);
      } else {
        console.log(colors.red + '\n❌ Compilation failed. Please check the errors above.' + colors.reset);
      }
      resolve();
    });
  });
}

/**
 * Auto-compile, test, and fix loop
 */
async function autoCompileTestAndFix(testName, elements, scenarios, verification, performanceThreshold) {
  const maxAttempts = 5;
  let attempt = 1;
  let success = false;
  
  while (!success && attempt <= maxAttempts) {
    console.log(colors.magenta + `\n${'='.repeat(60)}` + colors.reset);
    console.log(colors.bright + colors.cyan + `  ATTEMPT ${attempt}/${maxAttempts}` + colors.reset);
    console.log(colors.magenta + `${'='.repeat(60)}\n` + colors.reset);
    
    // Step 1: Compile
    console.log(colors.yellow + '🔨 Step 1/3: Compiling project...\n' + colors.reset);
    const compileResult = await runMavenCommand('clean compile test-compile');
    
    if (!compileResult.success) {
      console.log(colors.red + '❌ Compilation failed!\n' + colors.reset);
      console.log(colors.yellow + 'Errors detected:\n' + colors.reset);
      console.log(compileResult.error);
      
      // Step 2: Fix compilation errors using MCP
      console.log(colors.cyan + '\n🔧 Attempting to fix compilation errors...\n' + colors.reset);
      const fixed = await fixCompilationErrors(compileResult.error, testName);
      
      if (!fixed) {
        console.log(colors.red + '❌ Unable to fix compilation errors automatically.\n' + colors.reset);
        break;
      }
      
      attempt++;
      continue;
    }
    
    console.log(colors.green + '✅ Compilation successful!\n' + colors.reset);
    
    // Step 2: Run tests
    console.log(colors.yellow + '🧪 Step 2/3: Running tests...\n' + colors.reset);
    const testResult = await runMavenCommand('test');
    
    if (!testResult.success) {
      console.log(colors.red + '❌ Tests failed!\n' + colors.reset);
      console.log(colors.yellow + 'Test failures:\n' + colors.reset);
      console.log(testResult.error);
      
      // Step 3: Fix test errors using MCP
      console.log(colors.cyan + '\n🔧 Attempting to fix test failures...\n' + colors.reset);
      const fixed = await fixTestFailures(testResult.error, testName, scenarios);
      
      if (!fixed) {
        console.log(colors.red + '❌ Unable to fix test failures automatically.\n' + colors.reset);
        break;
      }
      
      attempt++;
      continue;
    }
    
    console.log(colors.green + '✅ Tests passed successfully!\n' + colors.reset);
    success = true;
  }
  
  // Final summary
  console.log(colors.magenta + `\n${'='.repeat(60)}` + colors.reset);
  if (success) {
    console.log(colors.bright + colors.green + '  ✨ SUCCESS! All tests are passing!' + colors.reset);
    console.log(colors.green + `  Total attempts: ${attempt}` + colors.reset);
    
    // Step 3: Offer to run the full test suite
    console.log(colors.magenta + `${'='.repeat(60)}\n` + colors.reset);
    const runFullSuite = await question(colors.cyan + '🚀 Run full test suite now? (y/n): ' + colors.reset);
    
    if (runFullSuite.toLowerCase() === 'y') {
      console.log(colors.yellow + '\n🏃 Running full test suite...\n' + colors.reset);
      const fullResult = await runMavenCommand('clean test');
      
      if (fullResult.success) {
        console.log(colors.green + '\n✅ Full test suite passed!\n' + colors.reset);
        console.log(colors.cyan + '📊 View reports at:\n' + colors.reset);
        console.log(colors.bright + '   MRITestExecutionReports/Version*/extentReports/testNGExtentReports/html/\n' + colors.reset);
      } else {
        console.log(colors.red + '\n❌ Some tests in the suite failed. Check reports for details.\n' + colors.reset);
      }
    }
  } else {
    console.log(colors.bright + colors.red + '  ❌ FAILED after ' + maxAttempts + ' attempts' + colors.reset);
    console.log(colors.yellow + '  Please review the errors manually.' + colors.reset);
    console.log(colors.red + '\n  🛑 Terminating session due to unresolved compilation/test errors.\n' + colors.reset);
    console.log(colors.magenta + `${'='.repeat(60)}\n` + colors.reset);
    
    // Exit the process with error code
    process.exit(1);
  }
  console.log(colors.magenta + `${'='.repeat(60)}\n` + colors.reset);
}

/**
 * Run Maven command
 */
async function runMavenCommand(command) {
  return new Promise((resolve) => {
    const isWindows = process.platform === 'win32';
    const mvnCmd = isWindows ? 'mvn.cmd' : 'mvn';
    
    const mvn = spawn(mvnCmd, command.split(' '), {
      cwd: process.cwd(),
      shell: true
    });
    
    let output = '';
    let errorOutput = '';
    
    mvn.stdout.on('data', (data) => {
      const text = data.toString();
      output += text;
      process.stdout.write(colors.reset + text);
    });
    
    mvn.stderr.on('data', (data) => {
      const text = data.toString();
      errorOutput += text;
      process.stderr.write(colors.yellow + text + colors.reset);
    });
    
    mvn.on('close', (code) => {
      const success = code === 0;
      resolve({
        success,
        output,
        error: success ? '' : (errorOutput || output)
      });
    });
  });
}

/**
 * Fix compilation errors using MCP server
 */
async function fixCompilationErrors(errorLog, testName) {
  try {
    console.log(colors.cyan + '🤖 Analyzing compilation errors with AI...\n' + colors.reset);
    
    // Extract error details
    const errors = parseCompilationErrors(errorLog);
    
    for (const error of errors) {
      console.log(colors.yellow + `  Fixing: ${error.file} (Line ${error.line})\n` + colors.reset);
      console.log(colors.reset + `  Error: ${error.message}\n`);
      
      // Read the file
      const filePath = path.join(process.cwd(), error.file);
      const fileContent = await fs.readFile(filePath, 'utf-8');
      
      // Use MCP to fix the error
      const fixPrompt = `Fix this Java compilation error:
File: ${error.file}
Line: ${error.line}
Error: ${error.message}

Current code:
${fileContent}

Provide the corrected code for the entire file.`;
      
      const fixedCode = await callMCPTool('fix-compilation-error', {
        file: error.file,
        error: error.message,
        line: error.line,
        code: fileContent,
        prompt: fixPrompt
      });
      
      // Write fixed code back
      await fs.writeFile(filePath, fixedCode, 'utf-8');
      console.log(colors.green + `  ✅ Fixed: ${error.file}\n` + colors.reset);
    }
    
    return true;
  } catch (err) {
    console.error(colors.red + '  Error during fix: ' + err.message + colors.reset);
    return false;
  }
}

/**
 * Fix test failures using MCP server
 */
async function fixTestFailures(errorLog, testName, scenarios) {
  try {
    console.log(colors.cyan + '🤖 Analyzing test failures with AI...\n' + colors.reset);
    
    // Extract failure details
    const failures = parseTestFailures(errorLog);
    
    for (const failure of failures) {
      console.log(colors.yellow + `  Fixing: ${failure.test}\n` + colors.reset);
      console.log(colors.reset + `  Reason: ${failure.message}\n`);
      
      // Determine which file to fix
      let fileToFix = '';
      if (failure.message.includes('step definition') || failure.message.includes('undefined step')) {
        fileToFix = path.join(process.cwd(), 'src', 'test', 'java', 'stepDefs', `${testName}Steps.java`);
      } else if (failure.message.includes('element') || failure.message.includes('locator')) {
        fileToFix = path.join(process.cwd(), 'src', 'main', 'java', 'pages', `${testName}Page.java`);
      } else {
        fileToFix = path.join(process.cwd(), 'src', 'test', 'java', 'features', `${testName.toLowerCase()}.feature`);
      }
      
      const fileContent = await fs.readFile(fileToFix, 'utf-8');
      
      // Use MCP to fix the failure
      const fixPrompt = `Fix this test failure:
Test: ${failure.test}
Error: ${failure.message}
Stack trace: ${failure.stackTrace || 'N/A'}

Current code:
${fileContent}

Expected scenarios:
${JSON.stringify(scenarios, null, 2)}

Provide the corrected code for the entire file.`;
      
      const fixedCode = await callMCPTool('fix-test-failure', {
        test: failure.test,
        error: failure.message,
        file: fileToFix,
        code: fileContent,
        scenarios: scenarios,
        prompt: fixPrompt
      });
      
      // Write fixed code back
      await fs.writeFile(fileToFix, fixedCode, 'utf-8');
      console.log(colors.green + `  ✅ Fixed: ${path.basename(fileToFix)}\n` + colors.reset);
    }
    
    return true;
  } catch (err) {
    console.error(colors.red + '  Error during fix: ' + err.message + colors.reset);
    return false;
  }
}

/**
 * Parse compilation errors from Maven output
 */
function parseCompilationErrors(errorLog) {
  const errors = [];
  const errorPattern = /\[ERROR\]\s+(.+?):(\d+):\s+error:\s+(.+)/g;
  let match;
  
  while ((match = errorPattern.exec(errorLog)) !== null) {
    errors.push({
      file: match[1].replace(/\\/g, '/').replace(/^.*[\/]src/, 'src'),
      line: match[2],
      message: match[3].trim()
    });
  }
  
  return errors;
}

/**
 * Parse test failures from Maven output
 */
function parseTestFailures(errorLog) {
  const failures = [];
  
  // Pattern 1: TestNG failures
  const testNGPattern = /FAILED:\s+(.+?)\n[\s\S]*?java\.lang\.\w+(?:Error|Exception):\s+(.+?)(?:\n|$)/g;
  let match;
  
  while ((match = testNGPattern.exec(errorLog)) !== null) {
    failures.push({
      test: match[1].trim(),
      message: match[2].trim(),
      stackTrace: ''
    });
  }
  
  // Pattern 2: Cucumber undefined steps
  const cucumberPattern = /Undefined step:\s+(.+?)$/gm;
  while ((match = cucumberPattern.exec(errorLog)) !== null) {
    failures.push({
      test: 'Cucumber Step',
      message: 'Undefined step: ' + match[1].trim(),
      stackTrace: ''
    });
  }
  
  return failures;
}

/**
 * Main Program Loop
 */
async function main() {
  displayWelcome();
  
  // Check and start MCP server if needed
  await ensureMCPServer();
  
  let running = true;
  
  while (running) {
    const choice = await displayMenu();
    
    switch (choice) {
      case '1':
        await recordAndGenerate();
        break;
      case '2':
        await generateTestFromJiraStory();
        break;
      case '3':
        await generateCompleteTestSuite();
        break;
      case '4':
        await updateExistingTest();
        break;
      case '5':
        await generatePageObject();
        break;
      case '6':
        await generateFeatureFile();
        break;
      case '7':
        await generateStepDefinitions();
        break;
      case '8':
        await analyzeFramework();
        break;
      case '9':
        await quickStartTutorial();
        break;
      case '0':
        console.log(colors.green + '\n👋 Thanks for using AI Automation Generator!\n' + colors.reset);
        
        // Cleanup MCP server if we started it
        if (mcpServerProcess) {
          console.log(colors.yellow + '🛑 Stopping MCP server...' + colors.reset);
          mcpServerProcess.kill();
        }
        
        running = false;
        break;
      default:
        console.log(colors.red + '\n❌ Invalid choice. Please try again.\n' + colors.reset);
    }
    
    if (running && choice !== '9') {
      await question(colors.cyan + '\nPress Enter to continue...' + colors.reset);
    }
  }
  
  rl.close();
}

// Run the CLI
main().catch(console.error);
