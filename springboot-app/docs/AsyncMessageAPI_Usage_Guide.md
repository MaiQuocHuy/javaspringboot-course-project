# Async Message API Usage Guide

## Overview

API này cho phép gửi message bất đồng bộ với các loại media (file, video, audio) theo pattern **pre-upload**.

## Workflow

### 1. Upload File trước (Required)

Frontend phải upload file trước qua các endpoint upload:

```http
POST /api/upload/image
POST /api/upload/video
POST /api/upload/audio
POST /api/upload/document
```

**Example Upload Video:**

```bash
curl -X POST \
  -H "Content-Type: multipart/form-data" \
  -F "file=@video.mp4" \
  https://api.example.com/api/upload/video
```

**Response:**

```json
{
  "statusCode": 201,
  "message": "Video uploaded successfully",
  "data": {
    "url": "https://res.cloudinary.com/example/video/upload/v1234567890/videos/abc123.mp4",
    "secureUrl": "https://res.cloudinary.com/example/video/upload/v1234567890/videos/abc123.mp4",
    "publicId": "videos/abc123",
    "format": "mp4",
    "duration": 120.5,
    "width": 1920,
    "height": 1080,
    "fileSize": 15728640
  }
}
```

### 2. Send Async Message với file URL

```http
POST /api/chat/{courseId}/messages
Content-Type: application/json
```

**Request Body:**

```json
{
  "tempId": "temp-msg-12345",
  "type": "video",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1234567890/videos/abc123.mp4",
  "fileName": "presentation.mp4",
  "fileSize": 15728640,
  "duration": 120,
  "mimeType": "video/mp4",
  "resolution": "1920x1080",
  "thumbnailUrl": "https://res.cloudinary.com/example/image/upload/v1234567890/thumbnails/thumb.jpg"
}
```

**Immediate Response (202 Accepted):**

```json
{
  "statusCode": 202,
  "message": "Message accepted and being processed",
  "data": {
    "tempId": "temp-msg-12345",
    "status": "PENDING"
  }
}
```

## Message Types và Required Fields

### TEXT Message

```json
{
  "tempId": "temp-txt-123",
  "type": "text",
  "content": "Hello world!"
}
```

### FILE Message

```json
{
  "tempId": "temp-file-123",
  "type": "file",
  "fileUrl": "https://res.cloudinary.com/example/raw/upload/v1234567890/documents/doc.pdf",
  "fileName": "document.pdf",
  "fileSize": 1024000,
  "mimeType": "application/pdf"
}
```

### VIDEO Message

```json
{
  "tempId": "temp-video-123",
  "type": "video",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1234567890/videos/video.mp4",
  "fileName": "video.mp4",
  "fileSize": 15728640,
  "duration": 120,
  "mimeType": "video/mp4",
  "resolution": "1920x1080",
  "thumbnailUrl": "https://res.cloudinary.com/example/image/upload/v1234567890/thumbnails/thumb.jpg"
}
```

### AUDIO Message

```json
{
  "tempId": "temp-audio-123",
  "type": "audio",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1234567890/audio/audio.mp3",
  "fileName": "audio.mp3",
  "fileSize": 5242880,
  "duration": 180,
  "mimeType": "audio/mpeg"
}
```

## WebSocket Status Updates

Frontend nhận status updates qua WebSocket:

```javascript
// Subscribe để nhận status updates
stomp.subscribe("/topic/courses/{courseId}/messages/status", (message) => {
  const status = JSON.parse(message.body);
  console.log(status);
});
```

### Status Flow:

1. **PENDING**: Message được tạo trong DB
2. **UPLOADING**: Đang xử lý media (thực tế file đã upload, chỉ là để consistency)
3. **SENT**: Message hoàn thành, hiển thị trong chat
4. **FAILED**: Có lỗi xảy ra

### Status Event Structure:

```json
{
  "tempId": "temp-msg-12345",
  "status": "SENT",
  "messageId": "msg-uuid-456",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1234567890/videos/abc123.mp4",
  "type": "video",
  "thumbnailUrl": "https://res.cloudinary.com/example/image/upload/v1234567890/thumbnails/thumb.jpg",
  "progress": null,
  "error": null
}
```

