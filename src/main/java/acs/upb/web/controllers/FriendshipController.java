package acs.upb.web.controllers;

import acs.upb.web.dtos.UserDataDTO;
import acs.upb.web.errors.FriendshipAlreadyExistsError;
import acs.upb.web.errors.FriendshipNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.services.FriendshipService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friendships")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class FriendshipController {
    private final JwtDecoder jwtDecoder;
    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<UserDataDTO>> listFriendsOfUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) throws UserNotFoundError {
        String userId = jwtDecoder.decode(authorization.substring(7)).getClaimAsString("id");
        return ResponseEntity.ok(friendshipService.listFriendsOfUser(userId));
    }

    @PostMapping
    public ResponseEntity<Void> createFriendship(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String receiver)
            throws UserNotFoundError, FriendshipAlreadyExistsError {
        String sender = jwtDecoder.decode(authorization.substring(7)).getClaimAsString("id");
        friendshipService.createFriendship(sender, receiver);
        return ResponseEntity.ok().build();
    }

    @PatchMapping()
    public ResponseEntity<Void> acceptFriendship(
            @RequestParam String sender,
            @RequestHeader(value = "Authorization", required = false) String authorization)
            throws FriendshipNotFoundError, UserNotFoundError {
        String receiver = jwtDecoder.decode(authorization.substring(7)).getClaimAsString("id");
        friendshipService.acceptFriendship(sender, receiver);
        return ResponseEntity.ok().build();
    }
}
