package me.synology.hajubal.userservice.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class LoginRequest {

    @NotNull(message = "Email not null")
    @Size(min = 2, message = "Email length wrong.")
    @Email
    private String email;

    @NotNull(message = "Password not null")
    @Size(min = 4)
    private String password;
}
