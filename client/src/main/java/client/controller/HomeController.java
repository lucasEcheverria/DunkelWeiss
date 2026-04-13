package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import client.service.UserServiceProxy;
import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private AuthServiceProxy authService;
    private ThreadServiceProxy threadService;
    private UserServiceProxy userService;
    private CommunityServiceProxy communityService;

    public HomeController(AuthServiceProxy authService,
                          ThreadServiceProxy threadService,
                          UserServiceProxy userService,
                          CommunityServiceProxy communityService) {
        this.authService = authService;
        this.threadService = threadService;
        this.userService = userService;
        this.communityService = communityService;
    }

    @GetMapping({"/", "/home"})
    public String showDashboard(Model model) {
        // TODO: reemplazar por threadService.getAllSummaries() cuando esté listo
        ThreadSummaryDTO th1 = new ThreadSummaryDTO(1, "Hilo de prueba 1", "Descripción del hilo de prueba 1", "Usuario1");
        ThreadSummaryDTO th2 = new ThreadSummaryDTO(2, "Hilo de prueba 2", "Descripción del hilo de prueba 2", "Usuario2");
        ThreadSummaryDTO th3 = new ThreadSummaryDTO(3, "Hilo de prueba 3", "Descripción del hilo de prueba 3", "Usuario3");
        ThreadSummaryDTO th4 = new ThreadSummaryDTO(4, "Hilo de prueba 4", "Descripción del hilo de prueba 4", "Usuario4");
        ThreadSummaryDTO th5 = new ThreadSummaryDTO(5, "Hilo de prueba 5", "Descripción del hilo de prueba 5", "Usuario5");

        List<ThreadSummaryDTO> threads = List.of(th1, th2, th3, th4, th5);
        List<CommunityDTO> top5 = communityService.getTop5();

        model.addAttribute("threadFeedList", threads);
        model.addAttribute("top5Communities", top5);
        return "home";
    }

    @GetMapping("/communities/new")
    public String showCommunityForm() {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/";
        }
        return "newCommunity";
    }

    @PostMapping("/communities/create")
    public String createCommunity(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/";
        }
        communityService.createCommunity(new CreateCommunityDTO(name, description), token);
        return "redirect:/home";
    }
}