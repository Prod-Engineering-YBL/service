package ro.unibuc.prodeng.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import ro.unibuc.prodeng.repository.ExpenseRepository;
import ro.unibuc.prodeng.request.CreateExpenseRequest;

import ro.unibuc.prodeng.model.ExpenseEntity;

import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseEntity expense1;
    private ExpenseEntity expense2;


        
    @BeforeEach
    void setUp() {
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
        void shouldReturnAllExpenses() {
        when(expenseRepository.findAll()).thenReturn(List.of(expense1, expense2));

        var result = expenseService.getAllExpenses();

        assertEquals(2, result.size());
        verify(expenseRepository).findAll();
    }


    @Test
    void shouldReturnExpenseById() {
        when(expenseRepository.findById("1")).thenReturn(Optional.of(expense1));

        var result = expenseService.getExpenseById("1");

        assertEquals("1", result.id());
        verify(expenseRepository).findById("1");
    }

    @Test
    void shouldCreateExpense() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                100f,
                LocalDateTime.of(2026, 3, 20, 12, 0),
                "Food",
                "user1",
                "cat1"
        );

        when(expenseRepository.save(any())).thenReturn(expense1);

        var result = expenseService.createExpense(request);

        assertEquals(100f, result.amount());
        verify(expenseRepository).save(any());
    }

    @Test
    void shouldReturnLargestExpense() {
        when(expenseRepository.findByAssignedUserId("user1"))
                .thenReturn(List.of(expense1, expense2));

        var result = expenseService.getLargestExpense("user1");

        assertEquals("2", result.id());
    }

    @Test
    void shouldReturnTotalsByCategory() {
        when(expenseRepository.findByAssignedUserId("user1"))
                .thenReturn(List.of(expense1, expense2));

        var result = expenseService.getTotalsByCategory("user1");

        assertEquals(100.0, result.get("cat1"));
        assertEquals(200.0, result.get("cat2"));
    }

    @Test
    void shouldCalculateMonthlyTotal() {
        when(expenseRepository.findByAssignedUserIdAndDateBetween(
                eq("user1"), any(), any()))
                .thenReturn(List.of(expense1, expense2));

        Float result = expenseService.getMonthlyTotal("user1", 2026, 3);

        assertEquals(300f, result);
    }
    
    @Test
    void shouldDeleteExpense() {
        when(expenseRepository.existsById("1")).thenReturn(true);

        expenseService.deleteExpense("1");

        verify(expenseRepository).deleteById("1");
    }

    @Test
    void shouldThrowWhenAmountInvalid() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                0f,
                LocalDateTime.now(),
                "Test",
                "user1",
                "cat1"
        );

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(request));
    }


    @Test
    void shouldReturnAllExpensesByUser() throws Exception{
        
    }


    
}
