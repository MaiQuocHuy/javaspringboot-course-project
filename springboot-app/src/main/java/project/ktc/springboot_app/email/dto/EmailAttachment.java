package project.ktc.springboot_app.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email attachment DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {

    private String filename;
    private byte[] content;
    private String contentType;
    private long size;

    public EmailAttachment(String filename, byte[] content, String contentType) {
        this.filename = filename;
        this.content = content;
        this.contentType = contentType;
        this.size = content != null ? content.length : 0;
    }
}
