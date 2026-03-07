# AWS S3 Integration for Image Storage

## Overview
This document describes the AWS S3 integration implemented for storing and managing images in the Badminton Academy Management System.

## Features
- Upload profile images for students, coaches, and parents
- Upload achievement certificates and medals photos
- Automatic file validation (type and size)
- Secure file deletion
- Presigned URLs for temporary file access
- Support for image formats: JPEG, PNG, GIF, WebP
- Maximum file size: 10MB (configurable)

## Configuration

### 1. Environment Variables
Set the following environment variables in your deployment environment:

```bash
AWS_S3_BUCKET_NAME=badminton-academy-images
AWS_S3_REGION=us-east-1
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key
```

### 2. Application Properties
Configuration is defined in `application.yml` files:

```yaml
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET_NAME:badminton-academy-images}
    region: ${AWS_S3_REGION:us-east-1}
    access-key: ${AWS_ACCESS_KEY:your-access-key}
    secret-key: ${AWS_SECRET_KEY:your-secret-key}
    max-file-size: 10485760  # 10MB in bytes
    allowed-file-types:
      - image/jpeg
      - image/png
      - image/jpg
      - image/gif
      - image/webp
```

### 3. AWS Setup

#### Create S3 Bucket
1. Log in to AWS Console
2. Navigate to S3 service
3. Create a new bucket (e.g., `badminton-academy-images`)
4. Configure bucket settings:
   - Block all public access (recommended)
   - Enable versioning (optional)
   - Enable server-side encryption

#### Create IAM User
1. Navigate to IAM service
2. Create a new user for the application
3. Attach the following policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket",
                "s3:GetObjectAcl",
                "s3:PutObjectAcl"
            ],
            "Resource": [
                "arn:aws:s3:::badminton-academy-images",
                "arn:aws:s3:::badminton-academy-images/*"
            ]
        }
    ]
}
```

4. Generate access keys for the user
5. Save the Access Key ID and Secret Access Key

## API Endpoints

### Upload Student Profile Image
```http
POST /api/files/upload/student-profile
Content-Type: multipart/form-data
Authorization: Bearer <token>

Body:
- file: (binary file)
```

**Response:**
```json
{
  "fileUrl": "https://badminton-academy-images.s3.us-east-1.amazonaws.com/students/profiles/uuid.jpg",
  "fileName": "profile.jpg",
  "fileType": "image/jpeg",
  "fileSize": 245678
}
```

### Upload Achievement Certificate
```http
POST /api/files/upload/achievement-certificate
Content-Type: multipart/form-data
Authorization: Bearer <token>

Body:
- file: (binary file)
```

### Upload Coach Profile Image
```http
POST /api/files/upload/coach-profile
Content-Type: multipart/form-data
Authorization: Bearer <token>

Body:
- file: (binary file)
```

### Upload Parent Profile Image
```http
POST /api/files/upload/parent-profile
Content-Type: multipart/form-data
Authorization: Bearer <token>

Body:
- file: (binary file)
```

### Delete File
```http
DELETE /api/files/delete?fileUrl=<full-s3-url>
Authorization: Bearer <token>
```

### Get Presigned URL
```http
GET /api/files/presigned-url?key=<s3-key>&durationSeconds=3600
Authorization: Bearer <token>
```

## Folder Structure in S3
```
badminton-academy-images/
├── students/
│   └── profiles/
│       └── uuid-generated-filename.jpg
├── coaches/
│   └── profiles/
│       └── uuid-generated-filename.jpg
├── parents/
│   └── profiles/
│       └── uuid-generated-filename.jpg
└── achievements/
    └── certificates/
        └── uuid-generated-filename.jpg
