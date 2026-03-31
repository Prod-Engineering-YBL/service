package ro.unibuc.prodeng.e2e.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ro.unibuc.prodeng.request.CreateBudgetRequest;
import ro.unibuc.prodeng.response.BudgetResponse;
import ro.unibuc.prodeng.response.UserResponse;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("null")
public class BudgetSteps {

	private static final String BASE_URL = "http://localhost:8080";

	private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	private final ObjectMapper objectMapper = new ObjectMapper();

	private ResponseEntity<String> latestResponse;
	private final List<String> createdBudgetIds = new ArrayList<>();
	private String lastCreatedBudgetId;

	@After
	public void cleanup() {
		for (String budgetId : createdBudgetIds) {
			try {
				restTemplate.delete(BASE_URL + "/api/budgets/" + budgetId);
			} catch (Exception e) {
				// Ignore cleanup errors for already deleted budgets.
			}
		}
		createdBudgetIds.clear();
	}

	@When("the client creates a budget for month {string} with amount {double} {word} for {word}")
	public void createBudget(String month, double amount, String currency, String email) throws Exception {
		String userId = getUserIdByEmail(email);
		CreateBudgetRequest request = new CreateBudgetRequest(userId, month, amount, currency);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<CreateBudgetRequest> entity = new HttpEntity<>(request, headers);

		latestResponse = restTemplate.postForEntity(BASE_URL + "/api/budgets", entity, String.class);
		BudgetResponse budget = objectMapper.readValue(latestResponse.getBody(), BudgetResponse.class);
		lastCreatedBudgetId = budget.id();
		createdBudgetIds.add(lastCreatedBudgetId);
	}

	@Then("the client can retrieve {int} budget\\(s\\) for {word}")
	public void verifyBudgetCountByUser(int count, String email) throws Exception {
		String userId = getUserIdByEmail(email);
		ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/budgets/user/" + userId, String.class);

		List<BudgetResponse> budgets = objectMapper.readValue(response.getBody(), new TypeReference<List<BudgetResponse>>() {});
		assertThat("budget count is incorrect", budgets.size(), is(count));
	}

	@When("the client retrieves all budgets")
	public void retrieveAllBudgets() {
		latestResponse = restTemplate.getForEntity(BASE_URL + "/api/budgets", String.class);
	}

	@Then("the client can see at least {int} budgets")
	public void verifyAtLeastBudgetCount(int minimumCount) throws Exception {
		List<BudgetResponse> budgets = objectMapper.readValue(latestResponse.getBody(), new TypeReference<List<BudgetResponse>>() {});
		assertThat("budget count should be at least " + minimumCount, budgets.size(), greaterThanOrEqualTo(minimumCount));
	}

	@When("a budget for month {string} with amount {double} {word} for {word} exists")
	public void ensureBudgetExists(String month, double amount, String currency, String email) throws Exception {
		createBudget(month, amount, currency, email);
	}

	@When("the client retrieves the budget by id")
	public void retrieveBudgetById() {
		latestResponse = restTemplate.getForEntity(BASE_URL + "/api/budgets/" + lastCreatedBudgetId, String.class);
	}

	@Then("the budget response status code is {int}")
	public void verifyBudgetStatusCode(int expectedStatusCode) {
		assertThat("status code is incorrect", latestResponse.getStatusCode().value(), is(expectedStatusCode));
	}

	@Then("the budget has month {string} with amount {double} and currency {word}")
	public void verifyBudgetPayload(String month, double amount, String currency) throws Exception {
		BudgetResponse budget = objectMapper.readValue(latestResponse.getBody(), BudgetResponse.class);
		assertThat("budget month is incorrect", budget.month(), is(month));
		assertThat("budget amount is incorrect", budget.amount(), is(amount));
		assertThat("budget currency is incorrect", budget.currency(), is(currency));
	}

