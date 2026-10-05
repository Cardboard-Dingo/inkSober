# Project Plan

Create a sobriety tracker Android app named inkSober specifically tailored for the Mudita Kompact phone running open-source Android 12 on an e-ink display (800 x 480 resolution).

Key requirements:
1. Target Device & Display: Mudita Kompact (E-ink, 800x480 resolution, Android 12). Pure black and white / grayscale high-contrast UI designed specifically for e-ink refresh rates (no fluid animations, sharp borders, clean natural typography).
2. Substance/Habit Tracking: Support tracking multiple substances or habits to be sober from, with start date and time for each.
3. Milestones & Progress: Display total days, hours, and milestone achievements (1 day, 1 week, 30 days, 90 days, 1 year, etc.).
4. Daily Journal / Notes: Offline daily note-taking and mood/reflection logging linked to dates.
5. Daily Inspirational Recovery Quotes: Embedded offline database of daily recovery quotes.
6. Share Milestones: Share milestone summaries or progress via system share sheet (SMS, Email, messaging apps).
7. Offline First: Completely offline app, no account required, all data saved locally in Room/DataStore.
8. Visual Design & Icon: Natural feel, black and white aesthetic, custom launcher icon based on provided Tree of Life symbol with roots, sun, and compass.

## Project Brief

# Project Brief: inkSober

## Description
**inkSober** is an offline-first sobriety and habit tracking application designed specifically for the **Mudita Kompact** phone (running Android 12 on an 800 x 480 E-Ink display). The app features a high-contrast black-and-white aesthetic optimized for E-Ink refresh rates with zero fluid animations, sharp typography, and absolute local data privacy.

---

## Features

1. **Substance & Habit Tracking with Milestones**: Track multiple substances or habits with precise start dates and times. Displays elapsed time (days, hours) and tracks milestone progress (1 day, 1 week, 30 days, 90 days, 1 year, etc.).
2. **E-Ink High-Contrast B&W UI**: Pure black, white, and high-contrast monochrome design system optimized for the Mudita Kompact 800x480 screen to minimize ghosting and refresh delay.
3. **Daily Journal & Reflection Logging**: Completely offline daily note-taking and mood/reflection logging linked to specific dates.
4. **Offline Recovery Quotes & Milestone Sharing**: Embedded offline daily recovery quotes and simple progress sharing via the native Android system share sheet (SMS, Messaging, Email).

---

## High-Level Tech Stack

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (High-contrast, non-animated E-Ink theme)
* **Navigation**: Jetpack Navigation 3 (state-driven)
* **Adaptive Strategy**: Compose Material Adaptive library
* **Concurrency & Async Data**: Kotlin Coroutines & Flow
* **Architecture**: MVVM with Unidirectional Data Flow (UDF)
* **Local Persistence**: Room Database (for substances, journal entries, and embedded quotes) & Jetpack DataStore (for user settings)

## Implementation Steps
**Total Duration:** 21m 6s

### Task_1_DataAndDomainLayer: Set up Room database entities (Substance, JournalEntry, RecoveryQuote), DAOs, DataStore for app settings, and Repository layer with Coroutines/Flow.
- **Status:** COMPLETED
- **Updates:** Successfully implemented Room DB entities, DAOs, pre-seeded recovery quotes, DataStore preferences, and Repository layer with milestone calculations. Unit tests passed and debug build succeeded cleanly.
- **Acceptance Criteria:**
  - Room DB schema defined for substances, journal entries, and recovery quotes
  - DAOs and Repository interfaces/implementations created
  - DataStore setup for user preferences
  - build pass
- **Duration:** 5m 12s

### Task_2_UIAndFeatures: Implement high-contrast B&W E-Ink Compose theme, navigation structure, Substance Tracking UI with milestone calculation and native share sheet integration, Daily Journal UI, and Daily Quotes screen.
- **Status:** COMPLETED
- **Updates:** Implemented high-contrast B&W E-Ink Compose theme with zero transition animations, Dashboard with real-time counter and milestone list, Native Android share sheet integration, Substance management screen, Daily Journal screen, Inspiration Quotes gallery, and Jetpack Navigation 3 shell. Build and tests passed with 100% success.
- **Acceptance Criteria:**
  - High contrast B&W Compose theme with zero animations for E-Ink screen created
  - Substance tracker screen displays elapsed time and milestones with option to add/reset substances
  - Milestone sharing via native Android share sheet working
  - Daily journal and reflection screen for date-linked entries implemented
  - Offline daily recovery quotes displayed on dashboard
  - build pass
- **Duration:** 6m 24s

### Task_3_AppIconAndAssets: Generate tree-of-life custom launcher icon and finalize E-Ink layout visual polish.
- **Status:** COMPLETED
- **Updates:** Created Tree of Life adaptive launcher icon and vector branding assets, configured AndroidManifest.xml, integrated Tree of Life header emblem, and optimized touch targets and layout sizing for 800x480 E-Ink resolution. Build and unit tests passed cleanly.
- **Acceptance Criteria:**
  - Tree-of-life adaptive launcher icon configured in AndroidManifest.xml
  - Monochrome app assets and launcher icon verified
  - build pass
- **Duration:** 4m 5s

### Task_4_RunAndVerify: Run and verify application stability and features. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Verified application stability, full feature set (substance tracking, milestone progress, journal logging, offline quotes gallery, native share sheet, and E-Ink B&W theme) on emulator with 0 crashes.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - verified alignment with user requirements
- **Duration:** 5m 25s

