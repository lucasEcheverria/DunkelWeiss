package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.entity.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    // Return only root (top-level) posts for a thread, ordered by id asc
    @Query("SELECT p FROM Post p WHERE p.thread.id = :threadId AND p.parentPost IS NULL ORDER BY p.id ASC")
    List<Post> findRootPostsByThreadId(@Param("threadId") Integer threadId);
}