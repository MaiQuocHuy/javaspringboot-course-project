# Chat Message API Documentation

## Overview

API này hỗ trợ gửi tin nhắn bất đồng bộ với các loại media thông qua pattern **pre-upload**. Frontend phải upload file trước, sau đó gửi message với URL đã upload.

## Endpoints

### 1. Send Async Message

```http
POST /api/chat/{courseId}/messages
Content-Type: application/json
Authorization: Bearer <token>
```

**Path Parameters:**

- `courseId` (string, required): UUID của course

**Request Body Schema:**

#### Shared Fields (All Message Types)

- `tempId` (string, required): ID tạm để track message (1-64 chars)
- `type` (string, required): Loại message: `"text"`, `"file"`, `"video"`, `"audio"`

#### Text Message Fields

- `content` (string, required): Nội dung text (max 5000 chars)

#### Media Message Fields (file/video/audio)

- `fileUrl` (string, required): URL file đã upload qua `/api/upload/*` (must start with `https://`)
- `fileName` (string, required): Tên file (max 255 chars)
- `fileSize` (number, optional): Kích thước file bytes (max 100MB)
- `mimeType` (string, optional): MIME type (max 100 chars)

#### Video/Audio Specific Fields

- `duration` (number, optional): Thời lượng giây (≥ 0)
- `thumbnailUrl` (string, optional): URL thumbnail (video only)
- `resolution` (string, optional): Độ phân giải "widthxheight" (video only, max 50 chars)

### Response

**Success Response (202 Accepted):**

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

**Error Responses:**

```json
// 400 Bad Request - Validation error
{
  "statusCode": 400,
  "message": "FileUrl is required for video messages. Upload the file first via /api/upload/* endpoints"
}

// 403 Forbidden - Not enrolled
{
  "statusCode": 403,
  "message": "User not enrolled in course"
}

// 404 Not Found - Course not exists
{
  "statusCode": 404,
  "message": "Course not found"
}
```

## Usage Examples

### 1. Text Message

```json
{
  "tempId": "temp-txt-123",
  "type": "text",
  "content": "Hello everyone!"
}
```

### 2. File Message

```json
{
  "tempId": "temp-file-456",
  "type": "file",
  "fileUrl": "https://res.cloudinary.com/example/raw/upload/v1693838234/documents/presentation.pdf",
  "fileName": "presentation.pdf",
  "fileSize": 2048000,
  "mimeType": "application/pdf"
}
```

### 3. Video Message

```json
{
  "tempId": "temp-video-789",
  "type": "video",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1693838234/videos/lecture.mp4",
  "fileName": "lecture.mp4",
  "fileSize": 52428800,
  "duration": 1800,
  "mimeType": "video/mp4",
  "resolution": "1920x1080",
  "thumbnailUrl": "https://res.cloudinary.com/example/image/upload/v1693838234/thumbnails/lecture_thumb.jpg"
}
```

### 4. Audio Message

```json
{
  "tempId": "temp-audio-101",
  "type": "audio",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1693838234/audio/recording.mp3",
  "fileName": "recording.mp3",
  "fileSize": 10485760,
  "duration": 600,
  "mimeType": "audio/mpeg"
}
```

## WebSocket Status Updates

Subscribe to receive real-time status updates:

```javascript
// WebSocket endpoint
const socket = new SockJS("/ws");
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // Subscribe to status updates for specific course
  stompClient.subscribe(
    `/topic/courses/${courseId}/messages/status`,
    (message) => {
      const statusEvent = JSON.parse(message.body);
      handleStatusUpdate(statusEvent);
    }
  );

  // Subscribe to new messages in the course
  stompClient.subscribe(`/topic/courses/${courseId}/messages`, (message) => {
    const newMessage = JSON.parse(message.body);
    addMessageToChat(newMessage);
  });
});
```

### Status Event Schema

```json
{
  "tempId": "temp-msg-12345",
  "status": "SENT",
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1693838234/videos/lecture.mp4",
  "type": "video",
  "thumbnailUrl": "https://res.cloudinary.com/example/image/upload/v1693838234/thumbnails/lecture_thumb.jpg",
  "progress": null,
  "error": null
}
```

### Status Flow

1. **PENDING**: Message created in database
2. **UPLOADING**: Processing media (for consistency, files already uploaded)
3. **SENT**: Message completed, visible in chat
4. **FAILED**: Error occurred during processing

## Pre-Upload File Workflow

Before sending media messages, upload files via these endpoints:

### Upload Endpoints

```http
POST /api/upload/image     # For images
POST /api/upload/video     # For videos
POST /api/upload/audio     # For audio files (NEW)
POST /api/upload/document  # For documents/files
```

### Upload Example

```bash
# Upload video file
curl -X POST \
  -H "Authorization: Bearer <token>" \
  -F "file=@lecture.mp4" \
  https://api.example.com/api/upload/video
```

**Upload Response:**

```json
{
  "statusCode": 201,
  "message": "Video uploaded successfully",
  "data": {
    "url": "https://res.cloudinary.com/example/video/upload/v1693838234/videos/abc123.mp4",
    "secureUrl": "https://res.cloudinary.com/example/video/upload/v1693838234/videos/abc123.mp4",
    "publicId": "videos/abc123",
    "format": "mp4",
    "duration": 1800.5,
    "width": 1920,
    "height": 1080,
    "fileSize": 52428800
  }
}
```

**Use upload response in message:**

```json
{
  "tempId": "temp-video-789",
  "type": "video",
  "fileUrl": "https://res.cloudinary.com/example/video/upload/v1693838234/videos/abc123.mp4", // from upload response
  "fileName": "lecture.mp4",
  "fileSize": 52428800, // from upload response
  "duration": 1800, // from upload response (rounded)
  "mimeType": "video/mp4", // from upload response format
  "resolution": "1920x1080" // from upload response width x height
}
```

