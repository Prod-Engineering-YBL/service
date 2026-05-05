package ro.unibuc.prodeng.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class BudgetMetricsService {

	private final MeterRegistry registry;
	private final Counter budgetsCreatedCounter;
	private final AtomicInteger activeDbConnections = new AtomicInteger(0);
	private final AtomicInteger budgetsInSystem = new AtomicInteger(0);

	public BudgetMetricsService(MeterRegistry registry) {
		this.registry = registry;
		this.budgetsCreatedCounter = Counter.builder("app_budgets_created_total")
				.description("Total number of budgets created")
				.tag("type", "business")
				.register(registry);

		Gauge.builder("app_db_connections_active", activeDbConnections, AtomicInteger::get)
				.description("Currently active database connections")
				.register(registry);

		Gauge.builder("app_budgets_in_system", budgetsInSystem, AtomicInteger::get)
				.description("Current number of budgets in the system")
				.register(registry);

		Counter.builder("app_errors_total")
				.description("Total application errors by type")
				.tag("type", "validation")
				.register(registry);

		Counter.builder("app_errors_total")
				.description("Total application errors by type")
				.tag("type", "not_found")
				.register(registry);
	}

	public void recordBudgetCreated() {
		budgetsCreatedCounter.increment();
	}

	public void recordBudgetError(String type) {
		Counter.builder("app_errors_total")
				.description("Total application errors by type")
				.tag("type", type)
				.register(registry)
				.increment();
	}

	public void incrementActiveDbConnections() {
		activeDbConnections.incrementAndGet();
	}

	public void decrementActiveDbConnections() {
		activeDbConnections.updateAndGet(current -> Math.max(0, current - 1));
	}

	public void incrementBudgetsInSystem() {
		budgetsInSystem.incrementAndGet();
	}

	public void decrementBudgetsInSystem() {
		budgetsInSystem.updateAndGet(current -> Math.max(0, current - 1));
	}

	public Timer.Sample startBudgetTimer() {
		return Timer.start(registry);
	}

	public void stopBudgetTimer(Timer.Sample sample, String operation) {
		sample.stop(Timer.builder("app_request_duration_seconds")
				.description("Time taken to process budget API operations")
				.tag("service", "budget")
				.tag("operation", operation)
				.register(registry));
	}
}
