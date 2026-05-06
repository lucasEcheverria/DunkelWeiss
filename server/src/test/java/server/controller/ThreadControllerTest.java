package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import server.entity.User;
import server.service.AuthService;
import server.service.ThreadService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThreadService threadService;

    @MockitoBean
    private AuthService authService;

    private User user;
    private final String token = "valid-token";
    private final String bearerToken = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        user = new User("email@email.com", "nickname", "password");
        user.setId(1);
    }

    // ==========================================
    // POST /api/threads/create
    // ==========================================

    @Nested
    class CreateThread {

        @Test
        void withValidToken_ReturnsCreatedAndBody() throws Exception {
            CreateThreadDTO dto = new CreateThreadDTO("title", "desc", 1);
            ThreadDTO threadDTO = new ThreadDTO(1, "title", "desc", "nickname", "community");

            when(authService.getUserByToken(token)).thenReturn(user);
            when(threadService.createThread(any(CreateThreadDTO.class), any(User.class))).thenReturn(threadDTO);

            mockMvc.perform(post("/api/threads/create")
                            .header("Authorization", bearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("title"))
                    .andExpect(jsonPath("$.description").value("desc"))
                    .andExpect(jsonPath("$.communityId").value("community"));
        }

        @Test
        void withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            CreateThreadDTO dto = new CreateThreadDTO("title", "desc", 1);

            mockMvc.perform(post("/api/threads/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void withMalformedToken_ReturnsUnauthorized() throws Exception {
            CreateThreadDTO dto = new CreateThreadDTO("title", "desc", 1);

            mockMvc.perform(post("/api/threads/create")
                            .header("Authorization", "InvalidTokenWithoutBearerPrefix")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void withExpiredOrInvalidToken_ReturnsForbidden() throws Exception {
            CreateThreadDTO dto = new CreateThreadDTO("title", "desc", 1);

            when(authService.getUserByToken(token)).thenReturn(null);

            mockMvc.perform(post("/api/threads/create")
                            .header("Authorization", bearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void withNonExistentCommunity_ReturnsBadRequest() throws Exception {
            CreateThreadDTO dto = new CreateThreadDTO("title", "desc", 99);

            when(authService.getUserByToken(token)).thenReturn(user);
            when(threadService.createThread(any(CreateThreadDTO.class), any(User.class)))
                    .thenThrow(new IllegalArgumentException("Community no encontrada: 99"));

            mockMvc.perform(post("/api/threads/create")
                            .header("Authorization", bearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Community no encontrada: 99"));
        }
    }

    // ==========================================
    // GET /api/threads/get/{id}
    // ==========================================

    @Nested
    class GetThread {

        @Test
        void withExistingId_ReturnsOkAndBody() throws Exception {
            ThreadDTO threadDTO = new ThreadDTO(1, "title", "desc", "nickname", "community");

            when(threadService.getThread(1)).thenReturn(threadDTO);

            mockMvc.perform(get("/api/threads/get/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("title"))
                    .andExpect(jsonPath("$.description").value("desc"))
                    .andExpect(jsonPath("$.ownerUsername").value("nickname"))
                    .andExpect(jsonPath("$.communityId").value("community"));
        }

        @Test
        void withNonExistentId_ReturnsNotFound() throws Exception {
            when(threadService.getThread(99))
                    .thenThrow(new IllegalArgumentException("Thread no encontrado: 99"));

            mockMvc.perform(get("/api/threads/get/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Thread no encontrado: 99"));
        }
    }

    // ==========================================
    // GET /api/threads/getAll
    // ==========================================

    @Nested
    class GetAllSummaries {

        @Test
        void withExistingThreads_ReturnsOkAndList() throws Exception {
            List<ThreadSummaryDTO> summaries = List.of(
                    new ThreadSummaryDTO(1, "title one", "desc one", "owner 1"),
                    new ThreadSummaryDTO(2, "title two", "desc one", "owner 1")
            );

            when(threadService.getAllSummaries()).thenReturn(summaries);

            mockMvc.perform(get("/api/threads/getAll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("title one"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("title two"));
        }

        @Test
        void withNoThreads_ReturnsOkAndEmptyList() throws Exception {
            when(threadService.getAllSummaries()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/threads/getAll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==========================================
    // GET /api/threads/search?query=
    // ==========================================

    @Nested
    class SearchThreads {

        @Test
        void withMatchingQuery_ReturnsOkAndResults() throws Exception {
            server.entity.Thread thread = buildThread(1, "Best IDE", "desc", user, buildCommunity("Tech"));

            when(threadService.getThreadsWithPrompt("IDE")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/search").param("query", "IDE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Best IDE"))
                    .andExpect(jsonPath("$[0].ownerUsername").value("nickname"))
                    .andExpect(jsonPath("$[0].communityId").value("Tech"));
        }

        @Test
        void withNoMatches_ReturnsOkAndEmptyList() throws Exception {
            when(threadService.getThreadsWithPrompt("xyz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/threads/search").param("query", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void withThreadHavingNullOwnerAndCommunity_ReturnsNullFields() throws Exception {
            server.entity.Thread thread = buildThread(1, "Orphan Thread", "desc", null, null);

            when(threadService.getThreadsWithPrompt("Orphan")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/search").param("query", "Orphan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].ownerUsername").isEmpty())
                    .andExpect(jsonPath("$[0].communityId").isEmpty());
        }
    }

    // ==========================================
    // GET /api/threads/user?email=
    // ==========================================

    @Nested
    class GetThreadsFromUser {

        @Test
        void withValidEmail_ReturnsOkAndUserThreads() throws Exception {
            server.entity.Thread thread = buildThread(1, "My Thread", "desc", user, buildCommunity("Tech"));

            when(threadService.getThreadsFromUser("email@email.com")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/user").param("email", "email@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("My Thread"))
                    .andExpect(jsonPath("$[0].ownerUsername").value("nickname"))
                    .andExpect(jsonPath("$[0].communityId").value("Tech"));
        }

        @Test
        void withThreadHavingNullOwnerAndCommunity_ReturnsNullFields() throws Exception {
            server.entity.Thread thread = buildThread(2, "Orphan Thread", "desc", null, null);

            when(threadService.getThreadsFromUser("email@email.com")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/user").param("email", "email@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].ownerUsername").isEmpty())
                    .andExpect(jsonPath("$[0].communityId").isEmpty());
        }

        @Test
        void withUserWithNoThreads_ReturnsOkAndEmptyList() throws Exception {
            when(threadService.getThreadsFromUser("empty@email.com")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/threads/user").param("email", "empty@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==========================================
    // GET /api/threads/user/conversations?email=
    // ==========================================

    @Nested
    class GetThreadsWhereUserPosted {

        @Test
        void withValidEmail_ReturnsOkAndList() throws Exception {
            server.entity.Thread thread = buildThread(1, "Replied Thread", "desc", user, buildCommunity("Tech"));

            when(threadService.getThreadsWhereUserPosted("email@email.com")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/user/conversations").param("email", "email@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Replied Thread"))
                    .andExpect(jsonPath("$[0].owner").value("nickname"));
        }

        @Test
        void withThreadHavingNullOwner_ReturnsNullOwnerField() throws Exception {
            server.entity.Thread thread = buildThread(2, "Orphan Thread", "desc", null, buildCommunity("Tech"));

            when(threadService.getThreadsWhereUserPosted("email@email.com")).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/user/conversations").param("email", "email@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].owner").isEmpty());
        }

        @Test
        void withNoConversations_ReturnsOkAndEmptyList() throws Exception {
            when(threadService.getThreadsWhereUserPosted("empty@email.com")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/threads/user/conversations").param("email", "empty@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==========================================
    // GET /api/threads/thread_feed
    // ==========================================

    @Nested
    class GetThreadFeed {

        @Test
        void withExistingThreads_ReturnsOkAndFeed() throws Exception {
            List<ThreadSummaryDTO> feed = List.of(
                    new ThreadSummaryDTO(3, "Hot Thread", "desc", "owner"),
                    new ThreadSummaryDTO(1, "Popular Thread", "desc", "owner")
            );

            when(threadService.getInitialFeed()).thenReturn(feed);

            mockMvc.perform(get("/api/threads/thread_feed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(3))
                    .andExpect(jsonPath("$[0].title").value("Hot Thread"));
        }

        @Test
        void withEmptyFeed_ReturnsOkAndEmptyList() throws Exception {
            when(threadService.getInitialFeed()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/threads/thread_feed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==========================================
    // GET/POST/DELETE /api/threads/favorites
    // ==========================================

    @Nested
    class Favorites {

        @Test
        void getFavorites_withValidToken_ReturnsOkAndList() throws Exception {
            server.entity.Thread thread = buildThread(2, "Fav Thread", "desc", user, buildCommunity("Music"));

            when(authService.getUserByToken(token)).thenReturn(user);
            when(threadService.getFavoriteThreads(user)).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/favorites").header("Authorization", bearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].title").value("Fav Thread"))
                    .andExpect(jsonPath("$[0].ownerUsername").value("nickname"))
                    .andExpect(jsonPath("$[0].communityId").value("Music"));
        }

        @Test
        void getFavorites_withThreadHavingNullOwnerAndCommunity_ReturnsNullFields() throws Exception {
            server.entity.Thread thread = buildThread(3, "Orphan Fav", "desc", null, null);

            when(authService.getUserByToken(token)).thenReturn(user);
            when(threadService.getFavoriteThreads(user)).thenReturn(List.of(thread));

            mockMvc.perform(get("/api/threads/favorites").header("Authorization", bearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].ownerUsername").isEmpty())
                    .andExpect(jsonPath("$[0].communityId").isEmpty());
        }

        @Test
        void getFavorites_withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/threads/favorites"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void getFavorites_withMalformedToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/threads/favorites").header("Authorization", "NoBearerToken"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void getFavorites_withInvalidToken_ReturnsForbidden() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(null);

            mockMvc.perform(get("/api/threads/favorites").header("Authorization", bearerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void addFavorite_withValidToken_ReturnsOk() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(user);

            mockMvc.perform(post("/api/threads/favorites/10").header("Authorization", bearerToken))
                    .andExpect(status().isOk());
        }

        @Test
        void addFavorite_withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post("/api/threads/favorites/10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void addFavorite_withMalformedToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post("/api/threads/favorites/10").header("Authorization", "NoBearer"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void addFavorite_withInvalidToken_ReturnsForbidden() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(null);

            mockMvc.perform(post("/api/threads/favorites/10").header("Authorization", bearerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void addFavorite_serviceThrowsBadRequest_ReturnsBadRequest() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(user);
            doThrow(new IllegalArgumentException("Thread no encontrado: 99")).when(threadService).addFavoriteThread(any(User.class), org.mockito.Mockito.eq(99));

            mockMvc.perform(post("/api/threads/favorites/99").header("Authorization", bearerToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Thread no encontrado: 99"));
        }

        @Test
        void removeFavorite_withValidToken_ReturnsOk() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(user);

            mockMvc.perform(delete("/api/threads/favorites/20").header("Authorization", bearerToken))
                    .andExpect(status().isOk());
        }

        @Test
        void removeFavorite_serviceThrowsBadRequest_ReturnsBadRequest() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(user);
            doThrow(new IllegalArgumentException("El hilo no está en favoritos: 30")).when(threadService).removeFavoriteThread(any(User.class), org.mockito.Mockito.eq(30));

            mockMvc.perform(delete("/api/threads/favorites/30").header("Authorization", bearerToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("El hilo no está en favoritos: 30"));
        }

        @Test
        void removeFavorite_withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/threads/favorites/20"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void removeFavorite_withMalformedToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/threads/favorites/20").header("Authorization", "NoBearer"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void removeFavorite_withInvalidToken_ReturnsForbidden() throws Exception {
            when(authService.getUserByToken(token)).thenReturn(null);

            mockMvc.perform(delete("/api/threads/favorites/20").header("Authorization", bearerToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // Helpers
    // ==========================================

    private server.entity.Thread buildThread(Integer id, String title, String description,
                                             User owner, server.entity.Community community) {
        server.entity.Thread thread = new server.entity.Thread();
        thread.setId(id);
        thread.setTitle(title);
        thread.setDescription(description);
        thread.setOwner(owner);
        thread.setCommunity(community);
        return thread;
    }

    private server.entity.Community buildCommunity(String name) {
        server.entity.Community community = new server.entity.Community();
        community.setId(1);
        community.setName(name);
        return community;
    }
}