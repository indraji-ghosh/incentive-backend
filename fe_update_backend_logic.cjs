const fs = require('fs');

let mapperPath = 'src/main/java/org/example/incentivebackend/module/transaction/partyentry/mapper/PartyEntryMapper.java';
let mapperCode = fs.readFileSync(mapperPath, 'utf8');
if (!mapperCode.includes('serviceTypeId')) {
    mapperCode = mapperCode.replace('@Mapping(source = "unit.name", target = "unitName")', '@Mapping(source = "unit.name", target = "unitName")\n    @Mapping(source = "serviceType.id", target = "serviceTypeId")\n    @Mapping(source = "serviceType.name", target = "serviceTypeName")');
    fs.writeFileSync(mapperPath, mapperCode);
}

let serviceImplPath = 'src/main/java/org/example/incentivebackend/module/transaction/partyentry/service/PartyEntryServiceImpl.java';
let serviceImplCode = fs.readFileSync(serviceImplPath, 'utf8');
if (!serviceImplCode.includes('ServiceTypeRepository')) {
    // Add imports
    serviceImplCode = serviceImplCode.replace('import org.example.incentivebackend.module.master.unit.repository.UnitRepository;', 'import org.example.incentivebackend.module.master.unit.repository.UnitRepository;\nimport org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;\nimport org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;');
    
    // Add repository injection
    serviceImplCode = serviceImplCode.replace('private final UnitRepository unitRepository;', 'private final UnitRepository unitRepository;\n    private final ServiceTypeRepository serviceTypeRepository;');
    
    // Update config mapping
    let oldConfigMapping = `              if (unitReq.getId() != null && existingUnits.containsKey(unitReq.getId())) {
                  configEntity = existingUnits.get(unitReq.getId());
                  configEntity.setRate(unitReq.getRate());
                  configEntity.setEffectiveFrom(unitReq.getEffectiveFrom());
                  configEntity.setEffectiveTo(unitReq.getEffectiveTo());
                  configEntity.setNotes(unitReq.getNotes());
              } else {
                  configEntity = partyEntryMapper.toEntity(unitReq);
              }
              
              configEntity.setUnit(unit);`;
    
    let newConfigMapping = `              if (unitReq.getId() != null && existingUnits.containsKey(unitReq.getId())) {
                  configEntity = existingUnits.get(unitReq.getId());
                  configEntity.setRate(unitReq.getRate());
                  configEntity.setEffectiveFrom(unitReq.getEffectiveFrom());
                  configEntity.setEffectiveTo(unitReq.getEffectiveTo());
                  configEntity.setNotes(unitReq.getNotes());
              } else {
                  configEntity = partyEntryMapper.toEntity(unitReq);
              }
              
              configEntity.setUnit(unit);
              if (unitReq.getServiceTypeId() != null) {
                  ServiceTypeEntity serviceType = serviceTypeRepository.findById(unitReq.getServiceTypeId())
                          .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found: " + unitReq.getServiceTypeId()));
                  configEntity.setServiceType(serviceType);
              } else {
                  configEntity.setServiceType(null);
              }`;
    
    serviceImplCode = serviceImplCode.replace(oldConfigMapping, newConfigMapping);
    fs.writeFileSync(serviceImplPath, serviceImplCode);
}
console.log("Updated Mapper and ServiceImpl");
