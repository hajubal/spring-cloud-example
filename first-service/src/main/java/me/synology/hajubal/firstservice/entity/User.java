package me.synology.hajubal.firstservice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@ToString
@Setter
@Getter
@RedisHash("users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;

}
