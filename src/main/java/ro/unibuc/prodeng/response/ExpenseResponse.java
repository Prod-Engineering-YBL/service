package ro.unibuc.prodeng.response;
import java.time.LocalDateTime;

public record ExpenseResponse(
    String id,
    Float amount,
    LocalDateTime date,
    String description,
    String assignedUserId,
    String assignedCategoryId
)
{}
