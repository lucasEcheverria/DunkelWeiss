package server.service;

import lib.dto.CommunityDTO;
import lib.dto.CreateCommunityDTO;
import org.springframework.stereotype.Service;
import server.dao.CommunityRepository;
import server.dao.UserRepository;
import server.entity.Community;
import server.entity.User;

import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public CommunityService(CommunityRepository communityRepository,
                            UserRepository userRepository,
                            AuthService authService) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public List<CommunityDTO> getAll() {
        return communityRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CommunityDTO createCommunity(CreateCommunityDTO dto, String token) {
        User cachedUser = authService.getUserByToken(token);
        if (cachedUser == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        User creator = userRepository.findById(cachedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Community community = new Community();
        community.setName(dto.name());
        community.setDescription(dto.description());
        Community saved = communityRepository.save(community);

        creator.getcommunities().add(saved);
        userRepository.save(creator);

        return toDto(saved);
    }

    public List<CommunityDTO> getTop5ByPopularity() {
        return communityRepository.findTop5ByMemberCount()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<CommunityDTO> getUserCommunities(String token) {
        User cachedUser = authService.getUserByToken(token);
        if (cachedUser == null) {
            throw new IllegalArgumentException("Invalid Token");
        }

        User user = userRepository.findById(cachedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getcommunities()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CommunityDTO toDto(Community c) {
        return new CommunityDTO(c.getId(), c.getName(), c.getDescription());
    }
}