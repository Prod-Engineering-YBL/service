package ro.unibuc.prodeng.controller;


import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.CreateCategoryRequest;
import ro.unibuc.prodeng.repository.CategoryRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.repository.ExpenseRepository;
import ro.unibuc.prodeng.request.CreateExpenseRequest;
import ro.unibuc.prodeng.request.UpdateExpenseRequest;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ExpenseController Integration Tests")
public class ExpenseControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private String createUser(String name, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, email);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createCategory(String name, String assignedUserId) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, assignedUserId);

        String response = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.assignedUserId").value(assignedUserId))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createExpense(String assignedUserId, String assignedCategoryId, Float amount, LocalDateTime date, String description) throws Exception {
        CreateExpenseRequest request = new CreateExpenseRequest(amount, date, description, assignedCategoryId, assignedUserId);

        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(assignedUserId))
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.assignedCategoryId").value(assignedCategoryId))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetExpense_validExpenseCreation_retrievesExpenseSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String categoryId = createCategory("Food", userId);
        LocalDateTime date = LocalDateTime.of(2026, 3, 20, 12, 0);
        String expenseId = createExpense(userId, categoryId, 100.0f, date, "Lunch");

        // Act & Assert
        mockMvc.perform(get("/api/expenses/" + expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(userId))
                .andExpect(jsonPath("$.assignedCategoryId").value(categoryId))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void testGetAllExpenses_multipleExpensesExist_returnsAllExpenses() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String categoryId = createCategory("Food", userId);
        LocalDateTime date1 = LocalDateTime.of(2026, 3, 20, 12, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 21, 14, 30);
        createExpense(userId, categoryId, 100.0f, date1, "Lunch");
        createExpense(userId, categoryId, 50.0f, date2, "Snack");

        // Act & Assert
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    void testDeleteExpense_existingExpense_deletesSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String categoryId = createCategory("Food", userId);
        LocalDateTime date = LocalDateTime.of(2026, 3, 20, 12, 0);
        String expenseId = createExpense(userId, categoryId, 100.0f, date, "Lunch");

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/" + expenseId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetMonthlyTotal_multipleExpenses_returnsCorrectTotal() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String categoryId = createCategory("Food", userId);
        LocalDateTime date1 = LocalDateTime.of(2026, 3, 20, 12, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 21, 14, 30);
        createExpense(userId, categoryId, 100.0f, date1, "Lunch");
        createExpense(userId, categoryId, 50.0f, date2, "Snack");

        // Act & Assert
        mockMvc.perform(get("/api/expenses/user/" + userId + "/monthly-total")
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.0"));
    }



    @Test
    void testGetLargestExpense_multipleExpenses_returnsLargestExpense() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String categoryId = createCategory("Food", userId);
        LocalDateTime date1 = LocalDateTime.of(2026, 3, 20, 12, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 21, 14, 30);
        createExpense(userId, categoryId, 100.0f, date1, "Lunch");
        createExpense(userId, categoryId, 200.0f, date2, "Dinner");

        // Act & Assert
        mockMvc.perform(get("/api/expenses/user/" + userId + "/largest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(userId))
                .andExpect(jsonPath("$.amount").value(200.0))
                .andExpect(jsonPath("$.description").value("Dinner"));
    }

    @Test
    void testGetExpenseById_nonExistentExpense_returnsNotFound() throws Exception {
        // Arrange
        String nonExistentId = "nonexistent-expense-id";

        // Act & Assert
        mockMvc.perform(get("/api/expenses/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity: " + nonExistentId + " was not found"));
    }

    
}
