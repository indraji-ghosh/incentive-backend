package org.example.incentivebackend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;

    private String message;

    private LocalDateTime timestamp;
}
