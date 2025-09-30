package project.ktc.springboot_app.revenue.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparativeAnalysisDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ComparisonData {
    private String period;
    private Double current;
    private Double previous;
    private Double growth;
    private Long transactions;
    private Long previousTransactions;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BestWorstPeriod {
    private String period;
    private Double growth;
  }

  private String comparisonType; // "monthly", "quarterly", "yearly"
  private Integer selectedYear;
  private List<ComparisonData> comparisons;
  private BestWorstPeriod bestPerformingPeriod;
  private BestWorstPeriod worstPerformingPeriod;
  private List<Integer> availableYears;
}
