package ro.unibuc.prodeng.controller;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import io.micrometer.core.instrument.Timer;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;
import ro.unibuc.prodeng.service.BudgetService;
import ro.unibuc.prodeng.service.BudgetMetricsService;

@RestController
@RequestMapping("api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetMetricsService metricsService;

    public BudgetController(BudgetService budgetService, BudgetMetricsService metricsService) {
        this.budgetService = budgetService;
        this.metricsService = metricsService;
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets() {
        return trace("getAllBudgets", budgetService::getAllBudgets);
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(@PathVariable @NonNull String id) {
        return trace("getBudgetById", () -> budgetService.getBudgetById(id));
    }

    @GetMapping("/user/{userId}")
    public List<BudgetResponse> getBudgetsByUserId(@PathVariable @NonNull String userId) {
        return trace("getBudgetsByUserId", () -> budgetService.getBudgetsByUserId(userId));
    }

    @GetMapping("/user/{userId}/total-amount")
    public double getTotalAmountByUserId(@PathVariable @NonNull String userId) {
        return trace("getTotalAmountByUserId", () -> budgetService.getTotalAmountByUserId(userId));
    }

    @GetMapping("/user/{userId}/average-amount")
    public double getAverageAmountByUserId(@PathVariable @NonNull String userId) {
        return trace("getAverageAmountByUserId", () -> budgetService.getAverageAmountByUserId(userId));
    }

    @GetMapping("/user/{userId}/highest")
    public BudgetResponse getHighestBudgetByUserId(@PathVariable @NonNull String userId) {
        return trace("getHighestBudgetByUserId", () -> budgetService.getHighestBudgetByUserId(userId));
    }

    @PostMapping
    public BudgetResponse createBudget(@RequestBody CreateBudgetRequest request) {
        return trace("createBudget", () -> {
            BudgetResponse createdBudget = budgetService.createBudget(request);
            metricsService.recordBudgetCreated();
            metricsService.incrementBudgetsInSystem();
            return createdBudget;
        });
    }

    @PutMapping("/{id}")
    public BudgetResponse updateBudget(@PathVariable @NonNull String id, @RequestBody CreateBudgetRequest request) {
        return trace("updateBudget", () -> budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable @NonNull String id) {
        traceVoid("deleteBudget", () -> {
            budgetService.deleteBudget(id);
            metricsService.decrementBudgetsInSystem();
        });
        return ResponseEntity.noContent().build();
    }

    private <T> T trace(String operation, Supplier<T> action) {
        Timer.Sample sample = metricsService.startBudgetTimer();
        metricsService.incrementActiveDbConnections();
        try {
            return action.get();
        } catch (IllegalArgumentException ex) {
            metricsService.recordBudgetError("validation");
            throw ex;
        } catch (EntityNotFoundException ex) {
            metricsService.recordBudgetError("not_found");
            throw ex;
        } catch (RuntimeException ex) {
            metricsService.recordBudgetError("runtime");
            throw ex;
        } finally {
            metricsService.stopBudgetTimer(sample, operation);
            metricsService.decrementActiveDbConnections();
        }
    }

    private void traceVoid(String operation, Runnable action) {
        Timer.Sample sample = metricsService.startBudgetTimer();
        metricsService.incrementActiveDbConnections();
        try {
            action.run();
        } catch (IllegalArgumentException ex) {
            metricsService.recordBudgetError("validation");
            throw ex;
        } catch (EntityNotFoundException ex) {
            metricsService.recordBudgetError("not_found");
            throw ex;
        } catch (RuntimeException ex) {
            metricsService.recordBudgetError("runtime");
            throw ex;
        } finally {
            metricsService.stopBudgetTimer(sample, operation);
            metricsService.decrementActiveDbConnections();
        }
    }
}
