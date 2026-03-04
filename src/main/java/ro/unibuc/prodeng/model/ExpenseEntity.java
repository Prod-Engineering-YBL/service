package ro.unibuc.prodeng.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "expense")
public record ExpenseEntity(
    @Id
    String id,
    Float amount,
    LocalDateTime date,
    String description,
    String assignedUserId,
    String assignedCategoryId
) {}
