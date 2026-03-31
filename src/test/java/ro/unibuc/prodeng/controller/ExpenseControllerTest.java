package ro.unibuc.prodeng.controller;

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

import ro.unibuc.prodeng.request.CreateExpenseRequest;
import ro.unibuc.prodeng.request.UpdateExpenseRequest;
import ro.unibuc.prodeng.response.ExpenseResponse;
import ro.unibuc.prodeng.service.ExpenseService;

import java.util.List;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(SpringExtension.class)
public class ExpenseControllerTest {
    @Mock
    private ExpenseService expenseService;
    
    @InjectMocks
    private ExpenseController expenseController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ExpenseResponse response1 = new ExpenseResponse("1", 100F, LocalDateTime.of(2026, 3, 20, 12, 0),  "Description1", "user1", "category1");
    private final ExpenseResponse response2 = new ExpenseResponse("2",100F,LocalDateTime.of(2026, 3, 21, 14, 30), "description2", "user1","category1");
    private final CreateExpenseRequest createExpenseRequest = new CreateExpenseRequest(100F,LocalDateTime.of(2026, 3, 20, 12, 0), "Description1", "category1", "user1");

    @BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();
        objectMapper.findAndRegisterModules();
	}


    @Test
    void shouldReturnAllExpenses() throws Exception{
        List<ExpenseResponse> responses = List.of(response1, response2);
        when(expenseService.getAllExpenses()).thenReturn(responses);

        mockMvc.perform(get("/api/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));


        verify(expenseService).getAllExpenses();
    }

    @Test
    void shouldReturnExpenseById() throws Exception{
        ExpenseResponse response = response1;
        when(expenseService.getExpenseById("1")).thenReturn(response);
        mockMvc.perform(get("/api/expenses/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"));

    }

    @Test
    void shouldCreateExpense() throws Exception {
        when(expenseService.createExpense(any())).thenReturn(response1);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100F))
                .andExpect(jsonPath("$.description").value("Description1"));

        verify(expenseService).createExpense(any(CreateExpenseRequest.class));
}

    @Test
    void shouldReturnExpensesByUser() throws Exception {
        List<ExpenseResponse> responses = List.of(response1, response2);

        when(expenseService.getExpensesByUser("user1")).thenReturn(responses);

        mockMvc.perform(get("/api/expenses/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(expenseService).getExpensesByUser("user1");
    }

    @Test
    void shouldReturnMonthlyTotal() throws Exception {
        when(expenseService.getMonthlyTotal("user1", 2026, 3)).thenReturn(300f);

        mockMvc.perform(get("/api/expenses/user/user1/monthly-total")
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("300.0"));

        verify(expenseService).getMonthlyTotal("user1", 2026, 3);
    }

    @Test
    void shouldReturnLargestExpense() throws Exception {
    when(expenseService.getLargestExpense("user1")).thenReturn(response2);

    mockMvc.perform(get("/api/expenses/user/user1/largest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("2"));

    verify(expenseService).getLargestExpense("user1");
    }

    @Test
    void shouldReturnExpensesByCategory() throws Exception {
        List<ExpenseResponse> responses = List.of(response1, response2);

        when(expenseService.getExpensesByCategory("cat1")).thenReturn(responses);

        mockMvc.perform(get("/api/expenses/category/cat1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(expenseService).getExpensesByCategory("cat1");
    }

    @Test
    void shouldDeleteExpense() throws Exception {

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isOk());

        verify(expenseService).deleteExpense("1");
    }
    
    @Test
    void shouldReturnYearlyTotal() throws Exception {
        when(expenseService.getYearlyTotal("user1", 2026)).thenReturn(200f);

        mockMvc.perform(get("/api/expenses/user/user1/yearly-total")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().string("200.0"));

        verify(expenseService).getYearlyTotal("user1", 2026);
    }

    @Test
    void shouldReturnLastNExpenses() throws Exception {
        List<ExpenseResponse> responses = List.of(response2);

        when(expenseService.getLastNExpenses("user1", 1)).thenReturn(responses);

        mockMvc.perform(get("/api/expenses/user/user1/last")
                        .param("n", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("2"));

        verify(expenseService).getLastNExpenses("user1", 1);
    }

    @Test
    void shouldReturnTotalsByCategory() throws Exception {
        var totals = Map.of("category1", 100.0);

        when(expenseService.getTotalsByCategory("user1")).thenReturn(totals);

        mockMvc.perform(get("/api/expenses/user/user1/totals-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category1").value(100.0));

        verify(expenseService).getTotalsByCategory("user1");
    }


    @Test
    void shouldReturnTotalByCategory() throws Exception {
        when(expenseService.getTotalByCategory("category1")).thenReturn(100f);

        mockMvc.perform(get("/api/expenses/category/category1/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));

        verify(expenseService).getTotalByCategory("category1");
    }

    @Test
    void shouldUpdateExpense() throws Exception {
        var updateRequest = new UpdateExpenseRequest("Updated description");
        var updatedResponse = new ExpenseResponse(
                "1",
                100F,
                LocalDateTime.of(2026, 3, 20, 12, 0),
                "Updated description",
                "user1",
                "category1"
        );

        when(expenseService.updateExpense(any(), any(UpdateExpenseRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.id").value("1"));

        verify(expenseService).updateExpense("1", updateRequest);
    }

    


    
}
