#!/usr/bin/env node

/**
 * AI Automation CLI - Interactive Test Generator
 * 
 * Cross-platform command-line interface for generating automation scripts
 * using AI and MCP server capabilities.
 * 
 * Features:
 * - Auto-start MCP server
 * - Generate Page Objects, Features, Step Definitions
 * - Auto-compile, test, and fix errors (up to 5 attempts)
 * - Run full test suite with reports
 * - Config-driven menu system (no duplication)
 * - 100% cross-platform (Windows, macOS, Linux)
 *
 * ARCHITECTURE:
 * - MENU_CONFIG: Single source of truth for all menu options
 * - displayMenu(): Renders menu from config
 * - executeMenuAction(): Routes to appropriate function
 * - Easy to extend: Add items to MENU_CONFIG
 *
 * Documentation: See README.md and MIGRATION_GUIDE.md
 * 
 * TODO BEFORE USING:
 *   [ ] Run Option S (Setup) once to install MCP Server
 *   [ ] Ensure Maven & Node.js installed
 *   [ ] Update configurations.properties
 *   [ ] Prepare JIRA credentials (optional)
 * 
 * TODO USAGE:
 *   [ ] Choose Option 1 (Record) for fastest results
 *   [ ] Or Option 2 (JIRA) for enterprise workflows
 *   [ ] Or Option 3 (AI Interactive) for no-JIRA setup
 *   [ ] Run Option 4 (Validate) after generation
 *   [ ] Check reports in MRITestExecutionReports/
 *
 * ADDING NEW MENU OPTIONS:
 *   1. Add your function (e.g., async function myNewFeature() { ... })
 *   2. Add to MENU_CONFIG.sections[n].items array
 *   3. Add corresponding NPM script in package.json (optional)
 *   4. Done! No switch statements needed
 * 
 * Full TODO checklist: See WORKFLOW_TODOS.md
 * 
 * ⚠️ DEVELOPMENT POLICY:
 * - DO NOT create separate script files for new features - add functions here
 * - DO NOT create new markdown files - update existing docs
 * - DO NOT duplicate menu items - use MENU_CONFIG
 * - Keep project structure minimal
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

// ============================================================================
// MENU CONFIGURATION - Single source of truth
// ============================================================================
const MENU_CONFIG = {
  title: 'AI Test Automation - Main Menu',
  sections: [
    {
      name: '📊 TEST GENERATION',
      items: [
        {
          key: '1',
          icon: '🎥',
          title: '[RECORD] Playwright Recording → Auto-Generate',
          subtitle: 'Fastest - 5-10 min',
          description: 'Launch Playwright Inspector → Record → Auto-generate all files',
          action: 'recordAndGenerate'
        },
        {
          key: '1B',
          icon: '🔄',
          title: '[RETRY] Regenerate from Existing Recording',
          subtitle: 'Use previous recording',
          description: 'List recordings from /Recorded → Select → Regenerate without re-recording',
          action: 'retryFromRecording'
        },
        {
          key: '2',
          icon: '🎫',
          title: '[JIRA] JIRA Story → Complete Test Suite',
          subtitle: 'Enterprise integration',
          description: 'Fetch JIRA story → Parse acceptance criteria → Generate BDD scenarios',
          action: 'generateTestFromJiraStory'
        },
        {
          key: '3',
          icon: '🤖',
          title: '[AI] AI-Assisted Interactive Generation',
          subtitle: 'Conversational test creation',
          description: 'Answer AI questions → Generate comprehensive test suite',
          action: 'generateCompleteTestSuite'
        }
      ]
    },
    {
      name: '⚙️  SETUP & VALIDATION',
      items: [
        {
          key: 'S',
          icon: '⚙️',
          title: '[SETUP] Complete Project Setup',
          subtitle: 'First-time setup',
          description: 'Install dependencies → Verify Maven/Node.js/Java → Configure environments',
          action: 'completeProjectSetup'
        },
        {
          key: 'V',
          icon: '✅',
          title: '[VALIDATE] Code Validation & Auto-Fix',
          subtitle: 'Check/Fix mode',
          description: 'Validate compilation → Check quality → Auto-fix common errors',
          action: 'quickJavaValidation'
        }
      ]
    },
    {
      name: '🧪 TEST EXECUTION',
      items: [
        {
          key: '4',
          icon: '🏷️',
          title: '[TAG] Run Tagged Tests',
          subtitle: '@smoke, @regression, etc.',
          description: 'Run tests by Cucumber tags with AND/OR/NOT combinations',
          action: 'runSpecificTagTests'
        },
        {
          key: '5',
          icon: '⚡',
          title: '[PARALLEL] Parallel Execution',
          subtitle: 'Faster execution',
          description: 'Run tests in parallel with configurable thread count',
          action: 'runParallelTests'
        },
        {
          key: '6',
          icon: '🚀',
          title: '[RUN] Full Test Suite',
          subtitle: 'Complete execution',
          description: 'Clean build → Compile → Execute all tests → Generate reports',
          action: 'validateAndRunTests'
        }
      ]
    },
    {
      name: '📈 REPORTING & UTILITIES',
      items: [
        {
          key: 'R',
          icon: '📊',
          title: '[REPORT] Generate & View Reports',
          subtitle: 'HTML/JSON/Allure/Extent',
          description: 'Generate test reports and open in browser',
          action: 'generateAndViewReports'
        },
        {
          key: 'M',
          icon: '📈',
          title: '[METRICS] Test Metrics Dashboard',
          subtitle: 'Pass rates & trends',
          description: 'View pass rates, execution times, and historical trends',
          action: 'showTestMetrics'
        },
        {
          key: 'C',
          icon: '🧹',
          title: '[CLEAN] Clean Build Artifacts',
          subtitle: 'Free up space',
          description: 'Clean target/, logs, old reports, and screenshots',
          action: 'cleanBuildArtifacts'
        }
      ]
    },
    {
      name: '❓ HELP',
      items: [
        {
          key: 'H',
          icon: '📖',
          title: '[HELP] Command Reference',
          description: 'Display usage guide, examples, and troubleshooting',
          action: 'showHelp'
        },
        {
          key: '0',
          icon: '🚪',
          title: '[EXIT] Exit Menu',
          description: 'Close application and cleanup',
          action: 'exit'
        }
      ]
    }
  ]
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

// ============================================================================
// MENU UTILITY FUNCTIONS
// ============================================================================

/**
 * Add a new menu item dynamically (for future extensibility)
 * @param {string} sectionName - Name of the section to add to
 * @param {object} item - Menu item object {key, icon, title, subtitle?, description, action}
 */
function addMenuItem(sectionName, item) {
  const section = MENU_CONFIG.sections.find(s => s.name === sectionName);
  if (section) {
    section.items.push(item);
  } else {
    console.warn(`Section "${sectionName}" not found in menu config`);
  }
}

/**
 * Get all menu actions (for validation)
 */
function getAllMenuActions() {
  const actions = [];
  MENU_CONFIG.sections.forEach(section => {
    section.items.forEach(item => {
      actions.push({ key: item.key, action: item.action, title: item.title });
    });
  });
  return actions;
}

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
 * Validate and fix Recorded directory
 * Ensures Recorded exists as a directory, not a file
 */
async function validateRecordedDirectory() {
  const fsSync = require('fs');
  const recordedPath = path.join(process.cwd(), 'Recorded');

  try {
    if (fsSync.existsSync(recordedPath)) {
      const stats = fsSync.statSync(recordedPath);

      if (!stats.isDirectory()) {
        console.log(colors.yellow + '⚠️  Warning: "Recorded" exists as a file, converting to directory...' + colors.reset);

        // Backup the file if it has content
        try {
          const content = await fs.readFile(recordedPath, 'utf-8');
          if (content.trim()) {
            await fs.writeFile(recordedPath + '.backup', content);
            console.log(colors.cyan + '   📄 Backed up to: Recorded.backup' + colors.reset);
          }
        } catch (e) {
          // File might be binary or unreadable, that's OK
        }

        // Delete file and create directory
        fsSync.unlinkSync(recordedPath);
        fsSync.mkdirSync(recordedPath, { recursive: true });
        console.log(colors.green + '   ✅ Converted "Recorded" to directory' + colors.reset);
      }
    } else {
      // Create directory if it doesn't exist
      fsSync.mkdirSync(recordedPath, { recursive: true });
    }
  } catch (error) {
    console.log(colors.red + '⚠️  Warning: Could not validate Recorded directory: ' + error.message + colors.reset);
  }
}

/**
 * Record & Auto-Generate (Option 1)
 * Integrated Playwright recording with auto-generation, validation, and testing
 */
