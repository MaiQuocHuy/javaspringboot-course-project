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
public class SeasonalHeatmapDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SeasonalData {
		private String month;
		private Integer day;
		private Double revenue;
		private String date;
		private Long transactions;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SeasonalSummary {
		private String season;
		private Double totalRevenue;
		private Double averageDailyRevenue;
		private Integer totalDays;
	}

	private List<SeasonalData> dailyData;
	private List<SeasonalSummary> seasonalSummary;
	private Double minRevenue;
	private Double maxRevenue;
	private Integer year;
}