## Frontend Implementation Example

```javascript
class AsyncMessageSender {
  constructor(courseId, stompClient) {
    this.courseId = courseId;
    this.stomp = stompClient;
    this.pendingMessages = new Map();
  }

  async sendMediaMessage(file, messageType) {
    const tempId = `temp-${Date.now()}-${Math.random()}`;

    try {
      // 1. Upload file first
      const uploadResponse = await this.uploadFile(file, messageType);

      // 2. Send message with file URL
      const messageData = {
        tempId,
        type: messageType,
        fileUrl: uploadResponse.data.secureUrl || uploadResponse.data.url,
        fileName: file.name,
        fileSize: file.size,
        mimeType: file.type,
        ...this.extractMediaMetadata(uploadResponse.data, messageType),
      };

      const response = await fetch(`/api/chat/${this.courseId}/messages`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${this.getToken()}`,
        },
        body: JSON.stringify(messageData),
      });

      if (response.ok) {
        this.pendingMessages.set(tempId, messageData);
        this.showPendingMessage(messageData);
      }
    } catch (error) {
      console.error("Failed to send message:", error);
    }
  }

  async uploadFile(file, messageType) {
    const formData = new FormData();
    formData.append("file", file);

    const endpoint = this.getUploadEndpoint(messageType);
    const response = await fetch(endpoint, {
      method: "POST",
      body: formData,
    });

    return response.json();
  }

  getUploadEndpoint(messageType) {
    switch (messageType) {
      case "video":
        return "/api/upload/video";
      case "audio":
        return "/api/upload/audio";
      case "file":
        return "/api/upload/document";
      default:
        throw new Error(`Unsupported type: ${messageType}`);
    }
  }

  extractMediaMetadata(uploadData, messageType) {
    const metadata = {};

    if (messageType === "video") {
      metadata.duration = Math.floor(uploadData.duration || 0);
      metadata.resolution = `${uploadData.width}x${uploadData.height}`;
    } else if (messageType === "audio") {
      metadata.duration = Math.floor(uploadData.duration || 0);
    }

    return metadata;
  }

  handleStatusUpdate(statusEvent) {
    const { tempId, status, messageId } = statusEvent;

    if (this.pendingMessages.has(tempId)) {
      switch (status) {
        case "SENT":
          this.replacePendingWithReal(tempId, messageId, statusEvent);
          this.pendingMessages.delete(tempId);
          break;
        case "FAILED":
          this.showError(tempId, statusEvent.error);
          this.pendingMessages.delete(tempId);
          break;
      }
    }
  }
}
```

## Validation Rules

### Required Fields by Type:

- **text**: `tempId`, `type`, `content`
- **file**: `tempId`, `type`, `fileUrl`, `fileName`
- **video**: `tempId`, `type`, `fileUrl`, `fileName` (duration, resolution optional)
- **audio**: `tempId`, `type`, `fileUrl`, `fileName` (duration optional)

### Constraints:

- `tempId`: 1-64 characters
- `fileUrl`: Must start with `https://`
- `fileName`: Max 255 characters
- `fileSize`: Max 100MB (104857600 bytes)
- `content`: Max 5000 characters (text only)
- `duration`: Non-negative integer (seconds)
- `mimeType`: Max 100 characters
- `resolution`: Max 50 characters (format: "widthxheight")

## Error Handling

### Common Errors:

```json
// Missing required field
{
  "statusCode": 400,
  "message": "FileUrl is required for video messages. Upload the file first via /api/upload/* endpoints"
}

// Invalid file URL
{
  "statusCode": 400,
  "message": "File URL must start with https://"
}

// User not enrolled
{
  "statusCode": 403,
  "message": "User not enrolled in course"
}
```

## Best Practices

1. **Always upload files first** via `/api/upload/*` endpoints
2. **Use secure URLs** from upload response (`secureUrl` preferred over `url`)
3. **Include metadata** (duration, resolution) for better UX
4. **Handle WebSocket reconnection** to avoid missing status updates
5. **Show pending states** in UI while processing
6. **Cache upload results** to avoid re-uploading same files
7. **Validate file types** on frontend before upload
8. **Show upload progress** during file upload phase
