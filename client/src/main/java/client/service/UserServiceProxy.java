package client.service;

import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Proxy client-to-server para operaciones relacionados con usuarios.
 */
@Service
public class UserServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;
    private final AuthServiceProxy authService;

    public UserServiceProxy(RestTemplate restTemplate,
                            AuthServiceProxy authService,
                            @Value("${server.api.url}") String serverApiUrl) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = serverApiUrl;
        this.authService = authService;
    }

    /**
     * Obtiene la información del usuario enviando el token en el Authorization header (Bearer),
     */
    public UserDTO getCurrentUser(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    serverApiUrl + "/api/users/me",
                    HttpMethod.GET,
                    request,
                    UserDTO.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (HttpClientErrorException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Actualiza nickname/password.
     */
    public UserDTO updateUser(UpdateUserDTO dto) {
        if (dto == null) return null;

        String token = authService.getToken();
        if (token == null || token.isBlank()) return null;

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("nickname", dto.getNickname());
            body.put("password", dto.getPassword());

            // include Authorization: Bearer <token> header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    serverApiUrl + "/api/users/me/update",
                    HttpMethod.PUT,
                    request,
                    UserDTO.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (HttpClientErrorException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}