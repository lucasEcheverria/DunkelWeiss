package client.service;

import client.config.AppConfig;
import lib.dto.UserCredentialsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio de autenticación (patrón Service Layer).
 *
 * Centraliza toda la lógica de comunicación con la API del servidor
 * para operaciones de login y registro, manteniendo los controladores limpios.
 */
@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;

    public AuthService(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
    }

    /**
     * Intenta autenticar al usuario contra el servidor.
     *
     * Llama a POST /api/auth/login con las credenciales proporcionadas.
     * Retorna true si el servidor responde con 200 OK (credenciales válidas).
     * Retorna false si el servidor responde con 401 Unauthorized (credenciales incorrectas)
     * o si ocurre cualquier error de comunicación.
     *
     * @param credentials DTO con username y password del usuario
     * @return true si el login fue exitoso, false en caso contrario
     */
    public boolean login(UserCredentialsDto credentials) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                serverApiUrl + "/api/auth/login",
                credentials,
                Void.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            // El servidor respondió con 4xx (ej. 401 Unauthorized)
            return false;
        } catch (Exception e) {
            // Error de red u otro error inesperado
            return false;
        }
    }

    /**
     * Registra un nuevo usuario en el servidor.
     *
     * Llama a POST /api/auth/register con los datos del nuevo usuario.
     * Retorna true si el servidor responde con 201 Created (registro exitoso).
     * Retorna false si el servidor responde con 409 Conflict (usuario ya existe)
     * o si ocurre cualquier error de comunicación.
     *
     * @param credentials DTO con username y password del nuevo usuario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    public boolean register(UserCredentialsDto credentials) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                serverApiUrl + "/api/auth/register",
                credentials,
                Void.class
            );
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (HttpClientErrorException e) {
            // El servidor respondió con 4xx (ej. 409 Conflict: usuario ya existe)
            return false;
        } catch (Exception e) {
            // Error de red u otro error inesperado
            return false;
        }
    }
}
