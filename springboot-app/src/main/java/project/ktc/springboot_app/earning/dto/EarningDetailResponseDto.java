package project.ktc.springboot_app.earning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningDetailResponseDto {
    private String id;
    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseThumbnailUrl;
    private String paymentId;
    private BigDecimal amount;
    private BigDecimal platformCut;
    private Integer platformCutPercentage;
    private BigDecimal instructorShare;
    private String status;
    private LocalDateTime paidAt;
}
