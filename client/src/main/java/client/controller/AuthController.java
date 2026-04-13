package client.controller;

import client.service.AuthServiceProxy;
import lib.dto.UserCredentialsDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthServiceProxy authService;

    public AuthController(AuthServiceProxy authService) {
        this.authService = authService;
    }

    @GetMapping("/auth")
    public String authPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "mode", defaultValue = "signin") String mode,
            Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        model.addAttribute("mode", mode);
        return "auth";
    }

    @PostMapping("/auth/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {
        UserCredentialsDTO credentials = new UserCredentialsDTO();
        credentials.setEmail(email);
        credentials.setPassword(password);
        boolean success = authService.login(credentials);
        if (success) {
            return "redirect:/home";
        }
        return "redirect:/auth?error=true&mode=signin";
    }

    @PostMapping("/auth/register")
    public String register(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        UserCredentialsDTO credentials = new UserCredentialsDTO(email, username, password);
        boolean success = authService.register(credentials);
        if (success) {
            return "redirect:/auth";
        }
        return "redirect:/auth?error=true&mode=signup";
    }
}