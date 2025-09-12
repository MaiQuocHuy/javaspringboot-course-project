# Discount Email API Documentation

## Endpoint: POST /api/discounts/email

### Description

Sends discount code emails to all students in the system. Backend automatically fetches discount details (code, start date, end date) from database using discount ID. Only users with ADMIN role can access this endpoint.

### Authorization

- **Required Role**: ADMIN
- **Authentication**: Bearer JWT Token

### Request Body

```json
{
  "discount_id": "d1a2b3c4-e5f6-7890-abcd-ef1234567890",
  "subject": "Special Discount Just for You!"
}
```

### Request Fields

| Field         | Type   | Required | Description                         | Example                                | Validation                |
| ------------- | ------ | -------- | ----------------------------------- | -------------------------------------- | ------------------------- |
| `discount_id` | String | Yes      | ID of existing discount in database | "d1a2b3c4-e5f6-7890-abcd-ef1234567890" | Must be valid discount ID |
| `subject`     | String | Yes      | Email subject line                  | "Special Discount!"                    | 5-200 characters          |

### Response (Success - 200)

```json
{
  "success": true,
  "message": "Successfully sent discount emails to 7 students",
  "data": {
    "estimatedRecipients": 7,
    "discountCode": "",
    "subject": "Special Discount Just for You!"
  }
}
```

### Response Fields

| Field                 | Type   | Description                                 |
| --------------------- | ------ | ------------------------------------------- |
| `estimatedRecipients` | Long   | Number of students who received the email   |
| `discountCode`        | String | Will be empty as code is fetched internally |
| `subject`             | String | Email subject that was used                 |

### Error Responses

#### 400 Bad Request

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": [
    {
      "field": "discount_code",
      "message": "Discount code must be between 3 and 50 characters"
    }
  ]
}
```

#### 401 Unauthorized

```json
{
  "success": false,
  "message": "Unauthorized - Invalid or missing JWT token",
  "data": null
}
```

#### 403 Forbidden

```json
{
  "success": false,
  "message": "Forbidden - User does not have ADMIN role",
  "data": null
}
```

#### 500 Internal Server Error

```json
{
  "success": false,
  "message": "Failed to send discount emails: Email service temporarily unavailable",
  "data": null
}
```

### Email Template Features

The discount emails sent to students include:

1. **Professional Design**: Modern gradient backgrounds and styling
2. **Personalization**: Student name personalization
3. **Clear Discount Code**: Highlighted discount code with copy-paste friendly format
4. **Validity Period**: Clear start and end dates
5. **Usage Instructions**: Step-by-step guide on how to use the discount
6. **Responsive Design**: Mobile and desktop friendly
7. **Emoji Enhancement**: Professional emojis for visual appeal
8. **Company Branding**: KTC Education branding and contact information

### Usage Examples

#### cURL Command

```bash
curl -X POST "http://localhost:8080/api/discounts/email" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "discount_id": "d1a2b3c4-e5f6-7890-abcd-ef1234567890",
    "subject": "🎉 New Year Special - 20% Off All Courses!"
  }'
```

#### JavaScript/Fetch

````javascript
```javascript
const response = await fetch('/api/discounts/email', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    discount_id: 'd1a2b3c4-e5f6-7890-abcd-ef1234567890',
    subject: '☀️ Summer Learning Sale - Save Big!'
  })
});

const result = await response.json();
console.log(`Sent to ${result.data.estimatedRecipients} students`);
````

### Business Logic

1. **Discount Lookup**: Fetches discount details from database using provided ID
2. **Student Query**: Finds all users with role "STUDENT" and active status
3. **Email Generation**: Creates personalized emails using the discount-code-template with fetched discount data
4. **Bulk Sending**: Sends emails asynchronously with small delays to prevent provider overload
5. **Progress Tracking**: Counts successful sends and provides detailed logging
6. **Error Handling**: Continues sending even if some emails fail

### Technical Notes

- Discount details (code, start date, end date) are automatically fetched from database
- Emails are sent asynchronously to improve performance
- 100ms delay between emails to respect provider rate limits
- Template uses Thymeleaf for variable substitution
- Supports email provider fallback for reliability
- Comprehensive logging for monitoring and debugging

### Advantages of New Design

1. **Data Consistency**: Ensures discount information is always current from database
2. **Simplified Frontend**: Frontend only needs to know discount ID and subject
3. **Reduced Errors**: Eliminates manual entry of discount code and dates
4. **Better Maintenance**: Changes to discount details don't require API updates

### Rate Limiting

- No explicit rate limiting on the API endpoint
- Email sending includes built-in delays to respect provider limits
- Recommended usage: Not more than once per day for promotional emails

### Monitoring

The API provides detailed logging for:

- Number of students found
- Email sending progress
- Success/failure counts
- Individual email send results
- Performance metrics

### Related Endpoints

- `GET /api/discounts` - List all discounts
- `POST /api/discounts` - Create new discount
- `GET /api/discounts/validate` - Validate discount for use
