package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommunityDTO(
        @JsonProperty("id")          Integer id,
        @JsonProperty("name")        String  name,
        @JsonProperty("description") String  description
) {}