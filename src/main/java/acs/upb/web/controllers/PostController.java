package acs.upb.web.controllers;

import acs.upb.web.dtos.CommentCreationDTO;
import acs.upb.web.dtos.PostCreationDto;
import acs.upb.web.dtos.PostDataDTO;
import acs.upb.web.errors.PostNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.services.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class PostController {
    private final JwtDecoder jwtDecoder;
    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> createPost(
            @RequestBody PostCreationDto postDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization) throws UserNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        postService.createPost(postDTO, jwt.getClaimAsString("id"));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PostDataDTO>> listPosts() {
        return ResponseEntity.ok(postService.listPosts());
    }

    @GetMapping("/following")
    public ResponseEntity<List<PostDataDTO>> listPostsOfFriends(
            @RequestHeader(value = "Authorization", required = false) String authorization) throws UserNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        return ResponseEntity.ok(postService.getFeedPosts(jwt.getClaimAsString("id")));
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePost(
            @RequestParam String uid,
            @RequestHeader(value = "Authorization", required = false) String authorization) throws PostNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        if (!jwt.getClaimAsString("role").equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        postService.deletePost(uid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment")
    public ResponseEntity<Void> addCommentToPost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String postId,
            @RequestBody CommentCreationDTO commentDTO)
            throws PostNotFoundError, UserNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        postService.addCommentToPost(jwt.getClaimAsString("id"), postId, commentDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find")
    public ResponseEntity<PostDataDTO> getPostById(@RequestParam String postId) throws PostNotFoundError {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/find/{userId}")
    public ResponseEntity<List<PostDataDTO>> getPostsByUserId(@PathVariable String userId) throws UserNotFoundError {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }
}
