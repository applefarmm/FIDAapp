# FIDA App — Architecture Analysis

## Architecture Pattern: Clean Architecture (MVVM + Use Cases)

The FIDA Android app follows **Clean Architecture** principles combined with **MVVM** (Model-View-ViewModel) in the presentation layer. The codebase is organized into four clearly separated layers:

```
com.fida.app/
├── domain/          ← Pure Kotlin, no Android dependencies
│   ├── model/
│   ├── repository/
│   └── usecase/
├── data/            ← Android/Room + Firebase implementation details
│   ├── local/
│   │   ├── dao/
│   │   └── entity/
│   ├── remote/       ← Firebase data sources
│   └── repository/
├── presentation/    ← Activities, Fragments, ViewModels (MVVM)
│   ├── home/
│   ├── profile/
│   ├── profilesetup/
│   ├── gamification/
│   └── leaderboard/
├── di/              ← Koin dependency injection modules
└── utils/           ← Utility classes (GameManager, etc.)
```

---

## Layer Responsibilities

### 1. `domain` — Business Logic (Innermost Layer)

**Purpose:** Contains pure business rules with zero Android framework dependencies. This is the core of the application.

**Responsibilities:**
- Define domain models (plain data classes)
- Declare repository contracts (interfaces)
- Encapsulate business operations in Use Cases

**Key Files:**

| File | Role |
|------|------|
| `domain/model/UserProfile.kt` | Domain data class representing a user profile (name, age, gender, weight, height, goals, etc.) |
| `domain/repository/UserRepository.kt` | Interface defining `saveUserProfile()` and `getUserProfile()` — no implementation details |
| `domain/usecase/GetUserProfileUseCase.kt` | Single-responsibility use case that retrieves the user profile via the repository |
| `domain/usecase/SaveUserProfileUseCase.kt` | Single-responsibility use case that persists a user profile via the repository |

**Design notes:**
- Use cases use the `operator fun invoke()` convention, allowing them to be called like functions: `getUserProfileUseCase()`.
- The domain layer depends on **nothing** — it defines interfaces that outer layers implement.

---

### 2. `data` — Data Access & Persistence (Outer Layer)

**Purpose:** Implements the repository contracts defined by the domain layer. Manages local persistence via Room.

**Responsibilities:**
- Provide Room database setup (singleton `AppDatabase`)
- Define DAO interfaces for database operations
- Define Room entities mapped to database tables
- Implement repository interfaces, mapping between domain models and entities

**Key Files:**

| File | Role |
|------|------|
| `data/local/AppDatabase.kt` | Room `@Database` class with singleton pattern. Contains `fida_database`, version 1. Exposes `userProfileDao()`. |
| `data/local/dao/UserProfileDao.kt` | Room `@Dao` interface with `insertOrUpdate()` (REPLACE strategy) and `getUserProfile()` query |
| `data/local/entity/UserProfileEntity.kt` | Room `@Entity` mapped to `user_profile` table, mirrors domain model fields plus auto-generated `id` |
| `data/repository/UserRepositoryImpl.kt` | Implements `UserRepository` interface. Manually maps between `UserProfile` (domain) ↔ `UserProfileEntity` (data) |

**Design notes:**
- The data layer depends on the domain layer (implements its interfaces) but the domain never depends on data.
- Entity ↔ Domain model mapping is done manually in `UserRepositoryImpl` (no mapping library).
- Currently only local storage (Room); no remote/API data sources yet.

---

### 3. `presentation` — UI Layer (Outermost Layer)

**Purpose:** Handles user interface rendering and user interactions. Follows the **MVVM** pattern with `ViewModel` + `LiveData`.

**Responsibilities:**
- Activities and Fragments render the UI (XML layouts with View Binding)
- ViewModels expose UI state via `LiveData` and delegate business logic to use cases
- No direct database or repository access from Views — always goes through ViewModel → UseCase → Repository

**Key Files:**

