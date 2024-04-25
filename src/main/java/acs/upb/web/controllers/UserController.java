package acs.upb.web.controllers;

import acs.upb.web.dtos.UserAuthDTO;
import acs.upb.web.dtos.UserDataDTO;
import acs.upb.web.dtos.UserUpdateDto;
import acs.upb.web.errors.UserAlreadyExistsError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    @Value("${app.adminKey}")
    private String adminKey;

    private final JwtDecoder jwtDecoder;

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> addUser(
            @RequestHeader(value = "x-admin-key", required = false) String adminKey,
            @RequestBody UserAuthDTO user)
            throws UserAlreadyExistsError {
        boolean isAdmin = this.adminKey.equals(adminKey);
        userService.addUser(user, isAdmin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Token> loginUser(@RequestBody UserAuthDTO user) throws UserNotFoundError {
        var loginResult = userService.loginUser(user);
        if (loginResult.equals("Wrong password")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new Token(loginResult));
    }

    @PatchMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> updateUser(@RequestBody UserUpdateDto user) throws UserNotFoundError {
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<UserDataDTO>> users() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{uid}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDataDTO> getUser(@PathVariable String uid) throws UserNotFoundError {
        return ResponseEntity.ok(userService.getUserById(uid));
    }

    @DeleteMapping("/{uid}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String uid) throws UserNotFoundError {

        if (!jwtDecoder.decode(authorization.substring(7)).getClaimAsString("scope").equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.deleteUser(uid);
        return ResponseEntity.ok().build();
    }

    public record Token(String token) {}
}
