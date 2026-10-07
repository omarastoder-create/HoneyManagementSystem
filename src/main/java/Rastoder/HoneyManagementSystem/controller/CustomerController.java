package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.request.CustomerRequest;
import Rastoder.HoneyManagementSystem.dto.response.CustomerResponse;
import Rastoder.HoneyManagementSystem.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request){
        CustomerResponse response = customerService.createCustomer(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.customerId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(){
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
