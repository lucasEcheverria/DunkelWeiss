package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import server.service.CommunityService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommunityController.class)
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @Test
    void getAll_returns200_withCommunityList() throws Exception {
        when(communityService.getAll()).thenReturn(List.of(
                new CommunityDTO(1, "Tech", "tech talks"),
                new CommunityDTO(2, "Gaming", null)
        ));

        mockMvc.perform(get("/api/communities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech"))
                .andExpect(jsonPath("$[1].name").value("Gaming"));
    }

    @Test
    void getAll_returns200_withEmptyList_whenNoCommunities() throws Exception {
        when(communityService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/communities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createCommunity_returns400_whenAuthorizationHeaderIsMissing() throws Exception {
        // Spring rechaza la petición antes de llegar al método porque el header es requerido
        mockMvc.perform(post("/api/communities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommunityDTO("Tech", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCommunity_returns201_withCreatedDTO_whenTokenIsValid() throws Exception {
        CommunityDTO created = new CommunityDTO(1, "Tech", "desc");
        when(communityService.createCommunity(any(CreateCommunityDTO.class), eq("mytoken")))
                .thenReturn(created);

        mockMvc.perform(post("/api/communities/create")
                        .header("Authorization", "Bearer mytoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommunityDTO("Tech", "desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void createCommunity_returns401_whenServiceThrowsOnInvalidToken() throws Exception {
        // El servicio lanza excepción → el controlador devuelve 401
        when(communityService.createCommunity(any(), any()))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        mockMvc.perform(post("/api/communities/create")
                        .header("Authorization", "Bearer bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommunityDTO("Tech", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTop5_returns200_withTopCommunities() throws Exception {
        when(communityService.getTop5ByPopularity()).thenReturn(List.of(
                new CommunityDTO(1, "Popular", null)
        ));

        mockMvc.perform(get("/api/communities/top5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Popular"));
    }

    @Test
    void getTop5_returns200_withEmptyList_whenNoCommunities() throws Exception {
        when(communityService.getTop5ByPopularity()).thenReturn(List.of());

        mockMvc.perform(get("/api/communities/top5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyCommunities_returns400_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/communities/my_communities"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyCommunities_returns200_withUserCommunities_whenTokenIsValid() throws Exception {
        when(communityService.getUserCommunities("tok"))
                .thenReturn(List.of(new CommunityDTO(3, "Sports", null)));

        mockMvc.perform(get("/api/communities/my_communities")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sports"));
    }

    @Test
    void getMyCommunities_returns401_whenServiceThrowsOnInvalidToken() throws Exception {
        when(communityService.getUserCommunities(any()))
                .thenThrow(new IllegalArgumentException("Invalid Token"));

        mockMvc.perform(get("/api/communities/my_communities")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leaveCommunity_returns401_whenAuthorizationHeaderIsMissing() throws Exception {
        // El header es optional=false en el controlador; la lógica devuelve 401 manualmente
        mockMvc.perform(post("/api/communities/leave/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leaveCommunity_returns200_whenTokenIsValid() throws Exception {
        doNothing().when(communityService).leaveCommunity(eq("tok"), eq(1));

        mockMvc.perform(post("/api/communities/leave/1")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isOk());
    }

    @Test
    void leaveCommunity_returns404_whenCommunityDoesNotExist() throws Exception {
        doThrow(new IllegalArgumentException("Community not found"))
                .when(communityService).leaveCommunity(any(), eq(99));

        mockMvc.perform(post("/api/communities/leave/99")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isNotFound());
    }
}