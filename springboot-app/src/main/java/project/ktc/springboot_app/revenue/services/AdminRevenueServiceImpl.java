package project.ktc.springboot_app.revenue.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.revenue.dto.*;
import project.ktc.springboot_app.revenue.interfaces.AdminRevenueService;
import project.ktc.springboot_app.revenue.repositories.AdminRevenueRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRevenueServiceImpl implements AdminRevenueService {

	private final AdminRevenueRepository adminRevenueRepository;

	@Override
	public ResponseEntity<ApiResponse<StatisticsDTO>> getStatistics() {
		log.info("Getting revenue statistics");

		try {
			// Get total revenue (30% of completed payments)
			Double totalRevenue = adminRevenueRepository.getTotalRevenue();

			// Get yearly revenue comparison for yearly growth
			int currentYear = LocalDate.now().getYear();
			int previousYear = currentYear - 1;

			Double currentYearRevenue = 0.0;
			Double previousYearRevenue = 0.0;

			// Check if data exists for both years before making comparison
			if (hasDataForYear(currentYear) || hasDataForYear(previousYear)) {
				List<Object[]> yearlyComparisonList = adminRevenueRepository.getYearlyRevenueComparison(currentYear,
						previousYear);

				if (yearlyComparisonList != null && !yearlyComparisonList.isEmpty()) {
					Object[] yearlyComparison = yearlyComparisonList.get(0);
					if (yearlyComparison.length >= 2) {
						currentYearRevenue = safeGetDouble(yearlyComparison[0]);
						previousYearRevenue = safeGetDouble(yearlyComparison[1]);
					}
				}
			}

			// Get monthly comparison for monthly growth
			Double monthlyGrowth = 0.0;
			List<Object[]> monthlyComparison = adminRevenueRepository.getMonthlyGrowthComparison(currentYear,
					previousYear);
			if (monthlyComparison != null && !monthlyComparison.isEmpty()) {
				monthlyGrowth = calculateMonthlyGrowthFromComparison(monthlyComparison);
			}

			// Calculate yearly growth
			Double yearlyGrowth = calculateGrowthPercentage(currentYearRevenue, previousYearRevenue);

			// Get user and transaction counts
			Long activeUsers = adminRevenueRepository.getTotalActiveUsers();
			Long totalTransactions = adminRevenueRepository.getTotalTransactions();
			Double avgRevenuePerUser = adminRevenueRepository.getAverageRevenuePerUser();

			// Validate and provide safe defaults
			activeUsers = activeUsers != null ? activeUsers : 0L;
			totalTransactions = totalTransactions != null ? totalTransactions : 0L;
			avgRevenuePerUser = avgRevenuePerUser != null ? avgRevenuePerUser : 0.0;

			// Build statistics cards
			List<StatisticsDTO.StatCard> statCards = Arrays.asList(
					StatisticsDTO.StatCard.builder()
							.title("Total Revenue")
							.value(formatCurrency(totalRevenue))
							.build(),
					StatisticsDTO.StatCard.builder()
							.title("Monthly Growth")
							.value(formatPercentage(monthlyGrowth))
							.build(),
					StatisticsDTO.StatCard.builder()
							.title("Yearly Growth")
							.value(formatPercentage(yearlyGrowth))
							.build(),
					StatisticsDTO.StatCard.builder()
							.title("Active Users")
							.value(String.valueOf(activeUsers))
							.build(),
					StatisticsDTO.StatCard.builder()
							.title("Total Transactions")
							.value(String.valueOf(totalTransactions))
							.build(),
					StatisticsDTO.StatCard.builder()
							.title("Avg Revenue Per User")
							.value(formatCurrency(avgRevenuePerUser))
							.build());

			StatisticsDTO statistics = StatisticsDTO.builder()
					.stats(statCards)
					.totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
					.monthlyGrowth(monthlyGrowth)
					.yearlyGrowth(yearlyGrowth)
					.avgRevenuePerUser(avgRevenuePerUser)
					.totalActiveUsers(activeUsers)
					.totalTransactions(totalTransactions)
					.build();

			return ApiResponseUtil.success(statistics, "Revenue statistics retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting statistics: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to get revenue statistics: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<MonthlyRevenueDTO>> getMonthlyRevenue(Integer year) {
		log.info("Getting monthly revenue for year: {}", year);

		try {
			// Check if data exists for the requested year
			if (!hasDataForYear(year)) {
				log.warn("No data found for year: {}", year);
				return ApiResponseUtil.notFound("No data found for year " + year);
			}

			List<Object[]> monthlyData = adminRevenueRepository.getMonthlyRevenueForYear(year);
			List<MonthlyRevenueDTO.MonthlyData> monthlyList = new ArrayList<>();

			if (isValidDataList(monthlyData)) {
				for (Object[] row : monthlyData) {
					if (row == null || row.length < 4)
						continue;

					Integer yearVal = safeGetInteger(row[0]);
					Integer month = safeGetInteger(row[1]);
					Double revenue = safeGetDouble(row[2]);
					Long transactions = safeGetLong(row[3]);

					monthlyList.add(
							MonthlyRevenueDTO.MonthlyData.builder()
									.month(getMonthName(month))
									.year(yearVal)
									.revenue(revenue)
									.transactions(transactions)
									.build());
				}
			}

			List<Integer> availableYears = adminRevenueRepository.getDistinctPaymentYears();

			MonthlyRevenueDTO monthlyRevenue = MonthlyRevenueDTO.builder()
					.monthlyData(monthlyList)
					.availableYears(availableYears != null ? availableYears : new ArrayList<>())
					.build();

			return ApiResponseUtil.success(monthlyRevenue, "Monthly revenue data retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting monthly revenue for year {}: {}", year, e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to get monthly revenue: " + e.getMessage());
		}
	}

	// Get recent revenue data for a specific year
	@Override
	public ResponseEntity<ApiResponse<MonthlyRevenueDTO.RecentMonthlyData>> getRecentRevenues() {
		try {
			int currentYear = LocalDate.now().getYear();
			int currentMonth = LocalDate.now().getMonthValue();

			List<MonthlyRevenueDTO.MonthlyData> recentRevenues = new ArrayList<>();

			// Calculate recent 3 months including current month
			for (int i = 3; i > 0; i--) {
				int month = currentMonth - i + 1;
				int year = currentYear;

				if (month <= 0) {
					month += 12;
					year -= 1;
				}

				List<Object[]> recentRevenueData = adminRevenueRepository.getRecentRevenueForYear(year, month);

				if (isValidDataList(recentRevenueData)) {
					for (Object[] row : recentRevenueData) {
						if (row == null || row.length < 4)
							continue;

						Integer yearVal = safeGetInteger(row[0]);
						Integer monthVal = safeGetInteger(row[1]);
						Double revenue = safeGetDouble(row[2]);
						Long transactions = safeGetLong(row[3]);

						recentRevenues.add(
								MonthlyRevenueDTO.MonthlyData.builder()
										.month(getMonthName(monthVal))
										.year(yearVal)
										.revenue(revenue)
										.transactions(transactions)
										.build());
					}
				} else {
					recentRevenues.add(
							MonthlyRevenueDTO.MonthlyData.builder()
									.month(getMonthName(month))
									.year(year)
									.revenue(0.0)
									.transactions(0L)
									.build());
				}
			}

			// Calculate growth between the most recent month and the previous month
			Double growth = 0.0;
			Double latestRevenue = 0.0;
			Double previousRevenue = 0.0;

			if (recentRevenues.size() >= 3 && recentRevenues.get(2).getRevenue() != null) {
				latestRevenue = recentRevenues.get(2).getRevenue();
			}
			if (recentRevenues.size() >= 2 && recentRevenues.get(1).getRevenue() != null) {
				previousRevenue = recentRevenues.get(1).getRevenue();
			}
			growth = calculateGrowthPercentage(latestRevenue, previousRevenue);

			MonthlyRevenueDTO.RecentMonthlyData recentMonthlyData = MonthlyRevenueDTO.RecentMonthlyData.builder()
					.recentRevenues(recentRevenues)
					.growth(growth)
					.build();

			return ApiResponseUtil.success(
					recentMonthlyData, "Recent monthly revenue data retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting recent revenues: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to get recent revenues: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<List<MonthlyRevenueDTO.DailyData>>> getDailysRevenue(
			Integer year, Integer month) {
		log.info("Getting daily revenue for {}/{}", month, year);

		try {
			// Check if data exists for the requested year
			if (!hasDataForYear(year)) {
				log.warn("No data found for year: {}", year);
				return ApiResponseUtil.notFound("No data found for year " + year);
			}

			List<Object[]> dailyData = adminRevenueRepository.getDailyRevenueForMonth(year, month);

			// Create a map to store revenue data by date
			Map<String, MonthlyRevenueDTO.DailyData> dailyDataMap = new HashMap<>();

			// Process existing data from database
			if (isValidDataList(dailyData)) {
				for (Object[] row : dailyData) {
					if (row == null || row.length < 3)
						continue;

					Date date = (Date) row[0];
					Double revenue = safeGetDouble(row[1]);
					Long transactions = safeGetLong(row[2]);

					String dateStr = date != null ? date.toString() : "";
					dailyDataMap.put(
							dateStr,
							MonthlyRevenueDTO.DailyData.builder()
									.date(dateStr)
									.revenue(revenue)
									.transactions(transactions)
									.build());
				}
			}

			// Generate complete list for all days in the month
			List<MonthlyRevenueDTO.DailyData> dailyList = new ArrayList<>();
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, 1); // month is 0-based in Calendar
			int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

			for (int day = 1; day <= daysInMonth; day++) {
				calendar.set(Calendar.DAY_OF_MONTH, day);
				String dateStr = new java.sql.Date(calendar.getTimeInMillis()).toString();

				// Check if we have data for this day, otherwise use zero values
				MonthlyRevenueDTO.DailyData dayData = dailyDataMap.get(dateStr);
				if (dayData == null) {
					dayData = MonthlyRevenueDTO.DailyData.builder()
							.date(dateStr)
							.revenue(0.0)
							.transactions(0L)
							.build();
				}

				dailyList.add(dayData);
			}

			List<MonthlyRevenueDTO.DailyData> result = dailyList;

			return ApiResponseUtil.success(result, "Daily revenue data retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting daily revenue for {}/{}: {}", month, year, e.getMessage(), e);
			return ApiResponseUtil.internalServerError("Failed to get daily revenue: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<TopSpendersDTO>> getTopSpenders(Integer limit) {
		log.info("Getting top {} spenders", limit);

		try {
			List<Object[]> topSpendersData = adminRevenueRepository.getTopSpendingStudents();
			List<TopSpendersDTO.StudentSpendingData> users = new ArrayList<>();

			if (!isValidDataList(topSpendersData)) {
				log.warn("No top spenders data found");
				TopSpendersDTO result = TopSpendersDTO.builder().topStudents(users).limit(limit != null ? limit : 5)
						.build();
				return ApiResponseUtil.success(result, "No top spenders data found");
			}

			int count = 0;
			int maxLimit = limit != null ? limit : 5;

			for (Object[] row : topSpendersData) {
				if (count >= maxLimit)
					break;
				if (row == null || row.length < 6)
					continue;

				String userId = (String) row[0];
				String userName = (String) row[1];
				String userEmail = (String) row[2];
				String userThumbnail = (String) row[3];
				Double totalSpent = safeGetDouble(row[4]);
				Long coursesEnrolled = safeGetLong(row[5]);

				// Skip users with no spending
				if (totalSpent <= 0)
					continue;

				users.add(
						TopSpendersDTO.StudentSpendingData.builder()
								.id(userId != null ? userId : "")
								.name(userName != null ? userName : "Unknown")
								.email(userEmail != null ? userEmail : "")
								.totalSpent(totalSpent)
								.coursesEnrolled(coursesEnrolled.intValue())
								.avatarUrl(userThumbnail != null ? userThumbnail : "")
								.build());

				count++;
			}

			TopSpendersDTO result = TopSpendersDTO.builder().topStudents(users).limit(maxLimit).build();

			return ApiResponseUtil.success(result, "Top spenders data retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting top {} spenders: {}", limit, e.getMessage(), e);
			return ApiResponseUtil.internalServerError("Failed to get top spenders: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<PerformanceMetricsDTO>> getPerformanceMetrics() {
		log.info("Getting performance metrics");

		try {
			// Get category revenue data
			List<Object[]> categoryData = adminRevenueRepository.getRevenueByCategory();
			List<PerformanceMetricsDTO.CategoryRevenue> categories = new ArrayList<>();

			if (isValidDataList(categoryData)) {
				// Calculate total revenue for percentage calculations
				Double totalRevenue = categoryData.stream().mapToDouble(row -> safeGetDouble(row[1])).sum();

				for (Object[] row : categoryData) {
					if (row == null || row.length < 4) {
						continue;
					}

					Double revenue = safeGetDouble(row[1]);

					// Skip categories with no revenue
					if (revenue == null || revenue <= 0) {
						continue;
					}

					String categoryName = (String) row[0];
					Long studentsCount = safeGetLong(row[2]);
					Long coursesCount = safeGetLong(row[3]);
					Double percentage = totalRevenue > 0 ? (Math.round((revenue / totalRevenue) * 100 * 100.0) / 100.0)
							: 0.0;

					categories.add(
							PerformanceMetricsDTO.CategoryRevenue.builder()
									.category(categoryName != null ? categoryName : "Unknown")
									.revenue(revenue)
									.studentsCount(studentsCount.intValue())
									.coursesCount(coursesCount.intValue())
									.percentage(percentage)
									.build());
				}
			}

			// Get instructor performance data
			List<Object[]> instructorData = adminRevenueRepository.getInstructorPerformanceMetrics();
			List<PerformanceMetricsDTO.InstructorPerformance> instructors = new ArrayList<>();

			if (isValidDataList(instructorData)) {
				for (Object[] row : instructorData) {
					if (row == null || row.length < 6) {
						continue;
					}

					// Skip instructors with no revenue
					Double revenue = safeGetDouble(row[4]);
					if (revenue == null || revenue <= 0) {
						continue;
					}

					String instructorId = (String) row[0];
					String instructorName = (String) row[1];
					String instructorEmail = (String) row[2];
					Long coursesCount = safeGetLong(row[3]);
					Long studentsCount = safeGetLong(row[5]);

					instructors.add(
							PerformanceMetricsDTO.InstructorPerformance.builder()
									.id(instructorId != null ? instructorId : "")
									.name(instructorName != null ? instructorName : "Unknown")
									.email(instructorEmail != null ? instructorEmail : "")
									.coursesCount(coursesCount != null ? coursesCount.intValue() : 0)
									.totalRevenue(revenue)
									.averageRating(null)
									.totalStudents(studentsCount != null ? studentsCount.intValue() : 0)
									.build());
				}
			}

			PerformanceMetricsDTO result = PerformanceMetricsDTO.builder()
					.categoryRevenues(categories)
					.topInstructors(instructors)
					.topPerformingCategory(
							!categories.isEmpty() ? categories.get(0).getCategory() : "No Data")
					.worstPerformingCategory(
							!categories.isEmpty()
									? categories.get(categories.size() - 1).getCategory()
									: "No Data")
					.build();

			return ApiResponseUtil.success(result, "Performance metrics retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting performance metrics: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to get performance metrics: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<ComparativeAnalysisDTO>> getComparativeAnalysis(
			String comparisonType, Integer year) {
		log.info("Getting comparative analysis for type: {} and year: {}", comparisonType, year);

		try {
			// Check if data exists for the requested year
			if (!hasDataForYear(year)) {
				log.warn("No data found for year: {}", year);
				return ApiResponseUtil.notFound("No data found for year " + year);
			}

			List<ComparativeAnalysisDTO.ComparisonData> comparisons = new ArrayList<>();

			switch (comparisonType.toLowerCase()) {
				case "monthly":
					comparisons = getMonthlyComparison(year);
					break;
				case "quarterly":
					comparisons = getQuarterlyComparison(year);
					break;
				case "yearly":
					comparisons = getYearlyComparison();
					break;
				default:
					comparisons = getMonthlyComparison(year);
			}

			if (isValidDataList(comparisons)) {
				List<ComparativeAnalysisDTO.BestWorstPeriod> bestWorstPeriods = getBestWorstPeriods(comparisons);

				ComparativeAnalysisDTO result = ComparativeAnalysisDTO.builder()
						.comparisonType(comparisonType)
						.selectedYear(year)
						.comparisons(comparisons)
						.bestPerformingPeriod(
								bestWorstPeriods != null && bestWorstPeriods.size() > 0
										? bestWorstPeriods.get(0)
										: null)
						.worstPerformingPeriod(
								bestWorstPeriods != null && bestWorstPeriods.size() > 1
										? bestWorstPeriods.get(1)
										: null)
						.availableYears(adminRevenueRepository.getDistinctPaymentYears())
						.build();

				return ApiResponseUtil.success(result, "Comparative analysis data retrieved successfully");
			} else {
				return ApiResponseUtil.notFound(
						"No comparative analysis data found for the given parameters");
			}
		} catch (Exception e) {
			log.error(
					"Error getting comparative analysis for type {} and year {}: {}",
					comparisonType,
					year,
					e.getMessage(),
					e);
			return ApiResponseUtil.internalServerError(
					"Failed to get comparative analysis: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<SeasonalHeatmapDTO>> getSeasonalHeatmap(Integer year) {
		log.info("Getting seasonal heatmap for year: {}", year);

		try {
			// Check if data exists for the requested year
			if (!hasDataForYear(year)) {
				log.warn("No data found for year: {}", year);
				return ApiResponseUtil.notFound("No data found for year " + year);
			}

			List<Object[]> seasonalData = adminRevenueRepository.getSeasonalDailyRevenue(year);
			List<SeasonalHeatmapDTO.SeasonalData> seasons = new ArrayList<>();

			// Group data by seasons
			Map<String, Double> seasonRevenue = new HashMap<>();
			seasonRevenue.put("Spring", 0.0);
			seasonRevenue.put("Summer", 0.0);
			seasonRevenue.put("Fall", 0.0);
			seasonRevenue.put("Winter", 0.0);

			if (isValidDataList(seasonalData)) {
				for (Object[] row : seasonalData) {
					if (row == null || row.length < 2)
						continue;

					Date date = (Date) row[0];
					Double revenue = safeGetDouble(row[1]);

					if (date != null) {
						String season = getSeasonForMonth(date.getMonth());
						seasonRevenue.put(season, seasonRevenue.get(season) + revenue);
					}
				}
			}

			// Create season data
			seasons.add(createSeasonData("Spring", "March-May", seasonRevenue.get("Spring")));
			seasons.add(createSeasonData("Summer", "June-August", seasonRevenue.get("Summer")));
			seasons.add(createSeasonData("Fall", "September-November", seasonRevenue.get("Fall")));
			seasons.add(createSeasonData("Winter", "December-February", seasonRevenue.get("Winter")));

			SeasonalHeatmapDTO result = SeasonalHeatmapDTO.builder().year(year).dailyData(seasons).build();

			return ApiResponseUtil.success(result, "Seasonal heatmap data retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting seasonal heatmap for year {}: {}", year, e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Error retrieving seasonal heatmap: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
		log.info("Getting available years with revenue data");

		try {
			List<Integer> availableYears = adminRevenueRepository.getDistinctPaymentYears();
			List<Integer> result = availableYears != null ? availableYears : new ArrayList<>();
			return ApiResponseUtil.success(result, "Available years retrieved successfully");
		} catch (Exception e) {
			log.error("Error getting available years: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to get available years: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<Object>> getRevenueSummary(Integer year) {
		Map<String, Object> summary = new HashMap<>();

		try {
			// Check if data exists for the requested year
			if (!hasDataForYear(year)) {
				log.warn("No data found for year: {}", year);
				return ApiResponseUtil.notFound("No data found for year " + year);
			}

			// Get quick stats for overview
			ResponseEntity<ApiResponse<StatisticsDTO>> statisticsResponse = getStatistics();
			ApiResponse<StatisticsDTO> statisticsBody = statisticsResponse.getBody();
			StatisticsDTO statistics = statisticsBody != null ? statisticsBody.getData() : null;

			// Get monthly revenue data
			ResponseEntity<ApiResponse<MonthlyRevenueDTO>> monthlyRevenueResponse = getMonthlyRevenue(year);
			ApiResponse<MonthlyRevenueDTO> monthlyRevenueBody = monthlyRevenueResponse.getBody();
			MonthlyRevenueDTO monthlyRevenue = monthlyRevenueBody != null ? monthlyRevenueBody.getData() : null;

			// Get performance metrics
			ResponseEntity<ApiResponse<PerformanceMetricsDTO>> performanceResponse = getPerformanceMetrics();
			ApiResponse<PerformanceMetricsDTO> performanceBody = performanceResponse.getBody();
			PerformanceMetricsDTO performance = performanceBody != null ? performanceBody.getData() : null;

			// Get comparative analysis
			ResponseEntity<ApiResponse<ComparativeAnalysisDTO>> comparativeResponse = getComparativeAnalysis("monthly",
					year);
			ApiResponse<ComparativeAnalysisDTO> comparativeBody = comparativeResponse.getBody();
			ComparativeAnalysisDTO comparative = comparativeBody != null ? comparativeBody.getData() : null;

			// Build comprehensive summary
			summary.put("statistics", statistics);
			summary.put("monthlyRevenue", monthlyRevenue);
			summary.put("performance", performance);
			summary.put("comparative", comparative);
			summary.put("year", year);
			summary.put("generatedAt", LocalDateTime.now());

			return ApiResponseUtil.success(summary, "Revenue summary generated successfully");
		} catch (Exception e) {
			log.error("Error generating revenue summary for year {}: {}", year, e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to generate revenue summary: " + e.getMessage());
		}
	}

	// Helper methods

	/** Check if data exists for a specific year */
	private boolean hasDataForYear(Integer year) {
		List<Integer> availableYears = adminRevenueRepository.getDistinctPaymentYears();
		return availableYears != null && availableYears.contains(year);
	}

	/** Validate and extract safe double value from Object */
	private Double safeGetDouble(Object value) {
		if (value == null)
			return 0.0;
		if (value instanceof Double)
			return (Double) value;
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		return 0.0;
	}

	/** Validate and extract safe long value from Object */
	private Long safeGetLong(Object value) {
		if (value == null)
			return 0L;
		if (value instanceof Long)
			return (Long) value;
		if (value instanceof Number)
			return ((Number) value).longValue();
		return 0L;
	}

	/** Validate and extract safe integer value from Object */
	private Integer safeGetInteger(Object value) {
		if (value == null)
			return 0;
		if (value instanceof Integer)
			return (Integer) value;
		if (value instanceof Number)
			return ((Number) value).intValue();
		return 0;
	}

	/** Check if list is null or empty */
	private boolean isValidDataList(List<?> data) {
		return data != null && !data.isEmpty();
	}

	// Get monthly comparison data for a specific year
	private List<ComparativeAnalysisDTO.ComparisonData> getMonthlyComparison(Integer year) {
		try {
			// Check if data exists for both years
			boolean currentYearExists = hasDataForYear(year);
			boolean previousYearExists = hasDataForYear(year - 1);

			if (!currentYearExists && !previousYearExists) {
				log.warn("No data found for years {} or {}", year, year - 1);
				return Collections.emptyList();
			}

			List<Object[]> currentYearData = currentYearExists
					? adminRevenueRepository.getMonthlyRevenueForYear(year)
					: new ArrayList<>();
			List<Object[]> previousYearData = previousYearExists
					? adminRevenueRepository.getMonthlyRevenueForYear(year - 1)
					: new ArrayList<>();

			Map<Integer, Double> currentYear = new HashMap<>();
			Map<Integer, Double> previousYear = new HashMap<>();
			Map<Integer, Long> currentTransactions = new HashMap<>();
			Map<Integer, Long> previousTransactions = new HashMap<>();

			// Process current year data safely
			if (isValidDataList(currentYearData)) {
				for (Object[] row : currentYearData) {
					if (row != null && row.length >= 4) {
						Integer month = safeGetInteger(row[1]);
						Double revenue = safeGetDouble(row[2]);
						currentYear.put(month, revenue);

						Long transactions = safeGetLong(row[3]);
						currentTransactions.put(month, transactions);
					}
				}
			}

			// Process previous year data safely
			if (isValidDataList(previousYearData)) {
				for (Object[] row : previousYearData) {
					if (row != null && row.length >= 4) {
						Integer month = safeGetInteger(row[1]);
						Double revenue = safeGetDouble(row[2]);
						previousYear.put(month, revenue);

						Long transactions = safeGetLong(row[3]);
						previousTransactions.put(month, transactions);
					}
				}
			}

			List<ComparativeAnalysisDTO.ComparisonData> comparisons = new ArrayList<>();

			for (int month = 1; month <= 12; month++) {
				Double current = currentYear.getOrDefault(month, 0.0);
				Double previous = previousYear.getOrDefault(month, 0.0);
				Double growth = calculateGrowthPercentage(current, previous);

				// Get actual transaction counts, null if no data exists for that month
				Long transactions = currentTransactions.get(month);
				Long prevTransactions = previousTransactions.get(month);

				// Use 0L as default only if we want to show zero instead of null
				transactions = transactions != null ? transactions : 0L;
				prevTransactions = prevTransactions != null ? prevTransactions : 0L;

				comparisons.add(
						ComparativeAnalysisDTO.ComparisonData.builder()
								.period(getMonthName(month))
								.current(current)
								.previous(previous)
								.growth(growth)
								.transactions(transactions)
								.previousTransactions(prevTransactions)
								.build());
			}

			return comparisons;
		} catch (Exception e) {
			log.error("Error getting monthly comparison for year {}: {}", year, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	// Get quarterly comparison data for a specific year
	private List<ComparativeAnalysisDTO.ComparisonData> getQuarterlyComparison(Integer year) {
		try {
			// Check if data exists for both years
			boolean currentYearExists = hasDataForYear(year);
			boolean previousYearExists = hasDataForYear(year - 1);

			if (!currentYearExists && !previousYearExists) {
				log.warn("No data found for years {} or {}", year, year - 1);
				return Collections.emptyList();
			}

			List<Object[]> currentYearData = currentYearExists
					? adminRevenueRepository.getQuarterlyRevenueForYear(year)
					: new ArrayList<>();
			List<Object[]> previousYearData = previousYearExists
					? adminRevenueRepository.getQuarterlyRevenueForYear(year - 1)
					: new ArrayList<>();

			Map<Integer, Double> currentYear = new HashMap<>();
			Map<Integer, Double> previousYear = new HashMap<>();
			Map<Integer, Long> currentTransactions = new HashMap<>();
			Map<Integer, Long> previousTransactions = new HashMap<>();

			// Process current year data safely
			if (isValidDataList(currentYearData)) {
				for (Object[] row : currentYearData) {
					if (row != null && row.length >= 3) {
						Integer quarter = safeGetInteger(row[0]);
						Double revenue = safeGetDouble(row[1]);
						currentYear.put(quarter, revenue);

						Long transactions = safeGetLong(row[2]);
						currentTransactions.put(quarter, transactions);
					}
				}
			}

			// Process previous year data safely
			if (isValidDataList(previousYearData)) {
				for (Object[] row : previousYearData) {
					if (row != null && row.length >= 2) {
						Integer quarter = safeGetInteger(row[0]);
						Double revenue = safeGetDouble(row[1]);
						previousYear.put(quarter, revenue);

						Long transactions = safeGetLong(row[2]);
						previousTransactions.put(quarter, transactions);
					}
				}
			}

			List<ComparativeAnalysisDTO.ComparisonData> comparisons = new ArrayList<>();

			for (int quarter = 1; quarter <= 4; quarter++) {
				Double current = currentYear.getOrDefault(quarter, 0.0);
				Double previous = previousYear.getOrDefault(quarter, 0.0);
				Double growth = calculateGrowthPercentage(current, previous);
				// Get actual transaction counts, null if no data exists for that quarter
				Long transactions = currentTransactions.get(quarter);
				Long prevTransactions = previousTransactions.get(quarter);

				// Use 0L as default only if we want to show zero instead of null
				transactions = transactions != null ? transactions : 0L;
				prevTransactions = prevTransactions != null ? prevTransactions : 0L;

				comparisons.add(
						ComparativeAnalysisDTO.ComparisonData.builder()
								.period("Quarter" + quarter)
								.current(current)
								.previous(previous)
								.growth(growth)
								.transactions(transactions)
								.previousTransactions(prevTransactions)
								.build());
			}

			return comparisons;
		} catch (Exception e) {
			log.error("Error getting quarterly comparison for year {}: {}", year, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	// Get yearly comparison data
	private List<ComparativeAnalysisDTO.ComparisonData> getYearlyComparison() {
		try {
			List<ComparativeAnalysisDTO.ComparisonData> yearly = new ArrayList<>();
			List<Integer> availableYears = adminRevenueRepository.getDistinctPaymentYears();

			if (!isValidDataList(availableYears)) {
				log.warn("No available years with payment data found");
				return Collections.emptyList();
			}

			int numOfYears = availableYears.size();
			for (int i = numOfYears; i > 0; i--) {
				int currentYear = availableYears.get(i - 1);
				int previousYear = currentYear - 1;

				Double currentYearRevenue = 0.0;
				Double previousYearRevenue = 0.0;

				// Only query if years have data
				if (availableYears.contains(currentYear) || availableYears.contains(previousYear)) {
					List<Object[]> currentYearDataList = adminRevenueRepository.getYearlyRevenueComparison(currentYear,
							previousYear);

					if (currentYearDataList != null && !currentYearDataList.isEmpty()) {
						Object[] currentYearData = currentYearDataList.get(0);
						if (currentYearData.length >= 2) {
							currentYearRevenue = safeGetDouble(currentYearData[0]);
							previousYearRevenue = safeGetDouble(currentYearData[1]);
						}
					}
				}

				Double growth = calculateGrowthPercentage(currentYearRevenue, previousYearRevenue);

				yearly.add(
						ComparativeAnalysisDTO.ComparisonData.builder()
								.period(String.valueOf(currentYear))
								.current(currentYearRevenue)
								.previous(previousYearRevenue)
								.growth(growth)
								.build());
			}

			return yearly;
		} catch (Exception e) {
			int currentYear = Year.now().getValue();
			log.error("Error getting yearly comparison for year {}: {}", currentYear, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	// Get best and worst performing periods from comparisons
	private List<ComparativeAnalysisDTO.BestWorstPeriod> getBestWorstPeriods(
			List<ComparativeAnalysisDTO.ComparisonData> comparisons) {
		// Get best performing periods based on growth
		ComparativeAnalysisDTO.ComparisonData best = comparisons.stream()
				.max(Comparator.comparing(ComparativeAnalysisDTO.ComparisonData::getGrowth))
				.orElse(null);

		// Get worst performing periods based on growth
		ComparativeAnalysisDTO.ComparisonData worst = comparisons.stream()
				.min(Comparator.comparing(ComparativeAnalysisDTO.ComparisonData::getGrowth))
				.orElse(null);

		List<ComparativeAnalysisDTO.BestWorstPeriod> bestWorstPeriod = new ArrayList<>();

		if (best != null) {
			ComparativeAnalysisDTO.BestWorstPeriod bestPeriod = ComparativeAnalysisDTO.BestWorstPeriod.builder()
					.period(best.getPeriod())
					.growth(best.getGrowth())
					.build();
			bestWorstPeriod.add(bestPeriod);
		}

		if (worst != null) {
			ComparativeAnalysisDTO.BestWorstPeriod worstPeriod = ComparativeAnalysisDTO.BestWorstPeriod.builder()
					.period(worst.getPeriod())
					.growth(worst.getGrowth())
					.build();
			bestWorstPeriod.add(worstPeriod);
		}

		return bestWorstPeriod;
	}

	private SeasonalHeatmapDTO.SeasonalData createSeasonData(
			String name, String period, Double revenue) {
		return SeasonalHeatmapDTO.SeasonalData.builder()
				.month(name)
				.revenue(revenue)
				.transactions(0L)
				.build();
	}

	private String getSeasonForMonth(Integer month) {
		if (month >= 3 && month <= 5)
			return "Spring";
		if (month >= 6 && month <= 8)
			return "Summer";
		if (month >= 9 && month <= 11)
			return "Fall";
		return "Winter";
	}

	private String getMonthName(Integer month) {
		String[] months = {
				"January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December"
		};
		return month >= 1 && month <= 12 ? months[month - 1] : "Unknown";
	}

	private Double calculateMonthlyGrowthFromComparison(List<Object[]> monthlyComparison) {
		// Get the latest month with data for comparison
		int currentMonth = LocalDate.now().getMonthValue();
		Double currentMonthRevenue = 0.0;
		Double previousMonthRevenue = 0.0;

		for (Object[] row : monthlyComparison) {
			if (row == null || row.length < 2)
				continue;

			Integer month = safeGetInteger(row[0]);
			Double currentYearValue = safeGetDouble(row[1]);

			if (month.equals(currentMonth)) {
				currentMonthRevenue = currentYearValue;
			} else if (month.equals(currentMonth - 1) || (currentMonth == 1 && month == 12)) {
				previousMonthRevenue = currentYearValue;
			}
		}

		return calculateGrowthPercentage(currentMonthRevenue, previousMonthRevenue);
	}

	private Double calculateGrowthPercentage(Double current, Double previous) {
		if (previous == null || previous == 0.0) {
			return current != null && current > 0.0 ? 100.0 : 0.0;
		}

		if (current == null) {
			return -100.0;
		}

		return Math.round(((current - previous) / previous * 100) * 100.0) / 100.0;
	}

	private String formatCurrency(Double amount) {
		if (amount == null)
			return "$0";

		if (amount >= 1000000) {
			return "$" + Math.round((amount / 1000000) * 100.0) / 100.0 + "M";
		} else if (amount >= 1000) {
			return "$" + Math.round((amount / 1000) * 10.0) / 10.0 + "k";
		} else {
			return "$" + Math.round(amount * 100.0) / 100.0;
		}
	}

	private String formatPercentage(Double percentage) {
		if (percentage == null)
			return "0%";

		String sign = percentage >= 0 ? "+" : "";
		return sign + Math.round(percentage * 10.0) / 10.0 + "%";
	}
}
