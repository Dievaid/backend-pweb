package acs.upb.web.dtos;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String uid;
    private String username;
    private String imgUrl;
    private String password;
}
