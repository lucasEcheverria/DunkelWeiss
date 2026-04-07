package client.controller;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import client.service.ThreadServiceProxy;

import java.util.List;

@Controller
@RequestMapping("/threads")
public class ThreadController {

    private final ThreadServiceProxy threadService;

    public ThreadController(ThreadServiceProxy threadService) {
        this.threadService = threadService;
    }

    @GetMapping
    public String listThreads(Model model) {
        List<ThreadSummaryDTO> hilos = threadService.getAllSummaries();
        model.addAttribute("hilos", hilos);
        return "threads/list";
    }

    @GetMapping("/{id}")
    public String getThread(@PathVariable Integer id, Model model) {
        ThreadDTO hilo = threadService.getHilo(id);
        if (hilo == null) return "redirect:/threads";
        model.addAttribute("hilo", hilo);
        return "threads/detail";
    }

    @PostMapping("/create")
    public String createThread(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Integer comunidadId,
            @RequestParam Integer ownerId) {
        threadService.createHilo(new CreateThreadDTO(title, description, comunidadId, ownerId));
        return "redirect:/threads";
    }
    @GetMapping("/search")
    public String getThreadsWithPrompt(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            Model model) {
        List<ThreadSummaryDTO> threads = threadService.getThreadsWithPrompt(query).stream()
                .map(t -> new ThreadSummaryDTO(t.getId(), t.getTitle(), t.getDescription(), t.getOwnerUsername()))
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
                .map(t -> new ThreadSummaryDTO(t.getId(), t.getTitle(), t.getDescription(), t.getOwnerUsername()))
                .toList();
        model.addAttribute("threadFeedList", threads);
        return "home";
    }

}