package client.service;

import lib.dto.UserCredentialsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthServiceProxy authServiceProxy;

    private final String SERVER_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        // Inicializamos nuestro servicio inyectando el mock del RestTemplate y una URL ficticia
        authServiceProxy = new AuthServiceProxy(restTemplate, SERVER_URL);
    }

    // ==========================================
    // Login
    // ==========================================
    @Nested
    class Login {

        @Test
        void login_Success_ReturnsTrueAndSetsToken() {
            // Arrange
            UserCredentialsDTO credentials = new UserCredentialsDTO("test@test.com", "user", "pass");
            ResponseEntity<String> response = new ResponseEntity<>("TOKEN_VALIDO_123", HttpStatus.OK);

            when(restTemplate.postForEntity(eq(SERVER_URL + "/auth/login"), eq(credentials), eq(String.class)))
                    .thenReturn(response);

            // Act
            boolean result = authServiceProxy.login(credentials);

            // Assert
            assertTrue(result);
            assertEquals("TOKEN_VALIDO_123", authServiceProxy.getToken(), "El token debería haberse guardado");
        }

        @Test
        void login_HttpError_ReturnsFalse() {
            // Arrange
            UserCredentialsDTO credentials = new UserCredentialsDTO("test@test.com", "user", "pass");

            // Simulamos que el servidor devuelve un 401 Unauthorized
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            // Act
            boolean result = authServiceProxy.login(credentials);

            // Assert
            assertFalse(result);
            assertNull(authServiceProxy.getToken(), "El token debe seguir siendo null tras un fallo");
        }
    }

    // ==========================================
    // Logout
    // ==========================================
    @Nested
    class Logout {

        @Test
        void logout_WithoutToken_ReturnsFalse() {
            // Act
            boolean result = authServiceProxy.logout();

            // Assert
            assertFalse(result);
            // Comprobamos que ni siquiera intentó comunicarse con el servidor
            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        }

        @Test
        void logout_WithToken_Success_ClearsTokenAndReturnsTrue() {
            // Arrange
            ReflectionTestUtils.setField(authServiceProxy, "token", "MI_TOKEN");
            ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.postForEntity(eq(SERVER_URL + "/auth/logout"), any(HttpEntity.class), eq(Void.class)))
                    .thenReturn(response);

            // Act
            boolean result = authServiceProxy.logout();

            // Assert
            assertTrue(result);
            assertNull(authServiceProxy.getToken(), "El token debió borrarse tras el logout");
        }

        @Test
        void logout_WithToken_ServerFails_ReturnsFalse() {
            // Arrange
            ReflectionTestUtils.setField(authServiceProxy, "token", "MI_TOKEN");

            when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            // Act
            boolean result = authServiceProxy.logout();

            // Assert
            assertFalse(result);
            assertEquals("MI_TOKEN", authServiceProxy.getToken(), "Si falla el servidor, conservamos el token");
        }
    }

    // ==========================================
    // Register
    // ==========================================
    @Nested
    class Register {

        @Test
        void register_Success_ReturnsTrue() {
            UserCredentialsDTO credentials = new UserCredentialsDTO("new@test.com", "user", "pass");
            ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.CREATED);

            when(restTemplate.postForEntity(eq(SERVER_URL + "/auth/register"), eq(credentials), eq(Void.class)))
                    .thenReturn(response);

            boolean result = authServiceProxy.register(credentials);

            assertTrue(result);
        }

        @Test
        void register_ConflictError_ReturnsFalse() {
            UserCredentialsDTO credentials = new UserCredentialsDTO("exist@test.com", "user", "pass");

            when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

            boolean result = authServiceProxy.register(credentials);

            assertFalse(result);
        }
    }

    // ==========================================
    // Validate Session
    // ==========================================
    @Nested
    class ValidateSession {

        @Test
        void validateSession_NoToken_ReturnsFalse() {
            boolean result = authServiceProxy.validateSession();

            assertFalse(result);
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Void.class));
        }

        @Test
        void validateSession_WithToken_Valid_ReturnsTrue() {
            // Arrange
            ReflectionTestUtils.setField(authServiceProxy, "token", "TOKEN_ACTIVO");
            ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);

            when(restTemplate.exchange(eq(SERVER_URL + "/auth/validate"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Void.class)))
                    .thenReturn(response);

            // Act
            boolean result = authServiceProxy.validateSession();

            // Assert
            assertTrue(result);
        }

        @Test
        void validateSession_WithToken_Invalid_ReturnsFalse() {
            ReflectionTestUtils.setField(authServiceProxy, "token", "TOKEN_CADUCADO");

            when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            boolean result = authServiceProxy.validateSession();

            assertFalse(result);
        }
    }

    // ==========================================
    // Create Auth Entity Helper
    // ==========================================
    @Nested
    class CreateAuthEntity {

        @Test
        void createAuthEntity_WithToken_IncludesBearerHeader() {
            ReflectionTestUtils.setField(authServiceProxy, "token", "SUPER_SECRET");

            HttpEntity<?> entity = authServiceProxy.createAuthEntity();

            HttpHeaders headers = entity.getHeaders();
            assertTrue(headers.containsKey(HttpHeaders.AUTHORIZATION));
            assertEquals("Bearer SUPER_SECRET", headers.getFirst(HttpHeaders.AUTHORIZATION));
        }

        @Test
        void createAuthEntity_WithoutToken_DoesNotIncludeBearerHeader() {
            HttpEntity<?> entity = authServiceProxy.createAuthEntity();

            HttpHeaders headers = entity.getHeaders();
            assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        }
    }
}