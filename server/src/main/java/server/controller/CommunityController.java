package server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lib.dto.CommunityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}