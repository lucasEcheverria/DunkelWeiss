package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import server.entity.Community;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {

    @Query("SELECT c FROM Community c LEFT JOIN c.users u GROUP BY c ORDER BY COUNT(u) DESC LIMIT 5")
    List<Community> findTop5ByMemberCount();
}
