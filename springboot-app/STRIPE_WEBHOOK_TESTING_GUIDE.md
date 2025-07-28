# Stripe Webhook Testing Guide

This guide will help you test Stripe webhooks locally using the Stripe CLI.

## Prerequisites

1. Install Stripe CLI: https://stripe.com/docs/stripe-cli
2. Authenticate with your Stripe account: `stripe login`

## Local Development Setup

### 1. Environment Variables

Add these to your `.env` file:

```bash
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_51xxxxx  # Your test secret key
STRIPE_PUBLISHABLE_KEY=pk_test_51xxxxx  # Your test publishable key
STRIPE_WEBHOOK_SECRET=whsec_xxxxx  # Will be provided by Stripe CLI
FRONTEND_URL=http://localhost:3000
```

### 2. Start Your Spring Boot Application

```bash
./mvnw spring-boot:run
```

Your webhook endpoint will be available at: `http://localhost:8080/api/stripe/webhook`

### 3. Start Stripe CLI Webhook Forwarding

Open a new terminal and run:

```bash
stripe listen --forward-to localhost:8080/api/stripe/webhook
```

This command will:

- Display a webhook endpoint secret (whsec_xxxxx)
- Forward all webhook events to your local endpoint
- Show real-time webhook events in the terminal

Copy the webhook endpoint secret and add it to your `.env` file as `STRIPE_WEBHOOK_SECRET`.

### 4. Test Webhook Events

#### Option A: Create Test Events via Stripe CLI

```bash
# Test checkout session completed
stripe trigger checkout.session.completed

# Test payment intent succeeded
stripe trigger payment_intent.succeeded

# Test payment intent failed
stripe trigger payment_intent.payment_failed
```

#### Option B: Create Real Checkout Session

1. Use your frontend or API client to call:

   ```
   POST http://localhost:8080/api/stripe/create-checkout-session
   {
     "courseId": "your-course-id"
   }
   ```

2. Complete the checkout using test card numbers:
   - Success: `4242424242424242`
   - Requires authentication: `4000002500003155`
   - Declined: `4000000000009995`

## Webhook Event Flow

### Successful Purchase Flow

1. **User initiates checkout** → `POST /api/stripe/create-checkout-session`
2. **Payment completed** → Stripe sends `checkout.session.completed` webhook
3. **Webhook processed** → Creates enrollment and updates payment status
4. **User redirected** → To success page with course access

### Expected Webhook Events

- `checkout.session.completed` - When payment succeeds
- `checkout.session.expired` - When session expires
- `payment_intent.succeeded` - When payment processes successfully
- `payment_intent.payment_failed` - When payment fails

## Testing Checklist

### Basic Webhook Functionality

- [ ] Webhook endpoint responds with 200 status
- [ ] Webhook signature verification works
- [ ] Events are logged correctly
- [ ] Unknown events are handled gracefully

### Checkout Session Flow

- [ ] Checkout session creates successfully
- [ ] Payment record is created
- [ ] Webhook updates payment status to COMPLETED
- [ ] Enrollment is created automatically
- [ ] No duplicate enrollments are created

### Error Handling

- [ ] Invalid signature returns 400
- [ ] Missing course/user handles gracefully
- [ ] Duplicate webhook events are idempotent
- [ ] Database errors don't crash webhook processing

## Monitoring Webhook Events

### 1. Application Logs

Check your Spring Boot application logs for:

```
INFO  - Received Stripe webhook event
INFO  - Processing webhook event: checkout.session.completed with ID: evt_xxxxx
INFO  - Enrollment created for user xxx in course xxx
INFO  - Successfully processed webhook event: checkout.session.completed
```

### 2. Stripe CLI Output

The Stripe CLI will show:

```
2024-01-15 10:30:45   --> checkout.session.completed [evt_xxxxx]
2024-01-15 10:30:45  <--  [200] POST http://localhost:8080/api/stripe/webhook [evt_xxxxx]
```

### 3. Stripe Dashboard

Visit https://dashboard.stripe.com/test/events to see all webhook events and their delivery status.

## Troubleshooting

### Common Issues

1. **Webhook signature verification fails**

   - Ensure `STRIPE_WEBHOOK_SECRET` is correctly set
   - Make sure you're using the secret from `stripe listen` command

2. **Events not being received**

   - Check if Stripe CLI is running
   - Verify the forward URL is correct
   - Ensure your application is running on the expected port

3. **Database errors**

   - Verify user and course exist before webhook processing
   - Check for proper transaction handling

4. **Duplicate processing**
   - Implement idempotency using Stripe event IDs
   - Check for existing enrollments before creating new ones

### Debug Commands

```bash
# Test webhook endpoint directly
curl -X GET http://localhost:8080/api/stripe/webhook/health

# Check recent events
stripe events list --limit 10

# Resend a specific event
stripe events resend evt_xxxxx
```

## Production Deployment

### 1. Create Webhook Endpoint in Stripe Dashboard

1. Go to https://dashboard.stripe.com/webhooks
2. Click "Add endpoint"
3. Enter your production URL: `https://yourdomain.com/api/stripe/webhook`
4. Select events to listen for:
   - `checkout.session.completed`
   - `checkout.session.expired`
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`

### 2. Update Environment Variables

Replace test keys with production keys:

```bash
STRIPE_SECRET_KEY=sk_live_xxxxx
STRIPE_PUBLISHABLE_KEY=pk_live_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxx  # From webhook endpoint settings
```

### 3. Security Considerations

- Always verify webhook signatures in production
- Use HTTPS for webhook endpoints
- Implement rate limiting
- Monitor webhook delivery failures
- Set up alerting for failed webhook processing

## Event Metadata

When creating checkout sessions, include metadata for webhook processing:

```json
{
  "userId": "user-123",
  "courseId": "course-456",
  "paymentId": "payment-789"
}
```

This metadata is essential for properly processing the webhook events and creating enrollments.
