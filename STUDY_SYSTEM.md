# Study System Documentation

## Overview

The Study System is a comprehensive feature in Oter KMP that helps users plan, track, and manage their study sessions. It supports three distinct study modes (Input, Processing, Review) and provides a pipeline-based workflow for managing study materials through different stages (Pending, In Progress, Processed).

### Key Features

- **Study Topics**: Organize study materials by topic with custom colors, icons, and disciplines
  - **Color Picker**: Visual color selection from predefined palette
  - **Icon Management**: Upload custom icons to S3 bucket (similar to blog posts)
  - **Discipline Autocomplete**: Smart autocomplete with existing disciplines
- **Study Items**: Track individual study materials (e.g., notes, articles) with Obsidian integration
- **Study Sessions**: Log and track study sessions with duration, mode, and notes
- **Pipeline Management**: Kanban-style workflow for managing study items through stages
- **Analytics**: Track study time by topic, mode, and item stage distribution
- **Multi-platform**: Available on Desktop, Mobile, and Server

## Architecture

The Study System follows the existing Oter KMP architecture patterns:

```
┌─────────────────┐
│   Desktop UI    │  ┌──────────────┐
│  (Compose)      │  │  Mobile UI   │
└────────┬────────┘  │  (Compose)   │
         │            └──────┬───────┘
         │                  │
         └──────────┬───────┘
                    │
         ┌──────────▼──────────┐
         │   Shared Models     │
         │   Shared Service    │
         └──────────┬──────────┘
                    │
         ┌──────────▼──────────┐
         │   Ktor Server       │
         │   - Routing         │
         │   - Service Layer   │
         │   - Repository      │
         └──────────┬──────────┘
                    │
         ┌──────────▼──────────┐
         │   PostgreSQL DB      │
         │   (Exposed ORM)     │
         └─────────────────────┘
```

### Components

1. **Server** (`server/src/main/kotlin/com/esteban/ruano/`)
   - `database/entities/`: Database entities (StudyTopic, StudyItem, StudySession)
   - `models/study/`: DTOs for API communication
   - `service/StudyService.kt`: Business logic
   - `repository/StudyRepository.kt`: Data access layer
   - `routing/StudyRouting.kt`: REST API endpoints

2. **Shared** (`shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/`)
   - `models/`: Shared data models
   - `services/study/StudyService.kt`: HTTP client for API calls

3. **Desktop** (`composeApp/src/desktopMain/kotlin/com/esteban/ruano/lifecommander/ui/`)
   - `screens/StudyScreen.kt`: Main study interface
   - `components/`: Form modals for CRUD operations
   - `viewmodels/StudyViewModel.kt`: State management

4. **Mobile** (`composeApp/src/androidMain/kotlin/com/esteban/ruano/lifecommander/screens/`)
   - `StudyScreen.kt`: Simplified mobile interface

## Data Models

### StudyTopic

Represents a study subject or category.

**Fields:**
- `id` (UUID): Unique identifier
- `name` (String, required): Topic name
- `description` (String, optional): Topic description
- `discipline` (String, optional): Academic discipline (e.g., "Mathematics", "History")
- `color` (String, optional): Hex color code for UI customization
- `icon` (String, optional): Material Icon name
- `isActive` (Boolean): Whether the topic is currently active
- `createdAt` (DateTime): Creation timestamp
- `updatedAt` (DateTime): Last update timestamp

**Example:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Linear Algebra",
  "description": "Vector spaces and linear transformations",
  "discipline": "Mathematics",
  "color": "#3B82F6",
  "icon": "calculate",
  "isActive": true,
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### StudyItem

Represents a specific study material (note, article, video, etc.).

**Fields:**
- `id` (UUID): Unique identifier
- `topicId` (UUID, optional): Associated study topic
- `title` (String, required): Item title
- `obsidianPath` (String, optional): Path to Obsidian note (for integration)
- `stage` (Enum, required): Current stage
  - `PENDING`: Not yet started
  - `IN_PROGRESS`: Currently being studied
  - `PROCESSED`: Completed
