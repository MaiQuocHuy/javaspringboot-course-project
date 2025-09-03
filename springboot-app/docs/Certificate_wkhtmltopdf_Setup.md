# Certificate PDF Generation with wkhtmltopdf

## Overview

The certificate system now uses `wkhtmltopdf` to generate high-quality PDF certificates from HTML templates. This provides better rendering quality, full CSS support, and perfect font rendering compared to Java-based PDF libraries.

## Installation Requirements

### 1. Install wkhtmltopdf

#### Windows:

```bash
# Download from official site
https://wkhtmltopdf.org/downloads.html

# Or using Chocolatey
choco install wkhtmltopdf

# Or using Scoop
scoop install wkhtmltopdf
```

#### Linux (Ubuntu/Debian):

```bash
sudo apt-get update
sudo apt-get install wkhtmltopdf
```

#### Linux (CentOS/RHEL):

```bash
sudo yum install wkhtmltopdf
# or
sudo dnf install wkhtmltopdf
```

#### macOS:

```bash
# Using Homebrew
brew install wkhtmltopdf
```

### 2. Verify Installation

```bash
wkhtmltopdf --version
```

Expected output:

```
wkhtmltopdf 0.12.6 (with patched qt)
```

## Configuration

### Application Properties

Add these configurations to your `application.properties`:

```properties
# Certificate PDF generation configuration
certificate.wkhtmltopdf.path=wkhtmltopdf
certificate.temp.directory=${java.io.tmpdir}/certificates
```

### Custom Configuration

For production environments, you may need to specify the full path:

```properties
# Windows
certificate.wkhtmltopdf.path=C:/Program Files/wkhtmltopdf/bin/wkhtmltopdf.exe

# Linux
certificate.wkhtmltopdf.path=/usr/bin/wkhtmltopdf

# macOS
certificate.wkhtmltopdf.path=/usr/local/bin/wkhtmltopdf
```

## How It Works

### 1. Template Processing Flow

```
1. Load certificate-background.html from resources/templates
2. Replace placeholders with actual data:
   - [STUDENT_NAME] → User's full name
   - [COURSE_TITLE] → Course title
   - [ISSUE_DATE] → Formatted issue date
   - [CERTIFICATE_CODE] → Unique certificate code
   - [INSTRUCTOR_NAME] → Instructor name or "KTC Faculty"
   - [COURSE_LEVEL] → Course difficulty level
3. Save processed HTML to temporary file
4. Execute wkhtmltopdf to convert HTML to PDF
5. Return PDF bytes for upload to Cloudinary
6. Clean up temporary files
```

### 2. wkhtmltopdf Command Options

The service uses optimized options for certificate generation:

```bash
wkhtmltopdf \
  --page-size A4 \
  --orientation Landscape \
  --margin-top 0mm \
  --margin-right 0mm \
  --margin-bottom 0mm \
  --margin-left 0mm \
  --dpi 300 \
  --image-quality 100 \
  --image-dpi 300 \
  --enable-local-file-access \
  --enable-external-links \
  --load-error-handling ignore \
  --load-media-error-handling ignore \
  --disable-smart-shrinking \
  --no-header-line \
  --no-footer-line \
  --javascript-delay 1000 \
  --no-stop-slow-scripts \
  input.html output.pdf
```

## Service Architecture

### Main Components

1. **CertificatePdfService** - Main service class
2. **Template Processing** - HTML placeholder replacement
3. **File Management** - Temporary file handling
4. **Process Execution** - wkhtmltopdf command execution
5. **Error Handling** - Comprehensive logging and exception handling

### Key Methods

```java
// Main generation method
public byte[] generateCertificatePdf(CertificateDataDto certificateData)

// Template processing
private String loadAndProcessHtmlTemplate(CertificateDataDto certificateData)

// PDF conversion
private Path convertHtmlToPdf(Path tempHtmlFile, String processId)

// Command building
private List<String> buildWkhtmltopdfCommand(Path inputFile, Path outputFile)
```

## Error Handling

### Common Issues and Solutions

#### 1. wkhtmltopdf Not Found

```
Error: Cannot run program "wkhtmltopdf": No such file or directory
```

**Solution**: Install wkhtmltopdf or update the path configuration.

#### 2. Permission Issues

```
Error: wkhtmltopdf failed with exit code 1
```

**Solution**:

- Check file permissions for temporary directory
- Ensure wkhtmltopdf has execution permissions
- Run application with appropriate user privileges

#### 3. Font Rendering Issues

```
Warning: Font not found or CSS not loading
```

**Solution**:

- Ensure `--enable-local-file-access` is enabled
- Use web fonts (Google Fonts) as in the template
- Check CSS paths and syntax

#### 4. Template Processing Errors

