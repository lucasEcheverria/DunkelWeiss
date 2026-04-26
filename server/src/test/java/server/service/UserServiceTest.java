package server.service;

import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.UserRepository;
import server.entity.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    private UserService userService;

    private final String token = "tok";

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, authService);
    }

    @Nested
    class GetUserByToken {

        @Test
        void returnsEmpty_whenTokenNullOrBlank() {
            assertThat(userService.getUserByToken(null)).isEmpty();
            assertThat(userService.getUserByToken("")).isEmpty();
            assertThat(userService.getUserByToken("   ")).isEmpty();
            verifyNoInteractions(authService);
        }

        @Test
        void returnsEmpty_whenAuthServiceReturnsNull() {
            when(authService.getUserByToken(token)).thenReturn(null);

            Optional<UserDTO> res = userService.getUserByToken(token);

            assertThat(res).isEmpty();
        }

        @Test
        void returnsDto_whenUserFound() {
            User u = new User();
            u.setId(5);
            u.setEmail("x@x.com");
            u.setNickname("nick");
            u.setPassword("pwd");

            when(authService.getUserByToken(token)).thenReturn(u);

            Optional<UserDTO> res = userService.getUserByToken(token);

            assertThat(res).isPresent();
            UserDTO dto = res.get();
            assertThat(dto.getId()).isEqualTo(5);
            assertThat(dto.getEmail()).isEqualTo("x@x.com");
            assertThat(dto.getUsername()).isEqualTo("nick");
        }
    }

    @Nested
    class UpdateUserByToken {

        @Test
        void returnsEmpty_whenTokenOrDtoInvalid() {
            assertThat(userService.updateUserByToken(null, new UpdateUserDTO("a","b"))).isEmpty();
            assertThat(userService.updateUserByToken("", new UpdateUserDTO("a","b"))).isEmpty();
            assertThat(userService.updateUserByToken(token, null)).isEmpty();
            verifyNoInteractions(authService, userRepository);
        }

        @Test
        void returnsEmpty_whenAuthServiceReturnsNull() {
            when(authService.getUserByToken(token)).thenReturn(null);

            Optional<UserDTO> res = userService.updateUserByToken(token, new UpdateUserDTO("n","p"));

            assertThat(res).isEmpty();
            verifyNoInteractions(userRepository);
        }

        @Test
        void updatesNicknameAndPassword_whenProvidedAndDifferent() {
            User u = new User();
            u.setId(7);
            u.setEmail("u@u.com");
            u.setNickname("oldNick");
            u.setPassword("oldPass");

            when(authService.getUserByToken(token)).thenReturn(u);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO("newNick","newPass");
            Optional<UserDTO> res = userService.updateUserByToken(token, dto);

            assertThat(res).isPresent();
            UserDTO out = res.get();
            assertThat(out.getId()).isEqualTo(7);
            assertThat(out.getUsername()).isEqualTo("newNick");

            ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(cap.capture());
            User saved = cap.getValue();
            assertThat(saved.getNickname()).isEqualTo("newNick");
            assertThat(saved.getPassword()).isEqualTo("newPass");
        }

        @Test
        void doesNotSave_whenNoChanges() {
            User u = new User();
            u.setId(8);
            u.setEmail("v@v.com");
            u.setNickname("same");
            u.setPassword("samePass");

            when(authService.getUserByToken(token)).thenReturn(u);

            UpdateUserDTO dto = new UpdateUserDTO(null, null); // no changes
            Optional<UserDTO> res = userService.updateUserByToken(token, dto);

            assertThat(res).isPresent();
            UserDTO out = res.get();
            assertThat(out.getId()).isEqualTo(8);
            assertThat(out.getUsername()).isEqualTo("same");

            verify(userRepository, never()).save(any());
        }
    }
}