package project.ktc.springboot_app.instructor_application.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.instructor_application.dto.AdminApplicationDetailDto;
import project.ktc.springboot_app.instructor_application.dto.AdminInstructorApplicationResponseDto;
import project.ktc.springboot_app.instructor_application.services.InstructorApplicationServiceImp;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Application Management API", description = "Endpoints for admin to manage instructor applications")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstructorApplicationController {
    private final InstructorApplicationServiceImp instructorApplicationService;

    @GetMapping("/applications")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<AdminInstructorApplicationResponseDto>>> getAllApplicationAdmin() {
        return instructorApplicationService.getAllApplicationAdmin();
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminApplicationDetailDto>> getApplicationByIdAdmin(
            @PathVariable String id) {
        return instructorApplicationService.getApplicationByIdAdmin(id);
    }
}
