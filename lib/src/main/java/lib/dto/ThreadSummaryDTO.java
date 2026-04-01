package lib.dto;

public record ThreadSummaryDTO(
    Integer id,
    String title,
    String description,
    String owner
){}