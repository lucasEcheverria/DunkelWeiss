package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HiloSummaryDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    public HiloSummaryDto() {}

    public HiloSummaryDto(Integer id, String title) {
        this.id    = id;
        this.title = title;
    }

    public Integer getId()    { return id; }
    public String  getTitle() { return title; }
}