package client.service;

import client.config.AppConfig;
import lib.dto.UserCredentialsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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

    public AuthServiceProxy(RestTemplate restTemplate,
                            @Value("${server.api.url}") String serverApiUrl) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = serverApiUrl;
    }

    public boolean login(UserCredentialsDTO credentials) {
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

    public boolean register(UserCredentialsDTO credentials) {
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

    public boolean validateSession() {
        if (this.token == null) return false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(this.token);
            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<Void> response = restTemplate.exchange(
                    serverApiUrl + "/auth/validate",
                    HttpMethod.GET,
                    request,
                    Void.class
            );
            System.out.println("VALIDATE STATUS: " + response.getStatusCode());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            System.out.println("VALIDATE ERROR: " + e.getStatusCode());
            return false;
        } catch (Exception e) {
            System.out.println("VALIDATE EXCEPTION: " + e.getMessage());
            return false;
        }
    }
}