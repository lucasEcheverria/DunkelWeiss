package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para actualizar campos editables del usuario.
 * Solo incluye nickname y password por ahora.
 */
public class UpdateUserDTO {
	
	/** Nombre de usuario. */
    @JsonProperty("nickname")
    private String nickname;
    
    /** Contraseña de usuario. */
    @JsonProperty("password")
    private String password;

    public UpdateUserDTO() {}

    public UpdateUserDTO(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }
}
