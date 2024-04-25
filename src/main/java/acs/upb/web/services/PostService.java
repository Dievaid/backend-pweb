package acs.upb.web.services;

import acs.upb.web.dtos.CommentCreationDTO;
import acs.upb.web.dtos.PostCreationDto;
import acs.upb.web.dtos.PostDataDTO;
import acs.upb.web.entities.Comment;
import acs.upb.web.entities.Post;
import acs.upb.web.errors.PostNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.repositories.CommentRepository;
import acs.upb.web.repositories.FriendshipRepository;
import acs.upb.web.repositories.PostRepository;
import acs.upb.web.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public void createPost(PostCreationDto postDTO, String uid) throws UserNotFoundError {
        var post = modelMapper.map(postDTO, Post.class);
        post.setCreatedAt(Timestamp.from(Instant.now()));
        post.setUser(userRepository.findById(uid).orElseThrow(UserNotFoundError::new));
        postRepository.save(post);
    }

    public List<PostDataDTO> listPosts() {
        var posts = new ArrayList<>(StreamSupport.stream(postRepository.findAll().spliterator(), false)
                .map(post -> modelMapper.map(post, PostDataDTO.class)).toList());
        Collections.shuffle(posts);
        return posts;
    }

    public List<PostDataDTO> getFeedPosts(String uid) throws UserNotFoundError {
        var user = userRepository.findById(uid).orElseThrow(UserNotFoundError::new);

        var friendships = friendshipRepository.findBySenderOrReceiverAndIsAccepted(user, user, true);
        var users = friendships.stream()
                .map(friendship -> {
                    if (friendship.getSender().equals(user)) {
                        return friendship.getReceiver();
                    }
                    return friendship.getSender();
                }).toList();

        return postRepository.findAllByUserIn(users).stream().map(post -> modelMapper.map(post, PostDataDTO.class)).toList();
    }

    public void addCommentToPost(String userId, String postUid, CommentCreationDTO commentDTO) throws PostNotFoundError, UserNotFoundError {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundError::new);
        var post = postRepository.findById(postUid).orElseThrow(PostNotFoundError::new);
        var comment = commentRepository.save(modelMapper.map(commentDTO, Comment.class));
        comment.setUser(user);
        comment.setCreatedAt(Timestamp.from(Instant.now()));
        post.getComments().add(comment);
        postRepository.save(post);
    }

    public void deletePost(String postUid) throws PostNotFoundError {
        var post = postRepository.findById(postUid).orElseThrow(PostNotFoundError::new);
        postRepository.delete(post);
    }

    public PostDataDTO getPostById(String postUid) throws PostNotFoundError {
        return modelMapper.map(postRepository.findById(postUid).orElseThrow(PostNotFoundError::new), PostDataDTO.class);
    }

    public List<PostDataDTO> getPostsByUserId(String userId) throws UserNotFoundError {
        return postRepository
                .findAllByUserIn(List.of(userRepository.findById(userId).orElseThrow(UserNotFoundError::new)))
                .stream().map(post -> modelMapper.map(post, PostDataDTO.class))
                .toList();
    }
}
