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

        // New standard business payment concepts
        initPaymentType("RAKE_BASED", "Rake Based", "Payable calculated per rake transaction.");
        initPaymentType("WAGON_BASED", "Wagon Based", "Payable calculated per wagon count.");
        initPaymentType("MT_BASED", "MT Based", "Payable calculated per metric ton.");
        initPaymentType("MONTHLY_FIXED", "Monthly Fixed", "Payable calculated on a monthly fixed basis on the 1st of the month.");
        initPaymentType("BILL_BASED", "Bill Based", "Payable calculated against bill generation.");
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
