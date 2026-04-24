package client.controller;

import client.service.AuthServiceProxy;
import client.service.UserServiceProxy;
import client.service.CommunityServiceProxy;
import client.service.ThreadServiceProxy;
import lib.dto.UpdateUserDTO;
import lib.dto.UserDTO;
import lib.dto.CommunityDTO;
import lib.dto.ThreadDTO;
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
    private final ThreadServiceProxy threadService;

    public UserController(AuthServiceProxy authService, UserServiceProxy userService, CommunityServiceProxy communityService, ThreadServiceProxy threadService) {
        this.authService = authService;
        this.userService = userService;
        this.communityService = communityService;
        this.threadService = threadService;
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

        // Añadimos las comunidades y los hilos del usuario al modelo para que el template pueda renderizarlas
        List<CommunityDTO> communities = communityService.getMyCommunities(token);
        List<ThreadDTO> createdThreads = threadService.getThreadsFromUser(user.getEmail());
        List<ThreadDTO> favoriteThreads = threadService.getFavoriteThreads();

        model.addAttribute("user", user);
        model.addAttribute("communities", communities);
        model.addAttribute("createdThreads", createdThreads);
        model.addAttribute("favoriteThreads", favoriteThreads);
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

    @PostMapping("/profile/favorite")
    public String favoriteThread(@RequestParam("threadId") Integer threadId) {
        String token = authService.getToken();
        if (token == null) return "redirect:/";

        boolean ok = threadService.addFavorite(threadId);
        if (!ok) {
            return "redirect:/profile?tab=threads&error=true";
        }
        return "redirect:/profile?tab=threads";
    }

    @PostMapping("/profile/unfavorite")
    public String unfavoriteThread(@RequestParam("threadId") Integer threadId) {
        String token = authService.getToken();
        if (token == null) return "redirect:/";

        boolean ok = threadService.removeFavorite(threadId);
        if (!ok) {
            return "redirect:/profile?tab=threads&error=true";
        }
        return "redirect:/profile?tab=threads";
    }
}