# ClearCash – Budget Tracker App (Final PoE – Part 3)

## Introduction
ClearCash is a Kotlin-based Android budget tracking application developed for Part 3 (Final App Development). The app allows users to manage expenses, track spending against budget goals, and store all data securely in Firebase Firestore (online database).

---

## Features

### Core Features (Carried over from Part 2)

**User Authentication**
Users register and log in with a username and password. Firebase Authentication handles secure login in the background.

**Category Management**
Users create and manage spending categories (e.g. groceries, transport). Categories are synced to Firestore.

**Expense Entry**
Users add expenses with date, time, description, category, and optional receipt photo. All expenses sync to Firestore.

**Budget Goals**
Users set a minimum and maximum monthly spending goal. These are stored in Firestore and used across the app.

**View Expenses by Period**
Users filter and view their expense list by a selected date range.

**View Category Totals**
The app calculates total spending per category over a selected period.

---

### New Features (Part 3 Only)

**Spending Graph with Min/Max Goals**
The graph screen shows a pie chart of spending per category over a user-selectable period. Below the chart, the app displays the user's minimum and maximum budget goals for the current month, along with a status message showing whether they are on track, over budget, or below their minimum.

**Visual Budget Goal Progress (Rewards Screen)**
The rewards screen shows the user's progress toward three achievement badges based on their spending behaviour relative to their budget goals over the past month. Each badge shows a progress bar and is marked as earned or locked with colour-coded feedback (green = earned, grey = locked).

**Online Database (Firebase Firestore)**
All categories, expenses, and budgets are synced to Firebase Firestore in real time. Data persists across devices and is tied to the user's Firebase account.

---

### Custom Own Features

**Feature 1 – Achievement Badges / Rewards System**
The Rewards screen awards users badges for meeting spending goals. The three badges are: Budget Saver (stayed under max goal), Expense Logger (logged expenses for 7 days), and Smart Spender (spent 30% under budget). Each badge shows a live progress bar so users can see how close they are. This feature encourages healthy financial habits through gamification.

**Feature 2 – Receipt Photo Viewer**
When adding an expense, users can optionally attach a photo of their receipt. The photo is saved locally and viewable from the expense list. This gives users a visual record of each purchase for later verification.

---

## GitHub Actions (Automated Testing)
The repository uses GitHub Actions to automatically build the project and run unit tests on every push and pull request to `main`. The workflow file is located at `.github/workflows/android-ci.yml`.

---

## Online Database Evidence
All data is stored in Firebase Firestore under each user's Firebase UID. The `FirestoreRepository.kt` class handles syncing categories, expenses, and budgets. The `google-services.json` file in the `app/` folder connects the app to the Firebase project.

---

## Demonstration Video
https://youtu.be/LtKQM_OC4hE 

---

## App Icon
The app uses a custom launcher icon (ClearCash logo) available in all mipmap densities: mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi.

---

## References

Karanpuria, R. and Roy, A.S. (2018) *Kotlin Programming Cookbook.* Birmingham, UK: Packt Publishing. Available at: https://search-ebscohost-com.ezproxy.iielearn.ac.za/login.aspx?direct=true&db=e000xww&AN=1699229&site=ehost-live&scope=site [Accessed 20 August 2024].

Smartherd (2019). *Kotlin Android Tutorial #2.1.* [video online] Available at: https://m.youtube.com/watch?v=RpHXPIm9j6s [Accessed 28 April 2026].

Smartherd (2018). *Android Kotlin Tutorial: Apply Material Design Themes #5.4.* [video online] Available at: https://m.youtube.com/watch?v=oShPHVG3c9o [Accessed 27 April 2026].
