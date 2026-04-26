package client.service;

import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthServiceProxy authService;

    private UserServiceProxy proxy;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        proxy = new UserServiceProxy(restTemplate, authService, BASE_URL);
    }

    @Nested
    class GetCurrentUser {

        @Test
        void returnsUser_whenServerRespondsOk() {
            UserDTO expected = new UserDTO(2, "a@a", "alice");

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me"),
                    eq(HttpMethod.GET),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.ok(expected));

            UserDTO result = proxy.getCurrentUser("tok");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2);
            assertThat(result.getEmail()).isEqualTo("a@a");
        }

        @Test
        void returnsNull_whenTokenIsNullOrBlank() {
            assertThat(proxy.getCurrentUser(null)).isNull();
            assertThat(proxy.getCurrentUser("")).isNull();
            assertThat(proxy.getCurrentUser("   ")).isNull();
        }

        @Test
        void returnsNull_whenServerReturnsNonOk() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me"),
                    eq(HttpMethod.GET),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

            assertThat(proxy.getCurrentUser("tok")).isNull();
        }

        @Test
        void returnsNull_whenServerReturnsNullBody() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me"),
                    eq(HttpMethod.GET),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.ok(null));

            assertThat(proxy.getCurrentUser("tok")).isNull();
        }

        @Test
        void returnsNull_whenServerThrowsHttpClientErrorException() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(UserDTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            assertThat(proxy.getCurrentUser("tok")).isNull();
        }

        @Test
        void returnsNull_whenServerThrowsException() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(UserDTO.class)))
                    .thenThrow(new RuntimeException("error"));

            assertThat(proxy.getCurrentUser("tok")).isNull();
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void returnsNull_whenDtoIsNull() {
            assertThat(proxy.updateUser(null)).isNull();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void returnsNull_whenNoTokenAvailable() {
            when(authService.getToken()).thenReturn(null);

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void returnsNull_whenTokenIsBlank() {
            when(authService.getToken()).thenReturn("   ");

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void returnsUser_whenServerRespondsOk() {
            when(authService.getToken()).thenReturn("tok");
            UserDTO expected = new UserDTO(3, "b@b", "bob");

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me/update"),
                    eq(HttpMethod.PUT),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.ok(expected));

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            UserDTO result = proxy.updateUser(dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3);
            assertThat(result.getUsername()).isEqualTo("bob");
        }

        @Test
        void returnsNull_whenServerReturnsNullBody() {
            when(authService.getToken()).thenReturn("tok");

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me/update"),
                    eq(HttpMethod.PUT),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.ok(null));

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
        }

        @Test
        void returnsNull_whenServerReturnsNonOk() {
            when(authService.getToken()).thenReturn("tok");

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/users/me/update"),
                    eq(HttpMethod.PUT),
                    any(),
                    eq(UserDTO.class)
            )).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
        }

        @Test
        void returnsNull_whenServerThrowsHttpClientErrorException() {
            when(authService.getToken()).thenReturn("tok");

            when(restTemplate.exchange(anyString(), any(), any(), eq(UserDTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
        }

        @Test
        void returnsNull_whenServerThrowsException() {
            when(authService.getToken()).thenReturn("tok");

            when(restTemplate.exchange(anyString(), any(), any(), eq(UserDTO.class)))
                    .thenThrow(new RuntimeException("error"));

            UpdateUserDTO dto = new UpdateUserDTO("nick", "pass");
            assertThat(proxy.updateUser(dto)).isNull();
        }
    }
}