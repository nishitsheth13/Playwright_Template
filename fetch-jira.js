const https = require('https');
const fs = require('fs');

// Read Jira configuration
const config = {};
const rawContent = fs.readFileSync('src/test/resources/jiraConfigurations.properties', 'utf8');

rawContent.split('\n').forEach(line => {
    const trimmedLine = line.trim().replace(/\r$/,''); // Remove carriage returns
    const [key, ...valueParts] = trimmedLine.split('=');
    if (key && valueParts.length > 0) {
      config[key.trim()] = valueParts.join('=').trim();
    }
});

console.log('📋 Config loaded:', Object.keys(config));

// Get issue key from command line argument or list all
const issueKey = process.argv[2];

const auth = 'Basic ' + Buffer.from(config.JIRA_EMAIL + ':' + config.JIRA_API_TOKEN).toString('base64');

let options;
let postData = null;

if (issueKey) {
  // Fetch specific issue
  console.log(`🎯 Fetching issue: ${issueKey}`);
  options = {
    hostname: 'nishitmrisoftware.atlassian.net',
    path: `/rest/api/3/issue/${issueKey}`,
    method: 'GET',
    headers: {
      'Authorization': auth,
      'Accept': 'application/json'
    }
  };
} else {
  // List all issues in project
  console.log('🎯 Listing all issues in ECS project');
  postData = JSON.stringify({
    jql: 'project=ECS',
    maxResults: 50,
    fields: ['key', 'summary', 'issuetype', 'status', 'priority', 'description']
  });
  
  options = {
    hostname: 'nishitmrisoftware.atlassian.net',
    path: '/rest/api/3/search/jql',
    method: 'POST',
    headers: {
      'Authorization': auth,
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(postData)
    }
  };
}

const req = https.request(options, res => {
  let rawData = '';
  res.on('data', chunk => { rawData += chunk; });
  
  res.on('end', () => {
    console.log(`📡 Status Code: ${res.statusCode}`);
    
    if (rawData.length === 0) {
      console.log('❌ Empty response received!');
      return;
    }
    
    try {
      const data = JSON.parse(rawData);
      fs.writeFileSync('jira-story.json', JSON.stringify(data, null, 2));
      
      if (issueKey) {
        // Single issue response
        console.log('\n✅ JIRA Issue Found:');
        console.log(`Key: ${data.key}`);
        console.log(`Summary: ${data.fields.summary}`);
        console.log(`Type: ${data.fields.issuetype.name}`);
        console.log(`Status: ${data.fields.status.name}`);
        console.log(`\nDescription:\n${data.fields.description?.content?.[0]?.content?.[0]?.text || 'No description'}`);
      } else {
        // List of issues response
        console.log(`\n✅ Found ${data.total} issues in ECS project:`);
        data.issues.forEach(issue => {
          console.log(`\n${issue.key}: ${issue.fields.summary}`);
          console.log(`  Type: ${issue.fields.issuetype.name} | Status: ${issue.fields.status.name}`);
        });
      }
    } catch (error) {
      console.error('❌ JSON Parse Error:', error.message);
      console.log('Raw response:', rawData.substring(0, 1000));
    }
  });
});

// Handle POST request body
if (postData) {
  req.write(postData);
}

req.on('error', error => {
  console.error('❌ Request Error:', error);
});

req.end();
