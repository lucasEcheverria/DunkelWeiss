package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO compartido entre cliente y servidor para transportar
 * las credenciales de autenticación del usuario.
 */
public class UserCredentialsDto {

    /** Nombre de usuario, no puede estar vacío. */
    @NotBlank
    @JsonProperty("username")
    private String username;

    /** Contraseña del usuario, no puede estar vacía. */
    @NotBlank
    @JsonProperty("password")
    private String password;

    // Constructor vacío requerido por Jackson para deserialización
    public UserCredentialsDto() {}

    public UserCredentialsDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
