package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateThreadDTO(
        @NotBlank
        @JsonProperty("title")
        String  title,
        @JsonProperty("description")
        String  description,
        @NotNull
        @JsonProperty("comunidadId")
        Integer comunidadId,
        @NotNull
        @JsonProperty("ownerId")
        Integer ownerId
) {}