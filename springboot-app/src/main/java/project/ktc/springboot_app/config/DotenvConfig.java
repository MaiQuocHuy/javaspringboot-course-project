package project.ktc.springboot_app.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
    
}