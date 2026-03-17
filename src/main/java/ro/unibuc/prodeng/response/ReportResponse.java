package ro.unibuc.prodeng.response;
import java.util.*;

import java.time.LocalDateTime;

public record ReportResponse (

    String id,
    String assignedUserId,
    int year,
    int month,
    Float totalAmount,
    Map<String, Double> categoryBreakdown
){}
