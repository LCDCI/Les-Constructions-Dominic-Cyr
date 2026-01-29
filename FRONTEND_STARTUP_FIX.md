# Frontend Not Starting - Fix Guide

## Problem
Nginx can't resolve `files-service` hostname because it's not running in Docker or the Docker network isn't available.

## Solution

### Option 1: Run Full Docker Stack (Recommended)

This is the cleanest approach - all services run in Docker with proper networking.

```bash
# Navigate to project root
cd C:\Users\olivi\Les-Constructions-Dominic-Cyr

# Stop and clean up previous containers
docker-compose -f docker-compose.local.yml down -v

# Start all services
docker-compose -f docker-compose.local.yml up -d

# Wait for services to be healthy (takes about 30 seconds)
# Check status with:
docker-compose -f docker-compose.local.yml ps

# Frontend will be available at: http://localhost:3000
```

**Expected Output:**
```
NAME              STATUS          PORTS
lcdci-frontend    healthy         0.0.0.0:3000->80/tcp
backend           running         0.0.0.0:8080->8080/tcp
files-service     running         0.0.0.0:8082->8080/tcp
postgres-backend  healthy         0.0.0.0:5434->5432/tcp
```

### Option 2: Run Frontend Separately (Advanced)

If you want to run frontend separately from other services:

```bash
# First, ensure other services are running
docker-compose -f docker-compose.local.yml up -d backend files-service postgres

# Then run frontend locally
cd frontend/les_constructions_dominic_cyr

# Install dependencies (if needed)
npm install

# Start dev server
npm run dev

# Frontend will be available at: http://localhost:5173
```

**Note:** This approach requires the backend and file service to be running in Docker.

## Verification

Once services are running, test each endpoint:

```bash
# Test Backend
curl http://localhost:8080/api/v1/projects

# Test File Service
curl http://localhost:8082/files

# Test Frontend
# Open in browser: http://localhost:3000
```

## If Docker Services Won't Start

Try these troubleshooting steps:

```bash
# Check Docker is running
docker ps

# Check logs
docker-compose -f docker-compose.local.yml logs

# Rebuild images (can take a few minutes)
docker-compose -f docker-compose.local.yml build --no-cache

# Start services
docker-compose -f docker-compose.local.yml up -d

# Watch logs in real-time
docker-compose -f docker-compose.local.yml logs -f
```

## Next Steps After Frontend Starts

Once the frontend is running locally:

1. **Test the Living Environment Page:**
   - Navigate to: `http://localhost:3000/projects/proj-001-foresta/overview`
   - Click on the leaf icon (Living Environment)
   - You should see the living environment page with project colors

2. **Upload Translations to DigitalOcean:**
   - Follow the guide in `DIGITALOCEAN_UPLOAD_GUIDE.md`
   - This uploads the translations to production

3. **Test in Production:**
   - Once uploaded, test on DigitalOcean
   - Navigate to: `https://lcdci-portal-jmjxt.ondigitalocean.app/projects/proj-001-foresta/living-environment`

## Port Reference

| Service | Local Port | Docker Port | Purpose |
|---------|-----------|-------------|---------|
| Frontend (Nginx) | 3000 | 80 | Web UI |
| Backend | 8080 | 8080 | API |
| Files Service | 8082 | 8080 | File storage |
| PostgreSQL | 5434 | 5432 | Database |

## Quick Command Reference

```bash
# Start all services
docker-compose -f docker-compose.local.yml up -d

# Stop all services
docker-compose -f docker-compose.local.yml down

# Stop and remove data
docker-compose -f docker-compose.local.yml down -v

# View logs
docker-compose -f docker-compose.local.yml logs -f frontend

# Restart a specific service
docker-compose -f docker-compose.local.yml restart frontend

# Rebuild and start
docker-compose -f docker-compose.local.yml up -d --build
```

## Common Issues

### "Connection refused" errors
- Ensure `docker-compose up -d` completed successfully
- Check `docker ps` to verify containers are running
- Wait 30 seconds for services to fully initialize

### "Host not found: files-service"
- You're likely using the wrong Nginx config
- Ensure you're running the full Docker stack with `docker-compose.local.yml`
- Don't try to run Nginx locally with the Docker config

### Frontend shows blank page
- Check browser console (F12) for errors
- Check `docker-compose logs -f frontend` for error messages
- Clear browser cache (Ctrl+Shift+Delete)

### API calls failing
- Verify backend is running: `curl http://localhost:8080/api/v1/projects`
- Check backend logs: `docker-compose logs -f backend`
- Ensure frontend is using correct API URL

---

**TL;DR:**
```bash
docker-compose -f docker-compose.local.yml down -v
docker-compose -f docker-compose.local.yml up -d
# Wait 30 seconds, then open http://localhost:3000
```