async function recordAndGenerate() {
  console.log(colors.green + '\n🎥 Record & Auto-Generate Test\n' + colors.reset);
  console.log('This will open Playwright Inspector to record your actions,');
  console.log('then automatically generate all test files with validation!\n');

  // Load default URL from configurations.properties
  const fs = require('fs');
  const fsPromises = require('fs').promises;
  let configUrl = '';

  try {
    const configPath = path.join(process.cwd(), 'src/test/resources/configurations.properties');
    console.log(colors.dim + `[DEBUG] Reading config from: ${configPath}` + colors.reset);
    const configContent = await fsPromises.readFile(configPath, 'utf-8');

    const config = {};
    configContent.split('\n').forEach(line => {
      const trimmedLine = line.trim().replace(/\r$/, '');
      const [key, ...valueParts] = trimmedLine.split('=');
      if (key && valueParts.length > 0) {
        config[key.trim()] = valueParts.join('=').trim();
      }
    });

    configUrl = config.URL || '';
    console.log(colors.dim + `[DEBUG] Raw URL from config: "${configUrl}"` + colors.reset);
    if (configUrl) {
      // Unescape Java properties format (e.g., \: to :)
      configUrl = configUrl.replace(/\\:/g, ':');
      console.log(colors.cyan + `\n✓ Loaded default URL from config: ${configUrl}\n` + colors.reset);
    } else {
      console.log(colors.dim + '[DEBUG] No URL found in config' + colors.reset);
    }
  } catch (error) {
    console.log(colors.yellow + '⚠️  Could not load URL from configurations.properties: ' + error.message + colors.reset);
  }

  // Get test details
  const featureName = await question(colors.cyan + '📝 Feature Name (e.g., Login, Profile): ' + colors.reset);

  let pageUrl = '';
  if (configUrl) {
    const urlInput = await question(colors.cyan + `🌐 Starting URL (press Enter for default: ${configUrl}): ` + colors.reset);
    pageUrl = urlInput.trim() || configUrl;
  } else {
    pageUrl = await question(colors.cyan + '🌐 Starting URL (e.g., https://example.com/login): ' + colors.reset);
  }

  const jiraStory = await question(colors.cyan + '🎫 JIRA Story ID (optional, e.g., ECS-123): ' + colors.reset);

  if (!featureName.trim()) {
    console.log(colors.red + '\n❌ Feature name is required!' + colors.reset);
    return;
  }

  if (!pageUrl.trim()) {
    console.log(colors.red + '\n❌ Starting URL is required!' + colors.reset);
    if (!configUrl) {
      console.log(colors.yellow + '💡 Tip: Set URL in src/test/resources/configurations.properties to use as default.' + colors.reset);
    }
    return;
  }

  const jiraId = jiraStory.trim() || 'AUTO-GEN';

  // Create recording directory
  const recordingDir = `Recorded/recording_${featureName}_${Date.now()}`;
  const recordingFile = path.join(recordingDir, 'recorded-actions.java');

  try {
    if (!fs.existsSync('Recorded')) {
      fs.mkdirSync('Recorded', { recursive: true });
    }
    if (!fs.existsSync(recordingDir)) {
      fs.mkdirSync(recordingDir, { recursive: true });
    }
  } catch (error) {
    console.log(colors.red + '\n❌ Failed to create recording directory: ' + error.message + colors.reset);
    return;
  }

  console.log(colors.yellow + '\n🚀 Starting recording process...' + colors.reset);
  console.log(colors.cyan + '📝 Feature: ' + featureName + colors.reset);
  console.log(colors.cyan + '🌐 URL: ' + pageUrl + colors.reset);
  console.log(colors.cyan + '🎫 JIRA: ' + jiraId + colors.reset);
  console.log(colors.cyan + '💾 Recording will be saved to: ' + recordingFile + colors.reset);

  console.log(colors.yellow + '\n⚠️  IMPORTANT Instructions:' + colors.reset);
  console.log('   1. Playwright Inspector will open with your starting URL');
  console.log('   2. Perform your test actions in the browser (click, type, etc.)');
  console.log('   3. Recording is AUTO-SAVED to the file');
  console.log('   4. Close the BROWSER window when done');
  console.log('   5. Files will be AUTO-GENERATED after recording\n');

  await new Promise((resolve) => setTimeout(resolve, 2000));

  console.log(colors.bright + '🎬 Opening Playwright Inspector...\n' + colors.reset);

  return new Promise(async (resolve, reject) => {
    try {
      // Validate Playwright installation before starting
      try {
        const checkInstall = spawn('npx', ['playwright', '--version'], {
          cwd: process.cwd(),
          shell: true,
          stdio: 'pipe'
        });

        let installCheckFailed = false;
        checkInstall.on('error', () => { installCheckFailed = true; });

        await new Promise((resolveCheck) => {
          checkInstall.on('close', (code) => {
            if (code !== 0 || installCheckFailed) {
              console.log(colors.red + '\n❌ Playwright is not properly installed!' + colors.reset);
              console.log(colors.yellow + '\n💡 Installing Playwright browsers...\n' + colors.reset);

              const installProcess = spawn('npx', ['playwright', 'install'], {
                cwd: process.cwd(),
                shell: true,
                stdio: 'inherit'
              });

              installProcess.on('close', (installCode) => {
                if (installCode !== 0) {
                  console.log(colors.red + '\n❌ Failed to install Playwright browsers!' + colors.reset);
                  console.log(colors.yellow + '💡 Please run: npx playwright install\n' + colors.reset);
                  reject(new Error('Playwright installation failed'));
                } else {
                  console.log(colors.green + '\n✅ Playwright browsers installed successfully!\n' + colors.reset);
                  resolveCheck();
                }
              });
            } else {
              resolveCheck();
            }
          });
        });
      } catch (checkError) {
        console.log(colors.yellow + '⚠️  Could not verify Playwright installation: ' + checkError.message + colors.reset);
      }

      // Launch Playwright Codegen with auto-save to file
      const codegenProcess = spawn('npx', [
        'playwright',
        'codegen',
        '--target', 'java',
        '--output', recordingFile,
        pageUrl
      ], {
        cwd: process.cwd(),
        shell: true,
        stdio: 'inherit'
      });

      console.log(colors.green + '✅ Playwright Inspector launched!' + colors.reset);
      console.log(colors.yellow + '📝 Recording in progress... Close browser when done.\n' + colors.reset);

      codegenProcess.on('close', async (code) => {
        console.log(colors.cyan + '\n📋 Recording session ended.\n' + colors.reset);

        // Check if recording file was created
        if (!fs.existsSync(recordingFile)) {
          console.log(colors.red + '❌ No recording file found. Recording may have been cancelled.\n' + colors.reset);
          resolve();
          return;
        }

        // Check if recording file has content
        const stats = fs.statSync(recordingFile);
        if (stats.size === 0) {
          console.log(colors.red + '❌ Recording file is empty. No actions were recorded.\n' + colors.reset);
          resolve();
          return;
        }

        console.log(colors.green + '✅ Recording saved successfully (' + stats.size + ' bytes)\n' + colors.reset);
        console.log(colors.yellow + '🔄 Auto-generating test files...\n' + colors.reset);

        // First, compile the project to ensure TestGeneratorHelper and dependencies are compiled
        console.log(colors.cyan + '🔨 Compiling framework classes...\n' + colors.reset);

        const compileFramework = spawn('mvn', ['compile', '-q'], {
          cwd: process.cwd(),
          shell: true,
          stdio: 'inherit'
        });

        compileFramework.on('close', (compileCode) => {
          if (compileCode !== 0) {
            console.log(colors.red + '\n❌ Framework compilation failed!' + colors.reset);
            console.log(colors.yellow + '💡 Please fix compilation errors and try again.\n' + colors.reset);
            resolve();
            return;
          }

          console.log(colors.green + '✅ Framework compiled successfully\n' + colors.reset);

          // Call TestGeneratorHelper to generate files
          // Use temp batch file to avoid Windows CMD quoting issues
          const escapedRecordingFile = recordingFile.replace(/\\/g, '\\\\');
          const escapedPageUrl = pageUrl.replace(/\\/g, '\\\\');

          // For -Dexec.args, use space-separated values
          const execArgsValue = `${escapedRecordingFile} ${featureName} ${escapedPageUrl} ${jiraId}`;

          console.log(colors.dim + `[DEBUG]URL being passed: ${escapedPageUrl}` + colors.reset);

          // Create temporary batch file with the Maven command
          const tempBatchFile = path.join(process.cwd(), 'temp_generate.bat');
          const batchContent = `@echo off\nmvn exec:java -e -Dexec.mainClass=configs.TestGeneratorHelper "-Dexec.args=${execArgsValue}"`;

          fs.writeFileSync(tempBatchFile, batchContent);
          console.log(colors.dim + `[DEBUG] Created temp batch: ${tempBatchFile}` + colors.reset);

          // Execute the batch file
          const generateProcess = spawn(tempBatchFile, [], {
            cwd: process.cwd(),
            stdio: 'inherit',
            shell: true
          });

          // Cleanup and original handler
          generateProcess.on('close', (genCode) => {
            // Clean up batch file
            try {
              fs.unlinkSync(tempBatchFile);
            } catch (err) {
              // Ignore cleanup errors
            }

            // Continue with original logic
            if (genCode === 0) {
              console.log(colors.green + '\n\n✅ Test files generated successfully!' + colors.reset);
              console.log(colors.cyan + '\n📋 Generated Files:' + colors.reset);
              console.log(`   ✓ src/main/java/pages/${featureName}.java`);
              console.log(`   ✓ src/test/java/features/${featureName.toLowerCase()}.feature`);
              console.log(`   ✓ src/test/java/stepDefs/${featureName}Steps.java`);

              console.log(colors.yellow + '\n🔨 Compiling generated files...\n' + colors.reset);

              // Compile to verify
              const compileProcess = spawn('mvn', ['clean', 'compile', '-DskipTests'], {
                cwd: process.cwd(),
                shell: true,
                stdio: 'inherit'
              });

              compileProcess.on('close', async (compileCode) => {
                if (compileCode === 0) {
                  console.log(colors.green + '\n\n✅ Compilation successful! Your test is ready to use.' + colors.reset);
                  console.log(colors.cyan + '\n💡 Next Steps:' + colors.reset);
                  console.log('   1. Review generated files for accuracy');
                  console.log('   2. Run with: npm run tag -- --tags @' + featureName.toLowerCase());
                  console.log('   3. Or run all tests with: npm run run\n');

                  // Clean up recording directory after successful generation
                  try {
                    const rimraf = require('fs').rmSync || require('fs').rmdirSync;
                    rimraf(recordingDir, { recursive: true, force: true });
                    console.log(colors.dim + `✓ Cleaned up recording directory: ${recordingDir}` + colors.reset);
                  } catch (cleanupErr) {
                    console.log(colors.dim + `⚠️  Could not clean up recording directory: ${cleanupErr.message}` + colors.reset);
                  }
                } else {
                  console.log(colors.yellow + '\n⚠️  Compilation had issues. Please review the errors above.' + colors.reset);
                  console.log(colors.cyan + '💡 You can fix errors with: npm run validate\n' + colors.reset);
                }
                resolve();
              });
            } else {
              console.log(colors.red + '\n❌ File generation failed! Check the errors above.\n' + colors.reset);
              console.log(colors.yellow + '💡 Troubleshooting:' + colors.reset);
              console.log('   - Ensure recording file has valid Playwright Java code');
              console.log('   - Check if feature name is valid (alphanumeric only)');
              console.log('   - Try Option 1B (Retry) if you want to regenerate\n');
              resolve();
            }
          });
        }); // Close compileFramework.on('close')
      });

      codegenProcess.on('error', (error) => {
        console.log(colors.red + '\n❌ Failed to start Playwright: ' + error.message + colors.reset);
        console.log(colors.yellow + '\n💡 Possible solutions:' + colors.reset);
        console.log('   1. Run: npm run setup (installs Playwright)');
        console.log('   2. Or manually run: npx playwright install');
        console.log('   3. Ensure Node.js and npm are installed\n');
        reject(error);
      });

    } catch (error) {
      console.log(colors.red + '\n❌ Failed to start recording: ' + error.message + colors.reset);
      reject(error);
    }
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
 * Validates if a string is a valid Java identifier
 * @param {string} identifier - The identifier to validate
 * @returns {boolean} - True if valid Java identifier
 */
function isValidJavaIdentifier(identifier) {
  if (!identifier || identifier.length === 0) {
    return false;
  }

  // Java reserved keywords
  const keywords = [
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class',
    'const', 'continue', 'default', 'do', 'double', 'else', 'enum', 'extends', 'final',
    'finally', 'float', 'for', 'goto', 'if', 'implements', 'import', 'instanceof', 'int',
    'interface', 'long', 'native', 'new', 'package', 'private', 'protected', 'public',
    'return', 'short', 'static', 'strictfp', 'super', 'switch', 'synchronized', 'this',
    'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while'
  ];

  if (keywords.includes(identifier)) {
    return false;
  }

  // First character must be letter, underscore, or dollar sign
  const first = identifier.charAt(0);
  if (!(/[a-zA-Z_$]/.test(first))) {
    return false;
  }

  // Rest must be alphanumeric, underscore, or dollar sign
  for (let i = 1; i < identifier.length; i++) {
    if (!(/[a-zA-Z0-9_$]/.test(identifier.charAt(i)))) {
      return false;
    }
  }

  return true;
}

/**
 * Converts string to valid PascalCase Java class name
 * @param {string} input - The input string
 * @returns {string} - Valid PascalCase class name
 */
function toPascalCase(input) {
  if (!input || input.length === 0) {
    return 'DefaultClass';
  }

  // Remove all non-alphanumeric except spaces
  const cleaned = input
    .replace(/[^a-zA-Z0-9\\s]/g, ' ')
    .trim()
    .replace(/\\s+/g, ' ');

  if (cleaned.length === 0) {
    return 'DefaultClass';
  }

  // Convert to PascalCase
  const words = cleaned.split(/\\s+/);
  let result = '';

  for (const word of words) {
    if (word.length > 0) {
      result += word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    }
  }

  // Ensure it starts with a letter
  if (!/[a-zA-Z]/.test(result.charAt(0))) {
    result = 'Class' + result;
  }

  // Validate and return
  if (isValidJavaIdentifier(result)) {
    return result;
  } else {
    console.warn(`⚠️  WARNING: Generated invalid class name '${result}', using DefaultClass`);
    return 'DefaultClass';
  }
}

/**
 * Converts string to valid camelCase Java method name
 * @param {string} input - The input string
 * @returns {string} - Valid camelCase method name
 */
function toCamelCase(input) {
  if (!input || input.length === 0) {
    return 'defaultMethod';
  }

  const pascalCase = toPascalCase(input);
  const camelCase = pascalCase.charAt(0).toLowerCase() + pascalCase.slice(1);

  // Validate and return
  if (isValidJavaIdentifier(camelCase)) {
    return camelCase;
  } else {
    console.warn(`⚠️  WARNING: Generated invalid method name '${camelCase}', using defaultMethod`);
    return 'defaultMethod';
  }
}

/**
 * Generate Page Object from recording
 */
function generatePageObjectFromRecording(pageName, url, locators) {
  // Use strict Java naming validation
  const className = toPascalCase(pageName);
  console.log(`[JAVA VALIDATION] Class name: '${pageName}' → '${className}'`);

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

  // Add action methods with validated naming
  const uniqueActions = [...new Set(locators.map(l => l.action))];
  locators.forEach((loc, idx) => {
    // Use strict camelCase validation for method names
    const methodName = toCamelCase(loc.name.toLowerCase().replace(/_/g, ' '));
    console.log(`[JAVA VALIDATION] Method name: '${loc.name}' → '${methodName}'`);

    code += `    /**\n     * ${loc.comment}\n     */\n`;

    switch (loc.action) {
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
    code += `        System.out.println("📍 Step: ${step.text}");\n`;
    code += `        ${pageName}.${methodName}(page);\n`;
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
      const trimmedLine = line.trim().replace(/\r$/, ''); // Remove carriage returns
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
/**
 * Display main menu - renders from MENU_CONFIG
 */
async function displayMenu() {
  console.log(colors.yellow + '\n╔════════════════════════════════════════════════════════════╗' + colors.reset);
  console.log(colors.yellow + '║' + colors.reset + colors.bright + '           ' + MENU_CONFIG.title + '                      ' + colors.reset + colors.yellow + '║' + colors.reset);
  console.log(colors.yellow + '╚════════════════════════════════════════════════════════════╝' + colors.reset);
  console.log('');

  // Render each section
  MENU_CONFIG.sections.forEach(section => {
    console.log(colors.bright + section.name + ':' + colors.reset);

    section.items.forEach(item => {
      const keyDisplay = `  ${item.key}`.padEnd(5) + ' ';
      const title = item.subtitle
        ? colors.green + `${item.icon} ${item.title}` + colors.reset + ` (${item.subtitle})`
        : colors.green + `${item.icon} ${item.title}` + colors.reset;

      console.log(keyDisplay + title);

      if (item.description) {
        console.log(`        └─ ${item.description}`);
      }
    });

    console.log('');
  });

  const choice = await question(colors.cyan + '👉 Enter your choice: ' + colors.reset);
  return choice.trim().toUpperCase();
}

/**
 * Execute menu action based on user choice
 */
async function executeMenuAction(choice) {
  // Normalize choice to uppercase for letter options
  const normalizedChoice = choice.toUpperCase();

  // Find the action from menu config
  for (const section of MENU_CONFIG.sections) {
    const item = section.items.find(i => i.key.toUpperCase() === normalizedChoice);
    if (item) {
      // Special handling for exit
      if (item.action === 'exit') {
        return { exit: true };
      }

      // Get the function by name and execute it
      const actionFunction = eval(item.action);
      if (typeof actionFunction === 'function') {
        await actionFunction();
        return { exit: false };
      } else {
        console.log(colors.red + '\n❌ Action not implemented: ' + item.action + '\n' + colors.reset);
        return { exit: false };
      }
    }
  }

  // Invalid choice
  console.log(colors.red + '\n❌ Invalid choice. Please try again.\n' + colors.reset);
  return { exit: false };
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
  console.log(`    • Performance: ${verification.performance ? '✅ (<' + (performanceThreshold / 1000) + 's)' : '❌'}`);
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
 * Generate enhanced scenarios based on verification criteria
 * Creates comprehensive test scenarios for functional, UI, UX, and performance testing
 */
function generateEnhancedScenarios(testName, elements, userScenarios, verification) {
  const scenarios = [];
  let priority = 0;

  // Add user-defined scenarios first
  userScenarios.forEach(scenario => {
    const tags = verification.performance ? '@Functional @PerformanceTest' : '@Functional';
    scenarios.push({
      name: scenario.name,
      steps: scenario.steps,
      tags: tags
    });
  });
  priority = scenarios.length;

  // Add Functional Verification Scenarios
  if (verification.functional) {
    // Negative scenario - Empty fields
    if (elements.some(el => el.action === 'type')) {
      scenarios.push({
        name: `Verify ${testName} with empty required fields`,
        tags: '@Functional @Negative',
        steps: [
          `Given User navigates to ${testName} page`,
          'When User leaves all required fields empty',
          `And User attempts to submit`,
          'Then Appropriate validation messages should be displayed',
          'And Submit action should be prevented'
        ]
      });

      // Negative scenario - Invalid data
      scenarios.push({
        name: `Verify ${testName} with invalid data`,
        tags: '@Functional @Negative',
        steps: [
          `Given User navigates to ${testName} page`,
          'When User enters invalid data in all fields',
          `And User attempts to submit`,
          'Then Validation errors should be displayed for invalid fields',
          'And Invalid fields should be highlighted'
        ]
      });
    }

    // Boundary testing
    scenarios.push({
      name: `Verify ${testName} with boundary values`,
      tags: '@Functional @Boundary',
      steps: [
        `Given User navigates to ${testName} page`,
        'When User enters minimum valid values',
        `And User submits the form`,
        'Then Action should complete successfully',
        'When User enters maximum valid values',
        `And User submits the form`,
        'Then Action should complete successfully'
      ]
    });
  }

  // Add UI Verification Scenarios
  if (verification.ui) {
    scenarios.push({
      name: `Verify ${testName} UI elements are displayed correctly`,
      tags: '@UI @Visual',
      steps: [
        `Given User navigates to ${testName} page`,
        'Then All required elements should be visible',
        'And All elements should have correct labels',
        'And All buttons should be properly styled',
        'And Page layout should be correct'
      ]
    });

    scenarios.push({
      name: `Verify ${testName} element states`,
      tags: '@UI @State',
      steps: [
        `Given User navigates to ${testName} page`,
        'Then All input fields should be enabled',
        'And Submit button should be in correct initial state',
        'When User interacts with elements',
        'Then Element states should update appropriately',
        'And Disabled states should be respected'
      ]
    });
  }

  // Add UX Verification Scenarios
  if (verification.ux) {
    scenarios.push({
      name: `Verify ${testName} user experience flow`,
      tags: '@UX @UserFlow',
      steps: [
        `Given User navigates to ${testName} page`,
        'When User completes the workflow step by step',
        'Then Each step transition should be smooth',
        'And User should receive clear feedback at each step',
        'And Navigation should be intuitive'
      ]
    });

    scenarios.push({
      name: `Verify ${testName} error handling and recovery`,
      tags: '@UX @ErrorHandling',
      steps: [
        `Given User navigates to ${testName} page`,
        'When User encounters an error',
        'Then Clear error message should be displayed',
        'And User should be able to recover from error',
        'And Previously entered data should be preserved'
      ]
    });
  }

  // Add Performance Verification Scenarios
  if (verification.performance) {
    scenarios.push({
      name: `Verify ${testName} page load performance`,
      tags: '@Performance @LoadTime',
      steps: [
        `Given User navigates to ${testName} page`,
        'Then Page should load completely within threshold',
        'And All elements should be interactive',
        'And No performance bottlenecks should exist'
      ]
    });

    scenarios.push({
      name: `Verify ${testName} action response time`,
      tags: '@Performance @ResponseTime',
      steps: [
        `Given User navigates to ${testName} page`,
        'And Page is fully loaded',
        'When User performs actions',
        'Then Each action should respond within performance threshold',
        'And UI should remain responsive throughout'
      ]
    });
  }

  return scenarios;
}

/**
 * Validate and fix Page Object code before writing to disk
 * Prevents common compilation errors by ensuring required imports and declarations
 */
function validateAndFixPageObject(code, className) {
  let fixedCode = code;

  // AUTO-FIX 1: Convert protected methods to public for accessibility
  if (fixedCode.includes('protected static void')) {
    console.log(colors.yellow + '[AUTO-FIX] Converting protected methods to public for accessibility' + colors.reset);
    const protectedCount = (fixedCode.match(/protected static void/g) || []).length;
    fixedCode = fixedCode.replace(/protected static void/g, 'public static void');
    console.log(colors.green + `[AUTO-FIX] Fixed ${protectedCount} method(s) from protected → public` + colors.reset);
  }

  // AUTO-FIX 2: Ensure navigateTo method exists with proper signature
  if (!fixedCode.includes('public static void navigateTo')) {
    console.log(colors.yellow + '[AUTO-FIX] Adding missing navigateTo method' + colors.reset);

    const navigateMethod = `
    /**
     * Navigate to ${className} page
     * @param page Playwright Page instance
     */
    public static void navigateTo(Page page) {
        log.info("🌐 Navigating to ${className} page");
        String url = loadProps.getProperty("URL");
        navigateToUrl(url);
        log.info("✅ Navigation completed");
    }
`;

    // Insert after constructor
    const constructorMatch = fixedCode.match(/public\s+\w+\s*\(\s*\)\s*\{[^}]*\}\n/);
    if (constructorMatch) {
      const insertPoint = constructorMatch.index + constructorMatch[0].length;
      fixedCode = fixedCode.slice(0, insertPoint) + navigateMethod + fixedCode.slice(insertPoint);
    }
  }

  // AUTO-FIX 3: Add imports only when their types are actually referenced in the file.
  // This prevents unused-import warnings/errors in generated code.
  const conditionalImports = [
    {
      check: 'import com.microsoft.playwright.Locator;',
      import: 'import com.microsoft.playwright.Locator;\n',
      used: () => /\bLocator\b/.test(fixedCode)
    },
    {
      check: 'import com.microsoft.playwright.Page;',
      import: 'import com.microsoft.playwright.Page;\n',
      used: () => /\bPage[\s,)]/.test(fixedCode)
    },
    {
      check: 'import com.microsoft.playwright.options.AriaRole;',
      import: 'import com.microsoft.playwright.options.AriaRole;\n',
      used: () => /AriaRole\./.test(fixedCode)
    },
    {
      check: 'import configs.loadProps;',
      import: 'import configs.loadProps;\n',
      used: () => /\bloadProps\./.test(fixedCode)
    },
    {
      check: 'import configs.TimeoutConfig;',
      import: 'import configs.TimeoutConfig;\n',
      used: () => /\bTimeoutConfig\./.test(fixedCode)
    }
  ];

  let missingImports = '';
  conditionalImports.forEach(({ check, import: importStatement, used }) => {
    if (!fixedCode.includes(check) && used()) {
      missingImports += importStatement;
    }
  });

  if (missingImports) {
    console.log(colors.yellow + '[AUTO-FIX] Adding missing imports' + colors.reset);
    const packageMatch = fixedCode.match(/package\s+[\w.]+;\s*\n/);
    if (packageMatch) {
      const insertPoint = packageMatch.index + packageMatch[0].length;
      fixedCode = fixedCode.slice(0, insertPoint) + missingImports + fixedCode.slice(insertPoint);
    }
  }

  // VALIDATION 4: Ensure Logger import and instance if log.* methods are used
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

  // VALIDATION 5: Fix old-pattern methods that still use String locator (legacy guard)
  fixedCode = fixedCode.replace(/\(String locator,\s*String text\)/g, '(Page page, String text)');
  fixedCode = fixedCode.replace(/\(String locator\)/g, '(Page page)');
  fixedCode = fixedCode.replace(/clickOnElement\(locator\)/g, '// TODO: replace with clickOnElement(yourLocatorMethod())');
  fixedCode = fixedCode.replace(/enterText\(locator,\s*text\)/g, '// TODO: replace with enterText(yourLocatorMethod(), text)');
  fixedCode = fixedCode.replace(/selectDropDownValueByText\(locator,\s*text\)/g, '// TODO: replace with selectDropDownValueByText(yourLocatorMethod(), text)');

  // VALIDATION 7: Sanitize double-quote usage inside Java string literals for locators.
  // CSS attribute selectors like [role="button"] break the Java string when the
  // outer delimiter is also ". Convert inner attribute quotes to single quotes.
  // Covers: page.locator("..."), .filter("..."), setName("...") patterns.
  //
  // Strategy: walk through every Java string literal (content between ") and replace
  // any CSS-attribute double-quotes [attr="val"] → [attr='val'].
  fixedCode = fixedCode.replace(
    /page\.locator\("([^"]*?)"\)/g,
    (match, inner) => {
      const safe = inner.replace(/\[([\w-]+)="([^"]*?)"\]/g, "[$1='$2']");
      return `page.locator("${safe}")`;
    }
  );
  fixedCode = fixedCode.replace(
    /\.filter\(new Locator\.FilterOptions\(\)\.setHasText\("([^"]*?)"\)\)/g,
    (match, inner) => {
      const safe = inner.replace(/"/g, "'");
      return `.filter(new Locator.FilterOptions().setHasText("${safe}"))`;
    }
  );
  // Also fix raw .filter("...") selector strings
  fixedCode = fixedCode.replace(
    /\.filter\("([^"]*?)"\)/g,
    (match, inner) => {
      const safe = inner.replace(/\[([\w-]+)="([^"]*?)"\]/g, "[$1='$2']");
      return `.filter("${safe}")`;
    }
  );
  if (fixedCode !== code) {
    console.log(colors.yellow + '[AUTO-FIX] Fixed double-quote inside locator string literals' + colors.reset);
  }

  // VALIDATION 6: Ensure TimeoutConfig import if used
  if (fixedCode.includes('TimeoutConfig.') && !fixedCode.includes('import configs.TimeoutConfig')) {
    const packageMatch = fixedCode.match(/package\s+[\w.]+;\s*\n/);
    if (packageMatch) {
      const insertPoint = packageMatch.index + packageMatch[0].length;
      fixedCode = fixedCode.slice(0, insertPoint) + 'import configs.TimeoutConfig;\n' + fixedCode.slice(insertPoint);
    }
  }

  console.log(colors.green + '[AUTO-FIX] Page Object validated and fixed' + colors.reset);
  return fixedCode;
}

/**
 * AUTO-FIX: Cleanup Placeholder Step Definitions
 * Removes step definitions that are just placeholders (TODO comments, empty bodies)
 * Prevents accumulation of non-functional step definitions
 */
function cleanupPlaceholderStepDefinitions(stepDefsContent) {
  console.log(colors.cyan + '[AUTO-FIX] Cleaning up placeholder step definitions...' + colors.reset);

  let cleanedContent = stepDefsContent;
  let removedCount = 0;

  // Pattern 1: Methods with only TODO comments
  const todoPattern = /@(?:Given|When|Then)\("([^"]+)"\)\s*\n\s*public\s+void\s+\w+\s*\(\s*\)\s*\{\s*\n\s*\/\/\s*TODO:[^\n]*\n\s*System\.out\.println\([^)]*Step not yet implemented[^)]*\);\s*\n\s*\}\s*\n/g;
  const todoMatches = cleanedContent.match(todoPattern);
  if (todoMatches) {
    removedCount += todoMatches.length;
    cleanedContent = cleanedContent.replace(todoPattern, '');
    console.log(colors.yellow + `[AUTO-FIX] Removed ${todoMatches.length} TODO placeholder step(s)` + colors.reset);
  }

  // Pattern 2: Completely empty methods (just {})
  const emptyPattern = /@(?:Given|When|Then)\("([^"]+)"\)\s*\n\s*public\s+void\s+\w+\s*\(\s*\)\s*\{\s*\}\s*\n/g;
  const emptyMatches = cleanedContent.match(emptyPattern);
  if (emptyMatches) {
    removedCount += emptyMatches.length;
    cleanedContent = cleanedContent.replace(emptyPattern, '');
    console.log(colors.yellow + `[AUTO-FIX] Removed ${emptyMatches.length} empty placeholder step(s)` + colors.reset);
  }

  // Pattern 3: Methods with only single comment line
  const commentOnlyPattern = /@(?:Given|When|Then)\("([^"]+)"\)\s*\n\s*public\s+void\s+\w+\s*\(\s*\)\s*\{\s*\n\s*\/\/[^\n]*\n\s*\}\s*\n/g;
  const commentMatches = cleanedContent.match(commentOnlyPattern);
  if (commentMatches) {
    removedCount += commentMatches.length;
    cleanedContent = cleanedContent.replace(commentOnlyPattern, '');
    console.log(colors.yellow + `[AUTO-FIX] Removed ${commentMatches.length} comment-only placeholder step(s)` + colors.reset);
  }

  if (removedCount === 0) {
    console.log(colors.green + '[AUTO-FIX] No placeholder steps found' + colors.reset);
  } else {
    console.log(colors.green + `[AUTO-FIX] ✅ Cleaned up ${removedCount} total placeholder step(s)` + colors.reset);
  }

  return cleanedContent;
}

/**
 * AUTO-FIX: Validate and Fix Step Matching between Feature File and Step Definitions
 * Ensures all steps in feature file have corresponding step definition implementations
 */
function validateAndFixStepMatching(featureContent, stepDefsContent, testName) {
  console.log(colors.cyan + '[AUTO-FIX] Validating step matching...' + colors.reset);

  // Extract steps from feature file
  const featureSteps = extractStepsFromFeature(featureContent);
  console.log(`[AUTO-FIX] Found ${featureSteps.size} unique steps in feature file`);

  // Extract implemented steps from step definitions
  const existingSteps = extractStepsFromStepDefs(stepDefsContent);
  console.log(`[AUTO-FIX] Found ${existingSteps.size} implemented step definitions`);

  // Find missing steps
  const missingSteps = new Set([...featureSteps].filter(step => !existingSteps.has(step)));

  if (missingSteps.size === 0) {
    console.log(colors.green + '[AUTO-FIX] ✅ All feature steps have matching step definitions' + colors.reset);
    return stepDefsContent;
  }

  console.log(colors.yellow + `[AUTO-FIX] ⚠️ Found ${missingSteps.size} missing step definitions` + colors.reset);

  // Generate missing step definitions
  let generatedSteps = '\n    // AUTO-GENERATED MISSING STEP DEFINITIONS\n';
  generatedSteps += `    // ${missingSteps.size} steps were missing and auto-generated\n\n`;

  for (const step of missingSteps) {
    generatedSteps += generateStepDefinition(step);
  }

  // Insert before closing brace
  const lastBraceIndex = stepDefsContent.lastIndexOf('}');
  if (lastBraceIndex !== -1) {
    stepDefsContent = stepDefsContent.substring(0, lastBraceIndex) +
      generatedSteps + '\n' +
      stepDefsContent.substring(lastBraceIndex);
  }

  console.log(colors.green + `[AUTO-FIX] ✅ Generated ${missingSteps.size} missing step definitions` + colors.reset);
  return stepDefsContent;
}

/**
 * Extract steps from feature file (Given/When/Then/And/But)
 */
function extractStepsFromFeature(featureContent) {
  const steps = new Set();
  const stepPattern = /^\s*(Given|When|Then|And|But)\s+(.+)$/gm;
  let match;

  while ((match = stepPattern.exec(featureContent)) !== null) {
    let stepText = match[2].trim();
    // Normalize step text (remove trailing punctuation, extra spaces)
    stepText = stepText.replace(/\s+/g, ' ').replace(/[.!?]$/, '');
    steps.add(stepText);
  }

  return steps;
}

/**
 * Extract implemented steps from step definitions (@Given/@When/@Then)
 */
function extractStepsFromStepDefs(stepDefsContent) {
  const steps = new Set();
  const stepPattern = /@(?:Given|When|Then)\("([^"]+)"\)/g;
  let match;

  while ((match = stepPattern.exec(stepDefsContent)) !== null) {
    steps.add(match[1].trim());
  }

  return steps;
}

/**
 * Generate step definition method for a missing step
 * Creates proper implementations based on step text pattern matching
 */
function generateStepDefinition(stepText) {
  const keyword = determineStepKeyword(stepText);
  const methodName = stepText
    .split(' ')
    .slice(0, 5)
    .map((word, idx) =>
      idx === 0
        ? word.toLowerCase()
        : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    )
    .join('')
    .replace(/[^a-zA-Z0-9]/g, '');

  const lower = stepText.toLowerCase();
  let implementation = '';

  // Pattern-based implementation generation

  // Navigation/Setup patterns
  if (lower.match(/user is on|navigates? to|opens?|visits?|goes? to/i)) {
    implementation = `        // Navigate to page\n` +
      `        page.navigate(loadProps.getProperty("URL"));\n` +
      `        page.waitForLoadState();\n` +
      `        TimeoutConfig.waitShort();\n`;
  }
  // Click patterns
  else if (lower.match(/clicks?|selects?|chooses?|press(es)?/i)) {
    const target = extractTarget(stepText);
    implementation = `        // Click on ${target}\n` +
      `        Locator element = page.locator("button, a, [role='button']").filter(new Locator.FilterOptions().setHasText("${target}"));\n` +
      `        if (element.count() == 0) element = page.locator("*:has-text('${target}')");\n` +
      `        if (element.count() > 0) element.first().click();\n` +
      `        TimeoutConfig.waitShort();\n`;
  }
  // Type/Enter patterns
  else if (lower.match(/enters?|types?|inputs?|fills?/i)) {
    const target = extractTarget(stepText);
    implementation = `        // Enter text in ${target}\n` +
      `        Locator inputField = page.locator("input, textarea").filter(new Locator.FilterOptions().setHasText("${target}"));\n` +
      `        if (inputField.count() == 0) inputField = page.locator("input[name*='${target.toLowerCase()}'], textarea[name*='${target.toLowerCase()}']");\n` +
      `        if (inputField.count() > 0) {\n` +
      `            inputField.first().fill("test_data");\n` +
      `            TimeoutConfig.waitShort();\n` +
      `        }\n`;
  }
  // Verification patterns - Visible/Displayed
  else if (lower.match(/should be (visible|displayed|shown)/i)) {
    const target = extractTarget(stepText);
    implementation = `        // Verify ${target} is visible\n` +
      `        Locator element = page.locator("*:visible:has-text('${target}')");\n` +
      `        if (element.count() == 0) element = page.locator("*:visible");\n` +
      `        Assert.assertTrue(element.count() > 0, "${target} should be visible");\n`;
  }
  // Verification patterns - Enabled/Disabled
  else if (lower.match(/should be (enabled|disabled)/i)) {
    const target = extractTarget(stepText);
    const shouldBeDisabled = lower.includes('disabled');
    implementation = `        // Verify ${target} is ${shouldBeDisabled ? 'disabled' : 'enabled'}\n` +
      `        Locator element = page.locator("button, input, select").filter(new Locator.FilterOptions().setHasText("${target}"));\n` +
      `        if (element.count() == 0) element = page.locator("button, input, select").first();\n` +
      `        Assert.${shouldBeDisabled ? 'assertTrue' : 'assertFalse'}(element.first().isDisabled(), "${target} should be ${shouldBeDisabled ? 'disabled' : 'enabled'}");\n`;
  }
  // Verification patterns - Contains text
  else if (lower.match(/should (contain|display|show)/i)) {
    const target = extractTarget(stepText);
    implementation = `        // Verify page contains ${target}\n` +
      `        Locator element = page.locator("*:has-text('${target}')");\n` +
      `        Assert.assertTrue(element.count() > 0, "Page should contain '${target}'");\n`;
  }
  // Wait/Delay patterns
  else if (lower.match(/waits?|delays?|pauses?/i)) {
    implementation = `        // Wait for condition\n` +
      `        TimeoutConfig.waitMedium();\n`;
  }
  // Error/Validation patterns
  else if (lower.match(/error|validation|invalid|warning/i)) {
    implementation = `        // Verify error/validation message\n` +
      `        Locator error = page.locator(".error, .invalid, .warning, [class*='error'], [class*='invalid']");\n` +
      `        Assert.assertTrue(error.count() > 0, "Expected error/validation message to be displayed");\n`;
  }
  // Success patterns
  else if (lower.match(/success|successful|complete|logged in|submitted/i)) {
    implementation = `        // Verify success state\n` +
      `        Locator success = page.locator(".success, [class*='success'], *:has-text('Success')");\n` +
      `        Assert.assertTrue(success.count() > 0 || page.url().contains("success") || page.url().contains("dashboard"), "Expected success state");\n`;
  }
  // Default: Log-only implementation with assertion
  else {
    implementation = `        // Step: ${stepText}\n` +
      `        System.out.println("Executing: ${stepText}");\n` +
      `        page.waitForLoadState();\n` +
      `        // Add specific implementation as needed\n`;
  }

  return `    @${keyword}("${stepText}")\n` +
    `    public void ${methodName}() {\n${implementation}` +
    `    }\n\n`;
}

/**
 * Extract target element/text from step description
 */
function extractTarget(stepText) {
  // Try to extract quoted text first
  const quotedMatch = stepText.match(/"([^"]+)"/);
  if (quotedMatch) return quotedMatch[1];

  // Extract text after common keywords
  const patterns = [
    /(?:clicks?|selects?|on|in|to|the)\s+["']?([^"'\n]+?)["']?\s*$/i,
    /(?:enters?|types?|fills?)\s+(?:in|on|into)?\s*["']?([^"'\n]+?)["']?\s*$/i,
    /["']?([^"'\n]+?)["']?\s+(?:should|must|is|are)/i
  ];

  for (const pattern of patterns) {
    const match = stepText.match(pattern);
    if (match) return match[1].trim();
  }

  // Default: use last few meaningful words
  const words = stepText.split(' ').filter(w => w.length > 2);
  return words.slice(-2).join(' ');
}

