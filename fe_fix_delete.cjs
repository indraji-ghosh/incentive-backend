const fs = require('fs');
let file = 'src/main/java/org/example/incentivebackend/module/master/servicetype/controller/ServiceTypeController.java';
let code = fs.readFileSync(file, 'utf8');
code = code.replace(/public ResponseEntity<ApiResponse<Void>> delete/g, 'public ResponseEntity<ApiResponse<Object>> delete');
fs.writeFileSync(file, code);
console.log("Fixed delete signature");
