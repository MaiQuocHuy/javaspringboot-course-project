# Chat Module Documentation

## Overview

The Chat Module provides real-time messaging functionality for course participants. It supports text messages, file sharing, audio messages, and video messages within course contexts.

## API Endpoints

### Base URL: `/api/chat`

#### Send Message

- **POST** `/{courseId}/messages`
- **Description**: Send a message to a course chat
- **Authentication**: Required (JWT Bearer token)
- **Authorization**: User must be enrolled in the course or be the instructor

**Request Body:**

```json
{
  "type": "TEXT",
  "content": "Hello everyone! How are you doing with the course?",
  "fileName": null,
  "fileSize": null,
  "duration": null,
  "thumbnailUrl": null
}
```

**Response:**

```json
{
  "statusCode": 201,
  "message": "Message sent",
  "data": {
    "id": "7200a420-2ff3-4f18-9933-1b86d05f1a78",
    "courseId": "course-uuid-123",
    "senderId": "user-uuid-456",
    "senderName": "John Doe",
    "senderRole": "STUDENT",
    "type": "TEXT",
    "textContent": "Hello everyone! How are you doing with the course?",
    "createdAt": "2025-08-26T10:30:00"
  },
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

#### Get Messages

- **GET** `/{courseId}/messages`
- **Description**: Retrieve paginated list of messages for a course
- **Authentication**: Required (JWT Bearer token)
- **Authorization**: User must be enrolled in the course or be the instructor

**Query Parameters:**

- `type` (optional): Filter by message type (TEXT, FILE, AUDIO, VIDEO)
- `page` (optional, default: 0): Page number (0-based)
- `size` (optional, default: 20): Number of items per page (1-100)

**Response:**

```json
{
  "statusCode": 200,
  "message": "Messages retrieved",
  "data": {
    "content": [
      {
        "id": "7200a420-2ff3-4f18-9933-1b86d05f1a78",
        "courseId": "course-uuid-123",
        "senderId": "user-uuid-456",
        "senderName": "John Doe",
        "senderRole": "STUDENT",
        "type": "TEXT",
        "textContent": "Hello everyone!",
        "createdAt": "2025-08-26T10:30:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  },
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

## WebSocket Integration

### Connection

- **Endpoint**: `/ws-chat`
- **Protocol**: STOMP over WebSocket with SockJS fallback
- **Authentication**: JWT token required

### Subscription

- **Topic**: `/topic/course/{courseId}/messages`
- **Description**: Real-time message updates for a specific course

### Example Client Implementation (JavaScript)

```javascript
// Connect to WebSocket
const socket = new SockJS("/ws-chat");
const stompClient = Stomp.over(socket);

// Connect with JWT token
stompClient.connect(
  {
    Authorization: "Bearer " + jwtToken,
  },
  function (frame) {
    console.log("Connected: " + frame);

    // Subscribe to course messages
    stompClient.subscribe(
      "/topic/course/" + courseId + "/messages",
      function (message) {
        const chatMessage = JSON.parse(message.body);
        displayMessage(chatMessage);
      }
    );
  }
);

// Send message via REST API
async function sendMessage(courseId, messageData) {
  const response = await fetch(`/api/chat/${courseId}/messages`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + jwtToken,
    },
    body: JSON.stringify(messageData),
  });

  return response.json();
}
```

## Message Types

### TEXT Messages

```json
{
  "type": "TEXT",
  "content": "Your text message here"
}
```

### FILE Messages

```json
{
  "type": "FILE",
  "content": "https://example.com/files/document.pdf",
  "fileName": "lecture-notes.pdf",
  "fileSize": 1024000
}
```

### AUDIO Messages

```json
{
  "type": "AUDIO",
  "content": "https://example.com/audio/recording.mp3",
  "fileName": "question.mp3",
  "fileSize": 512000,
  "duration": 180
}
```

### VIDEO Messages

```json
{
  "type": "VIDEO",
  "content": "https://example.com/videos/explanation.mp4",
  "fileName": "explanation.mp4",
  "fileSize": 5120000,
  "duration": 300,
  "thumbnailUrl": "https://example.com/thumbnails/video-thumb.jpg"
}
```

## Error Responses

### 400 Bad Request

```json
{
  "statusCode": 400,
  "message": "Invalid request data",
  "data": null,
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

### 401 Unauthorized

```json
{
  "statusCode": 401,
  "message": "User not authenticated",
  "data": null,
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

### 403 Forbidden

```json
{
  "statusCode": 403,
  "message": "User not enrolled in course or insufficient permissions",
  "data": null,
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

### 404 Not Found

```json
{
  "statusCode": 404,
  "message": "Course not found",
  "data": null,
  "timestamp": "2025-08-26T10:30:05.123+07:00"
}
```

## Security

- All endpoints require JWT authentication
- Users must be enrolled in the course or be the instructor to access chat
- Message content is validated before storage
- Real-time WebSocket connections are secured with JWT tokens

## Database Schema

The chat system uses a polymorphic design:

- `chat_message`: Main message table with common fields
- `chat_message_text`: Text-specific fields
- `chat_message_file`: File-specific fields
- `chat_message_audio`: Audio-specific fields
- `chat_message_video`: Video-specific fields
- `chat_message_type`: Message type lookup table

## Testing

Run the chat module tests:

```bash
./mvnw test -Dtest=ChatMessageServiceTest
```

## Integration

The chat module integrates with:

- **Course Management**: Access control based on course enrollment
- **User Management**: Sender information and roles
- **Security**: JWT authentication and authorization
- **WebSocket**: Real-time message broadcasting
