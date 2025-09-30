package project.ktc.springboot_app.payment.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.dto.projection.PriceRange;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.payment.dto.SampleDataGenerationDTO;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.interfaces.PaymentDataGenerationService;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDataGenerationServiceImpl implements PaymentDataGenerationService {

	private final PaymentRepository paymentRepository;
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final Random random = new Random();

	// Minimum payment count threshold to consider data sufficient
	private static final long SUFFICIENT_DATA_THRESHOLD = 1000L;

	@Override
	public boolean hasSufficientPaymentData() {
		long currentCount = paymentRepository.count();
		return currentCount >= SUFFICIENT_DATA_THRESHOLD;
	}

	@Override
	public long getCurrentPaymentCount() {
		return paymentRepository.count();
	}

	@Override
	@Transactional
	public SampleDataGenerationDTO generateSamplePaymentData() {
		log.info("Starting sample payment data generation...");

		// Check if we already have sufficient data
		if (hasSufficientPaymentData()) {
			return SampleDataGenerationDTO.builder()
					.success(false)
					.message("Database already contains sufficient payment data for analytics")
					.paymentsGenerated(0L)
					.totalPayments(getCurrentPaymentCount())
					.details(
							"Current payment count exceeds the minimum threshold of " + SUFFICIENT_DATA_THRESHOLD)
					.build();
		}

		try {
			// Get students and courses for random assignment with validation
			List<User> students = userRepository
					.findUsersWithFilters(
							null, "STUDENT", true, org.springframework.data.domain.Pageable.unpaged())
					.getContent();
			List<Course> courses = courseRepository.findAll();

			// Validate minimum student count (must be at least 5)
			if (students.size() < 5) {
				return SampleDataGenerationDTO.builder()
						.success(false)
						.message("Cannot generate payment data: Insufficient students in database")
						.paymentsGenerated(0L)
						.totalPayments(getCurrentPaymentCount())
						.details(
								String.format(
										"Found %d students, but minimum 5 active students required for payment data generation",
										students.size()))
						.build();
			}

			// Validate minimum course count (must be at least 8)
			if (courses.size() < 8) {
				return SampleDataGenerationDTO.builder()
						.success(false)
						.message("Cannot generate payment data: Insufficient courses in database")
						.paymentsGenerated(0L)
						.totalPayments(getCurrentPaymentCount())
						.details(
								String.format(
										"Found %d courses, but minimum 8 courses required for payment data generation",
										courses.size()))
						.build();
			}

			List<Payment> paymentsToSave = new ArrayList<>();
			long totalGenerated = 0;

			// Generate payments for current year and two previous years
			int currentYear = LocalDate.now().getYear();
			for (int year = currentYear - 2; year <= currentYear; year++) {
				totalGenerated += generatePaymentsForYear(year, students, courses, paymentsToSave);
			}

			// Save all payments in batches to improve performance
			List<Payment> savedPayments = paymentRepository.saveAll(paymentsToSave);

			log.info("Successfully generated {} sample payments", savedPayments.size());

			return SampleDataGenerationDTO.builder()
					.success(true)
					.message("Sample payment data generated successfully")
					.paymentsGenerated(totalGenerated)
					.totalPayments(getCurrentPaymentCount())
					.details(
							String.format(
									"Generated %d payments across years %d-%d with seasonal patterns",
									totalGenerated, currentYear - 2, currentYear))
					.build();

		} catch (Exception e) {
			log.error("Error generating sample payment data: {}", e.getMessage(), e);
			return SampleDataGenerationDTO.builder()
					.success(false)
					.message("Failed to generate sample payment data: " + e.getMessage())
					.paymentsGenerated(0L)
					.totalPayments(getCurrentPaymentCount())
					.details("Please check server logs for detailed error information")
					.build();
		}
	}

	private long generatePaymentsForYear(
			int year, List<User> students, List<Course> courses, List<Payment> paymentsToSave) {
		long paymentsGenerated = 0;

		for (int month = 1; month <= 12; month++) {
			LocalDate monthStart = LocalDate.of(year, month, 1);
			LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

			// Calculate number of days to generate data for (leave some days empty to save
			// space)
			int daysInMonth = monthEnd.getDayOfMonth();
			int daysWithData = random.nextInt(5) + (daysInMonth - 7); // Leave 2-7 days empty randomly

			for (int day = 1; day <= daysWithData; day++) {
				LocalDate currentDate = LocalDate.of(year, month, day);
				int paymentsForDay = calculatePaymentsForDay(currentDate, year, month, day);

				for (int i = 0; i < paymentsForDay; i++) {
					Payment payment = createPayment(currentDate, students, courses);
					paymentsToSave.add(payment);
					paymentsGenerated++;
				}
			}
		}

		return paymentsGenerated;
	}

	private int calculatePaymentsForDay(LocalDate date, int year, int month, int day) {
		int basePayments = 1 + random.nextInt(8); // Base 1-8 payments per day

		// First week of month (discount period) - significantly more payments
		if (day <= 7) {
			basePayments *= 2.5; // 250% increase
		}

		// School season boost (late August to early October)
		if ((month == 8 && day >= 20) || month == 9 || (month == 10 && day <= 10)) {
			basePayments *= 2.0; // 200% increase
		}

		// Black Friday boost (November 20-30)
		if (month == 11 && day >= 20 && day <= 30) {
			basePayments *= 3.0; // 300% increase
		}

		// New Year boost (January 1-15)
		if (month == 1 && day <= 15) {
			basePayments *= 1.8; // 180% increase
		}

		// Christmas season (December 15-25)
		if (month == 12 && day >= 15 && day <= 25) {
			basePayments *= 1.5; // 150% increase
		}

		// Add some randomness for realistic variation
		double randomMultiplier = 0.7 + (random.nextDouble() * 0.6); // 0.7 to 1.3
		basePayments = (int) (basePayments * randomMultiplier);

		return Math.max(0, basePayments); // Ensure non-negative
	}

	private Payment createPayment(LocalDate date, List<User> students, List<Course> courses) {
		Payment payment = new Payment();

		// Random student and course
		User randomStudent = students.get(random.nextInt(students.size()));
		Course randomCourse = courses.get(random.nextInt(courses.size()));

		// Get dynamic price range from courses in database
		PriceRange priceRange = courseRepository.findMinAndMaxPrice();
		BigDecimal minPrice = priceRange != null && priceRange.getMinPrice() != null
				? priceRange.getMinPrice()
				: BigDecimal.valueOf(30); // Fallback to 30 if no price range found
		BigDecimal maxPrice = priceRange != null && priceRange.getMaxPrice() != null
				? priceRange.getMaxPrice()
				: BigDecimal.valueOf(300); // Fallback to 300 if no price range found

		// Random amount between min and max price
		double priceRange_double = maxPrice.subtract(minPrice).doubleValue();
		BigDecimal amount = minPrice.add(BigDecimal.valueOf(random.nextDouble() * priceRange_double));
		amount = amount.setScale(2, RoundingMode.HALF_UP);

		// Random time during the day
		LocalTime randomTime = LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
		LocalDateTime paymentDateTime = LocalDateTime.of(date, randomTime);

		payment.setUser(randomStudent);
		payment.setCourse(randomCourse);
		payment.setAmount(amount);
		payment.setStatus(Payment.PaymentStatus.COMPLETED);
		payment.setPaymentMethod("stripe");
		payment.setPaidAt(paymentDateTime);
		payment.setSessionId(java.util.UUID.randomUUID().toString());

		return payment;
	}
}
