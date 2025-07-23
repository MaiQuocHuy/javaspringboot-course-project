# API Request Body Examples

## Update User Profile - PUT /api/users/profile

### Example Request Body (JSON):

```json
{
  "name": "John Doe",
  "bio": "Experienced software developer with expertise in Java Spring Boot and React. Passionate about clean code and best practices.",
  "thumbnailUrl": "https://example.com/images/profile/john-doe.jpg",
  "thumbnailId": "img_abc123xyz"
}
```

### Minimal Request Body (only required fields):

```json
{
  "name": "Jane Smith"
}
```

### Request Body with Bio only:

```json
{
  "name": "Mike Johnson",
  "bio": "Full-stack developer and tech enthusiast"
}
```

### Field Descriptions:

- **name** (required): User's full name (2-100 characters)
- **bio** (optional): User's biography or description (max 500 characters)
- **thumbnailUrl** (optional): URL to user's profile picture (max 255 characters)
- **thumbnailId** (optional): ID reference for the thumbnail in storage system (max 255 characters)

### Response Example:

```json
{
  "statusCode": 200,
  "message": "Profile updated successfully",
  "data": {
    "id": "user-123",
    "email": "john.doe@example.com",
    "name": "John Doe",
    "bio": "Experienced software developer with expertise in Java Spring Boot and React. Passionate about clean code and best practices.",
    "thumbnailUrl": "https://example.com/images/profile/john-doe.jpg",
    "thumbnailId": "img_abc123xyz",
    "roles": ["STUDENT"]
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```
