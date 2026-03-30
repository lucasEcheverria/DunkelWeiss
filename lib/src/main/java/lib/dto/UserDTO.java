package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para enviar la información pública del usuario al cliente.
 */
public class UserDTO {
	
	/** id de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("id")
    private Integer id;
    
    /** Email de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("email")
    private String email;
    
    /** Nombre de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("username")
    private String username;

    // Jackson necesita constructor vacío
    public UserDTO() {}

    public UserDTO(Integer id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}
