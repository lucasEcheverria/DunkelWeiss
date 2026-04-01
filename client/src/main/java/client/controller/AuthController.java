package client.controller;

import client.service.AuthServiceProxy;
import lib.dto.UserCredentialsDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de autenticación (patrón MVC).
 *
// * Gestiona las rutas relacionadas con el login, registro y la página principal.
 * Delega la lógica de negocio en AuthService, manteniéndose como capa de presentación.
 */
@Controller
public class AuthController {

    private final AuthServiceProxy authService;

    public AuthController(AuthServiceProxy authService) {
        this.authService = authService;
    }

    /**
     * Muestra la página de autenticación (sign in por defecto).
     * Si viene con ?error=true, añade el mensaje de error al modelo para
     * que Thymeleaf lo muestre en la vista.
     *
     * @param error  parámetro opcional de URL que indica si hubo un error previo
     * @param mode   parámetro opcional que indica si mostrar "signin" o "signup"
     * @param model  modelo de datos para la plantilla Thymeleaf
     * @return nombre de la plantilla a renderizar
     */
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

    /**
     * Procesa el formulario de inicio de sesión.
     *
     * Recoge username y password del formulario, construye el DTO y llama
     * a AuthService. Si el servidor confirma las credenciales (200 OK),
     * redirige a la página principal. Si falla, vuelve al login con error.
     *
     * @param email    correo electrónico del usuario
     * @param password contraseña introducida en el formulario
     * @return redirección a /home si OK, o a /?error=true si falla
     */
    @PostMapping("/login")
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
        return "redirect:/?error=true&mode=signin";
    }

    /**
     * Procesa el formulario de registro de nuevo usuario.
     *
     * Recoge username y password del formulario, construye el DTO y llama
     * a AuthService. Si el servidor crea el usuario (201 Created), redirige
     * al login para que el usuario inicie sesión. Si falla (usuario ya existe
     * u otro error), vuelve al formulario de registro con error.
     *
     * @param email    correo electrónico del usuario
     * @param username nombre de usuario deseado
     * @param password contraseña deseada
     * @return redirección a / (sign in) si OK, o a /?mode=signup&error=true si falla
     */
    @PostMapping("/register")
    public String register(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        // Construct DTO and set email so the proxy sends it to the server
        UserCredentialsDTO credentials = new UserCredentialsDTO(email, username, password);
        boolean success = authService.register(credentials);
        if (success) {
            return "redirect:/";
        }
        return "redirect:/?error=true&mode=signup";
    }

//    /**
//     * Muestra la página principal de la aplicación.
//     * Solo accesible tras un login exitoso (en una implementación completa
//     * estaría protegida por sesión/Spring Security).
//     *
//     * @return nombre de la plantilla home
//     */
//    @GetMapping("/home")
//    public String home() {
//        return "home";
//    }
}