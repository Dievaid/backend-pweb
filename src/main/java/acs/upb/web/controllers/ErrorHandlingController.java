package acs.upb.web.controllers;

import acs.upb.web.dtos.ErrorDto;
import acs.upb.web.errors.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandlingController {

    @ExceptionHandler(UserAlreadyExistsError.class)
    public ResponseEntity<ErrorDto> handleUserExists(UserAlreadyExistsError error) {
        return ResponseEntity.ok(new ErrorDto("A user already exists with the chosen username."));
    }

    @ExceptionHandler(UserNotFoundError.class)
    public ResponseEntity<ErrorDto> handleUserNotExists(UserNotFoundError error) {
        return ResponseEntity.ok(new ErrorDto("The user does not exist."));
    }

    @ExceptionHandler(PostNotFoundError.class)
    public ResponseEntity<ErrorDto> handlePostNotExists(PostNotFoundError error) {
        return ResponseEntity.ok(new ErrorDto("The post does not exist."));
    }

    @ExceptionHandler(FriendshipAlreadyExistsError.class)
    public ResponseEntity<ErrorDto> handleFriendshipExists(FriendshipAlreadyExistsError error) {
        return ResponseEntity.ok(new ErrorDto("The friend request already exists."));
    }

    @ExceptionHandler(FriendshipNotFoundError.class)
    public ResponseEntity<ErrorDto> handleFriendshipNotExists(FriendshipNotFoundError error) {
        return ResponseEntity.ok(new ErrorDto("The friend request does not exist."));
    }
}
