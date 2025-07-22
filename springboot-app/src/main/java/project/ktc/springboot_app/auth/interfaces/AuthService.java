package project.ktc.springboot_app.auth.interfaces;
import java.util.Map;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;

public interface AuthService {
        void registerUser(RegisterUserDto registerUserDto);
        Map<String, String> loginUser(LoginUserDto loginUserDto);
}
