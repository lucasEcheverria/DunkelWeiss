package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateThreadDTO {

    @NotBlank
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("comunidadId")
    private Integer comunidadId;

    @NotNull
    @JsonProperty("ownerId")
    private Integer ownerId;

    public CreateThreadDTO() {}

    public CreateThreadDTO(String title, String description,
                           Integer comunidadId, Integer ownerId) {
        this.title       = title;
        this.description = description;
        this.comunidadId = comunidadId;
        this.ownerId     = ownerId;
    }

    public String  getTitle()       { return title; }
    public String  getDescription() { return description; }
    public Integer getComunidadId() { return comunidadId; }
    public Integer getOwnerId()     { return ownerId; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setComunidadId(Integer comunidadId){ this.comunidadId = comunidadId; }
    public void setOwnerId(Integer ownerId)        { this.ownerId = ownerId; }

    @Override
    public String toString() {
        return "CreateThreadDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", comunidadId=" + comunidadId +
                ", ownerId=" + ownerId +
                '}';
    }
}