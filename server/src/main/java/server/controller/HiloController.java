package server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.entity.Hilo;
import server.service.HiloService;
import java.util.List;

/**
 * Controlador REST para la gestión de hilos.
 *
 * Expone los endpoints de la API relacionados con hilos,
 * delegando la lógica de negocio en HiloService.
 */
@RestController
@RequestMapping("/api/hilos")
public class HiloController {

    private final HiloService hiloService;

    public HiloController(HiloService hiloService) {
        this.hiloService = hiloService;
    }

    /**
     * Busca hilos por título o descripción.
     *
     * @param q texto a buscar
     * @return lista de hilos que coinciden con la búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<List<Hilo>> buscarHilos(@RequestParam String q) {
        return ResponseEntity.ok(hiloService.buscarHilos(q));
    }

}