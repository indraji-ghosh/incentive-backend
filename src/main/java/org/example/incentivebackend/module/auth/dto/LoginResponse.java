package org.example.incentivebackend.module.auth.dto;

import lombok.*;

@Getter
@Builder
public class LoginResponse {

    private String token;

    private Long userId;

    private String username;

    private String fullName;
}
