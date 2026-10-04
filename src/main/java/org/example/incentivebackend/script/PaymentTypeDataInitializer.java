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
        
        // Clean up old duplicated payment types
        paymentTypeRepository.findAll().forEach(pt -> {
            if (!pt.getCode().equals("RAKE_BASED") && 
                !pt.getCode().equals("BILL_PAYMENT_BASED") && 
                !pt.getCode().equals("MONTHLY_FIXED")) {
                try {
                    paymentTypeRepository.delete(pt);
                } catch (Exception e) {
                    // Ignore if foreign key constraint fails
                }
            }
        });

        initPaymentType("RAKE_BASED", "Rake Based", "Payable calculated per rake transaction.");
        initPaymentType("BILL_PAYMENT_BASED", "Bill Payment Based", "Commission is payable after customer payment is received.");
        initPaymentType("MONTHLY_FIXED", "Monthly Fixed", "Payable calculated on a monthly fixed basis.");
    }

    private void initPaymentType(String code, String name, String description) {
        if (!paymentTypeRepository.existsByCodeIgnoreCase(code)) {
            PaymentTypeEntity entity = new PaymentTypeEntity();
            entity.setCode(code);
            entity.setName(name);
            entity.setDescription(description);
            paymentTypeRepository.save(entity);
        }
    }
}
