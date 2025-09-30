package project.ktc.springboot_app.chat.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import project.ktc.springboot_app.chat.dtos.AsyncSendMessageRequest;
import project.ktc.springboot_app.chat.enums.MessageType;

public class AsyncMessageRequestValidator
    implements ConstraintValidator<ValidAsyncMessageRequest, AsyncSendMessageRequest> {

  @Override
  public boolean isValid(AsyncSendMessageRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }

    context.disableDefaultConstraintViolation();

    try {
      MessageType messageType = MessageType.fromValue(request.getType());

      if (messageType.isTextType()) {
        return validateTextMessage(request, context);
      } else if (messageType.isMediaType()) {
        return validateMediaMessage(request, context, messageType);
      }
    } catch (IllegalArgumentException e) {
      context
          .buildConstraintViolationWithTemplate("Invalid message type: " + request.getType())
          .addPropertyNode("type")
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  private boolean validateTextMessage(
      AsyncSendMessageRequest request, ConstraintValidatorContext context) {
    if (request.getContent() == null || request.getContent().isBlank()) {
      context
          .buildConstraintViolationWithTemplate("Content is required for text messages")
          .addPropertyNode("content")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean validateMediaMessage(
      AsyncSendMessageRequest request,
      ConstraintValidatorContext context,
      MessageType messageType) {
    // Media messages require fileUrl (pre-uploaded via /api/upload/* endpoints)
    boolean hasFileUrl = request.getFileUrl() != null && !request.getFileUrl().isBlank();

    if (!hasFileUrl) {
      context
          .buildConstraintViolationWithTemplate(
              "FileUrl is required for "
                  + messageType.getValue()
                  + " messages. Upload the file first via /api/upload/* endpoints")
          .addPropertyNode("fileUrl")
          .addConstraintViolation();
      return false;
    }

    // Validate fileName is provided
    if (request.getFileName() == null || request.getFileName().isBlank()) {
      context
          .buildConstraintViolationWithTemplate("FileName is required for media messages")
          .addPropertyNode("fileName")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
