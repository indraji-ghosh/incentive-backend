const fs = require('fs');
const path = require('path');

const rootDir = 'c:\\Users\\nilan\\Documents\\incentive\\incentive-backend\\src\\main\\java\\org\\example\\incentivebackend';
const outFile = 'c:\\Users\\nilan\\Documents\\incentive\\incentive-backend\\document\\entities.md';

const entities = [];

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

walkDir(rootDir, function(filePath) {
    if (filePath.endsWith('.java')) {
        const content = fs.readFileSync(filePath, 'utf-8');
        if (content.includes('@Entity')) {
            // extract class name
            const classMatch = content.match(/class\s+(\w+)/);
            if (!classMatch) return;
            const className = classMatch[1];
            
            // extract table name
            let tableName = 'Unknown';
            const tableMatch = content.match(/@Table\s*\([^)]*name\s*=\s*["']([^"']+)["']/);
            if (tableMatch) {
                tableName = tableMatch[1];
            }
            
            // extract fields
            const fields = [];
            const lines = content.split(/\r?\n/);
            for (let i = 0; i < lines.length; i++) {
                const line = lines[i].trim();
                // simple heuristic for fields
                if (line.startsWith('private ') && !line.includes('(') && !line.includes('=')) {
                    const parts = line.replace(';', '').split(/\s+/);
                    if (parts.length >= 3) {
                        const fType = parts[1];
                        const fName = parts[2];
                        
                        // Check for relationships or special annotations in previous lines
                        const annotations = [];
                        let j = i - 1;
                        while (j >= 0 && lines[j].trim().startsWith('@')) {
                            const ann = lines[j].trim();
                            if (['@Id', '@ManyToOne', '@OneToMany', '@OneToOne', '@Enumerated', '@Column'].some(a => ann.includes(a))) {
                                // just get the first part of annotation before arguments
                                const annName = ann.split('(')[0];
                                annotations.push(annName);
                            }
                            j--;
                        }
                        
                        fields.push({ fType, fName, annotations });
                    }
                }
            }
            
            entities.push({ className, tableName, fields, dirPath: path.dirname(filePath) });
        }
    }
});

// Group entities by module directory
const modules = {};
for (const entity of entities) {
    const pathParts = entity.dirPath.split(path.sep);
    const moduleIdx = pathParts.indexOf('module');
    let moduleName = 'Common';
    if (moduleIdx !== -1 && moduleIdx + 1 < pathParts.length) {
        const rawMod = pathParts[moduleIdx + 1];
        moduleName = rawMod.charAt(0).toUpperCase() + rawMod.slice(1);
    }
    
    if (!modules[moduleName]) {
        modules[moduleName] = [];
    }
    modules[moduleName].push(entity);
}

// Now build markdown
let md = '# Incentive Project Entities\n\nThis document lists all the entity classes present in the Incentive Backend project, along with their fields and relationships.\n\n';

for (const moduleName of Object.keys(modules).sort()) {
    md += `## ${moduleName}\n\n`;
    const ents = modules[moduleName].sort((a, b) => a.className.localeCompare(b.className));
    for (const ent of ents) {
        md += `### \`${ent.className}\`\n`;
        md += `- **Table Name:** \`${ent.tableName}\`\n`;
        md += `- **Fields:**\n`;
        if (ent.fields.length === 0) {
            md += '  - (No parsed fields)\n';
        }
        for (const f of ent.fields) {
            const annStr = f.annotations.length > 0 ? ` (${f.annotations.join(', ')})` : '';
            md += `  - \`${f.fType}\` \`${f.fName}\`${annStr}\n`;
        }
        md += '\n';
    }
}

fs.writeFileSync(outFile, md, 'utf-8');
console.log(`Done writing to ${outFile}`);
