package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.CustomerRequest;
import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.model.Customer;
import Rastoder.HoneyManagementSystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final  CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .familyName(request.familyName())
                .description(request.description())
                .phoneNumber(request.phoneNumber())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);

    }
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(){
        return customerRepository.findAll().stream()
                .map(this::mapToResponse).toList();
    }

    private CustomerResponse mapToResponse(Customer customer){
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getFamilyName(),
                customer.getDescription(),
                customer.getPhoneNumber()
        );
    }
}
