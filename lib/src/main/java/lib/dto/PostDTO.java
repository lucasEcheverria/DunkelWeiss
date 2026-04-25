package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PostDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("ownerUsername") String ownerUsername,
        @JsonProperty("threadId") Integer threadId,
        @JsonProperty("parentId") Integer parentId,
        @JsonProperty("likes") Integer likes,
        @JsonProperty("dislikes") Integer dislikes,
        @JsonProperty("replies") List<PostDTO> replies
) {}
