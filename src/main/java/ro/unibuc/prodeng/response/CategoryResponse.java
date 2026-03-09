package ro.unibuc.prodeng.response;

public record CategoryResponse (
    String id,
    String name,
    String assignedUserId
) {}
