package server.controller;

import java.util.Optional;

import lib.dto.UserCredentialsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Controlador de Autorización", description = "Operaciones de inicio y cierre de sesión")
public class AuthController {

	private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

	// Endpoint de inicio de sesión (Login)
    @Operation(
            summary = "Iniciar sesión en el sistema",
            description = "Permite a un usuario iniciar sesión proporcionando correo electrónico y contraseña. Devuelve un token si la operación es exitosa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: Inicio de sesión exitoso, devuelve un token"),
                    @ApiResponse(responseCode = "401", description = "No autorizado: Credenciales inválidas, inicio de sesión fallido"),
            }
    )
	@PostMapping("/login")
	public ResponseEntity<String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Correo electrónico y contraseña", required = true)
            @RequestBody UserCredentialsDTO credentialsDTO) {
		Optional<String> token = authService.login(credentialsDTO.getEmail(), credentialsDTO.getPassword());
		
		if (token.isPresent()) {
			return new ResponseEntity<>(token.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	// Endpoint de cierre de sesión (Logout)
    @Operation(
            summary = "Cerrar sesión en el sistema",
            description = "Permite a un usuario cerrar sesión proporcionando el token de autorización.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Sin contenido: Cierre de sesión exitoso"),
                    @ApiResponse(responseCode = "401", description = "No autorizado: Token inválido, cierre de sesión fallido"),
            }
    )
    @PostMapping("/logout")
	public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Envía el token en texto plano", required = true)
            @RequestBody String token) {
		Optional<Boolean> result = authService.logout(token);
		
		if (result.isPresent() && result.get()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

    // Endpoint de registro (Register)
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Registra un nuevo usuario si el email no está ya en uso.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created: Usuario registrado correctamente"),
                    @ApiResponse(responseCode = "409", description = "Conflict: Usuario con ese email ya existe"),
            }
    )
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email, username y password", required = true)
            @RequestBody UserCredentialsDTO credentialsDTO) {
        Optional<?> created = authService.register(credentialsDTO).map(u -> (Object) u);

        if (created.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}