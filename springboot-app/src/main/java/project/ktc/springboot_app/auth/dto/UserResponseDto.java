package project.ktc.springboot_app.auth.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private String id;
    private String email;
    private String name;
    private List<UserRoleEnum> roles; // optional: chỉ hiển thị khi cần

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
    }

}
