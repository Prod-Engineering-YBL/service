package ro.unibuc.prodeng.request;
import java.time.LocalDateTime;


import jakarta.validation.constraints.NotBlank;


public record CreateExpenseRequest(

    @NotBlank(message = "Amount is required")
    Float amount,

    @NotBlank(message = "Date is required")
    LocalDateTime date,

    @NotBlank(message = "Description is required")
    String description,

    @NotBlank(message = "CategoryId is required")
    String assignedCategoryId,

    @NotBlank(message = "UserId is required")
    String assignedUserId


){}