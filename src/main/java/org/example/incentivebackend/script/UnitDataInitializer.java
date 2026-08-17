package org.example.incentivebackend.script;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitDataInitializer implements CommandLineRunner {

    private final UnitRepository unitRepository;

    @Override
    public void run(String... args) {
        initUnit("RAKE", "Rake", "Commission or incentive calculated per rake.");
        initUnit("WAGON", "Wagon", "Commission or incentive calculated per wagon.");
        initUnit("MT", "Metric Ton", "Commission or incentive calculated based on metric ton.");
        initUnit("FIXED", "Fixed", "Commission or incentive based on a fixed amount.");
        initUnit("MONTHLY", "Monthly", "Commission or incentive calculated on a monthly basis.");
    }

    private void initUnit(String code, String name, String description) {
        if (!unitRepository.existsByCodeIgnoreCase(code)) {
            UnitEntity entity = new UnitEntity();
            entity.setCode(code);
            entity.setName(name);
            entity.setDescription(description);
            unitRepository.save(entity);
        }
    }
}
