package org.example.incentivebackend.module.master.businessHead.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadRequest;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadResponse;
import org.example.incentivebackend.module.master.businessHead.service.BusinessHeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BusinessHeadController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for controller testing
class BusinessHeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BusinessHeadService businessHeadService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_WithValidRequest_ShouldReturn201() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");
        request.setHeadShortCode("FIN");
        request.setHeadStatus(StatusEnum.A);

        BusinessHeadResponse response = BusinessHeadResponse.builder()
                .headId(1L)
                .headName("Finance")
                .headShortCode("FIN")
                .headStatus(StatusEnum.A)
                .build();

        when(businessHeadService.create(any(BusinessHeadRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.headId").value(1))
                .andExpect(jsonPath("$.data.headName").value("Finance"))
                .andExpect(jsonPath("$.data.headShortCode").value("FIN"))
                .andExpect(jsonPath("$.data.headStatus").value("A"));
    }

    @Test
    void create_WithMissingHeadName_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadShortCode("FIN"); // Missing name

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(businessHeadService, never()).create(any());
    }

    @Test
    void create_WithBlankHeadName_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("   "); // Blank name
        request.setHeadShortCode("FIN");

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithHeadNameTooLong_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("A".repeat(101)); // > 100 characters
        request.setHeadShortCode("FIN");

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithMissingHeadShortCode_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithBlankHeadShortCode_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");
        request.setHeadShortCode("");

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithHeadShortCodeTooLong_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");
        request.setHeadShortCode("F".repeat(51)); // > 50 characters

        mockMvc.perform(post("/api/master/business-heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_WithExistingId_ShouldReturn200() throws Exception {
        BusinessHeadResponse response = BusinessHeadResponse.builder()
                .headId(1L)
                .headName("Finance")
                .build();

        when(businessHeadService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/master/business-heads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headId").value(1))
                .andExpect(jsonPath("$.data.headName").value("Finance"));
    }

    @Test
    void getById_WithMissingId_ShouldReturn500() throws Exception {
        when(businessHeadService.getById(1L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/master/business-heads/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAll_ShouldReturn200() throws Exception {
        BusinessHeadResponse r1 = BusinessHeadResponse.builder().headId(1L).headName("Fin").build();
        BusinessHeadResponse r2 = BusinessHeadResponse.builder().headId(2L).headName("Ops").build();

        when(businessHeadService.getAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/master/business-heads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].headId").value(1))
                .andExpect(jsonPath("$.data[1].headId").value(2));
    }

    @Test
    void update_WithValidRequest_ShouldReturn200() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Updated Finance");
        request.setHeadShortCode("FIN");

        BusinessHeadResponse response = BusinessHeadResponse.builder()
                .headId(1L)
                .headName("Updated Finance")
                .build();

        when(businessHeadService.update(eq(1L), any(BusinessHeadRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/master/business-heads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headName").value("Updated Finance"));
    }

    @Test
    void update_WithInvalidRequest_ShouldReturn400() throws Exception {
        BusinessHeadRequest request = new BusinessHeadRequest(); // missing required fields

        mockMvc.perform(put("/api/master/business-heads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_WithExistingId_ShouldReturn204() throws Exception {
        doNothing().when(businessHeadService).delete(1L);

        mockMvc.perform(delete("/api/master/business-heads/1"))
                .andExpect(status().isOk());

        verify(businessHeadService).delete(1L);
    }

    @Test
    void delete_WithMissingId_ShouldReturn500() throws Exception {
        doThrow(new RuntimeException("Not found")).when(businessHeadService).delete(1L);

        mockMvc.perform(delete("/api/master/business-heads/1"))
                .andExpect(status().isInternalServerError());
    }
}
