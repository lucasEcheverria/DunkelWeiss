package server.service;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;

import lib.dto.CreatePostDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.PostRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Post;
import server.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JUnitPerfInterceptor.class) // ESTO es lo que hace que JUnitPerf funcione en JUnit 5
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostServicePerfTest {

    // Configuración del reporte HTML adaptada a JUnit 5
    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("build/reports/junitperf/report.html"))
            .build();

    @Mock
    private PostRepository postRepository;

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private server.entity.Thread thread;
    private User user;
    private Post rootPost;
    private Post replyPost;

    @BeforeAll
    public void globalSetup() {
        // Inicializa los mocks
        MockitoAnnotations.openMocks(this);

        user = new User("perf@test.com", "PerfUser", "1234");
        user.setId(1);

        thread = new server.entity.Thread();
        thread.setId(1);

        rootPost = new Post();
        rootPost.setId(1);

        server.entity.Thread emptyThread = new server.entity.Thread();
        emptyThread.setId(2);
        emptyThread.setTitle("Hilo Vacío");

        // STUBs
        lenient().doReturn(Optional.of(emptyThread)).when(threadRepository).findById(eq(2));
        lenient().doReturn(Collections.emptyList()).when(postRepository).findRootPostsByThreadId(eq(2));
        lenient().doReturn(Optional.of(thread)).when(threadRepository).findById(eq(1));
        lenient().doReturn(List.of(rootPost)).when(postRepository).findRootPostsByThreadId(eq(1));
        lenient().doReturn(Optional.of(rootPost)).when(postRepository).findById(eq(1));

        lenient().doAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        }).when(postRepository).save(any(Post.class));
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, warmUpMs = 500)
    @JUnitPerfTestRequirement(meanLatency = 50, executionsPerSec = 100, allowedErrorPercentage = 0.0f)
    public void getPostsByThread_UnderLoad_MeetsLatencyRequirements() {
        postService.getPostsByThread(1);
    }

    @Test
    @JUnitPerfTest(threads = 20, durationMs = 3000, warmUpMs = 500)
    @JUnitPerfTestRequirement(meanLatency = 100, executionsPerSec = 50, allowedErrorPercentage = 0.0f)
    public void getPostsByThread_HighConcurrency_MeetsRequirements() {
        postService.getPostsByThread(1);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, warmUpMs = 300)
    @JUnitPerfTestRequirement(meanLatency = 10, executionsPerSec = 500, allowedErrorPercentage = 0.0f)
    public void getPostsByThread_EmptyResults_HighThroughput() {
        postService.getPostsByThread(2);
    }

    @Test // <-- IMPORTANTE: ¡Te faltaba esta anotación en este método!
    @JUnitPerfTest(threads = 10, durationMs = 2000, warmUpMs = 500)
    @JUnitPerfTestRequirement(meanLatency = 50, executionsPerSec = 100, allowedErrorPercentage = 0.0f)
    public void createPost_UnderLoad_MeetsLatencyRequirements() {
        CreatePostDTO dto = new CreatePostDTO("Post de perf", "Contenido de rendimiento", 1, null);
        postService.createPost(dto, user);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, warmUpMs = 500)
    @JUnitPerfTestRequirement(meanLatency = 50, executionsPerSec = 80, allowedErrorPercentage = 0.0f)
    public void createPostWithReply_UnderLoad_MeetsLatencyRequirements() {
        CreatePostDTO dto = new CreatePostDTO("Respuesta perf", "Contenido respuesta", 1, 1);
        postService.createPost(dto, user);
    }

    @Test
    @JUnitPerfTest(threads = 50, durationMs = 5000, warmUpMs = 1000)
    @JUnitPerfTestRequirement(meanLatency = 200, executionsPerSec = 20, allowedErrorPercentage = 0.01f)
    public void createPost_StressTest_HandlesHighLoad() {
        CreatePostDTO dto = new CreatePostDTO("Stress test", "Contenido stress", 1, null);
        postService.createPost(dto, user);
    }

    @Test
    public void debugTest() {
        // Ejecuta esto una sola vez sin carga para ver el error en la consola
        postService.getPostsByThread(2);
    }

    private Post createPost(Integer id, String title, String content, User owner,
                            server.entity.Thread thread, Post parent) {
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
}