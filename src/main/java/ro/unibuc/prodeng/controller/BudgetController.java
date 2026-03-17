package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;
import ro.unibuc.prodeng.service.BudgetService;

@RestController
@RequestMapping("api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(@PathVariable @NonNull String id) {
        return budgetService.getBudgetById(id);
    }

    @GetMapping("/user/{userId}")
    public List<BudgetResponse> getBudgetsByUserId(@PathVariable @NonNull String userId) {
        return budgetService.getBudgetsByUserId(userId);
    }

    @PostMapping
    public BudgetResponse createBudget(@RequestBody CreateBudgetRequest request) {
        return budgetService.createBudget(request);
    }

    @PutMapping("/{id}")
    public BudgetResponse updateBudget(@PathVariable @NonNull String id, @RequestBody CreateBudgetRequest request) {
        return budgetService.updateBudget(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable @NonNull String id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
