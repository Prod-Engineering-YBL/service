package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "category")
public record CategoryEntity (
    
    @Id
    String id,
    String name,
    String assignedUserId
) {}
