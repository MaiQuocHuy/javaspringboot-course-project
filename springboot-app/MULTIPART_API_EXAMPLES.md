# Multipart Profile Update API Examples

## Update User Profile with Image Upload - PUT /api/users/profile

### Example 1: Update profile with image upload (multipart/form-data)

**Form Data:**

- `user` (JSON):

```json
{
  "name": "John Doe",
  "bio": "Full-stack developer with 5 years of experience in Java and React"
}
```

- `thumbnail` (File): profile-image.jpg

### Example 2: Update profile without image (multipart/form-data)

**Form Data:**

- `user` (JSON):

```json
{
  "name": "Jane Smith",
  "bio": "Software engineer passionate about clean code and best practices"
}
```

- `thumbnail`: (empty/not provided)

### Example 3: Minimal update (multipart/form-data)

**Form Data:**

- `user` (JSON):

```json
{
  "name": "Mike Johnson"
}
```

### cURL Examples:

#### With image upload:

```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "user={\"name\":\"John Doe\",\"bio\":\"Full-stack developer\"};type=application/json" \
  -F "thumbnail=@/path/to/profile-image.jpg;type=image/jpeg"
```

#### Without image upload:

```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "user={\"name\":\"John Doe\",\"bio\":\"Full-stack developer\"};type=application/json"
```

### JavaScript/Fetch Example:

```javascript
const formData = new FormData();

// Add user data as JSON
const userData = {
  name: "John Doe",
  bio: "Full-stack developer with 5 years of experience",
};
formData.append(
  "user",
  new Blob([JSON.stringify(userData)], {
    type: "application/json",
  })
);

// Add image file (optional)
const fileInput = document.getElementById("thumbnail-input");
if (fileInput.files[0]) {
  formData.append("thumbnail", fileInput.files[0]);
}

// Send request
fetch("/api/users/profile", {
  method: "PUT",
  headers: {
    Authorization: "Bearer " + yourJwtToken,
  },
  body: formData,
})
  .then((response) => response.json())
  .then((data) => console.log("Success:", data))
  .catch((error) => console.error("Error:", error));
```

### Response Example:

```json
{
  "statusCode": 200,
  "message": "Profile updated successfully",
  "data": {
    "id": "user-123",
    "email": "john.doe@example.com",
    "name": "John Doe",
    "bio": "Full-stack developer with 5 years of experience",
    "thumbnailUrl": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/course-management/profile_abc123.jpg",
    "thumbnailId": "course-management/profile_abc123",
    "roles": ["STUDENT"]
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

### Validation Rules:

**User Data (JSON part):**

- `name`: Required, 2-100 characters
- `bio`: Optional, max 500 characters

**Thumbnail File (File part):**

- Optional
- Supported formats: JPEG, PNG, GIF, BMP, WebP
- Maximum size: 10MB
- When uploaded, replaces existing profile image

### Error Examples:

#### Invalid file format:

```json
{
  "statusCode": 400,
  "message": "Failed to upload thumbnail: Invalid file format. Only JPEG, PNG, GIF, BMP, and WebP are allowed.",
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

#### Validation error:

```json
{
  "statusCode": 400,
  "message": "Name cannot be null or empty",
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```