| File | Role |
|------|------|
| `presentation/profile/ProfileFragment.kt` | Displays user profile data. Observes `ProfileViewModel.userProfile` LiveData. Injects ViewModel via Koin (`by viewModel()`). |
| `presentation/profile/ProfileViewModel.kt` | Loads profile through `GetUserProfileUseCase`. Exposes `userProfile: LiveData<UserProfile?>`. |
| `presentation/profilesetup/ProfileSetupActivity2.kt` | Step 2 of the multi-screen profile setup wizard |
| `presentation/profilesetup/ProfileSetupActivity3.kt` | Step 3 of the profile setup wizard |
| `presentation/profilesetup/ProfileSetupViewModel.kt` | Shared ViewModel for profile setup flow. Holds temporary field state, calls `SaveUserProfileUseCase` on completion, pre-fills from existing profile via `GetUserProfileUseCase`. |
| `presentation/home/HomeFragment.kt` | Main home screen fragment (placeholder) |
| `presentation/gamification/GamificationFragment.kt` | Gamification/rewards screen |
| `presentation/leaderboard/LeaderboardFragment.kt` | Leaderboard screen |
| `HomeActivity.kt` (root package) | Host activity with `BottomNavigationView`. Swaps fragments (Home, Gamification, Leaderboard, Profile). |
| `SplashActivity.kt` (root package) | App splash/launch screen |
| `OnboardingActivity.kt` (root package) | Onboarding flow with ViewPager2 |

**Design notes:**
- Uses **View Binding** (enabled in `build.gradle.kts`) — not Compose.
- ViewModels use `viewModelScope.launch` for coroutine-based async calls.
- Fragment navigation within `HomeActivity` is manual (`supportFragmentManager.replace()`), with `BottomNavigationView` driving the selection.

---

### 4. `di` — Dependency Injection

**Purpose:** Wires all layers together using **Koin** (a lightweight Kotlin DI framework).

**Key File:**

| File | Role |
|------|------|
| `di/AppModule.kt` | Single Koin module defining the entire dependency graph |

**Dependency graph defined in `AppModule.kt`:**

```
AppDatabase (singleton)
  └─► UserProfileDao (singleton, from AppDatabase)
        └─► UserRepositoryImpl (singleton, implements UserRepository)
              ├─► GetUserProfileUseCase (factory — new instance per injection)
              │     └─► ProfileViewModel (viewModel scope)
              │     └─► ProfileSetupViewModel (viewModel scope)
              └─► SaveUserProfileUseCase (factory)
                    └─► ProfileSetupViewModel (viewModel scope)
```

**Scoping strategy:**
- `single { }` — Database and DAO (one instance for the app lifetime)
- `single<UserRepository> { }` — Repository bound to its interface
- `factory { }` — Use cases (lightweight, stateless — new instance each time)
- `viewModel { }` — ViewModels (scoped to their lifecycle owner)

**How Koin is consumed:**
- In Fragments: `private val viewModel: ProfileViewModel by viewModel()` (Koin extension)
- The Koin module binds `UserRepositoryImpl` to the `UserRepository` interface, enforcing the Dependency Inversion Principle.

---

## Additional: `utils` Layer

| File | Role |
|------|------|
| `utils/GameManager.kt` | Manages gamification state (coins, XP, levels, streaks) via `SharedPreferences`. Stateless reward methods like `completeStepGoal()`, `completeWaterGoal()`, etc. |

> **Note:** `GameManager` does not currently follow Clean Architecture — it accesses `SharedPreferences` directly rather than going through a repository/use case. This is a candidate for future refactoring.

---

## Dependency Flow (Clean Architecture Rule)

```
presentation  ──►  domain  ◄──  data
     │                │             │
  ViewModels     Use Cases     Repositories
  Fragments      Models        Room DB/DAOs
  Activities     Interfaces    Entities
```

- **Domain depends on nothing** — it defines interfaces.
- **Data depends on domain** — it implements repository interfaces.
- **Presentation depends on domain** — ViewModels invoke use cases and observe domain models.
- **DI wires them together** — Koin binds implementations to interfaces at runtime.

This follows the **Dependency Rule**: source code dependencies point inward (toward domain), never outward.

---

## Technology Stack Summary

