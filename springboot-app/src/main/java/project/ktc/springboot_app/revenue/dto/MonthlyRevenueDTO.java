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
public class MonthlyRevenueDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthlyData {
		private String month;
		private Integer year;
		private Double revenue;
		private Long transactions;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RecentMonthlyData {
		private List<MonthlyData> recentRevenues;
		private Double growth;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DailyData {
		private String date;
		private Double revenue;
		private Long transactions;
	}

	private List<MonthlyData> monthlyData;
	private List<DailyData> dailyData;
	private List<Integer> availableYears;
}
