package server.service;

import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.PostRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Community;
import server.entity.Post;
import server.entity.Thread;
import server.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private Thread thread;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "TestUser", "1234");
        user.setId(1);

        thread = new Thread();
        thread.setId(1);
        thread.setTitle("Hilo de prueba");
        thread.setDescription("Descripción de prueba");
    }

    // ==========================================
    // Tests para getPostsByThread
    // ==========================================

    @Test
    void getPostsByThread_ThreadExists_ReturnsPosts() {
        Post post = createPost(1, "Post 1", "Contenido 1", user, thread, null);

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(List.of(post));

        List<PostDTO> result = postService.getPostsByThread(1);

        assertEquals(1, result.size());
        assertEquals("Post 1", result.get(0).title());
        assertEquals("Contenido 1", result.get(0).content());
        assertEquals("TestUser", result.get(0).ownerUsername());
        assertEquals(1, result.get(0).threadId());
        assertNull(result.get(0).parentId());

        verify(threadRepository).findById(1);
        verify(postRepository).findRootPostsByThreadId(1);
    }

    @Test
    void getPostsByThread_ThreadExists_NoPosts_ReturnsEmptyList() {
        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(Collections.emptyList());

        List<PostDTO> result = postService.getPostsByThread(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostsByThread_ThreadExists_NullPosts_ReturnsEmptyList() {
        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(null);

        List<PostDTO> result = postService.getPostsByThread(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostsByThread_ThreadNotFound_ThrowsException() {
        when(threadRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsByThread(99)
        );
        assertEquals("Thread no encontrado: 99", ex.getMessage());
    }

    @Test
    void getPostsByThread_WithReplies_ReturnsNestedStructure() {
        Post parent = createPost(1, "Post padre", "Contenido padre", user, thread, null);
        Post reply = createPost(2, "Respuesta", "Contenido respuesta", user, thread, parent);
        parent.setReplies(List.of(reply));
        reply.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(List.of(parent));

        List<PostDTO> result = postService.getPostsByThread(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).replies().size());
        assertEquals("Respuesta", result.get(0).replies().get(0).title());
        assertEquals(1, result.get(0).replies().get(0).parentId());
    }

    @Test
    void getPostsByThread_MultipleRootPosts_ReturnsAll() {
        Post post1 = createPost(1, "Post 1", "Contenido 1", user, thread, null);
        Post post2 = createPost(2, "Post 2", "Contenido 2", user, thread, null);
        Post post3 = createPost(3, "Post 3", "Contenido 3", user, thread, null);
        post1.setReplies(Collections.emptyList());
        post2.setReplies(Collections.emptyList());
        post3.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(List.of(post1, post2, post3));

        List<PostDTO> result = postService.getPostsByThread(1);

        assertEquals(3, result.size());
    }

    // ==========================================
    // Tests para createPost
    // ==========================================

    @Test
    void createPost_ValidPost_ReturnsCreatedPost() {
        CreatePostDTO dto = new CreatePostDTO("Nuevo post", "Contenido nuevo", 1, null);
        Post saved = createPost(10, "Nuevo post", "Contenido nuevo", user, thread, null);
        saved.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDTO result = postService.createPost(dto, user);

        assertEquals(10, result.id());
        assertEquals("Nuevo post", result.title());
        assertEquals("Contenido nuevo", result.content());
        assertEquals("TestUser", result.ownerUsername());
        assertEquals(1, result.threadId());
        assertNull(result.parentId());
        assertEquals(0, result.likes());
        assertEquals(0, result.dislikes());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_WithParent_ReturnsPostWithParentId() {
        Post parent = createPost(1, "Padre", "Contenido padre", user, thread, null);
        CreatePostDTO dto = new CreatePostDTO("Respuesta", "Contenido respuesta", 1, 1);
        Post saved = createPost(11, "Respuesta", "Contenido respuesta", user, thread, parent);
        saved.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findById(1)).thenReturn(Optional.of(parent));
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDTO result = postService.createPost(dto, user);

        assertEquals(11, result.id());
        assertEquals(1, result.parentId());
        assertEquals("Respuesta", result.title());
    }

    @Test
    void createPost_ThreadNotFound_ThrowsException() {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 99, null);

        when(threadRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(dto, user)
        );
        assertEquals("Thread no encontrado: 99", ex.getMessage());
    }

    @Test
    void createPost_ParentNotFound_ThrowsException() {
        CreatePostDTO dto = new CreatePostDTO("Respuesta", "Contenido", 1, 99);

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(dto, user)
        );
        assertEquals("Parent post no encontrado: 99", ex.getMessage());
    }

    @Test
    void createPost_SetsLikesAndDislikesToZero() {
        CreatePostDTO dto = new CreatePostDTO("Post", "Contenido", 1, null);
        Post saved = createPost(12, "Post", "Contenido", user, thread, null);
        saved.setLikes(0);
        saved.setDislikes(0);
        saved.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDTO result = postService.createPost(dto, user);

        assertEquals(0, result.likes());
        assertEquals(0, result.dislikes());
    }

    // ==========================================
    // Helper
    // ==========================================

    private Post createPost(Integer id, String title, String content, User owner, Thread thread, Post parent) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setOwner(owner);
        post.setThread(thread);
        post.setParentPost(parent);
        post.setLikes(0);
        post.setDislikes(0);
        return post;
    }

    @Test
    void getPostsByThread_PostWithNullOwner_ReturnsNullUsername() {
        Post post = createPost(1, "Post", "Contenido", null, thread, null);
        post.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(List.of(post));

        List<PostDTO> result = postService.getPostsByThread(1);

        assertEquals(1, result.size());
        assertNull(result.get(0).ownerUsername());
    }

    @Test
    void getPostsByThread_PostWithNullThread_ReturnsNullThreadId() {
        Post post = createPost(1, "Post", "Contenido", user, null, null);
        post.setReplies(Collections.emptyList());

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(postRepository.findRootPostsByThreadId(1)).thenReturn(List.of(post));

        List<PostDTO> result = postService.getPostsByThread(1);

        assertEquals(1, result.size());
        assertNull(result.get(0).threadId());
    }
}