| Concern | Technology |
|---------|------------|
| Language | Kotlin |
| Min SDK | 26 (Android 8.0) |
| Target/Compile SDK | 35 |
| UI Toolkit | XML Layouts + View Binding |
| Architecture | Clean Architecture + MVVM |
| Dependency Injection | **Koin** (`koin-android`) |
| Local Database | **Room** (with KSP annotation processing) |
| Remote Database | **Firebase Realtime Database** (`firebase-database-ktx`) |
| Cloud Storage | **Firebase Storage** (`firebase-storage-ktx`) — profile photos |
| Authentication | **Firebase Auth** (`firebase-auth-ktx`) + **Google Sign-In** (`play-services-auth`) |
| Firebase BOM | `com.google.firebase:firebase-bom:33.12.0` |
| Async | Kotlin Coroutines (`viewModelScope`) |
| Reactive UI | `LiveData` |
| Navigation | Manual fragment transactions + BottomNavigationView |
| Image Loading | Glide |
| Navigation Library | AndroidX Navigation (fragment-ktx, ui-ktx) |

---

*Generated by analyzing 22 Kotlin source files across 17 packages in `com.fida.app`.*


# FIDA App — Room Database Analysis

## 1. Database Overview

| Property | Value |
|----------|-------|
| **Database Class** | `AppDatabase` (`com.fida.app.data.local.AppDatabase`) |
| **Database Name** | `fida_database` |
| **Current Version** | `3` |
| **Room Version** | `2.6.1` (via KSP annotation processor) |
| **Export Schema** | `false` (schema history is NOT exported to JSON) |
| **Migration Strategy** | `fallbackToDestructiveMigration()` — wipes & recreates on version bump |
| **Registered Entities** | `UserProfileEntity`, `SleepRecordEntity`, `WaterIntakeEntity` (3 tables) |
| **Exposed DAOs** | `UserProfileDao`, `SleepRecordDao`, `WaterIntakeDao` (3 DAOs) |

### Database Singleton Pattern

```kotlin
@Database(
    entities = [UserProfileEntity::class, SleepRecordEntity::class, WaterIntakeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun waterIntakeDao(): WaterIntakeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fida_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Key design decisions:**
- Thread-safe singleton via `@Volatile` + `synchronized` (double-checked locking).
- The database is provided to the DI graph as a **Koin `single`**, so the singleton pattern in the companion object is technically redundant but acts as a safety net.
- `fallbackToDestructiveMigration()` means **all data is lost** when the schema version changes — acceptable for development but risky for production.

---

## 2. Entities (Tables)

### 2.1 `UserProfileEntity` — Table: `user_profile`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Int` | `PRIMARY KEY`, `autoGenerate = true` | Auto-incrementing surrogate key |
| `name` | `String` | `NOT NULL` (Kotlin non-null) | User's display name |
| `age` | `Int` | `NOT NULL` | User's age in years |
| `gender` | `String` | `NOT NULL` | Gender identifier (e.g., "Male", "Female") |
| `weight` | `Float` | `NOT NULL` | Body weight (unit unspecified, likely kg) |
| `height` | `Float` | `NOT NULL` | Body height (unit unspecified, likely cm) |
| `fitnessGoal` | `String` | `NOT NULL` | Selected fitness goal (e.g., "Lose Weight", "Build Muscle") |
| `experienceLevel` | `String` | `NOT NULL` | Fitness experience level (e.g., "Beginner", "Intermediate") |
| `profileImageUri` | `String?` | `NULLABLE` | Optional URI string pointing to profile image |

**Notes:**
- The app assumes a **single-user model** — only one row exists, fetched via `LIMIT 1`.
- `OnConflictStrategy.REPLACE` on insert means re-inserting with `id = 0` creates a new row while providing an existing `id` updates in-place.

---

### 2.2 `SleepRecordEntity` — Table: `sleep_records`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Int` | `PRIMARY KEY`, `autoGenerate = true` | Auto-incrementing surrogate key |
| `bedtime` | `Long` | `NOT NULL` | Bedtime as epoch milliseconds |
| `wakeTime` | `Long` | `NOT NULL` | Wake-up time as epoch milliseconds |
| `duration` | `Float` | `NOT NULL` | Sleep duration in hours (derived) |
| `quality` | `Int` | `NOT NULL` | Sleep quality rating (1–5 scale) |
| `date` | `String` | `NOT NULL` | Date string in `"yyyy-MM-dd"` format |

