package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO compartido entre cliente y servidor para transportar
 * las credenciales de autenticación del usuario.
 */
public class UserCredentialsDTO {

	/** Email de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("email")
    private String email;
	
    /** Nombre de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("username")
    private String username;

    /** Contraseña del usuario, no puede estar vacía. */
    @NotBlank
    @JsonProperty("password")
    private String password;

    // Constructor vacío requerido por Jackson para deserialización
    public UserCredentialsDTO() {}

    public UserCredentialsDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public UserCredentialsDTO(String email, String username, String password) {
		this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
