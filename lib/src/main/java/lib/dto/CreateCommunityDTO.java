package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommunityDTO(
        @NotBlank @JsonProperty("name")        String name,
        @JsonProperty("description") String description
) {}