**Notes:**
- `date` is stored as a `String`, not a `Long` epoch — this means date range queries use **lexicographic string comparison** (`BETWEEN :startDate AND :endDate`), which works correctly for `yyyy-MM-dd` format.
- Multiple sleep records can exist for the same date (no unique constraint on `date`), but the `getSleepRecordByDate()` query uses `LIMIT 1`.
- `duration` is a pre-computed field (could be derived from `wakeTime - bedtime`), introducing a potential data consistency risk.

---

### 2.3 `WaterIntakeEntity` — Table: `water_intake`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Int` | `PRIMARY KEY`, `autoGenerate = true` | Auto-incrementing surrogate key |
| `amount` | `Int` | `NOT NULL` | Water amount in milliliters |
| `timestamp` | `Long` | `NOT NULL` | Intake time as epoch milliseconds |
| `date` | `String` | `NOT NULL` | Date string in `"yyyy-MM-dd"` format |

**Notes:**
- Each row represents a **single water intake event**, not a daily total.
- Daily totals are computed via `SUM(amount)` aggregate query.
- `deleteIntakesByDate()` enables resetting a full day's records.

---

## 3. Entity Relationship Diagram

```
┌─────────────────────┐
│    user_profile      │
├─────────────────────┤
│ PK  id (auto)        │
│     name             │
│     age              │
│     gender           │
│     weight           │
│     height           │
│     fitnessGoal      │
│     experienceLevel  │
│     profileImageUri? │
└─────────────────────┘
         (no FK)

┌─────────────────────┐       ┌─────────────────────┐
│   sleep_records      │       │    water_intake      │
├─────────────────────┤       ├─────────────────────┤
│ PK  id (auto)        │       │ PK  id (auto)        │
│     bedtime          │       │     amount           │
│     wakeTime         │       │     timestamp        │
│     duration         │       │     date             │
│     quality          │       └─────────────────────┘
│     date             │
└─────────────────────┘
```

### Foreign Key Relationships

**There are no foreign key relationships defined between any entities.** All three tables are fully independent:

- `sleep_records` and `water_intake` are not linked to `user_profile` via FK.
- This is consistent with the **single-user design** — there's only ever one user, so there's no need for a user ID foreign key in the tracking tables.
- If the app evolves to support multiple users, foreign keys from `sleep_records.userId` and `water_intake.userId` → `user_profile.id` would be required.

---

## 4. DAOs — Detailed Analysis

### 4.1 `UserProfileDao`

```kotlin
@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?
}
```

| Method | Annotation | SQL | Explanation |
|--------|-----------|-----|-------------|
| `insertOrUpdate()` | `@Insert(REPLACE)` | `INSERT OR REPLACE INTO user_profile (...)` | Upsert pattern — if a row with the same PK exists, it is deleted and re-inserted. Since `id` defaults to `0` and `autoGenerate = true`, a fresh insert always auto-assigns a new ID. To update, the caller must pass the existing `id`. |
| `getUserProfile()` | `@Query` | `SELECT * FROM user_profile LIMIT 1` | Returns the first (and typically only) user profile row, or `null` if the table is empty. |

**Observations:**
- Only 2 operations — minimal CRUD. No `@Delete` or `@Update`.
- All methods are `suspend` — designed for coroutine-based async execution.

---

### 4.2 `SleepRecordDao`

```kotlin
@Dao
interface SleepRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(record: SleepRecordEntity)

    @Query("SELECT * FROM sleep_records WHERE date = :date LIMIT 1")
    suspend fun getSleepRecordByDate(date: String): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records ORDER BY date DESC")
    suspend fun getAllSleepRecords(): List<SleepRecordEntity>

    @Query("SELECT * FROM sleep_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getSleepRecordsBetween(startDate: String, endDate: String): List<SleepRecordEntity>
}
```

| Method | SQL | Explanation |
|--------|-----|-------------|
| `insertSleepRecord()` | `INSERT OR REPLACE INTO sleep_records (...)` | Inserts a new sleep record. With `id = 0`, auto-generates a new row. |
| `getSleepRecordByDate()` | `SELECT * ... WHERE date = :date LIMIT 1` | Retrieves the **first** sleep record for a given date string. Returns `null` if none exists. |
| `getAllSleepRecords()` | `SELECT * ... ORDER BY date DESC` | Returns all sleep records, newest first. Used for history views. |
| `getSleepRecordsBetween()` | `SELECT * ... WHERE date BETWEEN :start AND :end ORDER BY date ASC` | Date range query for chart/trend features (e.g., last 7 days). Lexicographic comparison works because dates use `yyyy-MM-dd` format. |

