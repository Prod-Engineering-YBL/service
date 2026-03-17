package ro.unibuc.prodeng.response;

public record BudgetResponse (
    String id,
    String assignedUserId,
    String month,
    double amount,
    String currency

) {}
