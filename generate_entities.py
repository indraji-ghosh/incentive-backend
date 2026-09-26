import os
import re

root_dir = r'c:\Users\nilan\Documents\incentive\incentive-backend\src\main\java\org\example\incentivebackend'
out_file = r'c:\Users\nilan\Documents\incentive\incentive-backend\document\entities.md'

entities = []

for dirpath, _, filenames in os.walk(root_dir):
    for filename in filenames:
        if filename.endswith('.java'):
            filepath = os.path.join(dirpath, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                if '@Entity' in content:
                    # extract class name
                    class_match = re.search(r'class\s+(\w+)', content)
                    if not class_match:
                        continue
                    class_name = class_match.group(1)
                    
                    # extract table name
                    table_name = 'Unknown'
                    table_match = re.search(r'@Table\s*\([^)]*name\s*=\s*["\']([^"\']+)["\']', content)
                    if table_match:
                        table_name = table_match.group(1)
                        
                    # extract fields
                    fields = []
                    lines = content.split('\n')
                    for i, line in enumerate(lines):
                        line = line.strip()
                        # simple heuristic for fields
                        if line.startswith('private ') and not '(' in line and not '=' in line:
                            parts = line.replace(';', '').split()
                            if len(parts) >= 3:
                                f_type = parts[1]
                                f_name = parts[2]
                                
                                # Check for relationships or special annotations in previous lines
                                annotations = []
                                j = i - 1
                                while j >= 0 and lines[j].strip().startswith('@'):
                                    ann = lines[j].strip()
                                    if any(x in ann for x in ['@Id', '@ManyToOne', '@OneToMany', '@OneToOne', '@Enumerated', '@Column']):
                                        # just get the first part of annotation before arguments
                                        ann_name = ann.split('(')[0]
                                        annotations.append(ann_name)
                                    j -= 1
                                    
                                fields.append((f_type, f_name, annotations))
                    
                    entities.append((class_name, table_name, fields, dirpath))

# Group entities by module directory
modules = {}
for class_name, table_name, fields, dirpath in entities:
    # Try to extract the module name from the path, e.g. "transaction", "master"
    path_parts = dirpath.split(os.sep)
    try:
        module_idx = path_parts.index('module')
        module_name = path_parts[module_idx + 1].capitalize()
    except ValueError:
        module_name = 'Common'
    
    if module_name not in modules:
        modules[module_name] = []
    modules[module_name].append((class_name, table_name, fields))

# Now build markdown
md = '# Incentive Project Entities\n\nThis document lists all the entity classes present in the Incentive Backend project, along with their fields and relationships.\n\n'

for module_name, ents in sorted(modules.items()):
    md += f'## {module_name}\n\n'
    for class_name, table_name, fields in sorted(ents, key=lambda x: x[0]):
        md += f'### `{class_name}`\n'
        md += f'- **Table Name:** `{table_name}`\n'
        md += '- **Fields:**\n'
        if not fields:
            md += '  - (No parsed fields)\n'
        for f_type, f_name, anns in fields:
            ann_str = f' ({", ".join(anns)})' if anns else ''
            md += f'  - `{f_type}` `{f_name}`{ann_str}\n'
        md += '\n'

with open(out_file, 'w', encoding='utf-8') as f:
    f.write(md)

print(f'Done writing to {out_file}')
