package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.model.BudgetEntity;
import ro.unibuc.prodeng.repository.BudgetRepository;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(@NonNull String id) {
        var budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        return toResponse(budget);
    }

    public List<BudgetResponse> getBudgetsByUserId(@NonNull String userId) {
        return budgetRepository.findByAssignedUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        BudgetEntity budget = new BudgetEntity(
                null,
                request.assignedUserId(),
                request.month(),
                request.amount(),
                request.currency()
        );
        BudgetEntity saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    public BudgetResponse updateBudget(@NonNull String id, CreateBudgetRequest request) {
        var existing = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        var updated = new BudgetEntity(
                existing.id(),
                request.assignedUserId(),
                request.month(),
                request.amount(),
                request.currency()
        );
        var saved = budgetRepository.save(updated);
        return toResponse(saved);
    }

    public void deleteBudget(@NonNull String id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget not found with id: " + id);
        }
        budgetRepository.deleteById(id);
    }

    private BudgetResponse toResponse(BudgetEntity budget) {
        return new BudgetResponse(
                budget.id(),
                budget.assignedUserId(),
                budget.month(),
                budget.amount(),
                budget.currency()
        );
    }

}
