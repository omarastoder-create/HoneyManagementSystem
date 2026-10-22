package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.service.CustomerService;
import Rastoder.HoneyManagementSystem.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(FileStorageService.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @DisplayName("PUT /customers/{id}: Should update and return the customer")
    void updateCustomer_ShouldReturnUpdatedCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.updateCustomer(eq(id), any()))
                .thenReturn(new CustomerResponse(id, "Lena", "Weber", "Stammkundin", "+352621123456", "lena@example.com"));

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lena","familyName":"Weber","description":"Stammkundin",
                                 "phoneNumber":"+352621123456","email":"lena@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lena@example.com"));
    }

    @Test
    @DisplayName("PUT /customers/{id}: Invalid phone and email should return 400 without calling the service")
    void updateCustomer_WithInvalidData_ShouldReturnFieldErrors() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lena","familyName":"Weber","phoneNumber":"abc","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phoneNumber").value("Invalid phone number format"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Invalid email format"));

        verify(customerService, never()).updateCustomer(any(), any());
    }
}
