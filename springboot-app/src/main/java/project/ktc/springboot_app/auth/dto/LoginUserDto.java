package project.ktc.springboot_app.auth.dto;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
}