# AI Coding Agent Instructions - Jandi Plan Backend

## Project Overview
Spring Boot 3.4 backend for a trip planning application with community features, deployed on Google Cloud Platform via Jenkins CI/CD to EC2.

## Architecture & Package Structure

### Domain Boundaries
- **`tripPlan/`**: Trip planning domain (trip, itinerary, reservation, place)
- **`commu/`**: Community features (community posts, comments with likes/reports)
- **`user/`**: User management, preferences (Continent, Country, City)
- **`socialLogin/`**: OAuth integration (Kakao, Naver, Google)
- **`image/`**: GCP Cloud Storage image management
- **`googlePlace/`**: Google Maps API integration for recommendations
- **`resource/`**: App resources (banner, notice)
- **`security/`**: JWT authentication (JwtTokenProvider, JwtAuthenticationFilter)
- **`global/`**: Global exception handling
- **`util/`**: Cross-cutting concerns (ValidationUtil, CommunityUtil)

### Service Layer Convention
Services follow a **Query/Update separation pattern**:
- **`*QueryService`**: Read operations, complex queries (e.g., `CommunityQueryService`, `CommentQueryService`)
- **`*UpdateService`**: Write operations (create/update/delete) with transactional logic (e.g., `CommunityUpdateService`)
- **Single-purpose services**: `*LikeService`, `*ReportService`, `*SearchService` for focused concerns

Example from [commu/community/controller/PostController.java](commu/community/controller/PostController.java):
```java
private final CommunityQueryService communityQueryService;
private final CommunityUpdateService communityUpdateService;
```

## Development Workflow

### Local Development Setup
1. **Environment Configuration**: Create `src/main/resources/application-dev.properties` with required secrets (DB, JWT, GCP credentials, API keys)
2. **Profile Activation**: Set `SPRING_PROFILES_ACTIVE=dev` environment variable
   - IntelliJ: Run Configuration → Environment variables
   - Terminal: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`
   - VS Code: Add to `launch.json` env section

### Build & Run Commands
- **Build**: `./gradlew build` (skips tests: `./gradlew build -x test`)
- **Run locally**: `./gradlew bootRun` (with SPRING_PROFILES_ACTIVE=dev)
- **Tests**: `./gradlew test`

### Docker & Deployment
- **Multi-stage Dockerfile** (build with gradle wrapper, run with JRE-only)
- **CI/CD**: Jenkins pipeline builds Docker image → pushes to GHCR → deploys via SSH to EC2
- **Health checks**: Actuator endpoint at `/actuator/health` monitored in container

## Key Technical Patterns

### Authentication Flow
1. JWT token provided via `Authorization` header
2. **`JwtAuthenticationFilter`** (extends `OncePerRequestFilter`) extracts and validates token
3. User loaded via **`CustomUserDetailsService`**
4. Authentication set in `SecurityContext`
5. Controllers access user via `@AuthenticationPrincipal String userEmail`

Open endpoints configured in [config/SecurityConfig.java](config/SecurityConfig.java#L80-L120) - check before adding new public routes.

### Validation Pattern
All entity existence/permission checks centralized in **`util/ValidationUtil.java`**:
```java
User user = validationUtil.validateUserExists(userEmail);
validationUtil.validateUserRestricted(user); // Check if reported
Community post = validationUtil.validatePostExists(postId);
validationUtil.validateUserIsAuthorOfPost(user, post);
```
Throws **`BadRequestExceptionMessage`** on failure → caught by **`GlobalExceptionHandler`** → returns 400 with standardized error response.

### Image Upload Workflow
1. **Temp upload**: POST to `/api/images/temp` → saves to GCS, returns temp URL
2. **Entity creation**: Frontend includes temp URLs in post/trip creation
3. **Image finalization**: `CommunityUpdateService` / `TripService` updates Image entity references
4. **Cleanup**: `ImageCleanupService` removes orphaned images using `@Scheduled` + transaction synchronization

### Transaction Management
Update services use `@Transactional` with cleanup hooks:
```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            // Async cleanup operations (e.g., unused image deletion)
        }
    }
);
```

## External Integrations

### GCP Services
- **Cloud Storage**: Images stored in bucket `plan-storage` via `GoogleCloudStorageService`
- **Secret Manager**: Credentials loaded via base64-encoded service account key (`GCP_SA_KEY_BASE64`)

### Google Maps API
Used for place recommendations in `googlePlace/`. API key configured in `application.properties` as `${GOOGLE_MAP_KEY}`.

### OAuth Providers
Redirect URIs configured for Kakao, Naver, Google (see `application.properties`). Controllers in `socialLogin/` handle callbacks.

## Database Conventions

### JPA Configuration
- **DDL auto**: `spring.jpa.hibernate.ddl-auto=update` (be cautious in production)
- **SQL logging**: Enabled with formatting (`spring.jpa.show-sql=true`)
- **Dialect**: MySQL with `org.hibernate.dialect.MySQLDialect`

### Entity Relationships
- Soft delete not used - rely on cascade operations and orphan removal
- Many-to-many through explicit join entities (e.g., `TripParticipant`, `CommunityLike`)
- User role stored as integer (`role` column), converted to enum in code

## Project-Specific Notes

### Korean Timezone
Services use `ZoneId.of("Asia/Seoul")` for timestamp operations:
```java
private static final ZoneId KST = ZoneId.of("Asia/Seoul");
LocalDateTime.now(KST);
```

### Role-Based Access
Admin/Staff checks via `ValidationUtil`:
- `validateUserIsAdmin(user)` - full access
- `validateUserIsStaff(user)` - limited management features

### Email Verification
New users receive verification emails via Gmail SMTP (`spring.mail.*` config). Verification endpoint: `/api/users/verify`.

## Common Pitfalls

1. **Missing profile activation**: App will fail to start without `SPRING_PROFILES_ACTIVE=dev` locally
2. **Circular service dependencies**: Keep Query/Update service separation to avoid dependency cycles
3. **Image orphaning**: Always use transaction synchronization for cleanup operations
4. **JWT secret exposure**: Never commit `application-dev.properties` - it's gitignored
5. **Cross-origin issues**: CORS configured for `https://justplanit.site` only in SecurityConfig

## Testing Strategy
- Tests run with H2 in-memory database (test profile)
- JUnit 5 + Spring Boot Test starter
- Security tests with `spring-security-test`

## Documentation References
- Main app class: [PlanBackendApplication.java](src/main/java/com/jandi/plan_backend/PlanBackendApplication.java)
- Security setup: [config/SecurityConfig.java](src/main/java/com/jandi/plan_backend/config/SecurityConfig.java)
- Validation patterns: [util/ValidationUtil.java](src/main/java/com/jandi/plan_backend/util/ValidationUtil.java)
