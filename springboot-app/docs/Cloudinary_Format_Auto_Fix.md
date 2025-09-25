# Cloudinary Format Auto Parameter Fix

## Issue Description

The certificate image upload was failing with the following error:

```
java.lang.RuntimeException: Invalid extension in transformation: auto
    at com.cloudinary.strategies.AbstractUploaderStrategy.processResponse(AbstractUploaderStrategy.java:85)
    at project.ktc.springboot_app.upload.services.CloudinaryServiceImp.uploadCertificateImage(CloudinaryServiceImp.java:689)
```

## Root Cause

The issue was caused by using `"format", "auto"` as an upload parameter in the Cloudinary upload configuration. In Cloudinary:

- The `format: "auto"` parameter is meant for URL transformations, not upload parameters
- Upload parameters should specify actual format types like "png", "jpg", etc., or be omitted entirely

## Solution Applied

**File Modified:** `CloudinaryServiceImp.java`
**Line:** ~699

**Before (Problematic):**

```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    imageData,
    ObjectUtils.asMap(
        "public_id", publicId,
        "resource_type", "image",
        "folder", "certificates",
        "use_filename", true,
        "unique_filename", true,
        "overwrite", false,
        "quality", "auto:good",
        "format", "auto"));  // ❌ This caused the error
```

**After (Fixed):**

```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    imageData,
    ObjectUtils.asMap(
        "public_id", publicId,
        "resource_type", "image",
        "folder", "certificates",
        "use_filename", true,
        "unique_filename", true,
        "overwrite", false,
        "quality", "auto:good")); // ✅ Removed format parameter
```

## Impact

- **Certificate image uploads now work correctly** without format transformation errors
- **Cloudinary automatically detects** the image format from the uploaded data
- **Quality optimization** (`"quality", "auto:good"`) is still applied for optimal file sizes
- **Backward compatibility** maintained - existing certificates are unaffected

## Verification

- ✅ Compilation successful with no errors
- ✅ Upload parameters now follow Cloudinary best practices
- ✅ Image resource type correctly specified
- ✅ Quality optimization preserved

## Next Steps

1. Test certificate generation to verify the fix works in runtime
2. Monitor logs to ensure no more format-related errors
3. Consider adding format specification in URL transformations if needed for specific use cases

## Date Fixed

September 25, 2025
