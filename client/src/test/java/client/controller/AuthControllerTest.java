package client.controller;

import client.service.AuthServiceProxy;
import lib.dto.UserCredentialsDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "authServiceProxy")
    private AuthServiceProxy authService;

    // ==========================================
    // AuthPage (GET /auth)
    // ==========================================
    @Nested
    class AuthPage {

        @Test
        void authPage_DefaultParameters_ReturnsAuthView() throws Exception {
            mockMvc.perform(get("/auth"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth"))
                    .andExpect(model().attribute("mode", "signin"))
                    .andExpect(model().attributeDoesNotExist("error"));
        }

        @Test
        void authPage_WithErrorAndSignupMode_ReturnsAuthViewWithAttributes() throws Exception {
            mockMvc.perform(get("/auth")
                            .param("error", "true")
                            .param("mode", "signup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth"))
                    .andExpect(model().attribute("mode", "signup"))
                    .andExpect(model().attribute("error", true));
        }
    }

    // ==========================================
    // Login (POST /login)
    // ==========================================
    @Nested
    class Login {

        @Test
        void login_Successful_RedirectsToHome() throws Exception {
            // Arrange: Cuando se llame al login con cualquier DTO, devuelve true
            when(authService.login(any(UserCredentialsDTO.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/login")
                            .param("email", "test@test.com")
                            .param("password", "password123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));

            verify(authService, times(1)).login(any(UserCredentialsDTO.class));
        }

        @Test
        void login_Failed_RedirectsToErrorPage() throws Exception {
            // Arrange: Cuando las credenciales son malas, devuelve false
            when(authService.login(any(UserCredentialsDTO.class))).thenReturn(false);

            // Act & Assert
            mockMvc.perform(post("/login")
                            .param("email", "wrong@test.com")
                            .param("password", "wrongpass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/?error=true&mode=signin"));
        }
    }

    // ==========================================
    // Register (POST /register)
    // ==========================================
    @Nested
    class Register {

        @Test
        void register_Successful_RedirectsToHome() throws Exception {
            when(authService.register(any(UserCredentialsDTO.class))).thenReturn(true);

            mockMvc.perform(post("/register")
                            .param("email", "new@test.com")
                            .param("username", "newuser")
                            .param("password", "pass123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));
        }

        @Test
        void register_Failed_RedirectsToSignupError() throws Exception {
            when(authService.register(any(UserCredentialsDTO.class))).thenReturn(false);

            mockMvc.perform(post("/register")
                            .param("email", "exists@test.com")
                            .param("username", "exists")
                            .param("password", "pass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/?error=true&mode=signup"));
        }
    }

    // ==========================================
    // Logout (GET /logout)
    // ==========================================
    @Nested
    class Logout {

        @Test
        void logout_CallsServiceAndRedirectsToRoot() throws Exception {
            mockMvc.perform(get("/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            // Verificamos que se llamó al método de borrar el token
            verify(authService, times(1)).logout();
        }
    }
}