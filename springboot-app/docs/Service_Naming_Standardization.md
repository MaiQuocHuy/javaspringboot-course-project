# Service Naming Inconsistency Analysis

## Current State:

- **ServiceImp pattern**: 74 files (majority - established convention)
- **ServiceImpl pattern**: 7 files (inconsistent outliers)

## Files needing standardization:

1. AdminAffiliatePayoutServiceImpl.java → AdminAffiliatePayoutServiceImp.java
2. AdminPaymentServiceImpl.java → AdminPaymentServiceImp.java
3. InsDashboardServiceImpl.java → InsDashboardServiceImp.java
4. InstructorStudentServiceImpl.java → InstructorStudentServiceImp.java
5. NotificationServiceImpl.java → NotificationServiceImp.java
6. PermissionRoleAssignRuleServiceImpl.java → PermissionRoleAssignRuleServiceImp.java
7. RedisCacheServiceImpl.java → RedisCacheServiceImp.java (special case - cache interface pattern)

## Recommendation:

Since 74 files follow `ServiceImp` pattern, standardize the 7 outliers to maintain consistency.
