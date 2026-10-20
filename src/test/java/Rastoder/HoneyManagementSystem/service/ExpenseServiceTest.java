package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ExpenseRequest;
import Rastoder.HoneyManagementSystem.dto.response.ExpenseResponse;
import Rastoder.HoneyManagementSystem.model.Expense;
import Rastoder.HoneyManagementSystem.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    @DisplayName("getAllExpenses: Should return ordered list of ExpenseResponse")
    void getAllExpenses_ShouldReturnOrderedList() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        var expense = new Expense();
        expense.setExpenseId(id);
        expense.setTitle("Wachs");
        expense.setSupplierName("Imkerbedarf");
        expense.setExpenseDate(date);
        expense.setAmount(new BigDecimal("49.99"));
        expense.setReceiptImagePath("path/to/receipt.png");

        when(expenseRepository.findAllByOrderByExpenseDateDesc()).thenReturn(List.of(expense));

        // When
        List<ExpenseResponse> result = expenseService.getAllExpenses();

        // Then
        verify(expenseRepository).findAllByOrderByExpenseDateDesc();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(new ExpenseResponse(
                id, "Wachs", "Imkerbedarf", date, new BigDecimal("49.99"), "path/to/receipt.png"
        ));
    }

    @Test
    @DisplayName("getExpenseById: When found, should return ExpenseResponse")
    void getExpenseById_WhenFound_ShouldReturnResponse() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        var expense = new Expense();
        expense.setExpenseId(id);
        expense.setTitle("Zucker");

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        // When
        ExpenseResponse response = expenseService.getExpenseById(id);

        // Then
        verify(expenseRepository).findById(id);
        assertThat(response.title()).isEqualTo("Zucker");
    }

    @Test
    @DisplayName("getExpenseById: When not found, should throw EntityNotFoundException")
    void getExpenseById_WhenNotFound_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        when(expenseRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> expenseService.getExpenseById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ausgabe mit ID " + id + " nicht gefunden.");
    }

    @Test
    @DisplayName("createExpense: Should persist and return ExpenseResponse")
    void createExpense_ShouldSaveAndReturnResponse() {
        // Given
        LocalDate date = LocalDate.now();
        var request = new ExpenseRequest("Gläser", "Imker-Shop", date, new BigDecimal("120.00"), "img.png");
        UUID id = UUID.randomUUID();

        var savedExpense = new Expense();
        savedExpense.setExpenseId(id);
        savedExpense.setTitle(request.title());
        savedExpense.setSupplierName(request.supplierName());
        savedExpense.setExpenseDate(request.expenseDate());
        savedExpense.setAmount(request.amount());
        savedExpense.setReceiptImagePath(request.receiptImagePath());

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(request);

        // Then
        verify(expenseRepository).save(any(Expense.class));
        assertThat(response.title()).isEqualTo("Gläser");
        assertThat(response.amount()).isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("updateExpense: When found, should update and return ExpenseResponse")
    void updateExpense_WhenFound_ShouldUpdateAndReturnResponse() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        var existingExpense = new Expense();
        existingExpense.setExpenseId(id);
        existingExpense.setTitle("Old Title");

        var request = new ExpenseRequest("New Title", "Supplier", date, new BigDecimal("50.00"), "new.png");

        when(expenseRepository.findById(id)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existingExpense);

        // When
        ExpenseResponse response = expenseService.updateExpense(id, request);

        // Then
        verify(expenseRepository).findById(id);
        verify(expenseRepository).save(existingExpense);
        assertThat(response.title()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("updateExpense: When not found, should throw EntityNotFoundException")
    void updateExpense_WhenNotFound_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        var request = new ExpenseRequest("Title", "Supplier", LocalDate.now(), BigDecimal.TEN, "path");
        when(expenseRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> expenseService.updateExpense(id, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("deleteExpense: When found, should delete entity")
    void deleteExpense_WhenFound_ShouldDelete() {
        // Given
        UUID id = UUID.randomUUID();
        when(expenseRepository.existsById(id)).thenReturn(true);
        doNothing().when(expenseRepository).deleteById(id);

        // When
        expenseService.deleteExpense(id);

        // Then
        verify(expenseRepository).existsById(id);
        verify(expenseRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteExpense: When not found, should throw EntityNotFoundException")
    void deleteExpense_WhenNotFound_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        when(expenseRepository.existsById(id)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> expenseService.deleteExpense(id))
                .isInstanceOf(EntityNotFoundException.class);
        verify(expenseRepository, never()).deleteById(any());
    }
    @Test
    @DisplayName("updateExpense: When request fields are null, should retain existing entity values (false-branch coverage)")
    void updateExpense_WithNullFields_ShouldRetainExistingValues() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate originalDate = LocalDate.of(2025, 1, 1);
        var existingExpense = new Expense();
        existingExpense.setExpenseId(id);
        existingExpense.setTitle("Old Title");
        existingExpense.setSupplierName("Old Supplier");
        existingExpense.setExpenseDate(originalDate);
        existingExpense.setAmount(new BigDecimal("10.00"));
        existingExpense.setReceiptImagePath("old.png");

        var request = new ExpenseRequest(null, null, null, null, null);

        when(expenseRepository.findById(id)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existingExpense);

        // When
        ExpenseResponse response = expenseService.updateExpense(id, request);

        // Then
        verify(expenseRepository).save(existingExpense);
        assertThat(response.title()).isEqualTo("Old Title");
        assertThat(response.supplierName()).isEqualTo("Old Supplier");
        assertThat(response.amount()).isEqualTo(new BigDecimal("10.00"));
    }
}