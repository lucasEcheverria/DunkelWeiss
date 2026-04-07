package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.Thread;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Integer> {

    List<Thread> findByTitleContainingIgnoreCase(String title);
    List<Thread> findByOwnerEmail(String email);

}
