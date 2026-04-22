package server.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import server.service.AuthService;
import server.service.ThreadService;

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @MockBean
    private ThreadService threadService;

    @MockBean
    private AuthService authService;

    @Test
    @Disabled("Ignorado hasta que se implementen las pruebas reales")
    void testDummy() {
    }
}