- `modeHint` (Enum, optional): Suggested study mode
  - `INPUT`: Initial learning phase
  - `PROCESSING`: Active processing/understanding
  - `REVIEW`: Review and reinforcement
- `discipline` (String, optional): Academic discipline
- `progress` (Int, 0-100): Completion percentage
- `estimatedEffortMinutes` (Int, optional): Estimated time to complete
- `createdAt` (DateTime): Creation timestamp
- `updatedAt` (DateTime): Last update timestamp

**Example:**
```json
{
  "id": "223e4567-e89b-12d3-a456-426614174001",
  "topicId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Eigenvalues and Eigenvectors",
  "obsidianPath": "Mathematics/Linear Algebra/Eigenvalues.md",
  "stage": "IN_PROGRESS",
  "modeHint": "PROCESSING",
  "discipline": "Mathematics",
  "progress": 45,
  "estimatedEffortMinutes": 120,
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-16T14:30:00Z"
}
```

### StudySession

Represents a study session with timing and notes.

**Fields:**
- `id` (UUID): Unique identifier
- `topicId` (UUID, optional): Associated study topic
- `studyItemId` (UUID, optional): Associated study item
- `mode` (Enum, required): Study mode
  - `INPUT`: Initial learning/reading
  - `PROCESSING`: Active processing/understanding
  - `REVIEW`: Review and reinforcement
- `plannedStart` (DateTime, optional): Planned start time
- `plannedEnd` (DateTime, optional): Planned end time
- `actualStart` (DateTime, optional): Actual start time
- `actualEnd` (DateTime, optional): Actual end time
- `durationMinutes` (Int, optional): Calculated duration in minutes
- `notes` (String, optional): Session notes
- `createdAt` (DateTime): Creation timestamp
- `updatedAt` (DateTime): Last update timestamp

**Example:**
```json
{
  "id": "323e4567-e89b-12d3-a456-426614174002",
  "topicId": "123e4567-e89b-12d3-a456-426614174000",
  "studyItemId": "223e4567-e89b-12d3-a456-426614174001",
  "mode": "PROCESSING",
  "plannedStart": "2024-01-16T09:00:00Z",
  "plannedEnd": "2024-01-16T11:00:00Z",
  "actualStart": "2024-01-16T09:05:00Z",
  "actualEnd": "2024-01-16T10:45:00Z",
  "durationMinutes": 100,
  "notes": "Focused on understanding eigenvalue decomposition",
  "createdAt": "2024-01-16T09:00:00Z",
  "updatedAt": "2024-01-16T10:45:00Z"
}
```

## API Endpoints

All endpoints require authentication and are prefixed with `/api/study`.

### Study Topics

#### GET `/api/study/topics`
Get all study topics for the authenticated user.

**Query Parameters:**
- `isActive` (Boolean, optional): Filter by active status

**Response:** `200 OK` with array of `StudyTopicDTO`

**Example:**
```bash
GET /api/study/topics?isActive=true
```

#### POST `/api/study/topics`
Create a new study topic.

**Request Body:** `CreateStudyTopicDTO`
```json
{
  "name": "Linear Algebra",
  "description": "Vector spaces and linear transformations",
  "discipline": "Mathematics",
  "color": "#3B82F6",
  "icon": "calculate",
  "isActive": true
}
```

**Response:** `201 Created` with `{"id": "uuid"}`

#### GET `/api/study/topics/{id}`
Get a specific study topic by ID.

**Response:** `200 OK` with `StudyTopicDTO` or `404 Not Found`

#### PATCH `/api/study/topics/{id}`
Update a study topic.

**Request Body:** `UpdateStudyTopicDTO` (all fields optional)

**Response:** `200 OK` or `400 Bad Request`

#### DELETE `/api/study/topics/{id}`
Delete (soft delete) a study topic.

**Response:** `200 OK` or `400 Bad Request`

### Study Items

#### GET `/api/study/items`
Get all study items for the authenticated user.

