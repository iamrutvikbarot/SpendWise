# 💎 SpendWise — Smart Financial Vault & AI Expense Tracker

<div align="center">

![SpendWise Banner](https://img.shields.io/badge/SpendWise-Smart%20Financial%20Vault-10B981?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

</div>

## 🌟 Overview
**SpendWise** is a sleek, modern financial management application crafted with **Jetpack Compose** and **Material Design 3**. Designed with a deep frosted slate aesthetic, fluid animations, and real-time on-device processing, SpendWise turns daily expense tracking into an effortless and visually delightful experience.

---

## ✨ Features (For Website Showcase)

### 🔐 1. Authentication & Privacy-First Guest Mode
- **Google Sign-In**: Secure 1-tap login with Google. Automatically syncs your Google profile picture and name to personalize the dashboard.
- **Offline Guest Mode**: Privacy-first approach! Users can choose "Continue as Guest" to use the app entirely offline with no account required.
- **Session Management**: Persistent sessions managed securely via Android DataStore.

### 💰 2. Smart Dashboard & Analytics
- **Live Net Balance, Inflow & Outflow**: Real-time totals computed dynamically from your local financial records.
- **Privacy Toggle**: A discrete "Eye" icon to hide your total balance from prying eyes (displays as ••••••).
- **Recent Transactions Timeline**: View recent spending activities with custom category avatars, color-coded badges, and native horizontal slide animations.

### 📸 3. AI-Powered Receipt Scanner (Gemini Integration)
- **Automated Expense Entry**: No more manual typing! Take a photo of a receipt, and the embedded AI automatically extracts the Title, Amount, Date, and infers the correct Category.
- **OS Intent Integration ("Share to SpendWise")**: Seamlessly share receipt images from your phone's Gallery, WhatsApp, or any other app directly to SpendWise to instantly scan and log the expense.

### 🗓️ 4. Comprehensive Transaction Management
- **Detailed Tracking**: Log income and expenses with detailed fields: categories (Food, Transport, Bills, etc.), multiple payment methods (UPI, Card, Cash), timestamps, and optional notes.
- **Clean Chronological Ledger**: See all transactions neatly grouped in a timeline.
- **Interactive Detail Sheets**: Tap any item to view its complete ledger breakdown in a beautiful translucent bottom sheet, complete with directional badges (Money In/Money Out).
- **Smart Validation**: The "Save" button intelligently color-codes itself (Green for Income, Red for Expense) and only enables when a valid amount is entered.

### ☁️ 5. Cloud Sync & Local Persistence
- **On-Device SQLite (Room DB)**: Fast, reliable, offline-first data architecture. The app runs instantaneously without internet.
- **Auto Google Drive Backup**: Transactions are silently backed up to your personal Google Drive in the background. If you reinstall or change devices, your data restores instantly upon signing back in!
- **Smart Fallback**: Cloud sync intelligently disables itself when in Guest Mode to preserve privacy.

### 🎨 6. Premium UI & Adaptive Design
- **Material Design 3 & Glassmorphism**: Stunning UI featuring translucent frosted-glass navigation bars and floating elements.
- **Adaptive Dark & Light Mode**: The UI automatically shifts between a clean white aesthetic and a deep charcoal dark mode, dynamically adjusting gradients and text contrast for perfect readability in any lighting.
- **Smooth Native Animations**: Fluid sliding page transitions and responsive button ripples that feel at home on modern Android.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3 with custom Glassmorphism/Dark Slate theme)
- **Local Persistence**: Room Database (SQLite) + Jetpack DataStore Preferences
- **Cloud/Backend**: Google Drive API for silent app-data backup.
- **AI/ML**: Gemini API Engine for intelligent OCR extraction.
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Architecture Repository pattern.
- **Concurrency**: Kotlin Coroutines & Flow.

---
<div align="center">
Crafted for modern Android devices.
</div>
