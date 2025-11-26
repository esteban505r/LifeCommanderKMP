# Timer System Documentation

## Overview

The Timer System in Oter uses a **server-authoritative, event-based model** where time is derived from timestamps rather than stored ticks. This ensures consistency across devices and handles network issues gracefully.

## Architecture

### Server-Authoritative Model

The server is the single source of truth for timer state. All timer actions (start, pause, resume, stop) are processed on the server using server time, and clients receive updates via WebSocket events.

### Data Model

```kotlin
Timer {
    id: UUID
    name: String
    duration: Long              // Total planned duration in seconds
    state: TimerState           // IDLE, RUNNING, PAUSED, STOPPED, COMPLETED
    startTime: DateTime?        // Server time when last started
    pauseTime: DateTime?        // Server time when last paused (if any)
    accumulatedPausedMs: Long  // Total time paused so far (milliseconds)
    updatedAt: DateTime         // For optimistic concurrency / conflict detection
    // ... other fields (listId, order, enabled, etc.)
}
```

### Time Calculation Logic

Time is calculated from timestamps, not stored ticks:

**For RUNNING timers:**
```
elapsed = (nowServer - startedAt) - accumulatedPausedMs
remaining = max(durationMs - elapsed, 0)
```

**For PAUSED timers:**
```
elapsed = (pausedAt - startedAt) - accumulatedPausedMs
remaining = max(durationMs - elapsed, 0)
```

**Key Points:**
- `startTime` is set when timer starts and **never reset** (even on resume)
- `accumulatedPausedMs` tracks total pause time across all pause/resume cycles
- When pausing: `pauseTime` is set, but accumulated pause is calculated on resume
- When resuming: pause duration `(now - pauseTime)` is added to `accumulatedPausedMs`

## API Endpoints

### Timer Control

All timer control endpoints require authentication and return the updated timer(s).

#### POST `/api/v1/timers/control/{listId}/start`
Start a timer in the specified list.

**Query Parameters:**
- `timerId` (UUID, optional): Specific timer to start. If omitted, starts the first enabled timer.

**Response:** `200 OK` with `List<Timer>`

#### POST `/api/v1/timers/control/{listId}/pause`
Pause the currently running timer(s) in the list.

**Query Parameters:**
- `timerId` (UUID, optional): Specific timer to pause. If omitted, pauses all running timers.

**Response:** `200 OK` with `List<Timer>`

#### POST `/api/v1/timers/control/{listId}/resume`
Resume the paused timer in the list.

**Response:** `200 OK` with `List<Timer>`

#### POST `/api/v1/timers/control/{listId}/stop`
Stop the running/paused timer(s) in the list.

**Query Parameters:**
- `timerId` (UUID, optional): Specific timer to stop. If omitted, stops all active timers.

**Response:** `200 OK` with `List<Timer>`

#### POST `/api/v1/timers/control/{listId}/restart`
Restart timer(s) from the beginning.

**Query Parameters:**
- `timerId` (UUID, optional): Specific timer to restart. If omitted, restarts all timers in the list.

**Response:** `200 OK` with `List<Timer>`

## WebSocket Communication

### Connection

**Endpoint:** `ws://host:port/api/v1/timers/notifications`

**Authentication:** Bearer token in headers (same as HTTP endpoints)

### Message Types

#### Client → Server

**Ping** (for time synchronization):
```json
{
  "type": "Ping",
  "clientTime": 1234567890123
}
```

