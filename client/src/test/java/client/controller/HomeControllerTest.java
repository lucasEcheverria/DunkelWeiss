package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import client.service.UserServiceProxy;
import lib.dto.CommunityDTO;
import lib.dto.ThreadSummaryDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean(name = "authServiceProxy") private AuthServiceProxy authService;
    @MockitoBean private ThreadServiceProxy threadService;
    @MockitoBean private UserServiceProxy userService;
    @MockitoBean private CommunityServiceProxy communityService;

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private void mockUnauthenticated() {
        when(authService.getToken()).thenReturn(null);
        when(communityService.getTop5()).thenReturn(List.of());
    }

    private void mockAuthenticated(String token) {
        when(authService.getToken()).thenReturn(token);
        when(communityService.getTop5()).thenReturn(List.of());
        when(communityService.getMyCommunities(token)).thenReturn(List.of());
    }

    // ─────────────────────────────────────────────

    @Nested
    class ShowDashboard {

        @Test
        void returns200_andRendersHomeView() throws Exception {
            mockUnauthenticated();
            when(threadService.getInitialFeed()).thenReturn(List.of());

            mockMvc.perform(get("/home"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"));
        }

        @Test
        void exposesThreadFeedList_inModel() throws Exception {
            mockUnauthenticated();
            List<ThreadSummaryDTO> feed = List.of(
                    new ThreadSummaryDTO(1, "Título", "Desc", "user1")
            );
            when(threadService.getInitialFeed()).thenReturn(feed);

            mockMvc.perform(get("/home"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("threadFeedList"));
        }

        @Test
        void rootPath_alsoRendersHomeView() throws Exception {
            mockUnauthenticated();
            when(threadService.getInitialFeed()).thenReturn(List.of());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"));
        }

        @Test
        void exposesTop5Communities_inModel_whenUserIsNotLoggedIn() throws Exception {
            when(authService.getToken()).thenReturn(null);
            when(threadService.getInitialFeed()).thenReturn(List.of());
            when(communityService.getTop5()).thenReturn(List.of(
                    new CommunityDTO(1, "Tech", null)
            ));

            mockMvc.perform(get("/home"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("top5Communities"));
        }

        @Test
        void exposesTop5AndMyCommunities_inModel_whenUserIsLoggedIn() throws Exception {
            mockAuthenticated("tok");
            when(threadService.getInitialFeed()).thenReturn(List.of());
            when(communityService.getMyCommunities("tok")).thenReturn(List.of(
                    new CommunityDTO(2, "Gaming", null)
            ));

            mockMvc.perform(get("/home"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("top5Communities"))
                    .andExpect(model().attributeExists("myCommunities"));
        }
    }

    @Nested
    class ShowCommunityForm {

        @Test
        void returns200_andRendersNewCommunityView_whenUserIsLoggedIn() throws Exception {
            mockAuthenticated("tok");

            mockMvc.perform(get("/communities/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("newCommunity"));
        }

        @Test
        void redirectsToRoot_whenUserIsNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(get("/communities/new"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Nested
    class CreateCommunity {

        @Test
        void redirectsToHome_whenCommunityIsCreatedSuccessfully() throws Exception {
            mockAuthenticated("tok");

            mockMvc.perform(post("/communities/create")
                            .param("name", "Gaming")
                            .param("description", "Una comunidad de gaming"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));

            verify(communityService).createCommunity(any(), eq("tok"));
        }

        @Test
        void redirectsToRoot_whenUserIsNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(post("/communities/create")
                            .param("name", "Gaming"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(communityService, never()).createCommunity(any(), any());
        }

        @Test
        void worksWithoutDescription_whenDescriptionIsOmitted() throws Exception {
            mockAuthenticated("tok");

            mockMvc.perform(post("/communities/create")
                            .param("name", "Gaming"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));
        }
    }
}