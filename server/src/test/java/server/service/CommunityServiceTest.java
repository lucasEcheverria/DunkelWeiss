package server.service;

import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.CommunityRepository;
import server.dao.UserRepository;
import server.entity.Community;
import server.entity.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private CommunityService communityService;

    @Test
    void getAll_returnsEmptyList_whenNoCommunitiesExist() {
        when(communityRepository.findAll()).thenReturn(List.of());

        assertThat(communityService.getAll()).isEmpty();
    }

    @Test
    void getAll_returnsMappedDTOs_whenCommunitiesExist() {
        Community c = buildCommunity(1, "Tech", "Tech talks");
        when(communityRepository.findAll()).thenReturn(List.of(c));

        List<CommunityDTO> result = communityService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo("Tech");
        assertThat(result.get(0).description()).isEqualTo("Tech talks");
    }

    @Test
    void createCommunity_throwsIllegalArgument_whenTokenIsInvalid() {
        when(authService.getUserByToken("bad-token")).thenReturn(null);

        assertThatThrownBy(() ->
                communityService.createCommunity(new CreateCommunityDTO("Gaming", null), "bad-token")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token");
    }

    @Test
    void createCommunity_savesAndReturnsCommunityDTO_whenTokenIsValid() {
        User user    = buildUser(1, "user@test.com", "tester");
        Community saved = buildCommunity(10, "Gaming", null);

        when(authService.getUserByToken("valid-token")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(communityRepository.save(any(Community.class))).thenReturn(saved);
        when(userRepository.save(any(User.class))).thenReturn(user);

        CommunityDTO result = communityService.createCommunity(
                new CreateCommunityDTO("Gaming", null), "valid-token");

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.name()).isEqualTo("Gaming");
        verify(communityRepository).save(any(Community.class));
    }

    @Test
    void createCommunity_addsCreatorToCommunitiesList_whenTokenIsValid() {
        User user       = buildUser(1, "user@test.com", "tester");
        Community saved = buildCommunity(10, "Gaming", null);

        when(authService.getUserByToken("valid-token")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(communityRepository.save(any())).thenReturn(saved);
        when(userRepository.save(any())).thenReturn(user);

        communityService.createCommunity(new CreateCommunityDTO("Gaming", null), "valid-token");

        // El creador debe haberse añadido a la lista de la comunidad guardada
        verify(userRepository).save(argThat(u -> u.getcommunities().contains(saved)));
    }

    @Test
    void getTop5ByPopularity_returnsMappedDTOs_inOrder() {
        Community c1 = buildCommunity(1, "Alpha", null);
        Community c2 = buildCommunity(2, "Beta",  null);
        when(communityRepository.findTop5ByMemberCount()).thenReturn(List.of(c1, c2));

        List<CommunityDTO> result = communityService.getTop5ByPopularity();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CommunityDTO::name).containsExactly("Alpha", "Beta");
    }

    @Test
    void getTop5ByPopularity_returnsEmptyList_whenNoCommunities() {
        when(communityRepository.findTop5ByMemberCount()).thenReturn(List.of());

        assertThat(communityService.getTop5ByPopularity()).isEmpty();
    }

    @Test
    void getUserCommunities_throwsIllegalArgument_whenTokenIsInvalid() {
        when(authService.getUserByToken("bad")).thenReturn(null);

        assertThatThrownBy(() -> communityService.getUserCommunities("bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getUserCommunities_returnsUserCommunities_whenTokenIsValid() {
        User user      = buildUser(1, "u@test.com", "u");
        Community comm = buildCommunity(5, "Sports", "sport stuff");
        user.getcommunities().add(comm);

        when(authService.getUserByToken("tok")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        List<CommunityDTO> result = communityService.getUserCommunities("tok");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Sports");
    }

    @Test
    void getUserCommunities_returnsEmptyList_whenUserHasNoCommunities() {
        User user = buildUser(1, "u@test.com", "u");
        // Sin comunidades añadidas

        when(authService.getUserByToken("tok")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThat(communityService.getUserCommunities("tok")).isEmpty();
    }

    @Test
    void leaveCommunity_throwsIllegalArgument_whenTokenIsInvalid() {
        when(authService.getUserByToken("bad")).thenReturn(null);

        assertThatThrownBy(() -> communityService.leaveCommunity("bad", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void leaveCommunity_throwsIllegalArgument_whenCommunityDoesNotExist() {
        User user = buildUser(1, "u@test.com", "u");

        when(authService.getUserByToken("tok")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(communityRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.leaveCommunity("tok", 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Community");
    }

    @Test
    void leaveCommunity_removesCommunityFromUserAndSaves() {
        Community comm = buildCommunity(5, "Tech", null);
        User user      = buildUser(1, "u@test.com", "u");
        user.getcommunities().add(comm);

        when(authService.getUserByToken("tok")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(communityRepository.findById(5)).thenReturn(Optional.of(comm));

        communityService.leaveCommunity("tok", 5);

        assertThat(user.getcommunities()).doesNotContain(comm);
        verify(userRepository).save(user);
    }

    @Test
    void leaveCommunity_doesNotSave_whenUserIsNotMemberOfCommunity() {
        Community comm = buildCommunity(5, "Tech", null);
        User user      = buildUser(1, "u@test.com", "u");
        // El usuario NO pertenece a la comunidad

        when(authService.getUserByToken("tok")).thenReturn(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(communityRepository.findById(5)).thenReturn(Optional.of(comm));

        communityService.leaveCommunity("tok", 5);

        // removeIf devuelve false → no se debería guardar
        verify(userRepository, never()).save(any());
    }

    private Community buildCommunity(int id, String name, String description) {
        Community c = new Community();
        c.setId(id);
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private User buildUser(int id, String email, String nickname) {
        User u = new User(email, nickname, "pass");
        u.setId(id);
        return u;
    }
}