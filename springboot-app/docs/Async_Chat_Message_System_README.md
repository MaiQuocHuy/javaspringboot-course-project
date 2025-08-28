# Async Chat Message System

## 📋 Overview

Hệ thống chat message bất đồng bộ hỗ trợ gửi tin nhắn với media (file, video, audio) thông qua **pre-upload pattern**.

### 🔄 Workflow

1. **Upload File** → Upload file trước qua `/api/upload/*` endpoints
2. **Send Message** → Gửi message với file URL đã upload
3. **Status Updates** → Nhận updates qua WebSocket (PENDING → UPLOADING → SENT/FAILED)

### ✅ Key Features

- **Async Processing**: Non-blocking message sending với immediate acknowledgment
- **Media Support**: Text, File, Video, Audio messages
- **Real-time Updates**: WebSocket status notifications
- **Type Safety**: MessageType enum và validation
- **Pre-upload Pattern**: Tách biệt file upload và message sending
- **Robust Error Handling**: Comprehensive validation và error responses

## 🏗️ Architecture

### Core Components

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend       │    │   Cloudinary    │
│                 │    │                  │    │                 │
│ 1. Upload File  ├────┤ Upload Endpoints ├────┤   File Storage  │
│                 │    │                  │    │                 │
│ 2. Send Message ├────┤ Chat Controller  │    └─────────────────┘
│                 │    │       ↓          │
│ 3. WebSocket    ├────┤ Async Processing │
│   Updates       │    │       ↓          │
│                 │    │ Status Broadcast │
└─────────────────┘    └──────────────────┘
```

### Database Entities

- `ChatMessage`: Core message entity
- `MessageType` enum: TEXT, FILE, VIDEO, AUDIO
- Media detail entities: `FileDetail`, `VideoDetail`, `AudioDetail`

### Service Layer

- `ChatMessageServiceImp`: Async message processing
- `CloudinaryService`: File upload management
- WebSocket: Real-time status broadcasting

## 📚 Documentation

| Document                                                 | Description                       |
| -------------------------------------------------------- | --------------------------------- |
| [API Documentation](./Chat_Message_API_Documentation.md) | Complete API guide with examples  |
| [Usage Guide](./AsyncMessageAPI_Usage_Guide.md)          | Frontend integration guide        |
| [OpenAPI Spec](./chat-message-api-openapi.yaml)          | Swagger/OpenAPI 3.0 specification |

## 🚀 Quick Start

### 1. Text Message

```javascript
const response = await fetch(`/api/chat/${courseId}/messages`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({
    tempId: "temp-txt-123",
    type: "text",
    content: "Hello everyone!",
  }),
});
```

### 2. Video Message (2-step process)

```javascript
// Step 1: Upload video
const formData = new FormData();
formData.append("file", videoFile);

const uploadResponse = await fetch("/api/upload/video", {
  method: "POST",
  body: formData,
});

const uploadData = await uploadResponse.json();

