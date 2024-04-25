package acs.upb.web.controllers;

import acs.upb.web.errors.PostNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.services.LikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class LikeController {
    private final JwtDecoder jwtDecoder;
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Void> likePost(
            @RequestParam @NotNull @Valid String postId,
            @RequestHeader(value = "Authorization", required = false) String authorization)
            throws UserNotFoundError, PostNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        likeService.addLikeToPost(postId, jwt.getClaimAsString("id"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikePost(
            @RequestParam @NotNull @Valid String postId,
            @RequestHeader(value = "Authorization", required = false) String authorization)
            throws UserNotFoundError, PostNotFoundError {
        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
        likeService.removeLikeFromPost(postId, jwt.getClaimAsString("id"));
        return ResponseEntity.ok().build();
    }
}
