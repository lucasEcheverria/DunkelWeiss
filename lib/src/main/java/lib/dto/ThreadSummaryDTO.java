package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThreadSummaryDTO(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("owner")
        String owner
){}