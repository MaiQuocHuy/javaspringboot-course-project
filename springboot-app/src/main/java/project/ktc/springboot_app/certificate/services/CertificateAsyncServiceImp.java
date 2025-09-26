package project.ktc.springboot_app.certificate.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.certificate.dto.CertificateDataDto;
import project.ktc.springboot_app.certificate.entity.Certificate;
import project.ktc.springboot_app.certificate.repositories.CertificateRepository;
import project.ktc.springboot_app.upload.interfaces.CloudinaryService;
import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;

import java.util.Optional;

/**
 * Implementation of CertificateAsyncService for handling asynchronous
 * certificate processing
 * This service is dedicated to background tasks like PDF generation, cloud
 * upload, and email notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateAsyncServiceImp implements CertificateAsyncService {

    private final CertificateRepository certificateRepository;
    private final CertificateImageService certificateImageService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    /**
     * Process certificate PDF generation, cloud upload, and email notification
     * asynchronously
     * This method runs in a separate thread and has its own transaction context
     * 
     * @param certificateId The ID of the certificate to process
     */
    @Override
    @Async("taskExecutor")
    @Transactional
    public void processCertificateAsync(String certificateId) {
        log.info("Starting async processing for certificate ID: {}", certificateId);

        try {
            // Reload the certificate entity with all necessary relationships in a fresh
            // transaction
            // Use method with JOIN FETCH to avoid LazyInitializationException
            Optional<Certificate> certificateOpt = certificateRepository.findByIdWithRelationships(certificateId);
            if (certificateOpt.isEmpty()) {
                log.error("Certificate not found with ID: {} during async processing", certificateId);
                return;
            }

            Certificate certificate = certificateOpt.get();

            // Ensure all necessary relationships are loaded to avoid
            // LazyInitializationException
            // The certificate should have user, course, and course.instructor loaded
            if (certificate.getUser() == null || certificate.getCourse() == null) {
                log.error("Certificate {} has missing relationships during async processing", certificateId);
                return;
            }

            log.info("Certificate reloaded successfully: {}", certificate.getCertificateCode());

            // Generate image and upload to cloud storage
            generateAndUploadCertificateImage(certificate);

            log.info("Async processing completed successfully for certificate: {}", certificate.getCertificateCode());

        } catch (Exception e) {
            log.error("Failed to process certificate asynchronously for ID {}: {}",
                    certificateId, e.getMessage(), e);
            // Note: In a production environment, you might want to implement retry logic
            // or move failed certificates to a dead letter queue for manual processing
        }
    }

    /**
     * Generate certificate image and upload to cloud storage asynchronously
     */
    private void generateAndUploadCertificateImage(Certificate certificate) {
        try {
            log.info("Starting image generation for certificate: {}", certificate.getCertificateCode());

            // Prepare certificate data for image generation
            CertificateDataDto certificateData = CertificateDataDto.builder()
                    .studentName(certificate.getUser().getName())
                    .studentEmail(certificate.getUser().getEmail())
                    .courseTitle(certificate.getCourse().getTitle())
                    .instructorName(certificate.getCourse().getInstructor().getName())
                    .certificateCode(certificate.getCertificateCode())
                    .issueDate(certificate.getIssuedAt())
                    .courseLevel(
                            certificate.getCourse().getLevel() != null ? certificate.getCourse().getLevel().toString()
                                    : "General")
                    .build();

            // Generate Image
            byte[] imageData = certificateImageService.generateCertificateImageDirect(certificateData);

            // Generate filename
            String filename = certificateImageService.generateImageFilename(
                    certificate.getUser().getId(),
                    certificate.getCourse().getId(),
                    certificate.getCertificateCode());

            // Upload to Cloudinary
            ImageUploadResponseDto uploadResponse = cloudinaryService.uploadCertificateImage(imageData, filename);
            log.info("Url Image: {}", uploadResponse);

            // Update certificate with file URL
            certificate.setFileUrl(uploadResponse.getUrl());
            certificateRepository.save(certificate);

            log.info("PDF generated and uploaded successfully for certificate: {}. URL: {}",
                    certificate.getCertificateCode(), uploadResponse.getUrl());

            // Send email notification asynchronously
            sendCertificateNotificationEmail(certificate, uploadResponse.getUrl());

        } catch (Exception e) {
            log.error("Failed to generate/upload PDF for certificate {}: {}",
                    certificate.getCertificateCode(), e.getMessage(), e);
        }
    }

    /**
     * Send certificate notification email
     */
    private void sendCertificateNotificationEmail(Certificate certificate, String certificateUrl) {
        try {
            log.info("Sending certificate notification email for: {}", certificate.getCertificateCode());

            emailService.sendCertificateNotificationEmailAsync(
                    certificate.getUser().getEmail(),
                    certificate.getUser().getName(),
                    certificate.getCourse().getTitle(),
                    certificate.getCourse().getInstructor().getName(),
                    certificate.getCertificateCode(),
                    certificateUrl,
                    certificate.getIssuedAt()).thenAccept(result -> {
                        if (result.isSuccess()) {
                            log.info("Certificate notification email sent successfully for: {}",
                                    certificate.getCertificateCode());
                        } else {
                            log.error("Failed to send certificate notification email for {}: {}",
                                    certificate.getCertificateCode(), result.getErrorMessage());
                        }
                    }).exceptionally(throwable -> {
                        log.error("Exception sending certificate notification email for {}: {}",
                                certificate.getCertificateCode(), throwable.getMessage(), throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error sending certificate notification email for {}: {}",
                    certificate.getCertificateCode(), e.getMessage(), e);
        }
    }
}