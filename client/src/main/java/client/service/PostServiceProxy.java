package client.service;

import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class PostServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;
    private final AuthServiceProxy authService;

    public PostServiceProxy(RestTemplate restTemplate,
                            AuthServiceProxy authService,
                            @Value("${server.api.url}") String serverApiUrl) {
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.serverApiUrl = serverApiUrl;
    }

    public List<PostDTO> getPostsByThread(Integer threadId) {
        try {
            String url = serverApiUrl + "/api/posts/thread/" + threadId;
            List<PostDTO> body = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PostDTO>>() {}
            ).getBody();
            return body != null ? body : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public PostDTO createPost(CreatePostDTO dto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = authService.getToken();
            if (token == null) return null;
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreatePostDTO> request = new HttpEntity<>(dto, headers);

            return restTemplate.exchange(
                    serverApiUrl + "/api/posts/create",
                    HttpMethod.POST,
                    request,
                    PostDTO.class
            ).getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
