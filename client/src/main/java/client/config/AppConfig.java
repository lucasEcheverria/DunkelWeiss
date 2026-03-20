package client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración general de la aplicación cliente.
 *
 * Aplica el patrón Facade: expone un bean RestTemplate centralizado
 * que oculta la complejidad de las llamadas HTTP al resto de la aplicación.
 */
@Configuration
public class AppConfig {

    /** URL base del servidor REST (leída desde application.properties). */
    @Value("${server.api.url}")
    private String serverApiUrl;

    /**
     * Crea y registra un bean RestTemplate reutilizable.
     * Todos los servicios que necesiten llamar al servidor lo inyectarán.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /** Devuelve la URL base del servidor para que los servicios la usen. */
    public String getServerApiUrl() {
        return serverApiUrl;
    }
}
