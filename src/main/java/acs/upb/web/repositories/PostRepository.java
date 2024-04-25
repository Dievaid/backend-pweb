package acs.upb.web.repositories;

import acs.upb.web.entities.Post;
import acs.upb.web.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends CrudRepository<Post, String> {
    List<Post> findAllByUserIn(Collection<User> users);
}
