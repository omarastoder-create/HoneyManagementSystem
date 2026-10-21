// src/main/java/Rastoder/HoneyManagementSystem/controller/ExpenseController.java
package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.request.ExpenseRequest;
import Rastoder.HoneyManagementSystem.dto.response.ExpenseResponse;
import Rastoder.HoneyManagementSystem.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    // Upload or replace the receipt (PDF or photo) of an expense
    @PutMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> uploadReceipt(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(expenseService.attachReceipt(id, file));
    }

    @DeleteMapping("/{id}/receipt")
    public ResponseEntity<ExpenseResponse> deleteReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseService.removeReceipt(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable UUID id, @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}