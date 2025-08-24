package project.ktc.springboot_app.refund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundStatisticsResponseDto {
    private Long total;
    private Long pending;
    private Long completed;
    private Long failed;

}
