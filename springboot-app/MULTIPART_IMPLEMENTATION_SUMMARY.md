# Profile Update API - Multipart Implementation Summary

## ✅ **Changes Made**

### **1. Controller Updates (UserController.java)**

**Before:**

```java
@PutMapping("/profile")
public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
    @Valid @RequestBody UpdateUserDto userDto) {
    return userService.updateProfile(userDto);
}
```

**After:**

```java
@PutMapping(value = "/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Operation(
    summary = "Update user profile with optional image upload",
    description = """
        Updates the profile of the currently authenticated user including name, bio, and optionally uploads a new profile image.

        **Multipart Form Data:**
        - `user` (JSON): User profile data (name, bio)
        - `thumbnail` (File, optional): Profile image file

        **Supported Image Formats:** JPEG, PNG, GIF, BMP, WebP
        **Maximum File Size:** 10MB
        """,
    security = @SecurityRequirement(name = "bearerAuth")
)
public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
    @Parameter(description = "User profile data as JSON", required = true)
    @Valid @RequestPart("user") UpdateUserDto userDto,
    @Parameter(description = "Optional profile image file", required = false)
    @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile) {
    return userService.updateProfile(userDto, thumbnailFile);
}
```

**Key Changes:**

- Added `consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE}`
- Changed `@RequestBody` to `@RequestPart("user")` for JSON data
- Added `@RequestPart("thumbnail")` for optional file upload
- Enhanced Swagger documentation with detailed descriptions
- Added parameter descriptions and content type specifications

### **2. Service Interface Updates (UserService.java)**

**Added new method signature:**

```java
ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(UpdateUserDto userDto, MultipartFile thumbnailFile);
```

### **3. Service Implementation Updates (UserServiceImp.java)**

**Added dependencies:**

```java
private final CloudinaryServiceImp cloudinaryService;
private final FileValidationService fileValidationService;
```

**New method implementation:**

- Handles both profile data update and optional image upload
- Validates uploaded image files using existing FileValidationService
- Uploads images to Cloudinary and updates user's thumbnailUrl and thumbnailId
- Deletes old profile image when new one is uploaded
- Maintains backward compatibility with existing updateProfile method

**Key Features:**

- File validation (format, size, content)
- Automatic old image cleanup
- Detailed logging for debugging
- Proper error handling and user feedback
- Maintains transaction integrity

### **4. UpdateUserDto (Already Updated)**

The DTO already includes all necessary fields:

```java
@Schema(description = "User's full name", example = "John Doe", required = true)
@NotBlank(message = "Name cannot be blank")
@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
private String name;

@Schema(description = "User's biography or description", required = false)
@Size(max = 500, message = "Bio cannot exceed 500 characters")
private String bio;

@Schema(description = "URL to user's profile picture", required = false)
@Size(max = 255, message = "Thumbnail URL cannot exceed 255 characters")
private String thumbnailUrl;

@Schema(description = "ID of the thumbnail image in storage", required = false)
@Size(max = 255, message = "Thumbnail ID cannot exceed 255 characters")
private String thumbnailId;
```

### **5. Cloudinary Service Integration**

**Leverages existing services:**

- `CloudinaryServiceImp.uploadImage(MultipartFile file)` - for uploading images
- `CloudinaryServiceImp.deleteImage(String publicId)` - for deleting old images
- `FileValidationService.validateImageFile(MultipartFile file)` - for file validation

## ✅ **API Usage**

### **Request Format:**

- **Content-Type:** `multipart/form-data`
- **Parts:**
  - `user` (JSON): User profile data
  - `thumbnail` (File, optional): Profile image

### **Example Request (cURL):**

```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "user={\"name\":\"John Doe\",\"bio\":\"Software developer\"};type=application/json" \
  -F "thumbnail=@profile-image.jpg;type=image/jpeg"
```

### **Response Format:**

```json
{
  "statusCode": 200,
  "message": "Profile updated successfully",
  "data": {
    "id": "user-123",
    "email": "john.doe@example.com",
    "name": "John Doe",
    "bio": "Software developer",
    "thumbnailUrl": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/course-management/profile_abc123.jpg",
    "thumbnailId": "course-management/profile_abc123",
    "roles": ["STUDENT"]
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

## ✅ **Features Implemented**

1. **Multipart Form Data Support** - Accepts JSON data + file upload
2. **Optional Image Upload** - Thumbnail parameter is optional
3. **File Validation** - Uses existing validation service
4. **Cloudinary Integration** - Leverages existing upload/delete services
5. **Old Image Cleanup** - Automatically deletes previous profile images
6. **Comprehensive Documentation** - Enhanced Swagger annotations
7. **Error Handling** - Proper validation and error responses
8. **Backward Compatibility** - Original method still exists
9. **Security** - Maintains JWT authentication requirements
10. **Logging** - Detailed logging for debugging and monitoring

## ✅ **Benefits**

1. **User Experience** - Single API call for profile + image update
2. **Efficiency** - No need for separate image upload endpoint
3. **Data Consistency** - Profile and image updated atomically
4. **Clean Architecture** - Reuses existing services and validation
5. **Developer Friendly** - Clear documentation and examples
6. **Production Ready** - Proper error handling and validation

The implementation maintains clean code principles while providing a robust, user-friendly API for profile updates with optional image uploads.
