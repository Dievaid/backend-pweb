package acs.upb.web.services;

import acs.upb.web.dtos.UserDataDTO;
import acs.upb.web.entities.Friendship;
import acs.upb.web.errors.FriendshipAlreadyExistsError;
import acs.upb.web.errors.FriendshipNotFoundError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.repositories.FriendshipRepository;
import acs.upb.web.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;
    private final ModelMapper modelMapper;

    public void createFriendship(String sender, String receiver) throws FriendshipAlreadyExistsError, UserNotFoundError {
        var senderData = userRepository.findById(sender).orElseThrow(UserNotFoundError::new);
        var receiverData = userRepository.findById(receiver).orElseThrow(UserNotFoundError::new);

        if (friendshipRepository.findBySenderAndReceiver(senderData, receiverData).isPresent()) {
            throw new FriendshipAlreadyExistsError();
        }

        if (friendshipRepository.findBySenderAndReceiver(receiverData, receiverData).isPresent()) {
            throw new FriendshipAlreadyExistsError();
        }

        Friendship friendship = new Friendship();
        friendship.setSender(senderData);
        friendship.setReceiver(receiverData);
        friendship.setAccepted(false);
        friendshipRepository.save(friendship);

        rabbitTemplate.convertAndSend(
                topicExchange.getName(),
                String.format("queue.user.%s", receiverData.getUid()),
                String.format("%s sent you a follow request", senderData.getUid())
        );
    }

    public void acceptFriendship(String sender, String receiver) throws FriendshipNotFoundError, UserNotFoundError {
        var senderData = userRepository.findById(sender).orElseThrow(UserNotFoundError::new);
        var receiverData = userRepository.findById(receiver).orElseThrow(UserNotFoundError::new);

        var friendships = friendshipRepository.findBySenderAndReceiver(senderData, receiverData);

        var friendship = friendships.stream().findFirst().orElseThrow(FriendshipNotFoundError::new);
        friendship.setAccepted(true);
        friendship.setCreatedAt(Timestamp.from(Instant.now()));
        friendshipRepository.save(friendship);

        rabbitTemplate.convertAndSend(
                topicExchange.getName(),
                String.format("queue.user.%s", senderData.getUid()),
                String.format("%s accepted your follow request", receiverData.getUid())
        );
    }

    public List<UserDataDTO> listFriendsOfUser(String userId) throws UserNotFoundError {
        var foundUser = userRepository.findById(userId).orElseThrow(UserNotFoundError::new);
        var friends = friendshipRepository.findBySenderOrReceiverAndIsAccepted(foundUser, foundUser, true);

        return friends.stream()
                .map(friendship -> friendship.getSender().getUid().equals(userId) ? friendship.getReceiver() : friendship.getSender())
                .map(user -> modelMapper.map(user, UserDataDTO.class))
                .toList();
    }
}
