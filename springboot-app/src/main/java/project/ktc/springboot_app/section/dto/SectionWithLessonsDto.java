package project.ktc.springboot_app.section.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionWithLessonsDto {
  private String id;
  private String title;
  private String description;
  private Integer orderIndex;
  private Integer lessonCount;
  private List<LessonDto> lessons;
}
