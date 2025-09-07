# Common Discount API Documentation

## Overview
The Common Discount API provides endpoints for creating discounts with flexible date requirements based on discount type.

## Base URL
```
/api/common/discounts
```

## Authentication
All endpoints require authentication via Bearer token and appropriate role permissions.

## Endpoints

### 1. Create Common Discount
**Endpoint:** `POST /api/common/discounts/create`  
**Authorization:** `ADMIN` or `MANAGER` roles required

#### Description
Creates a new discount with type-specific date validation rules:
- **GENERAL type**: Requires both `startDate` and `endDate`
- **REFERRAL type**: Allows nullable dates (uses defaults if not provided)

#### Request Body
```json
{
  "code": "WELCOME10",
  "discountPercent": 10.00,
  "description": "Welcome discount for new users",
  "type": "GENERAL",
  "ownerUserId": null,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "usageLimit": 1000,
  "perUserLimit": 1
}
```

#### GENERAL Type Example
```json
{
  "code": "NEWYEAR2024",
  "discountPercent": 25.00,
  "description": "New Year discount for all users",
  "type": "GENERAL",
  "ownerUserId": null,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "usageLimit": 500,
  "perUserLimit": 1
}
```

#### REFERRAL Type Example (with dates)
```json
{
  "code": "REF-USER123",
  "discountPercent": 15.00,
  "description": "Referral discount from user123",
  "type": "REFERRAL",
  "ownerUserId": "user-123-uuid",
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-06-30T23:59:59",
  "usageLimit": 10,
  "perUserLimit": 1
}
```

#### REFERRAL Type Example (without dates - uses defaults)
```json
{
  "code": "REF-USER456",
  "discountPercent": 15.00,
  "description": "Referral discount from user456",
  "type": "REFERRAL",
  "ownerUserId": "user-456-uuid",
  "startDate": null,
  "endDate": null,
  "usageLimit": 10,
  "perUserLimit": 1
}
```
*Note: When `startDate` and `endDate` are `null` for REFERRAL type:*
- `startDate` defaults to current timestamp
- `endDate` defaults to `2099-12-31 23:59:59`

### 2. Create Induction Discount
**Endpoint:** `POST /api/common/discounts/induction`  
**Authorization:** `ADMIN`, `MANAGER`, or `USER` roles required

#### Description
Creates a special induction discount code for the "receive induction code" feature. Must be GENERAL type.

#### Request Body
```json
{
  "code": "INDUCTION2024",
  "discountPercent": 20.00,
  "description": "Special induction discount for new learners",
  "type": "GENERAL",
  "ownerUserId": null,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "usageLimit": null,
  "perUserLimit": 1
}
```

## Response Format

### Success Response (201 Created)
```json
{
  "statusCode": 201,
  "message": "Discount created successfully",
  "data": {
    "id": "discount-uuid-123",
    "code": "WELCOME10",
    "discountPercent": 10.00,
    "description": "Welcome discount for new users",
    "type": "GENERAL",
    "ownerUserId": null,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "usageLimit": 1000,
    "perUserLimit": 1,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00+07:00",
    "updatedAt": "2024-01-01T10:00:00+07:00"
  },
  "timestamp": "2024-01-01T10:00:00.000+07:00"
}
```

### Error Responses

#### 400 Bad Request - Invalid Data
```json
{
  "statusCode": 400,
  "message": "Start date and end date are required for GENERAL discount type",
  "data": null,
  "timestamp": "2024-01-01T10:00:00.000+07:00"
}
```

#### 409 Conflict - Duplicate Code
```json
{
  "statusCode": 409,
  "message": "Discount code already exists",
  "data": null,
  "timestamp": "2024-01-01T10:00:00.000+07:00"
}
```

## Validation Rules

### Common Validations
- `code`: 2-50 characters, uppercase letters, numbers, hyphens, underscores only
- `discountPercent`: 0.01 to 100.00, max 2 decimal places
- `description`: max 255 characters (optional)
- `type`: must be either "GENERAL" or "REFERRAL"
- `usageLimit`: min 1 if specified (optional)
- `perUserLimit`: min 1 if specified (optional)

### Type-Specific Validations

#### GENERAL Type
- `startDate`: required, cannot be in the past
- `endDate`: required, must be after startDate
- `ownerUserId`: must be null

#### REFERRAL Type  
- `startDate`: optional (defaults to current time if null)
- `endDate`: optional (defaults to 2099-12-31 23:59:59 if null)
- `ownerUserId`: required, must exist in database
- If both dates provided: startDate cannot be in the past, endDate must be after startDate

### Induction Discount Specific
- Must be GENERAL type
- All GENERAL type validations apply

## Business Logic Flow

1. **Request Validation**: Validate input data and business rules
2. **Authentication Check**: Verify user permissions
3. **Type-Specific Validation**: Apply different rules based on discount type
4. **Date Handling**:
   - GENERAL: Validate required dates
   - REFERRAL: Use provided dates or apply defaults
5. **Code Uniqueness**: Check if discount code already exists
6. **Owner Validation**: Verify owner user for REFERRAL type
7. **Entity Creation**: Create and save discount entity
8. **Response**: Return created discount data

## Error Codes

| Code | Description |
|------|-------------|
| 400  | Invalid request data or business rules violation |
| 401  | Authentication required |
| 403  | Insufficient permissions |
| 404  | Owner user not found (REFERRAL type) |
| 409  | Discount code already exists |
| 500  | Internal server error |

## Usage Examples

### cURL Examples

#### Create GENERAL Discount
```bash
curl -X POST "http://localhost:8080/api/common/discounts/create" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SPRING2024",
    "discountPercent": 30.00,
    "description": "Spring promotion discount",
    "type": "GENERAL",
    "ownerUserId": null,
    "startDate": "2024-03-01T00:00:00",
    "endDate": "2024-03-31T23:59:59",
    "usageLimit": 200,
    "perUserLimit": 1
  }'
```

#### Create REFERRAL Discount with Auto Dates
```bash
curl -X POST "http://localhost:8080/api/common/discounts/create" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "REF-FRIEND",
    "discountPercent": 20.00,
    "description": "Friend referral discount",
    "type": "REFERRAL",
    "ownerUserId": "user-uuid-789",
    "startDate": null,
    "endDate": null,
    "usageLimit": 5,
    "perUserLimit": 1
  }'
```

#### Create Induction Discount
```bash
curl -X POST "http://localhost:8080/api/common/discounts/induction" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WELCOME2024",
    "discountPercent": 15.00,
    "description": "Welcome bonus for new students",
    "type": "GENERAL",
    "ownerUserId": null,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "usageLimit": null,
    "perUserLimit": 1
  }'
```
