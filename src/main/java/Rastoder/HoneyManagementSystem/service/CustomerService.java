package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.CustomerRequest;
import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.model.Customer;
import Rastoder.HoneyManagementSystem.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final  CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .familyName(request.familyName())
                .description(blankToNull(request.description()))
                .phoneNumber(request.phoneNumber())
                .email(blankToNull(request.email()))
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);

    }
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(){
        return customerRepository.findAll().stream()
                .map(this::mapToResponse).toList();
    }

    // PUT semantics: the request replaces all editable fields of the customer.
    // The managed entity is changed in place; Hibernate writes the changes on commit.
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        customer.updateDetails(
                request.name(),
                request.familyName(),
                blankToNull(request.description()),
                request.phoneNumber(),
                blankToNull(request.email())
        );
        return mapToResponse(customer);
    }

    // Email is unique but optional: store "no email" as NULL, because several
    // customers with an empty string "" would violate the unique constraint.
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CustomerResponse mapToResponse(Customer customer){
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getFamilyName(),
                customer.getDescription(),
                customer.getPhoneNumber(),
                customer.getEmail()
        );
    }
}
