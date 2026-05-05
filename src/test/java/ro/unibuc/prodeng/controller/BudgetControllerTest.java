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
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;
import ro.unibuc.prodeng.service.BudgetService;
import ro.unibuc.prodeng.service.BudgetMetricsService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class BudgetControllerTest {

	@Mock
	private BudgetService budgetService;

	@Mock
	private BudgetMetricsService metricsService;

	@InjectMocks
	private BudgetController budgetController;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final BudgetResponse testBudget1 = new BudgetResponse("1", "user-1", "2026-03", 1000.0, "RON");
	private final BudgetResponse testBudget2 = new BudgetResponse("2", "user-1", "2026-04", 1200.0, "RON");
	private final CreateBudgetRequest createBudgetRequest = new CreateBudgetRequest("user-1", "2026-03", 1000.0, "RON");
	private final Timer.Sample timerSample = Timer.start(new SimpleMeterRegistry());

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
		when(metricsService.startBudgetTimer()).thenReturn(timerSample);
	}

	@Test
	void testGetAllBudgets_withMultipleBudgets_returnsListOfBudgets() throws Exception {
		// Arrange
		List<BudgetResponse> budgets = Arrays.asList(testBudget1, testBudget2);
		when(budgetService.getAllBudgets()).thenReturn(budgets);

		// Act & Assert
		mockMvc.perform(get("/api/budgets")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is("1")))
				.andExpect(jsonPath("$[0].assignedUserId", is("user-1")))
				.andExpect(jsonPath("$[0].month", is("2026-03")))
				.andExpect(jsonPath("$[0].amount", is(1000.0)))
				.andExpect(jsonPath("$[0].currency", is("RON")))
				.andExpect(jsonPath("$[1].id", is("2")))
				.andExpect(jsonPath("$[1].month", is("2026-04")));

		verify(budgetService, times(1)).getAllBudgets();
	}

	@Test
	void testGetAllBudgets_withNoBudgets_returnsEmptyList() throws Exception {
		// Arrange
		when(budgetService.getAllBudgets()).thenReturn(Arrays.asList());

		// Act & Assert
		mockMvc.perform(get("/api/budgets")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		verify(budgetService, times(1)).getAllBudgets();
	}

	@Test
	void testGetBudgetById_existingBudgetRequested_returnsBudget() throws Exception {
		// Arrange
		String budgetId = "1";
		when(budgetService.getBudgetById(budgetId)).thenReturn(testBudget1);

		// Act & Assert
		mockMvc.perform(get("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.assignedUserId", is("user-1")))
				.andExpect(jsonPath("$.month", is("2026-03")))
				.andExpect(jsonPath("$.amount", is(1000.0)))
				.andExpect(jsonPath("$.currency", is("RON")));

		verify(budgetService, times(1)).getBudgetById(budgetId);
	}

	@Test
	void testGetBudgetById_nonExistingBudgetRequested_returnsNotFound() throws Exception {
		// Arrange
		String budgetId = "999";
		when(budgetService.getBudgetById(budgetId)).thenThrow(new EntityNotFoundException("Budget"));

		// Act & Assert
		mockMvc.perform(get("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(budgetService, times(1)).getBudgetById(budgetId);
	}

	@Test
	void testGetBudgetsByUserId_existingUserRequested_returnsBudgets() throws Exception {
		// Arrange
		String userId = "user-1";
		when(budgetService.getBudgetsByUserId(userId)).thenReturn(Arrays.asList(testBudget1, testBudget2));

		// Act & Assert
		mockMvc.perform(get("/api/budgets/user/{userId}", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].assignedUserId", is("user-1")))
				.andExpect(jsonPath("$[1].assignedUserId", is("user-1")));

		verify(budgetService, times(1)).getBudgetsByUserId(userId);
	}

	@Test
	void testCreateBudget_validRequestProvided_createsAndReturnsBudget() throws Exception {
		// Arrange
		when(budgetService.createBudget(any(CreateBudgetRequest.class))).thenReturn(testBudget1);

		// Act & Assert
		mockMvc.perform(post("/api/budgets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createBudgetRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.assignedUserId", is("user-1")))
				.andExpect(jsonPath("$.month", is("2026-03")))
				.andExpect(jsonPath("$.amount", is(1000.0)))
				.andExpect(jsonPath("$.currency", is("RON")));

		verify(budgetService, times(1)).createBudget(any(CreateBudgetRequest.class));
	}

	@Test
	void testUpdateBudget_existingBudgetRequested_updatesAndReturnsBudget() throws Exception {
		// Arrange
		String budgetId = "1";
		CreateBudgetRequest updateRequest = new CreateBudgetRequest("user-1", "2026-04", 1300.0, "EUR");
		BudgetResponse updated = new BudgetResponse("1", "user-1", "2026-04", 1300.0, "EUR");
		when(budgetService.updateBudget(eq(budgetId), any(CreateBudgetRequest.class))).thenReturn(updated);

		// Act & Assert
		mockMvc.perform(put("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.month", is("2026-04")))
				.andExpect(jsonPath("$.amount", is(1300.0)))
				.andExpect(jsonPath("$.currency", is("EUR")));

		verify(budgetService, times(1)).updateBudget(eq(budgetId), any(CreateBudgetRequest.class));
	}

	@Test
	void testUpdateBudget_nonExistingBudgetRequested_returnsNotFound() throws Exception {
		// Arrange
		String budgetId = "999";
		CreateBudgetRequest updateRequest = new CreateBudgetRequest("user-1", "2026-04", 1300.0, "EUR");
		when(budgetService.updateBudget(eq(budgetId), any(CreateBudgetRequest.class)))
				.thenThrow(new EntityNotFoundException("Budget"));

		// Act & Assert
		mockMvc.perform(put("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isNotFound());

		verify(budgetService, times(1)).updateBudget(eq(budgetId), any(CreateBudgetRequest.class));
	}

	@Test
	void testDeleteBudget_existingBudgetRequested_returnsNoContent() throws Exception {
		// Arrange
		String budgetId = "1";

		// Act & Assert
		mockMvc.perform(delete("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(budgetService, times(1)).deleteBudget(budgetId);
	}

	@Test
	void testDeleteBudget_nonExistingBudgetRequested_returnsNotFound() throws Exception {
		// Arrange
		String budgetId = "999";
		doThrow(new EntityNotFoundException("Budget")).when(budgetService).deleteBudget(budgetId);

		// Act & Assert
		mockMvc.perform(delete("/api/budgets/{id}", budgetId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(budgetService, times(1)).deleteBudget(budgetId);
	}

	@Test
	void testGetTotalAmountByUserId_existingUserRequested_returnsTotalAmount() throws Exception {
		// Arrange
		String userId = "user-1";
		when(budgetService.getTotalAmountByUserId(userId)).thenReturn(2200.0);

		// Act & Assert
		mockMvc.perform(get("/api/budgets/user/{userId}/total-amount", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("2200.0"));

		verify(budgetService, times(1)).getTotalAmountByUserId(userId);
	}

	@Test
	void testGetAverageAmountByUserId_existingUserRequested_returnsAverageAmount() throws Exception {
		// Arrange
		String userId = "user-1";
		when(budgetService.getAverageAmountByUserId(userId)).thenReturn(1100.0);

		// Act & Assert
		mockMvc.perform(get("/api/budgets/user/{userId}/average-amount", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("1100.0"));

		verify(budgetService, times(1)).getAverageAmountByUserId(userId);
	}

	@Test
	void testGetHighestBudgetByUserId_existingUserRequested_returnsHighestBudget() throws Exception {
		// Arrange
		String userId = "user-1";
		when(budgetService.getHighestBudgetByUserId(userId)).thenReturn(testBudget2);

		// Act & Assert
		mockMvc.perform(get("/api/budgets/user/{userId}/highest", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("2")))
				.andExpect(jsonPath("$.month", is("2026-04")))
				.andExpect(jsonPath("$.amount", is(1200.0)));

		verify(budgetService, times(1)).getHighestBudgetByUserId(userId);
	}

	@Test
	void testGetHighestBudgetByUserId_nonExistingUserRequested_returnsNotFound() throws Exception {
		// Arrange
		String userId = "user-999";
		when(budgetService.getHighestBudgetByUserId(anyString()))
				.thenThrow(new EntityNotFoundException("Budget"));

		// Act & Assert
		mockMvc.perform(get("/api/budgets/user/{userId}/highest", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(budgetService, times(1)).getHighestBudgetByUserId(userId);
	}
}
