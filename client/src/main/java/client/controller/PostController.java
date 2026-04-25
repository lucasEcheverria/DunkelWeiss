package client.controller;

import client.service.AuthServiceProxy;
import client.service.PostServiceProxy;
import lib.dto.CreatePostDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {

    private final PostServiceProxy postService;
    private final AuthServiceProxy authService;

    public PostController(PostServiceProxy postService, AuthServiceProxy authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @PostMapping("/posts/create")
    public String createPost(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String content,
                             @RequestParam Integer threadId,
                             @RequestParam(required = false) Integer parentId) {

        // require authentication
        if (authService.getToken() == null) {
            return "redirect:/auth";
        }

        // If title is missing (e.g. when replying), use a non-blank default so server validation passes
        String safeTitle = (title == null || title.isBlank()) ? "Re:" : title;
        CreatePostDTO dto = new CreatePostDTO(safeTitle, content, threadId, parentId);
        postService.createPost(dto);
        return "redirect:/threads/" + threadId;
    }
}