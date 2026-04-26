package server.controller;

import java.util.Map;
import java.util.Optional;

import lib.dto.UserDTO;
import lib.dto.UpdateUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.service.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Controlador de usuarios", description = "Operaciones relacionadas con usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(
        summary = "Obtener información del usuario actual",
        description = "Devuelve la información pública del usuario asociado al token proporcionado en la cabecera Authorization.",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK: Usuario encontrado, devuelve información del usuario"),
            @ApiResponse(responseCode = "401", description = "No autorizado: Token inválido o no proporcionado, acceso denegado"),
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = null;
        if (authHeader != null && !authHeader.isBlank()) {
            if (authHeader.startsWith("Bearer ")) token = authHeader.substring(7);
            else token = authHeader;
        }

        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserDTO> userOpt = userService.getUserByToken(token);
        if (userOpt.isPresent()) {
            return new ResponseEntity<>(userOpt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(
        summary = "Actualizar nickname y/o contraseña del usuario actual",
        description = "Actualiza los campos modificables del usuario asociado al token proporcionado en la cabecera Authorization. Si un campo está ausente o vacío no se modifica.",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK: Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Bad Request: DTO inválido"),
            @ApiResponse(responseCode = "401", description = "No autorizado: Token inválido o no proporcionado")
        }
    )
    @PutMapping("/me/update")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> body) {

        if (body == null) {
            body = Map.of();
        }

        String token = null;
        if (authHeader != null && !authHeader.isBlank()) {
            if (authHeader.startsWith("Bearer ")) token = authHeader.substring(7);
            else token = authHeader;
        }

        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String nickname = null;
        String password = null;
        Object nickObj = body.get("nickname");
        if (nickObj instanceof String) nickname = (String) nickObj;
        Object passObj = body.get("password");
        if (passObj instanceof String) password = (String) passObj;

        UpdateUserDTO dto = new UpdateUserDTO(nickname, password);

        Optional<UserDTO> updated = userService.updateUserByToken(token, dto);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}