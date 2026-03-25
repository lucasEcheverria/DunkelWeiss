package server.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	// Método personalizado para buscar un usuario por su email
	Optional<User> findByEmail(String email);
}
