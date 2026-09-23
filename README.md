# 🏛️ Pillar Pocket — Budget Tracker App

> *"Nulum gratuitum prandium"* — There is no free lunch.

---

## Table of Contents

1. [Overview](#overview)
2. [Purpose](#purpose)
3. [Features](#features)
4. [Design Considerations](#design-considerations)
5. [Architecture](#architecture)
6. [Technology Stack](#technology-stack)
7. [Project Structure](#project-structure)
8. [Database Schema](#database-schema)
9. [Security](#security)
10. [Gamification](#gamification)
11. [Notifications](#notifications)
12. [Reporting](#reporting)
13. [Getting Started](#getting-started)
14. [Requirements](#requirements)
15. [Known Limitations](#known-limitations)
16. [Future Improvements](#future-improvements)

---

## Overview

**Pillar Pocket** is a personal budget tracking Android application built with modern Android development practices. The name reflects two core ideas: *pillars* as the foundational columns of financial stability, and *pocket* as the everyday place where money is kept and managed. Together, the name captures the app's mission — to give users a solid, reliable foundation for managing their daily finances.

The app is designed to be entirely offline-first, storing all data locally on the device using Room Database. No internet connection is required for any of the app's core features, ensuring that the user's financial data remains private and accessible at all times, regardless of connectivity.

---

## Purpose

Pillar Pocket was built to solve a common and deeply personal problem: most people do not track where their money goes, and as a result, they frequently overspend without realising it until it is too late. Traditional budgeting tools are often too complex, require internet access, or do not provide enough visual feedback to keep users motivated.

Pillar Pocket addresses this by providing:

- A **simple, accessible interface** that makes expense logging fast and frictionless.
- **Visual feedback** through graphs, dashboards, and progress bars so users can see their spending patterns at a glance.
- **Accountability mechanisms** such as budget goals, category breakdowns, and overspending alerts.
- **Gamification elements** that reward consistent financial behaviour and encourage users to maintain healthy spending habits over time.
- **Exportable reports** that allow users to review and share a complete picture of their monthly finances.

The app is targeted at individuals who want a lightweight, private, and rewarding way to manage their personal finances on their Android device.

---

## Features

### 🔐 Authentication
- User registration with username, email address, and password.
- Secure login using locally stored credentials.
- Passwords are hashed using the SHA-256 algorithm before storage — the raw password is never saved.
- Session management using SharedPreferences to persist the logged-in user across app restarts.
- Logout with full back-stack clearing to prevent unauthorised access after signing out.

### 🗂️ Categories
- Create, edit, and delete custom expense categories.
- Each category carries a unique name, a colour (chosen from a curated palette), and a Material Design icon (chosen from a visual icon picker).
- Categories are scoped per user — each account has its own independent set of categories.
- Duplicate category names per user are prevented by a database-level uniqueness check.

### 🧾 Expense Tracking
- Log expense entries with the following required fields:
  - **Amount** (supports up to two decimal places)
  - **Date** (calendar date picker)
  - **Start Time** and **End Time** (24-hour clock pickers)
  - **Description** (free-text label)
  - **Category** (selected from the user's own categories)
- Optionally attach a **receipt photo** to any expense entry, captured via the device camera or selected from the gallery.
- All expenses are stored locally and displayed in a scrollable list ordered by date (newest first).
- A running total of all expenses is displayed at the top of the list.

### 📅 Date Range Filtering
- Filter the expense list by a user-selected start and end date.
- The filtered total updates automatically to reflect only the expenses within the selected period.
- A clear button removes the filter and restores the full list.
- Empty state messages adapt based on whether a filter is active.

### 📸 Receipt Photo Viewer
- Expense entries with an attached photo display a clickable thumbnail in the list.
- Tapping the thumbnail opens a full-screen photo viewer with the expense description shown as a title.
- The viewer can be dismissed by tapping the back arrow.

### 🎯 Budget Goals
- Set a **minimum** and **maximum** monthly spending goal for any month and year.
- Navigate between months using previous/next arrows.
- The Spending Overview card shows:
  - Current monthly total
  - An animated progress bar relative to the maximum goal
  - Minimum and maximum goal labels
  - A status badge: *"Within budget goal ✓"*, *"Below minimum goal"*, or *"Exceeded maximum goal!"*
- If a goal already exists for the selected month, the input fields are pre-filled with the saved values for easy editing.

### 📊 Category Spending
- View a ranked breakdown of spending per category for any user-selected period.
- Each category card shows:
  - The category icon and colour
  - The total amount spent
  - The percentage of total spending
  - An animated, colour-coded progress bar
- Categories are sorted from highest to lowest spending.

### 📈 Spending Graph
- A horizontally scrollable bar chart displays spending per category for the selected month.
- Each bar is colour-coded to match its category's assigned colour.
- Dashed reference lines are drawn across the chart at the minimum goal (amber) and maximum goal (red) levels.
- A summary card above the chart shows the total spent, minimum goal, and maximum goal side by side.
- A legend card below the chart lists goal line colours and each category with its amount and percentage.

### 🏠 Budget Dashboard
- A dedicated dashboard for the current month showing an at-a-glance health overview.
- A **Budget Health Hero Card** changes colour based on status:
  - 🟢 **Green** — spending is within budget
  - 🟡 **Amber** — spending is below the minimum goal
  - 🔴 **Red** — spending has exceeded the maximum goal
  - ⚫ **Grey** — no budget goal has been set
- Quick stat cards show the total spent, remaining budget (or overspent amount), and the number of active categories.
- Each category is listed with an animated progress bar showing its contribution to the maximum goal.
- Categories that are using a high proportion of the maximum goal are **visually highlighted in red** with a ⚠️ warning icon and a red background border.
- An overspending alert banner shows how many categories are flagged.

### 🏅 Badges (Gamification)
Pillar Pocket includes a badge system to reward consistent financial behaviour. Badges are organised into four categories:

| Category | Badges |
|---|---|
| Getting Started | Welcome |
| Expense Logging | First Step, On a Roll, Dedicated Logger, Expense Tracker, Expense Master, Receipt Keeper |
| Organisation | Organised, Category Pro |
| Budget Goals | Goal Setter, On Target, Budget Champion |

- Badges are awarded automatically in the background as the user's data changes.
- Each badge is earned only once per user.
- Newly earned badges show a **teal notification border** on the badge card.
- A **red notification dot** on the Home screen's Badges button shows how many new badges are waiting.
- The dot clears when the user visits the Badges screen.
- Unearned badges are displayed as locked (🔒) in greyscale, giving users a clear target to work towards.
- A progress bar at the top of the Badges screen shows overall completion (e.g. 4/12 earned).

### 🔔 Smart Notifications
Pillar Pocket sends local notifications to keep users informed and consistent:

- **Approaching Budget Limit** — triggered when monthly spending reaches 80% or more of the maximum goal. Shows the percentage used and the remaining amount.
- **Budget Exceeded** — triggered when spending goes over the maximum goal. Shows the overspent amount.
- **Consistency Reminder** — triggered when no expenses have been logged for 3 or more days.

Notifications run via a background **WorkManager** periodic task that checks daily, even when the app is closed. Tapping any notification opens the app directly. The daily check stops when the user logs out, and resumes when they log back in.

### 📄 Monthly Expense Summary Report
- Generate a formatted **PDF report** for any selected month.
- The report includes:
  - A branded green header with the report period and generation date
  - A spending summary showing total spent, transaction count, and budget goal comparison
  - A category breakdown table with amounts, percentages, and percentage of maximum goal
  - A full expense entries table sorted by date (newest first)
  - A colour-coded total row at the bottom
  - A footer with the generation date and page number
- The PDF is generated entirely on-device using Android's built-in `PdfDocument` API — no external libraries or internet connection required.
- Once generated, the report can be shared via any installed app (WhatsApp, Gmail, Google Drive, etc.) using Android's native share sheet.

### 🌅 Splash Screen
- A branded splash screen appears briefly when the app is cold-started.
- Built using the official AndroidX Core SplashScreen API for compatibility across all supported Android versions.
- Shows the Pillar Pocket logo (white pillar and pocket design) centred on the brand green background.
- Transitions smoothly into the Login screen.

---

## Design Considerations

### Colour Palette
The app uses a consistent, purposeful colour system:

| Token | Hex | Usage |
|---|---|---|
| Pillar Green | `#2E7D32` | Primary brand colour — buttons, app bars, highlights |
| Pillar Green Light | `#4CAF50` | Dark theme primary, hover states |
| Pillar Green Pale | `#E8F5E9` | Backgrounds, subtle highlights, dashboard card |
| Pillar Accent | `#00BFA5` | Teal accent — receipt badges, new badge borders |
| Pillar Red | `#D32F2F` | Errors, overspending, delete actions |
| Pillar Red Light | `#EF9A9A` | Dark theme error states |
| Pillar Amber | `#F9A825` | Warnings — below minimum goal, minimum goal line |
| Pillar Surface | `#F5F5F5` | Screen backgrounds |
| Pillar On Surface | `#1C1C1C` | Primary text |
| Pillar Grey | `#9E9E9E` | Secondary text, empty states, inactive icons |

The colour system was chosen to give green a strong, trustworthy association with financial health, while red and amber carry their universally understood meanings of danger and caution respectively.

### Typography & Formatting
- The app uses Material 3's default typography system.
- Monetary amounts are consistently formatted with the South African Rand prefix (`R`) and two decimal places (e.g. `R 1 250.00`).
- Dates are stored internally in `yyyy-MM-dd` format (enabling correct lexicographic sorting in SQLite) and displayed to the user in `dd MMM yyyy` format (e.g. `28 Apr 2026`).
- Times are stored and displayed in 24-hour `HH:mm` format.

### UI & UX Principles
- **Offline-first**: every feature works without an internet connection.
- **Reactive UI**: all screens observe `Flow`-based data streams, meaning the UI automatically updates whenever the underlying data changes — no manual refresh is needed.
- **Empty states**: every list screen has a meaningful empty state with an icon, a primary message, and a helpful suggestion.
- **Confirmation dialogs**: destructive actions (delete category, log out) always require a confirmation dialog before proceeding.
- **Validation**: all input forms validate fields before saving and display inline error messages in red.
- **Loading states**: long-running operations (saving, generating PDF) show a circular progress indicator in the action button.
- **Progress animations**: all progress bars use animated transitions (`animateFloatAsState`) for a polished feel.
- **Adaptive icons**: the launcher icon is built as an Android adaptive icon (foreground + background layers) so it renders correctly across all device manufacturers and launcher shapes.

### Navigation
The app uses **Jetpack Navigation Compose** with a single `NavGraph` that manages the full navigation stack. Key navigation decisions:

- After login or registration, the back stack is cleared so the user cannot navigate back to the login screen while authenticated.
- After logout, the entire back stack is cleared (`popUpTo(0)`) so the user cannot navigate back to any screen that requires authentication.
- The `userId` is passed through the navigation route as a path argument so each screen always operates on the correct user's data, even in a multi-user setup.

---

## Architecture

Pillar Pocket follows **MVVM (Model-View-ViewModel)** architecture with a clean separation of concerns across three main layers:

```
UI Layer        →   Composable screens observe StateFlow from ViewModels
ViewModel Layer →   ViewModels hold UI state and call Repository functions
Data Layer      →   Repositories abstract DAOs; DAOs talk to Room Database
```

### Layer responsibilities

**Data Layer**
- `Entity` classes define the database table schema.
- `DAO` interfaces define all SQL queries using Room annotations.
- `Repository` classes wrap DAOs, add business validation logic, and expose clean `Flow`-based APIs to the ViewModel layer.

**ViewModel Layer**
- One ViewModel per major screen.
- ViewModels use `viewModelScope` to launch coroutines and collect Flows.
- UI state is exposed as `StateFlow` objects.
- `ViewModelFactory` classes are used for manual dependency injection (no DI framework).

**UI Layer**
- All screens are `@Composable` functions.
- Screens collect state using `collectAsStateWithLifecycle()` for lifecycle-aware observation.
- UI events flow upward via lambda callbacks; state flows downward via `StateFlow`.
- Navigation callbacks are passed in as parameters so screens have no direct dependency on the `NavController`.

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| Local Database | Room (SQLite) |
| Async / Reactive | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| Image Loading | Coil (for receipt photo thumbnails) |
| Background Tasks | WorkManager |
| Notifications | AndroidX Core NotificationCompat |
| PDF Generation | Android PdfDocument API (built-in) |
| Splash Screen | AndroidX Core SplashScreen |
| Build System | Gradle (Kotlin DSL) |
| Minimum SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

---

## Project Structure

```
com.example.pillarpocket/
├── data/
│   ├── local/
│   │   ├── BadgeType.kt            ← Enum of all badge definitions
│   │   ├── BudgetGoal.kt           ← Budget goal entity
│   │   ├── BudgetGoalDao.kt        ← Budget goal queries
│   │   ├── Category.kt             ← Category entity
│   │   ├── CategoryDao.kt          ← Category CRUD queries
│   │   ├── EarnedBadge.kt          ← Earned badge entity
│   │   ├── EarnedBadgeDao.kt       ← Badge queries
│   │   ├── Expense.kt              ← Expense entity
│   │   ├── ExpenseDao.kt           ← Expense queries
│   │   ├── PillarPocketDatabase.kt ← Room database singleton
│   │   └── User.kt                 ← User entity
│   ├── preferences/
│   │   └── UserPreferences.kt      ← SharedPreferences wrapper
│   └── repository/
│       ├── BadgeRepository.kt
│       ├── BudgetGoalRepository.kt
│       ├── CategoryRepository.kt
│       ├── ExpenseRepository.kt
│       └── UserRepository.kt
├── notifications/
│   └── NotificationHelper.kt       ← Notification channel & display logic
├── ui/
│   ├── components/
│   │   ├── CategoryIconPicker.kt   ← Icon + colour picker composables
│   │   ├── DateTimePickers.kt      ← Date & time picker dialogs
│   │   └── SpendingBarChart.kt     ← Custom Canvas bar chart
│   ├── navigation/
│   │   └── NavGraph.kt             ← Full navigation graph & route definitions
│   ├── screens/
│   │   ├── AddExpenseScreen.kt
│   │   ├── BadgesScreen.kt
│   │   ├── BudgetGoalScreen.kt
│   │   ├── CategorySpendingScreen.kt
│   │   ├── CategoriesScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── ExpensesScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MonthlyReportScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── SpendingGraphScreen.kt
│   └── theme/
│       ├── Color.kt                ← Brand colour tokens
│       ├── Theme.kt                ← Light/dark Material 3 theme
│       └── Type.kt                 ← Typography
├── utils/
│   └── PdfReportGenerator.kt       ← PDF generation logic
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── AuthViewModelFactory.kt
│   ├── BadgeViewModel.kt
│   ├── BadgeViewModelFactory.kt
│   ├── BudgetGoalViewModel.kt
│   ├── BudgetGoalViewModelFactory.kt
│   ├── CategorySpendingViewModel.kt
│   ├── CategorySpendingViewModelFactory.kt
│   ├── CategoryViewModel.kt
│   ├── CategoryViewModelFactory.kt
│   ├── DashboardViewModel.kt
│   ├── DashboardViewModelFactory.kt
│   ├── ExpenseViewModel.kt
│   ├── ExpenseViewModelFactory.kt
│   ├── ReportViewModel.kt
│   ├── ReportViewModelFactory.kt
│   ├── SpendingGraphViewModel.kt
│   └── SpendingGraphViewModelFactory.kt
├── workers/
│   └── SpendingCheckWorker.kt      ← Background WorkManager task
├── MainActivity.kt
└── PillarPocketApp.kt              ← Application class & repository wiring
```

---

## Database Schema

Pillar Pocket uses a Room (SQLite) database named `pillar_pocket_database` with the following tables:

### `users`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| username | TEXT | Unique per app |
| email | TEXT | Validated email format |
| passwordHash | TEXT | SHA-256 hash of password |

### `categories`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| userId | INTEGER | Foreign reference to users.id |
| name | TEXT | Unique per user |
| colorHex | TEXT | e.g. `#E53935` |
| iconName | TEXT | Maps to Material icon |

### `expenses`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| userId | INTEGER | Foreign reference to users.id |
| categoryId | INTEGER | Foreign reference to categories.id |
| amount | REAL | Must be > 0 |
| date | TEXT | `yyyy-MM-dd` format |
| startTime | TEXT | `HH:mm` format |
| endTime | TEXT | `HH:mm` format |
| description | TEXT | Cannot be blank |
| photoUri | TEXT | Nullable — local URI string |
| createdAt | INTEGER | Unix timestamp in ms |

### `budget_goals`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| userId | INTEGER | Foreign reference to users.id |
| month | INTEGER | 1–12 |
| year | INTEGER | e.g. 2026 |
| minimumGoal | REAL | Must be > 0 and < maximumGoal |
| maximumGoal | REAL | Must be > minimumGoal |

*Unique index on (userId, month, year) — one goal per user per month.*

### `earned_badges`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| userId | INTEGER | Foreign reference to users.id |
| badgeType | TEXT | Enum name from BadgeType |
| earnedAt | INTEGER | Unix timestamp in ms |
| isNew | INTEGER | 1 = unread, 0 = seen |

*Unique index on (userId, badgeType) — each badge earned only once per user.*

---

## Security

| Concern | Approach |
|---|---|
| Password storage | SHA-256 hashed before insertion — raw password never persisted |
| Multi-user isolation | All queries scoped to `userId` — users cannot access each other's data |
| Camera permission | Declared as optional (`required="false"`) — app works without a camera |
| FileProvider | Receipt photos are shared via `FileProvider` — no direct file URI exposure |
| Notification permission | Requested at runtime on Android 13+ (`POST_NOTIFICATIONS`) |
| Session persistence | `userId` stored in SharedPreferences — cleared on logout |

> **Note:** This application is designed for personal single-device use. It does not implement network-level security, end-to-end encryption, or server-side authentication. For a production multi-user system, these concerns would need to be addressed.

---

## Gamification

The badge system in Pillar Pocket is designed around the principle of **progressive reinforcement** — rewarding the user not just for big achievements but for small, consistent actions that build good financial habits over time.

### Design rationale
- **Immediate reward** — the Welcome badge is earned the moment a user registers, giving instant positive feedback.
- **Progressive milestones** — expense logging badges scale from 1 → 5 → 10 → 25 → 50, so users are rewarded early and often.
- **Behaviour diversity** — badges span multiple feature areas (logging, organisation, goal-setting) to encourage broad engagement with the app.
- **Visibility** — locked badges are always visible alongside earned ones, acting as a persistent motivational prompt.
- **Notification** — the red dot on the Home screen creates a low-friction discovery moment for newly earned badges.

---

## Notifications

The notification system is built on two components:

1. **`NotificationHelper`** — a singleton object that manages channel creation and provides named methods for each notification type. This keeps all notification logic in one place and makes it easy to adjust messaging or add new types.

2. **`SpendingCheckWorker`** — a `CoroutineWorker` scheduled via WorkManager to run once per day. It retrieves the current user's spending data from the local database and fires the appropriate notification based on the conditions found.

### Notification channels
| Channel ID | Name | Importance | Covers |
|---|---|---|---|
| `pillar_budget_alerts` | Budget Alerts | HIGH | Approaching & exceeded budget |
| `pillar_consistency_reminders` | Consistency Reminders | DEFAULT | 3+ days without logging |

---

## Reporting

The PDF report is generated entirely on-device using Android's built-in `android.graphics.pdf.PdfDocument` API — no internet connection or external library is required.

The generator (`PdfReportGenerator`) uses a manual page-break system that tracks the current Y position as content is drawn and automatically starts a new page when the remaining space is insufficient. This allows the report to scale to any number of expense entries without overflowing a single page.

The generated PDF is written to the app's `cacheDir` and shared via Android's `Intent.ACTION_SEND` with a `FileProvider` URI, allowing the user to send it to any installed app that can handle PDF files.

---

## Getting Started

### Prerequisites
- Android Studio (Hedgehog or later recommended)
- JDK 11
- Android device or emulator running API 26 or higher

### Setup
1. Clone or download the project
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Connect a physical Android device via USB (with USB Debugging enabled) or start an emulator
5. Click ▶️ **Run** to build and deploy the app

### First run
1. Tap **Sign Up** on the login screen
2. Enter a username (minimum 3 characters), a valid email, and a password (minimum 6 characters)
3. Create at least one **Category** before logging expenses
4. Set a **Budget Goal** for the current month to unlock the full dashboard experience

---

## Requirements

| Requirement | Minimum |
|---|---|
| Android OS | 8.0 (API 26) |
| RAM | 2 GB (recommended) |
| Storage | ~50 MB (app + database) |
| Camera | Optional (for receipt photos) |
| Internet | Not required |

---

## Known Limitations

- **Single device only** — data is stored locally and does not sync across devices.
- **No data export beyond PDF** — there is no CSV or spreadsheet export option.
- **No recurring expenses** — expenses must be logged individually each time.
- **No currency selector** — the app is fixed to South African Rand (R).
- **No budget categories** — the budget goal applies to total spending, not per-category limits.
- **`fallbackToDestructiveMigration`** — the database currently uses destructive migration during development. This means a schema change wipes all data. This must be replaced with proper Room migrations before a production release.

---

## Future Improvements

- ☁️ **Cloud sync** — optional backup and sync via Firebase or Google Drive
- 💱 **Multi-currency support** — allow users to set their preferred currency
- 📆 **Recurring expenses** — schedule repeating expense entries (e.g. monthly subscriptions)
- 🎯 **Per-category budget limits** — set individual spending caps per category, not just a global total
- 📊 **Yearly overview** — a 12-month bar chart showing spending trends across the full year
- 🔍 **Expense search** — full-text search across expense descriptions
- 🌙 **Dark mode** — a fully styled dark theme (the colour tokens already support it)
- 🔒 **Biometric lock** — fingerprint or face authentication to open the app
- 📤 **CSV export** — export expense data as a spreadsheet-compatible file
- 🏦 **Multiple accounts** — manage finances for more than one person on the same device

---

## License

This project was developed as a personal Android development learning project. All code is original and written from scratch using publicly documented Android APIs and Jetpack libraries.

---

*Built with care using Kotlin, Jetpack Compose, and Room Database.*
*Pillar Pocket — Your money, managed.*