	@When("the client updates the budget amount to {double}")
	public void updateLastBudgetAmount(double amount) throws Exception {
		BudgetResponse current = getBudgetById(lastCreatedBudgetId);
		CreateBudgetRequest request = new CreateBudgetRequest(
				current.assignedUserId(),
				current.month(),
				amount,
				current.currency()
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<CreateBudgetRequest> entity = new HttpEntity<>(request, headers);

		latestResponse = restTemplate.exchange(
				BASE_URL + "/api/budgets/" + lastCreatedBudgetId,
				HttpMethod.PUT,
				entity,
				String.class
		);
	}

	@When("the client deletes the budget")
	public void deleteLastBudget() {
		latestResponse = restTemplate.exchange(
				BASE_URL + "/api/budgets/" + lastCreatedBudgetId,
				HttpMethod.DELETE,
				null,
				String.class
		);
	}

	@When("the client tries to retrieve the deleted budget by id")
	public void retrieveDeletedBudgetById() {
		try {
			latestResponse = restTemplate.getForEntity(BASE_URL + "/api/budgets/" + lastCreatedBudgetId, String.class);
		} catch (HttpClientErrorException e) {
			latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
		}
	}

	@When("the client updates the budget for month {string} to amount {double} {word} for {word}")
	public void updateBudgetByMonthAndUser(String month, double amount, String currency, String email) throws Exception {
		String userId = getUserIdByEmail(email);
		List<BudgetResponse> userBudgets = getBudgetsForUser(userId);

		BudgetResponse budgetToUpdate = userBudgets.stream()
				.filter(b -> b.month().equals(month))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Budget not found for month: " + month));

		CreateBudgetRequest request = new CreateBudgetRequest(userId, month, amount, currency);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<CreateBudgetRequest> entity = new HttpEntity<>(request, headers);

		latestResponse = restTemplate.exchange(
				BASE_URL + "/api/budgets/" + budgetToUpdate.id(),
				HttpMethod.PUT,
				entity,
				String.class
		);

		lastCreatedBudgetId = budgetToUpdate.id();
	}

	@Then("the client can retrieve total budget amount {double} for {word}")
	public void verifyTotalAmountForUser(double expectedTotal, String email) throws Exception {
		String userId = getUserIdByEmail(email);
		ResponseEntity<String> response = restTemplate.getForEntity(
				BASE_URL + "/api/budgets/user/" + userId + "/total-amount",
				String.class
		);

		double total = Double.parseDouble(response.getBody());
		assertThat("total budget amount is incorrect", total, is(expectedTotal));
	}

	@Then("the client can retrieve average budget amount {double} for {word}")
	public void verifyAverageAmountForUser(double expectedAverage, String email) throws Exception {
		String userId = getUserIdByEmail(email);
		ResponseEntity<String> response = restTemplate.getForEntity(
				BASE_URL + "/api/budgets/user/" + userId + "/average-amount",
				String.class
		);

		double average = Double.parseDouble(response.getBody());
		assertThat("average budget amount is incorrect", average, is(expectedAverage));
	}

	@Then("the highest budget amount for {word} is {double}")
	public void verifyHighestAmountForUser(String email, double expectedHighestAmount) throws Exception {
		String userId = getUserIdByEmail(email);
		ResponseEntity<String> response = restTemplate.getForEntity(
				BASE_URL + "/api/budgets/user/" + userId + "/highest",
				String.class
		);

		BudgetResponse highest = objectMapper.readValue(response.getBody(), BudgetResponse.class);
		assertThat("highest budget amount is incorrect", highest.amount(), is(expectedHighestAmount));
	}

	@When("the client retrieves budget id {word}")
	public void retrieveBudgetById(String budgetId) {
		try {
			latestResponse = restTemplate.getForEntity(BASE_URL + "/api/budgets/" + budgetId, String.class);
		} catch (HttpClientErrorException e) {
			latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
		}
	}

	@When("the client deletes budget id {word}")
	public void deleteBudgetById(String budgetId) {
		try {
			latestResponse = restTemplate.exchange(
					BASE_URL + "/api/budgets/" + budgetId,
					HttpMethod.DELETE,
					null,
					String.class
			);
		} catch (HttpClientErrorException e) {
			latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
		}
	}

	private String getUserIdByEmail(String email) throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity(
				BASE_URL + "/api/users/by-email?email=" + email,
				String.class
		);
		UserResponse user = objectMapper.readValue(response.getBody(), UserResponse.class);
		return user.id();
	}

	private BudgetResponse getBudgetById(String budgetId) throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/budgets/" + budgetId, String.class);
		return objectMapper.readValue(response.getBody(), BudgetResponse.class);
	}

	private List<BudgetResponse> getBudgetsForUser(String userId) throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/budgets/user/" + userId, String.class);
		return objectMapper.readValue(response.getBody(), new TypeReference<List<BudgetResponse>>() {});
	}
}
