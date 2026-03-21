package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.Comunidad;

@Repository
public interface ComunidadRepository extends JpaRepository<Comunidad, Integer> {
}
