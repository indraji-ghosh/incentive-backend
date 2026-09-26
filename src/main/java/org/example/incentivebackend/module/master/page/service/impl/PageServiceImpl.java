package org.example.incentivebackend.module.master.page.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.page.dto.PageResponseDTO;
import org.example.incentivebackend.module.master.page.repository.PageRepository;
import org.example.incentivebackend.module.master.page.service.PageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Override
    public List<PageResponseDTO> getAllPages() {
        return pageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(page -> PageResponseDTO.builder()
                        .id(page.getId())
                        .pageCode(page.getPageCode())
                        .pageName(page.getPageName())
                        .moduleName(page.getModuleName())
                        .route(page.getRoute())
                        .icon(page.getIcon())
                        .displayOrder(page.getDisplayOrder())
                        .isActive(page.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }
}
