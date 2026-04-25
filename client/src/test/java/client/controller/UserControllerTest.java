package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import client.service.UserServiceProxy;
import lib.dto.CommunityDTO;
import lib.dto.ThreadDTO;
import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean(name = "authServiceProxy") private AuthServiceProxy authService;
    @MockBean private UserServiceProxy userService;
    @MockBean private CommunityServiceProxy communityService;
    @MockBean private ThreadServiceProxy threadService;

    private void mockUnauthenticated() {
        when(authService.getToken()).thenReturn(null);
    }

    private void mockAuthenticated(String token) {
        when(authService.getToken()).thenReturn(token);
    }

    @Nested
    class ShowProfile {

        @Test
        void redirectsToRoot_whenNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(get("/profile"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }

        @Test
        void redirectsToError_whenUserNotFound() throws Exception {
            mockAuthenticated("tok");
            when(userService.getCurrentUser("tok")).thenReturn(null);

            mockMvc.perform(get("/profile"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/?error=true"));
        }

        @Test
        void returns200_andRendersProfile_andExposesModelAttributes() throws Exception {
            mockAuthenticated("tok");
            UserDTO user = new UserDTO(1, "a@a", "alice");
            when(userService.getCurrentUser("tok")).thenReturn(user);
            when(communityService.getMyCommunities("tok")).thenReturn(List.of(new CommunityDTO(1, "Tech", null)));
            when(threadService.getThreadsFromUser(user.getEmail())).thenReturn(List.of(new ThreadDTO(1, "Hilo", "Desc", "alice", "General")));
            when(threadService.getFavoriteThreads()).thenReturn(List.of());

            mockMvc.perform(get("/profile").param("tab", "threads"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attributeExists("communities"))
                    .andExpect(model().attributeExists("createdThreads"))
                    .andExpect(model().attributeExists("favoriteThreads"))
                    .andExpect(model().attributeExists("activeTab"));
        }
    }

    @Nested
    class LeaveCommunity {

        @Test
        void redirectsToRoot_whenNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(post("/profile/leaveCommunity").param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(communityService, never()).leaveCommunity(any(), any());
        }

        @Test
        void redirectsToProfileTab_whenSuccessful() throws Exception {
            mockAuthenticated("tok");
            when(communityService.leaveCommunity("tok", 1)).thenReturn(true);

            mockMvc.perform(post("/profile/leaveCommunity").param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?tab=communities"));

            verify(communityService).leaveCommunity("tok", 1);
        }

        @Test
        void redirectsToProfileWithError_whenNotSuccessful() throws Exception {
            mockAuthenticated("tok");
            when(communityService.leaveCommunity("tok", 1)).thenReturn(false);

            mockMvc.perform(post("/profile/leaveCommunity").param("communityId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?error=true"));
        }
    }

    @Nested
    class UpdateProfile {

        @Test
        void redirectsToRoot_whenNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(post("/profile/update").param("nickname", "n"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(userService, never()).updateUser(any());
        }

        @Test
        void redirectsToProfileWithError_whenUpdateFails() throws Exception {
            mockAuthenticated("tok");
            when(userService.updateUser(any(UpdateUserDTO.class))).thenReturn(null);

            mockMvc.perform(post("/profile/update").param("nickname", "n").param("password", "p"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?error=true"));
        }

        @Test
        void redirectsToProfile_whenUpdateSucceeds() throws Exception {
            mockAuthenticated("tok");
            when(userService.updateUser(any(UpdateUserDTO.class))).thenReturn(new UserDTO(1, "a@a", "alice"));

            mockMvc.perform(post("/profile/update").param("nickname", "n").param("password", "p"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));

            verify(userService).updateUser(any(UpdateUserDTO.class));
        }
    }

    @Nested
    class FavoriteUnfavorite {

        @Test
        void favorite_redirectsToRoot_whenNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(post("/profile/favorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(threadService, never()).addFavorite(anyInt());
        }

        @Test
        void favorite_redirectsToProfileTab_whenSuccessful() throws Exception {
            mockAuthenticated("tok");
            when(threadService.addFavorite(1)).thenReturn(true);

            mockMvc.perform(post("/profile/favorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?tab=threads"));

            verify(threadService).addFavorite(1);
        }

        @Test
        void favorite_redirectsToProfileTabWithError_whenFails() throws Exception {
            mockAuthenticated("tok");
            when(threadService.addFavorite(1)).thenReturn(false);

            mockMvc.perform(post("/profile/favorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?tab=threads&error=true"));
        }

        @Test
        void unfavorite_redirectsToRoot_whenNotLoggedIn() throws Exception {
            mockUnauthenticated();

            mockMvc.perform(post("/profile/unfavorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(threadService, never()).removeFavorite(anyInt());
        }

        @Test
        void unfavorite_redirectsToProfileTab_whenSuccessful() throws Exception {
            mockAuthenticated("tok");
            when(threadService.removeFavorite(1)).thenReturn(true);

            mockMvc.perform(post("/profile/unfavorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?tab=threads"));

            verify(threadService).removeFavorite(1);
        }

        @Test
        void unfavorite_redirectsToProfileTabWithError_whenFails() throws Exception {
            mockAuthenticated("tok");
            when(threadService.removeFavorite(1)).thenReturn(false);

            mockMvc.perform(post("/profile/unfavorite").param("threadId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?tab=threads&error=true"));
        }
    }
}