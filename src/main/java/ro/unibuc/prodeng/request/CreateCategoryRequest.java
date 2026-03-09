package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest (
    
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Assigned user ID is required")
    String assignedUserId
) {}
