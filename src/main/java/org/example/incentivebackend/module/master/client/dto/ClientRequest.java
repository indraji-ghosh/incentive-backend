package org.example.incentivebackend.module.master.client.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class ClientRequest {

    @NotBlank(message = "Client name is required")
    @Size(
            max = 150,
            message = "Client name cannot exceed 150 characters"
    )
    private String clientName;

    @NotBlank(message = "Client short code is required")
    @Size(
            max = 50,
            message = "Client short code cannot exceed 50 characters"
    )
    private String clientShortCode;

    private StatusEnum clientStatus;
}
