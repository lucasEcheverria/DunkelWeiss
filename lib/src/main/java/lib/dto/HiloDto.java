package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HiloDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ownerUsername")
    private String ownerUsername;

    @JsonProperty("comunidadNombre")
    private String comunidadNombre;

    public HiloDto() {}

    public HiloDto(Integer id, String title, String description,
                   String ownerUsername, String comunidadNombre) {
        this.id             = id;
        this.title          = title;
        this.description    = description;
        this.ownerUsername  = ownerUsername;
        this.comunidadNombre = comunidadNombre;
    }

    public Integer getId()              { return id; }
    public String  getTitle()           { return title; }
    public String  getDescription()     { return description; }
    public String  getOwnerUsername()   { return ownerUsername; }
    public String  getComunidadNombre() { return comunidadNombre; }

    @Override
    public String toString() {
        return "HiloDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", comunidadNombre='" + comunidadNombre + '\'' +
                '}';
    }
}