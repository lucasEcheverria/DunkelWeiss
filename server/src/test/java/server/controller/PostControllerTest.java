package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server.entity.User;
import server.service.AuthService;
import server.service.PostService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
        objectMapper = new ObjectMapper();

        user = new User("test@test.com", "TestUser", "1234");
        user.setId(1);
    }

    // ==========================================
    // Tests para GET /api/posts/thread/{threadId}
    // ==========================================

    @Test
    void getPostsByThread_ReturnsOkWithPosts() throws Exception {
        PostDTO post = new PostDTO(1, "Post 1", "Contenido", "TestUser", 1, null, 5, 2, Collections.emptyList());

        when(postService.getPostsByThread(1)).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Post 1"))
                .andExpect(jsonPath("$[0].content").value("Contenido"))
                .andExpect(jsonPath("$[0].ownerUsername").value("TestUser"))
                .andExpect(jsonPath("$[0].likes").value(5))
                .andExpect(jsonPath("$[0].dislikes").value(2));

        verify(postService).getPostsByThread(1);
    }

    @Test
    void getPostsByThread_EmptyList_ReturnsOk() throws Exception {
        when(postService.getPostsByThread(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/posts/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getPostsByThread_ThreadNotFound_Returns404() throws Exception {
        when(postService.getPostsByThread(99))
                .thenThrow(new IllegalArgumentException("Thread no encontrado: 99"));

        mockMvc.perform(get("/api/posts/thread/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Thread no encontrado: 99"));
    }

    // ==========================================
    // Tests para POST /api/posts/create
    // ==========================================

    @Test
    void createPost_ValidRequest_Returns201() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Nuevo post", "Contenido", 1, null);
        PostDTO created = new PostDTO(10, "Nuevo post", "Contenido", "TestUser", 1, null, 0, 0, Collections.emptyList());

        when(authService.getUserByToken("valid-token")).thenReturn(user);
        when(postService.createPost(any(CreatePostDTO.class), eq(user))).thenReturn(created);

        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Nuevo post"))
                .andExpect(jsonPath("$.ownerUsername").value("TestUser"));
    }

    @Test
    void createPost_NoAuthHeader_Returns401() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_InvalidAuthFormat_Returns401() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);

        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", "Basic invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_InvalidToken_Returns403() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);

        when(authService.getUserByToken("bad-token")).thenReturn(null);

        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", "Bearer bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_ThreadNotFound_Returns400() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 99, null);

        when(authService.getUserByToken("valid-token")).thenReturn(user);
        when(postService.createPost(any(CreatePostDTO.class), eq(user)))
                .thenThrow(new IllegalArgumentException("Thread no encontrado: 99"));

        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Thread no encontrado: 99"));
    }

    @Test
    void createPost_WithParent_Returns201() throws Exception {
        CreatePostDTO dto = new CreatePostDTO("Respuesta", "Contenido", 1, 5);
        PostDTO created = new PostDTO(11, "Respuesta", "Contenido", "TestUser", 1, 5, 0, 0, Collections.emptyList());

        when(authService.getUserByToken("valid-token")).thenReturn(user);
        when(postService.createPost(any(CreatePostDTO.class), eq(user))).thenReturn(created);

        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(5));
    }
}