**Sample query walkthrough — `getSleepRecordsBetween()`:**

```sql
SELECT * FROM sleep_records
WHERE date BETWEEN '2025-04-28' AND '2025-05-04'
ORDER BY date ASC
```

This returns all sleep records from April 28 to May 4, sorted chronologically. The `BETWEEN` operator on strings works correctly here because ISO 8601 date strings sort lexicographically in chronological order.

---

### 4.3 `WaterIntakeDao`

```kotlin
@Dao
interface WaterIntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(intake: WaterIntakeEntity)

    @Query("SELECT * FROM water_intake WHERE date = :date ORDER BY timestamp ASC")
    suspend fun getIntakesByDate(date: String): List<WaterIntakeEntity>

    @Query("SELECT SUM(amount) FROM water_intake WHERE date = :date")
    suspend fun getTotalIntakeByDate(date: String): Int?

    @Query("DELETE FROM water_intake WHERE date = :date")
    suspend fun deleteIntakesByDate(date: String)
}
```

| Method | SQL | Explanation |
|--------|-----|-------------|
| `insertWaterIntake()` | `INSERT OR REPLACE INTO water_intake (...)` | Adds a single water intake event (e.g., user logs 250ml at 2:30 PM). |
| `getIntakesByDate()` | `SELECT * ... WHERE date = :date ORDER BY timestamp ASC` | Returns all intake entries for a given day, sorted by time — used to render an intake timeline. |
| `getTotalIntakeByDate()` | `SELECT SUM(amount) ... WHERE date = :date` | **Aggregate query** — returns the total milliliters consumed in a day. Returns `null` if no records exist (handled in repository as `?: 0`). |
| `deleteIntakesByDate()` | `DELETE FROM water_intake WHERE date = :date` | Bulk-deletes all intake records for a specific date — used as a "reset day" feature. |

**Sample query walkthrough — `getTotalIntakeByDate()`:**

```sql
SELECT SUM(amount) FROM water_intake WHERE date = '2025-05-04'
```

If the user logged 250ml at 8am, 500ml at noon, and 330ml at 3pm, this returns `1080`. If no records exist for the date, SQL `SUM` returns `NULL`, which Room maps to `Int?`. The repository layer converts this to `0` via the null-safe operator `?: 0`.

---

## 5. Migration Strategy

### Current Approach: Destructive Migration

```kotlin
Room.databaseBuilder(...)
    .fallbackToDestructiveMigration()
    .build()
```

| Aspect | Details |
|--------|---------|
| **Strategy** | `fallbackToDestructiveMigration()` |
| **Behavior** | When the database version changes (currently at `3`), Room **drops all tables** and recreates them from scratch |
| **Data preservation** | ❌ None — all existing user data is permanently lost on version upgrade |
| **Schema export** | `exportSchema = false` — no JSON schema files are generated for migration validation |
| **Manual migrations** | None defined — no `Migration(1, 2)` or `Migration(2, 3)` objects exist |
| **Auto-migration** | Not used (requires `exportSchema = true` and `@AutoMigration` annotations) |

### Version History (Inferred)

| Version | Probable Change |
|---------|----------------|
| `1` | Initial schema — `user_profile` table only |
| `2` | Added `sleep_records` table |
| `3` | Added `water_intake` table |

### Risks & Recommendations

| Risk | Severity | Recommendation |
|------|----------|----------------|
| Data loss on any schema change | 🔴 High | Implement proper `Migration(N, N+1)` objects before production release |
| No schema export for validation | 🟡 Medium | Set `exportSchema = true` and configure the schema output directory in `build.gradle.kts` |
| Cannot rollback to previous version | 🟡 Medium | Define `fallbackToDestructiveMigrationOnDowngrade()` separately or implement reverse migrations |
| No version history documentation | 🟡 Medium | Maintain a migration changelog once proper migrations are in place |

---

## 6. Indexing & Constraints

### Primary Keys

