package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThreadDTO(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("title")
        String  title,
        @JsonProperty("description")
        String  description,
        @JsonProperty("ownerUsername")
        String  ownerUsername,
        @JsonProperty("comunidadNombre")
        String  comunidadNombre
) {}