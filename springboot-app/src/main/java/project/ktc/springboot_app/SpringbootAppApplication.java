package project.ktc.springboot_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringbootAppApplication {

	@Value("${DB_URL:http://localhost:8080}")
	private String dbUrl;
	public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringbootAppApplication.class, args);
		SpringbootAppApplication app = context.getBean(SpringbootAppApplication.class);
        System.out.println("Spring Boot Application started successfully!");
        System.out.println("Database URL: " + app.getDbUrl());
	}

	public String getDbUrl() {
		return dbUrl;
	}
}