**Query Parameters:**
- `topicId` (UUID, optional): Filter by topic
- `stage` (String, optional): Filter by stage (PENDING, IN_PROGRESS, PROCESSED)
- `search` (String, optional): Search in title

**Response:** `200 OK` with array of `StudyItemDTO`

**Example:**
```bash
GET /api/study/items?topicId=123e4567-e89b-12d3-a456-426614174000&stage=IN_PROGRESS
```

#### POST `/api/study/items`
Create a new study item.

**Request Body:** `CreateStudyItemDTO`
```json
{
  "topicId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Eigenvalues and Eigenvectors",
  "obsidianPath": "Mathematics/Linear Algebra/Eigenvalues.md",
  "stage": "PENDING",
  "modeHint": "INPUT",
  "discipline": "Mathematics",
  "progress": 0,
  "estimatedEffortMinutes": 120
}
```

**Response:** `201 Created` with `{"id": "uuid"}`

#### GET `/api/study/items/{id}`
Get a specific study item by ID.

**Response:** `200 OK` with `StudyItemDTO` or `404 Not Found`

#### PATCH `/api/study/items/{id}`
Update a study item.

**Request Body:** `UpdateStudyItemDTO` (all fields optional)

**Response:** `200 OK` or `400 Bad Request`

#### DELETE `/api/study/items/{id}`
Delete (soft delete) a study item.

**Response:** `200 OK` or `400 Bad Request`

### Study Sessions

#### GET `/api/study/sessions`
Get all study sessions for the authenticated user.

**Query Parameters:**
- `topicId` (UUID, optional): Filter by topic
- `mode` (String, optional): Filter by mode (INPUT, PROCESSING, REVIEW)
- `startDate` (String, optional): Filter from date (YYYY-MM-DD)
- `endDate` (String, optional): Filter to date (YYYY-MM-DD)

**Response:** `200 OK` with array of `StudySessionDTO`

**Example:**
```bash
GET /api/study/sessions?startDate=2024-01-01&endDate=2024-01-31&mode=PROCESSING
```

#### POST `/api/study/sessions`
Create a new study session.

**Request Body:** `CreateStudySessionDTO`
```json
{
  "topicId": "123e4567-e89b-12d3-a456-426614174000",
  "studyItemId": "223e4567-e89b-12d3-a456-426614174001",
  "mode": "PROCESSING",
  "plannedStart": "2024-01-16T09:00:00Z",
  "plannedEnd": "2024-01-16T11:00:00Z",
  "actualStart": "2024-01-16T09:05:00Z",
  "notes": "Starting session"
}
```

**Response:** `201 Created` with `{"id": "uuid"}`

#### GET `/api/study/sessions/{id}`
Get a specific study session by ID.

**Response:** `200 OK` with `StudySessionDTO` or `404 Not Found`

#### PATCH `/api/study/sessions/{id}`
Update a study session.

**Request Body:** `UpdateStudySessionDTO` (all fields optional)

**Response:** `200 OK` or `400 Bad Request`

#### DELETE `/api/study/sessions/{id}`
Delete (soft delete) a study session.

**Response:** `200 OK` or `400 Bad Request`

#### POST `/api/study/sessions/{id}/complete`
Complete a study session (sets actualEnd and calculates duration).

**Query Parameters:**
- `actualEnd` (String, required): End datetime (ISO 8601 format)
- `notes` (String, optional): Session notes

**Response:** `200 OK` or `400 Bad Request`

**Example:**
```bash
POST /api/study/sessions/323e4567-e89b-12d3-a456-426614174002/complete?actualEnd=2024-01-16T10:45:00Z&notes=Completed%20eigenvalue%20section
```

### Icon Upload

#### POST `/api/study/icons/upload`
Upload an icon image to S3 bucket for use in study topics.

**Request:** Multipart form data
- `file` (File, required): Image file to upload
- `fileName` (String, optional): Custom file name

**Response:** `200 OK` with `{"url": "https://..."}`

