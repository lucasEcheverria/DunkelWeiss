package client.service;

import lib.dto.CommunityDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
}