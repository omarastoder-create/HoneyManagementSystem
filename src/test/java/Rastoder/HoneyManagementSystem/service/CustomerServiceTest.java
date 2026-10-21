package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.CustomerRequest;
import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.model.Customer;
import Rastoder.HoneyManagementSystem.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        var request = new CustomerRequest("Omar", "Rastoder", "VIP Beekeeper", "+352600000000", "omar@example.com");
        UUID generatedId = UUID.randomUUID();

        var savedCustomer = Customer.builder()
                .customerId(generatedId)
                .name("Omar")
                .familyName("Rastoder")
                .description("VIP Beekeeper")
                .phoneNumber("+352600000000")
                .email("omar@example.com")
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
        assertThat(capturedEntity.getEmail()).isEqualTo("omar@example.com");

        assertThat(response).isEqualTo(new CustomerResponse(
                generatedId, "Omar", "Rastoder", "VIP Beekeeper", "+352600000000", "omar@example.com"
        ));
    }

    @Test
    @DisplayName("createCustomer: Blank email should be stored as null so the unique constraint allows many customers without email")
    void createCustomer_WithBlankEmail_ShouldStoreNull() {
        // Given
        var request = new CustomerRequest("Max", "Mustermann", "", "+352600000009", "  ");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        assertThat(response.email()).isNull();
        assertThat(response.description()).isNull();
    }

    @Test
    @DisplayName("updateCustomer: Should replace all editable fields of the existing customer")
    void updateCustomer_ShouldUpdateAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        var existing = Customer.builder()
                .customerId(id)
                .name("Omar")
                .familyName("Rastoder")
                .description("Old")
                .phoneNumber("+352600000000")
                .build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        var request = new CustomerRequest("Omar", "Rastoder", "Stammkunde", "+352600000111", "omar@example.com");

        // When
        CustomerResponse response = customerService.updateCustomer(id, request);

        // Then
        assertThat(existing.getDescription()).isEqualTo("Stammkunde");
        assertThat(existing.getPhoneNumber()).isEqualTo("+352600000111");
        assertThat(response).isEqualTo(new CustomerResponse(
                id, "Omar", "Rastoder", "Stammkunde", "+352600000111", "omar@example.com"
        ));
    }

    @Test
    @DisplayName("updateCustomer: When customer does not exist, should throw EntityNotFoundException")
    void updateCustomer_WhenNotFound_ShouldThrow() {
        // Given
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        var request = new CustomerRequest("A", "B", null, "+352600000000", null);

        // When / Then
        assertThatThrownBy(() -> customerService.updateCustomer(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Customer not found");
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
                        new CustomerResponse(id1, "Omar", "Rastoder", "Regular buyer", "+352600000001", null),
                        new CustomerResponse(id2, "Max", "Mustermann", "Wholesale", "+352600000002", null)
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