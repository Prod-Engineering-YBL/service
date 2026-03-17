package ro.unibuc.prodeng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mongodb.lang.NonNull;

import ro.unibuc.prodeng.request.CreateExpenseRequest;
import ro.unibuc.prodeng.request.UpdateExpenseRequest;
import ro.unibuc.prodeng.response.ExpenseResponse;
import ro.unibuc.prodeng.service.ExpenseService;



@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {


    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public List<ExpenseResponse> getAll() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/{id}")
    public ExpenseResponse getById(@PathVariable @NonNull String id) {
        return expenseService.getExpenseById(id);
    }

    @PostMapping
    public ExpenseResponse create(@RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(request);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(
            @PathVariable @NonNull String id,
            @RequestBody UpdateExpenseRequest request) {
        return expenseService.updateExpense(id, request);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NonNull String id) {
        expenseService.deleteExpense(id);
    }

    @GetMapping("/user/{userId}")
    public List<ExpenseResponse> getByUser(@PathVariable @NonNull String userId) {
        return expenseService.getExpensesByUser(userId);
    }

    @GetMapping("/category/{categoryId}")
    public List<ExpenseResponse> getByCategory(@PathVariable @NonNull String categoryId) {
        return expenseService.getExpensesByCategory(categoryId);
    }

    @GetMapping("/user/{userId}/monthly-total")
    public Float getMonthlyTotal(
            @PathVariable @NonNull String userId,
            @RequestParam int year,
            @RequestParam int month) {
        return expenseService.getMonthlyTotal(userId, year, month);
    }

    @GetMapping("/user/{userId}/yearly-total")
    public Float getYearlyTotal(
            @PathVariable @NonNull String userId,
            @RequestParam int year) {
        return expenseService.getYearlyTotal(userId, year);
    }

    @GetMapping("/category/{categoryId}/total")
    public Float getTotalByCategory(@PathVariable @NonNull String categoryId) {
        return expenseService.getTotalByCategory(categoryId);
    }

    @GetMapping("/user/{userId}/totals-by-category")
    public Map<String, Double> getTotalsByCategory(@PathVariable @NonNull String userId) {
        return expenseService.getTotalsByCategory(userId);
    }

    @GetMapping("/user/{userId}/last")
    public List<ExpenseResponse> getLastN(
            @PathVariable @NonNull String userId,
            @RequestParam int n) {
        return expenseService.getLastNExpenses(userId, n);
    }

    @GetMapping("/user/{userId}/largest")
    public ExpenseResponse getLargest(@PathVariable @NonNull String userId) {
        return expenseService.getLargestExpense(userId);
    }

}
