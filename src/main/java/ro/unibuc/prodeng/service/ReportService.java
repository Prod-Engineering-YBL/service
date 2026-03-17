package ro.unibuc.prodeng.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mongodb.lang.NonNull;

import ro.unibuc.prodeng.model.ExpenseEntity;
import ro.unibuc.prodeng.model.ReportEntity;
import ro.unibuc.prodeng.repository.ExpenseRepository;
import ro.unibuc.prodeng.repository.ReportRepository;
import ro.unibuc.prodeng.response.ReportResponse;


@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final ReportRepository reportRepository;

    public ReportService(ExpenseRepository expenseRepository, ReportRepository reportRepository){
        this.expenseRepository= expenseRepository;
        this.reportRepository = reportRepository;
    }

    public ReportResponse getReportById(@NonNull String id) {
        var expense = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return toResponse(expense);
    }

    public List<ReportResponse> getReportsByUser(@NonNull String userId) {
        return reportRepository.findByAssignedUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public void deleteReport(@NonNull String id) {
        if (!reportRepository.existsById(id)) {
            throw new RuntimeException("Report not found");
        }
        reportRepository.deleteById(id);
    }

public ReportResponse generateReport(@NonNull String userId, int year, int month) {
        
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<ExpenseEntity> expenses = expenseRepository.findByAssignedUserIdAndDateBetween(userId, start, end);

        if (expenses.isEmpty()) {
            throw new RuntimeException("No expenses found for this period. Cannot generate report.");
        }

        float total = (float) expenses.stream()
                .mapToDouble(ExpenseEntity::amount)
                .sum();

        var breakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseEntity::assignedCategoryId,
                        Collectors.summingDouble(ExpenseEntity::amount)
                ));

        var report = new ReportEntity(
                null,
                userId,
                year,
                month,
                total,
                breakdown
        );
        
        var savedReport = reportRepository.save(report);
        

        return toResponse(savedReport);
    }



    private ReportResponse toResponse(ReportEntity entity) {
        return new ReportResponse(
                entity.id(),
                entity.assignedUserId(),
                entity.year(),
                entity.month(),
                entity.totalAmount(),
                entity.categoryBreakdown()
        );
    }






    
}
