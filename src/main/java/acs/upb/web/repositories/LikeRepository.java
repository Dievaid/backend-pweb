package acs.upb.web.repositories;

import acs.upb.web.entities.Like;
import acs.upb.web.entities.Post;
import acs.upb.web.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LikeRepository extends CrudRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
}
