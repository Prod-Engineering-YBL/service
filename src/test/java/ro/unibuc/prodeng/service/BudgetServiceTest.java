package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.model.BudgetEntity;
import ro.unibuc.prodeng.repository.BudgetRepository;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BudgetServiceTest {

	@Mock
	private BudgetRepository budgetRepository;

	@InjectMocks
	private BudgetService budgetService;

	@Test
	void testGetAllBudgets_withMultipleBudgets_returnsAllBudgets() {
		// Arrange
		List<BudgetEntity> budgets = Arrays.asList(
				new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON"),
				new BudgetEntity("2", "user-2", "2026-04", 1500.0, "EUR")
		);
		when(budgetRepository.findAll()).thenReturn(budgets);

		// Act
		List<BudgetResponse> result = budgetService.getAllBudgets();

		// Assert
		assertEquals(2, result.size());
		assertEquals("user-1", result.get(0).assignedUserId());
		assertEquals("user-2", result.get(1).assignedUserId());
	}

	@Test
	void testGetBudgetById_existingBudgetRequested_returnsBudget() {
		// Arrange
		BudgetEntity budget = new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON");
		when(budgetRepository.findById("1")).thenReturn(Optional.of(budget));

		// Act
		BudgetResponse result = budgetService.getBudgetById("1");

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("user-1", result.assignedUserId());
		assertEquals("2026-03", result.month());
		assertEquals(1000.0, result.amount());
		assertEquals("RON", result.currency());
	}

	@Test
	void testGetBudgetById_nonExistingBudgetRequested_throwsRuntimeException() {
		// Arrange
		when(budgetRepository.findById("999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(RuntimeException.class, () -> budgetService.getBudgetById("999"));
	}

	@Test
	void testGetBudgetsByUserId_existingUserRequested_returnsBudgets() {
		// Arrange
		List<BudgetEntity> budgets = Arrays.asList(
				new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON"),
				new BudgetEntity("2", "user-1", "2026-04", 1200.0, "RON")
		);
		when(budgetRepository.findByAssignedUserId("user-1")).thenReturn(budgets);

		// Act
		List<BudgetResponse> result = budgetService.getBudgetsByUserId("user-1");

		// Assert
		assertEquals(2, result.size());
		assertEquals("2026-03", result.get(0).month());
		assertEquals("2026-04", result.get(1).month());
	}

	@Test
	void testCreateBudget_validRequestProvided_createsAndReturnsBudget() {
		// Arrange
		CreateBudgetRequest request = new CreateBudgetRequest("user-1", "2026-03", 1000.0, "RON");
		when(budgetRepository.save(any(BudgetEntity.class))).thenAnswer(invocation -> {
			BudgetEntity entity = invocation.getArgument(0);
			return new BudgetEntity("generated-id-1", entity.assignedUserId(), entity.month(), entity.amount(), entity.currency());
		});

		// Act
		BudgetResponse result = budgetService.createBudget(request);

		// Assert
		assertNotNull(result);
		assertNotNull(result.id());
		assertEquals("user-1", result.assignedUserId());
		assertEquals("2026-03", result.month());
		assertEquals(1000.0, result.amount());
		assertEquals("RON", result.currency());
		verify(budgetRepository, times(1)).save(any(BudgetEntity.class));
	}

	@Test
	void testUpdateBudget_existingBudgetRequested_updatesAndReturnsBudget() {
		// Arrange
		CreateBudgetRequest request = new CreateBudgetRequest("user-1", "2026-04", 1300.0, "EUR");
		BudgetEntity existing = new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON");
		when(budgetRepository.findById("1")).thenReturn(Optional.of(existing));
		when(budgetRepository.save(any(BudgetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		BudgetResponse result = budgetService.updateBudget("1", request);

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("user-1", result.assignedUserId());
		assertEquals("2026-04", result.month());
		assertEquals(1300.0, result.amount());
		assertEquals("EUR", result.currency());
	}

	@Test
	void testUpdateBudget_nonExistingBudgetRequested_throwsRuntimeException() {
		// Arrange
		CreateBudgetRequest request = new CreateBudgetRequest("user-1", "2026-04", 1300.0, "EUR");
		when(budgetRepository.findById("999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(RuntimeException.class, () -> budgetService.updateBudget("999", request));
	}

	@Test
	void testDeleteBudget_existingBudgetRequested_deletesSuccessfully() {
		// Arrange
		when(budgetRepository.existsById("1")).thenReturn(true);

		// Act
		budgetService.deleteBudget("1");

		// Assert
		verify(budgetRepository, times(1)).deleteById("1");
	}

	@Test
	void testDeleteBudget_nonExistingBudgetRequested_throwsRuntimeException() {
		// Arrange
		when(budgetRepository.existsById("999")).thenReturn(false);

		// Act & Assert
		assertThrows(RuntimeException.class, () -> budgetService.deleteBudget("999"));
	}

	@Test
	void testGetTotalAmountByUserId_withMultipleBudgets_returnsTotalAmount() {
		// Arrange
		List<BudgetEntity> budgets = Arrays.asList(
				new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON"),
				new BudgetEntity("2", "user-1", "2026-04", 500.5, "RON")
		);
		when(budgetRepository.findByAssignedUserId("user-1")).thenReturn(budgets);

		// Act
		double result = budgetService.getTotalAmountByUserId("user-1");

		// Assert
		assertEquals(1500.5, result);
	}

	@Test
	void testGetAverageAmountByUserId_withMultipleBudgets_returnsAverageAmount() {
		// Arrange
		List<BudgetEntity> budgets = Arrays.asList(
				new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON"),
				new BudgetEntity("2", "user-1", "2026-04", 500.0, "RON")
		);
		when(budgetRepository.findByAssignedUserId("user-1")).thenReturn(budgets);

		// Act
		double result = budgetService.getAverageAmountByUserId("user-1");

		// Assert
		assertEquals(750.0, result);
	}

	@Test
	void testGetAverageAmountByUserId_withNoBudgets_returnsZero() {
		// Arrange
		when(budgetRepository.findByAssignedUserId("user-1")).thenReturn(List.of());

		// Act
		double result = budgetService.getAverageAmountByUserId("user-1");

		// Assert
		assertEquals(0.0, result);
	}

	@Test
	void testGetHighestBudgetByUserId_withBudgets_returnsHighestBudget() {
		// Arrange
		List<BudgetEntity> budgets = Arrays.asList(
				new BudgetEntity("1", "user-1", "2026-03", 1000.0, "RON"),
				new BudgetEntity("2", "user-1", "2026-04", 1800.0, "RON"),
				new BudgetEntity("3", "user-1", "2026-05", 1200.0, "RON")
		);
		when(budgetRepository.findByAssignedUserId("user-1")).thenReturn(budgets);

		// Act
		BudgetResponse result = budgetService.getHighestBudgetByUserId("user-1");

		// Assert
		assertNotNull(result);
		assertEquals("2", result.id());
		assertEquals(1800.0, result.amount());
	}

	@Test
	void testGetHighestBudgetByUserId_withNoBudgets_throwsRuntimeException() {
		// Arrange
		when(budgetRepository.findByAssignedUserId("user-999")).thenReturn(List.of());

		// Act & Assert
		assertThrows(RuntimeException.class, () -> budgetService.getHighestBudgetByUserId("user-999"));
	}
}