**Example:**
```bash
POST /api/study/icons/upload
Content-Type: multipart/form-data
file: [image file]
```

### Disciplines

#### GET `/api/study/disciplines`
Get all unique disciplines from existing study topics.

**Response:** `200 OK` with `List<String>`

**Example:**
```json
["Mathematics", "History", "Science", "Literature"]
```

### Statistics

#### GET `/api/study/stats`
Get study statistics for the authenticated user.

**Query Parameters:**
- `startDate` (String, optional): Filter from date (YYYY-MM-DD)
- `endDate` (String, optional): Filter to date (YYYY-MM-DD)

**Response:** `200 OK` with `StudyStatsDTO`
```json
{
  "totalTimeByTopic": {
    "123e4567-e89b-12d3-a456-426614174000": 300
  },
  "totalTimeByMode": {
    "INPUT": 120,
    "PROCESSING": 150,
    "REVIEW": 30
  },
  "itemsByStage": {
    "PENDING": 5,
    "IN_PROGRESS": 3,
    "PROCESSED": 12
  }
}
```

## Database Schema

### study_topics

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Topic name |
| description | TEXT | NULLABLE | Topic description |
| discipline | VARCHAR(50) | NULLABLE | Academic discipline |
| color | VARCHAR(20) | NULLABLE | Hex color code |
| icon | VARCHAR(50) | NULLABLE | Icon name |
| is_active | BOOLEAN | DEFAULT true | Active status |
| user_id | INTEGER | FOREIGN KEY → users.id | Owner user |
| status | ENUM | DEFAULT ACTIVE | Soft delete status |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Update timestamp |

### study_items

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| topic_id | UUID | FOREIGN KEY → study_topics.id, NULLABLE | Associated topic |
| title | VARCHAR(255) | NOT NULL | Item title |
| obsidian_path | VARCHAR(500) | NULLABLE | Obsidian note path |
| stage | ENUM | DEFAULT PENDING | Current stage |
| mode_hint | ENUM | NULLABLE | Suggested study mode |
| discipline | VARCHAR(100) | NULLABLE | Academic discipline |
| progress | INTEGER | DEFAULT 0 | Progress (0-100) |
| estimated_effort_minutes | INTEGER | NULLABLE | Estimated time |
| user_id | INTEGER | FOREIGN KEY → users.id | Owner user |
| status | ENUM | DEFAULT ACTIVE | Soft delete status |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Update timestamp |

**Enums:**
- `stage`: `PENDING`, `IN_PROGRESS`, `PROCESSED`
- `mode_hint`: `INPUT`, `PROCESSING`, `REVIEW`

### study_sessions

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| topic_id | UUID | FOREIGN KEY → study_topics.id, NULLABLE | Associated topic |
| study_item_id | UUID | FOREIGN KEY → study_items.id, NULLABLE | Associated item |
| mode | ENUM | NOT NULL | Study mode |
| planned_start | TIMESTAMP | NULLABLE | Planned start time |
| planned_end | TIMESTAMP | NULLABLE | Planned end time |
| actual_start | TIMESTAMP | NULLABLE | Actual start time |
| actual_end | TIMESTAMP | NULLABLE | Actual end time |
| duration_minutes | INTEGER | NULLABLE | Calculated duration |
| notes | VARCHAR(1000) | NULLABLE | Session notes |
| user_id | INTEGER | FOREIGN KEY → users.id | Owner user |
| status | ENUM | DEFAULT ACTIVE | Soft delete status |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Update timestamp |

**Enums:**
- `mode`: `INPUT`, `PROCESSING`, `REVIEW`

## Topic and Discipline Management

### Color Selection

The Study System includes a visual color picker component (`ColorPicker`) that provides:
- **Predefined Palette**: 16 carefully selected colors optimized for UI
- **Visual Selection**: Click-to-select interface with color preview
- **None Option**: Ability to remove color selection

