package org.example.incentivebackend.module.master.page.service;

import org.example.incentivebackend.module.master.page.dto.PageResponseDTO;
import java.util.List;

public interface PageService {
    List<PageResponseDTO> getAllPages();
}
