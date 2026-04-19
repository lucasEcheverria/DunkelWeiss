package server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.CommunityService;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@Tag(name = "Comunidades", description = "Operaciones relacionadas con comunidades")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Operation(
            summary = "Listar todas las comunidades",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de comunidades")
            }
    )
    @GetMapping
    public ResponseEntity<List<CommunityDTO>> getAll() {
        return ResponseEntity.ok(communityService.getAll());
    }

    @Operation(
            summary = "Crear una comunidad",
            description = "Crea una nueva comunidad y asigna automáticamente al creador como primer miembro.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comunidad creada correctamente"),
                    @ApiResponse(responseCode = "401", description = "Token inválido o no proporcionado")
            }
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createCommunity(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateCommunityDTO dto) {
        try {
            String token = authHeader.replace("Bearer ", "");
            CommunityDTO created = communityService.createCommunity(dto, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Top 5 comunidades",
            description = "Devuelve las 5 comunidades con más miembros. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de comunidades")
            }
    )
    @GetMapping("/top5")
    public ResponseEntity<List<CommunityDTO>> getTop5() {
        return ResponseEntity.ok(communityService.getTop5ByPopularity());
    }

    @GetMapping("/my_communities")
    public ResponseEntity<List<CommunityDTO>> getMyCommunities(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            List<CommunityDTO> myCommunities = communityService.getUserCommunities(token);
            return ResponseEntity.ok(myCommunities);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}