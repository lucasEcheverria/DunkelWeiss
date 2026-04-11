package server.service;

import lib.dto.CommunityDTO;
import org.springframework.stereotype.Service;
import server.dao.CommunityRepository;

import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;

    public CommunityService(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    public List<CommunityDTO> getAll() {
        return communityRepository.findAll()
                .stream()
                .map(c -> new CommunityDTO(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }
}