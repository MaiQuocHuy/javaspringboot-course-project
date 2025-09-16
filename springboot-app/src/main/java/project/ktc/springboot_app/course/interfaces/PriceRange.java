package project.ktc.springboot_app.course.interfaces;

import java.math.BigDecimal;

public interface PriceRange {
  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();
}
