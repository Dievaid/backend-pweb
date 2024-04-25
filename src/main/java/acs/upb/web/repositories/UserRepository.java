package acs.upb.web.repositories;

import acs.upb.web.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
