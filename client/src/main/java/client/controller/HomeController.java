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

        List<ThreadSummaryDTO> threads = threadService.getAllSummaries();

        model.addAttribute("threadFeedList", threads);
        String token = authService.getToken();
        if (token == null) {
            // NO LOGUEADO: Pedimos las top 5 y las pasamos al modelo
            List<CommunityDTO> top5 = communityService.getTop5();
            System.out.println(top5);
            model.addAttribute("top5Communities", top5);
        } else {
            // LOGUEADO: Pedimos sus comunidades
            List<CommunityDTO> myCommunities = communityService.getMyCommunities(token);
            model.addAttribute("myCommunities", myCommunities);
        }
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