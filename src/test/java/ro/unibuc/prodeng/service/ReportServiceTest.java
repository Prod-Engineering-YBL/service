package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.ExpenseEntity;
import ro.unibuc.prodeng.model.ReportEntity;
import ro.unibuc.prodeng.repository.ExpenseRepository;
import ro.unibuc.prodeng.repository.ReportRepository;
import ro.unibuc.prodeng.response.ReportResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ReportService reportService;

    private ReportEntity report1;
    private ExpenseEntity expense1;
    private ExpenseEntity expense2;

    @BeforeEach
    void setUp() {
        report1 = new ReportEntity(
                "r1",
                "user1",
                2026,
                3,
                300f,
                Map.of("cat1", 100.0, "cat2", 200.0)
        );

        expense1 = new ExpenseEntity(
                "1",
                100f,
                LocalDateTime.of(2026, 3, 20, 12, 0),
                "Food",
                "user1",
                "cat1"
        );

        expense2 = new ExpenseEntity(
                "2",
                200f,
                LocalDateTime.of(2026, 3, 21, 12, 0),
                "Drinks",
                "user1",
                "cat2"
        );
    }

    @Test
    void shouldReturnReportById() {
        when(reportRepository.findById("r1")).thenReturn(Optional.of(report1));

        ReportResponse result = reportService.getReportById("r1");

        assertEquals("r1", result.id());
        assertEquals("user1", result.assignedUserId());
        assertEquals(2026, result.year());
        assertEquals(3, result.month());
        assertEquals(300f, result.totalAmount());
        assertEquals(100.0, result.categoryBreakdown().get("cat1"));
    }

    @Test
    void shouldThrowWhenDeletingNonExistingReport() {
        when(reportRepository.existsById("missing")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> reportService.deleteReport("missing"));
    }

    @Test
    void shouldGenerateReport() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        when(expenseRepository.findByAssignedUserIdAndDateBetween("user1", start, end))
                .thenReturn(List.of(expense1, expense2));
        when(reportRepository.save(any(ReportEntity.class))).thenReturn(report1);

        ReportResponse result = reportService.generateReport("user1", 2026, 3);

        assertEquals("r1", result.id());
        assertEquals("user1", result.assignedUserId());
        assertEquals(300f, result.totalAmount());
        assertEquals(100.0, result.categoryBreakdown().get("cat1"));
        assertEquals(200.0, result.categoryBreakdown().get("cat2"));

        verify(expenseRepository).findByAssignedUserIdAndDateBetween("user1", start, end);
        verify(reportRepository).save(any(ReportEntity.class));
    }


    @Test
    void shouldThrowWhenGenerateReportWithNoExpenses() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        when(expenseRepository.findByAssignedUserIdAndDateBetween("user1", start, end))
                .thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> reportService.generateReport("user1", 2026, 3));
    }

    @Test
    void shouldDeleteReportById() {
        when(reportRepository.existsById("r1")).thenReturn(true);

        reportService.deleteReport("r1");

        verify(reportRepository).deleteById("r1");
    }

    @Test
    void shouldThrowWhenReportNotFoundById() {
        when(reportRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reportService.getReportById("missing"));
    }

    @Test
    void shouldReturnReportsByUser() {
        when(reportRepository.findByAssignedUserId("user1")).thenReturn(List.of(report1));

        var result = reportService.getReportsByUser("user1");

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).id());
        assertEquals(300f, result.get(0).totalAmount());
    }




}