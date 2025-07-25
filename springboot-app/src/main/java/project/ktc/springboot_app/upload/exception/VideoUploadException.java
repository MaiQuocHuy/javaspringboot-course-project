package project.ktc.springboot_app.upload.exception;

/**
 * Exception thrown when video upload to Cloudinary fails
 */
public class VideoUploadException extends RuntimeException {

    public VideoUploadException(String message) {
        super(message);
    }

    public VideoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
