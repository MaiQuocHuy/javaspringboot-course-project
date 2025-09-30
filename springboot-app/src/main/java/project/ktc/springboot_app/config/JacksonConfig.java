package project.ktc.springboot_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class JacksonConfig {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		log.info("Configuring global ObjectMapper with LocalDateTime format: {}", DATETIME_FORMAT);

		ObjectMapper objectMapper = new ObjectMapper();

		// Configure JavaTimeModule with custom serializers
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(
				LocalDateTime.class,
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
		javaTimeModule.addSerializer(
				LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
		javaTimeModule.addSerializer(
				LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));

		objectMapper.registerModule(javaTimeModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		objectMapper.setDateFormat(new SimpleDateFormat(DATETIME_FORMAT));

		log.info("Global ObjectMapper configured successfully with @Primary annotation");
		return objectMapper;
	}
}
