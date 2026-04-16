package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/threads")
public class ThreadController {

    private final ThreadServiceProxy threadService;
    private final AuthServiceProxy authService;
    private final CommunityServiceProxy communityService;

    public ThreadController(ThreadServiceProxy threadService,
                            AuthServiceProxy authService,
                            CommunityServiceProxy communityService) {
        this.threadService = threadService;
        this.authService = authService;
        this.communityService = communityService;
    }

    @GetMapping("/new")
    public String showNewThreadForm(Model model) {
        // CORTAFUEGOS FIABLE: Comprobamos si tenemos el token guardado
        if (authService.getToken() == null) {
            return "redirect:/auth";
        }

        model.addAttribute("communities", communityService.getAll());
        return "newThread";
    }

    @GetMapping
    public String listThreads(Model model) {
        List<ThreadSummaryDTO> threads = threadService.getAllSummaries();
        model.addAttribute("threads", threads);
        return "threads/list";
    }

    @GetMapping("/{id}")
    public String getThread(@PathVariable Integer id, Model model) {
        ThreadDTO thread = threadService.getThread(id);
        if (thread == null) return "redirect:/threads";
        model.addAttribute("thread", thread);
        return "threads/detail";
    }

    @PostMapping("/create")
    public String createThread(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Integer communityId) {

        // CORTAFUEGOS FIABLE
        if (authService.getToken() == null) {
            return "redirect:/auth";
        }

        ThreadDTO created = threadService.createThread(new CreateThreadDTO(title, description, communityId));
        if (created == null) {
            return "redirect:/auth?error=true";
        }
        return "redirect:/home";
    }
    @GetMapping("/search")
    public String getThreadsWithPrompt(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            Model model) {

        // --- ESTO ES LO ÚNICO NUEVO ---
        if (authService.getToken() == null) {
            return "redirect:/auth";
        }
        // ------------------------------

        List<ThreadSummaryDTO> threads = threadService.getThreadsWithPrompt(query).stream()
                .map(t -> new ThreadSummaryDTO(t.id(), t.title(), t.description(), t.ownerUsername()))
                .toList();
        model.addAttribute("threadFeedList", threads);
        model.addAttribute("query", query);
        return "home";
    }

    @GetMapping("/user")
    public String getThreadsFromUser(
            @RequestParam String email,
            Model model) {
        List<ThreadSummaryDTO> threads = threadService.getThreadsFromUser(email).stream()
                .map(t -> new ThreadSummaryDTO(t.id(), t.title(), t.description(), t.ownerUsername()))
                .toList();
        model.addAttribute("threadFeedList", threads);
        return "home";
    }

}