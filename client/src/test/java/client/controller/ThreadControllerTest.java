package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // <-- ¡Importante!

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @MockBean
    private ThreadServiceProxy threadService;

    @MockBean
    private AuthServiceProxy authService;

    @MockBean
    private CommunityServiceProxy communityService;

    @Test
    @Disabled("Ignorado hasta que se implementen las pruebas reales")
    void testDummy() {
    }
}
