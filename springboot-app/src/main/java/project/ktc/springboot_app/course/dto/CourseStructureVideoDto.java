package project.ktc.springboot_app.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Video information for course structure")
public class CourseStructureVideoDto {

    @Schema(description = "Video ID", example = "video-uuid")
    private String id;

    @Schema(description = "Video URL", example = "https://res.cloudinary.com/.../video.mp4")
    private String url;

    @Schema(description = "Video duration in seconds", example = "1800")
    private Integer duration;

    @Schema(description = "Video title", example = "Introduction to React")
    private String title;

    @Schema(description = "Video thumbnail URL", example = "https://res.cloudinary.com/.../thumbnail.jpg")
    private String thumbnail;
}