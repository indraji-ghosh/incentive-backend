package org.example.incentivebackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEndpoint_ShouldBeAccessibleWithoutJwt() throws Exception {
        // We expect a 400 Bad Request because we're not sending a valid body,
        // but NOT a 401 Unauthorized or 403 Forbidden.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Or whatever validation returns, but NOT 401/403
    }

    @Test
    void protectedEndpoint_WithoutJwt_ShouldReturn403() throws Exception {
        // Because of the stateless nature and default config, Spring Security returns 403 Forbidden for missing/invalid auth
        mockMvc.perform(get("/api/master/business-heads"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_WithInvalidJwt_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/master/business-heads")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }
}
