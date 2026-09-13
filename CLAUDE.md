# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## Overview

`finances` is a modular, single-Activity Android expense manager built with Jetpack Compose. It supports accounts, categories, transactions, settings, SMS transaction parsing, spending analysis, and loan history.

`MainActivity` enables edge-to-edge rendering, applies `FinancesTheme`, and hosts the Navigation Compose graph. `Navigation.kt` wires the permission, login, home, account, category, loan, analysis, transaction, settings, and profile screens. Routes are defined by the `Screen` sealed class; parameterized routes expose an instance route and a `ROUTE` template. The Loans action on Home opens the loan history destination.

## Current status

Recently fixed (verified via `./gradlew test compileDebugAndroidTestKotlin assembleDebug`, all green):

- `:core:database` and `:core:common` `compileDebugAndroidTestKotlin` — previously failed; stale androidTest files referenced APIs removed from `main` (see below).
- `AccountRepository.updateAccount` — added an `updateBalance: Boolean = true` param so metadata-only edits (wired from `AddEditAccountContent`'s `balanceEdited` flag) can't clobber a balance changed concurrently by `adjustBalance()`.
- `TransactionRepository.resolveTransactionAccounts` — `create()` used to unconditionally overwrite `accountId` from a `rawAccountNo` lookup; it now prefers an explicit `accountId` already on the transaction, matching `updateTransaction()`'s behavior.
- Deleted two stale `:core:database` androidTest files (`AccountIdentifierMigrationTest`, `PayeeCategoryPreferenceDAOTest`) that referenced migrations/backfill SQL no longer present in `DatabaseModule`.
- Minor cleanup: unused `Migration` import in `DatabaseModule.kt`, deprecated `Modifier.menuAnchor()` in `DropDown.kt`.

Open backlog (not yet started, need product/UX scoping before implementation):

- Loan add/edit screen — `Navigation.kt` wires `LoanHistoryScreen(onLoanClick = {})` as a no-op; `LoanRepository` supports create/update/delete but nothing in the UI calls them.
- "Categories at risk" query — `CategoryDAO` has a TODO for a query over categories whose cycle spend exceeds `budget_per_cycle`; unimplemented.
- Debit-card preference onboarding UI — `DebitCardPreferenceRepository` is fully implemented and tested but unused by any screen.
- Fate of the orphaned `feature/transactions/data/repository/` module — candidate for deletion, pending confirmation.

Known still-open issue (pre-existing, unrelated to the above): `./gradlew lint` fails with one error, `RemoveWorkManagerInitializer` in `app/src/main/AndroidManifest.xml` (see Build and test).

## Behavioral guidelines

These guidelines bias toward caution over speed. Use judgment for trivial tasks.

### 1. Think before coding

Do not assume or hide confusion. Surface tradeoffs.

Before implementing:

- State assumptions explicitly and ask when uncertain.
- Present multiple interpretations instead of silently choosing one.
- Point out simpler approaches and push back when warranted.
- Stop and ask when requirements are unclear.

### 2. Simplicity first

Write the minimum code that solves the requested problem:

- Do not add speculative features, abstractions, flexibility, or configurability.
- Do not add error handling for impossible scenarios.
- If a solution is substantially longer than necessary, simplify it.
- Prefer the approach a senior engineer would consider direct and maintainable.

### 3. Surgical changes

Touch only what the task requires and clean up only changes introduced by the task:

- Do not refactor adjacent code, comments, or formatting without a direct need.
- Match the existing style and mention unrelated dead code instead of deleting it.
- Remove imports, variables, and functions made unused by the current change.
- Every changed line should trace directly to the request.

### 4. Goal-driven execution

Define verifiable success criteria and work until they pass:

- Convert behavior changes into tests that fail before the fix and pass afterward.
- Ensure refactors preserve existing tests before and after.
- For multi-step tasks, state a brief plan with a verification check for each step.
- Prefer concrete outcomes over vague goals such as "make it work."

These guidelines are successful when diffs contain fewer unnecessary changes, implementations need fewer rewrites, and clarification happens before coding.

## Build and test

The project uses Gradle 9.6.1, AGP 9.2.1, Kotlin 2.4.0, Java 11, minSdk 29, and targetSdk 36. Modules use the new compile SDK declaration for API 37.1. Always use the Gradle wrapper.

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
./gradlew :feature:transaction:build
./gradlew :core:database:testDebugUnitTest
./gradlew connectedAndroidTest # requires a device or emulator
```

Run a single JVM test with:

```bash
./gradlew :core:database:testDebugUnitTest --tests "com.example.core.database.ExampleUnitTest"
```

Automated coverage is limited but includes SMS parsing, payee-category backfill/normalization, account identifier normalization, debit-card preferences, transaction account matching, and account balance synchronization. The remaining `ExampleUnitTest` and `ExampleInstrumentedTest` files are Android template stubs.

All modules' `compileDebugAndroidTestKotlin` and `test` tasks currently pass. `lintDebug` currently fails with one pre-existing error (`RemoveWorkManagerInitializer`, unrelated to app logic — `app/src/main/AndroidManifest.xml` keeps the default WorkManager `InitializationProvider` alongside `FinancesApp`'s `Configuration.Provider`); this was not introduced by recent work and is still open.

## Module architecture

Modules are registered in `settings.gradle.kts`. Dependency flow is `:app`/`:feature:*` -> `:core:*`; core modules must not depend on feature modules.

- **`:app`** - application entry point, Hilt/WorkManager setup, theme host, routes, and navigation.
- **`:core:common`** - shared Compose components, formatting utilities, repositories, and cross-feature models. It exposes `:core:database` and `:core:datastore` as API dependencies.
- **`:core:database`** - Room 3 database, entities, DAOs, projections, converters, and the Hilt database module.
- **`:core:datastore`** - Preferences DataStore-backed user and cycle settings.
- **`:core:sms`** - SMS receiver, parser, and Hilt-enabled WorkManager worker for transaction import.
- **`:feature:account`** - account list and add/edit UI.
- **`:feature:category`** - category list and editing UI.
- **`:feature:transaction`** - transaction history and add/edit UI.
- **`:feature:analysis`** - current-cycle budget groups, donut visualization, and spending heatmap.
- **`:feature:home`**, **`:feature:login`**, **`:feature:permission`**, **`:feature:setting`** - their corresponding screens and ViewModels.
- **`:feature:loan`** - loan history UI and ViewModel, linked from Home through the `loans` navigation destination.

`feature/transactions/data/repository/` (plural `transactions`) is an orphaned earlier module that is not included in `settings.gradle.kts`. Do not modify it; repositories used by the app live in `:core:common`.

## Dependency injection and background work

The project uses Hilt:

- `FinancesApp` is annotated with `@HiltAndroidApp` and supplies `HiltWorkerFactory` to WorkManager.
- `MainActivity` is annotated with `@AndroidEntryPoint`.
- ViewModels use `@HiltViewModel` and constructor injection.
- Repositories and `Setting` use constructor injection.
- `DatabaseModule` provides the singleton `AppDatabase` and its DAOs.
- `ParseSmsWorker` uses `@HiltWorker` and assisted injection.

Use constructor injection rather than manually constructing repositories or ViewModels. Both `jakarta.inject` and `javax.inject` imports currently exist; follow the convention in the file being edited rather than introducing another inconsistency.

## Persistence

Room 3 is configured with KSP in `:core:database`. `AppDatabase` is version 1 and contains `Transaction`, `Account`, `Category`, `Loan`, `PayeeCategoryPreference`, and `DebitCardPreference` entities — all shipped together in the initial schema since the app has no released version yet. Its DAOs are exposed through Hilt; converters use Room 3 `@ColumnTypeConverter`, and Paging DAO return conversion is enabled.

`DatabaseModule` builds `finances.db`, seeds default categories (Food, Transport, Groceries, Utilities, Shopping, Friends & Family, Salary, Investment) via a `RoomDatabase.Callback.onCreate`, and provides all DAOs through Hilt. **No `Migration` is currently defined.** Preference rows are deleted when their referenced category or account is deleted (`ForeignKey.CASCADE`/`SET_NULL`). Once the app ships, increment the database version and add a real `Migration` for every future schema change so existing user data is preserved; until then a fresh version-1 schema is acceptable.

Transaction and preference payees are normalized once with `trim().lowercase(Locale.ROOT)` when inserted. All later lookups and updates use the stored value directly.

Account and debit-card identifiers are optional digit strings. Their repositories strip non-digits while preserving leading zeroes. Non-null account numbers and debit-card preference keys are unique; `Account.id` remains the account primary key. A debit-card preference maps one card identifier to an existing account that has an account number. Its repository is ready for future onboarding, but no current UI or manual transaction action creates preferences. `AccountRepository.updateAccount(account, updateBalance = true)` writes the full row by default; pass `updateBalance = false` for metadata-only edits (e.g. from the add/edit account screen) so a stale in-memory `balance` can't clobber a balance change made concurrently by `adjustBalance()`.

Important projections include:

- `TransactionWithCategory` for transaction UI rows.
- `CategoryWithInfo` for category spending and budget summaries.
- `DailyExpense` for analysis heatmap data.

Transaction and loan datetimes are stored as epoch seconds. Daily analysis converts timestamps to local epoch days using the system time-zone offset.

## Data flow and state

Follow the existing DAO -> repository -> ViewModel -> Compose UI flow. UI code must not access DAOs directly.

Transaction history is the primary Paging 3 example:

1. `TransactionDAO` returns `PagingSource` queries for joined transaction/category rows.
2. `TransactionRepository` wraps them in `Pager(PagingConfig(15))` and switches reactively between monthly and salary-date cycle ranges from `Setting`.
3. `TransactionsHistoryViewModel` switches between current and past cycles, maps rows to `TransactionListItem`, inserts date headers, and caches the stream in `viewModelScope`.

`TransactionRepository.getCurrentCycleDailyExpenses()` supplies current-cycle aggregate data to `AnalysisViewModel`. The analysis screen combines this with `CategoryRepository.getCategoriesWithInfo`, groups expense categories into necessities, disposables, and investments, and exposes a `StateFlow`.

`PayeeCategoryPreferenceRepository` is the only production layer that accesses `PayeeCategoryPreferenceDAO`; it normalizes payees when inserting preferences. `TransactionRepository` normalizes transaction payees before insertion and coordinates category inheritance inside `AppDatabase.withWriteTransaction`. Both single and batch creates resolve a remembered category only when the incoming transaction has no category; explicit categories always win. Remembering a category performs one exact payee-and-`TransactionType` update and inserts or replaces the preference in the same transaction.

For each new transaction, `TransactionRepository.resolveTransactionAccounts` prefers an explicit `accountId` already set on the incoming `Transaction`; only when it is `null` does it normalize `rawAccountNo` and look it up against account numbers first, then debit-card preferences, leaving unmatched/absent identifiers unassigned. `updateTransaction()` never re-resolves the account, so an explicit `accountId` passed to an update is always preserved. Preferences apply only to future creates and never retag existing rows. The repository applies transaction and account-balance writes atomically: credits add, debits subtract, updates reverse the old effect before applying the new effect, deletions reverse their effect, and null-account transactions are balance-neutral.

`Setting` wraps the application `settingDataStore`. Preferences are exposed as `Flow`s with defaults; enum values are persisted by `.name`. Only `IOException` is converted to `emptyPreferences()` and other errors are rethrown.

## Conventions and known rough edges

- Manage dependencies through `gradle/libs.versions.toml`, not inline versions. The catalog contains redundant Room 3, Material 3, and related aliases; reuse an alias already used by the target module rather than adding another.
- Package names follow each module's `namespace`, not necessarily its directory path (for example, `com.example.datastore` and `com.example.transaction`).
- Shared UI belongs in `:core:common`; feature-specific screens, components, and ViewModels stay in their feature module.
- DAOs are abstract classes and use `suspend`, `Flow`, or `PagingSource` according to the operation.
- Existing DAO query annotations use concatenated multiline SQL strings; preserve the local style.
- Analysis and loan code are actively being developed. Check the worktree before editing and do not revert unrelated in-progress changes.
- The working tree currently carries substantial uncommitted changes on top of `HEAD` (new `:feature:loan` module, reworked `:feature:analysis`, atomic balance/preference logic in `TransactionRepository`, etc.). Run `git status`/`git diff HEAD` before assuming the last commit reflects the current design, and don't revert in-progress work while investigating.
- See "Current status" above for the open feature backlog (loan add/edit UI, categories-at-risk query, debit-card onboarding UI, orphaned `feature/transactions/data/repository` module).
