package client.controller;

import lib.dto.CreateHiloDto;
import lib.dto.HiloDto;
import lib.dto.HiloSummaryDto;
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
        List<HiloSummaryDto> hilos = threadService.getAllSummaries();
        model.addAttribute("hilos", hilos);
        return "threads/list";
    }

    @GetMapping("/{id}")
    public String getThread(@PathVariable Integer id, Model model) {
        HiloDto hilo = threadService.getHilo(id);
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
        threadService.createHilo(new CreateHiloDto(title, description, comunidadId, ownerId));
        return "redirect:/threads";
    }
}