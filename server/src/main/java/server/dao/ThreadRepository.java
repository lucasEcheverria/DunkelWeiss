package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import server.entity.Thread;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Integer> {

    List<Thread> findByTitleContainingIgnoreCase(String title);
    List<Thread> findByOwnerEmail(String email);
    @Query("SELECT t FROM Thread t LEFT JOIN t.posts p GROUP BY t ORDER BY COUNT(p) DESC LIMIT 10")
    List<Thread> findTop10ByPostCount();

}
