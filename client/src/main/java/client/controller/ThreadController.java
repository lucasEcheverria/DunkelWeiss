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
        ThreadDTO created = threadService.createThread(new CreateThreadDTO(title, description, communityId));
        if (created == null) {
            return "redirect:/auth?error=true";
        }
        return "redirect:/home";
    }
}