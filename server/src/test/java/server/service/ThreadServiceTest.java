package server.service;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.CommunityRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Community;
import server.entity.Thread;
import server.entity.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThreadServiceTest {

    @Mock
    private ThreadRepository threadRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommunityRepository communityRepository;

    private ThreadService threadService;

    @BeforeEach
    void setUp() {
        threadService = new ThreadService(threadRepository, userRepository, communityRepository);
    }

    // ==========================================
    // CreateThread
    // ==========================================

    @Nested
    class CreateThread {

        @Test
        void createThread_ReturnOk() {
            User owner       = buildUser("email@email.com", "nickname");
            Community community = buildCommunity(1, "General");
            CreateThreadDTO dto  = new CreateThreadDTO("titulo", "descripcion", 1);

            when(communityRepository.findById(dto.communityId())).thenReturn(Optional.of(community));
            when(threadRepository.save(any(Thread.class)))
                    .thenAnswer(invocation -> {
                        Thread t = invocation.getArgument(0);
                        t.setId(10);
                        return t;
                    });

            ThreadDTO result = threadService.createThread(dto, owner);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(10);
            assertThat(result.title()).isEqualTo("titulo");
            assertThat(result.description()).isEqualTo("descripcion");
            assertThat(result.ownerUsername()).isEqualTo("nickname");
            assertThat(result.communityId()).isEqualTo("General");

            verify(communityRepository).findById(1);
            verify(threadRepository).save(any(Thread.class));
        }

        @Test
        void createThread_CommunityNotFound_ThrowsIllegalArgumentException() {
            User owner      = buildUser("email@email.com", "nickname");
            CreateThreadDTO dto = new CreateThreadDTO("titulo", "descripcion", 99);

            when(communityRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.createThread(dto, owner))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");

            verify(threadRepository, never()).save(any());
        }
    }

    // ==========================================
    // GetAllSummaries
    // ==========================================

    @Nested
    class GetThread {

        @Test
        void getThread_ExistingId_ReturnsThreadDTO() {
            User owner      = buildUser("a@a.com", "alice");
            Community community = buildCommunity(2, "Tech");
            Thread thread   = buildThread(5, "Mi Thread", "descripcion", owner, community);

            when(threadRepository.findById(5)).thenReturn(Optional.of(thread));

            ThreadDTO result = threadService.getThread(5);

            assertThat(result.id()).isEqualTo(5);
            assertThat(result.title()).isEqualTo("Mi Thread");
            assertThat(result.ownerUsername()).isEqualTo("alice");
            assertThat(result.communityId()).isEqualTo("Tech");
        }

        @Test
        void getThread_NotFound_ThrowsIllegalArgumentException() {
            when(threadRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.getThread(404))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("404");
        }
    }

    // ==========================================
    // GetAllSummaries
    // ==========================================

    @Nested
    class GetAllSummaries {

        @Test
        void getAllSummaries_ReturnsMappedList() {
            User owner      = buildUser("b@b.com", "bob");
            Community community = buildCommunity(1, "General");
            Thread t1 = buildThread(1, "Thread A", "Desc A", owner, community);
            Thread t2 = buildThread(2, "Thread B", "Desc B", owner, community);

            when(threadRepository.findAll()).thenReturn(List.of(t1, t2));

            List<ThreadSummaryDTO> result = threadService.getAllSummaries();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).title()).isEqualTo("Thread A");
            assertThat(result.get(0).owner()).isEqualTo("bob");
            assertThat(result.get(1).title()).isEqualTo("Thread B");
        }

        @Test
        void getAllSummaries_EmptyRepository_ReturnsEmptyList() {
            when(threadRepository.findAll()).thenReturn(List.of());

            List<ThreadSummaryDTO> result = threadService.getAllSummaries();

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // GetThreadsWithPrompt
    // ==========================================

    @Nested
    class GetThreadsWithPrompt {

        @Test
        void getThreadsWithPrompt_MatchingQuery_ReturnsThreads() {
            User owner      = buildUser("c@c.com", "carol");
            Community community = buildCommunity(1, "General");
            Thread t = buildThread(3, "Java tips", "desc", owner, community);

            when(threadRepository.findByTitleContainingIgnoreCase("java")).thenReturn(List.of(t));

            List<Thread> result = threadService.getThreadsWithPrompt("java");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Java tips");
        }

        @Test
        void getThreadsWithPrompt_NoMatches_ReturnsEmptyList() {
            when(threadRepository.findByTitleContainingIgnoreCase("xyz")).thenReturn(List.of());

            List<Thread> result = threadService.getThreadsWithPrompt("xyz");

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // GetThreadsFromUser
    // ==========================================

    @Nested
    class GetThreadsFromUser {

        @Test
        void getThreadsFromUser_ExistingEmail_ReturnsThreads() {
            User owner      = buildUser("d@d.com", "dave");
            Community community = buildCommunity(1, "General");
            Thread t = buildThread(4, "Thread de dave", "desc", owner, community);

            when(threadRepository.findByOwnerEmail("d@d.com")).thenReturn(List.of(t));

            List<Thread> result = threadService.getThreadsFromUser("d@d.com");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOwner().getNickname()).isEqualTo("dave");
        }

        @Test
        void getThreadsFromUser_NoThreads_ReturnsEmptyList() {
            when(threadRepository.findByOwnerEmail("noone@x.com")).thenReturn(List.of());

            List<Thread> result = threadService.getThreadsFromUser("noone@x.com");

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
            User owner      = buildUser("e@e.com", "eve");
            Community community = buildCommunity(1, "General");
            Thread t1 = buildThread(7, "Popular A", "desc A", owner, community);
            Thread t2 = buildThread(8, "Popular B", "desc B", owner, community);

            when(threadRepository.findTop10ByPostCount()).thenReturn(List.of(t1, t2));

            List<ThreadSummaryDTO> result = threadService.getInitialFeed();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(7);
            assertThat(result.get(0).title()).isEqualTo("Popular A");
            assertThat(result.get(0).owner()).isEqualTo("eve");
            assertThat(result.get(1).id()).isEqualTo(8);
        }

        @Test
        void getInitialFeed_EmptyRepository_ReturnsEmptyList() {
            when(threadRepository.findTop10ByPostCount()).thenReturn(List.of());

            List<ThreadSummaryDTO> result = threadService.getInitialFeed();

            assertThat(result).isEmpty();
        }
    }

    // ==========================================
    // Helpers
    // ==========================================

    private User buildUser(String email, String nickname) {
        User u = new User(email, nickname, "password");
        u.setId(1);
        return u;
    }

    private Community buildCommunity(Integer id, String name) {
        Community c = new Community();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Thread buildThread(Integer id, String title, String description,
                               User owner, Community community) {
        Thread t = new Thread();
        t.setId(id);
        t.setTitle(title);
        t.setDescription(description);
        t.setOwner(owner);
        t.setCommunity(community);
        return t;
    }
}