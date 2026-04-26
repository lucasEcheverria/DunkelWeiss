package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.dto.UserCredentialsDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import server.entity.User;
import server.service.AuthService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Nos ayuda a convertir el DTO a texto JSON

    @MockitoBean
    private AuthService authService;

    // ==========================================
    // Login (POST /auth/login)
    // ==========================================
    @Nested
    class Login {

        @Test
        void login_ValidCredentials_ReturnsOkAndToken() throws Exception {
            // Arrange
            UserCredentialsDTO credentials = new UserCredentialsDTO("user@test.com", "user", "password");
            when(authService.login("user@test.com", "password")).thenReturn(Optional.of("TOKEN_JWT_123"));

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials))) // Convertimos el DTO a JSON
                    .andExpect(status().isOk())
                    .andExpect(content().string("TOKEN_JWT_123")); // Comprobamos que el body tiene el token

            verify(authService, times(1)).login("user@test.com", "password");
        }

        @Test
        void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
            // Arrange
            UserCredentialsDTO credentials = new UserCredentialsDTO("user@test.com", "user", "wrong_pass");
            when(authService.login("user@test.com", "wrong_pass")).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isUnauthorized()); // Esperamos un error 401
        }
    }

    // ==========================================
    // Logout (POST /auth/logout)
    // ==========================================
    @Nested
    class Logout {

        @Test
        void logout_ValidToken_ReturnsNoContent() throws Exception {
            // Arrange
            String token = "TOKEN_VALIDO";
            when(authService.logout(token)).thenReturn(Optional.of(true));

            // Act & Assert
            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.TEXT_PLAIN) // Ojo: tu controlador espera texto plano aquí
                            .content(token))
                    .andExpect(status().isNoContent()); // 204 No Content
        }

        @Test
        void logout_InvalidToken_ReturnsUnauthorized() throws Exception {
            // Arrange
            String token = "TOKEN_FALSO";
            // Simulamos que el servicio falla al intentar borrar el token (devuelve empty o false)
            when(authService.logout(token)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(token))
                    .andExpect(status().isUnauthorized()); // 401 Unauthorized
        }
    }

    // ==========================================
    // Register (POST /auth/register)
    // ==========================================
    @Nested
    class Register {

        @Test
        void register_NewUser_ReturnsCreated() throws Exception {
            // 1. Preparamos los datos
            UserCredentialsDTO credentials = new UserCredentialsDTO("new@test.com", "newuser", "pass");

            // 2. Creamos un usuario real para que el Optional coincida con el tipo esperado (Optional<User>)
            User user = new User("new@test.com", "newuser", "pass");

            // 3. Configuramos el Mock
            when(authService.register(any(UserCredentialsDTO.class))).thenReturn(Optional.of(user));

            // 4. Ejecutamos y comprobamos
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isCreated()); // Debería dar 201 Created
        }

        @Test
        void register_ExistingUser_ReturnsConflict() throws Exception {
            UserCredentialsDTO credentials = new UserCredentialsDTO("exists@test.com", "existing", "pass");

            // Si el email ya existe, el servicio devuelve un Optional vacío
            when(authService.register(any(UserCredentialsDTO.class))).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isConflict()); // Debería dar 409 Conflict
        }
    }
}
