package server.service;

import lib.dto.CreatePostDTO;
import lib.dto.PostDTO;
import org.springframework.stereotype.Service;
import server.dao.PostRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Post;
import server.entity.Thread;
import server.entity.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       ThreadRepository threadRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
    }

    public List<PostDTO> getPostsByThread(Integer threadId) {
        // Validate thread exists
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread no encontrado: " + threadId));

        // Fetch only root posts (parentPost is null) ordered by id — handles threads with no posts
        List<Post> posts = postRepository.findRootPostsByThreadId(threadId);
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        return posts.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PostDTO createPost(CreatePostDTO dto, User owner) {
        Thread thread = threadRepository.findById(dto.threadId())
                .orElseThrow(() -> new IllegalArgumentException("Thread no encontrado: " + dto.threadId()));

        Post post = new Post();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setOwner(owner);
        post.setThread(thread);
        post.setLikes(0);
        post.setDislikes(0);

        if (dto.parentId() != null) {
            Post parent = postRepository.findById(dto.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent post no encontrado: " + dto.parentId()));
            post.setParentPost(parent);
        }

        Post saved = postRepository.save(post);
        return toDto(saved);
    }

    private PostDTO toDto(Post p) {
        List<PostDTO> replies = p.getReplies() == null ? List.of() : p.getReplies().stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toList());

        String ownerUsername = p.getOwner() != null ? p.getOwner().getNickname() : null;
        Integer parentId = p.getParentPost() != null ? p.getParentPost().getId() : null;
        Integer threadId = p.getThread() != null ? p.getThread().getId() : null;

        return new PostDTO(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                ownerUsername,
                threadId,
                parentId,
                p.getLikes(),
                p.getDislikes(),
                replies
        );
    }
}