package ro.unibuc.prodeng.request;
import java.util.*;

import jakarta.validation.constraints.NotBlank;

public record CreateRecordRequest (

    
    @NotBlank(message = "UserId is required")
    String assignedUserId,
        
        
    @NotBlank(message = "year is required")
    int year,

    @NotBlank(message = "month is required")
    int month,

    @NotBlank(message = "totalAmount is required")
    Float totalAmount,

    @NotBlank(message = "categoryBreakdown is required")
    Map<String, Double> categoryBreakdown

)
{}
