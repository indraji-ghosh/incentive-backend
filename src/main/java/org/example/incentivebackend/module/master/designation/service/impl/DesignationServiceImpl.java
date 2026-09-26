package org.example.incentivebackend.module.master.designation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.designation.dto.DesignationRequestDTO;
import org.example.incentivebackend.module.master.designation.dto.DesignationResponseDTO;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.designation.repository.DesignationRepository;
import org.example.incentivebackend.module.master.designation.service.DesignationService;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;

    @Override
    public List<DesignationResponseDTO> getAllDesignations() {
        return designationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DesignationResponseDTO createDesignation(DesignationRequestDTO request) {
        if (designationRepository.existsByDesignationCodeIgnoreCase(request.getDesignationCode())) {
            throw new DuplicateResourceException("Designation code already exists");
        }
        
        DesignationEntity entity = new DesignationEntity();
        entity.setDesignationCode(request.getDesignationCode());
        entity.setDesignationName(request.getDesignationName());
        entity.setLevel(request.getLevel());
        entity.setDescription(request.getDescription());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return mapToDTO(designationRepository.save(entity));
    }

    @Override
    public DesignationResponseDTO updateDesignation(Long id, DesignationRequestDTO request) {
        DesignationEntity entity = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));
                
        if (!entity.getDesignationCode().equalsIgnoreCase(request.getDesignationCode()) && 
            designationRepository.existsByDesignationCodeIgnoreCase(request.getDesignationCode())) {
            throw new DuplicateResourceException("Designation code already exists");
        }
        
        entity.setDesignationCode(request.getDesignationCode());
        entity.setDesignationName(request.getDesignationName());
        entity.setLevel(request.getLevel());
        entity.setDescription(request.getDescription());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return mapToDTO(designationRepository.save(entity));
    }

    @Override
    public void deleteDesignation(Long id) {
        if (!designationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Designation not found");
        }
        designationRepository.deleteById(id);
    }

    private DesignationResponseDTO mapToDTO(DesignationEntity entity) {
        DesignationResponseDTO dto = new DesignationResponseDTO();
        dto.setId(entity.getId());
        dto.setDesignationCode(entity.getDesignationCode());
        dto.setDesignationName(entity.getDesignationName());
        dto.setLevel(entity.getLevel());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}
