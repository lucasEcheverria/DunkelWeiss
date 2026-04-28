package server.service;

import lib.dto.CreatePostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.PostRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Post;
import server.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class PostServicePerfTest {

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

    @BeforeEach
    public void setUp() {
        user = new User("perf@test.com", "PerfUser", "1234");
        user.setId(1);

        thread = new server.entity.Thread();
        thread.setId(1);
        thread.setTitle("Hilo de rendimiento");
        thread.setDescription("Test de performance");

        rootPost = createPost(1, "Post raíz", "Contenido del post raíz", user, thread, null);
        replyPost = createPost(2, "Respuesta", "Contenido de respuesta", user, thread, rootPost);
        rootPost.setReplies(List.of(replyPost));
        replyPost.setReplies(Collections.emptyList());

        lenient().when(threadRepository.findById(eq(1))).thenReturn(Optional.of(thread));
        lenient().when(postRepository.findRootPostsByThreadId(eq(1))).thenReturn(List.of(rootPost));
        lenient().when(postRepository.findById(eq(1))).thenReturn(Optional.of(rootPost));
        lenient().when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(100);
            saved.setReplies(Collections.emptyList());
            return saved;
        });
    }

    // ==========================================
    // Tests de rendimiento
    // ==========================================

    @Test
    public void getPostsByThread_UnderLoad_MeetsLatencyRequirements() {
        runPerformanceTest(
                () -> postService.getPostsByThread(1),
                10,     // threads
                2000,   // durationMs
                500,    // warmUpMs
                50.0,   // maxMeanLatencyMs
                100,    // minExecutionsPerSec
                0.0f    // allowedErrorPercentage
        );
    }

    @Test
    public void getPostsByThread_HighConcurrency_MeetsRequirements() {
        runPerformanceTest(
                () -> postService.getPostsByThread(1),
                20, 3000, 500, 100.0, 50, 0.0f
        );
    }

    @Test
    public void getPostsByThread_EmptyResults_HighThroughput() {
        lenient().when(postRepository.findRootPostsByThreadId(eq(1))).thenReturn(Collections.emptyList());
        runPerformanceTest(
                () -> postService.getPostsByThread(1),
                10, 2000, 300, 10.0, 500, 0.0f
        );
    }

    @Test
    public void createPost_UnderLoad_MeetsLatencyRequirements() {
        CreatePostDTO dto = new CreatePostDTO("Post de perf", "Contenido de rendimiento", 1, null);
        runPerformanceTest(
                () -> postService.createPost(dto, user),
                10, 2000, 500, 50.0, 100, 0.0f
        );
    }

    @Test
    public void createPost_StressTest_HandlesHighLoad() {
        CreatePostDTO dto = new CreatePostDTO("Stress test", "Contenido stress", 1, null);
        runPerformanceTest(
                () -> postService.createPost(dto, user),
                50, 5000, 1000, 200.0, 20, 0.01f
        );
    }

    // ==========================================
    // Motor de Pruebas de Rendimiento (Sustituto de JUnitPerf)
    // ==========================================

    private void runPerformanceTest(Runnable task, int threads, long durationMs, long warmUpMs,
                                    double maxMeanLatencyMs, int minExecutionsPerSec, float allowedErrorPercentage) {

        // 1. Fase de Calentamiento (Warm-up)
        long warmUpEnd = System.currentTimeMillis() + warmUpMs;
        while (System.currentTimeMillis() < warmUpEnd) {
            try { task.run(); } catch (Exception ignored) {}
        }

        // 2. Fase de Prueba Real
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalLatencyNs = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                while (System.currentTimeMillis() < endTime) {
                    long startOp = System.nanoTime();
                    try {
                        task.run();
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        totalLatencyNs.addAndGet(System.nanoTime() - startOp);
                    }
                }
            }, executor));
        }

        // Esperar a que todos los hilos terminen
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // 3. Cálculos y Aserciones
        int totalExecutions = successCount.get() + errorCount.get();
        double actualDurationSecs = durationMs / 1000.0;
        double executionsPerSec = totalExecutions / actualDurationSecs;
        double meanLatencyMs = totalExecutions > 0 ? (totalLatencyNs.get() / 1_000_000.0) / totalExecutions : 0;
        float errorPercentage = totalExecutions > 0 ? (float) errorCount.get() / totalExecutions : 0;

        System.out.printf("[PerfResult] Ops/sec: %.2f | Latencia Media: %.2fms | Errores: %.2f%%%n",
                executionsPerSec, meanLatencyMs, errorPercentage * 100);

        assertTrue(meanLatencyMs <= maxMeanLatencyMs,
                String.format("La latencia media (%.2fms) superó el máximo permitido (%.2fms)", meanLatencyMs, maxMeanLatencyMs));
        assertTrue(executionsPerSec >= minExecutionsPerSec,
                String.format("El throughput (%.2f ops/s) fue menor al mínimo requerido (%d ops/s)", executionsPerSec, minExecutionsPerSec));
        assertTrue(errorPercentage <= allowedErrorPercentage,
                String.format("El porcentaje de errores (%.2f%%) superó el permitido (%.2f%%)", errorPercentage * 100, allowedErrorPercentage * 100));
    }

    // ==========================================
    // Helper
    // ==========================================

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