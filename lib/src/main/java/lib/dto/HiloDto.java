package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO compartido entre cliente y servidor para transportar
 * la información de un hilo en las búsquedas.
 */
public class HiloDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    // Constructor vacío requerido por Jackson para deserialización
    public HiloDto() {}

    public HiloDto(Integer id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}