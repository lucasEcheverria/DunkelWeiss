package server.service;

import lib.dto.UserCredentialsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import server.dao.UserRepository;
import server.entity.User;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // TRUCO PRO: Como tokenStorage es estático (compartido entre todos los tests),
        // lo reseteamos a un mapa vacío antes de cada test para que no se contaminen entre sí.
        ReflectionTestUtils.setField(AuthService.class, "tokenStorage", new HashMap<>());
    }

    // ==========================================
    // Login
    // ==========================================
    @Nested
    class Login {

        @Test
        void login_ValidCredentials_ReturnsTokenAndStoresIt() {
            // Arrange
            String email = "test@test.com";
            String password = "password123";

            // Creamos un Mock del usuario para no depender de cómo está programada la clase User
            User mockUser = mock(User.class);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
            when(mockUser.checkPassword(password)).thenReturn(true);

            // Act
            Optional<String> tokenOpt = authService.login(email, password);

            // Assert
            assertTrue(tokenOpt.isPresent(), "Debería devolver un token");
            String token = tokenOpt.get();
            assertNotNull(token);

            // Verificamos que el token se guardó en el tokenStorage estático
            assertEquals(mockUser, authService.getUserByToken(token), "El usuario debe estar guardado en memoria con su token");
        }

        @Test
        void login_InvalidPassword_ReturnsEmpty() {
            String email = "test@test.com";
            String wrongPassword = "mala";

            User mockUser = mock(User.class);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
            // Simulamos que la contraseña es incorrecta
            when(mockUser.checkPassword(wrongPassword)).thenReturn(false);

            Optional<String> tokenOpt = authService.login(email, wrongPassword);

            assertFalse(tokenOpt.isPresent(), "No debería devolver token si la contraseña falla");
        }

        @Test
        void login_UserNotFound_ReturnsEmpty() {
            when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

            Optional<String> tokenOpt = authService.login("noexiste@test.com", "pass");

            assertFalse(tokenOpt.isPresent(), "No debería devolver token si el usuario no existe");
        }
    }

    // ==========================================
    // Logout
    // ==========================================
    @Nested
    class Logout {

        @Test
        void logout_ExistingToken_RemovesTokenAndReturnsTrue() {
            // Arrange
            User mockUser = mock(User.class);
            // Inyectamos un token directamente en el mapa estático
            HashMap<String, User> fakeStorage = new HashMap<>();
            fakeStorage.put("TOKEN_VALIDO", mockUser);
            ReflectionTestUtils.setField(AuthService.class, "tokenStorage", fakeStorage);

            // Act
            Optional<Boolean> result = authService.logout("TOKEN_VALIDO");

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get());
            assertNull(authService.getUserByToken("TOKEN_VALIDO"), "El token debería haber sido borrado de memoria");
        }

        @Test
        void logout_NonExistingToken_ReturnsEmpty() {
            Optional<Boolean> result = authService.logout("TOKEN_FALSO");

            assertFalse(result.isPresent(), "Si el token no existe, debe devolver Optional.empty()");
        }
    }

    // ==========================================
    // Register
    // ==========================================
    @Nested
    class Register {

        @Test
        void register_NewUser_SavesAndReturnsUser() {
            // Arrange
            UserCredentialsDTO credentials = new UserCredentialsDTO("new@test.com", "username", "pass");
            when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.empty());

            // Configuramos el mock para que cuando guarde, devuelva el mismo objeto que se le pasó
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Optional<User> result = authService.register(credentials);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("new@test.com", result.get().getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        void register_ExistingEmail_ReturnsEmpty() {
            UserCredentialsDTO credentials = new UserCredentialsDTO("exists@test.com", "username", "pass");
            User existingUser = mock(User.class);

            when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(existingUser));

            Optional<User> result = authService.register(credentials);

            assertFalse(result.isPresent(), "No debe registrar si el email ya existe");
            verify(userRepository, never()).save(any(User.class)); // Comprobamos que no se intentó guardar en BD
        }
    }

    // ==========================================
    // Add User (Utility method)
    // ==========================================
    @Nested
    class AddUser {

        @Test
        void addUser_CallsRepositorySave() {
            User user = mock(User.class);

            authService.addUser(user);

            verify(userRepository, times(1)).save(user);
        }
    }

    // ==========================================
    // Get User By Token
    // ==========================================
    @Nested
    class GetUserByToken {

        @Test
        void getUserByToken_TokenExists_ReturnsUser() {
            User mockUser = mock(User.class);
            HashMap<String, User> fakeStorage = new HashMap<>();
            fakeStorage.put("MI_TOKEN", mockUser);
            ReflectionTestUtils.setField(AuthService.class, "tokenStorage", fakeStorage);

            User result = authService.getUserByToken("MI_TOKEN");

            assertEquals(mockUser, result);
        }

        @Test
        void getUserByToken_TokenDoesNotExist_ReturnsNull() {
            User result = authService.getUserByToken("TOKEN_FALSO");

            assertNull(result);
        }
    }
}