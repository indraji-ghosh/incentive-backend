const fs = require('fs');
let file = 'src/main/java/org/example/incentivebackend/module/master/servicetype/controller/ServiceTypeController.java';
let code = fs.readFileSync(file, 'utf8');

code = code.replace(/"\/api\/v1\/master\/service-types"/, '"/api/master/service-types"');

fs.writeFileSync(file, code);
console.log("Updated ServiceTypeController path");
