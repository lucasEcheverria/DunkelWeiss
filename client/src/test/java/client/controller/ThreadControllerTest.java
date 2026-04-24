package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import lib.dto.CommunityDTO;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "threadServiceProxy")
    private ThreadServiceProxy threadService;

    @MockitoBean(name = "authServiceProxy")
    private AuthServiceProxy authService;

    @MockitoBean(name = "communityServiceProxy")
    private CommunityServiceProxy communityService;

    // ==========================================
    // @ModelAttribute — addCommunities (base de todos los tests)
    // ==========================================

    private void mockAddCommunitiesUnauthenticated() {
        when(authService.getToken()).thenReturn(null);
        when(communityService.getTop5()).thenReturn(List.of());
    }

    private void mockAddCommunitiesAuthenticated() {
        when(authService.getToken()).thenReturn("token123");
        when(communityService.getTop5()).thenReturn(List.of(
                new CommunityDTO(1, "General", "desc")
        ));
        when(communityService.getMyCommunities("token123")).thenReturn(List.of(
                new CommunityDTO(1, "General", "desc")
        ));
    }

    // ==========================================
    // ShowNewThreadForm
    // ==========================================

    @Nested
    class ShowNewThreadForm {

        @Test
        void showNewThreadForm_WithToken_ReturnsNewThreadView() throws Exception {
            mockAddCommunitiesAuthenticated();
            when(communityService.getAll()).thenReturn(List.of(new CommunityDTO(1, "General", "desc")));

            mockMvc.perform(get("/threads/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("newThread"))
                    .andExpect(model().attributeExists("communities"));
        }

        @Test
        void showNewThreadForm_WithoutToken_RedirectsToAuth() throws Exception {
            mockAddCommunitiesUnauthenticated();

            mockMvc.perform(get("/threads/new"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth"));
        }
    }

    // ==========================================
    // ListThreads
    // ==========================================

    @Nested
    @Disabled("Methods not implemented")
    class ListThreads {

        @Test
        void listThreads_ReturnsListView() throws Exception {
            mockAddCommunitiesUnauthenticated();
            List<ThreadSummaryDTO> threads = List.of(
                    new ThreadSummaryDTO(1, "Thread A", "Desc A", "alice")
            );
            when(threadService.getAllSummaries()).thenReturn(threads);

            mockMvc.perform(get("/threads"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("threads/list"))
                    .andExpect(model().attribute("threads", threads));
        }

        @Test
        void listThreads_EmptyList_ReturnsListViewWithEmptyModel() throws Exception {
            mockAddCommunitiesUnauthenticated();
            when(threadService.getAllSummaries()).thenReturn(List.of());

            mockMvc.perform(get("/threads"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("threads/list"))
                    .andExpect(model().attribute("threads", List.of()));
        }
    }

    // ==========================================
    // GetThread
    // ==========================================

    @Nested
    @Disabled("Methods not implemented")
    class GetThread {

        @Test
        void getThread_ExistingId_ReturnsDetailView() throws Exception {
            mockAddCommunitiesUnauthenticated();
            ThreadDTO thread = new ThreadDTO(5, "Mi hilo", "desc", "alice", "Tech");
            when(threadService.getThread(5)).thenReturn(thread);

            mockMvc.perform(get("/threads/5"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("threads/detail"))
                    .andExpect(model().attribute("thread", thread));
        }

        @Test
        void getThread_NotFound_RedirectsToThreads() throws Exception {
            mockAddCommunitiesUnauthenticated();
            when(threadService.getThread(404)).thenReturn(null);

            mockMvc.perform(get("/threads/404"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/threads"));
        }
    }

    // ==========================================
    // CreateThread
    // ==========================================

    @Nested
    class CreateThread {

        @Test
        void createThread_WithToken_RedirectsToHome() throws Exception {
            mockAddCommunitiesAuthenticated();
            ThreadDTO created = new ThreadDTO(1, "titulo", "desc", "alice", "General");
            when(threadService.createThread(any(CreateThreadDTO.class))).thenReturn(created);

            mockMvc.perform(post("/threads/create")
                            .param("title", "titulo")
                            .param("description", "desc")
                            .param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));
        }

        @Test
        void createThread_WithoutToken_RedirectsToAuth() throws Exception {
            mockAddCommunitiesUnauthenticated();

            mockMvc.perform(post("/threads/create")
                            .param("title", "titulo")
                            .param("description", "desc")
                            .param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth"));
        }

        @Test
        void createThread_ServiceReturnsNull_RedirectsToAuthWithError() throws Exception {
            mockAddCommunitiesAuthenticated();
            when(threadService.createThread(any(CreateThreadDTO.class))).thenReturn(null);

            mockMvc.perform(post("/threads/create")
                            .param("title", "titulo")
                            .param("description", "desc")
                            .param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth?error=true"));
        }
    }

    // ==========================================
    // GetThreadsWithPrompt
    // ==========================================

    @Nested
    class GetThreadsWithPrompt {

        @Test
        void getThreadsWithPrompt_WithQuery_ReturnsHomeView() throws Exception {
            mockAddCommunitiesUnauthenticated();
            List<ThreadDTO> threads = List.of(
                    new ThreadDTO(3, "Java tips", "desc", "carol", "Tech")
            );
            when(threadService.getThreadsWithPrompt("java")).thenReturn(threads);

            mockMvc.perform(get("/threads/search").param("query", "java"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attributeExists("threadFeedList"));
        }

        @Test
        void getThreadsWithPrompt_EmptyQuery_ReturnsHomeViewWithEmptyList() throws Exception {
            mockAddCommunitiesUnauthenticated();
            when(threadService.getThreadsWithPrompt("")).thenReturn(List.of());

            mockMvc.perform(get("/threads/search"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attribute("threadFeedList", List.of()));
        }
    }

    // ==========================================
    // GetThreadsFromUser
    // ==========================================

    @Nested
    class GetThreadsFromUser {

        @Test
        void getThreadsFromUser_WithEmail_ReturnsHomeView() throws Exception {
            mockAddCommunitiesUnauthenticated();
            List<ThreadDTO> threads = List.of(
                    new ThreadDTO(4, "Hilo de dave", "desc", "dave", "General")
            );
            when(threadService.getThreadsFromUser("dave@d.com")).thenReturn(threads);

            mockMvc.perform(get("/threads/user").param("email", "dave@d.com"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attributeExists("threadFeedList"));
        }

        @Test
        void getThreadsFromUser_NoThreads_ReturnsHomeViewWithEmptyList() throws Exception {
            mockAddCommunitiesUnauthenticated();
            when(threadService.getThreadsFromUser("nobody@x.com")).thenReturn(List.of());

            mockMvc.perform(get("/threads/user").param("email", "nobody@x.com"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attribute("threadFeedList", List.of()));
        }
    }
}