```
Error: Certificate template not found
```

**Solution**:

- Verify `certificate-background.html` exists in `resources/certificates/templates/`
- Check classpath and resource loading

### Debugging

Enable debug logging to troubleshoot issues:

```properties
logging.level.project.ktc.springboot_app.certificate=DEBUG
```

This will show:

- Template loading and processing steps
- wkhtmltopdf command execution
- Temporary file paths
- Process output and errors

## Template Requirements

### HTML Template Structure

The HTML template must:

1. Be valid HTML5
2. Include all necessary CSS inline or via CDN
3. Use placeholders in brackets: `[PLACEHOLDER_NAME]`
4. Be designed for A4 landscape orientation
5. Include proper viewport and print CSS

### Supported Placeholders

- `[STUDENT_NAME]` - Student's full name
- `[COURSE_TITLE]` - Course title
- `[ISSUE_DATE]` - Certificate issue date (formatted)
- `[CERTIFICATE_CODE]` - Unique certificate identifier
- `[INSTRUCTOR_NAME]` - Instructor name
- `[COURSE_LEVEL]` - Course difficulty level

### CSS Best Practices

```css
@page {
  size: A4 landscape;
  margin: 0;
}

body {
  width: 297mm;
  height: 210mm;
  /* Use web fonts for consistent rendering */
  font-family: "Playfair Display", "Georgia", serif;
}
```

## Performance Considerations

### Optimization Tips

1. **Template Caching**: Templates are loaded once and cached
2. **Parallel Processing**: Multiple certificates can be generated simultaneously
3. **Temporary File Cleanup**: Files are automatically cleaned up after generation
4. **Resource Management**: ProcessBuilder properly handles system resources

### Resource Usage

- **Memory**: ~50MB per concurrent generation
- **Disk**: ~1-2MB temporary files per certificate
- **CPU**: Depends on wkhtmltopdf rendering complexity
- **Time**: ~2-5 seconds per certificate (depending on template complexity)

## Production Deployment

### Docker Configuration

If using Docker, ensure wkhtmltopdf is installed in the container:

```dockerfile
# Add to your Dockerfile
RUN apt-get update && \
    apt-get install -y wkhtmltopdf && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

### Environment Variables

```bash
# Override default paths for production
CERTIFICATE_WKHTMLTOPDF_PATH=/usr/bin/wkhtmltopdf
CERTIFICATE_TEMP_DIRECTORY=/tmp/certificates
```

### Security Considerations

1. **Path Validation**: Service validates all file paths
2. **HTML Escaping**: User input is properly escaped
3. **File Cleanup**: Temporary files are always cleaned up
4. **Process Isolation**: wkhtmltopdf runs as separate process

## Testing

### Unit Tests

Test the service with various certificate data scenarios:

```java
@Test
public void testCertificateGeneration() {
    CertificateDataDto data = CertificateDataDto.builder()
        .studentName("John Doe")
        .courseTitle("Advanced Spring Boot")
        .certificateCode("CERT-2025-001")
        .issueDate(LocalDateTime.now())
        .build();

    byte[] pdf = certificatePdfService.generateCertificatePdf(data);
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
}
```

### Integration Tests

Verify end-to-end certificate generation and upload:

```java
@Test
public void testCertificateCreationFlow() {
    CreateCertificateDto request = new CreateCertificateDto();
    request.setUserId("user-123");
    request.setCourseId("course-456");

    ResponseEntity<?> response = certificateController.createCertificate(request);
    assertEquals(201, response.getStatusCodeValue());
}
```

## Monitoring and Logging

### Key Metrics to Monitor

1. **Generation Success Rate**: Percentage of successful PDF generations
2. **Generation Time**: Average time per certificate
3. **Error Rates**: Failed generations by error type
4. **Resource Usage**: Memory and disk usage patterns

### Log Analysis

Important log patterns to monitor:

```
INFO  - PDF certificate generated successfully using wkhtmltopdf
ERROR - wkhtmltopdf failed with exit code 1
WARN  - Failed to clean up temporary files
DEBUG - Executing wkhtmltopdf command: [...]
```

## Troubleshooting Guide

### Quick Checks

1. ✅ Is wkhtmltopdf installed and accessible?
2. ✅ Are file permissions correct for temp directory?
3. ✅ Is the HTML template syntactically valid?
4. ✅ Are Google Fonts loading properly?
5. ✅ Is enough disk space available for temp files?

### Support Commands

```bash
# Check wkhtmltopdf installation
wkhtmltopdf --version

# Test basic conversion
wkhtmltopdf --page-size A4 --orientation Landscape https://www.google.com test.pdf

# Check available fonts
wkhtmltopdf --help | grep -i font
```

This completes the wkhtmltopdf-based certificate generation system! 🎉
