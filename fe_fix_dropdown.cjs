const fs = require('fs');

const files = [
    'src/main/java/org/example/incentivebackend/module/master/servicetype/controller/ServiceTypeController.java',
    'src/main/java/org/example/incentivebackend/module/master/servicetype/service/ServiceTypeService.java',
    'src/main/java/org/example/incentivebackend/module/master/servicetype/service/ServiceTypeServiceImpl.java'
];

files.forEach(file => {
    let code = fs.readFileSync(file, 'utf8');
    code = code.replace(/org\.example\.incentivebackend\.common\.dto\.DropdownOptionDTO/g, 'org.example.incentivebackend.common.DropdownDTO');
    code = code.replace(/DropdownOptionDTO/g, 'DropdownDTO');
    fs.writeFileSync(file, code);
});
console.log("Fixed DropdownDTO");
