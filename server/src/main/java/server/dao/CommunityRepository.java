package server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.Community;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
}
