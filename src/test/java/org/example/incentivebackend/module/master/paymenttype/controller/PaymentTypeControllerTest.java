package org.example.incentivebackend.module.master.paymenttype.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.service.CustomUserDetailsService;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.example.incentivebackend.module.master.paymenttype.service.PaymentTypeService;
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

@WebMvcTest(controllers = PaymentTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PaymentTypeService paymentTypeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_ShouldReturn201() throws Exception {
        PaymentTypeRequest request = new PaymentTypeRequest();
        request.setCode("TEST");
        request.setName("Test Name");

        PaymentTypeResponse response = PaymentTypeResponse.builder().id(1L).code("TEST").build();

        when(paymentTypeService.create(any(PaymentTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/master/payment-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        PaymentTypeResponse response = PaymentTypeResponse.builder().id(1L).build();
        Page<PaymentTypeResponse> page = new PageImpl<>(List.of(response));

        when(paymentTypeService.findAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/master/payment-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(1));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        PaymentTypeResponse response = PaymentTypeResponse.builder().id(1L).build();
        when(paymentTypeService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/master/payment-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void update_ShouldReturn200() throws Exception {
        PaymentTypeRequest request = new PaymentTypeRequest();
        request.setCode("TEST");
        request.setName("Test");

        PaymentTypeResponse response = PaymentTypeResponse.builder().id(1L).build();
        when(paymentTypeService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/master/payment-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(paymentTypeService).delete(1L);
        mockMvc.perform(delete("/api/master/payment-types/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDropdown_ShouldReturn200() throws Exception {
        DropdownDTO dto = DropdownDTO.builder().value(1L).label("Test").build();
        when(paymentTypeService.getDropdown()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/master/payment-types/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1));
    }
}
