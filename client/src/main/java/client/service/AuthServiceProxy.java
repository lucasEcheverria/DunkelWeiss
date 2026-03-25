package client.service;

import client.config.AppConfig;
import lib.dto.UserCredentialsDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio de autenticación (proxy hacia el servidor).
 * Guarda el token devuelto por POST `/auth/login`, lo usa en logout
 * y ofrece helper para llamadas autenticadas.
 */
@Service
public class AuthServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;

    // Token almacenado tras un inicio de sesión exitoso
    private String token;

    public AuthServiceProxy(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
    }

    public boolean login(UserCredentialsDto credentials) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                serverApiUrl + "/auth/login",
                credentials,
                String.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.token = response.getBody();
                return true;
            }
            return false;
        } catch (HttpClientErrorException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean logout() {
        if (this.token == null) {
            return false;
        }
        try {
            // El servidor espera el token en el cuerpo de la petición (texto plano)
            HttpEntity<String> request = new HttpEntity<>(this.token);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                serverApiUrl + "/auth/logout",
                request,
                Void.class
            );
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                this.token = null; // limpia el token de sesión local
                return true;
            }
            return false;
        } catch (HttpClientErrorException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método auxiliar para crear un HttpEntity con el encabezado Authorization: Bearer <token>
     * para ser utilizado en peticiones autenticadas posteriores.
     */
    public HttpEntity<?> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        if (this.token != null) {
            headers.setBearerAuth(this.token);
        }
        return new HttpEntity<>(headers);
    }

    public String getToken() {
        return this.token;
    }

    public boolean register(UserCredentialsDto credentials) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                serverApiUrl + "/auth/register",
                credentials,
                Void.class
            );
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (HttpClientErrorException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}