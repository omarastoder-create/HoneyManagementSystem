package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.response.ExpenseResponse;
import Rastoder.HoneyManagementSystem.service.ExpenseService;
import Rastoder.HoneyManagementSystem.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests: URLs, status codes, multipart handling and the error format
 * of the expense endpoints (the service itself is covered by ExpenseServiceTest).
 */
@WebMvcTest(ExpenseController.class)
@Import(FileStorageService.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    private static ExpenseResponse expense(UUID id, String receiptUrl) {
        return new ExpenseResponse(id, "Zuckersirup", "Imkerbedarf", LocalDate.of(2026, 10, 3), new BigDecimal("42.90"), receiptUrl);
    }

    @Test
    @DisplayName("POST /expenses: Should return 201 Created")
    void createExpense_ShouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();
        when(expenseService.createExpense(any())).thenReturn(expense(id, null));

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Zuckersirup","supplierName":"Imkerbedarf","expenseDate":"2026-10-03","amount":42.90}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expenseId").value(id.toString()));
    }

    @Test
    @DisplayName("POST /expenses: Invalid body should return 400 with field errors")
    void createExpense_WithInvalidBody_ShouldReturnFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","expenseDate":"2026-10-03","amount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    @DisplayName("POST /expenses: Malformed JSON should return 400")
    void createExpense_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    @DisplayName("PUT /expenses/{id}/receipt: Should pass the uploaded file to the service")
    void uploadReceipt_ShouldReturnUpdatedExpense() throws Exception {
        UUID id = UUID.randomUUID();
        var file = new MockMultipartFile("file", "rechnung.pdf", "application/pdf", "%PDF-1.7".getBytes());
        when(expenseService.attachReceipt(eq(id), any())).thenReturn(expense(id, "/receipts/abc.pdf"));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/expenses/{id}/receipt", id).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptUrl").value("/receipts/abc.pdf"));

        verify(expenseService).attachReceipt(eq(id), any());
    }

    @Test
    @DisplayName("DELETE /expenses/{id}/receipt: Should return the expense without receipt")
    void deleteReceipt_ShouldReturnUpdatedExpense() throws Exception {
        UUID id = UUID.randomUUID();
        when(expenseService.removeReceipt(id)).thenReturn(expense(id, null));

        mockMvc.perform(delete("/api/v1/expenses/{id}/receipt", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenseId").value(id.toString()));
    }

    @Test
    @DisplayName("DELETE /expenses/{id}/receipt: An invalid id should return 400 instead of 500")
    void deleteReceipt_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/expenses/{id}/receipt", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }
}
