package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "budget")
public record BudgetEntity(
    @Id
    String id,
    String assignedUserId,
    String month,
    double amount,
    String currency
) {}
