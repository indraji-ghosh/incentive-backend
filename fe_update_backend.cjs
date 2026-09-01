const fs = require('fs');

let entityPath = 'src/main/java/org/example/incentivebackend/module/transaction/partyentry/entity/PartyUnitConfigurationEntity.java';
let entityCode = fs.readFileSync(entityPath, 'utf8');

if (!entityCode.includes('service_type_id')) {
    let newField = `
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = true)
    private org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity serviceType;
`;
    entityCode = entityCode.replace('private UnitEntity unit;', 'private UnitEntity unit;\n' + newField);
    fs.writeFileSync(entityPath, entityCode);
}

let reqPath = 'src/main/java/org/example/incentivebackend/module/transaction/partyentry/dto/request/PartyUnitConfigurationRequest.java';
let reqCode = fs.readFileSync(reqPath, 'utf8');
if (!reqCode.includes('serviceTypeId')) {
    reqCode = reqCode.replace('private Long unitId;', 'private Long unitId;\n    private Long serviceTypeId;');
    fs.writeFileSync(reqPath, reqCode);
}

let resPath = 'src/main/java/org/example/incentivebackend/module/transaction/partyentry/dto/response/PartyUnitConfigurationResponse.java';
let resCode = fs.readFileSync(resPath, 'utf8');
if (!resCode.includes('serviceTypeId')) {
    resCode = resCode.replace('private Long unitId;', 'private Long unitId;\n    private Long serviceTypeId;\n    private String serviceTypeName;');
    fs.writeFileSync(resPath, resCode);
}

console.log("Updated entities and DTOs");
