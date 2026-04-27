package client.service;

import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthServiceProxy authService;

    private PostServiceProxy postServiceProxy;

    @BeforeEach
    void setUp() throws Exception {
        postServiceProxy = new PostServiceProxy(restTemplate, authService, "http://localhost:8080");
    }

    // ==========================================
    // Tests para getPostsByThread
    // ==========================================

    @Test
    void getPostsByThread_ReturnsPosts() {
        PostDTO post = new PostDTO(1, "Post 1", "Contenido", "TestUser", 1, null, 5, 2, Collections.emptyList());
        ResponseEntity<List<PostDTO>> response = ResponseEntity.ok(List.of(post));

        when(restTemplate.exchange(
                eq("http://localhost:8080/api/posts/thread/1"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<PostDTO> result = postServiceProxy.getPostsByThread(1);

        assertEquals(1, result.size());
        assertEquals("Post 1", result.get(0).title());
        assertEquals("TestUser", result.get(0).ownerUsername());
    }

    @Test
    void getPostsByThread_NullBody_ReturnsEmptyList() {
        ResponseEntity<List<PostDTO>> response = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<PostDTO> result = postServiceProxy.getPostsByThread(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostsByThread_Exception_ReturnsEmptyList() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection refused"));

        List<PostDTO> result = postServiceProxy.getPostsByThread(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostsByThread_EmptyList_ReturnsEmptyList() {
        ResponseEntity<List<PostDTO>> response = ResponseEntity.ok(Collections.emptyList());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<PostDTO> result = postServiceProxy.getPostsByThread(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostsByThread_MultiplePosts_ReturnsAll() {
        PostDTO post1 = new PostDTO(1, "Post 1", "Contenido 1", "User1", 1, null, 0, 0, Collections.emptyList());
        PostDTO post2 = new PostDTO(2, "Post 2", "Contenido 2", "User2", 1, null, 0, 0, Collections.emptyList());
        ResponseEntity<List<PostDTO>> response = ResponseEntity.ok(List.of(post1, post2));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<PostDTO> result = postServiceProxy.getPostsByThread(1);

        assertEquals(2, result.size());
    }

    // ==========================================
    // Tests para createPost
    // ==========================================

    @Test
    void createPost_ValidRequest_ReturnsCreatedPost() {
        CreatePostDTO dto = new CreatePostDTO("Nuevo post", "Contenido", 1, null);
        PostDTO created = new PostDTO(10, "Nuevo post", "Contenido", "TestUser", 1, null, 0, 0, Collections.emptyList());

        when(authService.getToken()).thenReturn("valid-token");
        when(restTemplate.exchange(
                eq("http://localhost:8080/api/posts/create"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PostDTO.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(created));

        PostDTO result = postServiceProxy.createPost(dto);

        assertNotNull(result);
        assertEquals(10, result.id());
        assertEquals("Nuevo post", result.title());
    }

    @Test
    void createPost_NoToken_ReturnsNull() {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);

        when(authService.getToken()).thenReturn(null);

        PostDTO result = postServiceProxy.createPost(dto);

        assertNull(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(PostDTO.class));
    }

    @Test
    void createPost_Exception_ReturnsNull() {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);

        when(authService.getToken()).thenReturn("valid-token");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PostDTO.class)
        )).thenThrow(new RestClientException("Server error"));

        PostDTO result = postServiceProxy.createPost(dto);

        assertNull(result);
    }

    @Test
    void createPost_WithParent_ReturnsPost() {
        CreatePostDTO dto = new CreatePostDTO("Respuesta", "Contenido", 1, 5);
        PostDTO created = new PostDTO(11, "Respuesta", "Contenido", "TestUser", 1, 5, 0, 0, Collections.emptyList());

        when(authService.getToken()).thenReturn("valid-token");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PostDTO.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(created));

        PostDTO result = postServiceProxy.createPost(dto);

        assertNotNull(result);
        assertEquals(5, result.parentId());
    }

    @Test
    void createPost_SendsBearerToken() {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);
        PostDTO created = new PostDTO(10, "Post", "Contenido", "TestUser", 1, null, 0, 0, Collections.emptyList());

        when(authService.getToken()).thenReturn("my-secret-token");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PostDTO.class)
        )).thenReturn(ResponseEntity.ok(created));

        postServiceProxy.createPost(dto);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    String auth = httpEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    return auth != null && auth.equals("Bearer my-secret-token");
                }),
                eq(PostDTO.class)
        );
    }
}