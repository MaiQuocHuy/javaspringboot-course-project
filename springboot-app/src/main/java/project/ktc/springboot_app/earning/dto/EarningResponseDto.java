package project.ktc.springboot_app.earning.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningResponseDto {
	private String id;
	private String courseId;
	private String courseTitle;
	private String courseThumbnailUrl;
	private String paymentId;
	private BigDecimal amount;
	private BigDecimal platformCut;
	private BigDecimal instructorShare;
	private String status;
	private LocalDateTime paidAt;
}
