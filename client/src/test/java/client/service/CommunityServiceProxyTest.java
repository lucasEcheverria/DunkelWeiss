package client.service;

import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    private CommunityServiceProxy proxy;

    private static final String SERVER_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        proxy = new CommunityServiceProxy(restTemplate, SERVER_URL);
    }

    @Nested
    class GetAll {

        @Test
        void returnsList_whenServerRespondsSuccessfully() {
            List<CommunityDTO> communities = List.of(new CommunityDTO(1, "Tech", "desc"));

            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(communities));

            assertThat(proxy.getAll()).isEqualTo(communities);
        }

        @Test
        void returnsEmptyList_whenServerThrowsException() {
            when(restTemplate.exchange(
                    any(String.class), any(), any(), any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("connection refused"));

            assertThat(proxy.getAll()).isEmpty();
        }

        @Test
        void returnsEmptyList_whenServerReturnsNullBody() {
            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            assertThat(proxy.getAll()).isEmpty();
        }
    }

    @Nested
    class CreateCommunity {

        @Test
        void returnsDTO_whenServerRespondsSuccessfully() {
            CommunityDTO dto = new CommunityDTO(1, "Gaming", null);

            when(restTemplate.postForEntity(
                    eq(SERVER_URL + "/api/communities/create"),
                    any(HttpEntity.class),
                    eq(CommunityDTO.class)
            )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(dto));

            CommunityDTO result = proxy.createCommunity(new CreateCommunityDTO("Gaming", null), "tok");

            assertThat(result).isEqualTo(dto);
        }

        @Test
        void returnsNull_whenServerThrowsException() {
            when(restTemplate.postForEntity(
                    any(String.class), any(), eq(CommunityDTO.class)
            )).thenThrow(new RuntimeException("error"));

            assertThat(proxy.createCommunity(new CreateCommunityDTO("Gaming", null), "tok")).isNull();
        }
    }

    @Nested
    class GetTop5 {

        @Test
        void returnsList_whenServerRespondsSuccessfully() {
            List<CommunityDTO> top5 = List.of(new CommunityDTO(1, "Popular", null));

            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities/top5"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(top5));

            assertThat(proxy.getTop5()).isEqualTo(top5);
        }

        @Test
        void returnsEmptyList_whenServerThrowsException() {
            when(restTemplate.exchange(
                    any(String.class), any(), any(), any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException());

            assertThat(proxy.getTop5()).isEmpty();
        }
    }

    @Nested
    class GetMyCommunities {

        @Test
        void returnsList_whenServerRespondsSuccessfully() {
            List<CommunityDTO> list = List.of(new CommunityDTO(2, "Sports", "sport"));

            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities/my_communities"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(list));

            assertThat(proxy.getMyCommunities("tok")).isEqualTo(list);
        }

        @Test
        void returnsEmptyList_whenServerThrowsException() {
            when(restTemplate.exchange(
                    any(String.class), any(), any(), any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException());

            assertThat(proxy.getMyCommunities("tok")).isEmpty();
        }

        @Test
        void returnsEmptyList_whenServerReturnsNullBody() {
            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities/my_communities"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            assertThat(proxy.getMyCommunities("tok")).isEmpty();
        }
    }

    @Nested
    class LeaveCommunity {

        @Test
        void returnsTrue_whenServerResponds2xx() {
            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities/leave/5"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Void.class)
            )).thenReturn(ResponseEntity.ok().build());

            assertThat(proxy.leaveCommunity("tok", 5)).isTrue();
        }

        @Test
        void returnsFalse_whenServerThrowsException() {
            when(restTemplate.exchange(
                    any(String.class), any(), any(), eq(Void.class)
            )).thenThrow(new RuntimeException("error"));

            assertThat(proxy.leaveCommunity("tok", 5)).isFalse();
        }

        @Test
        void returnsFalse_whenServerRespondsWithError() {
            when(restTemplate.exchange(
                    eq(SERVER_URL + "/api/communities/leave/5"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Void.class)
            )).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

            assertThat(proxy.leaveCommunity("tok", 5)).isFalse();
        }
    }
}