# Quiz API Test Documentation

## POST /api/instructor/quizzes

### Description

Creates a new quiz for a lesson owned by the instructor.

### Prerequisites

- User must have INSTRUCTOR role
- Must own the lesson (through course ownership)
- Lesson must be of type QUIZ (type-002)
- Lesson should not already have quiz questions

### Request Example

```bash
curl -X POST http://localhost:8080/api/instructor/quizzes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "title": "Spring Boot Fundamentals Quiz",
    "description": "Test your knowledge of Spring Boot basics",
    "lessonId": "lesson-uuid-here",
    "questions": [
      {
        "questionText": "What is Spring Boot?",
        "options": {
          "A": "A Java framework for building web applications",
          "B": "A database management system",
          "C": "A programming language",
          "D": "An IDE for Java development"
        },
        "correctAnswer": "A",
        "explanation": "Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring based applications."
      },
      {
        "questionText": "Which annotation is used to mark a class as a Spring Boot main class?",
        "options": {
          "A": "@SpringBootMain",
          "B": "@SpringBootApplication",
          "C": "@EnableSpringBoot",
          "D": "@MainApplication"
        },
        "correctAnswer": "B",
        "explanation": "@SpringBootApplication is the main annotation that combines @Configuration, @EnableAutoConfiguration, and @ComponentScan."
      }
    ]
  }'
```

### Success Response (201 Created)

```json
{
  "statusCode": 201,
  "message": "Quiz created successfully",
  "data": {
    "id": "lesson-uuid-here",
    "title": "Spring Boot Fundamentals Quiz",
    "description": "Test your knowledge of Spring Boot basics",
    "courseId": "course-uuid-here",
    "lessonId": "lesson-uuid-here",
    "questions": [
      {
        "id": "question-uuid-1",
        "questionText": "What is Spring Boot?",
        "options": {
          "A": "A Java framework for building web applications",
          "B": "A database management system",
          "C": "A programming language",
          "D": "An IDE for Java development"
        },
        "correctAnswer": "A",
        "explanation": "Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring based applications."
      },
      {
        "id": "question-uuid-2",
        "questionText": "Which annotation is used to mark a class as a Spring Boot main class?",
        "options": {
          "A": "@SpringBootMain",
          "B": "@SpringBootApplication",
          "C": "@EnableSpringBoot",
          "D": "@MainApplication"
        },
        "correctAnswer": "B",
        "explanation": "@SpringBootApplication is the main annotation that combines @Configuration, @EnableAutoConfiguration, and @ComponentScan."
      }
    ],
    "createdAt": "2025-08-04T10:30:00Z"
  },
  "timestamp": "2025-08-04T10:30:00Z"
}
```

### Error Responses

#### 400 Bad Request - Validation Error

```json
{
  "statusCode": 400,
  "message": "Validation Error",
  "error": "Correct answer 'E' must be one of the provided options",
  "timestamp": "2025-08-04T10:30:00Z",
  "path": "/api/instructor/quizzes"
}
```

#### 403 Forbidden - Not Lesson Owner

```json
{
  "statusCode": 403,
  "message": "Validation Error",
  "error": "You are not authorized to create quiz for this lesson",
  "timestamp": "2025-08-04T10:30:00Z",
  "path": "/api/instructor/quizzes"
}
```

#### 404 Not Found - Lesson Not Found

```json
{
  "statusCode": 404,
  "message": "Not Found",
  "error": "Lesson not found with ID: lesson-uuid",
  "timestamp": "2025-08-04T10:30:00Z",
  "path": "/api/instructor/quizzes"
}
```

#### 409 Conflict - Quiz Already Exists

```json
{
  "statusCode": 409,
  "message": "Validation Error",
  "error": "Lesson already has quiz questions. Use update endpoint to modify existing quiz.",
  "timestamp": "2025-08-04T10:30:00Z",
  "path": "/api/instructor/quizzes"
}
```

### Business Rules Implemented

1. ✅ **Instructor Authorization**: Only users with INSTRUCTOR role can create quizzes
2. ✅ **Lesson Ownership**: Instructors can only create quizzes for lessons in their own courses
3. ✅ **Lesson Type Validation**: Can only create quizzes for lessons of type QUIZ (type-002)
4. ✅ **Question Validation**: Each question must have valid options and correct answer
5. ✅ **Duplicate Prevention**: Prevents creating multiple quizzes for the same lesson
6. ✅ **Data Validation**: Comprehensive validation on all input fields

### Database Impact

- Creates records in `quiz_questions` table
- Each question is linked to the lesson via `lesson_id`
- Options are stored as JSON in the `options` column
- Maintains referential integrity with lesson and course relationships

### Security

- ✅ JWT authentication required
- ✅ INSTRUCTOR role validation
- ✅ Course ownership verification through lesson relationship
- ✅ Input sanitization and validation

### Performance Considerations

- Uses batch insert for multiple questions
- Leverages database indexes on lesson_id for efficient queries
- Transaction management ensures data consistency
