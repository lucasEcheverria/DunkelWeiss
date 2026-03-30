package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ThreadSummaryDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    public ThreadSummaryDTO() {}

    public ThreadSummaryDTO(Integer id, String title) {
        this.id    = id;
        this.title = title;
    }

    public Integer getId()    { return id; }
    public String  getTitle() { return title; }
}