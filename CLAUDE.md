# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`finances` is an early-stage, single-Activity Jetpack Compose Android app (an expense manager). Much of the UI is scaffolded but not yet wired up — for example, every destination in `app/.../Navigation.kt` is registered with an empty `composable { }` body. Expect to be building features onto skeletons rather than modifying finished screens.

## Build & Test

Uses the Gradle wrapper (Gradle 9.6.1, AGP 9.2.1, Kotlin 2.4.0). Always invoke `./gradlew`.

```bash
./gradlew assembleDebug                       # build the debug APK
./gradlew :feature:transaction:build          # build a single module
./gradlew test                                 # all JVM unit tests
./gradlew :core:database:testDebugUnitTest     # unit tests for one module
./gradlew connectedAndroidTest                 # instrumented tests (needs a device/emulator)
./gradlew lint                                 # Android lint
```

Run a single unit test class/method:

```bash
./gradlew :core:database:testDebugUnitTest --tests "com.example.core.database.ExampleUnitTest"
```

Note: the `*ExampleUnitTest`/`*ExampleInstrumentedTest` files in every module are unmodified template stubs, not real tests.

## Module architecture

Modules are declared in `settings.gradle.kts`. Dependencies flow one way: `:app` and `:feature:*` depend on `:core:*`; core modules never depend on features.

- **`:app`** — the only `com.android.application` module. Hosts `MainActivity` (single Activity, `enableEdgeToEdge` + `FinancesTheme`) and the Navigation Compose graph. Its `Screen` sealed class defines all routes; parameterized routes (`Account`, `Transaction`) expose both an instance (`account/$id`) and a `ROUTE` template constant (`account/{id}`).
- **`:core:common`** — shared Compose theme (`FinancesTheme`) and reusable UI components (`AppBar`, `ListOfItems`, `Screen`). Consumed by `:app` and features.
- **`:core:database`** — Room persistence: entities (`Transaction`, `Account`, `Category`), DAOs (`abstract class`, exposing `suspend`/`Flow`/`PagingSource` methods), and type converters. See the Room note below.
- **`:core:datastore`** — user settings via Preferences DataStore. `Setting` wraps a single `settingDataStore` and exposes each preference as a `Flow` with a default (falling back to `emptyPreferences()` on `IOException`); enums (`CycleType`, `Currency`) are stored by `.name`.
- **`:feature:transaction`** — transaction feature (repository + ViewModel + UI). Depends on all three core modules.

There is **no dependency-injection framework**. Objects are constructed by hand: `Setting(context)` → `TransactionRepository(setting, dao)` → `TransactionViewModel(repo)`. Wire new dependencies manually through constructors.

## Data flow (transactions)

The transaction list is the reference example of the intended DAO → Repository → ViewModel → UI pattern, all built on **Paging 3**:

1. `TransactionDAO.getTransactionsBetween(start, end)` returns a `PagingSource<Int, Transaction>`.
2. `TransactionRepository` wraps it in a `Pager` (`PagingConfig(15)`) and selects the date range from user settings — it reactively switches between monthly and salary-cycle windows via `setting.cycleType.flatMapLatest { ... }`. Timestamps are epoch **seconds** (via `java.time` + `ZoneId.systemDefault()`), matching the entity's `datetime` column.
3. `TransactionViewModel` maps `PagingData<Transaction>` to a `TransactionListItem` sealed type (`TransactionItem` / `DateHeader`), using `insertSeparators` to inject date headers when the day changes, and `.cachedIn(viewModelScope)`.

When adding features, follow this same layering rather than accessing DAOs from UI.

## Conventions & known rough edges

- **Room 3 migration is in flight.** Entities and DAOs import `androidx.room3.*`, but `TransactionConverters` still imports the old `androidx.room.TypeConverter`. There is **no `@Database`/`RoomDatabase` class yet** and **no KSP/annotation processor is configured**, so DAOs and converters are not yet compiled into a working database. Adding persistence will require creating the database class and its build wiring — don't assume Room is fully set up.
- Dependencies are managed through the version catalog `gradle/libs.versions.toml`; add libraries there, not inline. The catalog currently has redundant duplicate entries (e.g. multiple `material3` and `paging-compose` aliases at different versions) — reuse an existing alias before adding a new one.
- `feature/transactions/data/repository/` (plural "transactions") is an **orphaned earlier structure** — it is not in `settings.gradle.kts` and is superseded by `:feature:transaction`. Don't edit it; the live code is under `feature/transaction/`.
- Package names are inconsistent with module paths by design: `:core:datastore` uses `com.example.datastore` and `:feature:transaction` uses `com.example.transaction` (no `feature.` prefix). Match each module's existing `namespace` in its `build.gradle.kts`.
- SQL in DAO `@Query` annotations is written as multi-line string concatenation (`"" + "SELECT..."`); follow that style for consistency.
