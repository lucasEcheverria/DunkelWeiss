package lib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para actualizar campos editables del usuario.
 * Solo incluye nickname y password por ahora.
 */
public record UpdateUserDTO(
        @JsonProperty("nickname") String nickname,
        @JsonProperty("password") String password
) {
    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }
}