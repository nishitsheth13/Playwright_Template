#!/usr/bin/env node

/**
 * AI Automation CLI - Interactive Test Generator
 * 
 * User-friendly command-line interface for generating automation scripts
 * using AI and MCP server capabilities.
 */

const readline = require('readline');
const { spawn } = require('child_process');
const fs = require('fs').promises;
const path = require('path');

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
 * Display main menu
 */
async function displayMenu() {
  console.log(colors.yellow + '\n📋 What would you like to create?\n' + colors.reset);
  console.log('  1️⃣  Generate Complete Test Suite (Recommended)');
  console.log('  2️⃣  Generate Page Object only');
  console.log('  3️⃣  Generate Feature File only');
  console.log('  4️⃣  Generate Step Definitions only');
  console.log('  5️⃣  Analyze Existing Framework');
  console.log('  6️⃣  Quick Start Tutorial');
  console.log('  0️⃣  Exit\n');
  
  const choice = await question(colors.cyan + '👉 Enter your choice (0-6): ' + colors.reset);
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
    
    // Offer to compile
    const compile = await question(colors.cyan + '\n🔨 Compile the project now? (y/n): ' + colors.reset);
    if (compile.toLowerCase() === 'y') {
      await compileProject();
    }
  } else {
    console.log(colors.yellow + 'Generation cancelled.' + colors.reset);
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
    description
  });
  
  console.log(colors.green + result + colors.reset);
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
 * Call MCP Tool (Simulated - actual implementation would use MCP SDK)
 */
async function callMCPTool(toolName, args) {
  // In real implementation, this would communicate with MCP server
  // For now, we'll simulate by directly calling Node.js implementation
  
  // Placeholder - actual implementation will invoke MCP server
  return `✅ Tool '${toolName}' executed successfully!\n\nGenerated files:\n  • Page Object\n  • Feature File\n  • Step Definitions\n\nNext: Review and compile your project.`;
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
 * Main Program Loop
 */
async function main() {
  displayWelcome();
  
  let running = true;
  
  while (running) {
    const choice = await displayMenu();
    
    switch (choice) {
      case '1':
        await generateCompleteTestSuite();
        break;
      case '2':
        await generatePageObject();
        break;
      case '3':
        console.log(colors.yellow + '\nFeature generation - Coming soon!' + colors.reset);
        break;
      case '4':
        console.log(colors.yellow + '\nStep definition generation - Coming soon!' + colors.reset);
        break;
      case '5':
        await analyzeFramework();
        break;
      case '6':
        await quickStartTutorial();
        break;
      case '0':
        console.log(colors.green + '\n👋 Thanks for using AI Automation Generator!\n' + colors.reset);
        running = false;
        break;
      default:
        console.log(colors.red + '\n❌ Invalid choice. Please try again.\n' + colors.reset);
    }
    
    if (running && choice !== '6') {
      await question(colors.cyan + '\nPress Enter to continue...' + colors.reset);
    }
  }
  
  rl.close();
}

// Run the CLI
main().catch(console.error);
