package server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import server.dao.UserRepository;
import server.entity.User;
import lib.dto.UserCredentialsDTO;

@Service
public class AuthService {
	
	private final UserRepository userRepository;
	
	private static Map<String, User> tokenStorage = new HashMap<>();
	
	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	// Método de inicio de sesión
	public Optional<String> login(String email, String password) {
		Optional<User> useropt = userRepository.findByEmail(email);
		
		if (useropt.isPresent()) {
			User user = useropt.get();
			if (user.checkPassword(password)) {
				String token = generateToken(user);
				tokenStorage.put(token, user);
				return Optional.of(token);
			}
			else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	// Método de cierre de sesión
	public Optional<Boolean> logout(String token) {
		if (tokenStorage.containsKey(token)) {
			tokenStorage.remove(token);
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
		
	// Método de generación de tokens	
	private static synchronized String generateToken(User user) {
		return Long.toHexString(System.currentTimeMillis());
	}
	
	public void addUser(User user) {
		userRepository.save(user);
	}

	/**
	 * Registra un nuevo usuario con las credenciales dadas.
	 * Si ya existe un usuario con ese email, devuelve Optional.empty().
	 * En caso contrario guarda y devuelve el usuario creado.
	 */
	public Optional<User> register(UserCredentialsDTO credentials) {
		// Comprobar si ya existe un usuario con el mismo email
		Optional<User> existing = userRepository.findByEmail(credentials.getEmail());
		if (existing.isPresent()) {
			return Optional.empty();
		}

		// Crear y guardar el nuevo usuario
		User user = new User(credentials.getEmail(), credentials.getUsername(), credentials.getPassword());
		User saved = userRepository.save(user);
		return Optional.of(saved);
	}
	
	// Obtener usuario mediante el token
	public User getUserByToken(String token) {
		return tokenStorage.get(token);
	}
		
}