/**
 * Determine appropriate Cucumber annotation keyword for a step
 */
function determineStepKeyword(stepText) {
  const lower = stepText.toLowerCase();

  // Given: Setup/preconditions (state, navigation, initialization)
  if (lower.match(/^(user is|page is|application is|system is|database|data)/)) {
    return 'Given';
  }

  // When: Actions (user interactions, operations)
  if (lower.match(/(clicks?|enters?|types?|selects?|submits?|navigates?|performs?|executes?|tries?|attempts?|focuses?)/)) {
    return 'When';
  }

  // Then: Assertions (verifications, expected results)
  if (lower.match(/(should|must|will|cannot|all|every|any|displayed|visible|enabled|disabled|contains?|shows?|appears?|prevented|accepted|rejected|validates?|verified|completes?|loads?|masked|protected|logged|tracked)/)) {
    return 'Then';
  }

  // Default to Then for verification-like steps
  return 'Then';
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
        enterText(${el.name}, text, TimeoutConfig.waitShort());
    }\n`;
        } else if (el.type === 'button') {
          methodCode = `\n    public void click${el.name.charAt(0).toUpperCase() + el.name.slice(1)}() {
        clickOnElement(${el.name}, TimeoutConfig.waitShort());
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

  // Check if story ID was provided via CLI argument
  let issueKey = global.cliJiraStoryId || '';

  if (!issueKey) {
    // Get JIRA issue key from user input
    issueKey = await question(colors.cyan + '🎫 Enter JIRA Story/Issue key (e.g., ECS-123, or press Enter to skip): ' + colors.reset);
  } else {
    console.log(colors.cyan + `🎫 Using JIRA Story: ${issueKey} (from command line)\n` + colors.reset);
  }

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
  console.log(`    • Performance: ${suggestedVerification.performance ? '✅ Enabled (<' + (suggestedVerification.performanceThreshold / 1000) + 's)' : '❌'}`);
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
  console.log(`    • Performance: ${verification.performance ? '✅ (<' + (performanceThreshold / 1000) + 's)' : '❌'}`);
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
    await autoCompileTestAndFix(finalTestName, elements, scenarios, verification, performanceThreshold, story.key);

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
 * 🎯 COMPREHENSIVE SCENARIO GENERATOR
 * Generates detailed test scenarios based on verification criteria
 * This makes AI smarter to create scenarios for functional, UI, UX, performance, and security testing
 */
function generateComprehensiveScenarios(testName, elements, userScenarios, verification) {
  const allScenarios = [...userScenarios];

  // Only enhance if verification flags are enabled
  if (!verification) return allScenarios;

  const testNameLower = testName.toLowerCase();
  const hasUsernameField = elements.some(el => el.name.toLowerCase().includes('username') || el.name.toLowerCase().includes('email'));
  const hasPasswordField = elements.some(el => el.name.toLowerCase().includes('password'));
  const hasSubmitButton = elements.some(el => el.action === 'click' && (el.name.toLowerCase().includes('submit') || el.name.toLowerCase().includes('login') || el.name.toLowerCase().includes('sign')));

  // ========== FUNCTIONAL TESTING ==========
  if (verification.functional) {
    // Negative scenarios
    allScenarios.push({
      name: `Verify ${testName} with invalid data`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User enters invalid data',
        'And User attempts to submit',
        'Then Validation error should be displayed',
        'And Action should be prevented'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} with empty required fields`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User leaves all required fields empty',
        'And User attempts to submit',
        'Then Appropriate validation messages should be displayed',
        'And Submit action should be prevented'
      ]
    });

    // Boundary testing
    if (hasUsernameField || hasPasswordField) {
      allScenarios.push({
        name: `Verify ${testName} with boundary values`,
        steps: [
          'Given User navigates to ' + testName + ' page',
          'When User enters minimum valid data',
          'And User submits the form',
          'Then Action should complete successfully'
        ]
      });

      allScenarios.push({
        name: `Verify ${testName} with maximum length values`,
        steps: [
          'Given User navigates to ' + testName + ' page',
          'When User enters maximum valid data',
          'And User submits the form',
          'Then Action should complete successfully'
        ]
      });
    }
  }

  // ========== UI TESTING ==========
  if (verification.ui) {
    allScenarios.push({
      name: `Verify ${testName} UI elements are displayed correctly`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        ...elements.map(el => `Then ${el.name} should be visible`),
        'And All elements should have correct labels',
        'And Page layout should be correct'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} element states`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'Then All input fields should be enabled',
        'When User enters data in fields',
        'Then Input fields should accept user input',
        'And Submit button should remain enabled'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} accessibility features`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'Then All form elements should have proper labels',
        'And Tab navigation should work correctly',
        'And Keyboard shortcuts should be functional',
        'And ARIA labels should be present'
      ]
    });
  }

  // ========== UX TESTING ==========
  if (verification.ux) {
    allScenarios.push({
      name: `Verify ${testName} user flow experience`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User focuses on first input field',
        'Then Field should be highlighted',
        'When User presses Tab key',
        'Then Focus should move to next field',
        'When User completes all fields and submits',
        'Then Smooth transition should occur'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} error handling and feedback`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User enters invalid data',
        'And User attempts to submit',
        'Then Clear error message should be displayed',
        'And Previously entered valid data should be preserved',
        'And User should be able to correct errors'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} provides appropriate feedback`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User submits valid data',
        'Then Loading indicator should appear',
        'When action completes',
        'Then Success feedback should be provided',
        'And User should see confirmation'
      ]
    });
  }

  // ========== PERFORMANCE TESTING ==========
  if (verification.performance) {
    const threshold = verification.performanceThreshold || 3000;

    allScenarios.push({
      name: `Verify ${testName} page load performance`,
      steps: [
        'When User navigates to ' + testName + ' page',
        `Then Page should load completely within ${threshold / 1000} seconds`,
        'And All elements should be interactive',
        'And No performance bottlenecks should exist'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} action response time`,
      steps: [
        'Given Page is fully loaded',
        'When User submits valid data',
        'Then Action should complete within 2 seconds',
        'And Server response should be fast',
        'And No UI freezing should occur'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} handles concurrent access`,
      steps: [
        'When Multiple users access page simultaneously',
        'Then Each action should process correctly',
        'And Response times should remain acceptable',
        'And No system degradation should occur'
      ]
    });
  }

  // ========== SECURITY TESTING (for login/authentication) ==========
  if (hasPasswordField && verification.functional) {
    allScenarios.push({
      name: `Verify ${testName} password security`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User types password',
        'Then Password characters should be masked',
        'And Password should not be visible in page source',
        'And No password leakage in network requests'
      ]
    });

    allScenarios.push({
      name: `Verify ${testName} brute force protection`,
      steps: [
        'Given User navigates to ' + testName + ' page',
        'When User makes multiple failed attempts',
        'Then Account should be temporarily locked after threshold',
        'And Appropriate warning should be displayed'
      ]
    });

    if (hasUsernameField) {
      allScenarios.push({
        name: `Verify ${testName} prevents SQL injection`,
        steps: [
          'Given User navigates to ' + testName + ' page',
          'When User enters SQL injection attempts in fields',
          'And User attempts to submit',
          'Then System should reject malicious input safely',
          'And No database errors should be exposed',
          'And Appropriate error message should be shown'
        ]
      });
    }
  }

  return allScenarios;
}

/**
 * 🎯 COMPREHENSIVE STEP DEFINITIONS GENERATOR
 * Generates complete Java step definitions with full implementations
 */
function generateComprehensiveStepDefinitions(className, testName, elements, verification) {

  // ── imports ────────────────────────────────────────────────────────────────
  // Only include LoadState if a step actually uses it (UI/perf verification blocks)
  const useLoadState = !!(verification?.ui || verification?.performance);
  // Only declare timing fields when the performance scenario is generated
  const usePerformanceFields = !!verification?.performance;

  let content = `package stepDefs;\n\n`;
  content += `import io.cucumber.java.en.*;\n`;
  content += `import configs.browserSelector;\n`;
  content += `import configs.TimeoutConfig;\n`;
  // loadProps is a page-object concern; step defs never call it directly
  content += `import pages.${className};\n`;
  content += `import org.testng.Assert;\n`;
  content += `import com.microsoft.playwright.Locator;\n`;
  if (useLoadState) content += `import com.microsoft.playwright.options.LoadState;\n`;
  content += `\n`;

  content += `/**\n * Step Definitions for ${testName}\n`;
  content += ` * Auto-generated by AI Automation CLI (Option 3)\n`;
  content += ` * Uses Page Object methods - NO inline locators in step definitions\n`;
  content += ` */\n`;
  content += `public class ${testName}Steps extends browserSelector {\n\n`;
  // Declare timing fields only when the performance check step will be generated
  if (usePerformanceFields) {
    content += `    private long startTime;\n    private long pageLoadTime;\n\n`;
  }

  // ── standard background steps ──────────────────────────────────────────────
  content += `    @Given("the application is ready")\n`;
  content += `    public void applicationIsReady() {\n`;
  content += `        page.waitForLoadState();\n`;
  content += `    }\n\n`;

  content += `    @Given("User navigates to ${testName} page")\n`;
  content += `    public void userNavigatesToPage() {\n`;
  if (usePerformanceFields) content += `        startTime = System.currentTimeMillis();\n`;
  content += `        ${className}.navigateTo${className}(page);\n`;
  if (usePerformanceFields) content += `        pageLoadTime = System.currentTimeMillis() - startTime;\n`;
  content += `    }\n\n`;

  // ── element-specific step definitions ──────────────────────────────────────
  // Each element gets a step that calls the corresponding page object method.
  // NO inline locators - all locators live in the page object.
  const seenSteps = new Set();

  elements.forEach(el => {
    // Skip login elements - they are handled by Login class
    const nameLower = el.name.toLowerCase();
    if (nameLower.includes('username') || nameLower.includes('password')
      || nameLower.includes('sign in') || nameLower.includes('login')) {
      return; // use existing Login.java steps
    }

    const pascalId = toPascalCaseId(el.name);
    const { prefix, callParam, sigParam } = elementActionParts(el);
    const pageMethod = `${prefix}${pascalId}`;   // e.g., "clickSubmitButton"
    const stepLabel = el.name.toLowerCase();    // e.g., "submit button"

    if (el.action === 'type' || prefix === 'enter') {
      const stepText = `user enters {string} into ${stepLabel}`;
      if (!seenSteps.has(stepText)) {
        seenSteps.add(stepText);
        const javaMethod = `enter${pascalId}`;
        content += `    @And("${stepText}")\n`;
        content += `    public void ${javaMethod}(String text) {\n`;
        content += `        System.out.println("📍 Step: Entering text into ${el.name}: '" + text + "'");\n`;
        content += `        ${className}.${pageMethod}(page, text);\n`;
        content += `    }\n\n`;
      }
    } else if (el.action === 'select' || prefix === 'select') {
      const stepText = `user selects {string} from ${stepLabel}`;
      if (!seenSteps.has(stepText)) {
        seenSteps.add(stepText);
        const javaMethod = `select${pascalId}`;
        content += `    @And("${stepText}")\n`;
        content += `    public void ${javaMethod}(String option) {\n`;
        content += `        System.out.println("📍 Step: Selecting option from ${el.name}: '" + option + "'");\n`;
        content += `        ${className}.${pageMethod}(page, option);\n`;
        content += `    }\n\n`;
      }
    } else {
      // click
      const stepText = `user clicks on ${stepLabel}`;
      if (!seenSteps.has(stepText)) {
        seenSteps.add(stepText);
        const javaMethod = `click${pascalId}`;
        content += `    @When("${stepText}")\n`;
        content += `    public void ${javaMethod}() {\n`;
        content += `        System.out.println("📍 Step: user clicks on ${el.name}");\n`;
        content += `        ${className}.${pageMethod}(page);\n`;
        content += `    }\n\n`;
      }
    }
  });

  // ── common negative / boundary step definitions ────────────────────────────
  content += `    @When("User enters invalid data")\n`;
  content += `    public void enterInvalidData() {\n`;
  content += `        Locator firstInput = page.locator("input:visible, textarea:visible").first();\n`;
  content += `        firstInput.fill("@@@InvalidData!!!");\n`;
  content += `        TimeoutConfig.waitShort();\n`;
  content += `    }\n\n`;

  content += `    @When("User attempts to submit")\n`;
  content += `    public void attemptSubmit() {\n`;
  content += `        Locator submitBtn = page.locator("button[type='submit'], input[type='submit'], button:has-text('Submit')").first();\n`;
  content += `        submitBtn.click();\n`;
  content += `        TimeoutConfig.waitShort();\n`;
  content += `    }\n\n`;

  content += `    @Then("Validation error should be displayed")\n`;
  content += `    public void validationError() {\n`;
  content += `        Locator error = page.locator(".error, [class*='error'], .invalid-feedback, [class*='invalid']");\n`;
  content += `        Assert.assertTrue(error.count() > 0, "Expected validation error to be displayed");\n`;
  content += `    }\n\n`;

  content += `    @When("User leaves all required fields empty")\n`;
  content += `    public void leaveFieldsEmpty() {\n`;
  content += `        page.locator("input[required]:visible, textarea[required]:visible").evaluateAll("elements => elements.forEach(el => el.value = '')");\n`;
  content += `        TimeoutConfig.waitShort();\n`;
  content += `    }\n\n`;

  content += `    @Then("Appropriate validation messages should be displayed")\n`;
  content += `    public void validationMessages() {\n`;
  content += `        Locator errors = page.locator(".error, [class*='error'], .invalid-feedback, :invalid");\n`;
  content += `        Assert.assertTrue(errors.count() > 0, "Expected validation messages to be displayed");\n`;
  content += `    }\n\n`;

  content += `    @Then("Submit action should be prevented")\n`;
  content += `    public void submitPrevented() {\n`;
  content += `        Locator submitBtn = page.locator("button[type='submit'], input[type='submit']").first();\n`;
  content += `        Assert.assertTrue(submitBtn.isDisabled(), "Expected submit button to be disabled");\n`;
  content += `    }\n\n`;

  // ── UI verification steps ──────────────────────────────────────────────────
  if (verification?.ui) {
    elements.forEach(el => {
      const pascalId = toPascalCaseId(el.name);
      const camelId = toCamelCaseId(el.name);
      const stepText = `${el.name} should be visible`;
      if (!seenSteps.has(stepText)) {
        seenSteps.add(stepText);
        // Use page object locator method where available, fallback to direct assertion
        content += `    @Then("${stepText}")\n`;
        content += `    public void ${camelId}Visible() {\n`;
        content += `        System.out.println("📍 Verify: ${el.name} should be visible");\n`;
        content += `        Assert.assertTrue(${className}.${camelId}().isVisible(), "${el.name} should be visible");\n`;
        content += `    }\n\n`;
      }
    });

    content += `    @Then("All required elements should be visible")\n`;
    content += `    public void allElementsVisible() {\n`;
    content += `        page.waitForLoadState(LoadState.DOMCONTENTLOADED);\n`;
    content += `        Assert.assertTrue(page.locator("body").isVisible(), "Page body should be present");\n`;
    content += `    }\n\n`;

    content += `    @Then("Page layout should be correct")\n`;
    content += `    public void pageLayoutCorrect() {\n`;
    content += `        page.waitForLoadState(LoadState.DOMCONTENTLOADED);\n`;
    content += `        Assert.assertTrue(page.locator("body").count() > 0, "Page body should be present");\n`;
    content += `    }\n\n`;
  }

  // ── Performance steps ──────────────────────────────────────────────────────
  if (verification?.performance) {
    content += `    @Then("Page should load completely within 3 seconds")\n`;
    content += `    public void pageLoadsQuickly() {\n`;
    content += `        Assert.assertTrue(pageLoadTime < 3000, "Page load time exceeded 3 seconds: " + pageLoadTime + "ms");\n`;
    content += `    }\n\n`;

    content += `    @Then("All elements should be interactive")\n`;
    content += `    public void elementsInteractive() {\n`;
    content += `        page.waitForLoadState(LoadState.NETWORKIDLE);\n`;
    content += `    }\n\n`;
  }

  content += `}\n`;
  return content;
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
 * Generate a smart Playwright Locator Java expression for a given element.
 *
 * Priority order (mirrors TestGeneratorHelper.java):
 *   1. Semantic locators: getByRole (BUTTON, LINK, COMBOBOX, CHECKBOX, TEXTBOX)
 *   2. Type-specific CSS: input[type='email'], input[type='password']
 *   3. Multi-strategy CSS: name/id/placeholder combinaton
 *   4. Fallback: getByText
 *
 * @param {Object} el  - { name, action, description }
 * @returns {string}   - Java expression, e.g. page.getByRole(AriaRole.BUTTON, ...)
 */
function generateSmartLocator(el) {
  const nameLower = el.name.toLowerCase();
  const action = (el.action || 'click').toLowerCase();

  // Strip trailing role word to get a clean label, e.g. "Submit Button" → "Submit"
  const label = el.name.replace(/\s+(field|button|btn|dropdown|select|input|checkbox|link|text|area|textbox)$/i, '').trim();
  // Escape any " in the label so it is safe to embed inside a Java string literal
  const safeLabel = label.replace(/"/g, '\\"');
  const cleanId = label.toLowerCase().replace(/[^a-z0-9]/g, '');  // "userName"  → "username"
  const cleanSel = label.toLowerCase().replace(/\s+/g, '-');        // "User Name" → "user-name"

  // ── Password ──────────────────────────────────────────────────────────────
  if (nameLower.includes('password') || nameLower.includes('pass')) {
    return "page.locator(\"input[type='password'], input[name*='password' i], input[id*='password' i]\")";
  }

  // ── E-mail ────────────────────────────────────────────────────────────────
  if (nameLower.includes('email') || nameLower.includes('e-mail')) {
    return "page.locator(\"input[type='email'], input[name*='email' i], input[id*='email' i]\")";
  }

  // ── Username ──────────────────────────────────────────────────────────────
  if (nameLower.includes('username') || nameLower.includes('user name')) {
    return "page.locator(\"input[type='email'], input[name*='user' i], input[id*='user' i]\")";
  }

  // ── Search ────────────────────────────────────────────────────────────────
  if (nameLower.includes('search')) {
    return `page.locator(\"input[name*='search' i], input[id*='search' i], input[placeholder*='search' i]\")`;
  }

  // ── Checkbox ──────────────────────────────────────────────────────────────
  if (nameLower.includes('checkbox') || nameLower.includes('check') || nameLower.includes('agree')) {
    return `page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("${safeLabel}"))`;
  }

  // ── Dropdown / Select ─────────────────────────────────────────────────────
  if (action === 'select' || nameLower.includes('dropdown') || nameLower.includes('select') || nameLower.includes('combo')) {
    return `page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("${safeLabel}"))`;
  }

  // ── Link ──────────────────────────────────────────────────────────────────
  if (nameLower.includes('link') || nameLower.includes('menu') || nameLower.includes('nav')) {
    return `page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("${safeLabel}"))`;
  }

  // ── Text input / textarea ─────────────────────────────────────────────────
  if (action === 'type' || nameLower.includes('field') || nameLower.includes('input')
    || nameLower.includes('text') || nameLower.includes('name')
    || nameLower.includes('phone') || nameLower.includes('address')) {
    return `page.locator("input[name*='${cleanId}' i], input[id*='${cleanId}' i], input[placeholder*='${safeLabel}' i], textarea[name*='${cleanId}' i]")`;
  }

  // ── Button (click actions, or explicit button name) ───────────────────────
  if (action === 'click' || nameLower.includes('button') || nameLower.includes('btn')
    || nameLower.includes('submit') || nameLower.includes('sign') || nameLower.includes('save')
    || nameLower.includes('add') || nameLower.includes('cancel') || nameLower.includes('open')
    || nameLower.includes('close') || nameLower.includes('delete') || nameLower.includes('update')) {
    return `page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("${safeLabel}"))`;
  }

  // ── Fallback ──────────────────────────────────────────────────────────────
  return `page.getByText("${safeLabel}")`;
}

/**
 * Generate action method prefix + parameter based on element action.
 * Returns { prefix, params, paramSignature } e.g.:
 *   click  → { prefix:"click",        params:"",          paramSignature:"" }
 *   type   → { prefix:"enter",        params:"text",      paramSignature:", String text" }
 *   select → { prefix:"select",       params:"option",    paramSignature:", String option" }
 */
function elementActionParts(el) {
  const action = (el.action || 'click').toLowerCase();
  if (action === 'type') return { prefix: 'enter', callParam: 'text', sigParam: 'String text' };
  if (action === 'select') return { prefix: 'select', callParam: 'option', sigParam: 'String option' };
  return { prefix: 'click', callParam: '', sigParam: '' };
}

/**
 * Convert element name to camelCase Java identifier.
 * "Username Field" → "usernameField"
 */
function toCamelCaseId(name) {
  const words = name.replace(/[^a-zA-Z0-9\s]/g, '').trim().split(/\s+/);
  return words.map((w, i) => i === 0 ? w.toLowerCase() : w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join('');
}

/**
 * Convert element name to PascalCase Java identifier.
 * "Username Field" → "UsernameField"
 */
function toPascalCaseId(name) {
  const words = name.replace(/[^a-zA-Z0-9\s]/g, '').trim().split(/\s+/);
  return words.map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join('');
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

      // 🎯 ENHANCEMENT: Generate comprehensive scenarios based on verification flags
      const enhancedScenarios = generateComprehensiveScenarios(testName, pageElements, scenarios, verification);

      // Generate Page Object
      const pageFile = path.join(pagesDir, `${className}.java`);

      // ── Generate Locator + Action methods following framework pattern ──────────
      // Pattern (same as Treecomponent.java / Login.java):
      //   public static Locator elementName()  { return page.getByRole(...); }
      //   public static void clickElementName(Page page) { clickOnElement(elementName()); ... }
      const seenLocators = new Set();
      const elementMethods = pageElements.map(el => {
        const locatorId = toCamelCaseId(el.name);    // "usernameField"
        const pascalId = toPascalCaseId(el.name);   // "UsernameField"
        const { prefix, callParam, sigParam } = elementActionParts(el);
        const actionMethodName = `${prefix}${pascalId}`;  // "enterUsernameField"
        const locatorExpr = generateSmartLocator(el);

        let block = '';

        // Locator method (deduplicated – some elements share the same locator identity)
        if (!seenLocators.has(locatorId)) {
          seenLocators.add(locatorId);
          block += `    /**\n     * Locator for ${el.name}\n     */\n`;
          block += `    public static Locator ${locatorId}() {\n`;
          block += `        return ${locatorExpr};\n`;
          block += `    }\n\n`;
        }

        // Action method
        block += `    /**\n     * ${el.description || el.name} - ${prefix} action\n     * Element: ${el.name}\n     * @param page Playwright Page instance\n     */\n`;
        if (sigParam) {
          block += `    public static void ${actionMethodName}(Page page, ${sigParam}) {\n`;
        } else {
          block += `    public static void ${actionMethodName}(Page page) {\n`;
        }

        if (enableLogging) {
          block += `        log.info("${prefix === 'enter' ? '⌨️' : '🖱️'} ${el.name}");\n`;
        } else {
          block += `        System.out.println("${prefix === 'enter' ? '⌨️' : '🖱️'} Step: ${prefix} ${el.name}");\n`;
        }

        if (enablePerformance) {
          block += `        long startTime = System.currentTimeMillis();\n`;
        }

        const locatorCall = `${locatorId}()`;
        if (prefix === 'click') {
          block += `        clickOnElement(${locatorCall});\n`;
        } else if (prefix === 'enter') {
          block += `        enterText(${locatorCall}, text);\n`;
        } else if (prefix === 'select') {
          block += `        selectDropDownValueByText(${locatorCall}, option);\n`;
        }

        if (enablePerformance) {
          block += `        long duration = System.currentTimeMillis() - startTime;\n`;
          block += `        log.info("⏱️ Action completed in " + duration + "ms");\n`;
        }

        block += `        TimeoutConfig.waitShort();\n`;

        if (enableLogging) {
          block += `        log.info("✅ ${el.name} completed");\n`;
        }

        block += `    }\n`;
        return block;
      }).join('\n');
      // ─────────────────────────────────────────────────────────────────────────

      let verificationMethods = '';
      if (enableAssertions) {
        verificationMethods += `\n    /**\n     * Verify page loaded via URL check\n     */\n`;
        verificationMethods += `    public static void verifyPageLoaded(String expectedUrlPart) {\n`;
        verificationMethods += `        log.info("🔍 Verifying page loaded");\n`;
        verificationMethods += `        Assert.assertTrue(isUrlContains(expectedUrlPart), "Page URL verification failed");\n`;
        verificationMethods += `        log.info("✓ Page verified");\n`;
        verificationMethods += `    }\n`;
      }

      // Include Logger only when it is actually referenced in the generated methods
      // (logging flag, performance timing log, or assertion verify method all use log.*)
      const useLogger = enableLogging || enablePerformance || enableAssertions;
      let imports = '';
      if (enableAssertions) imports += `\nimport org.testng.Assert;`;
      if (useLogger) imports += `\nimport java.util.logging.Logger;`;

      const loggerDecl = useLogger
        ? `\n    private static final Logger log = Logger.getLogger(${className}.class.getName());`
        : '';

      const pageContent = `package pages;\n\nimport com.microsoft.playwright.Locator;\nimport com.microsoft.playwright.Page;\nimport com.microsoft.playwright.options.AriaRole;\nimport configs.TimeoutConfig;${imports}\n\n/**\n * ${description || className + ' Page Object'}\n * Auto-generated by AI Automation CLI (Option 3)\n * Uses Playwright Locator pattern - extends BasePage\n */\npublic class ${className} extends BasePage {${loggerDecl}\n    private static final String PAGE_PATH = "";\n\n    /* --------------------\n       Locators for ${className}\n       -----------------------*/\n\n${elementMethods}${verificationMethods}\n}\n`;

      // VALIDATE AND FIX before writing to disk
      const validatedPageContent = validateAndFixPageObject(pageContent, className);

      await fs.mkdir(pagesDir, { recursive: true });
      await fs.writeFile(pageFile, validatedPageContent, 'utf-8');

      // Generate Feature File with enhanced scenarios based on verification criteria
      const featureFile = path.join(featuresDir, `${testName}.feature`);

      // Use the already generated enhancedScenarios from above
      const scenarioContent = enhancedScenarios.map((s, i) => {
        const steps = s.steps.map(step => `    ${step}`).join('\n');
        // Auto-tag scenarios based on content
        let tags = '@Functional';
        if (s.name.toLowerCase().includes('ui') || s.name.toLowerCase().includes('display') || s.name.toLowerCase().includes('visible')) {
          tags = '@UI';
        }
        if (s.name.toLowerCase().includes('ux') || s.name.toLowerCase().includes('experience') || s.name.toLowerCase().includes('flow')) {
          tags = '@UX';
        }
        if (s.name.toLowerCase().includes('performance') || s.name.toLowerCase().includes('load') || s.name.toLowerCase().includes('response')) {
          tags = '@Performance';
        }
        if (s.name.toLowerCase().includes('security') || s.name.toLowerCase().includes('password') || s.name.toLowerCase().includes('injection')) {
          tags = '@Security';
        }
        if (s.name.toLowerCase().includes('invalid') || s.name.toLowerCase().includes('empty') || s.name.toLowerCase().includes('error')) {
          tags += ' @Negative';
        }
        if (s.name.toLowerCase().includes('boundary') || s.name.toLowerCase().includes('minimum') || s.name.toLowerCase().includes('maximum')) {
          tags += ' @Boundary';
        }
        return `  ${tags} @Priority=${i}\n  Scenario: ${s.name}\n${steps}\n`;
      }).join('\n');

      const featureContent = `Feature: ${description || testName}\n  As a user\n  I want to test ${testName} functionality\n\n  Background:\n    Given the application is ready\n\n${scenarioContent}`;

      await fs.mkdir(featuresDir, { recursive: true });
      await fs.writeFile(featureFile, featureContent, 'utf-8');

      // Generate Step Definitions with comprehensive implementations
      const stepsFile = path.join(stepDefsDir, `${testName}Steps.java`);
      let stepsContent = generateComprehensiveStepDefinitions(className, testName, pageElements, verification);

      // AUTO-FIX: Clean up any placeholder step definitions first
      stepsContent = cleanupPlaceholderStepDefinitions(stepsContent);

      // AUTO-FIX: Validate feature steps match step definitions and generate missing ones
      stepsContent = validateAndFixStepMatching(featureContent, stepsContent, testName);

      // VALIDATION: Comprehensive step count validation
      const featureStepCount = extractStepsFromFeature(featureContent).size;
      const stepDefCount = extractStepsFromStepDefs(stepsContent).size;
      const generationRate = featureStepCount > 0 ? (stepDefCount / featureStepCount * 100).toFixed(1) : 0;

      console.log(colors.cyan + '\n' + '='.repeat(70) + colors.reset);
      console.log(colors.cyan + 'STEP GENERATION VALIDATION REPORT' + colors.reset);
      console.log(colors.cyan + '='.repeat(70) + colors.reset);
      console.log(`  Feature File Steps:           ${featureStepCount}`);
      console.log(`  Step Definitions Generated:   ${stepDefCount}`);
      console.log(`  Generation Rate:              ${generationRate}%`);

      if (stepDefCount < featureStepCount) {
        console.log(colors.yellow + `  ⚠️  WARNING: Not all steps generated (${featureStepCount - stepDefCount} missing)` + colors.reset);
      } else if (stepDefCount === featureStepCount) {
        console.log(colors.green + `  ✅ SUCCESS: All feature steps have definitions` + colors.reset);
      } else {
        console.log(colors.green + `  ✅ SUCCESS: Extra utility steps generated (${stepDefCount - featureStepCount} additional)` + colors.reset);
      }

      console.log(colors.cyan + '='.repeat(70) + '\n' + colors.reset);

      await fs.mkdir(stepDefsDir, { recursive: true });
      await fs.writeFile(stepsFile, stepsContent, 'utf-8');

      return `✅ Test suite generated successfully!\n\nGenerated files:\n  📄 ${pageFile}\n  📋 ${featureFile}\n  🧪 ${stepsFile}\n\nNext steps:\n  1. Review generated files\n  2. Add step definition implementations\n  3. Compile: mvn clean compile\n  4. Run: mvn test -DsuiteXmlFile=src/test/testng.xml`;

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
      } catch (e) { }

      try {
        const features = await fs.readdir(featuresDir);
        featureFiles = features.filter(f => f.endsWith('.feature')).map(f => ({
          name: f.replace('.feature', ''),
          scenarioCount: 0
        }));
      } catch (e) { }

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

      // Fix 1: Missing imports (Enhanced with comprehensive coverage)
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
            'base': 'import configs.base;',
            'Page': 'import com.microsoft.playwright.Page;',
            'loadProps': 'import configs.loadProps;',
            'browserSelector': 'import configs.browserSelector;'
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

        // Fix missing navigateTo method in page objects
        if (error.includes('method navigateTo') && file.includes('Page.java')) {
          const classMatch = fixedCode.match(/public class (\w+)/);
          if (classMatch && !fixedCode.includes('public static void navigateTo')) {
            const className = classMatch[1];
            // Ensure required imports
            if (!fixedCode.includes('import com.microsoft.playwright.Page')) {
              const packageLineEnd = fixedCode.indexOf(';') + 1;
              fixedCode = fixedCode.slice(0, packageLineEnd) + '\nimport com.microsoft.playwright.Page;' + fixedCode.slice(packageLineEnd);
            }
            if (!fixedCode.includes('import configs.loadProps')) {
              const lastImport = fixedCode.lastIndexOf('import ');
              const nextLine = fixedCode.indexOf('\n', lastImport) + 1;
              fixedCode = fixedCode.slice(0, nextLine) + 'import configs.loadProps;\n' + fixedCode.slice(nextLine);
            }
            // Add navigateTo method after class declaration
            const classEnd = fixedCode.indexOf('{', fixedCode.indexOf('public class')) + 1;
            const navigateToMethod = `\n    /**\n     * Navigate to ${className} page\n     * @param page Playwright Page instance\n     */\n    public static void navigateTo(Page page) {\n        log.info("🌐 Navigating to ${className} page");\n        String url = loadProps.getProperty("URL");\n        navigateToUrl(url);\n        log.info("✅ Navigation completed");\n    }\n`;
            fixedCode = fixedCode.slice(0, classEnd) + navigateToMethod + fixedCode.slice(classEnd);
          }
        }
      }

      // Fix 2: Method not found - use correct framework methods (Enhanced with all TimeoutConfig methods)
      if (error.includes('method') && error.includes('cannot find symbol')) {
        // Fix incorrect TimeoutConfig method names
        fixedCode = fixedCode.replace(/TimeoutConfig\.shortWait\(\)/g, 'TimeoutConfig.waitShort()');
        fixedCode = fixedCode.replace(/TimeoutConfig\.mediumWait\(\)/g, 'TimeoutConfig.waitMedium()');
        fixedCode = fixedCode.replace(/TimeoutConfig\.longWait\(\)/g, 'TimeoutConfig.waitLong()');

        // Fix direct Selenium calls to use framework methods
        fixedCode = fixedCode.replace(/(\w+)\.click\(\)/g, 'clickOnElement($1, TimeoutConfig.waitShort())');
        fixedCode = fixedCode.replace(/(\w+)\.sendKeys\(([^)]+)\)/g, 'enterText($1, $2, TimeoutConfig.waitShort())');
        fixedCode = fixedCode.replace(/(\w+)\.getText\(\)/g, '$1.getText()'); // Already correct

        // Fix common navigation method issues
        if (error.includes('navigateTo') && !fixedCode.includes('public static void navigateTo')) {
          const classMatch = fixedCode.match(/public class (\w+)/);
          if (classMatch) {
            const className = classMatch[1];
            const classEnd = fixedCode.indexOf('{', fixedCode.indexOf('public class')) + 1;
            const navigateToMethod = `\n    public static void navigateTo(Page page) {\n        String url = loadProps.getProperty("URL");\n        navigateToUrl(url);\n    }\n`;
            fixedCode = fixedCode.slice(0, classEnd) + navigateToMethod + fixedCode.slice(classEnd);
          }
        }

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
            '$1 extends browserSelector'
          );
        }
      }

      // Fix 6: Syntax errors - missing semicolons, brackets, invalid names
      if (error.includes('expected') || error.includes('illegal') || error.includes('invalid')) {
        // Fix missing semicolons
        const lines = fixedCode.split('\n');
        for (let i = 0; i < lines.length; i++) {
          const line = lines[i].trim();
          // Add semicolon to common statement types if missing
          if (line.length > 0 &&
            !line.endsWith(';') &&
            !line.endsWith('{') &&
            !line.endsWith('}') &&
            !line.startsWith('//') &&
            !line.startsWith('/*') &&
            !line.startsWith('*') &&
            !line.startsWith('@') &&
            (line.includes('return ') || line.includes('= ') || line.match(/^\w+\.\w+\(/))) {
            lines[i] = lines[i] + ';';
          }
        }
        fixedCode = lines.join('\n');

        // Fix invalid class names (ensure PascalCase)
        const invalidClassName = fixedCode.match(/public class ([a-z]\w+)/);  // starts with lowercase
        if (invalidClassName) {
          const oldName = invalidClassName[1];
          const newName = oldName.charAt(0).toUpperCase() + oldName.slice(1);
          fixedCode = fixedCode.replace(new RegExp('\\b' + oldName + '\\b', 'g'), newName);
        }
      }

      // Fix 7: Invalid method names (ensure camelCase)
      if (error.includes('method') && (error.includes('illegal') || error.includes('invalid'))) {
        const invalidMethod = fixedCode.match(/public (?:static )?void ([A-Z]\w+)\(/);  // starts with uppercase
        if (invalidMethod) {
          const oldName = invalidMethod[1];
          const newName = oldName.charAt(0).toLowerCase() + oldName.slice(1);
          fixedCode = fixedCode.replace(new RegExp('\\b' + oldName + '\\(', 'g'), newName + '(');
        }
      }

      // Fix 8: Step definition class name mismatch
      if (file.includes('Steps.java')) {
        const fileNameMatch = file.match(/([\w]+)Steps\.java/);
        const classNameMatch = fixedCode.match(/public class (\w+)/);
        if (fileNameMatch && classNameMatch && fileNameMatch[1] !== classNameMatch[1].replace('Steps', '')) {
          const expectedClassName = fileNameMatch[1] + 'Steps';
          fixedCode = fixedCode.replace(
            /public class \w+/,
            'public class ' + expectedClassName
          );
        }
      }

      // Fix 9: Missing or incorrect extends in Steps
      if (file.includes('Steps.java') && !fixedCode.includes('extends')) {
        if (file.includes('Steps.java') && !fixedCode.includes('extends browserSelector')) {
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
        fixedCode = fixedCode.replace(/TimeoutConfig\.shortWait\(\)/g, 'TimeoutConfig.waitShort()');
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
async function autoCompileTestAndFix(testName, elements, scenarios, verification, performanceThreshold, jiraKey = null) {
  const maxAttempts = 5;
  let attempt = 1;
  let success = false;
  let executionStartTime = Date.now();

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

    // Calculate execution time
    const executionTime = Date.now() - executionStartTime;

    // Complete JIRA story if jiraKey provided
    if (jiraKey) {
      console.log(colors.cyan + `\n🎫 Completing JIRA story ${jiraKey}...\n` + colors.reset);

      try {
        const completeResult = await runJavaCommand(
          'configs.jira.jiraClient',
          ['completeStory', jiraKey, testName, executionTime.toString()]
        );

        if (completeResult.success) {
          console.log(colors.green + `🎉 JIRA story ${jiraKey} marked as complete!\n` + colors.reset);
        } else {
          console.log(colors.yellow + `⚠️ Could not auto-complete JIRA story ${jiraKey}\n` + colors.reset);
          console.log(colors.yellow + '   (Story may need manual transition in JIRA)\n' + colors.reset);
        }
      } catch (error) {
        console.log(colors.red + `❌ Error completing JIRA story: ${error.message}\n` + colors.reset);
      }
    }

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
 * Run Java command via Maven exec:java
 * @param {string} className - Fully qualified Java class name (e.g., 'configs.jira.jiraClient')
 * @param {Array<string>} args - Command line arguments
 * @returns {Promise<{success: boolean, output: string}>}
 */
async function runJavaCommand(className, args = []) {
  return new Promise((resolve) => {
    const isWindows = process.platform === 'win32';
    const mvnCmd = isWindows ? 'mvn.cmd' : 'mvn';

    const argsString = args.join(' ');
    const execArgs = [
      'exec:java',
      `-Dexec.mainClass=${className}`,
      `-Dexec.args="${argsString}"`
    ];

    const mvn = spawn(mvnCmd, execArgs, {
      cwd: process.cwd(),
      shell: true
    });

    let output = '';
    let errorOutput = '';

    mvn.stdout.on('data', (data) => {
      const text = data.toString();
      output += text;
      // Only show important output (not Maven build logs)
      if (text.includes('✅') || text.includes('❌') || text.includes('🎉') || text.includes('⚠️')) {
        process.stdout.write(colors.reset + text);
      }
    });

    mvn.stderr.on('data', (data) => {
      const text = data.toString();
      errorOutput += text;
    });

    mvn.on('close', (code) => {
      const success = (code === 0 || output.includes('✅'));
      resolve({
        success,
        output,
        error: errorOutput
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

// ============================================================================
// MENU FUNCTIONS
// ============================================================================

/**
 * Retry from Existing Recording (Option 1B)
 * Regenerate files from a previously saved recording
 */
async function retryFromRecording() {
  console.log(colors.green + '\n🔄 Retry from Existing Recording\n' + colors.reset);
  console.log('This will use a previously saved recording to regenerate test files.\n');

  const fs = require('fs');

  // Check if Recorded directory exists
  if (!fs.existsSync('Recorded')) {
    console.log(colors.red + '❌ No recordings found! Recorded/ directory does not exist.\n' + colors.reset);
    console.log(colors.yellow + '💡 Use Option 1 (Record) to create a new recording first.\n' + colors.reset);
    return;
  }

  // Declare variables outside try block for proper scope
  let recordings = [];
  let selection = '';

  // List available recordings
  try {
    recordings = fs.readdirSync('Recorded')
      .filter(dir => {
        const fullPath = path.join('Recorded', dir);
        return fs.statSync(fullPath).isDirectory();
      })
      .map((dir, index) => {
        const recordingFile = path.join('Recorded', dir, 'recorded-actions.java');
        const exists = fs.existsSync(recordingFile);
        const stats = exists ? fs.statSync(recordingFile) : null;
        return {
          index: index + 1,
          dirname: dir,
          path: recordingFile,
          exists: exists,
          size: stats ? stats.size : 0,
          modified: stats ? stats.mtime.toLocaleString() : 'N/A'
        };
      })
      .filter(rec => rec.exists); // Only show recordings with valid files

    if (recordings.length === 0) {
      console.log(colors.red + '❌ No valid recordings found!\n' + colors.reset);
      console.log(colors.yellow + '💡 Use Option 1 (Record) to create a new recording.\n' + colors.reset);
      return;
    }

    console.log(colors.cyan + '📂 Available Recordings:\n' + colors.reset);
    recordings.forEach(rec => {
      console.log(colors.green + `  ${rec.index}. ` + colors.reset + rec.dirname);
      console.log(`      📄 File: ${rec.path}`);
      console.log(`      📏 Size: ${rec.size} bytes`);
      console.log(`      🕐 Modified: ${rec.modified}\n`);
    });

    selection = await question(colors.cyan + `👉 Select recording (1-${recordings.length}): ` + colors.reset);
    const selectedIndex = parseInt(selection) - 1;

    if (isNaN(selectedIndex) || selectedIndex < 0 || selectedIndex >= recordings.length) {
      console.log(colors.red + '\n❌ Invalid selection!\n' + colors.reset);
      return;
    }

    const selectedRecording = recordings[selectedIndex];
    console.log(colors.green + `\n✓ Selected: ${selectedRecording.dirname}\n` + colors.reset);

  } catch (error) {
    console.log(colors.red + '❌ Error reading recordings: ' + error.message + '\n' + colors.reset);
    return;
  }

  // Load default URL from configurations.properties
  const fsPromises = require('fs').promises;
  let configUrl = '';

  try {
    const configPath = path.join(process.cwd(), 'src/test/resources/configurations.properties');
    console.log(colors.dim + `[DEBUG] Reading config from: ${configPath}` + colors.reset);
    const configContent = await fsPromises.readFile(configPath, 'utf-8');

    const config = {};
    configContent.split('\n').forEach(line => {
      const trimmedLine = line.trim().replace(/\r$/, '');
      const [key, ...valueParts] = trimmedLine.split('=');
      if (key && valueParts.length > 0) {
        config[key.trim()] = valueParts.join('=').trim();
      }
    });

    configUrl = config.URL || '';
    console.log(colors.dim + `[DEBUG] Raw URL from config: "${configUrl}"` + colors.reset);
    if (configUrl) {
      configUrl = configUrl.replace(/\\:/g, ':');
      console.log(colors.cyan + `\n✓ Loaded default URL from config: ${configUrl}\n` + colors.reset);
    } else {
      console.log(colors.dim + '[DEBUG] No URL found in config' + colors.reset);
    }
  } catch (error) {
    console.log(colors.yellow + '⚠️  Could not load URL from configurations.properties: ' + error.message + colors.reset);
  }

  // Get user inputs for generation
  const featureName = await question(colors.cyan + '📝 Feature Name (e.g., Login): ' + colors.reset);

  let pageUrl = '';
  if (configUrl) {
    const urlInput = await question(colors.cyan + `🌐 Page URL (press Enter for default: ${configUrl}): ` + colors.reset);
    pageUrl = urlInput.trim() || configUrl;
  } else {
    pageUrl = await question(colors.cyan + '🌐 Page URL (e.g., https://example.com/login): ' + colors.reset);
  }

  const jiraStory = await question(colors.cyan + '🎫 JIRA Story ID (optional): ' + colors.reset) || 'AUTO-GEN';
  const mergeMode = await question(colors.cyan + '🔄 Enable merge mode (preserve existing code)? (y/n): ' + colors.reset);

  if (!featureName.trim()) {
    console.log(colors.red + '\n❌ Feature name is required!\n' + colors.reset);
    return;
  }

  if (!pageUrl.trim()) {
    console.log(colors.red + '\n❌ Page URL is required!' + colors.reset);
    if (!configUrl) {
      console.log(colors.yellow + '💡 Tip: Set URL in src/test/resources/configurations.properties to use as default.\n' + colors.reset);
    }
    return;
  }

  const recordingFile = recordings[parseInt(selection) - 1].path;

  console.log(colors.yellow + '\n🔄 Generating test files from recording...\n' + colors.reset);
  console.log(colors.cyan + '  📄 Recording: ' + recordingFile + colors.reset);
  console.log(colors.cyan + '  📝 Feature: ' + featureName + colors.reset);
  console.log(colors.cyan + '  🌐 URL: ' + pageUrl + colors.reset);
  console.log(colors.cyan + '  🎫 JIRA: ' + jiraStory + colors.reset);
  console.log(colors.cyan + '  🔄 Merge Mode: ' + (mergeMode.toLowerCase() === 'y' ? 'Yes' : 'No') + colors.reset);
  console.log('');

  // First, compile the project to ensure TestGeneratorHelper and dependencies are compiled
  console.log(colors.cyan + '🔨 Compiling framework classes...\n' + colors.reset);

  return new Promise((resolve) => {
    const compileFramework = spawn('mvn', ['compile', '-q'], {
      cwd: process.cwd(),
      shell: true,
      stdio: 'inherit'
    });

    compileFramework.on('close', (compileCode) => {
      if (compileCode !== 0) {
        console.log(colors.red + '\n❌ Framework compilation failed!' + colors.reset);
        console.log(colors.yellow + '💡 Please fix compilation errors and try again.\n' + colors.reset);
        resolve();
        return;
      }

      console.log(colors.green + '✅ Framework compiled successfully\n' + colors.reset);

      // Call Java TestGeneratorHelper
      // Use temp batch file to avoid Windows CMD quoting issues
      const escapedRecordingFile = recordingFile.replace(/\\/g, '\\\\');
      const escapedPageUrl = pageUrl.replace(/\\/g, '\\\\');

      // For -Dexec.args, use space-separated values
      const execArgsValue = `${escapedRecordingFile} ${featureName} ${escapedPageUrl} ${jiraStory}`;

      console.log(colors.dim + `[DEBUG] URL being passed: ${escapedPageUrl}` + colors.reset);

      // Create temporary batch file with the Maven command
      const tempBatchFile = path.join(process.cwd(), 'temp_generate.bat');
      const batchContent = `@echo off\nmvn exec:java -e -Dexec.mainClass=configs.TestGeneratorHelper "-Dexec.args=${execArgsValue}"`;

      fs.writeFileSync(tempBatchFile, batchContent);
      console.log(colors.dim + `[DEBUG] Created temp batch: ${tempBatchFile}` + colors.reset);

      // Execute the batch file
      const generate = spawn(tempBatchFile, [], {
        cwd: process.cwd(),
        stdio: 'inherit',
        shell: true
      });

      generate.on('close', (code) => {
        // Clean up batch file
        try {
          fs.unlinkSync(tempBatchFile);
        } catch (err) {
          // Ignore cleanup errors
        }

        // Continue with original logic
        if (code === 0) {
          console.log(colors.green + '\n\n✅ Test files generated successfully!' + colors.reset);
          console.log(colors.cyan + '\n📋 Generated Files:' + colors.reset);
          console.log(`   ✓ src/main/java/pages/${featureName}.java`);
          console.log(`   ✓ src/test/java/features/${featureName.toLowerCase()}.feature`);
          console.log(`   ✓ src/test/java/stepDefs/${featureName}Steps.java`);

          console.log(colors.yellow + '\n🔨 Compiling generated files...\n' + colors.reset);

          // Compile to verify
          const compileProcess = spawn('mvn', ['clean', 'compile', '-DskipTests'], {
            cwd: process.cwd(),
            shell: true,
            stdio: 'inherit'
          });

          compileProcess.on('close', (compileCode) => {
            if (compileCode === 0) {
              console.log(colors.green + '\n\n✅ Compilation successful! Your test is ready to use.' + colors.reset);
              console.log(colors.cyan + '\n💡 Next Steps:' + colors.reset);
              console.log('   1. Review generated files for accuracy');
              console.log('   2. Run with: npm run tag -- --tags @' + featureName.toLowerCase());
              console.log('   3. Or run all tests with: npm run run\n');

              // Clean up recording directory after successful generation
              try {
                const recordingDirPath = path.dirname(recordingFile);
                const rimraf = require('fs').rmSync || require('fs').rmdirSync;
                rimraf(recordingDirPath, { recursive: true, force: true });
                console.log(colors.dim + `✓ Cleaned up recording directory: ${recordingDirPath}` + colors.reset);
              } catch (cleanupErr) {
                console.log(colors.dim + `⚠️  Could not clean up recording directory: ${cleanupErr.message}` + colors.reset);
              }
            } else {
              console.log(colors.yellow + '\n⚠️  Compilation had issues. Please review the errors above.' + colors.reset);
              console.log(colors.cyan + '💡 You can fix errors with: npm run validate\n' + colors.reset);
            }
            resolve();
          });
        } else {
          console.log(colors.red + '\n❌ Generation failed! Check the error messages above.\n' + colors.reset);
          console.log(colors.yellow + '💡 Troubleshooting:' + colors.reset);
          console.log('   - Ensure recording file has valid Playwright Java code');
          console.log('   - Check if feature name is valid (alphanumeric only)');
          console.log('   - Try regenerating the recording with Option 1\n');
          resolve();
        }
      });
    }); // Close compileFramework.on('close')
  });
}

/**
 * Validate & Run Tests (Option 4)
 * Compile, validate, and execute test suite
 */
async function validateAndRunTests() {
  console.log(colors.green + '\n✅ Validate & Run Tests\n' + colors.reset);
  console.log('This will compile your project and run the test suite.\n');

  console.log(colors.yellow + '📦 Step 1: Compiling project...\n' + colors.reset);

  return new Promise((resolve) => {
    const compile = spawn('mvn', ['clean', 'compile'], {
      cwd: process.cwd(),
      shell: true,
      stdio: 'inherit'
    });

    compile.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ Compilation successful!\n' + colors.reset);
        console.log(colors.yellow + '🧪 Step 2: Running tests...\n' + colors.reset);

        const test = spawn('mvn', ['test'], {
          cwd: process.cwd(),
          shell: true,
          stdio: 'inherit'
        });

        test.on('close', (testCode) => {
          if (testCode === 0) {
            console.log(colors.green + '\n✅ All tests passed!\n' + colors.reset);
            console.log(colors.cyan + '📊 View reports in: MRITestExecutionReports/\n' + colors.reset);
          } else {
            console.log(colors.yellow + '\n⚠️  Some tests failed. Check reports for details.\n' + colors.reset);
          }
          resolve();
        });
      } else {
        console.log(colors.red + '\n❌ Compilation failed! Fix errors and try again.\n' + colors.reset);
        resolve();
      }
    });
  });
}

/**
 * Complete Project Setup (Option S)
 * Install MCP server and configure project
 */
async function completeProjectSetup() {
  console.log(colors.green + '\n⚙️  Complete Project Setup\n' + colors.reset);
  console.log('This will install MCP Server dependencies and configure your project.\n');

  console.log(colors.yellow + '📦 Installing MCP Server dependencies...\n' + colors.reset);

  return new Promise((resolve) => {
    const setup = spawn('npm', ['install'], {
      cwd: path.join(process.cwd(), 'mcp-server'),
      shell: true,
      stdio: 'inherit'
    });

    setup.on('close', async (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ MCP Server installed successfully!\n' + colors.reset);

        console.log(colors.cyan + '\n📋 Next Steps:' + colors.reset);
        console.log('   1. Update src/test/resources/configurations.properties');
        console.log('   2. Configure JIRA credentials (optional)');
        console.log('   3. Start using Option 1, 2, or 3 to generate tests\n');

        await ensureMCPServer();
      } else {
        console.log(colors.red + '\n❌ Setup failed! Check the errors above.\n' + colors.reset);
      }
      resolve();
    });
  });
}

/**
 * Run Tagged Tests (Option 5)
 * Run specific tests using Cucumber tags
 */
async function runSpecificTagTests() {
  console.log(colors.green + '\n🏷️  Run Specific Tag Tests\n' + colors.reset);

  const tag = await question(colors.cyan + '📝 Enter Cucumber tag (e.g., @Login, @Smoke): ' + colors.reset);

  if (!tag.trim()) {
    console.log(colors.red + '\n❌ Tag is required!\n' + colors.reset);
    return;
  }

  console.log(colors.yellow + `\n🧪 Running tests with tag: ${tag}\n` + colors.reset);

  return new Promise((resolve) => {
    const test = spawn('mvn', ['test', `-Dcucumber.filter.tags="${tag}"`], {
      cwd: process.cwd(),
      shell: true,
      stdio: 'inherit'
    });

    test.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + `\n✅ Tests with tag ${tag} passed!\n` + colors.reset);
      } else {
        console.log(colors.yellow + `\n⚠️  Some tests with tag ${tag} failed.\n` + colors.reset);
      }
      resolve();
    });
  });
}

/**
 * Quick Java Validation (Option 6)
 * Validate Java code and auto-fix common errors
 */
async function quickJavaValidation() {
  console.log(colors.green + '\n🔍 Quick Java Code Validation\n' + colors.reset);
  console.log('This tool checks for common Java coding errors and can auto-fix them.\n');

  console.log(colors.cyan + 'Choose an option:' + colors.reset);
  console.log('  1. Validate Only (check for errors)');
  console.log('  2. Validate & Auto-Fix (recommended)');
  console.log('  3. Back to Main Menu\n');

  const choice = await question(colors.cyan + 'Enter choice (1-3): ' + colors.reset);

  if (choice === '3') {
    return;
  }

  if (choice !== '1' && choice !== '2') {
    console.log(colors.red + '\n❌ Invalid choice!\n' + colors.reset);
    return;
  }

  const mode = choice === '1' ? 'check' : 'fix';
  console.log(colors.yellow + `\n📋 Running validation in ${mode} mode...\n` + colors.reset);

  // Node.js validation logic (replaces PowerShell)
  const path = require('path');
  const glob = require('fs').promises;

  let errorCount = 0;
  let warningCount = 0;
  let fixCount = 0;

  console.log(colors.cyan + '[SCAN] Checking Java files...\n' + colors.reset);

  async function scanDirectory(dir) {
    try {
      const entries = await glob.readdir(dir, { withFileTypes: true });
      for (const entry of entries) {
        const fullPath = path.join(dir, entry.name);
        if (entry.isDirectory()) {
          await scanDirectory(fullPath);
        } else if (entry.name.endsWith('.java')) {
          await checkFile(fullPath, entry.name);
        }
      }
    } catch (err) {
      // Ignore errors
    }
  }

  async function checkFile(filePath, fileName) {
    try {
      let content = await glob.readFile(filePath, 'utf8');
      let hasIssue = false;
      let fileIssues = [];
      let modified = false;

      // Check for common errors
      if (/public\s+static\s+void\s+methodName\s*\(/.test(content)) {
        errorCount++;
        fileIssues.push("  [ERROR] 'methodName' should be 'main'");
        hasIssue = true;
        if (mode === 'fix') {
          content = content.replace(/(public\s+static\s+void\s+)methodName(\s*\()/g, '$1main$2');
          modified = true;
          fixCount++;
        }
      }

      if (/System\.out\.printline/.test(content)) {
        errorCount++;
        fileIssues.push("  [ERROR] 'printline' should be 'println'");
        hasIssue = true;
        if (mode === 'fix') {
          content = content.replace(/System\.out\.printline/g, 'System.out.println');
          modified = true;
          fixCount++;
        }
      }

      if (/login\.(enter|click)/.test(content)) {
        errorCount++;
        fileIssues.push("  [ERROR] 'login.' should be 'Login.' (capital L)");
        hasIssue = true;
        if (mode === 'fix') {
          content = content.replace(/login\./g, 'Login.');
          modified = true;
          fixCount++;
        }
      }

      if (hasIssue) {
        console.log(colors.yellow + `  File: ${fileName}` + colors.reset);
        fileIssues.forEach(issue => {
          console.log(mode === 'fix' ? colors.green + issue + colors.reset : colors.red + issue + colors.reset);
        });

        if (modified) {
          await glob.writeFile(filePath, content, 'utf8');
        }
      }
    } catch (err) {
      // Ignore file read errors
    }
  }

  // Scan src directory
  await scanDirectory('src');

  console.log('\n' + colors.cyan + '[SUMMARY]' + colors.reset);
  console.log((errorCount === 0 ? colors.green : colors.red) + `  Errors Found: ${errorCount}` + colors.reset);
  console.log(colors.yellow + `  Warnings: ${warningCount}` + colors.reset);
  if (mode === 'fix') {
    console.log(colors.green + `  Fixes Applied: ${fixCount}` + colors.reset);
  }

  if (errorCount === 0) {
    console.log('\n' + colors.green + '[OK] No issues found! Code looks good.' + colors.reset);
  } else if (mode === 'check') {
    console.log('\n' + colors.cyan + '[TIP] Run auto-fix mode (option 2) to fix these issues' + colors.reset);
  }

  if (mode === 'fix') {
    console.log(colors.green + '\n✅ Validation complete! Run Option 4 to verify changes.\n' + colors.reset);
  }

  console.log(''); // Add spacing
}

/**
 * Run Tests in Parallel (Option 5)
 */
async function runParallelTests() {
  console.log(colors.green + '\n⚡ Parallel Test Execution\n' + colors.reset);
  console.log('This will run all tests in parallel for faster execution.\n');

  // Detect CPU cores
  const os = require('os');
  const cpuCores = os.cpus().length;
  const maxThreads = cpuCores * 2;

  console.log(colors.cyan + `💻 Detected ${cpuCores} CPU cores` + colors.reset);
  console.log(colors.cyan + `📊 Recommended thread count: ${cpuCores} to ${maxThreads}\n` + colors.reset);

  const threadInput = await question(colors.cyan + `Enter thread count (1-${maxThreads}, default ${cpuCores}): ` + colors.reset);
  const threadCount = threadInput.trim() === '' ? cpuCores : parseInt(threadInput);

  if (isNaN(threadCount) || threadCount < 1 || threadCount > maxThreads) {
    console.log(colors.red + `\n❌ Invalid thread count! Must be between 1 and ${maxThreads}\n` + colors.reset);
    return;
  }

  console.log(colors.yellow + `\n🚀 Running tests in parallel with ${threadCount} threads...\n` + colors.reset);

  return new Promise((resolve) => {
    const mvnProcess = spawn('mvn', [
      'clean',
      'test',
      `-Dparallel=methods`,
      `-DthreadCount=${threadCount}`,
      `-DdataproviderthreadCount=${threadCount}`
    ], {
      cwd: process.cwd(),
      shell: true,
      stdio: 'inherit'
    });

    mvnProcess.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ Parallel test execution completed successfully!' + colors.reset);
        console.log(colors.cyan + '\n📊 Check reports in: target/surefire-reports/' + colors.reset);
        console.log(colors.cyan + '📊 HTML Report: target/cucumber-reports/cucumber.html\n' + colors.reset);
      } else {
        console.log(colors.red + `\n❌ Tests failed with exit code ${code}\n` + colors.reset);
        console.log(colors.yellow + '💡 Tip: Check test-health-logs/ for detailed error logs\n' + colors.reset);
      }
      resolve();
    });
  });
}

/**
 * Generate and View Test Reports (Option R)
 */
async function generateAndViewReports() {
  console.log(colors.green + '\n📊 Test Reports Generator\n' + colors.reset);
  console.log('Select report type to generate:\n');
  console.log('  1. HTML Report (Cucumber)');
  console.log('  2. JSON Report (for CI/CD)');
  console.log('  3. Allure Report (Interactive Dashboard)');
  console.log('  4. ExtentReports (Detailed HTML)');
  console.log('  5. View All Existing Reports');
  console.log('  6. Back to Main Menu\n');

  const choice = await question(colors.cyan + 'Enter choice (1-6): ' + colors.reset);

  if (choice === '6') {
    return;
  }

  // Check if test results exist
  const fs = require('fs');
  const resultsExist = fs.existsSync('target/surefire-reports') || fs.existsSync('target/cucumber-reports');

  if (!resultsExist && choice !== '5') {
    console.log(colors.yellow + '\n⚠️  No test results found. Please run tests first (Option 6).\n' + colors.reset);
    return;
  }

  switch (choice) {
    case '1':
      console.log(colors.yellow + '\n📄 Generating HTML Report...\n' + colors.reset);
      await generateHTMLReport();
      break;
    case '2':
      console.log(colors.yellow + '\n📄 Generating JSON Report...\n' + colors.reset);
      await generateJSONReport();
      break;
    case '3':
      console.log(colors.yellow + '\n📄 Generating Allure Report...\n' + colors.reset);
      await generateAllureReport();
      break;
    case '4':
      console.log(colors.yellow + '\n📄 Generating ExtentReport...\n' + colors.reset);
      await generateExtentReport();
      break;
    case '5':
      await viewAllReports();
      break;
    default:
      console.log(colors.red + '\n❌ Invalid choice!\n' + colors.reset);
  }
}

async function generateHTMLReport() {
  const fs = require('fs');
  const reportPath = path.join(process.cwd(), 'target', 'cucumber-reports', 'cucumber.html');
  if (fs.existsSync(reportPath)) {
    console.log(colors.green + '\u2705 HTML Report found at: ' + reportPath + '\n' + colors.reset);
    const open = await question(colors.cyan + 'Open in browser? (Y/n): ' + colors.reset);
    if (open.toLowerCase() !== 'n') {
      openInBrowser(reportPath);
    }
  } else {
    console.log(colors.red + '\u274c HTML report not found. Run tests first.\n' + colors.reset);
  }
}

async function generateJSONReport() {
  const reportPath = path.join(process.cwd(), 'target', 'json-report', 'cucumber.json');
  if (require('fs').existsSync(reportPath)) {
    console.log(colors.green + '✅ JSON Report found at: ' + reportPath + '\n' + colors.reset);
    console.log(colors.cyan + '📋 This report can be used for CI/CD integration\n' + colors.reset);
  } else {
    console.log(colors.red + '❌ JSON report not found. Run tests first.\n' + colors.reset);
  }
}

async function generateAllureReport() {
  console.log(colors.yellow + '🚀 Launching Allure server...\n' + colors.reset);
  console.log(colors.cyan + '📊 Allure will open in your browser automatically\n' + colors.reset);

  return new Promise((resolve) => {
    const allureProcess = spawn('mvn', ['allure:serve'], {
      cwd: process.cwd(),
      shell: true,
      stdio: 'inherit'
    });

    allureProcess.on('close', (code) => {
      if (code === 0) {
        console.log(colors.green + '\n✅ Allure report generated!\n' + colors.reset);
      } else {
        console.log(colors.red + '\n❌ Failed to generate Allure report\n' + colors.reset);
        console.log(colors.yellow + '💡 Tip: Ensure Allure is configured in pom.xml\n' + colors.reset);
      }
      resolve();
    });
  });
}

async function generateExtentReport() {
  const fs = require('fs');
  // ExtentReports are written to MRITestExecutionReports/<Version>/extentReports/
  const mriBase = path.join(process.cwd(), 'MRITestExecutionReports');
  let latestReport = null;

  if (fs.existsSync(mriBase)) {
    const walk = (dir) => {
      let results = [];
      if (!fs.existsSync(dir)) return results;
      fs.readdirSync(dir).forEach(f => {
        const full = path.join(dir, f);
        const stat = fs.statSync(full);
        if (stat.isDirectory()) results = results.concat(walk(full));
        else if (f.endsWith('.html')) results.push({ file: full, mtime: stat.mtimeMs });
      });
      return results;
    };
    const all = walk(mriBase).sort((a, b) => b.mtime - a.mtime);
    if (all.length > 0) latestReport = all[0].file;
  }

  // Fallback: legacy target path
  if (!latestReport) {
    const fallback = path.join(process.cwd(), 'target', 'extent-reports', 'index.html');
    if (fs.existsSync(fallback)) latestReport = fallback;
  }

  if (latestReport) {
    console.log(colors.green + '\u2705 ExtentReport found:\n  ' + latestReport + '\n' + colors.reset);
    const open = await question(colors.cyan + 'Open in browser? (Y/n): ' + colors.reset);
    if (open.toLowerCase() !== 'n') {
      openInBrowser(latestReport);
    }
  } else {
    console.log(colors.red + '\u274c No ExtentReport found in MRITestExecutionReports/. Run tests first.\n' + colors.reset);
  }
}

async function viewAllReports() {
  const fs = require('fs');
  console.log(colors.cyan + '\n\u{1F4C2} Available Report Locations:\n' + colors.reset);

  const reportLocations = [
    { name: 'Cucumber HTML',      path: 'target/cucumber-reports/cucumber.html' },
    { name: 'Cucumber JSON',      path: 'target/json-report/cucumber.json' },
    { name: 'Surefire Reports',   path: 'target/surefire-reports/' },
    { name: 'MRI Extent Reports', path: 'MRITestExecutionReports/' },
    { name: 'Recorded Videos',    path: 'Recorded/' },
    { name: 'Test Health Logs',   path: 'test-health-logs/' }
  ];

  reportLocations.forEach(location => {
    const exists = fs.existsSync(location.path);
    const status = exists ? colors.green + '\u2713' : colors.red + '\u2717';
    console.log('  ' + status + ' ' + location.name + ': ' + location.path + colors.reset);
  });
  console.log('');

  // Find latest HTML report files in MRITestExecutionReports/
  const mriBase = path.join(process.cwd(), 'MRITestExecutionReports');
  if (fs.existsSync(mriBase)) {
    const walk = (dir) => {
      let results = [];
      if (!fs.existsSync(dir)) return results;
      fs.readdirSync(dir).forEach(f => {
        const full = path.join(dir, f);
        const stat = fs.statSync(full);
        if (stat.isDirectory()) results = results.concat(walk(full));
        else if (f.endsWith('.html')) results.push({ file: full, mtime: stat.mtimeMs });
      });
      return results;
    };
    const reports = walk(mriBase).sort((a, b) => b.mtime - a.mtime).slice(0, 5);
    if (reports.length > 0) {
      console.log(colors.cyan + '\u{1F4CA} Recent Extent Reports (newest first):' + colors.reset);
      reports.forEach((r, i) => {
        const rel = path.relative(process.cwd(), r.file);
        console.log('  ' + (i + 1) + '. ' + rel);
      });
      console.log('');
      const pick = await question(colors.cyan + 'Open a report? Enter number or press Enter to skip: ' + colors.reset);
      const idx = parseInt(pick, 10);
      if (!isNaN(idx) && idx >= 1 && idx <= reports.length) {
        openInBrowser(reports[idx - 1].file);
      }
    }
  }
}


/**
 * Cross-platform open-in-browser helper.
 * On Windows uses `start "" "path"` -- the empty first arg is the window title,
 * preventing cmd from treating the file path as the title and silently doing nothing.
 */
function openInBrowser(filePath) {
  const { exec } = require('child_process');
  let cmd;
  if (process.platform === 'win32') {
    const winPath = filePath.replace(/\//g, '\\');
    cmd = 'start "" "' + winPath + '"';
  } else if (process.platform === 'darwin') {
    cmd = 'open "' + filePath + '"';
  } else {
    cmd = 'xdg-open "' + filePath + '"';
  }
  exec(cmd, { shell: true }, (err) => {
    if (err) {
      console.log(colors.yellow + '\u26a0\ufe0f  Could not open browser automatically.\n   Open manually: ' + filePath + colors.reset);
    } else {
      console.log(colors.green + '\u2705 Report opened in browser.\n' + colors.reset);
    }
  });
}
/**
 * Show Test Metrics Dashboard (Option M)
 */
async function showTestMetrics() {
  console.log(colors.green + '\n📈 Test Metrics Dashboard\n' + colors.reset);

  const fs = require('fs');
  const path = require('path');

  // Parse TestNG results
  const testngResultsPath = path.join(process.cwd(), 'target', 'surefire-reports', 'testng-results.xml');

  if (!fs.existsSync(testngResultsPath)) {
    console.log(colors.yellow + '⚠️  No test results found. Please run tests first (Option 6).\n' + colors.reset);
    return;
  }

  try {
    const xml = await fs.promises.readFile(testngResultsPath, 'utf8');

    // Simple XML parsing (basic approach)
    const totalTests = (xml.match(/<test-method/g) || []).length;
    const passedTests = (xml.match(/status="PASS"/g) || []).length;
    const failedTests = (xml.match(/status="FAIL"/g) || []).length;
    const skippedTests = (xml.match(/status="SKIP"/g) || []).length;

    const passRate = totalTests > 0 ? ((passedTests / totalTests) * 100).toFixed(2) : 0;

    console.log(colors.cyan + '╔════════════════════════════════════════╗' + colors.reset);
    console.log(colors.cyan + '║' + colors.bright + '     TEST EXECUTION METRICS            ' + colors.reset + colors.cyan + '║' + colors.reset);
    console.log(colors.cyan + '╚════════════════════════════════════════╝' + colors.reset);
    console.log('');

    console.log(colors.bright + '  Total Tests:     ' + colors.reset + totalTests);
    console.log(colors.green + '  ✓ Passed:        ' + colors.reset + passedTests);
    console.log(colors.red + '  ✗ Failed:        ' + colors.reset + failedTests);
    console.log(colors.yellow + '  ⊘ Skipped:       ' + colors.reset + skippedTests);
    console.log(colors.cyan + '  Pass Rate:       ' + colors.reset + passRate + '%');
    console.log('');

    // Save current metrics and show historical trends
    const metricsDir = path.join(process.cwd(), 'test-health-logs');
    if (!fs.existsSync(metricsDir)) {
      fs.mkdirSync(metricsDir, { recursive: true });
    }

    const metricsLogPath = path.join(metricsDir, 'metrics.json');
    let allMetrics = [];

    // Show historical trend if metrics log exists
    if (fs.existsSync(metricsLogPath)) {
      allMetrics = JSON.parse(await fs.promises.readFile(metricsLogPath, 'utf8'));
      console.log(colors.bright + '📊 Historical Trends (Last 10 Runs):' + colors.reset);
      const last10 = allMetrics.slice(-10);

      last10.forEach((run, index) => {
        const date = new Date(run.timestamp).toLocaleDateString();
        const trend = run.passRate >= passRate ? '↑' : '↓';
        console.log(`  ${index + 1}. ${date} - Pass Rate: ${run.passRate}% ${trend}`);
      });
      console.log('');
    }

    // Add current metrics
    const currentMetrics = {
      timestamp: new Date().toISOString(),
      total: totalTests,
      passed: passedTests,
      failed: failedTests,
      skipped: skippedTests,
      passRate: parseFloat(passRate)
    };

    allMetrics.push(currentMetrics);
    await fs.promises.writeFile(metricsLogPath, JSON.stringify(allMetrics, null, 2));

    console.log(colors.green + '✅ Metrics updated successfully!\n' + colors.reset);

  } catch (error) {
    console.log(colors.red + '❌ Error reading test results: ' + error.message + '\n' + colors.reset);
  }
}

/**
 * Clean Build Artifacts (Option C)
 */
async function cleanBuildArtifacts() {
  console.log(colors.green + '\n🧹 Clean Build Artifacts\n' + colors.reset);
  console.log('This will remove:\n');
  console.log('  • target/ directory (Maven build files)');
  console.log('  • test-health-logs/*.log (old log files)');
  console.log('  • Old test reports (keeping last 5)');
  console.log('  • Failed test screenshots (>7 days old)\n');

  const fs = require('fs');
  const path = require('path');

  // Calculate size to be cleaned
  let totalSize = 0;
  const targetDir = path.join(process.cwd(), 'target');

  if (fs.existsSync(targetDir)) {
    totalSize = await getDirSize(targetDir);
  }

  const sizeMB = (totalSize / (1024 * 1024)).toFixed(2);
  console.log(colors.cyan + `📦 Estimated space to be freed: ${sizeMB} MB\n` + colors.reset);

  const confirm = await question(colors.yellow + '⚠️  Proceed with cleanup? (Y/n): ' + colors.reset);

  if (confirm.toLowerCase() === 'n') {
    console.log(colors.cyan + '\n✓ Cleanup cancelled\n' + colors.reset);
    return;
  }

  console.log(colors.yellow + '\n🧹 Cleaning...\n' + colors.reset);

  let cleaned = 0;

  // Clean target directory
  if (fs.existsSync(targetDir)) {
    await deleteFolderRecursive(targetDir);
    console.log(colors.green + '  ✓ Cleaned target/' + colors.reset);
    cleaned += totalSize;
  }

  // Clean old logs
  const logsDir = path.join(process.cwd(), 'test-health-logs');
  if (fs.existsSync(logsDir)) {
    const files = await fs.promises.readdir(logsDir);
    for (const file of files) {
      if (file.endsWith('.log')) {
        await fs.promises.unlink(path.join(logsDir, file));
      }
    }
    console.log(colors.green + '  ✓ Cleaned test-health-logs/*.log' + colors.reset);
  }

  // Clean old screenshots (>7 days)
  const screenshotsDir = path.join(process.cwd(), 'target', 'screenshots');
  if (fs.existsSync(screenshotsDir)) {
    const files = await fs.promises.readdir(screenshotsDir);
    const now = Date.now();
    const sevenDays = 7 * 24 * 60 * 60 * 1000;

    for (const file of files) {
      const filePath = path.join(screenshotsDir, file);
      const stats = await fs.promises.stat(filePath);
      if (now - stats.mtimeMs > sevenDays) {
        await fs.promises.unlink(filePath);
      }
    }
    console.log(colors.green + '  ✓ Cleaned old screenshots' + colors.reset);
  }

  const cleanedMB = (cleaned / (1024 * 1024)).toFixed(2);
  console.log(colors.green + `\n✅ Cleanup complete! Freed ${cleanedMB} MB\n` + colors.reset);
}

async function getDirSize(dirPath) {
  const fs = require('fs');
  const path = require('path');
  let size = 0;

  try {
    const files = await fs.promises.readdir(dirPath);
    for (const file of files) {
      const filePath = path.join(dirPath, file);
      const stats = await fs.promises.stat(filePath);
      if (stats.isDirectory()) {
        size += await getDirSize(filePath);
      } else {
        size += stats.size;
      }
    }
  } catch (err) {
    // Ignore errors
  }

  return size;
}

async function deleteFolderRecursive(dirPath) {
  const fs = require('fs');
  const path = require('path');

  if (fs.existsSync(dirPath)) {
    const files = await fs.promises.readdir(dirPath);
    for (const file of files) {
      const curPath = path.join(dirPath, file);
      const stats = await fs.promises.stat(curPath);
      if (stats.isDirectory()) {
        await deleteFolderRecursive(curPath);
      } else {
        await fs.promises.unlink(curPath);
      }
    }
    await fs.promises.rmdir(dirPath);
  }
}

/**
 * Show Help (Option H)
 */
async function showHelp() {
  console.log(colors.cyan + '\n' + '='.repeat(70) + colors.reset);
  console.log(colors.bright + colors.cyan + '  📖 AI TEST AUTOMATION - COMPLETE COMMAND REFERENCE' + colors.reset);
  console.log(colors.cyan + '='.repeat(70) + '\n' + colors.reset);

  console.log(colors.yellow + '🎯 QUICK START:' + colors.reset);
  console.log('   1. First time? Run Option S (Setup) to install dependencies');
  console.log('   2. Use Option 1 (Record) for fastest test creation');
  console.log('   3. Validate with Option V before running tests');
  console.log('   4. Run tests with Option 6 (Full Suite) or Option 4 (Tagged)\n');

  console.log(colors.yellow + '📝 ALL OPTIONS:' + colors.reset);
  console.log('');
  console.log(colors.bright + '  📊 TEST GENERATION:' + colors.reset);
  console.log(colors.green + '   1' + colors.reset + '   [RECORD]   - Playwright Recording → Auto-generate (5-10 min)');
  console.log(colors.green + '   1B' + colors.reset + '  [RETRY]    - Regenerate from existing recording');
  console.log(colors.green + '   2' + colors.reset + '   [JIRA]     - JIRA Story → Complete test suite');
  console.log(colors.green + '   3' + colors.reset + '   [AI]       - AI-assisted interactive generation');
  console.log('');
  console.log(colors.bright + '  ⚙️  SETUP & VALIDATION:' + colors.reset);
  console.log(colors.green + '   S' + colors.reset + '   [SETUP]    - Complete project setup (first-time)');
  console.log(colors.green + '   V' + colors.reset + '   [VALIDATE] - Code validation & auto-fix');
  console.log('');
  console.log(colors.bright + '  🧪 TEST EXECUTION:' + colors.reset);
  console.log(colors.green + '   4' + colors.reset + '   [TAG]      - Run tagged tests (@smoke, @regression)');
  console.log(colors.green + '   5' + colors.reset + '   [PARALLEL] - Parallel execution (faster)');
  console.log(colors.green + '   6' + colors.reset + '   [RUN]      - Full test suite (clean → compile → test)');
  console.log('');
  console.log(colors.bright + '  📈 REPORTING & UTILITIES:' + colors.reset);
  console.log(colors.green + '   R' + colors.reset + '   [REPORT]   - Generate & view reports (HTML/JSON/Allure)');
  console.log(colors.green + '   M' + colors.reset + '   [METRICS]  - Test metrics dashboard (pass rates, trends)');
  console.log(colors.green + '   C' + colors.reset + '   [CLEAN]    - Clean build artifacts (free space)');
  console.log('');
  console.log(colors.bright + '  ❓ HELP:' + colors.reset);
  console.log(colors.green + '   H' + colors.reset + '   [HELP]     - Show this help guide');
  console.log(colors.green + '   0' + colors.reset + '   [EXIT]     - Exit application\n');

  console.log(colors.yellow + '💻 NPM SCRIPT SHORTCUTS:' + colors.reset);
  console.log('   npm run record    → Option 1  | npm run retry     → Option 1B');
  console.log('   npm run jira      → Option 2  | npm run ai        → Option 3');
  console.log('   npm run setup     → Option S  | npm run validate  → Option V');
  console.log('   npm run tag       → Option 4  | npm run parallel  → Option 5');
  console.log('   npm run run       → Option 6  | npm run report    → Option R');
  console.log('   npm run metrics   → Option M  | npm run clean     → Option C');
  console.log('   npm run help      → Option H\n');

  console.log(colors.yellow + '📂 GENERATED FILES:' + colors.reset);
  console.log('   • Page Objects: src/main/java/pages/YourFeature.java');
  console.log('   • Features: src/test/java/features/YourFeature.feature');
  console.log('   • Steps: src/test/java/stepDefs/YourFeatureSteps.java\n');

  console.log(colors.yellow + '📊 REPORTS LOCATION:' + colors.reset);
  console.log('   • HTML: target/cucumber-reports/cucumber.html');
  console.log('   • JSON: target/json-report/cucumber.json');
  console.log('   • Extent: target/extent-reports/index.html');
  console.log('   • Screenshots: target/screenshots/');
  console.log('   • Logs: test-health-logs/\n');

  console.log(colors.yellow + '⚠️  COMMON ISSUES:' + colors.reset);
  console.log('   • Recording not working? Run Option S (Setup) first');
  console.log('   • Compilation errors? Run Option V (Validate) with auto-fix');
  console.log('   • JIRA errors? Update jiraConfigurations.properties');
  console.log('   • Tests slow? Use Option 5 (Parallel) instead of Option 6\n');

  console.log(colors.yellow + '📖 DOCUMENTATION:' + colors.reset);
  console.log('   • Full Guide: README.md');
  console.log('   • AI Prompts: docs/AI_PROMPT_TEMPLATES.md');
  console.log('   • Workflow: WORKFLOW_TODOS.md\n');

  console.log(colors.cyan + '='.repeat(70) + '\n' + colors.reset);
}

/**
 * Exit function
 */
async function exit() {
  return { exit: true };
}

/**
 * Main Program Loop
 */
async function main() {
  displayWelcome();

  // Validate Recorded directory structure
  await validateRecordedDirectory();

  // Check and start MCP server if needed
  await ensureMCPServer();

  let running = true;

  while (running) {
    const choice = await displayMenu();
    const result = await executeMenuAction(choice);

    if (result.exit) {
      console.log(colors.green + '\n👋 Thanks for using AI Automation Generator!\n' + colors.reset);

      // Cleanup MCP server if we started it
      if (mcpServerProcess) {
        console.log(colors.yellow + '🛑 Stopping MCP server...' + colors.reset);
        mcpServerProcess.kill();
      }

      running = false;
    } else if (choice !== '9') {
      // Don't pause after tutorial since it already has its own pause
      await question(colors.cyan + '\nPress Enter to continue...' + colors.reset);
    }
  }

  rl.close();
}

// ============================================================================
// CLI ARGUMENT SUPPORT (for NPM scripts integration)
// ============================================================================

/**
 * Parse and handle command-line arguments
 * Supports:
 *   node automation-cli.js --option=1      → Run Option 1 (RECORD)
 *   node automation-cli.js --option=1B     → Run Option 1B (RETRY)
 *   node automation-cli.js --option=2      → Run Option 2 (JIRA)
 *   node automation-cli.js --option=3      → Run Option 3 (AI)
 *   node automation-cli.js --option=S      → Run Option S (SETUP)
 *   node automation-cli.js --option=V      → Run Option V (VALIDATE)
 *   node automation-cli.js --option=4      → Run Option 4 (TAG)
 *   node automation-cli.js --option=5      → Run Option 5 (PARALLEL)
 *   node automation-cli.js --option=6      → Run Option 6 (RUN)
 *   node automation-cli.js --option=R      → Run Option R (REPORT)
 *   node automation-cli.js --option=M      → Run Option M (METRICS)
 *   node automation-cli.js --option=C      → Run Option C (CLEAN)
 *   node automation-cli.js --option=H      → Run Option H (HELP)
 *   node automation-cli.js interactive    → Run Option 3 directly (backward compat)
 *   node automation-cli.js jira <id>      → Run Option 2 with story ID (backward compat)
 *   node automation-cli.js                → Show interactive menu (default)
 * 
 * NPM script mapping:
 *   npm run record        → --option=1  (RECORD - Playwright Recording)
 *   npm run retry         → --option=1B (RETRY - From Existing Recording)
 *   npm run jira          → --option=2  (JIRA - From Story)
 *   npm run ai            → --option=3  (AI - Interactive Generation)
 *   npm run setup         → --option=S  (SETUP - Project Setup)
 *   npm run validate      → --option=V  (VALIDATE - Code Validation)
 *   npm run tag           → --option=4  (TAG - Run Tagged Tests)
 *   npm run parallel      → --option=5  (PARALLEL - Parallel Execution)
 *   npm run run           → --option=6  (RUN - Full Test Suite)
 *   npm run report        → --option=R  (REPORT - Generate Reports)
 *   npm run metrics       → --option=M  (METRICS - Test Metrics)
 *   npm run clean         → --option=C  (CLEAN - Clean Artifacts)
 *   npm run help          → --option=H  (HELP - Show Help)
 */
async function handleCLIArguments() {
  const args = process.argv.slice(2);

  // Check for --option=N format (NPM scripts)
  for (const arg of args) {
    if (arg.startsWith('--option=')) {
      const optionKey = arg.split('=')[1];

      displayWelcome();

      // Validate Recorded directory structure
      await validateRecordedDirectory();

      // Map option to menu choice (matches MENU_CONFIG keys exactly)
      const validOptions = ['1', '1B', '2', '3', 'S', 'V', '4', '5', '6', 'R', 'M', 'C', 'H', '0'];

      // Options that don't require MCP server
      const noMCPOptions = ['H', 'V', 'R', 'M', 'C', '4', '5', '6', '0'];

      if (validOptions.includes(optionKey)) {
        // Only start MCP server for options that need it
        if (!noMCPOptions.includes(optionKey)) {
          await ensureMCPServer();
        }

        console.log(colors.cyan + `\n🚀 Executing option ${optionKey}...\n` + colors.reset);
        await executeMenuAction(optionKey);

        // Cleanup and exit
        if (mcpServerProcess) {
          console.log(colors.yellow + '\n🛑 Stopping MCP server...' + colors.reset);
          mcpServerProcess.kill();
        }
        rl.close();
        return true;
      } else {
        console.log(colors.red + `\n❌ Error: Unknown option "${optionKey}"` + colors.reset);
        console.log(colors.yellow + 'Valid options: 1, 1B, 2, 3, S, V, 4, 5, 6, R, M, C, H, 0' + colors.reset);
        rl.close();
        return true;
      }
    }
  }

  // Backward compatibility: Check for old-style modes
  const mode = args[0];

  if (!mode) {
    // No arguments, run interactive menu
    return false;
  }

  displayWelcome();
  await ensureMCPServer();

  if (mode === 'interactive') {
    console.log(colors.cyan + '\n🚀 Running in Interactive Mode (Option 3)\n' + colors.reset);
    await generateCompleteTestSuite();

    console.log(colors.green + '\n✅ Interactive generation complete!' + colors.reset);
    console.log(colors.cyan + 'Check generated files and run Option 4 to validate.\n' + colors.reset);

    // Cleanup and exit
    if (mcpServerProcess) {
      console.log(colors.yellow + '🛑 Stopping MCP server...' + colors.reset);
      mcpServerProcess.kill();
    }
    rl.close();
    return true;

  } else if (mode === 'jira') {
    const storyId = args[1];

    if (!storyId) {
      console.log(colors.red + '\n❌ Error: JIRA Story ID required!' + colors.reset);
      console.log(colors.yellow + 'Usage: node automation-cli.js jira <STORY-ID>' + colors.reset);
      console.log(colors.yellow + 'Example: node automation-cli.js jira ECS-123\n' + colors.reset);
      rl.close();
      return true;
    }

    console.log(colors.cyan + `\n🚀 Running in JIRA Mode (Option 2) with story: ${storyId}\n` + colors.reset);

    // Set story ID for the function
    global.cliJiraStoryId = storyId;
    await generateTestFromJiraStory();

    console.log(colors.green + '\n✅ JIRA-based generation complete!' + colors.reset);
    console.log(colors.cyan + 'Check generated files and run Option 4 to validate.\n' + colors.reset);

    // Cleanup and exit
    if (mcpServerProcess) {
      console.log(colors.yellow + '🛑 Stopping MCP server...' + colors.reset);
      mcpServerProcess.kill();
    }
    rl.close();
    return true;

  } else {
    console.log(colors.red + `\n❌ Error: Unknown mode "${mode}"` + colors.reset);
    console.log(colors.yellow + '\nSupported modes:' + colors.reset);
    console.log(colors.cyan + '  node automation-cli.js --option=N' + colors.reset + ' → Run menu option N directly (1-8)');
    console.log(colors.cyan + '  node automation-cli.js interactive' + colors.reset + ' → AI-guided test generation');
    console.log(colors.cyan + '  node automation-cli.js jira <ID>' + colors.reset + ' → Generate from JIRA story');
    console.log(colors.cyan + '  node automation-cli.js' + colors.reset + ' → Interactive menu (default)\n');
    rl.close();
    return true;
  }
}

// Run the CLI with argument support
(async () => {
  const handled = await handleCLIArguments();
  if (!handled) {
    // No CLI args, run interactive menu
    await main();
  }
})().catch(console.error);
