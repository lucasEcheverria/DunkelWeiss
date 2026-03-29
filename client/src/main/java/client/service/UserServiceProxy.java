package client.service;

import client.config.AppConfig;
import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    public UserServiceProxy(RestTemplate restTemplate, AppConfig appConfig, AuthServiceProxy authService) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
        this.authService = authService;
    }

    /**
     * Obtiene la información del usuario enviando el token en el body (texto plano),
     * tal y como espera el servidor en POST /api/users/me.
     */
    public UserDTO getCurrentUser(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> request = new HttpEntity<>(token, headers);

            ResponseEntity<UserDTO> response = restTemplate.postForEntity(
                    serverApiUrl + "/api/users/me",
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
     * Actualiza nickname/password. The token is taken from AuthServiceProxy and
     * a small JSON payload { token, nickname, password } is sent to the server.
     */
    public UserDTO updateUser(UpdateUserDTO dto) {
        if (dto == null) return null;

        String token = authService.getToken();
        if (token == null || token.isBlank()) return null;

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("nickname", dto.getNickname());
            body.put("password", dto.getPassword());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    serverApiUrl + "/api/users/me/update",
                    org.springframework.http.HttpMethod.PUT,
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