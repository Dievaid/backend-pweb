package acs.upb.web.services;

import acs.upb.web.entities.Like;
import acs.upb.web.errors.PostNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.repositories.LikeRepository;
import acs.upb.web.repositories.PostRepository;
import acs.upb.web.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;

    public void addLikeToPost(String postId, String userId) throws UserNotFoundError, PostNotFoundError {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundError::new);
        var post = postRepository.findById(postId).orElseThrow(PostNotFoundError::new);

        post.setLikeCount(post.getLikeCount() + 1);

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        likeRepository.save(like);
        postRepository.save(post);

        rabbitTemplate.convertAndSend(
                topicExchange.getName(),
                String.format("queue.user.%s", post.getUser().getUid()),
                String.format("%s has liked your post", userId)
        );
    }

    public void removeLikeFromPost(String postId, String userId) throws UserNotFoundError, PostNotFoundError {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundError::new);
        var post = postRepository.findById(postId).orElseThrow(PostNotFoundError::new);
        var like = likeRepository.findByPostAndUser(post, user).orElseThrow();

        post.setLikeCount(post.getLikeCount() - 1);

        likeRepository.delete(like);
        postRepository.save(post);
        rabbitTemplate.convertAndSend(
                topicExchange.getName(),
                String.format("queue.user.%s", post.getUser().getUid()),
                String.format("%s has unliked your post", userId)
        );
    }
}
