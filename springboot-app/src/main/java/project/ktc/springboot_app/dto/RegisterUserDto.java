package project.ktc.springboot_app.dto;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String name;
    private String email;
    private String password;
}