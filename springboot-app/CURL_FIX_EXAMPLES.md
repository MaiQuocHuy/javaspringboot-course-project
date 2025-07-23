# Fixed Multipart Profile Update API

## ✅ **SOLUTION FOR YOUR cURL ERROR**

The issue with your cURL command was that Spring Boot couldn't parse the JSON part correctly. Here's the fix:

### ❌ **Your Original Command (that failed):**

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJTVFVERU5UIl0sInN1YiI6InN0cmluZ2hlaGVoMTIzMTIzQGdtYWlsLmNvbSIsImlhdCI6MTc1MzIwMDk4NCwiZXhwIjoxNzUzMjA0NTg0fQ.Hk6YM8N1sPY3S0JVI56jPZHWdFPut6cNfk7KN2THsvDydKry9BPYvwULqF3hsHHpB4gwKCGOIww3weDe1rABdg' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={"name":"John Doe","bio":"Software developer with 5 years of experience"}' \
  -F 'thumbnail='
```

### ✅ **FIXED Command (use this):**

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJTVFVERU5UIl0sInN1YiI6InN0cmluZ2hlaGVoMTIzMTIzQGdtYWlsLmNvbSIsImlhdCI6MTc1MzIwMDk4NCwiZXhwIjoxNzUzMjA0NTg0fQ.Hk6YM8N1sPY3S0JVI56jPZHWdFPut6cNfk7KN2THsvDydKry9BPYvwULqF3hsHHpB4gwKCGOIww3weDe1rABdg' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={"name":"John Doe","bio":"Software developer with 5 years of experience"};type=application/json'
```

## 🔧 **What Changed:**

1. **Controller Method**: Now uses `@RequestParam` instead of `@RequestPart`
2. **JSON Parsing**: Added JSON string parsing in the service layer
3. **Content-Type**: Specify `;type=application/json` for the user part

## 📋 **More Examples:**

### With Image Upload:

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={"name":"John Doe","bio":"Software developer"};type=application/json' \
  -F 'thumbnail=@profile.jpg;type=image/jpeg'
```

### Without Image (your case):

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={"name":"John Doe","bio":"Software developer"};type=application/json'
```

### Minimal Update:

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={"name":"John Doe"};type=application/json'
```

## 🎯 **Expected Response:**

```json
{
  "statusCode": 200,
  "message": "Profile updated successfully",
  "data": {
    "id": "user-123",
    "email": "stringheheh123123@gmail.com",
    "name": "John Doe",
    "bio": "Software developer with 5 years of experience",
    "thumbnailUrl": null,
    "thumbnailId": null,
    "roles": ["STUDENT"]
  },
  "timestamp": "2025-07-22T23:30:00.000+07:00"
}
```

## 💡 **Why the Original Failed:**

- **Error**: `Content-Type 'application/octet-stream' is not supported`
- **Cause**: Spring Boot couldn't parse the JSON part without explicit content type
- **Solution**: Added `;type=application/json` to the form field and changed to `@RequestParam`

## 🧪 **Testing in Swagger UI:**

1. Go to `http://localhost:8080/swagger-ui.html`
2. Find `PUT /api/users/profile`
3. Use these form fields:
   - **user**: `{"name":"John Doe","bio":"Software developer"}`
   - **thumbnail**: (upload file or leave empty)

Try the fixed command now!