| Table | PK Column | Type | Auto-Generated |
|-------|-----------|------|-----------------|
| `user_profile` | `id` | `Int` | ✅ Yes |
| `sleep_records` | `id` | `Int` | ✅ Yes |
| `water_intake` | `id` | `Int` | ✅ Yes |

All primary keys are auto-incrementing integers. Room creates an implicit index on primary key columns.

### Explicit Indexes

**None defined.** No `@Index` annotations or `indices` array in `@Entity` declarations.

### Constraints

| Constraint Type | Used? | Details |
|----------------|-------|---------|
| `PRIMARY KEY` | ✅ | All 3 entities have auto-generated integer PKs |
| `NOT NULL` | ✅ | All Kotlin non-null fields are `NOT NULL` in SQLite. Only `profileImageUri` is nullable. |
| `FOREIGN KEY` | ❌ | No inter-table relationships |
| `UNIQUE` | ❌ | No unique constraints (e.g., `date` in `sleep_records` is not unique) |
| `CHECK` | ❌ | No value-range constraints (e.g., `quality BETWEEN 1 AND 5` is not enforced at DB level) |
| `DEFAULT` | ⚠️ Partial | Room uses Kotlin default values (`id = 0`, `profileImageUri = null`) at the code level, but doesn't generate SQLite `DEFAULT` clauses |

### Missing Indexes — Performance Impact

| Query Pattern | Column(s) | Should Be Indexed? |
|--------------|-----------|--------------------|
| `WHERE date = :date` on `sleep_records` | `date` | ✅ Yes — frequently queried |
| `WHERE date BETWEEN :start AND :end` on `sleep_records` | `date` | ✅ Yes — range scan |
| `WHERE date = :date` on `water_intake` | `date` | ✅ Yes — frequently queried |
| `ORDER BY timestamp ASC` on `water_intake` | `timestamp` | 🟡 Optional — depends on row volume |

**Recommended index additions:**

```kotlin
@Entity(
    tableName = "sleep_records",
    indices = [Index(value = ["date"])]
)
data class SleepRecordEntity(...)

@Entity(
    tableName = "water_intake",
    indices = [Index(value = ["date"])]
)
data class WaterIntakeEntity(...)
```

---

## 7. Domain ↔ Entity Mapping

The app uses **manual mapping** (no mapping library like MapStruct or ModelMapper). Mapping is done in each `RepositoryImpl`.

### Mapping Flow

```
ViewModel → UseCase → Repository (interface)
                           ↓
                    RepositoryImpl (mapping here)
                           ↓
                      DAO → Entity → SQLite
```

### Example: `UserRepositoryImpl`

```kotlin
// Domain → Entity (saving)
val entity = UserProfileEntity(
    name = profile.name,
    age = profile.age,
    gender = profile.gender,
    weight = profile.weight,
    height = profile.height,
    fitnessGoal = profile.fitnessGoal,
    experienceLevel = profile.experienceLevel,
    profileImageUri = profile.profileImageUri
)

// Entity → Domain (loading)
UserProfile(
    name = entity.name,
    age = entity.age,
    gender = entity.gender,
    weight = entity.weight,
    height = entity.height,
    fitnessGoal = entity.fitnessGoal,
    experienceLevel = entity.experienceLevel,
    profileImageUri = entity.profileImageUri
)
```

**Observations:**
- The `id` field is deliberately **dropped** during Entity → Domain mapping. The domain model has no concept of database IDs.
- This means when saving, a new `id` is auto-generated every time (since `id = 0` triggers `autoGenerate`), effectively creating a new row rather than updating the existing one.
- Potential bug: if the user updates their profile, the old row is **not** replaced — a new row is inserted. The `LIMIT 1` query masks this by always returning the first row (which is the oldest, not the newest).

---

## 8. Dependency Injection Wiring (Koin)

```kotlin
val appModule = module {
    // Database (singleton)
    single { AppDatabase.getDatabase(androidContext()) }

    // DAOs (singleton, extracted from database)
    single { get<AppDatabase>().userProfileDao() }
    single { get<AppDatabase>().sleepRecordDao() }
    single { get<AppDatabase>().waterIntakeDao() }

    // Repositories (singleton, receives DAO via constructor)
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<SleepRepository> { SleepRepositoryImpl(get()) }
    single<WaterIntakeRepository> { WaterIntakeRepositoryImpl(get()) }
}
```

