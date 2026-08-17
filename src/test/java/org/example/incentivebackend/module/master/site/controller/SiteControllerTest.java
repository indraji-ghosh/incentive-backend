package org.example.incentivebackend.module.master.site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.master.site.dto.SiteFilter;
import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.example.incentivebackend.module.master.site.service.SiteService;
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

@WebMvcTest(controllers = SiteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SiteService siteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_WithValidRequest_ShouldReturn201() throws Exception {
        SiteRequest request = new SiteRequest();
        request.setSiteName("Main Office");
        request.setSiteShortCode("HQ");
        request.setSiteStatus(StatusEnum.A);

        SiteResponse response = SiteResponse.builder()
                .siteId(1L)
                .siteName("Main Office")
                .siteShortCode("HQ")
                .siteStatus(StatusEnum.A)
                .build();

        when(siteService.create(any(SiteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/master/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.siteId").value(1))
                .andExpect(jsonPath("$.data.siteShortCode").value("HQ"));
    }

    @Test
    void getById_WithExistingId_ShouldReturn200() throws Exception {
        SiteResponse response = SiteResponse.builder()
                .siteId(1L)
                .siteShortCode("HQ")
                .build();

        when(siteService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/master/sites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(1))
                .andExpect(jsonPath("$.data.siteShortCode").value("HQ"));
    }

    @Test
    void getAll_ShouldReturn200() throws Exception {
        SiteResponse r1 = SiteResponse.builder().siteId(1L).siteShortCode("HQ").build();
        SiteResponse r2 = SiteResponse.builder().siteId(2L).siteShortCode("BR").build();
        Page<SiteResponse> page = new PageImpl<>(List.of(r1, r2));

        when(siteService.findAll(any(SiteFilter.class))).thenReturn(page);

        mockMvc.perform(get("/api/master/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(2))
                .andExpect(jsonPath("$.data.content[0].siteId").value(1))
                .andExpect(jsonPath("$.data.content[1].siteId").value(2));
    }

    @Test
    void update_WithValidRequest_ShouldReturn200() throws Exception {
        SiteRequest request = new SiteRequest();
        request.setSiteName("Updated HQ");
        request.setSiteShortCode("HQ_UPD");

        SiteResponse response = SiteResponse.builder()
                .siteId(1L)
                .siteShortCode("HQ_UPD")
                .build();

        when(siteService.update(eq(1L), any(SiteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/master/sites/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteShortCode").value("HQ_UPD"));
    }

    @Test
    void softDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(siteService).softDelete(1L);

        mockMvc.perform(delete("/api/master/sites/1"))
                .andExpect(status().isOk());

        verify(siteService).softDelete(1L);
    }

    @Test
    void hardDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(siteService).hardDelete(1L);

        mockMvc.perform(delete("/api/master/sites/hard/1"))
                .andExpect(status().isOk());

        verify(siteService).hardDelete(1L);
    }

    @Test
    void lookup_ShouldReturnDropdownDTOs() throws Exception {
        SiteResponse r1 = SiteResponse.builder().siteId(1L).siteShortCode("HQ").siteName("Main Office").build();
        SiteResponse r2 = SiteResponse.builder().siteId(2L).siteShortCode("BR").siteName("Branch Office").build();

        when(siteService.findActive()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/master/sites/lookup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].value").value(1))
                .andExpect(jsonPath("$.data[0].label").value("[HQ] Main Office"))
                .andExpect(jsonPath("$.data[1].value").value(2))
                .andExpect(jsonPath("$.data[1].label").value("[BR] Branch Office"));
    }
}