## Integration Examples

### React/JavaScript Example

```javascript
class ChatMessageSender {
  constructor(courseId, authToken, stompClient) {
    this.courseId = courseId;
    this.authToken = authToken;
    this.stomp = stompClient;
    this.pendingMessages = new Map();
  }

  // Send text message
  async sendTextMessage(content) {
    const tempId = this.generateTempId();

    const messageData = {
      tempId,
      type: "text",
      content,
    };

    return this.sendMessage(messageData);
  }

  // Send media message (video/audio/file)
  async sendMediaMessage(file, messageType) {
    const tempId = this.generateTempId();

    try {
      // 1. Upload file first
      const uploadResponse = await this.uploadFile(file, messageType);

      // 2. Extract metadata from upload response
      const metadata = this.extractMetadata(uploadResponse.data, messageType);

      // 3. Send message with file URL
      const messageData = {
        tempId,
        type: messageType,
        fileUrl: uploadResponse.data.secureUrl || uploadResponse.data.url,
        fileName: file.name,
        fileSize: file.size,
        mimeType: file.type,
        ...metadata,
      };

      return this.sendMessage(messageData);
    } catch (error) {
      console.error("Failed to send media message:", error);
      throw error;
    }
  }

  async uploadFile(file, messageType) {
    const formData = new FormData();
    formData.append("file", file);

    const endpoint = this.getUploadEndpoint(messageType);

    const response = await fetch(endpoint, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${this.authToken}`,
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`Upload failed: ${response.statusText}`);
    }

    return response.json();
  }

  async sendMessage(messageData) {
    const response = await fetch(`/api/chat/${this.courseId}/messages`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.authToken}`,
      },
      body: JSON.stringify(messageData),
    });

    if (response.ok) {
      this.pendingMessages.set(messageData.tempId, messageData);
      this.showPendingMessage(messageData);
      return await response.json();
    } else {
      throw new Error(`Send failed: ${response.statusText}`);
    }
  }

  getUploadEndpoint(messageType) {
    const endpoints = {
      video: "/api/upload/video",
      audio: "/api/upload/audio",
      file: "/api/upload/document",
    };

    return endpoints[messageType] || "/api/upload/document";
  }

  extractMetadata(uploadData, messageType) {
    const metadata = {};

    if (messageType === "video") {
      metadata.duration = Math.floor(uploadData.duration || 0);
      metadata.resolution =
        uploadData.width && uploadData.height
          ? `${uploadData.width}x${uploadData.height}`
          : undefined;
    } else if (messageType === "audio") {
      metadata.duration = Math.floor(uploadData.duration || 0);
    }

    return metadata;
  }

  generateTempId() {
    return `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  // Handle WebSocket status updates
  handleStatusUpdate(statusEvent) {
    const { tempId, status, messageId, error } = statusEvent;

    if (this.pendingMessages.has(tempId)) {
      switch (status) {
        case "UPLOADING":
          this.updatePendingStatus(tempId, "Uploading...");
          break;
        case "SENT":
          this.replacePendingWithReal(tempId, messageId, statusEvent);
          this.pendingMessages.delete(tempId);
          break;
        case "FAILED":
          this.showError(tempId, error);
          this.pendingMessages.delete(tempId);
          break;
      }
    }
  }

  showPendingMessage(messageData) {
    // Add pending message to UI with loading state
    console.log("Showing pending message:", messageData);
  }

  updatePendingStatus(tempId, status) {
    // Update pending message status in UI
    console.log(`Update ${tempId} status:`, status);
  }

  replacePendingWithReal(tempId, messageId, fullMessage) {
    // Replace pending message with real message from server
    console.log(`Replace ${tempId} with ${messageId}:`, fullMessage);
  }

  showError(tempId, error) {
    // Show error for failed message
    console.error(`Message ${tempId} failed:`, error);
  }
}

// Usage
const messageSender = new ChatMessageSender(courseId, authToken, stompClient);

// Send text
messageSender.sendTextMessage("Hello everyone!");

// Send video
const videoFile = document.querySelector("#video-input").files[0];
messageSender.sendMediaMessage(videoFile, "video");

// Handle status updates
stompClient.subscribe(
  `/topic/courses/${courseId}/messages/status`,
  (message) => {
    const statusEvent = JSON.parse(message.body);
    messageSender.handleStatusUpdate(statusEvent);
  }
);
```

## Validation Rules Summary

| Field          | Type   | Required        | Constraints                             |
| -------------- | ------ | --------------- | --------------------------------------- |
| `tempId`       | string | ✅              | 1-64 characters                         |
| `type`         | string | ✅              | "text", "file", "video", "audio"        |
| `content`      | string | ✅ (text only)  | max 5000 characters                     |
| `fileUrl`      | string | ✅ (media only) | must start with "https://"              |
| `fileName`     | string | ✅ (media only) | max 255 characters                      |
| `fileSize`     | number | ❌              | max 104857600 (100MB)                   |
| `duration`     | number | ❌              | ≥ 0 (video/audio only)                  |
| `mimeType`     | string | ❌              | max 100 characters                      |
| `resolution`   | string | ❌              | max 50 characters (video only)          |
| `thumbnailUrl` | string | ❌              | must start with "https://" (video only) |

## Error Handling Best Practices

1. **Always upload files first** before sending media messages
2. **Handle upload failures** gracefully with retry logic
3. **Show appropriate loading states** during upload and processing
4. **Subscribe to WebSocket updates** to track message status
5. **Validate file types and sizes** on frontend before upload
6. **Use secure URLs** from upload responses
7. **Cache upload results** to avoid duplicate uploads
8. **Implement proper error messaging** for users
