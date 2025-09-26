package project.ktc.springboot_app.certificate.services;

/**
 * Service interface for handling asynchronous certificate processing
 * operations.
 * This service is responsible for background tasks like PDF generation,
 * file upload, and email notifications that shouldn't block the main
 * certificate creation flow.
 */
public interface CertificateAsyncService {

    /**
     * Processes certificate asynchronously after the certificate record has been
     * saved.
     * This includes:
     * - Generating certificate PDF/image
     * - Uploading to cloud storage
     * - Updating certificate record with file URL
     * - Sending email notification to user
     * 
     * @param certificateId The ID of the certificate to process
     */
    void processCertificateAsync(String certificateId);
}