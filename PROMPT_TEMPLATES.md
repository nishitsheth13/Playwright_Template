# 🤖 AI Prompt Templates for Test Generation

Quick reference guide for generating tests using AI prompts. Copy and customize these templates for your needs.

---

## 🎫 JIRA Story-Based (Recommended)

**Basic:**
```
Create test from JIRA story {ISSUE-KEY}
```

**With Options:**
```
Generate test from JIRA story {ISSUE-KEY} with:
- Performance threshold: {X} seconds
- {Additional requirements}
```

**Example:**
```
Create test from JIRA story ECS-123
```

---

## 📝 Manual Test Generation

**Standard Test:**
```
Using MCP server, create {feature-name} test with:
Elements: {element1}, {element2}, {element3}
Scenarios: {scenario1}, {scenario2}
Verification: {functional/UI/performance/logging}
```

**Example:**
```
Using MCP server, create login test with:
Elements: username field, password field, submit button
Scenarios: successful login, invalid credentials
Verification: functional, UI, performance (<3s)
```

---

## 🔄 Update Existing Test

**Add Scenarios:**
```
Update {TestName} test to add scenarios: {scenario1}, {scenario2}
```

**Add Elements:**
```
Add {element-name} to {TestName} page object with {action} action
```

---

## 🎨 Page Object Only

```
Generate page object for {PageName} with elements:
- {element1} ({action})
- {element2} ({action})
```

---

## 🧪 Feature File Only

```
Generate Cucumber feature for {feature-name}:
Scenario: {scenario-name}
  Given {precondition}
  When {action}
  Then {expected-result}
```

---

## ⚡ Quick Verification

**Check Structure:**
```
Validate test structure for {TestName}
```

**Compile & Run:**
```
Compile and run tests for {TestName}
```

---

## 📋 Template Variables

Replace these placeholders:
- `{ISSUE-KEY}` → JIRA issue (e.g., ECS-123)
- `{feature-name}` → Test name (e.g., login, checkout)
- `{element}` → UI element (e.g., username field, submit button)
- `{action}` → Element action (type, click, select)
- `{scenario}` → Test scenario description
- `{X}` → Number value (e.g., 3 for 3 seconds)
- `{TestName}` → Existing test class name

---

## 💡 Tips

1. **Use JIRA prompts** when requirements exist in JIRA (fastest)
2. **Be specific** about elements and scenarios for manual generation
3. **Always specify verification types** (functional, UI, performance, logging)
4. **Include thresholds** for performance tests (e.g., <3s)
5. **One prompt at a time** for best results

---

**Need help?** See [AI_AUTOMATION_COMPLETE_GUIDE.md](AI_AUTOMATION_COMPLETE_GUIDE.md) for detailed examples.
