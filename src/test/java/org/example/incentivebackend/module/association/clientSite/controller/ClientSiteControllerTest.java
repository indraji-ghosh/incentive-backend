package org.example.incentivebackend.module.association.clientSite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteRequest;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;
import org.example.incentivebackend.module.association.clientSite.service.ClientSiteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClientSiteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientSiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ClientSiteService clientSiteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void assign_WithValidRequest_ShouldReturn201() throws Exception {
        ClientSiteRequest request = new ClientSiteRequest();
        request.setClientId(1L);
        request.setSiteIds(List.of(2L));

        ClientSiteResponse response = ClientSiteResponse.builder()
                .clientSiteId(10L)
                .clientId(1L)
                .siteId(2L)
                .build();

        when(clientSiteService.assign(any(ClientSiteRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/api/association/client-sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].clientSiteId").value(10))
                .andExpect(jsonPath("$.data[0].clientId").value(1))
                .andExpect(jsonPath("$.data[0].siteId").value(2));
    }

    @Test
    void getByClient_WithExistingId_ShouldReturn200() throws Exception {
        ClientSiteResponse response = ClientSiteResponse.builder()
                .clientSiteId(10L)
                .clientId(1L)
                .siteId(2L)
                .build();

        when(clientSiteService.findByClientId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/association/client-sites/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].clientId").value(1))
                .andExpect(jsonPath("$.data[0].siteId").value(2));
    }

    @Test
    void getBySite_WithExistingId_ShouldReturn200() throws Exception {
        ClientSiteResponse response = ClientSiteResponse.builder()
                .clientSiteId(10L)
                .clientId(1L)
                .siteId(2L)
                .build();

        when(clientSiteService.findBySiteId(2L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/association/client-sites/site/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].clientId").value(1))
                .andExpect(jsonPath("$.data[0].siteId").value(2));
    }

    @Test
    void remove_WithExistingIds_ShouldReturn200() throws Exception {
        doNothing().when(clientSiteService).remove(1L, 2L);

        mockMvc.perform(delete("/api/association/client-sites/client/1/site/2"))
                .andExpect(status().isOk());

        verify(clientSiteService).remove(1L, 2L);
    }
}
