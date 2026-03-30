package client.service;

import client.config.AppConfig;
import lib.dto.ThreadDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de búsqueda de hilos (patrón Service Layer).
 *
 * Centraliza toda la lógica de comunicación con la API del servidor
 * para operaciones relacionadas con hilos.
 */
@Service
public class HiloServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;

    public HiloServiceProxy(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
    }

    /**
     * Busca hilos por título o descripción contra el servidor.
     *
     * Llama a GET /api/hilos/search?q={query}.
     * Retorna una lista vacía si no hay resultados o si ocurre un error.
     *
     * @param query texto a buscar
     * @return lista de hilos que coinciden con la búsqueda
     */
    public List<ThreadDTO> buscarHilos(String query) {
        try {
            ThreadDTO[] resultado = restTemplate.getForObject(
                    serverApiUrl + "/api/hilos/search?q=" + query,
                    ThreadDTO[].class
            );
            return resultado != null ? Arrays.asList(resultado) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}