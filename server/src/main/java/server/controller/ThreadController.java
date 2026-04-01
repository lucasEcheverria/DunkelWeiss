package server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.ThreadService;

import java.util.List;

@Tag(
        name = "Hilos",
        description = "Gestión de hilos de discusión dentro de comunidades"
)
@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @Operation(
            summary = "Crear un hilo",
            description = "Crea un nuevo hilo de discusión asociado a una comunidad y a un usuario propietario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Thread creado correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ThreadDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El usuario o la comunidad indicados no existen",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Usuario no encontrado: 99")
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos necesarios para crear el hilo",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateThreadDTO.class),
                    examples = @ExampleObject(
                            name = "Ejemplo básico",
                            value = """
                {
                  "title": "¿Cuál es el mejor IDE?",
                  "description": "Debatimos sobre IntelliJ, VS Code y Neovim.",
                  "comunidadId": 1,
                  "ownerId": 1
                }
                """
                    )
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createHilo(@RequestBody CreateThreadDTO dto) {
        try {
            ThreadDTO created = threadService.createHilo(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Obtener un hilo por ID",
            description = "Devuelve la información completa de un hilo: título, descripción, propietario y comunidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Thread encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ThreadDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe ningún hilo con ese ID",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Thread no encontrado: 99")
                    )
            )
    })
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getHilo(
            @Parameter(description = "ID del hilo a consultar", example = "1", required = true)
            @PathVariable Integer id
    ) {
        try {
            ThreadDTO hilo = threadService.getHilo(id);
            return ResponseEntity.ok(hilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Listar todos los hilos",
            description = "Devuelve un listado ligero con el ID y el título de todos los hilos existentes. " +
                    "Útil para renderizar índices o menús sin cargar el contenido completo."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de hilos (puede estar vacía si no hay ninguno)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ThreadSummaryDTO.class)
            )
    )
    @GetMapping("/getAll")
    public ResponseEntity<List<ThreadSummaryDTO>> getAllSummaries() {
        return ResponseEntity.ok(threadService.getAllSummaries());
    }

    // TODO IMPLEMENTATION LOGIC IS REQUIRED
    @GetMapping("/thread_feed")
    public ResponseEntity<List<ThreadSummaryDTO>> getThreadFeed() {
        return ResponseEntity.ok(threadService.getAllSummaries());
    }
}