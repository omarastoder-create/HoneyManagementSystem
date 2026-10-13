package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.request.SaleTransactionRequest;
import Rastoder.HoneyManagementSystem.dto.response.SaleTransactionResponse;
import Rastoder.HoneyManagementSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<List<SaleTransactionResponse>> getAllTransactions() {
        List<SaleTransactionResponse> responses = transactionService.getAllTransactions();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleTransactionResponse> getTransactionById(@PathVariable UUID id) {
        SaleTransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
}