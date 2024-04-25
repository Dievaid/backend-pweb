package acs.upb.web.dtos;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PostDataDTO {
    private String uid;
    private UserDataDTO user;
    private String content;
    private Timestamp createdAt;
    private List<CommentDataDTO> comments;
    private Long likeCount;
}
