package client.controller;

import client.service.AuthServiceProxy;
import client.service.UserServiceProxy;
import client.service.CommunityServiceProxy;
import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import lib.dto.CommunityDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controlador de perfil de usuario (patrón MVC).
 */
@Controller
public class UserController {

    private final AuthServiceProxy authService;
    private final UserServiceProxy userService;
    private final CommunityServiceProxy communityService;

    public UserController(AuthServiceProxy authService, UserServiceProxy userService, CommunityServiceProxy communityService) {
        this.authService = authService;
        this.userService = userService;
        this.communityService = communityService;
    }

    @GetMapping("/profile")
    public String profile(Model model,
                          @RequestParam(value = "tab", required = false) String tab) {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/"; // No se ha identificado, redirige al login
        }

        UserDTO user = userService.getCurrentUser(token);
        if (user == null) {
            return "redirect:/?error=true";
        }

        // Añadimos las comunidades del usuario al modelo para que el template pueda renderizarlas
        List<CommunityDTO> communities = communityService.getMyCommunities(token);

        model.addAttribute("user", user);
        model.addAttribute("communities", communities);
        // pass the requested tab (may be null) so the template can decide which tab to show
        model.addAttribute("activeTab", tab);
        return "profile"; // Necesita template profile.html
    }

    @PostMapping("/profile/leaveCommunity")
    public String leaveCommunity(@RequestParam("communityId") Integer communityId) {
        String token = authService.getToken();
        if (token == null) return "redirect:/";

        boolean ok = communityService.leaveCommunity(token, communityId);
        if (!ok) {
            return "redirect:/profile?error=true";
        }
        // After leaving, redirect to profile and show the communities tab so the user can see the updated list
        return "redirect:/profile?tab=communities";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "password", required = false) String password) {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/";
        }

        UpdateUserDTO dto = new UpdateUserDTO(nickname, password);

        UserDTO updated = userService.updateUser(dto);
        if (updated == null) {
            return "redirect:/profile?error=true";
        }

        return "redirect:/profile";
    }
}