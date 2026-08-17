package org.example.incentivebackend.module.master.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.example.incentivebackend.module.master.unit.service.UnitService;
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

@WebMvcTest(controllers = UnitController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UnitService unitService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_ShouldReturn201() throws Exception {
        UnitCreateRequest request = new UnitCreateRequest();
        request.setCode("TEST");
        request.setName("Test Name");

        UnitResponse response = UnitResponse.builder().id(1L).code("TEST").build();

        when(unitService.create(any(UnitCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/master/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        UnitResponse response = UnitResponse.builder().id(1L).build();
        Page<UnitResponse> page = new PageImpl<>(List.of(response));

        when(unitService.findAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/master/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(1));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        UnitResponse response = UnitResponse.builder().id(1L).build();
        when(unitService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/master/units/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void update_ShouldReturn200() throws Exception {
        UnitUpdateRequest request = new UnitUpdateRequest();
        request.setName("Test");

        UnitResponse response = UnitResponse.builder().id(1L).build();
        when(unitService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/master/units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(unitService).delete(1L);
        mockMvc.perform(delete("/api/master/units/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDropdown_ShouldReturn200() throws Exception {
        DropdownDTO dto = DropdownDTO.builder().value(1L).label("Test").build();
        when(unitService.getDropdown()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/master/units/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1));
    }
}
