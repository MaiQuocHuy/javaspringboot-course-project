package project.ktc.springboot_app.earning.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MonthlyEarningsDto {
  private int year;
  private int month;
  private BigDecimal revenue;
}