All database-related objects are `single` (singleton) scoped — created once and reused throughout the app lifecycle.

---

## 9. Summary Table

| Aspect | Status | Notes |
|--------|--------|-------|
| **Entity count** | 3 | `user_profile`, `sleep_records`, `water_intake` |
| **DAO count** | 3 | One per entity |
| **Foreign keys** | None | All tables are independent |
| **Indexes** | PKs only | No secondary indexes (should add on `date` columns) |
| **Unique constraints** | None | `date` columns allow duplicates |
| **Check constraints** | None | No DB-level validation |
| **Type converters** | None | All fields use primitives (`Int`, `Float`, `Long`, `String`) |
| **Migration strategy** | Destructive | `fallbackToDestructiveMigration()` — data lost on version change |
| **Schema export** | Disabled | `exportSchema = false` |
| **All DAO methods** | `suspend` | Full coroutine support |
| **Conflict strategy** | `REPLACE` | Used on all `@Insert` methods |
| **Room version** | `2.6.1` | Latest stable as of app creation |
| **Annotation processor** | KSP | `com.google.devtools.ksp` plugin |



# FIDA App - Top Features Report

## Overview
FIDA is a gamified fitness and wellness Android app that combines health tracking with game mechanics to motivate users toward healthier habits. Below are the app's top features, derived from the source code.

---

## 1. 🏃 GPS Run Tracking
**Files:** `RunActivity.kt`, `RunTrackingService.kt`, `RunSummaryActivity.kt`, `RunHistoryActivity.kt`

- Real-time GPS-based run tracking via a foreground service (`RunTrackingService`)
- Tracks **distance**, **duration**, and **goal completion** per session
- Post-run summary screen shows stats and awards XP/coins based on performance:
  - **XP:** 10 XP per 100 meters run
  - **Coins:** 10 coins per run; 20 coins if the distance goal is reached
- Run history stored in Firestore and browsable via `RunHistoryActivity` with a RecyclerView adapter
- Data model (`RunData.kt`) persists session details for historical analysis

---

## 2. 👟 Step Counter Dashboard
**Files:** `StepCounterActivity.kt`, `StepCounterService.kt`

- Background step counting via Android sensor / Google Fit integration (`StepCounterService`)
- Weekly bar chart visualization using **MPAndroidChart** with animated bars
- Automatic **distance** conversion (steps → km using stride-length constant ~1312 steps/km)
- Automatic **calorie** estimation (steps → kcal using ~29 steps/kcal constant)
- Dark mode–aware chart theming (white text/axes when night mode is active)
- Daily step data stored per-date in Firestore under the user's `allDays` map

---

## 3. 💧 Water Intake Tracker
**Files:** `WaterTrackerActivity.kt`

- Dedicated screen for logging daily water consumption
- Data persisted to Firestore per-date for streak and badge calculations
- Contributes to the streak system (any tracked activity counts toward maintaining a streak)

---

## 4. 😴 Sleep Tracker
**Files:** `SleepTrackerActivity.kt`

- Allows users to log nightly sleep duration
- Sleep data stored per-date in Firestore (`sleepHours` field in `allDays`)
- Feeds into the streak calendar and badge unlock conditions

---

## 5. 🏆 Badge & Achievement System
**Files:** `Badge.kt`, `BadgeFragment.kt`, `BadgeAdapter.kt`

- Rich achievement model with the following attributes:
  - **Categories:** running, water, sleep, streak, general
  - **Rarity tiers:** common, rare, epic, legendary
  - **Rewards:** XP, coins, and gems awarded on unlock
  - **Progress tracking:** `progress` / `targetProgress` with a computed `progressPercent`
- Badge collection displayed in a grid via `BadgeFragment` and `BadgeAdapter`
- Each badge has a human-readable `requirement` string (e.g., "Complete 10 runs")
- Badges are Parcelable for safe passing between screens

---

## 6. 🔥 Streak System with Calendar Visualization
**Files:** `StreakDashboardFragment.kt`

