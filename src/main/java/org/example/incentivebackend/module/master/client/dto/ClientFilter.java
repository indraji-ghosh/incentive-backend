package org.example.incentivebackend.module.master.client.dto;


import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class ClientFilter {

    private String search;

    private StatusEnum clientStatus;

    private int page = 0;

    private int size = 10;

    private String sortBy = "clientId";

    private String sortDirection = "desc";
}
