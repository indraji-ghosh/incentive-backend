const fs = require('fs');

const docFolder = 'document';
if (!fs.existsSync(docFolder)) {
    fs.mkdirSync(docFolder);
}

const apiDocs = JSON.parse(fs.readFileSync('api-docs.json', 'utf8'));

let md = `# Commission Payment API Frontend Contracts\n\n`;

const paths = apiDocs.paths;
const components = apiDocs.components.schemas;

function resolveRef(ref) {
    if (!ref) return null;
    const schemaName = ref.split('/').pop();
    return components[schemaName];
}

function formatSchema(schema, indent = "") {
    if (!schema) return "unknown";
    if (schema.$ref) {
        const resolved = resolveRef(schema.$ref);
        return formatSchema(resolved, indent);
    }
    if (schema.type === 'object' && schema.properties) {
        let out = "{\n";
        for (const [key, prop] of Object.entries(schema.properties)) {
            out += `${indent}  "${key}": ${formatSchema(prop, indent + "  ")},\n`;
        }
        out += `${indent}}`;
        return out;
    }
    if (schema.type === 'array' && schema.items) {
        return `[\n${indent}  ${formatSchema(schema.items, indent + "  ")}\n${indent}]`;
    }
    return schema.type || "any";
}

for (const [path, methods] of Object.entries(paths)) {
    // Check if path belongs to commission payment
    if (path.includes('/commission-payments') || path.includes('/commission-payment')) {
        
        for (const [method, details] of Object.entries(methods)) {
            md += `## [${method.toUpperCase()}] ${path}\n`;
            md += `**Summary**: ${details.summary || 'N/A'}\n\n`;
            
            if (details.parameters && details.parameters.length > 0) {
                md += `### Parameters\n`;
                for (const p of details.parameters) {
                    md += `- **${p.name}** (${p.in}): ${p.schema ? p.schema.type : 'any'} ${p.required ? '(Required)' : ''}\n`;
                }
                md += `\n`;
            }
            
            if (details.requestBody && details.requestBody.content && details.requestBody.content['application/json']) {
                md += `### Request Body\n`;
                md += '```json\n';
                const schema = details.requestBody.content['application/json'].schema;
                md += formatSchema(schema);
                md += '\n```\n\n';
            }
            
            if (details.responses && details.responses['200'] && details.responses['200'].content && details.responses['200'].content['*/*']) {
                md += `### Response (200 OK)\n`;
                md += '```json\n';
                const schema = details.responses['200'].content['*/*'].schema;
                md += formatSchema(schema);
                md += '\n```\n\n';
            } else if (details.responses && details.responses['200'] && details.responses['200'].content && details.responses['200'].content['application/json']) {
                md += `### Response (200 OK)\n`;
                md += '```json\n';
                const schema = details.responses['200'].content['application/json'].schema;
                md += formatSchema(schema);
                md += '\n```\n\n';
            }
            
            md += `---\n\n`;
        }
    }
}

fs.writeFileSync(`${docFolder}/commision.txt`, md);
console.log('Documentation generated successfully in document/commision.txt');
