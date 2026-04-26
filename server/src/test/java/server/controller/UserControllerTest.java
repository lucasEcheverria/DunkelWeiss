package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import server.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String token = "valid-token";
    private final String bearer = "Bearer valid-token";

    // ==========================================
    // GET /api/users/me
    // ==========================================

    @Nested
    class GetCurrentUser {

        @Test
        void withValidToken_ReturnsOkAndBody() throws Exception {
            UserDTO dto = new UserDTO(1, "a@a.com", "alice");
            when(userService.getUserByToken(token)).thenReturn(Optional.of(dto));

            mockMvc.perform(get("/api/users/me").header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("a@a.com"))
                    .andExpect(jsonPath("$.username").value("alice"));
        }

        @Test
        void withRawTokenHeader_ReturnsOkAndBody() throws Exception {
            UserDTO dto = new UserDTO(4, "a@a.com", "alice");
            when(userService.getUserByToken(token)).thenReturn(Optional.of(dto));

            mockMvc.perform(get("/api/users/me").header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(4))
                    .andExpect(jsonPath("$.email").value("a@a.com"))
                    .andExpect(jsonPath("$.username").value("alice"));
        }

        @Test
        void withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void withInvalidToken_ReturnsUnauthorized() throws Exception {
            when(userService.getUserByToken(token)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/me").header("Authorization", bearer))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // PUT /api/users/me/update
    // ==========================================

    @Nested
    class UpdateCurrentUser {

        @Test
        void withValidTokenAndDto_ReturnsOkAndBody() throws Exception {
            UpdateUserDTO dto = new UpdateUserDTO("aliceNew", "newPass");
            UserDTO updated = new UserDTO(2, "a@a.com", "aliceNew");

            when(userService.updateUserByToken(token, dto)).thenReturn(Optional.of(updated));

            mockMvc.perform(put("/api/users/me/update")
                            .header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.email").value("a@a.com"))
                    .andExpect(jsonPath("$.username").value("aliceNew"));
        }

        @Test
        void withRawTokenAndDto_ReturnsOkAndBody() throws Exception {
            UpdateUserDTO dto = new UpdateUserDTO("aliceNew", "rawPass");
            UserDTO updated = new UserDTO(5, "a@a.com", "aliceNew");

            when(userService.updateUserByToken(token, dto)).thenReturn(Optional.of(updated));

            mockMvc.perform(put("/api/users/me/update")
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.email").value("a@a.com"))
                    .andExpect(jsonPath("$.username").value("aliceNew"));
        }

        @Test
        void withoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
            UpdateUserDTO dto = new UpdateUserDTO("x", "y");

            mockMvc.perform(put("/api/users/me/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void withInvalidToken_ReturnsUnauthorized() throws Exception {
            UpdateUserDTO dto = new UpdateUserDTO("x", "y");
            when(userService.updateUserByToken(token, dto)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/me/update")
                            .header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void withNoBody_DefaultsAndReturnsOkWhenServiceReturns() throws Exception {
            UpdateUserDTO dto = new UpdateUserDTO(null, null);
            UserDTO updated = new UserDTO(3, "a@a.com", "alice");

            when(userService.updateUserByToken(token, dto)).thenReturn(Optional.of(updated));

            mockMvc.perform(put("/api/users/me/update")
                            .header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.email").value("a@a.com"));
        }
    }
}