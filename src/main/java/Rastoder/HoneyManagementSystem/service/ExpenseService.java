// src/main/java/Rastoder/HoneyManagementSystem/service/ExpenseService.java
package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ExpenseRequest;
import Rastoder.HoneyManagementSystem.dto.response.ExpenseResponse;
import Rastoder.HoneyManagementSystem.model.Expense;
import Rastoder.HoneyManagementSystem.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id) {
        return mapToResponse(findEntityOrThrow(id));
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        updateEntityFromRequest(expense, request);
        return mapToResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID id, ExpenseRequest request) {
        Expense existing = findEntityOrThrow(id);
        updateEntityFromRequest(existing, request);
        return mapToResponse(expenseRepository.save(existing));
    }

    @Transactional
    public void deleteExpense(UUID id) {
        Expense expense = findEntityOrThrow(id);
        expenseRepository.delete(expense);
        fileStorageService.deleteAfterCommit(expense.getReceiptUrl());
    }

    // Uploads (or replaces) the receipt of an expense.
    @Transactional
    public ExpenseResponse attachReceipt(UUID id, MultipartFile file) {
        Expense expense = findEntityOrThrow(id);

        String newReceiptUrl = fileStorageService.saveReceipt(file);
        fileStorageService.deleteOnRollback(newReceiptUrl);

        String oldReceiptUrl = expense.getReceiptUrl();
        expense.setReceiptUrl(newReceiptUrl);
        fileStorageService.deleteAfterCommit(oldReceiptUrl);

        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse removeReceipt(UUID id) {
        Expense expense = findEntityOrThrow(id);
        String oldReceiptUrl = expense.getReceiptUrl();
        expense.setReceiptUrl(null);
        fileStorageService.deleteAfterCommit(oldReceiptUrl);
        return mapToResponse(expense);
    }

    private Expense findEntityOrThrow(UUID id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ausgabe mit ID " + id + " nicht gefunden."));
    }

    private ExpenseResponse mapToResponse(Expense entity) {
        return new ExpenseResponse(
                entity.getExpenseId(),
                entity.getTitle(),
                entity.getSupplierName(),
                entity.getExpenseDate(),
                entity.getAmount(),
                entity.getReceiptUrl()
        );
    }

    private void updateEntityFromRequest(Expense entity, ExpenseRequest request) {
        if (request.title() != null) entity.setTitle(request.title());
        if (request.supplierName() != null) entity.setSupplierName(request.supplierName());
        if (request.expenseDate() != null) entity.setExpenseDate(request.expenseDate());
        if (request.amount() != null) entity.setAmount(request.amount());
    }
}