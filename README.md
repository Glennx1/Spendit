# SpendSplit — Native Android Personal Finance Tracker

**SpendSplit** is a native, modern, 100% offline personal finance and IOU tracking Android application built using Kotlin, Jetpack Compose (Material 3), and Room SQLite persistence. No backend, no login, and no network connections required.

---

## Key Features

### 1. Add Transaction (Smart Main Input Flow)
- **4 Transaction Modes**:
  - **Just my spend**: Standard personal budget expense.
  - **Someone paid for me**: Pick a person, enter amount covered $\rightarrow$ adds to what I owe them.
  - **I paid for someone**: Pick a person, enter amount $\rightarrow$ adds to what they owe me.
  - **Split expense**: Enter total bill, pick a person, 50/50 quick toggle with two-way auto calculation ($Total - MyShare = TheirShare$). "My share" logs as personal spend; "Their share" logs to their IOU balance.
- **Smart Autocomplete & Recurrence Detection**:
  - Live query against past descriptions.
  - Selecting a match auto-fills amount and category from the most recent entry.
  - Automatically flags **Recurring?** toggle as **ON** if 2+ past occurrences exist.
- **Inline Creation**: Add new categories or people without leaving the screen.
- **Interactive Date & Time Pickers**: Edit timestamp or default to now.

### 2. Dashboard
- **Monthly Spend Summary**: Shows total expenditure this month with comparison delta to the previous month (percentage & amount difference).
- **Spend by Category**: Interactive custom Jetpack Compose Donut Chart with touch drill-down filtering.
- **Spend Over Time**: Dynamic custom Bar Chart with Day / Week / Month toggle.
- **Recurring Spends Card**: Displays periodic expenses with average spend, entry count, and last date.
- **Quick Filters**: Category chips and Date filters (This Month, Last 30 Days, All Time) + live text search.
- **Transaction History**: Full list with delete actions.

### 3. People / IOU Tracker
- **Top-level Summary Card**: Displays "You are owed ₹X total", "You owe ₹Y total", and net position.
- **People Ledger**: Running balance for each person (color-coded: green for positive, red for negative, neutral for zero).
- **Drill-down Person History**: Tap any contact to view full chronological transactions with them.
- **One-Tap "Settle Up"**: Zeroes out the balance with a recorded settlement transaction.

### 4. Category Management
- Pre-populated defaults: *Food*, *Travel*, *Rent*, *Utilities*, *Shopping*, *Entertainment*, *Subscriptions*, *Other*.
- Add custom categories with color swatch palette and icon picker.
- Edit categories and delete custom categories (automatically reassigns existing transactions to "Other").
- Real-time spend metrics per category (this month and all-time).

### 5. Settings & Customization
- **Currency Switcher**: Default to `₹` (INR), configurable to `$`, `€`, `£`, `¥`, `₩`, etc.
- **Theme Support**: Follows system theme, with manual Dark and Light mode toggles.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0.0
- **UI**: Jetpack Compose + Material 3
- **Local Database**: Room 2.6.1 (SQLite) with KSP
- **Architecture**: MVVM with Kotlin Coroutines and `StateFlow`
- **Preferences**: Jetpack DataStore Preferences
- **Build System**: Gradle 8.7 with Android Gradle Plugin 8.4.2

---

## Build & Installation Instructions

### Exact Commands to Build the APK

From the project root directory:

#### 1. Build Debug APK (Installable immediately)
```powershell
.\gradlew.bat assembleDebug
```
or use the included helper script:
```powershell
.\build-apk.bat debug
```

**Output APK location**:
```
app\build\outputs\apk\debug\app-debug.apk
```

#### 2. Build Release APK
```powershell
.\gradlew.bat assembleRelease
```
or:
```powershell
.\build-apk.bat release
```

**Output APK location**:
```
app\build\outputs\apk\release\app-release-unsigned.apk
```

---

## How to Install the APK

### Via ADB (USB Debugging / Android Emulator):
```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Direct Transfer:
Copy `app-debug.apk` to your Android device via USB, Google Drive, WhatsApp, or email, open the file on your device, and tap **Install**.
