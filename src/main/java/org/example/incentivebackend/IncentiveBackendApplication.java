package org.example.incentivebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IncentiveBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncentiveBackendApplication.class, args);
    }

}
