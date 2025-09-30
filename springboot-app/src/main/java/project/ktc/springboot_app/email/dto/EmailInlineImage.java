package project.ktc.springboot_app.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email inline image DTO for embedding images in HTML emails */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailInlineImage {

	private String contentId; // Used in HTML as <img src="cid:contentId">
	private byte[] content;
	private String contentType;
	private String filename;

	public EmailInlineImage(String contentId, byte[] content, String contentType) {
		this.contentId = contentId;
		this.content = content;
		this.contentType = contentType;
	}
}
