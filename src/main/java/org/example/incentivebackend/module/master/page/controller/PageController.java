package org.example.incentivebackend.module.master.page.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.constant.ApiConstants;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.page.dto.PageResponseDTO;
import org.example.incentivebackend.module.master.page.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/master/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PageResponseDTO>>> getAllPages() {
        List<PageResponseDTO> data = pageService.getAllPages();
        return ResponseBuilder.list("Pages", data);
    }
}
