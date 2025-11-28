# Nginx WebSocket Logging Guide

## What Was Added

1. **Custom WebSocket log format** (`websocket_log`) - Includes:
   - Standard request info
   - `upgrade` header value
   - `connection` header value  
   - `auth_header` (Authorization header preview)

2. **Standard access log format** (`main`) - For all other requests

3. **Explicit logging directives**:
   - Access logs to `/dev/stdout` (visible in Docker logs)
   - Error logs to `/dev/stderr` (visible in Docker logs)
   - WebSocket location uses the custom `websocket_log` format

## How to View Logs

### View All Nginx Logs
```bash
docker logs <nginx-container-name>
```

### View Only WebSocket Requests
```bash
docker logs <nginx-container-name> | grep "timers/notifications"
```

### View Real-Time Logs
```bash
docker logs -f <nginx-container-name>
```

### View Only Access Logs (WebSocket format)
```bash
docker logs <nginx-container-name> 2>&1 | grep -E "(timers/notifications|upgrade=)"
```

### View Error Logs Only
```bash
docker logs <nginx-container-name> 2>&1 | grep -i error
```

## What You'll See in Logs

### WebSocket Request Log Format
```
<IP> - - [<timestamp>] "GET /api/v1/timers/notifications HTTP/1.1" 101 <bytes> 
"-" "<user-agent>" upgrade="websocket" connection="upgrade" 
auth_header="Bearer eyJhbGciOiJIUzI1..."
```

### Key Fields to Check
- **Status code**: Should be `101` for successful WebSocket upgrade
- **upgrade**: Should be `"websocket"` for WebSocket requests
- **connection**: Should be `"upgrade"` for WebSocket requests
- **auth_header**: Should show your Bearer token (first part)

## Common Status Codes

- **101**: WebSocket upgrade successful ✅
- **400**: Bad request (check headers, authentication)
- **401**: Unauthorized (authentication failed)
- **404**: Not found (wrong path)
- **502**: Bad gateway (backend server not reachable)
- **503**: Service unavailable (backend server down)

## Troubleshooting

### If you don't see any logs:
1. Check if nginx container is running: `docker ps | grep nginx`
2. Verify nginx config is loaded: `docker exec <nginx-container> nginx -t`
3. Check if logs are being written: `docker exec <nginx-container> ls -la /var/log/nginx/`

### If you see 400 errors:
- Check the `upgrade` and `connection` headers in the log
- Verify `auth_header` is present and valid
- Check if the request is reaching nginx (should see it in logs)

### If you see 502 errors:
- Backend server (`oter`) might be down
- Check backend logs: `docker logs <backend-container>`
- Verify network connectivity between nginx and backend

## After Deployment

After deploying the updated nginx.conf:
1. Restart nginx: `docker restart <nginx-container>`
2. Test WebSocket connection from client
3. Immediately check logs: `docker logs -f <nginx-container>`
4. Look for the WebSocket request with status code

