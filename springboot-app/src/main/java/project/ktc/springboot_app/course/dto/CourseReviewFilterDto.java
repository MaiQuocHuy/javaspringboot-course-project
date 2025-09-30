package project.ktc.springboot_app.course.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReviewFilterDto {

  private List<String> status;
  private String createdBy;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime dateFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime dateTo;

  @Builder.Default private int page = 0;
  @Builder.Default private int size = 10;
  @Builder.Default private String sort = "createdAt,desc";
}
