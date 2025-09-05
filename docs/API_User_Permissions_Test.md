# Test User Permission APIs

## 1. Get Current User Permissions

```bash
curl -X GET "http://localhost:8080/api/auth/permissions" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

## 2. Check Specific Permission

```bash
curl -X GET "http://localhost:8080/api/auth/permissions/check?permissionKey=course:READ" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

## 3. Get Current User Role

```bash
curl -X GET "http://localhost:8080/api/auth/role" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

## 4. Update User Role (Admin only)

```bash
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "ROLE_ID_HERE"
  }'
```

## Expected Responses:

### User Permissions Response:

```json
{
  "statusCode": 200,
  "message": "User permissions retrieved successfully",
  "data": {
    "userId": "user-123",
    "email": "user@example.com",
    "name": "John Doe",
    "role": {
      "id": "role-456",
      "name": "STUDENT"
    },
    "permissions": ["course:READ", "enrollment:CREATE", "payment:READ"],
    "detailedPermissions": [
      {
        "permissionKey": "course:READ",
        "description": "Read courses",
        "resource": "Course",
        "action": "READ",
        "filterType": "ALL",
        "canAccessAll": true,
        "canAccessOwn": true
      }
    ]
  }
}
```

### Permission Check Response:

```json
{
  "statusCode": 200,
  "message": "Permission check for 'course:READ' completed",
  "data": true
}
```

### User Role Response:

```json
{
  "statusCode": 200,
  "message": "User role retrieved successfully",
  "data": {
    "id": "role-456",
    "name": "STUDENT"
  }
}
```
