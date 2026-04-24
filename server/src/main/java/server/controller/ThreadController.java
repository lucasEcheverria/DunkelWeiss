package server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.entity.Thread;
import server.entity.User;
import server.service.AuthService;
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
    private final AuthService authService;

    public ThreadController(ThreadService threadService, AuthService authService) {
        this.authService = authService;
        this.threadService = threadService;
    }

    @Operation(
            summary = "Crear un hilo",
            description = "Crea un nuevo hilo de discusión asociado a una comunidad. El propietario se resuelve automáticamente desde el token de sesión."
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
                    description = "La comunidad indicada no existe",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Community no encontrada: 99")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado: falta el token o no tiene formato Bearer",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Token inválido o sesión expirada",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
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
                          "comunidadId": 1
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createHilo(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateThreadDTO dto) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Debes iniciar sesión para crear un hilo.");
        }

        String token = authHeader.substring(7);
        User user = authService.getUserByToken(token);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Tu sesión no es válida. Vuelve a iniciar sesión.");
        }

        try {
            ThreadDTO created = threadService.createThread(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<ThreadDTO>> getThreadsWithPrompt(
            @Parameter(description = "Texto a buscar en título", example = "IDE", required = true)
            @RequestParam String query) {
        List<Thread> threads = threadService.getThreadsWithPrompt(query);
        List<ThreadDTO> result = threads.stream()
                .map(thread -> new ThreadDTO(
                        thread.getId(),
                        thread.getTitle(),
                        thread.getDescription(),
                        thread.getOwner()     != null ? thread.getOwner().getNickname()   : null,
                        thread.getCommunity() != null ? thread.getCommunity().getName() : null
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ThreadDTO>> getThreadsFromUser(
            @Parameter(description = "Email del propietario", example = "test@test.com", required = true)
            @RequestParam String email) {
        List<Thread> threads = threadService.getThreadsFromUser(email);
        List<ThreadDTO> result = threads.stream()
                .map(thread -> new ThreadDTO(
                        thread.getId(),
                        thread.getTitle(),
                        thread.getDescription(),
                        thread.getOwner()     != null ? thread.getOwner().getNickname()   : null,
                        thread.getCommunity() != null ? thread.getCommunity().getName() : null
                ))
                .toList();
        return ResponseEntity.ok(result);
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
            ThreadDTO hilo = threadService.getThread(id);
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

    @GetMapping("/thread_feed")
    public ResponseEntity<List<ThreadSummaryDTO>> getThreadFeed() {
        return ResponseEntity.ok(threadService.getInitialFeed());
    }

    @Operation(
            summary = "Obtener hilos favoritos del usuario autenticado",
            description = "Devuelve la lista de hilos que el usuario ha marcado como favoritos. Requiere token de sesión."
    )
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoriteThreads(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        User user = authService.getUserByToken(token);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Thread> threads = threadService.getFavoriteThreads(user);
        List<ThreadDTO> result = threads.stream()
                .map(thread -> new ThreadDTO(
                        thread.getId(),
                        thread.getTitle(),
                        thread.getDescription(),
                        thread.getOwner()     != null ? thread.getOwner().getNickname()   : null,
                        thread.getCommunity() != null ? thread.getCommunity().getName() : null
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
    
    @Operation(
            summary = "Añadir un hilo a favoritos",
            description = "Marca un hilo como favorito para el usuario autenticado. Requiere token de sesión."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hilo añadido a favoritos"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token o no tiene formato Bearer"),
            @ApiResponse(responseCode = "403", description = "Token inválido o sesión expirada")
    })
    @PostMapping("/favorites/{id}")
    public ResponseEntity<?> addFavorite(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "ID del hilo a añadir a favoritos", required = true)
            @PathVariable Integer id) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        User user = authService.getUserByToken(token);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            threadService.addFavoriteThread(user, id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @Operation(
            summary = "Quitar un hilo de favoritos",
            description = "Quita un hilo de la lista de favoritos del usuario autenticado. Requiere token de sesión."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hilo eliminado de favoritos"),
            @ApiResponse(responseCode = "400", description = "El hilo no está en favoritos o solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token o no tiene formato Bearer"),
            @ApiResponse(responseCode = "403", description = "Token inválido o sesión expirada")
    })
    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<?> removeFavorite(
            @Parameter(
                    name = "Authorization",
                    description = "Token de sesión en formato Bearer <token>",
                    required = true,
                    in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
            )
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "ID del hilo a quitar de favoritos", required = true)
            @PathVariable Integer id) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        User user = authService.getUserByToken(token);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            threadService.removeFavoriteThread(user, id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}