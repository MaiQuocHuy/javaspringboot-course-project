package project.ktc.springboot_app.log.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.log.dto.CreateLogDto;
import project.ktc.springboot_app.log.dto.LogResponseDto;

/**
 * Service interface for system log operations Following the Interface
 * Segregation Principle (ISP)
 */
public interface LogService {

	/**
	 * Creates a new system log entry
	 *
	 * @param createLogDto
	 *            The log data to create
	 * @return ResponseEntity with the created log information
	 */
	ResponseEntity<ApiResponse<LogResponseDto>> createLog(CreateLogDto createLogDto);
}
