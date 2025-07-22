# Cloudinary Image Upload Service

This feature provides a complete image upload solution using Cloudinary for the Spring Boot course management system.

## 📁 Feature Structure

```
upload/
├── config/
│   └── CloudinaryConfig.java          # Cloudinary client configuration
├── controller/
│   └── UploadController.java          # REST endpoints for file operations
├── dto/
│   └── ImageUploadResponseDto.java    # Response DTO for upload results
├── exception/
│   ├── ImageUploadException.java      # Upload failure exception
│   └── InvalidImageFormatException.java # Invalid file format exception
└── service/
    └── CloudinaryService.java        # Core upload service logic
```

## 🚀 Features

### ✅ Image Upload

- **Endpoint**: `POST /api/upload/image`
- **Supported Formats**: JPEG, JPG, PNG, GIF, BMP, WebP
- **Maximum Size**: 10MB
- **Automatic Optimization**: Quality and format optimization
- **Organized Storage**: Files stored in `course-management/` folder

### ✅ Image Deletion

- **Endpoint**: `DELETE /api/upload/image/{publicId}`
- **Secure Deletion**: Remove images by public ID
- **Status Tracking**: Returns success/failure status

### ✅ Validation & Security

- File size validation (max 10MB)
- Content type validation
- Automatic file naming with timestamps
- Secure URL generation

## 📊 API Endpoints

### Upload Image

```http
POST /api/upload/image
Content-Type: multipart/form-data

{
  "file": [binary image data]
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "message": "Image uploaded successfully",
  "data": {
    "url": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/course-management/filename_1234567890_abcd1234.jpg",
    "publicId": "course-management/filename_1234567890_abcd1234",
    "originalFilename": "profile.jpg",
    "size": 2048576,
    "format": "jpg",
    "width": 1920,
    "height": 1080
  },
  "timestamp": "2025-07-22T10:29:34.123+07:00"
}
```

**Error Response (400):**

```json
{
  "statusCode": 400,
  "message": "Invalid file format. Allowed formats: image/jpeg, image/jpg, image/png, image/gif, image/bmp, image/webp",
  "error": "Invalid File Format",
  "path": "/api/upload/image",
  "timestamp": "2025-07-22T10:29:34.123+07:00"
}
```

### Delete Image

```http
DELETE /api/upload/image/{publicId}
```

**Success Response (200):**

```json
{
  "statusCode": 200,
  "message": "Image deleted successfully",
  "data": null,
  "timestamp": "2025-07-22T10:29:34.123+07:00"
}
```

## 🔧 Configuration

### Application Properties

```properties
# Cloudinary Configuration
cloudinary.cloudName=${CLOUDINARY_CLOUD_NAME:your-cloud-name}
cloudinary.apiKey=${CLOUDINARY_API_KEY:your-api-key}
cloudinary.apiSecret=${CLOUDINARY_API_SECRET:your-api-secret}

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.file-size-threshold=2KB
```

### Environment Variables

```bash
CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

## 💡 Usage Examples

### Frontend (JavaScript/React)

```javascript
const uploadImage = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await fetch("/api/upload/image", {
      method: "POST",
      body: formData,
    });

    const result = await response.json();
    console.log("Upload successful:", result.data.url);
    return result.data;
  } catch (error) {
    console.error("Upload failed:", error);
  }
};
```

### cURL Command

```bash
curl -X POST \
  http://localhost:8080/api/upload/image \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/image.jpg'
```

## 🧪 Testing

### Run Unit Tests

```bash
./mvnw test -Dtest=CloudinaryServiceTest
```

### Manual Testing with Swagger

1. Start the application: `./mvnw spring-boot:run`
2. Open Swagger UI: `http://localhost:8080/swagger-ui.html`
3. Navigate to "File Upload API" section
4. Test the `/api/upload/image` endpoint

## 🛡️ Error Handling

The service includes comprehensive error handling for:

- **Empty files**: Returns 400 with "File is empty" message
- **Invalid formats**: Returns 400 with supported format list
- **File too large**: Returns 400 with size limit information
- **Upload failures**: Returns 500 with descriptive error message
- **Network issues**: Graceful degradation with retry logic

## 📈 Performance Optimizations

- **Automatic Quality Optimization**: `quality: auto:good`
- **Format Optimization**: `fetch_format: auto`
- **Lazy Loading**: Efficient image delivery
- **CDN Distribution**: Global content delivery via Cloudinary

## 🔒 Security Features

- **Content Type Validation**: Only allows image MIME types
- **File Size Limits**: Prevents DoS attacks via large files
- **Secure URLs**: All uploaded images use HTTPS
- **Public ID Generation**: Prevents file name collisions
- **Input Sanitization**: Cleans file names to prevent injection

## 🚦 Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 201  | Image uploaded successfully  |
| 200  | Image deleted successfully   |
| 400  | Invalid file format or size  |
| 404  | Image not found for deletion |
| 500  | Upload/deletion failed       |

This implementation provides a production-ready image upload solution with proper error handling, validation, and security measures.