// Step 2: Send message with video URL
const messageResponse = await fetch(`/api/chat/${courseId}/messages`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({
    tempId: "temp-video-456",
    type: "video",
    fileUrl: uploadData.data.secureUrl,
    fileName: videoFile.name,
    fileSize: videoFile.size,
    duration: Math.floor(uploadData.data.duration),
    mimeType: videoFile.type,
    resolution: `${uploadData.data.width}x${uploadData.data.height}`,
  }),
});
```

### 3. WebSocket Status Tracking

```javascript
const socket = new SockJS("/ws");
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe(
    `/topic/courses/${courseId}/messages/status`,
    (message) => {
      const status = JSON.parse(message.body);

      switch (status.status) {
        case "PENDING":
          showPending(status.tempId);
          break;
        case "UPLOADING":
          showUploading(status.tempId);
          break;
        case "SENT":
          replacePendingWithMessage(status.tempId, status.messageId);
          break;
        case "FAILED":
          showError(status.tempId, status.error);
          break;
      }
    }
  );
});
```

## 🔧 API Endpoints

### Chat Messages

- `POST /api/chat/{courseId}/messages` - Send async message

### File Upload (Pre-requisite for media messages)

- `POST /api/upload/image` - Upload images
- `POST /api/upload/video` - Upload videos
- `POST /api/upload/audio` - Upload audio files
- `POST /api/upload/document` - Upload documents/files

### WebSocket

- `/topic/courses/{courseId}/messages/status` - Status updates
- `/topic/courses/{courseId}/messages` - New messages

## 📝 Message Types & Requirements

### Text Message

```json
{
  "tempId": "required",
  "type": "text",
  "content": "required"
}
```

### File Message

```json
{
  "tempId": "required",
  "type": "file",
  "fileUrl": "required (from upload)",
  "fileName": "required",
  "fileSize": "optional",
  "mimeType": "optional"
}
```

### Video Message

```json
{
  "tempId": "required",
  "type": "video",
  "fileUrl": "required (from upload)",
  "fileName": "required",
  "fileSize": "optional",
  "duration": "optional",
  "mimeType": "optional",
  "resolution": "optional",
  "thumbnailUrl": "optional"
}
```

### Audio Message

```json
{
  "tempId": "required",
  "type": "audio",
  "fileUrl": "required (from upload)",
  "fileName": "required",
  "fileSize": "optional",
  "duration": "optional",
  "mimeType": "optional"
}
```

## ⚠️ Important Notes

### Pre-Upload Pattern

- **MUST upload files first** via `/api/upload/*` endpoints
- **Cannot send MultipartFile** directly in message request
- **Use fileUrl from upload response** in message payload

### Validation Rules

- `tempId`: 1-64 characters, unique per request
- `fileUrl`: Must start with `https://`
- `fileName`: Required for media messages, max 255 chars
- `fileSize`: Max 100MB (104857600 bytes)
- `content`: Required for text, max 5000 chars
- `duration`: Non-negative integer (seconds)

### Error Handling

- **400**: Validation errors (missing fields, invalid formats)
- **401**: Authentication required
- **403**: User not enrolled in course
- **404**: Course not found
- **500**: Server errors during processing

## 🔍 Code Changes Summary

### Modified Files

1. **AsyncSendMessageRequest.java** - Removed `MultipartFile`, only `String fileUrl`
2. **AsyncMessageRequestValidator.java** - Updated validation for pre-upload pattern
3. **ChatMessageServiceImp.java** - Removed upload logic, use provided URLs
4. **ChatMessageController.java** - Unchanged (still uses `@RequestBody`)

### Key Improvements

- ✅ **Consistent Pattern**: Aligns with existing `/api/upload/*` endpoints
- ✅ **Type Safety**: MessageType enum instead of strings
- ✅ **Clean Architecture**: Separation of upload and messaging concerns
- ✅ **Robust Validation**: Comprehensive field validation
- ✅ **Real-time Updates**: WebSocket status broadcasting
- ✅ **Error Handling**: Detailed error messages and status codes

## 🧪 Testing

### Unit Tests

```bash
./mvnw test -Dtest=ChatMessageServiceTest
./mvnw test -Dtest=AsyncMessageRequestValidatorTest
```

### Integration Tests

```bash
./mvnw test -Dtest=ChatMessageControllerTest
```

### Manual Testing

1. Test text message sending
2. Test file upload → message sending workflow
3. Test WebSocket status updates
4. Test validation errors
5. Test unauthorized access

## 🚀 Deployment

### Build

```bash
./mvnw clean compile
./mvnw clean package
```

### Run

```bash
./mvnw spring-boot:run
# or
java -jar target/springboot-app-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
./build-docker.ps1
docker-compose up -d
```

## 📊 Performance Considerations

- **Async Processing**: Non-blocking message handling
- **File Upload Optimization**: Direct Cloudinary upload bypassing server
- **WebSocket Efficiency**: Targeted status updates per course
- **Database Indexing**: Optimized queries for message retrieval
- **Caching**: Consider Redis for frequently accessed data

## 🔒 Security

- **Authentication**: JWT bearer token required
- **Authorization**: Course enrollment verification
- **File Validation**: Size, type, and format checks
- **HTTPS**: All file URLs must use secure protocol
- **CORS**: Configured for frontend domains
- **Rate Limiting**: Consider implementing for upload endpoints

## 🐛 Troubleshooting

### Common Issues

1. **"FileUrl is required"** - Upload file first via `/api/upload/*`
2. **"User not enrolled"** - Check course enrollment status
3. **"File URL must start with https"** - Use secure URLs from upload response
4. **WebSocket not receiving updates** - Check subscription path and connection
5. **Upload fails** - Verify file size (<100MB) and supported formats

### Debug Tips

- Check browser network tab for request/response details
- Monitor WebSocket connections in browser developer tools
- Verify JWT token validity and permissions
- Check server logs for detailed error messages
- Test with smaller files if upload fails
