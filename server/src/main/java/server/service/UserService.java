package server.service;

import java.util.Optional;

import lib.dto.UserDTO;
import org.springframework.stereotype.Service;

import server.entity.User;
import lib.dto.UpdateUserDTO;
import server.dao.UserRepository;

@Service
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Obtiene la información pública del usuario asociado al token.
     *
     * @param token token de sesión
     * @return Optional con UserDTO si el token es válido, vacío en caso contrario
     */
    public Optional<UserDTO> getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        User user = authService.getUserByToken(token);
        if (user == null) return Optional.empty();
        
        return Optional.of(toDto(user));
    }

    /**
     * Actualiza nickname y/o password del usuario asociado al token.
     * Si un campo en el DTO es nulo o vacío, no se modifica.
     *
     * @param token token de sesión
     * @param dto datos a actualizar
     * @return Optional con UserDTO actualizado si el token es válido, vacío en caso contrario
     */
    public Optional<UserDTO> updateUserByToken(String token, UpdateUserDTO dto) {
        if (token == null || token.isBlank() || dto == null) {
            return Optional.empty();
        }

        User user = authService.getUserByToken(token);
        if (user == null) return Optional.empty();

        boolean modified = false;

        String newNickname = dto.getNickname();
        if (newNickname != null && !newNickname.isBlank() && !newNickname.equals(user.getNickname())) {
            user.setNickname(newNickname);
            modified = true;
        }

        String newPassword = dto.getPassword();
        if (newPassword != null && !newPassword.isBlank() && !newPassword.equals(user.getPassword())) {
            user.setPassword(newPassword);
            modified = true;
        }

        if (modified) {
            userRepository.save(user);
        }

        return Optional.of(toDto(user));
    }

    private UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getNickname());
    }     
}