package ro.unibuc.prodeng.model;


import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "record")
public record ReportEntity (
    @Id
    String id,
    String assignedUserId,
    int year,
    int month,
    Float totalAmount,
    Map<String, Double> categoryBreakdown
) {}
