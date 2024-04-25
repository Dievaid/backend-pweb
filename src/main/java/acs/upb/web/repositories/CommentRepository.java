package acs.upb.web.repositories;

import acs.upb.web.entities.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, String> {
}
