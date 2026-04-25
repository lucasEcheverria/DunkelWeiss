package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostDTO(
        @NotBlank
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @NotNull
        @JsonProperty("threadId") Integer threadId,
        @JsonProperty("parentId") Integer parentId
) {}
