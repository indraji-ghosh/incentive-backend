package org.example.incentivebackend.module.master.sector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.master.sector.dto.SectorFilter;
import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.example.incentivebackend.module.master.sector.service.SectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SectorController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for controller testing
class SectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SectorService sectorService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_WithValidRequest_ShouldReturn201() throws Exception {
        SectorRequest request = new SectorRequest();
        request.setSectorName("IT");
        request.setSectorShortCode("IT_SEC");
        request.setSectorStatus(StatusEnum.A);

        SectorResponse response = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("IT")
                .sectorShortCode("IT_SEC")
                .sectorStatus(StatusEnum.A)
                .build();

        when(sectorService.create(any(SectorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/master/sectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sectorId").value(1))
                .andExpect(jsonPath("$.data.sectorName").value("IT"))
                .andExpect(jsonPath("$.data.sectorShortCode").value("IT_SEC"));
    }

    @Test
    void getById_WithExistingId_ShouldReturn200() throws Exception {
        SectorResponse response = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("IT")
                .build();

        when(sectorService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/master/sectors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sectorId").value(1))
                .andExpect(jsonPath("$.data.sectorName").value("IT"));
    }

    @Test
    void getAll_ShouldReturn200() throws Exception {
        SectorResponse r1 = SectorResponse.builder().sectorId(1L).sectorName("IT").build();
        SectorResponse r2 = SectorResponse.builder().sectorId(2L).sectorName("HR").build();
        Page<SectorResponse> page = new PageImpl<>(List.of(r1, r2));

        when(sectorService.findAll(any(SectorFilter.class))).thenReturn(page);

        mockMvc.perform(get("/api/master/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(2))
                .andExpect(jsonPath("$.data.content[0].sectorId").value(1))
                .andExpect(jsonPath("$.data.content[1].sectorId").value(2));
    }

    @Test
    void update_WithValidRequest_ShouldReturn200() throws Exception {
        SectorRequest request = new SectorRequest();
        request.setSectorName("Updated IT");
        request.setSectorShortCode("IT_UPD");

        SectorResponse response = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("Updated IT")
                .build();

        when(sectorService.update(eq(1L), any(SectorRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/master/sectors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sectorName").value("Updated IT"));
    }

    @Test
    void softDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(sectorService).softDelete(1L);

        mockMvc.perform(delete("/api/master/sectors/1"))
                .andExpect(status().isOk()); // ApiResponse OK

        verify(sectorService).softDelete(1L);
    }

    @Test
    void hardDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(sectorService).hardDelete(1L);

        mockMvc.perform(delete("/api/master/sectors/hard/1"))
                .andExpect(status().isOk()); // ApiResponse OK

        verify(sectorService).hardDelete(1L);
    }

    @Test
    void lookup_ShouldReturnDropdownDTOs() throws Exception {
        SectorResponse r1 = SectorResponse.builder().sectorId(1L).sectorName("IT").build();
        SectorResponse r2 = SectorResponse.builder().sectorId(2L).sectorName("HR").build();

        when(sectorService.findActive()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/master/sectors/lookup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].value").value(1))
                .andExpect(jsonPath("$.data[0].label").value("[1] IT"))
                .andExpect(jsonPath("$.data[1].value").value(2))
                .andExpect(jsonPath("$.data[1].label").value("[2] HR"));
    }
}
