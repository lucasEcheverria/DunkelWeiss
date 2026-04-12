package client.controller;

import client.service.AuthServiceProxy;
import client.service.ThreadServiceProxy;
import client.service.UserServiceProxy;
import lib.dto.ThreadSummaryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private AuthServiceProxy authService;
    private ThreadServiceProxy threadService;
    private UserServiceProxy userService;

    public HomeController(AuthServiceProxy authService,
                          ThreadServiceProxy threadService,
                          UserServiceProxy userService) {
        this.authService = authService;
        this.threadService = threadService;
        this.userService = userService;
    }

    /* ------------ MAIN PAGE ------------ */
    /*TODO
     * INCLUDE SESSION TOKEN, VERIFICATION AND REDIRECTION
     * Make a create thread window to add a title and a description through this URL
     * a
     * I'm using a test object for now
     */
    @GetMapping("/home")
    public String showDashboard(Model model) {
        //REMOVE
        ThreadSummaryDTO th1 = new ThreadSummaryDTO(1, "Hilo de prueba 1", "Descripción del hilo de prueba 1", "Usuario1");
        ThreadSummaryDTO th2 = new ThreadSummaryDTO(2, "Hilo de prueba 2", "Descripción del hilo de prueba 2", "Usuario2");
        ThreadSummaryDTO th3 = new ThreadSummaryDTO(3, "Hilo de prueba 3", "Descripción del hilo de prueba 3", "Usuario3");
        ThreadSummaryDTO th4 = new ThreadSummaryDTO(4, "Hilo de prueba 4", "Descripción del hilo de prueba 4", "Usuario4");
        ThreadSummaryDTO th5 = new ThreadSummaryDTO(5, "Hilo de prueba 5", "Descripción del hilo de prueba 5", "Usuario5");

        List<ThreadSummaryDTO> threads = List.of(th1, th2, th3, th4, th5);
        //List<ThreadSummaryDTO> threads = threadService.getAllSummaries();

        model.addAttribute("threadFeedList", threads);
        return "home";
    }

}
