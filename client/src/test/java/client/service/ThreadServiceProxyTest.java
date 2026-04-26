package client.service;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThreadServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthServiceProxy authService;

    private ThreadServiceProxy threadServiceProxy;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        threadServiceProxy = new ThreadServiceProxy(restTemplate, authService, BASE_URL);
    }

    // ==========================================
    // CreateThread
    // ==========================================

    @Nested
    class CreateThread {

        @Test
        void createThread_ReturnsThreadDTO() {
            ThreadDTO expected = new ThreadDTO(1, "titulo", "descripcion", "nickname", "General");
            CreateThreadDTO dto = new CreateThreadDTO("titulo", "descripcion", 1);

            when(authService.getToken()).thenReturn("token123");
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/create"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(ThreadDTO.class)
            )).thenReturn(ResponseEntity.ok(expected));

            ThreadDTO result = threadServiceProxy.createThread(dto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1);
            assertThat(result.title()).isEqualTo("titulo");
            assertThat(result.ownerUsername()).isEqualTo("nickname");
        }

        @Test
        void createThread_WhenExceptionThrown_ReturnsNull() {
            CreateThreadDTO dto = new CreateThreadDTO("titulo", "descripcion", 1);

            when(authService.getToken()).thenReturn("token123");
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/create"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(ThreadDTO.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            ThreadDTO result = threadServiceProxy.createThread(dto);

            assertThat(result).isNull();
        }
    }

    // ==========================================
    // GetThread
    // ==========================================

    @Nested
    class GetThread {

        @Test
        void getThread_ExistingId_ReturnsThreadDTO() {
            ThreadDTO expected = new ThreadDTO(5, "Mi hilo", "desc", "alice", "Tech");

            when(restTemplate.getForEntity(
                    BASE_URL + "/api/threads/get/5",
                    ThreadDTO.class
            )).thenReturn(ResponseEntity.ok(expected));

            ThreadDTO result = threadServiceProxy.getThread(5);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(5);
            assertThat(result.title()).isEqualTo("Mi hilo");
        }

        @Test
        void getThread_WhenExceptionThrown_ReturnsNull() {
            when(restTemplate.getForEntity(
                    BASE_URL + "/api/threads/get/404",
                    ThreadDTO.class
            )).thenThrow(new RuntimeException("Not found"));

            ThreadDTO result = threadServiceProxy.getThread(404);

            assertThat(result).isNull();
        }
    }

    // ==========================================
    // GetAllSummaries
    // ==========================================

    @Nested
    class GetAllSummaries {

        @Test
        void getAllSummaries_ReturnsList() {
            List<ThreadSummaryDTO> expected = List.of(
                    new ThreadSummaryDTO(1, "Hilo A", "Desc A", "bob"),
                    new ThreadSummaryDTO(2, "Hilo B", "Desc B", "bob")
            );

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/getAll"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(expected));

            List<ThreadSummaryDTO> result = threadServiceProxy.getAllSummaries();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).title()).isEqualTo("Hilo A");
        }

        @Test
        void getAllSummaries_WhenBodyIsNull_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/getAll"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            List<ThreadSummaryDTO> result = threadServiceProxy.getAllSummaries();

            assertThat(result).isEmpty();
        }

        @Test
        void getAllSummaries_WhenExceptionThrown_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/getAll"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            List<ThreadSummaryDTO> result = threadServiceProxy.getAllSummaries();

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // GetThreadsWithPrompt
    // ==========================================

    @Nested
    class GetThreadsWithPrompt {

        @Test
        void getThreadsWithPrompt_MatchingQuery_ReturnsList() {
            List<ThreadDTO> expected = List.of(
                    new ThreadDTO(3, "Java tips", "desc", "carol", "Tech")
            );

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/search?query=java"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(expected));

            List<ThreadDTO> result = threadServiceProxy.getThreadsWithPrompt("java");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).title()).isEqualTo("Java tips");
        }

        @Test
        void getThreadsWithPrompt_WhenBodyIsNull_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/search?query=xyz"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            List<ThreadDTO> result = threadServiceProxy.getThreadsWithPrompt("xyz");

            assertThat(result).isEmpty();
        }

        @Test
        void getThreadsWithPrompt_WhenExceptionThrown_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/search?query=fail"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            List<ThreadDTO> result = threadServiceProxy.getThreadsWithPrompt("fail");

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // GetThreadsFromUser
    // ==========================================

    @Nested
    class GetThreadsFromUser {

        @Test
        void getThreadsFromUser_ExistingEmail_ReturnsList() {
            List<ThreadDTO> expected = List.of(
                    new ThreadDTO(4, "Hilo de dave", "desc", "dave", "General")
            );

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/user?email=dave@d.com"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(expected));

            List<ThreadDTO> result = threadServiceProxy.getThreadsFromUser("dave@d.com");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).ownerUsername()).isEqualTo("dave");
        }

        @Test
        void getThreadsFromUser_WhenBodyIsNull_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/user?email=nobody@x.com"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            List<ThreadDTO> result = threadServiceProxy.getThreadsFromUser("nobody@x.com");

            assertThat(result).isEmpty();
        }

        @Test
        void getThreadsFromUser_WhenExceptionThrown_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/user?email=error@x.com"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            List<ThreadDTO> result = threadServiceProxy.getThreadsFromUser("error@x.com");

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // GetInitialFeed
    // ==========================================

    @Nested
    class GetInitialFeed {

        @Test
        void getInitialFeed_ReturnsMappedTopThreads() {
            List<ThreadSummaryDTO> expected = List.of(
                    new ThreadSummaryDTO(7, "Popular A", "desc A", "eve"),
                    new ThreadSummaryDTO(8, "Popular B", "desc B", "eve")
            );

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/thread_feed"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(expected));

            List<ThreadSummaryDTO> result = threadServiceProxy.getInitialFeed();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(7);
            assertThat(result.get(0).title()).isEqualTo("Popular A");
        }

        @Test
        void getInitialFeed_WhenBodyIsNull_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/thread_feed"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            List<ThreadSummaryDTO> result = threadServiceProxy.getInitialFeed();

            assertThat(result).isEmpty();
        }

        @Test
        void getInitialFeed_WhenExceptionThrown_ReturnsEmptyList() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/thread_feed"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            List<ThreadSummaryDTO> result = threadServiceProxy.getInitialFeed();

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // Favorites
    // ==========================================

    @Nested
    class Favorites {

        @Test
        void getFavoriteThreads_ReturnsList_whenLoggedIn() {
            List<ThreadDTO> expected = List.of(
                    new ThreadDTO(10, "Fav A", "d", "sam", "General")
            );

            when(authService.getToken()).thenReturn("tok");
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/favorites"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(expected));

            List<ThreadDTO> result = threadServiceProxy.getFavoriteThreads();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(10);
        }

        @Test
        void getFavoriteThreads_ReturnsEmpty_whenExceptionThrown() {
            when(authService.getToken()).thenReturn("tok");

            // Simulate RestTemplate throwing any runtime exception to hit the catch block
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/favorites"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("boom"));

            List<ThreadDTO> result = threadServiceProxy.getFavoriteThreads();

            assertThat(result).isEmpty();
        }

        @Test
        void getFavoriteThreads_ReturnsEmpty_whenBodyIsNull() {
            when(authService.getToken()).thenReturn("tok");

            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/favorites"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            List<ThreadDTO> result = threadServiceProxy.getFavoriteThreads();

            assertThat(result).isEmpty();
        }

        @Test
        void getFavoriteThreads_ReturnsEmpty_whenNoToken() {
            when(authService.getToken()).thenReturn(null);

            List<ThreadDTO> result = threadServiceProxy.getFavoriteThreads();

            assertThat(result).isEmpty();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void addFavorite_ReturnsTrue_whenSuccessful() {
            when(authService.getToken()).thenReturn("tok");
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/favorites/5"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Void.class)
            )).thenReturn(ResponseEntity.ok().build());

            boolean ok = threadServiceProxy.addFavorite(5);

            assertThat(ok).isTrue();
        }

        @Test
        void addFavorite_ReturnsFalse_whenNoToken() {
            when(authService.getToken()).thenReturn(null);

            boolean ok = threadServiceProxy.addFavorite(5);

            assertThat(ok).isFalse();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void addFavorite_ReturnsFalse_whenException() {
            when(authService.getToken()).thenReturn("tok");
            when(restTemplate.exchange(anyString(), any(), any(HttpEntity.class), eq(Void.class)))
                    .thenThrow(new RuntimeException("fail"));

            boolean ok = threadServiceProxy.addFavorite(5);

            assertThat(ok).isFalse();
        }

        @Test
        void removeFavorite_ReturnsTrue_whenSuccessful() {
            when(authService.getToken()).thenReturn("tok");
            when(restTemplate.exchange(
                    eq(BASE_URL + "/api/threads/favorites/5"),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Void.class)
            )).thenReturn(ResponseEntity.ok().build());

            boolean ok = threadServiceProxy.removeFavorite(5);

            assertThat(ok).isTrue();
        }

        @Test
        void removeFavorite_ReturnsFalse_whenNoToken() {
            when(authService.getToken()).thenReturn(null);

            boolean ok = threadServiceProxy.removeFavorite(5);

            assertThat(ok).isFalse();
            verifyNoInteractions(restTemplate);
        }

        @Test
        void removeFavorite_ReturnsFalse_whenException() {
            when(authService.getToken()).thenReturn("tok");
            when(restTemplate.exchange(anyString(), any(), any(HttpEntity.class), eq(Void.class)))
                    .thenThrow(new RuntimeException("fail"));

            boolean ok = threadServiceProxy.removeFavorite(5);

            assertThat(ok).isFalse();
        }
    }
}