package project.ktc.springboot_app.payment.interfaces;

import project.ktc.springboot_app.payment.dto.SampleDataGenerationDTO;

public interface PaymentDataGenerationService {

	/**
	 * Generates sample payment data for testing and development purposes. Creates
	 * payments for
	 * current year and two previous years with realistic patterns including
	 * seasonal variations,
	 * discount periods, and special occasions.
	 *
	 * @return Response containing the result of data generation
	 */
	SampleDataGenerationDTO generateSamplePaymentData();

	/**
	 * Checks if there is sufficient payment data in the database
	 *
	 * @return true if there's enough data, false otherwise
	 */
	boolean hasSufficientPaymentData();

	/**
	 * Gets the current count of payments in the database
	 *
	 * @return current payment count
	 */
	long getCurrentPaymentCount();
}
