package project.ktc.springboot_app.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for audio upload to Cloudinary")
public class AudioUploadResponseDto {

    @Schema(description = "Secure HTTPS URL of the uploaded audio", example = "https://res.cloudinary.com/example/video/upload/v1234567890/course-audio/audio_1234567890_abcd1234.mp3")
    private String url;

    @Schema(description = "Public ID for the audio file", example = "course-audio/audio_1234567890_abcd1234")
    private String publicId;

    @Schema(description = "Original filename", example = "lecture-audio.mp3")
    private String fileName;

    @Schema(description = "File size in bytes", example = "5242880")
    private Long size;

    @Schema(description = "Audio format", example = "mp3")
    private String format;

    @Schema(description = "Duration in seconds", example = "180")
    private Double duration;

    @Schema(description = "Audio bitrate", example = "128")
    private Integer bitrate;

    @Schema(description = "Sample rate in Hz", example = "44100")
    private Integer sampleRate;

    @Schema(description = "Number of audio channels", example = "2")
    private Integer channels;

    @Schema(description = "Upload timestamp", example = "2023-12-01T10:30:00")
    private LocalDateTime uploadedAt;

    @Schema(description = "MIME type", example = "audio/mpeg")
    private String mimeType;
}
