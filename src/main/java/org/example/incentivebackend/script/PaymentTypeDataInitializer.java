package org.example.incentivebackend.script;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentTypeDataInitializer implements CommandLineRunner {

    private final PaymentTypeRepository paymentTypeRepository;

    @Override
    public void run(String... args) {

        initPaymentType("RAKE", "Rake Based", "Commission is payable after rake covering is completed.");
        initPaymentType("BILL", "Bill Based", "Commission is payable after the bill is generated.");
        initPaymentType("BILL_PAYMENT", "Bill Payment Based", "Commission is payable after customer payment is received.");
        initPaymentType("FIXED", "Fixed", "Commission is paid according to a fixed agreed amount.");
    }

    private void initPaymentType(String code, String name, String description) {
        if (!paymentTypeRepository.existsByCodeIgnoreCase(code)) {
            PaymentTypeEntity entity = new PaymentTypeEntity();
            entity.setCode(code);
            entity.setName(name);
            entity.setDescription(description);
            entity.setAppStatus(StatusEnum.A.name());
            paymentTypeRepository.save(entity);
        }
    }
}
