package project.ktc.springboot_app.notification.interfaces;

import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;

/** Service interface for notification operations */
public interface NotificationService {

	/**
	 * Create a new notification
	 *
	 * @param createNotificationDto
	 *            the notification data
	 * @return the created notification response
	 */
	CompletableFuture<NotificationResponseDto> createNotification(
			CreateNotificationDto createNotificationDto);

	/**
	 * Create a notification synchronously (for internal use)
	 *
	 * @param createNotificationDto
	 *            the notification data
	 * @return the created notification response
	 */
	NotificationResponseDto createNotificationSync(CreateNotificationDto createNotificationDto);

	ResponseEntity<ApiResponse<PaginatedResponse<NotificationDto>>> getNotificationsByUserId(
			String userId, Pageable pageable);

	ResponseEntity<ApiResponse<Void>> markNotificationAsRead(String id);

	ResponseEntity<ApiResponse<Void>> deleteNotification(String id);
}
