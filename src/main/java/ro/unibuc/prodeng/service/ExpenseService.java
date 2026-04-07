package ro.unibuc.prodeng.service;
import java.util.*;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.ExpenseEntity;
import ro.unibuc.prodeng.repository.ExpenseRepository;
import ro.unibuc.prodeng.request.CreateExpenseRequest;
import ro.unibuc.prodeng.request.UpdateExpenseRequest;
import ro.unibuc.prodeng.response.ExpenseResponse;

@Service
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    //testat
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    //testat
    public ExpenseResponse getExpenseById(@NonNull String id) {
        var expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        return toResponse(expense);
    }

    //testat
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        if (request.amount() == null || request.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        ExpenseEntity expense = new ExpenseEntity(null, request.amount(), request.date(), request.description(), request.assignedUserId(), request.assignedCategoryId());

        var savedExpense = expenseRepository.save(expense);
        
        return toResponse(savedExpense);

    }

    //de testat
    public ExpenseResponse updateExpense(@NonNull String id, UpdateExpenseRequest request) {

        var existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        var updated = new ExpenseEntity(
                existing.id(),
                existing.amount(),
                existing.date(),
                request.description(),
                existing.assignedUserId(),
                existing.assignedCategoryId()
        );
        
        return toResponse(expenseRepository.save(updated));

    }

    //de testat
    public void deleteExpense(@NonNull String id) {

        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found");
        }

        expenseRepository.deleteById(id);
    }

    //de testat
    public List<ExpenseResponse> getExpensesByUser(@NonNull String userId) {
        return expenseRepository.findByAssignedUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    //de testat
    public List<ExpenseResponse> getExpensesByCategory(@NonNull String categoryId) {
    return expenseRepository.findByAssignedCategoryId(categoryId)
            .stream()
            .map(this::toResponse)
            .toList();

    }

    //de testat
    public Float getMonthlyTotal(@NonNull String userId, int year, int month){

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

            return expenseRepository
            .findByAssignedUserIdAndDateBetween(userId, start, end)
            .stream()
            .map(ExpenseEntity::amount)
            .reduce(0f, Float::sum);
    }

    public Float getYearlyTotal(@NonNull String userId, int year) {

        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = start.plusYears(1);

        return expenseRepository
                .findByAssignedUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(ExpenseEntity::amount)
                .reduce(0f, Float::sum);
    }

    public Float getTotalByCategory(@NonNull String categoryId) {

        return expenseRepository
                .findByAssignedCategoryId(categoryId)
                .stream()
                .map(ExpenseEntity::amount)
                .reduce(0f, Float::sum);
    }

    public Float getMonthlyCategoryTotal(String categoryId, int year, int month) {

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        return expenseRepository
                .findByAssignedCategoryId(categoryId)
                .stream()
                .filter(e -> !e.date().isBefore(start) && e.date().isBefore(end))
                .map(ExpenseEntity::amount)
                .reduce(0f, Float::sum);
    }

    public Map<String, Double> getTotalsByCategory(String userId) {

        return expenseRepository.findByAssignedUserId(userId)
                .stream()
                .collect(Collectors.groupingBy(
                        ExpenseEntity::assignedCategoryId,
                        Collectors.summingDouble(ExpenseEntity::amount)
                ));
    }


    public List<ExpenseResponse> getLastNExpenses(String userId, int n) {

        return expenseRepository.findByAssignedUserId(userId)
                .stream()
                .sorted(Comparator.comparing(ExpenseEntity::date).reversed())
                .limit(n)
                .map(this::toResponse)
                .toList();
    }


    //testat
    public ExpenseResponse getLargestExpense(String userId) {

        ExpenseEntity expense = expenseRepository.findByAssignedUserId(userId)
                .stream()
                .max(Comparator.comparing(ExpenseEntity::amount))
                .orElseThrow(() -> new RuntimeException("No expenses found"));

        return toResponse(expense);
    }

    



    




    







    private ExpenseResponse toResponse(ExpenseEntity expense){
        return new ExpenseResponse(
            expense.id(),
            expense.amount(),
            expense.date(),
            expense.description(),
            expense.assignedUserId(),
            expense.assignedCategoryId()
        );


    }



}