package project.ktc.springboot_app.discount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPayoutActionResultDto {
    private String action;
    private int totalRequested;
    private int totalProcessed;
    private int totalFailed;
    private List<String> failedReasons;
    private String summary;
}
