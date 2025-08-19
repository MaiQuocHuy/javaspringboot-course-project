package project.ktc.springboot_app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.auth.dto.UserResponseDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPageResponseDto {

    private List<UserResponseDto> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    // Statistics
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long instructors;
    private long students;
    private long admins;
}
