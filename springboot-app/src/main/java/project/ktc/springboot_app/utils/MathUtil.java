package project.ktc.springboot_app.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

/** Utility class for consistent mathematical operations and formatting across the application */
@UtilityClass
public class MathUtil {

  /**
   * Rounds a double value to 2 decimal places using HALF_UP rounding mode This ensures consistent
   * rating display across the application
   *
   * @param value the value to round
   * @return the rounded value, or 0.0 if value is null
   */
  public Double roundToTwoDecimals(Double value) {
    if (value == null) {
      return 0.0;
    }
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Rounds a double value to specified decimal places using HALF_UP rounding mode
   *
   * @param value the value to round
   * @param scale the number of decimal places
   * @return the rounded value, or 0.0 if value is null
   */
  public Double roundToDecimals(Double value, int scale) {
    if (value == null) {
      return 0.0;
    }
    return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Safely converts a BigDecimal to Double with null handling
   *
   * @param value the BigDecimal value
   * @return the double value, or 0.0 if value is null
   */
  public Double safeDoubleValue(BigDecimal value) {
    return value != null ? value.doubleValue() : 0.0;
  }

  /**
   * Safely converts a Long to Integer with null handling
   *
   * @param value the Long value
   * @return the integer value, or 0 if value is null
   */
  public Integer safeIntValue(Long value) {
    return value != null ? value.intValue() : 0;
  }
}
