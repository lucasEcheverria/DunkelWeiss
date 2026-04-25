package client.controller;

import client.service.AuthServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import client.service.PostServiceProxy;
import lib.dto.CommunityDTO;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import lib.dto.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/threads")
public class ThreadController {

    private final ThreadServiceProxy threadService;
    private final AuthServiceProxy authService;
    private final CommunityServiceProxy communityService;
    // make postService optional for test contexts
    @Autowired(required = false)
    private PostServiceProxy postService;

    public ThreadController(ThreadServiceProxy threadService,
                            AuthServiceProxy authService,
                            CommunityServiceProxy communityService) {
        this.threadService = threadService;
        this.authService = authService;
        this.communityService = communityService;
    }

    @ModelAttribute
    public void addCommunities(Model model){
        String token = authService.getToken();
        List<CommunityDTO> top5 = communityService.getTop5();
        model.addAttribute("top5Communities", top5);
        if (token != null) {
            // LOGUEADO: Pedimos sus comunidades
            List<CommunityDTO> myCommunities = communityService.getMyCommunities(token);
            model.addAttribute("myCommunities", myCommunities);
        }
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
        // Load posts for this thread (may return empty list)
        List<PostDTO> posts = Collections.emptyList();
        if (postService != null) {
            posts = postService.getPostsByThread(id);
        }
        model.addAttribute("posts", posts);
        // Return the existing template name (templates/threadDetail.html)
        return "threadDetail";
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
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            Model model) {

        List<ThreadSummaryDTO> threads = threadService.getThreadsWithPrompt(query).stream()
                .map(t -> new ThreadSummaryDTO(t.id(), t.title(), t.description(), t.ownerUsername()))
                .toList();
        model.addAttribute("threadFeedList", threads);
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