- Tracks consecutive days of activity (current streak & longest streak)
- **28-day calendar grid** (4 weeks × 7 days) rendered programmatically in a `GridLayout`
- Each cell indicates whether the user had activity on that date by checking:
  - `runDistance > 0`
  - `waterIntake > 0`
  - `sleepHours > 0`
- Today's cell is visually highlighted
- **Streak Shields** – a consumable item count (`streakShields`) that can protect a streak from breaking on a missed day
- Stats displayed: current streak days, longest streak, shield inventory

---

## 7. 🎁 Daily Login Rewards
**Files:** `DailyRewardActivity.kt`

- Once-per-day reward claim gated by a `lastRewardDate` field in Firestore
- Randomized rewards:
  - **Coins:** 10–30 (base 10 + random 0–20)
  - **XP:** 5–20 (base 5 + random 0–15)
- UI disables the claim button and shows "Already Claimed" if the user has already collected today's reward
- Reward fields written atomically to Firestore via `FirestoreRepository.updateUserFields`

---

## 8. 🥇 Leaderboard
**Files:** `LeaderboardFragment.kt`, `LeaderboardAdapter.kt`, `LeaderboardEntry.kt`

- Global and/or friends-based ranking system
- Leaderboard entries displayed via RecyclerView
- Data model (`LeaderboardEntry.kt`) supports ranking users by XP, steps, or other metrics
- Encourages competition and social engagement among users

---

## 9. 💰 Gamification Economy (XP, Coins, Gems)
**Across:** `RunSummaryActivity.kt`, `DailyRewardActivity.kt`, `Badge.kt`, `PreferenceHelper.kt`

- **Three-currency system:**
  - **XP** – earned from runs, daily rewards, badge unlocks; used for leveling
  - **Coins** – earned from runs, daily rewards, badge unlocks; spendable on items/shields
  - **Gems** – rarer currency awarded by certain badges; premium store potential
- Local caching in `PreferenceHelper` for instant UI display; Firestore as source of truth
- Rewards are context-sensitive (distance-based XP, goal-based coin bonuses, randomized daily rewards)

---

## 10. 👤 User Profile, Auth & Onboarding
**Files:** `LoginActivity.kt`, `RegisterActivity.kt`, `OnboardingActivity.kt`, `ProfileFragment.kt`, `UserProfile.kt`

- **Firebase Authentication** with email/password and Google Sign-In
- First-time **onboarding flow** to set up profile and preferences
- **Profile screen** showing aggregated stats (total XP, coins, badges earned, streak info)
- User data model (`UserProfile.kt`) consolidates identity and progress data
- Settings screen (`SettingsFragment.kt`) for notification preferences and account management

---

## Feature Interaction Map

```
┌─────────────┐     rewards      ┌───────────────────┐
│  Run Tracker ├────────────────►│  Gamification      │
│  Step Counter│     XP/coins    │  (XP, Coins, Gems) │
│  Water/Sleep │                 └────────┬──────────┘
└──────┬───────┘                          │
       │ activity data                    │ unlock
       ▼                                  ▼
┌──────────────┐                ┌──────────────────┐
│ Streak System│◄───────────────│  Badge System     │
│ (Calendar)   │  streak badges │  (Achievements)   │
└──────┬───────┘                └──────────────────┘
       │ ranking data                     │
       ▼                                  │
┌──────────────┐        XP ranking        │
│ Leaderboard  │◄─────────────────────────┘
└──────────────┘
```

---

## Summary Table

| # | Feature | Key Metric | Reward Type |
|---|---------|-----------|-------------|
| 1 | GPS Run Tracking | Distance (m), Duration (s) | XP + Coins |
| 2 | Step Counter | Daily steps | Chart visualization |
| 3 | Water Tracker | Glasses/mL per day | Streak contribution |
| 4 | Sleep Tracker | Hours per night | Streak contribution |
| 5 | Badge System | 4 rarity tiers, 5 categories | XP + Coins + Gems |
| 6 | Streak System | Consecutive active days | Shield protection |
| 7 | Daily Rewards | 1 claim per day | 10–30 Coins, 5–20 XP |
| 8 | Leaderboard | Global/friend ranking | Social motivation |
| 9 | Gamification Economy | XP, Coins, Gems | Progression system |
| 10 | Auth & Onboarding | Firebase Auth | Profile + settings |
