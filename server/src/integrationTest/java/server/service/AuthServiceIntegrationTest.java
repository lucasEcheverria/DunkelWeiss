package server.service;

import lib.dto.UserCredentialsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import server.App;
import server.dao.UserRepository;
import server.entity.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = App.class)
@Testcontainers
@Transactional  // Rollback automático tras cada test
class AuthServiceIntegrationTest {

    // Testcontainers levanta MySQL automáticamente
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("schema.sql"),
                    "/docker-entrypoint-initdb.d/schema.sql"
            );

    // Conecta Spring al contenedor levantado
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // --- Tests del flujo de registro y login ---

    @Test
    void register_conEmailNuevo_deberiaCrearElUsuario() {
        UserCredentialsDTO dto = new UserCredentialsDTO("test@example.com", "testuser", "pass123");

        Optional<User> result = authService.register(dto);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }

    @Test
    void register_conEmailDuplicado_deberiaRetornarEmpty() {
        UserCredentialsDTO dto = new UserCredentialsDTO("dup@example.com", "user1", "pass");
        authService.register(dto);

        Optional<User> result = authService.register(dto); // segundo intento

        assertThat(result).isEmpty();
    }

    @Test
    void login_conCredencialesCorrectas_deberiaRetornarToken() {
        authService.register(new UserCredentialsDTO("login@example.com", "user", "secret"));

        Optional<String> token = authService.login("login@example.com", "secret");

        assertThat(token).isPresent();
        assertThat(token.get()).isNotBlank();
    }

    @Test
    void login_conPasswordIncorrecta_deberiaRetornarEmpty() {
        authService.register(new UserCredentialsDTO("wrong@example.com", "user", "correct"));

        Optional<String> token = authService.login("wrong@example.com", "wrongpass");

        assertThat(token).isEmpty();
    }

    @Test
    void logout_conTokenValido_deberiaEliminarSesion() {
        authService.register(new UserCredentialsDTO("logout@example.com", "user", "pass"));
        String token = authService.login("logout@example.com", "pass").get();

        authService.logout(token);

        assertThat(authService.getUserByToken(token)).isNull();
    }
}
