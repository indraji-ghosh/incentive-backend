package org.example.incentivebackend.module.master.designation.service;

import org.example.incentivebackend.module.master.designation.dto.DesignationRequestDTO;
import org.example.incentivebackend.module.master.designation.dto.DesignationResponseDTO;
import java.util.List;

public interface DesignationService {
    List<DesignationResponseDTO> getAllDesignations();
    DesignationResponseDTO createDesignation(DesignationRequestDTO request);
    DesignationResponseDTO updateDesignation(Long id, DesignationRequestDTO request);
    void deleteDesignation(Long id);
}
