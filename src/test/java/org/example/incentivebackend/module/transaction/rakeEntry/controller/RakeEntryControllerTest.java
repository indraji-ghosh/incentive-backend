package org.example.incentivebackend.module.transaction.rakeEntry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeAnnexureRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.service.RakeEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RakeEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class RakeEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RakeEntryService rakeEntryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_WithValidRequest_ShouldReturn201() throws Exception {
        String requestJson = """
                {
                    "workingMonth": "2023-10-01",
                    "clientId": 1,
                    "partyId": 2,
                    "annexures": [
                        {
                            "rrNo": "RR123",
                            "rrDate": "2023-10-01"
                        }
                    ]
                }
                """;

        RakeEntryResponse response = RakeEntryResponse.builder()
                .rakeEntryId(10L)
                .clientId(1L)
                .partyId(2L)
                .build();

        when(rakeEntryService.create(any(RakeEntryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transaction/rake-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rakeEntryId").value(10))
                .andExpect(jsonPath("$.data.clientId").value(1))
                .andExpect(jsonPath("$.data.partyId").value(2));
    }

    @Test
    void create_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        String requestJson = "{}";
        
        mockMvc.perform(post("/api/transaction/rake-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
                
        verify(rakeEntryService, never()).create(any());
    }

    @Test
    void findById_WithExistingId_ShouldReturn200() throws Exception {
        RakeEntryResponse response = RakeEntryResponse.builder()
                .rakeEntryId(10L)
                .build();

        when(rakeEntryService.findById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/transaction/rake-entries/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rakeEntryId").value(10));
    }

    @Test
    void update_WithValidRequest_ShouldReturn200() throws Exception {
        String requestJson = """
                {
                    "workingMonth": "2023-10-01",
                    "clientId": 1,
                    "partyId": 2,
                    "annexures": [
                        {
                            "rrNo": "RR123",
                            "rrDate": "2023-10-01"
                        }
                    ]
                }
                """;

        RakeEntryResponse response = RakeEntryResponse.builder()
                .rakeEntryId(10L)
                .build();

        when(rakeEntryService.update(eq(10L), any(RakeEntryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/transaction/rake-entries/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rakeEntryId").value(10));
    }

    @Test
    void softDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(rakeEntryService).softDelete(10L);

        mockMvc.perform(delete("/api/transaction/rake-entries/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(rakeEntryService).softDelete(10L);
    }

    @Test
    void hardDelete_WithExistingId_ShouldReturn200() throws Exception {
        doNothing().when(rakeEntryService).hardDelete(10L);

        mockMvc.perform(delete("/api/transaction/rake-entries/hard/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(rakeEntryService).hardDelete(10L);
    }
}
