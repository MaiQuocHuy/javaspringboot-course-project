package project.ktc.springboot_app.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDataGenerationDTO {

  private boolean success;
  private String message;
  private long paymentsGenerated;
  private long totalPayments;
  private String details;
}
