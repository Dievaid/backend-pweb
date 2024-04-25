package acs.upb.web.dtos;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CommentDataDTO {
    private String uid;
    private String content;
    private Timestamp createdAt;
    private UserDataDTO user;
}
