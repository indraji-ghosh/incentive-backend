package org.example.incentivebackend.module.master.businessHead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessHeadLookupResponseDTO {
    private Long headId;
    List<String> headName;
}