**Available Colors:**
- Red (#FF6B6B), Teal (#4ECDC4), Blue (#45B7D1)
- Light Salmon (#FFA07A), Mint (#98D8C8), Yellow (#F7DC6F)
- Purple (#BB8FCE), Sky Blue (#85C1E2), Orange (#F8B739)
- Green (#52BE80), Coral (#EC7063), Light Blue (#5DADE2)
- Pink (#F1948A), Light Green (#82E0AA), Gold (#F4D03F)
- Lavender (#AF7AC5)

### Icon Management

Icons are managed similarly to blog post images:
- **S3 Bucket Storage**: Icons are uploaded to AWS S3 bucket (`estebanruanoposts` by default)
- **Upload Endpoint**: `POST /api/study/icons/upload`
- **Icon Picker Component**: UI component for selecting/uploading icons
- **URL Storage**: Icons are stored as URLs in the database

**Icon Upload Flow:**
1. User clicks "Add Icon URL" in the IconPicker component
2. User enters icon URL (or uploads file via future file picker integration)
3. Icon URL is stored in the `icon` field of StudyTopic

**Future Enhancement**: Direct file upload from desktop file picker (similar to blog post image uploads)

### Discipline Autocomplete

The discipline field includes smart autocomplete:
- **Existing Disciplines**: Automatically loads all disciplines from existing topics
- **Filtering**: Filters suggestions as user types
- **New Discipline Creation**: Allows creating new disciplines on-the-fly
- **API Endpoint**: `GET /api/study/disciplines` returns all unique disciplines

**Usage:**
- Type to see matching disciplines
- Select from dropdown to reuse existing discipline
- Type new discipline name to create a new one

## Frontend Implementation

### Desktop UI

The desktop implementation provides a comprehensive study management interface with multiple views:

#### StudyScreen.kt

Main screen with three tabs:
1. **Today View**: Shows today's study sessions and upcoming items
2. **Pipeline View**: Kanban board for managing study items by stage
3. **Sessions View**: List of all study sessions with filtering

**Key Components:**
- `StudyTopicFormModal`: Create/edit study topics with color picker, icon picker, and discipline autocomplete
- `StudyItemFormModal`: Create/edit study items
- `StudySessionFormModal`: Create/edit study sessions
- `ColorPicker`: Visual color selection component
- `IconPicker`: Icon URL input and preview component
- Session timer and completion controls

#### StudyViewModel.kt

Manages state and business logic:
- Loading states for topics, items, and sessions
- CRUD operations via shared `StudyService`
- Filtering and sorting
- Statistics calculation

### Mobile UI

The mobile implementation provides a simplified interface:

#### StudyScreen.kt (Android)

Features:
- Quick session start/stop
- Today's focus items
- Basic session log
- Simplified forms for creating topics/items

### Shared Service

The shared `StudyService` (`shared/src/commonMain/kotlin/com/esteban/ruano/lifecommander/services/study/StudyService.kt`) provides:

- HTTP client methods for all API endpoints
- Automatic authentication token handling
- Error handling and response parsing
- Type-safe DTO serialization

## Usage Examples

### Creating a Study Topic

```kotlin
val studyService = StudyService()
val topic = CreateStudyTopicDTO(
    name = "Linear Algebra",
    description = "Vector spaces and linear transformations",
    discipline = "Mathematics",
    color = "#3B82F6",
    icon = "calculate",
    isActive = true
)
val topicId = studyService.createTopic(topic)
```

### Creating a Study Item

```kotlin
val item = CreateStudyItemDTO(
    topicId = topicId,
    title = "Eigenvalues and Eigenvectors",
    obsidianPath = "Mathematics/Linear Algebra/Eigenvalues.md",
    stage = "PENDING",
    modeHint = "INPUT",
    discipline = "Mathematics",
    progress = 0,
    estimatedEffortMinutes = 120
)
val itemId = studyService.createItem(item)
```

### Starting a Study Session

```kotlin
val session = CreateStudySessionDTO(
    topicId = topicId,
    studyItemId = itemId,
    mode = "PROCESSING",
    plannedStart = "2024-01-16T09:00:00Z",
    plannedEnd = "2024-01-16T11:00:00Z",
    actualStart = "2024-01-16T09:05:00Z",
    notes = "Starting session"
)
val sessionId = studyService.createSession(session)
```

### Completing a Study Session

```kotlin
studyService.completeSession(
    sessionId = sessionId,
    actualEnd = "2024-01-16T10:45:00Z",
    notes = "Completed eigenvalue section"
)
```

### Getting Statistics

```kotlin
val stats = studyService.getStats(
    startDate = "2024-01-01",
    endDate = "2024-01-31"
)
println("Total time by topic: ${stats.totalTimeByTopic}")
println("Total time by mode: ${stats.totalTimeByMode}")
println("Items by stage: ${stats.itemsByStage}")
```

## Study Modes

The system supports three distinct study modes:

### INPUT Mode
- **Purpose**: Initial learning and information intake
- **Activities**: Reading, watching videos, taking initial notes
- **When to use**: First exposure to new material

### PROCESSING Mode
- **Purpose**: Active understanding and synthesis
- **Activities**: Working through problems, creating summaries, connecting concepts
- **When to use**: After initial input, when actively working with material
- **Special behavior**: When a PROCESSING session is completed, the linked StudyItem automatically moves to PROCESSED stage

### REVIEW Mode
- **Purpose**: Reinforcement and retention
- **Activities**: Reviewing notes, practicing recall, spaced repetition
- **When to use**: After material has been processed, for retention

## Pipeline Workflow

Study items flow through three stages:

1. **PENDING**: Item created but not yet started
2. **IN_PROGRESS**: Item is currently being studied
3. **PROCESSED**: Item has been completed (automatically set when PROCESSING session completes)

## Obsidian Integration

The system stores references to Obsidian notes via the `obsidianPath` field. This allows users to:
- Link study items to their Obsidian vault
- Track which notes need study
- Maintain a connection between Oter and their note-taking system

**Note**: The system only stores the path reference. Actual Obsidian integration (opening notes, syncing content) would require additional implementation.

## Testing

### Server Tests

Unit tests are located in `server/src/test/kotlin/com/esteban/ruano/service/study/StudyServiceTest.kt`:

- CRUD operations for topics, items, and sessions
- Filtering and search functionality
- Statistics calculation
- Date range filtering
- User isolation

Run tests with:
```bash
./gradlew :server:test
```

### Integration Tests

API endpoint tests should verify:
- Authentication requirements
- Request validation
- Response formats
- Error handling

## Future Enhancements

Potential improvements for the Study System:

1. **Weekly Study Plans**: Implement the optional `WeeklyStudyPlan` entity for planning
2. **Spaced Repetition**: Add automatic scheduling based on review intervals
3. **Pomodoro Integration**: Link with existing Pomodoro timer
4. **Obsidian Sync**: Direct integration with Obsidian API
5. **Analytics Dashboard**: Visual charts and graphs for study patterns
6. **Study Goals**: Set and track study time goals per topic/week
7. **Export/Import**: Export study data for backup or analysis
8. **Mobile Notifications**: Reminders for planned study sessions

## Troubleshooting

### Common Issues

1. **Session duration not calculating**: Ensure both `actualStart` and `actualEnd` are set
2. **Item not moving to PROCESSED**: Only PROCESSING mode sessions trigger automatic stage update
3. **Date filtering not working**: Ensure dates are in `YYYY-MM-DD` format
4. **Authentication errors**: Verify token is being sent in request headers

### Debugging

Enable debug logging in `StudyService.kt` to see:
- Database queries
- Transaction boundaries
- Error details

## Related Documentation

- [README.md](./README.md): Project overview
- [RELEASE_CHECKLIST.md](./RELEASE_CHECKLIST.md): Release procedures
- Server architecture: See `server/src/main/kotlin/com/esteban/ruano/service/` for service patterns
- Frontend architecture: See `composeApp/src/desktopMain/kotlin/com/esteban/ruano/lifecommander/ui/` for UI patterns

