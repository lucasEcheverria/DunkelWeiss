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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

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
    // Favorites
    // ==========================================

    @Nested
    class Favorites {

        @Test
        void addFavoriteThread_addsAndSavesWhenNotPresent() {
            User owner = buildUser("fav@u.com", "alice");
            Community community = buildCommunity(1, "General");
            Thread hilo = buildThread(10, "T", "D", owner, community);

            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(10)).thenReturn(Optional.of(hilo));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            threadService.addFavoriteThread(owner, 10);

            ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(cap.capture());
            User saved = cap.getValue();
            assertThat(saved.getFavoriteThreads()).isNotNull();
            assertThat(saved.getFavoriteThreads()).hasSize(1);
            assertThat(saved.getFavoriteThreads().get(0).getId()).isEqualTo(10);
        }

        @Test
        void addFavoriteThread_doesNotSaveWhenAlreadyPresent() {
            User owner = buildUser("fav@u.com", "alice");
            Community community = buildCommunity(1, "General");
            Thread hilo = buildThread(11, "T2", "D2", owner, community);

            // owner already has hilo in favorites
            owner.setFavoriteThreads(new ArrayList<>());
            owner.getFavoriteThreads().add(hilo);

            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(11)).thenReturn(Optional.of(hilo));

            threadService.addFavoriteThread(owner, 11);

            verify(userRepository, never()).save(any());
        }

        @Test
        void addFavoriteThread_throwsWhenUserNotFound() {
            User owner = buildUser("x@x.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.addFavoriteThread(owner, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(owner.getId()));
        }

        @Test
        void addFavoriteThread_throwsWhenThreadNotFound() {
            User owner = buildUser("x@x.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.addFavoriteThread(owner, 99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void removeFavoriteThread_removesAndSavesWhenPresent() {
            User owner = buildUser("r@r.com", "alice");
            Community community = buildCommunity(2, "Tech");
            Thread hilo = buildThread(20, "TT", "DD", owner, community);

            owner.setFavoriteThreads(new ArrayList<>());
            owner.getFavoriteThreads().add(hilo);

            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(20)).thenReturn(Optional.of(hilo));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            threadService.removeFavoriteThread(owner, 20);

            ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(cap.capture());
            User saved = cap.getValue();
            assertThat(saved.getFavoriteThreads()).isEmpty();
        }

        @Test
        void removeFavoriteThread_throwsWhenNotInFavorites() {
            User owner = buildUser("r@r.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(30)).thenReturn(Optional.of(buildThread(30, "X", "Y", owner, buildCommunity(1, "G"))));

            assertThatThrownBy(() -> threadService.removeFavoriteThread(owner, 30))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("30");
        }

        @Test
        void removeFavoriteThread_throwsWhenUserNotFound() {
            User owner = buildUser("no@u.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.removeFavoriteThread(owner, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(owner.getId()));
        }

        @Test
        void removeFavoriteThread_throwsWhenThreadNotFound() {
            User owner = buildUser("no@u.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(threadRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.removeFavoriteThread(owner, 999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("999");
        }

        @Test
        void getFavoriteThreads_returnsListOrEmpty() {
            User owner = buildUser("g@u.com", "alice");
            Thread t = buildThread(40, "Title", "Desc", owner, buildCommunity(1, "G"));

            owner.setFavoriteThreads(new ArrayList<>());
            owner.getFavoriteThreads().add(t);

            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

            var res = threadService.getFavoriteThreads(owner);
            assertThat(res).hasSize(1);

            // null favorites -> empty list
            User other = buildUser("h@u.com", "alice");
            other.setFavoriteThreads(null);
            when(userRepository.findById(other.getId())).thenReturn(Optional.of(other));
            var res2 = threadService.getFavoriteThreads(other);
            assertThat(res2).isEmpty();
        }

        @Test
        void getFavoriteThreads_throwsWhenUserNotFound() {
            User owner = buildUser("missing@u.com", "alice");
            when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> threadService.getFavoriteThreads(owner))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(owner.getId()));
        }
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
    // GetThread
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
    // GetThreadsWhereUserPosted
    // ==========================================

    @Nested
    class GetThreadsWhereUserPosted {

        @Test
        void getThreadsWhereUserPosted_ExistingEmail_ReturnsThreads() {
            User owner = buildUser("p@p.com", "poster");
            Community community = buildCommunity(1, "General");
            Thread t = buildThread(5, "Thread con post", "desc", owner, community);

            when(threadRepository.findDistinctByPostsOwnerEmail("p@p.com")).thenReturn(List.of(t));

            List<Thread> result = threadService.getThreadsWhereUserPosted("p@p.com");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Thread con post");
            assertThat(result.get(0).getOwner().getNickname()).isEqualTo("poster");
        }

        @Test
        void getThreadsWhereUserPosted_NoThreads_ReturnsEmptyList() {
            when(threadRepository.findDistinctByPostsOwnerEmail("noone@x.com")).thenReturn(List.of());

            List<Thread> result = threadService.getThreadsWhereUserPosted("noone@x.com");

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
