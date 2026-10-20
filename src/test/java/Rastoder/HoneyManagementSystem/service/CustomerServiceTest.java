package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.CustomerRequest;
import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.model.Customer;
import Rastoder.HoneyManagementSystem.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("createCustomer: Should map request, persist entity, and return CustomerResponse")
    void createCustomer_ShouldSaveAndReturnCustomerResponse() {
        // Given
        var request = new CustomerRequest("Omar", "Rastoder", "VIP Beekeeper", "+352600000000");
        UUID generatedId = UUID.randomUUID();

        var savedCustomer = Customer.builder()
                .customerId(generatedId)
                .name("Omar")
                .familyName("Rastoder")
                .description("VIP Beekeeper")
                .phoneNumber("+352600000000")
                .build();

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        var customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer capturedEntity = customerCaptor.getValue();
        assertThat(capturedEntity.getCustomerId()).isNull();
        assertThat(capturedEntity.getName()).isEqualTo("Omar");
        assertThat(capturedEntity.getFamilyName()).isEqualTo("Rastoder");
        assertThat(capturedEntity.getDescription()).isEqualTo("VIP Beekeeper");
        assertThat(capturedEntity.getPhoneNumber()).isEqualTo("+352600000000");

        assertThat(response).isEqualTo(new CustomerResponse(
                generatedId, "Omar", "Rastoder", "VIP Beekeeper", "+352600000000"
        ));
    }

    @Test
    @DisplayName("getAllCustomers: Should return mapped list of CustomerResponse when customers exist")
    void getAllCustomers_ShouldReturnMappedCustomerList() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        var customer1 = Customer.builder()
                .customerId(id1)
                .name("Omar")
                .familyName("Rastoder")
                .description("Regular buyer")
                .phoneNumber("+352600000001")
                .build();

        var customer2 = Customer.builder()
                .customerId(id2)
                .name("Max")
                .familyName("Mustermann")
                .description("Wholesale")
                .phoneNumber("+352600000002")
                .build();

        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));

        // When
        List<CustomerResponse> result = customerService.getAllCustomers();

        // Then
        verify(customerRepository).findAll();
        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        new CustomerResponse(id1, "Omar", "Rastoder", "Regular buyer", "+352600000001"),
                        new CustomerResponse(id2, "Max", "Mustermann", "Wholesale", "+352600000002")
                );
    }

    @Test
    @DisplayName("getAllCustomers: Should return empty list when no customers exist")
    void getAllCustomers_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(customerRepository.findAll()).thenReturn(List.of());

        // When
        List<CustomerResponse> result = customerService.getAllCustomers();

        // Then
        verify(customerRepository).findAll();
        assertThat(result).isEmpty();
    }
}