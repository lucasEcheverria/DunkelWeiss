package client.controller;

import client.service.HiloServiceProxy;
import lib.dto.ThreadDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * Controlador de búsqueda de hilos (patrón MVC).
 *
 * Gestiona las rutas relacionadas con la búsqueda de hilos.
 * Delega la lógica de negocio en HiloService,
 * manteniéndose como capa de presentación.
 */
@Controller
public class HiloController {

    private final HiloServiceProxy hiloService;

    public HiloController(HiloServiceProxy hiloService) {
        this.hiloService = hiloService;
    }

    /**
     * Muestra la página de búsqueda de hilos.
     * Si viene con ?q=texto realiza la búsqueda y añade los resultados al modelo.
     *
     * @param query texto a buscar, vacío por defecto
     * @param model modelo de datos para la plantilla Thymeleaf
     * @return nombre de la plantilla a renderizar
     */
    @GetMapping("/search")
    public String buscarHilos(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            Model model) {
        List<ThreadDTO> hilos = hiloService.buscarHilos(query);
        model.addAttribute("hilos", hilos);
        model.addAttribute("query", query);
        return "search";
    }

}