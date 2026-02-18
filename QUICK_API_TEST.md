# Quick API Testing Guide

## Using Swagger UI (Recommended)

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Open Swagger UI in browser:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

3. Test endpoints directly from the UI:
   - Click on any endpoint
   - Click "Try it out"
   - Fill in parameters
   - Click "Execute"

## Using cURL

### 1. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@academy.com",
    "password": "admin123"
  }'
```

**Save the accessToken from response!**

### 2. Get All Students
```bash
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### 3. Create Batch
```bash
curl -X POST http://localhost:8080/api/batches \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Advanced Batch",
    "skillLevel": "ADVANCED",
    "coachId": 1,
    "startTime": "18:00:00",
    "endTime": "19:30:00"
  }'
```

### 4. Mark Attendance
```bash
curl -X POST http://localhost:8080/api/attendance \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "batchId": 1,
    "date": "2024-02-08",
    "status": "PRESENT",
    "notes": "On time"
  }'
```

### 5. Create Skill Evaluation
```bash
curl -X POST http://localhost:8080/api/skill-evaluations \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "footwork": 7,
    "strokes": 8,
    "stamina": 6,
    "attack": 7,
    "defence": 8,
    "agility": 7,
    "courtCoverage": 6,
    "notes": "Good progress"
  }'
```

## Test Accounts

```
Admin:   admin@academy.com   / admin123
Coach:   coach@academy.com   / coach123
Student: student@academy.com / student123
Parent:  parent@academy.com  / parent123
```

## Common HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Not logged in
- `403 Forbidden` - No permission
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

## Tips

1. **Always include Authorization header** for protected endpoints
2. **Token expires in 24 hours** - use refresh token to get new one
3. **Check Swagger UI** for complete request/response schemas
4. **Use Postman** for easier testing and collection management
