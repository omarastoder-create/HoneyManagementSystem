package Rastoder.HoneyManagementSystem.config;

import Rastoder.HoneyManagementSystem.controller.ExpenseController;
import Rastoder.HoneyManagementSystem.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Simulates the browser's CORS "preflight" request that Angular triggers
 * before every non-simple request (PATCH, DELETE, JSON POST, ...).
 */
@WebMvcTest(ExpenseController.class)
class WebConfigCorsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    @DisplayName("Preflight from the Angular dev server should allow PATCH")
    void preflight_FromFrontendOrigin_ShouldAllowPatch() throws Exception {
        mockMvc.perform(options("/api/v1/expenses/123")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test
    @DisplayName("Preflight from an unknown origin should be rejected")
    void preflight_FromUnknownOrigin_ShouldBeForbidden() throws Exception {
        mockMvc.perform(options("/api/v1/expenses")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
