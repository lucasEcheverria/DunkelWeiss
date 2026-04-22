package client.service;

import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class CommunityServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;

    public CommunityServiceProxy(RestTemplate restTemplate,
                                 @Value("${server.api.url}") String serverApiUrl) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = serverApiUrl;
    }

    public List<CommunityDTO> getAll() {
        try {
            ResponseEntity<List<CommunityDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/communities",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public CommunityDTO createCommunity(CreateCommunityDTO dto, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<CreateCommunityDTO> request = new HttpEntity<>(dto, headers);

            ResponseEntity<CommunityDTO> response = restTemplate.postForEntity(
                    serverApiUrl + "/api/communities/create",
                    request,
                    CommunityDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public List<CommunityDTO> getTop5() {
        try {
            ResponseEntity<List<CommunityDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/communities/top5",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<CommunityDTO> getMyCommunities(String token) {
        try {
            // Preparamos las cabeceras con el token del usuario
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Hacemos la petición GET (cambia la ruta "/api/communities/my" por la tuya)
            ResponseEntity<List<CommunityDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/communities/my_communities",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList(); // Si falla (ej. error 404 porque no está hecho en el server aún), devuelve lista vacía
        }
    }

    public boolean leaveCommunity(String token, Integer communityId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    serverApiUrl + "/api/communities/leave/" + communityId,
                    HttpMethod.POST,
                    request,
                    Void.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}