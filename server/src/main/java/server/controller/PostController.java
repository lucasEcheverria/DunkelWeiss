package server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.entity.User;
import server.service.AuthService;
import server.service.PostService;

import java.util.List;

@Tag(name = "Posts", description = "Gestión de posts dentro de hilos")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @Operation(summary = "Obtener posts de un hilo")
    @GetMapping(value = "/thread/{threadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPostsByThread(@PathVariable Integer threadId) {
        try {
            List<PostDTO> posts = postService.getPostsByThread(threadId);
            return ResponseEntity.ok(posts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Crear un post en un hilo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post creado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Token inválido"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreatePostDTO dto) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para publicar.");
        }

        String token = authHeader.substring(7);
        User user = authService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tu sesión no es válida. Vuelve a iniciar sesión.");
        }

        try {
            PostDTO created = postService.createPost(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
