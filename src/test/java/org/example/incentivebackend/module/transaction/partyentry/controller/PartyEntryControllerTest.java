package org.example.incentivebackend.module.transaction.partyentry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.example.incentivebackend.module.transaction.partyentry.service.PartyEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PartyEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PartyEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PartyEntryService partyEntryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_ShouldReturn201() throws Exception {
        PartyEntryRequest request = new PartyEntryRequest();
        request.setPartyName("Test");
        request.setBusinessHeadId(1L);
        request.setPaymentTypeId(1L);
        request.setClientIds(List.of(1L));
        request.setSiteIds(List.of(1L));
        
        org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest unitReq = new org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest();
        unitReq.setUnitId(1L);
        unitReq.setRate(java.math.BigDecimal.TEN);
        request.setUnitConfigurations(List.of(unitReq));
        
        PartyEntryResponse response = PartyEntryResponse.builder().id(1L).build();

        when(partyEntryService.create(any(PartyEntryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transaction/party-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        Page<PartyEntryResponse> page = new PageImpl<>(List.of(PartyEntryResponse.builder().id(1L).build()));

        when(partyEntryService.findAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transaction/party-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(1));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        PartyEntryResponse response = PartyEntryResponse.builder().id(1L).build();
        when(partyEntryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/transaction/party-entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void update_ShouldReturn200() throws Exception {
        PartyEntryRequest request = new PartyEntryRequest();
        request.setPartyName("Test");
        request.setBusinessHeadId(1L);
        request.setPaymentTypeId(1L);
        request.setClientIds(List.of(1L));
        request.setSiteIds(List.of(1L));

        org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest unitReq = new org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest();
        unitReq.setUnitId(1L);
        unitReq.setRate(java.math.BigDecimal.TEN);
        request.setUnitConfigurations(List.of(unitReq));

        PartyEntryResponse response = PartyEntryResponse.builder().id(1L).build();
        when(partyEntryService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/transaction/party-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(partyEntryService).delete(1L);
        mockMvc.perform(delete("/api/transaction/party-entries/1"))
                .andExpect(status().isOk());
    }
}