**SubscribeTimers** (optional, implicit for user's own timers):
```json
{
  "type": "SubscribeTimers",
  "scope": "user",
  "userId": "123"
}
```

**TimerUpdate** (legacy, deprecated):
```json
{
  "type": "TimerUpdate",
  "listId": "uuid",
  "timer": { ... },
  "remainingSeconds": 60
}
```

#### Server → Client

**TimerUpdate** (state change notification):
```json
{
  "type": "TimerUpdate",
  "listId": "uuid",
  "timer": {
    "id": "uuid",
    "name": "Timer Name",
    "duration": 300,
    "state": "RUNNING",
    "remainingSeconds": 245,
    ...
  },
  "remainingTime": 245
}
```

**Pong** (time synchronization response):
```json
{
  "type": "Pong",
  "serverTime": 1234567890123,
  "clientTime": 1234567890122
}
```

### Time Synchronization

Clients maintain a `serverTimeOffset` calculated from ping/pong messages:

1. Client sends `Ping` with `clientTime` every 15 seconds
2. Server responds with `Pong` containing `serverTime` and echoed `clientTime`
3. Client calculates: `serverTimeOffset = serverTime - clientTime`
4. Client uses `(Date.now() + serverTimeOffset)` as approximate server time for UI calculations

**Note:** This offset is used for UI display only. All authoritative calculations happen on the server.

## Client Behavior

### On Load / Screen Open

1. Fetch timer lists via `GET /api/v1/timers/lists`
2. Connect to WebSocket
3. For each timer, compute remaining time from server state:
   - Use `timer.remainingSeconds` from server response
   - Or calculate locally: `remaining = duration - elapsed` using server timestamps

### Local UI Update Loop

- Update displayed remaining time every 1 second (not per-tick)
- Do NOT send network requests on each tick
- Use server-provided `remainingSeconds` as the base

### Reacting to WebSocket Events

When `TimerUpdate` is received:
1. Update local timer state
2. Recompute remaining time from server state
3. Sync `TimerPlaybackManager` with server state
4. Re-render UI

### Offline / Flaky Network

**If connection drops:**
- Continue running local UI timer based on last known state
- Optionally mark timer as "syncing..." in UI
- When network returns, server state will be authoritative

**For actions done offline:**
- Queue user actions with local timestamps (future enhancement)
- When network returns, send queued actions to server
- Server reconciles and sends back authoritative timer state

## Server-Side Timer Completion

A background worker (`TimerCheckerService`) runs every 30 seconds to:

1. Find all timers with `state = RUNNING`
2. Use `TimerTimeCalculator.shouldComplete()` to check if remaining <= 0
3. Set timer to `COMPLETED`
4. Emit `TimerUpdate` WebSocket event
5. Send push notification (if enabled)
6. Optionally start next timer in sequence (if list has loop enabled)

**Note:** Timer completion is server-authoritative, so timers will complete even if no client is connected.

## Migration from Old Model

### Database Changes

The following fields were added to the `timers` table:
- `accumulated_paused_ms` (BIGINT, default 0)
- `updated_at` (TIMESTAMP, default now)

**Migration:**
- Existing timers get `accumulated_paused_ms = 0`
- `updated_at` is set to current timestamp for all existing timers

### Backward Compatibility

- Existing APIs remain functional
- Old WebSocket messages are still accepted (but deprecated)
- Client can gradually migrate to new HTTP endpoints

## Testing

### Time Calculation Tests

Test cases cover:
- Running timer: elapsed time increases correctly
- Paused timer: elapsed time is frozen
- Multiple pause/resume cycles: accumulated pause time is correct
- Timer completion: remaining time reaches 0
- Edge cases: negative values, long pauses, timezone handling

### Concurrency Tests

Test cases cover:
- Two clients trying to pause/resume the same timer
- Server completing timer while client is paused
- Network delays and out-of-order messages

### WebSocket Tests

Test cases cover:
- Client starting timer, another client receiving update
- Ping/pong time synchronization
- Connection drops and reconnection

## Implementation Details

### Server Components

- **TimerService**: Business logic for timer operations
- **TimerTimeCalculator**: Helper for time calculations
- **TimerNotifier**: WebSocket broadcasting
- **TimerCheckerService**: Background worker for completion

### Client Components

- **TimerService** (shared): HTTP client for API calls
- **TimerWebSocketClient**: WebSocket connection and message handling
- **TimerPlaybackManager**: Local UI state management
- **TimersViewModel**: State management and coordination

### Key Files

**Server:**
- `server/src/main/kotlin/com/esteban/ruano/database/entities/Timers.kt`
- `server/src/main/kotlin/com/esteban/ruano/service/TimerService.kt`
- `server/src/main/kotlin/com/esteban/ruano/service/TimerTimeCalculator.kt`
- `server/src/main/kotlin/com/esteban/ruano/service/TimerNotifier.kt`
- `server/src/main/kotlin/com/esteban/ruano/routing/TimerRouting.kt`

**Shared:**
- `shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/models/Timer.kt`
- `shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/timer/TimerWebSocketServerMessage.kt`
- `shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/timer/TimerWebSocketClientMessage.kt`

**Client:**
- `composeApp/src/desktopMain/kotlin/com/esteban/ruano/lifecommander/ui/viewmodels/TimersViewModel.kt`
- `shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/timer/TimerPlaybackManager.kt`

## Best Practices

1. **Always use server time** for authoritative calculations
2. **Update UI optimistically** but sync with server on WebSocket events
3. **Handle network failures gracefully** - continue UI updates, mark as syncing
4. **Use WebSocket for state changes**, not for per-second updates
5. **Calculate remaining time locally** from server state for smooth UI
6. **Respect `updatedAt`** for conflict detection (future enhancement)

## Troubleshooting

### Timer not completing
- Check `TimerCheckerService` is running
- Verify `startTime` is set correctly
- Check time calculation logic in `TimerTimeCalculator`

### Time drift between devices
- Ensure ping/pong is working (check WebSocket logs)
- Verify `serverTimeOffset` is being updated
- Check server time is accurate

### Timer state out of sync
- Check WebSocket connection status
- Verify `TimerUpdate` events are being received
- Check server logs for broadcast errors

### Pause/resume not working correctly
- Verify `accumulatedPausedMs` is being updated on resume
- Check `startTime` is not being reset
- Verify pause duration calculation

