package org.example.incentivebackend.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public final class ResponseBuilder {

    private ResponseBuilder() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(
            String resource,
            T data
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(resource + " created successfully")
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static <T> ResponseEntity<ApiResponse<T>> fetched(
            String resource,
            T data
    ) {

        return ResponseEntity
                .ok(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(resource + " fetched successfully")
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static <T> ResponseEntity<ApiResponse<T>> updated(
            String resource,
            T data
    ) {

        return ResponseEntity
                .ok(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(resource + " updated successfully")
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static ResponseEntity<ApiResponse<Object>> deleted(
            String resource
    ) {

        return ResponseEntity
                .ok(
                        ApiResponse.builder()
                                .success(true)
                                .message(resource + " deleted successfully")
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static <T> ResponseEntity<ApiResponse<T>> list(
            String resource,
            T data
    ) {

        return ResponseEntity
                .ok(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(resource + " list fetched successfully")
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }
}