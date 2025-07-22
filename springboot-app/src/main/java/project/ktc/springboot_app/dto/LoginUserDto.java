package project.ktc.springboot_app.dto;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
}