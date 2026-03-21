package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.Hilo;

@Repository
public interface HiloRepository extends JpaRepository<Hilo, Integer> {

}
