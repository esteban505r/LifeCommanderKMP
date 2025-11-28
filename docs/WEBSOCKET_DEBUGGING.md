# WebSocket 400 Error Debugging Guide

## Problem
Getting `Handshake exception, expected status code 101 but was 400` when connecting to WebSocket in production.

## What the Logs Will Show

### Client-Side Logs (Desktop App)
When you try to connect, you should see:
```
🔌 [WebSocket] Connecting to wss://api.estebanruano.com:443/api/v1/timers/notifications
🔌 [WebSocket] Host: api.estebanruano.com, Port: 443, Path: /api/v1/timers/notifications
🔌 [WebSocket] Token present: true, Token length: XXX
🔌 [WebSocket] Token preview: Bearer eyJhbGciOiJIUzI1...
🔌 [WebSocket] Request headers:
  - Authorization: Bearer eyJhbGciOiJIUzI1...
  - Connection: Keep-Alive
  - ...
```

If connection fails:
```
🚫 [WebSocket] Failed to connect to WebSocket: Handshake exception...
🚫 [WebSocket] Exception type: WebSocketException
```

### Server-Side Logs
Check your server logs for:
```
========== WebSocket Connection Attempt ==========
URI: /api/v1/timers/notifications
Method: GET
Path: /api/v1/timers/notifications
Headers (X):
  - Authorization: Bearer ...
  - Upgrade: websocket
  - Connection: Upgrade
  ...
```

If authentication fails:
```
========== WebSocket Connection REJECTED ==========
Reason: No authenticated user found
Authorization header present: true/false
```

## Common Causes of 400 Error

### 1. Authentication Failure (Most Common)
**Symptoms:**
- Server logs show "No authenticated user found"
- Authorization header is present but invalid/expired

**Solutions:**
- Check if token is valid and not expired
- Verify token format: `Bearer <token>`
- Check JWT secret matches between client and server
- Ensure token is being sent in Authorization header

### 2. Missing Headers
**Symptoms:**
- Server logs show missing `Upgrade` or `Connection` headers
- Nginx might be stripping headers

**Solutions:**
- Check nginx config has proper WebSocket headers:
  ```nginx
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection $connection_upgrade;
  proxy_set_header Authorization $http_authorization;
  ```

### 3. Nginx Rejecting Request
**Symptoms:**
- No server logs appear (request never reaches server)
- 400 error happens before authentication

**Solutions:**
- Check nginx error logs: `docker logs <nginx-container>`
- Verify nginx WebSocket location block is correct
- Check if nginx is returning 400 due to method restrictions

### 4. Wrong Path or Port
**Symptoms:**
- Client connecting to wrong URL
- Port mismatch (8080 vs 443)

**Solutions:**
- Verify `SOCKETS_HOST`, `SOCKETS_PORT`, `SOCKETS_PATH` constants
- For production, use port 443 with `wss://`
- For dev, use port 8080 with `ws://` (if nginx allows)

## Debugging Steps

### Step 1: Check Client Logs
1. Run the desktop app
2. Try to connect to WebSocket
3. Look for the `🔌 [WebSocket]` log messages
4. Verify:
   - URL is correct
   - Token is present
   - Headers are being sent

### Step 2: Check Server Logs
1. SSH into your server or check Docker logs:
   ```bash
   docker logs <server-container> | grep -i websocket
   docker logs <server-container> | grep -i "WebSocket Connection"
   ```
2. Look for:
   - "WebSocket Connection Attempt" - request reached server
   - "WebSocket Connection REJECTED" - authentication failed
   - "WebSocket Connection ACCEPTED" - connection successful

### Step 3: Check Nginx Logs
1. Check nginx access logs:
   ```bash
   docker logs <nginx-container> | grep timers/notifications
   ```
2. Check nginx error logs:
   ```bash
   docker logs <nginx-container> 2>&1 | grep -i error
   ```

### Step 4: Test Direct Connection (Bypass Nginx)
If possible, test connecting directly to the backend server (port 8080) to see if nginx is the issue.

### Step 5: Verify Configuration
1. **Client:**
   - Check `DesktopConstants.kt` - verify `SOCKETS_HOST_PROD`, `SOCKETS_PORT_PROD`
   - Ensure `VARIANT = PROD_VARIANT` for production
   - Verify certificate is in `composeApp/src/desktopMain/resources/certs/cloudflare-cert.pem`

2. **Server:**
   - Check JWT secret matches
   - Verify authentication is configured correctly
   - Check WebSocket route is properly set up

3. **Nginx:**
   - Verify WebSocket location block exists
   - Check headers are being forwarded
   - Ensure no method restrictions blocking WebSocket upgrade

## Quick Test Commands

### Test WebSocket with curl (from server)
```bash
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  https://api.estebanruano.com/api/v1/timers/notifications
```

### Check if request reaches server
```bash
# Watch server logs in real-time
docker logs -f <server-container> | grep -i websocket
```

## Expected Behavior

### Successful Connection
**Client:**
```
✅ WebSocket connected
```

**Server:**
```
========== WebSocket Connection ACCEPTED ==========
User ID: 123
```

### Failed Connection (Authentication)
**Client:**
```
🚫 [WebSocket] Failed to connect to WebSocket: Handshake exception...
```

**Server:**
```
========== WebSocket Connection REJECTED ==========
Reason: No authenticated user found
```

## Next Steps
1. Run the app and collect logs from both client and server
2. Compare the logs with this guide
3. Identify which step is failing
4. Apply the appropriate solution

