package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.request.SaleTransactionRequest;
import Rastoder.HoneyManagementSystem.dto.response.SaleTransactionResponse;
import Rastoder.HoneyManagementSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<SaleTransactionResponse> createTransaction(
            @Valid @RequestBody SaleTransactionRequest request) {

        SaleTransactionResponse response = transactionService.createSaleTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}