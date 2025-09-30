package project.ktc.springboot_app.course.dto.projection;

import java.math.BigDecimal;

public interface PriceRange {
  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();
}
