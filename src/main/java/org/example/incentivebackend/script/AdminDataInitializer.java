package org.example.incentivebackend.script;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.master.user.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByUsername("admin")) {
            return;
        }

        UserEntity admin = UserEntity.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .email("admin@incentive.com")
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
    }
}
