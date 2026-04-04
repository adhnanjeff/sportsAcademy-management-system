# Render Deployment Guide

## Issues Fixed

### 1. Port Binding Issue
- ✅ Added `server.port: ${PORT:8080}` to use Render's PORT environment variable
- ✅ Updated Dockerfile to EXPOSE 8080
- ✅ Application now properly binds to the port Render provides

### 2. Database Connection Issue ("Tenant or user not found")
- ✅ Fixed DATABASE_URL format - now uses proper JDBC URL format
- ✅ Reduced connection timeout from 120s to 30s
- ✅ Added connection test query: `SELECT 1`
- ✅ Reduced pool size to 3 (suitable for Render free tier)
- ✅ Added leak detection
- ✅ Improved Hikari pool configuration

### 3. Health Check
- ✅ Added Spring Boot Actuator dependency
- ✅ Configured health check endpoint: `/actuator/health`
- ✅ Added liveness and readiness probes

### 4. Dockerfile Optimization
- ✅ Multi-stage build for smaller image size
- ✅ Using Alpine Linux (smaller footprint)
- ✅ Optimized JVM settings for containers
- ✅ Proper separation of build and runtime

## Render Setup Instructions

### Step 1: Database Configuration

On Render, make sure your PostgreSQL database is created and note:
- Internal Database URL (starts with `postgresql://`)
- Username
- Password

### Step 2: Environment Variables

Set these environment variables in Render Web Service:

**Required:**
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<your-render-postgres-internal-url>
DATABASE_USERNAME=<your-database-username>
DATABASE_PASSWORD=<your-database-password>
JWT_SECRET=<generate-a-secure-random-string>
```

**Optional (for S3 features):**
```
AWS_ACCESS_KEY=<your-aws-access-key>
AWS_SECRET_KEY=<your-aws-secret-key>
AWS_S3_BUCKET_NAME=<your-bucket-name>
AWS_S3_REGION=us-east-1
```

### Step 3: Build Configuration

**Build Command:** (Leave default - Docker will handle it)

**Start Command:** (Leave default - Dockerfile CMD will be used)

**Health Check Path:** `/actuator/health`

**Docker Context:** Root directory

### Step 4: Important Database URL Format

Render provides the DATABASE_URL in this format:
```
postgres://user:password@host:port/database
```

But Spring Boot needs JDBC format:
```
jdbc:postgresql://host:port/database
```

**Solution:** In Render, add these environment variables:

1. Get the Internal Database URL from your Render PostgreSQL instance
2. If it starts with `postgres://`, manually set:
   ```
   DATABASE_URL=jdbc:postgresql://dpg-xxxxx.oregon-postgres.render.com:5432/badminton_academy_xxxx
   DATABASE_USERNAME=badminton_user
   DATABASE_PASSWORD=your_password_here
   ```

**OR** Render's automatic connection string (if using render.yaml):
- The render.yaml is configured to automatically inject the correct format

### Step 5: Deploy

1. Push these changes to GitHub:
   ```bash
   git add .
   git commit -m "fix: Configure application for Render deployment"
   git push origin main
   ```

2. In Render Dashboard:
   - Go to your Web Service
   - Click "Manual Deploy" → "Deploy latest commit"
   - OR it will auto-deploy if you have auto-deploy enabled

### Step 6: Verify Deployment

Once deployed, check:
1. Logs for successful startup
2. Health endpoint: `https://your-app.onrender.com/actuator/health`
3. Should return: `{"status":"UP"}`

## Troubleshooting

### If you still see "Tenant or user not found":

1. **Check Database URL Format:**
   ```bash
   # Wrong format (PostgreSQL native):
   postgres://user:password@host:port/db
   
   # Correct format (JDBC):
   jdbc:postgresql://host:port/db
   ```

2. **Verify credentials in Render:**
   - Go to your PostgreSQL instance
   - Click "Connect" 
   - Use the "Internal Database URL"
   - Convert it to JDBC format

3. **Test connection locally:**
   ```bash
   export DATABASE_URL="jdbc:postgresql://your-render-db-url"
   export DATABASE_USERNAME="your-username"
   export DATABASE_PASSWORD="your-password"
   export SPRING_PROFILES_ACTIVE=prod
   ./mvnw spring-boot:run
   ```

### If port binding still fails:

1. Check logs for: "Tomcat started on port(s): XXXX"
2. Ensure no firewall blocking
3. Verify Dockerfile EXPOSE command
4. Check that server.port=${PORT:8080} is in application.yml

### Database connection timeout:

1. Reduce connection timeout (already set to 30s)
2. Check if database is in same region
3. Verify database is not sleeping (free tier may sleep)
4. Check Flyway migration logs

## Performance Tips for Render Free Tier

The configuration is optimized for Render's free tier:
- Reduced connection pool size (3 connections max)
- Aggressive caching with Caffeine
- Multi-stage Docker build (smaller image = faster cold starts)
- JVM memory optimization for containers
- Connection keepalive to prevent timeouts

## Quick Fix Checklist

- [ ] DATABASE_URL uses `jdbc:postgresql://` prefix
- [ ] All three database env vars are set (URL, USERNAME, PASSWORD)
- [ ] SPRING_PROFILES_ACTIVE=prod is set
- [ ] JWT_SECRET is generated and set
- [ ] Health check path is set to `/actuator/health`
- [ ] Application binds to PORT environment variable
- [ ] Dockerfile has EXPOSE 8080
- [ ] Database is in the same region as web service

## After Deployment

Monitor your application:
```bash
# View logs
render logs -a your-app-name

# Check health
curl https://your-app.onrender.com/actuator/health
```

## Need Help?

If issues persist, check:
1. Render logs (look for the exact error)
2. Database connectivity from Render shell
3. Environment variables are correctly set
4. Database is active (not sleeping)
