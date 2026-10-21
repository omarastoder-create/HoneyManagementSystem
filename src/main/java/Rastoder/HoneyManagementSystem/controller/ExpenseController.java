// src/main/java/Rastoder/HoneyManagementSystem/controller/ExpenseController.java
package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.request.ExpenseRequest;
import Rastoder.HoneyManagementSystem.dto.response.ExpenseResponse;
import Rastoder.HoneyManagementSystem.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@CrossOrigin(origins = "http://localhost:4200")
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
        return ResponseEntity.ok(expenseService.createExpense(request));
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