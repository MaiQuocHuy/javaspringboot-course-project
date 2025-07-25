package project.ktc.springboot_app.section.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionWithLessonsDto {
    private String id;
    private String title;
    private Integer order;
    private Integer lessonCount;
    private List<LessonDto> lessons;
}
