package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.BudgetRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BudgetController Integration Tests")
public class BudgetControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    BudgetRepository budgetRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        budgetRepository.deleteAll();
        userRepository.deleteAll();
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

    private String createBudget(String assignedUserId, String month, double amount, String currency) throws Exception {
        CreateBudgetRequest request = new CreateBudgetRequest(assignedUserId, month, amount, currency);

        String response = mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(assignedUserId))
                .andExpect(jsonPath("$.month").value(month))
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.currency").value(currency))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetBudget_validBudgetCreation_retrievesBudgetSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String budgetId = createBudget(userId, "2026-03", 1500.0, "RON");

        // Act & Assert
        mockMvc.perform(get("/api/budgets/" + budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(userId))
                .andExpect(jsonPath("$.month").value("2026-03"))
                .andExpect(jsonPath("$.amount").value(1500.0))
                .andExpect(jsonPath("$.currency").value("RON"));
    }

    @Test
    void testGetAllBudgets_multipleBudgetsExist_returnsAllBudgets() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        createBudget(userId, "2026-03", 1500.0, "RON");
        createBudget(userId, "2026-04", 1800.0, "RON");

        // Act & Assert
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetBudgetsByUserId_multipleUsersWithDifferentBudgets_filtersCorrectly() throws Exception {
        // Arrange
        String aliceId = createUser("Alice", "alice@example.com");
        String bobId = createUser("Bob", "bob@example.com");
        createBudget(aliceId, "2026-03", 1500.0, "RON");
        createBudget(aliceId, "2026-04", 1700.0, "RON");
        createBudget(bobId, "2026-03", 1100.0, "EUR");

        // Act & Assert
        mockMvc.perform(get("/api/budgets/user/" + aliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/budgets/user/" + bobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateBudgetWithPut_validBudgetData_updatesBudgetSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String budgetId = createBudget(userId, "2026-03", 1500.0, "RON");

        // Act & Assert
        mockMvc.perform(put("/api/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedUserId\":\"" + userId + "\",\"month\":\"2026-03\",\"amount\":2000.0,\"currency\":\"RON\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(userId))
                .andExpect(jsonPath("$.month").value("2026-03"))
                .andExpect(jsonPath("$.amount").value(2000.0))
                .andExpect(jsonPath("$.currency").value("RON"));
    }

    @Test
    void testDeleteBudget_existingBudget_deletesSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        String budgetId = createBudget(userId, "2026-03", 1500.0, "RON");

        // Act & Assert
        mockMvc.perform(delete("/api/budgets/" + budgetId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetTotalAmountByUserId_multipleBudgets_returnsCorrectTotal() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        createBudget(userId, "2026-03", 1500.0, "RON");
        createBudget(userId, "2026-04", 2500.0, "RON");

        // Act & Assert
        mockMvc.perform(get("/api/budgets/user/" + userId + "/total-amount"))
                .andExpect(status().isOk())
                .andExpect(content().string("4000.0"));
    }

    @Test
    void testGetAverageAmountByUserId_multipleBudgets_returnsCorrectAverage() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        createBudget(userId, "2026-03", 1000.0, "RON");
        createBudget(userId, "2026-04", 2000.0, "RON");
        createBudget(userId, "2026-05", 3000.0, "RON");

        // Act & Assert
        mockMvc.perform(get("/api/budgets/user/" + userId + "/average-amount"))
                .andExpect(status().isOk())
                .andExpect(content().string("2000.0"));
    }

    @Test
    void testGetHighestBudgetByUserId_multipleBudgets_returnsHighestBudget() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");
        createBudget(userId, "2026-03", 1200.0, "RON");
        createBudget(userId, "2026-04", 2600.0, "RON");
        createBudget(userId, "2026-05", 2000.0, "RON");

        // Act & Assert
        mockMvc.perform(get("/api/budgets/user/" + userId + "/highest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(userId))
                .andExpect(jsonPath("$.month").value("2026-04"))
                .andExpect(jsonPath("$.amount").value(2600.0))
                .andExpect(jsonPath("$.currency").value("RON"));
    }

    @Test
    void testGetBudgetById_nonExistentBudget_returnsNotFound() throws Exception {
        // Arrange
        String nonExistentId = "nonexistent-budget-id";

        // Act & Assert
        mockMvc.perform(get("/api/budgets/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity: Budget with id: " + nonExistentId + " was not found"));
    }

    
}
