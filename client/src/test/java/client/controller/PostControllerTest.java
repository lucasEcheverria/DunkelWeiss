package client.controller;

import client.service.AuthServiceProxy;
import client.service.PostServiceProxy;
import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostServiceProxy postService;

    @Mock
    private AuthServiceProxy authService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    // ==========================================
    // Tests para POST /posts/create
    // ==========================================

    @Test
    void createPost_Authenticated_RedirectsToThread() throws Exception {
        when(authService.getToken()).thenReturn("valid-token");
        when(postService.createPost(any(CreatePostDTO.class)))
                .thenReturn(new PostDTO(1, "Post", "Contenido", "User", 5, null, 0, 0, Collections.emptyList()));

        mockMvc.perform(post("/posts/create")
                        .param("title", "Mi post")
                        .param("content", "Contenido del post")
                        .param("threadId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/threads/5"));

        verify(postService).createPost(any(CreatePostDTO.class));
    }

    @Test
    void createPost_NotAuthenticated_RedirectsToAuth() throws Exception {
        when(authService.getToken()).thenReturn(null);

        mockMvc.perform(post("/posts/create")
                        .param("title", "Mi post")
                        .param("content", "Contenido")
                        .param("threadId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth"));

        verify(postService, never()).createPost(any());
    }

    @Test
    void createPost_WithParentId_RedirectsToThread() throws Exception {
        when(authService.getToken()).thenReturn("valid-token");
        when(postService.createPost(any(CreatePostDTO.class)))
                .thenReturn(new PostDTO(2, "Re:", "Respuesta", "User", 5, 1, 0, 0, Collections.emptyList()));

        mockMvc.perform(post("/posts/create")
                        .param("title", "Respuesta")
                        .param("content", "Contenido respuesta")
                        .param("threadId", "5")
                        .param("parentId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/threads/5"));
    }

    @Test
    void createPost_BlankTitle_UsesDefaultTitle() throws Exception {
        when(authService.getToken()).thenReturn("valid-token");
        when(postService.createPost(any(CreatePostDTO.class)))
                .thenReturn(new PostDTO(3, "Re:", "Contenido", "User", 5, 1, 0, 0, Collections.emptyList()));

        mockMvc.perform(post("/posts/create")
                        .param("title", "")
                        .param("content", "Contenido")
                        .param("threadId", "5")
                        .param("parentId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/threads/5"));

        verify(postService).createPost(argThat(dto -> dto.title().equals("Re:")));
    }

    @Test
    void createPost_NullTitle_UsesDefaultTitle() throws Exception {
        when(authService.getToken()).thenReturn("valid-token");
        when(postService.createPost(any(CreatePostDTO.class)))
                .thenReturn(new PostDTO(4, "Re:", "Contenido", "User", 5, null, 0, 0, Collections.emptyList()));

        mockMvc.perform(post("/posts/create")
                        .param("content", "Contenido")
                        .param("threadId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/threads/5"));

        verify(postService).createPost(argThat(dto -> dto.title().equals("Re:")));
    }

    @Test
    void createPost_RedirectsToCorrectThread() throws Exception {
        when(authService.getToken()).thenReturn("valid-token");
        when(postService.createPost(any(CreatePostDTO.class)))
                .thenReturn(new PostDTO(5, "Post", "Contenido", "User", 42, null, 0, 0, Collections.emptyList()));

        mockMvc.perform(post("/posts/create")
                        .param("title", "Post")
                        .param("content", "Contenido")
                        .param("threadId", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/threads/42"));
    }
}