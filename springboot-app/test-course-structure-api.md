## Test the new Course Structure API endpoint

### Endpoint Details

- **Method**: GET
- **URL**: `/api/student/courses/{courseId}/structure`
- **Description**: Get course structure without completion status for enrolled students

### Sample Request

```bash
curl -X GET \
  'http://localhost:8080/api/student/courses/{courseId}/structure' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json'
```

### Expected Response Format

```json
{
  "message": "Course structure fetched successfully",
  "data": [
    {
      "id": "section-001",
      "title": "Introduction to Programming",
      "description": "Basic concepts of programming",
      "order": 0,
      "lessonCount": 2,
      "lessons": [
        {
          "id": "lesson-001",
          "title": "Variables and Data Types",
          "type": "VIDEO",
          "order": 0,
          "video": {
            "id": "video-001",
            "url": "https://res.cloudinary.com/.../video.mp4",
            "duration": 1800,
            "title": "Variables and Data Types",
            "thumbnail": "https://res.cloudinary.com/.../thumbnail.jpg"
          }
        },
        {
          "id": "lesson-002",
          "title": "Programming Quiz",
          "type": "QUIZ",
          "order": 1,
          "quiz": {
            "questions": [
              {
                "id": "question-001",
                "questionText": "What is a variable?",
                "options": {
                  "A": "A storage location",
                  "B": "A function",
                  "C": "A loop",
                  "D": "A condition"
                },
                "correctAnswer": "A",
                "explanation": "A variable is a storage location with an associated name."
              }
            ]
          }
        }
      ]
    }
  ]
}
```

### Key Differences from getCourseSections:

1. **Property Names**: Uses `order` instead of `orderIndex`
2. **No Completion Status**: Excludes `isCompleted` and `completedAt` fields
3. **Simplified Structure**: Focus on course structure without progress tracking

### Error Responses

- **401**: Unauthorized (invalid token)
- **403**: Forbidden (not enrolled in course)
- **404**: Course not found or user not enrolled

### Testing Checklist

- [ ] Verify enrollment check works
- [ ] Confirm sections are returned in correct order
- [ ] Verify lessons are returned in correct order
- [ ] Check VIDEO type lessons include video content
- [ ] Check QUIZ type lessons include quiz questions
- [ ] Verify no completion status fields are included
- [ ] Test with non-enrolled user (should return 404)
- [ ] Test with invalid course ID (should return 404)