```

## Usage Examples

### Frontend Integration (Angular)

#### Upload Student Profile Image
```typescript
uploadStudentProfile(file: File): Observable<FileUploadResponse> {
  const formData = new FormData();
  formData.append('file', file);
  
  return this.http.post<FileUploadResponse>(
    `${this.apiUrl}/files/upload/student-profile`,
    formData
  );
}
```

#### Update Student with Profile Image
```typescript
async updateStudentWithImage(studentId: number, studentData: any, imageFile?: File) {
  // First upload the image if provided
  if (imageFile) {
    const uploadResponse = await this.uploadStudentProfile(imageFile).toPromise();
    studentData.photoUrl = uploadResponse.fileUrl;
  }
  
  // Then update the student record
  return this.http.put(`${this.apiUrl}/students/${studentId}`, studentData).toPromise();
}
```

#### Delete Old Image When Updating
```typescript
async replaceStudentImage(oldImageUrl: string, newImageFile: File) {
  // Upload new image
  const uploadResponse = await this.uploadStudentProfile(newImageFile).toPromise();
  
  // Delete old image
  if (oldImageUrl) {
    await this.http.delete(
      `${this.apiUrl}/files/delete?fileUrl=${encodeURIComponent(oldImageUrl)}`
    ).toPromise();
  }
  
  return uploadResponse.fileUrl;
}
```

### Backend Service Integration

#### Update Student Service Example
```java
@Service
public class StudentService {
    
    private final S3Service s3Service;
    private final StudentRepository studentRepository;
    
    public StudentResponse updateStudentWithImage(Long studentId, 
                                                  StudentRequest request, 
                                                  MultipartFile imageFile) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // If new image provided, delete old one and upload new one
        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImageUrl = student.getPhotoUrl();
            String newImageUrl = s3Service.replaceFile(oldImageUrl, imageFile, "students/profiles");
            student.setPhotoUrl(newImageUrl);
        }
        
        // Update other fields
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        // ... other fields
        
        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }
}
```

## Security Considerations

1. **Authentication**: All endpoints require valid JWT authentication
2. **Authorization**: 
   - Students/Achievements: ADMIN and COACH roles
   - Parent profiles: ADMIN, COACH, and PARENT roles
3. **File Validation**: 
   - Type validation (only image formats allowed)
   - Size validation (max 10MB)
4. **Bucket Access**: Keep bucket private, use presigned URLs for public access
5. **CORS Configuration**: Configure S3 bucket CORS if frontend needs direct access

## Error Handling

The system handles various error scenarios:

- **Invalid file type**: Returns 400 Bad Request
- **File too large**: Returns 400 Bad Request
- **Upload failure**: Returns 500 Internal Server Error
- **File not found**: Returns 404 Not Found
- **AWS credentials invalid**: Returns 500 Internal Server Error

## Database Models

### Existing Fields
All models already have URL fields for storing S3 URLs:

- `Student.photoUrl` - Student profile image
- `User.photoUrl` - Coach/Parent profile image (inherited)
- `Achievement.certificateUrl` - Achievement certificate/medal image

## Testing

### Local Development
For local development, you can:
1. Use AWS S3 with a separate bucket (e.g., `badminton-academy-images-local`)
2. Use LocalStack for S3 emulation
3. Configure a test AWS account

### Test Endpoints with cURL

```bash
# Upload student profile
curl -X POST http://localhost:8080/api/files/upload/student-profile \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/image.jpg"

# Delete file
curl -X DELETE "http://localhost:8080/api/files/delete?fileUrl=<encoded-url>" \
  -H "Authorization: Bearer <token>"
```

## Maintenance

### Monitoring
- Monitor S3 bucket size and usage
- Set up CloudWatch alarms for unusual activity
- Regularly review access logs

### Cleanup
Consider implementing:
- Orphaned file detection (files not referenced in database)
- Automatic cleanup of old/unused files
- Lifecycle policies for S3 bucket

## Cost Optimization

1. Use S3 Standard-IA for less frequently accessed files
2. Enable S3 Intelligent-Tiering
3. Set lifecycle rules to move old files to Glacier
4. Optimize image sizes before upload
5. Use CloudFront CDN for frequently accessed images

## Dependencies

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>
```

## Troubleshooting

### Upload Fails
- Verify AWS credentials are correct
- Check bucket name and region
- Ensure IAM user has correct permissions
- Verify file meets size and type requirements

### Images Not Loading
- Check if S3 URL is correctly stored in database
- Verify bucket CORS settings
- Check if presigned URLs have expired
- Ensure bucket permissions allow GetObject

### Performance Issues
- Consider using CloudFront CDN
- Optimize image sizes
- Implement caching strategies
- Use presigned URLs for large files
