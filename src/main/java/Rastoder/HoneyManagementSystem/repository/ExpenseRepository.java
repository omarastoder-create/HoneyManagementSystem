package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findAllByOrderByExpenseDateDesc();
}