package client.controller;

import client.service.AuthServiceProxy;
import client.service.UserServiceProxy;
import lib.dto.UpdateUserDto;
import lib.dto.UserDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de perfil de usuario (patrón MVC).
 */
@Controller
public class UserController {

    private final AuthServiceProxy authService;
    private final UserServiceProxy userService;

    public UserController(AuthServiceProxy authService, UserServiceProxy userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/"; // No se ha identificado, redirige al login
        }

        UserDto user = userService.getCurrentUser(token);
        if (user == null) {
            return "redirect:/?error=true";
        }

        model.addAttribute("user", user);
        return "profile"; // Necesita template profile.html
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "password", required = false) String password) {
        String token = authService.getToken();
        if (token == null) {
            return "redirect:/";
        }

        UpdateUserDto dto = new UpdateUserDto(nickname, password);

        UserDto updated = userService.updateUser(dto);
        if (updated == null) {
            return "redirect:/profile?error=true";
        }

        return "redirect:/profile";
    }
}