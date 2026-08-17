package org.example.incentivebackend.common.bulkUpload.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadErrorDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer rowNumber;

    private T record;

    private String errorMessage;
}
