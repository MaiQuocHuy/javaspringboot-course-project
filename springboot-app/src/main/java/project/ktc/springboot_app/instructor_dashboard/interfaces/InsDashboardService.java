package project.ktc.springboot_app.instructor_dashboard.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;

public interface InsDashboardService {
	ResponseEntity<ApiResponse<InsDashboardDto>> getInsDashboardStatistics();
}
