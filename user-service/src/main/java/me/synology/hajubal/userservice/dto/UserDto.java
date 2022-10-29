package me.synology.hajubal.userservice.dto;

import lombok.Data;
import me.synology.hajubal.userservice.vo.ResponseOrder;

import java.util.Date;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String decryptedPwd;

    private String encryptedPwd;

    private List<ResponseOrder> orders;
}
