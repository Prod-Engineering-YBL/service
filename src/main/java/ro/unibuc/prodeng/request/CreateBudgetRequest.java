package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBudgetRequest(  

    @NotBlank(message = "User ID is required")
    String assignedUserId,

    @NotBlank(message = "Month is required")
    String month,

    @NotBlank(message = "Amount is required")
    double amount,

    @NotBlank(message = "Currency is required")
    String currency

) {}
