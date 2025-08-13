# Email API Documentation

This document describes the REST API endpoints for the Email Service in the Spring Boot application.

## Base URL
All email API endpoints are available under: `/api/email`

## Authentication
Most endpoints require authentication with `ADMIN` or `INSTRUCTOR` roles using Spring Security.

## Endpoints

### 1. Send Email
**POST** `/api/email/send`

Send email with full customization including attachments, templates, and multiple recipients.

**Authorization:** `ADMIN` or `INSTRUCTOR` role required

**Request Body:**
```json
{
  "to": ["recipient1@example.com", "recipient2@example.com"],
  "cc": ["cc@example.com"],
  "bcc": ["bcc@example.com"],
  "from": "sender@example.com",
  "replyTo": "reply@example.com",
  "subject": "Email Subject",
  "htmlBody": "<h1>HTML Content</h1>",
  "plainTextBody": "Plain text content",
  "templateName": "welcome-template",
  "templateVariables": {
    "name": "John Doe",
    "course": "Spring Boot Course"
  },
  "attachments": [
    {
      "filename": "document.pdf",
      "content": "base64-encoded-content",
      "contentType": "application/pdf"
    }
  ],
  "async": true,
  "priority": "NORMAL"
}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Email sent successfully",
  "data": {
    "success": true,
    "messageId": "msg-12345",
    "sentAt": "2024-01-15T10:30:00Z",
    "provider": "SMTP",
    "attemptCount": 1
  },
  "timestamp": "2024-01-15T10:30:00.123Z"
}
```

### 2. Send Simple Email
**POST** `/api/email/send-simple`

Send a simple text email to a single recipient.

**Authorization:** `ADMIN` or `INSTRUCTOR` role required

**Parameters:**
- `to` (required): Recipient email address
- `subject` (required): Email subject
- `content` (required): Email content
- `async` (optional, default: true): Send asynchronously

**Example Request:**
```
POST /api/email/send-simple?to=user@example.com&subject=Hello&content=This is a test email&async=true
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Simple email sent successfully",
  "data": {
    "success": true,
    "messageId": "msg-67890",
    "sentAt": "2024-01-15T10:35:00Z",
    "provider": "SendGrid",
    "attemptCount": 1
  },
  "timestamp": "2024-01-15T10:35:00.456Z"
}
```

### 3. Send Template Email
**POST** `/api/email/send-template`

Send email using a predefined template with variables.

**Authorization:** `ADMIN` or `INSTRUCTOR` role required

**Request Body:**
```json
{
  "to": "user@example.com",
  "subject": "Welcome to our platform",
  "templateName": "welcome",
  "templateVariables": {
    "firstName": "John",
    "lastName": "Doe",
    "activationUrl": "https://example.com/activate/123"
  },
  "async": true
}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Template email sent successfully",
  "data": {
    "success": true,
    "messageId": "msg-template-123",
    "sentAt": "2024-01-15T10:40:00Z",
    "provider": "SMTP",
    "attemptCount": 1
  },
  "timestamp": "2024-01-15T10:40:00.789Z"
}
```

### 4. Send Email with Attachments
**POST** `/api/email/send-with-attachment`

Send email with file attachments using multipart form data.

**Authorization:** `ADMIN` or `INSTRUCTOR` role required

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `to` (required): Recipient email address
- `subject` (required): Email subject
- `content` (required): Email content
- `attachments` (optional): File attachments
- `async` (optional, default: true): Send asynchronously

**Response:**
```json
{
  "statusCode": 200,
  "message": "Email with attachments sent successfully",
  "data": {
    "success": true,
    "messageId": "msg-attachment-456",
    "sentAt": "2024-01-15T10:45:00Z",
    "provider": "SendGrid",
    "attemptCount": 1
  },
  "timestamp": "2024-01-15T10:45:00.012Z"
}
```

### 5. Retry Failed Emails
**POST** `/api/email/retry-failed`

Retry sending failed emails from the queue.

**Authorization:** `ADMIN` role required

**Response:**
```json
{
  "statusCode": 200,
  "message": "Processed 5 failed emails for retry",
  "data": {
    "processedCount": 5
  },
  "timestamp": "2024-01-15T10:50:00.345Z"
}
```

### 6. Cleanup Failed Emails
**DELETE** `/api/email/cleanup-failed`

Remove old failed email records from the database.

**Authorization:** `ADMIN` role required

**Parameters:**
- `days` (optional, default: 30): Number of days to keep failed email records

**Example Request:**
```
DELETE /api/email/cleanup-failed?days=60
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Cleaned up 10 failed email records older than 60 days",
  "data": {
    "cleanedCount": 10,
    "daysThreshold": 60
  },
  "timestamp": "2024-01-15T10:55:00.678Z"
}
```

### 7. Health Check
**GET** `/api/email/health`

Check if email service is running and configured properly.

**Authorization:** None required

**Response:**
```json
{
  "statusCode": 200,
  "message": "Email service is healthy",
  "data": {
    "status": "UP",
    "service": "Email Service",
    "timestamp": 1705315200000
  },
  "timestamp": "2024-01-15T11:00:00.901Z"
}
```

## Error Responses

All endpoints return standardized error responses:

```json
{
  "statusCode": 400,
  "message": "Validation error message",
  "data": null,
  "timestamp": "2024-01-15T11:05:00.234Z"
}
```

### Common HTTP Status Codes:
- `200`: Success
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (authentication required)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (template not found)
- `413`: Payload Too Large (attachment too large)
- `500`: Internal Server Error

## Email Templates

The service supports Thymeleaf templates located in `src/main/resources/templates/email/`.

**Available Templates:**
- `welcome.html`: User welcome email
- `password-reset.html`: Password reset email
- `course-enrollment.html`: Course enrollment confirmation
- `payment-confirmation.html`: Payment confirmation

**Template Variables:**
Templates can use variables passed in the `templateVariables` object. Common variables include:
- `firstName`, `lastName`: User name
- `email`: User email
- `courseName`: Course name
- `amount`: Payment amount
- `date`: Date information
- `url`: Action URLs

## Configuration

The email service is configured through application properties:

```properties
# Email Configuration
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false

# SendGrid Configuration (optional)
app.email.sendgrid.api-key=your-sendgrid-api-key
app.email.sendgrid.enabled=false

# Email Settings
app.email.from-email=noreply@example.com
app.email.from-name=Your App Name
app.email.async.pool-size=10
app.email.retry.max-attempts=3
app.email.retry.delay-minutes=5
```

## Usage Examples

### cURL Examples

**Send Simple Email:**
```bash
curl -X POST "http://localhost:8080/api/email/send-simple" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "to=user@example.com&subject=Test&content=Hello World"
```

**Send Template Email:**
```bash
curl -X POST "http://localhost:8080/api/email/send-template" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "Welcome",
    "templateName": "welcome",
    "templateVariables": {
      "firstName": "John"
    }
  }'
```

### JavaScript/Fetch Example

```javascript
const sendEmail = async () => {
  const response = await fetch('/api/email/send-simple', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({
      to: 'user@example.com',
      subject: 'Test Email',
      content: 'This is a test email from JavaScript'
    })
  });
  
  const result = await response.json();
  console.log(result);
};
```

## Swagger Documentation

When the application is running, you can access the interactive Swagger documentation at:
`http://localhost:8080/swagger-ui.html`

The Email API endpoints are grouped under the "Email API" tag with detailed parameter descriptions and example requests/responses.
