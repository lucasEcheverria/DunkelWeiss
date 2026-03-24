package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.Hilo;
import java.util.List;

@Repository
public interface HiloRepository extends JpaRepository<Hilo, Integer> {

    List<Hilo> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description
    );


}
