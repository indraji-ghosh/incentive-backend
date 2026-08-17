package org.example.incentivebackend.common.bulkUpload.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponseDTO<REQ, RES> implements Serializable {

    private static final long serialVersionUID = 1L;

    private BulkUploadSummaryDTO summary;

    private List<RES> insertedRecords;

    private List<BulkUploadErrorDTO<REQ>> failedRecords;

}
