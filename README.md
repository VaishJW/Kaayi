[]!(screenshots/header.png)

A local-first Android app for splitting expenses with friends — no server, no account, no data leaving your device.

## Features

- **Per-friend tabs** — open a running tab with anyone, tracked independently
- **Transaction logging** — log what you paid or what they paid, with optional categories (Food, Travel, Shopping, etc.)
- **UPI pay shortcut** — one-tap redirect to your UPI app (GPay/PhonePe/etc.) with the amount and payee pre-filled; you confirm payment manually, no fragile auto-detection
- **QR-based friend adding** — scan a UPI QR code to auto-fill a friend's name and VPA instead of typing it out
- **Split Bill** — split a shared expense across multiple friends at once, equal or custom shares, tracked separately from personal tabs
- **Categories dashboard** — monthly breakdown of spending by category, with totals and a sortable transaction list
- **Profile / Friends list** — friends persist even after a tab is deleted, so your contact list stays intact
- **New-tab notifications** — get notified the moment a new tab is opened with someone
- **Default categories per friend** — optionally pre-fill a category for recurring payments to the same person (e.g., a roommate → Utilities)

## Why local-only

Tab Splitter deliberately has no backend. All data lives in a local Room database on your device. This means:
- No account creation, no login
- No data ever leaves your phone
- Payment confirmation is manual (tap "Mark as paid" yourself) rather than relying on unreliable auto-detection from UPI app callbacks

## Tech stack

- **Kotlin** + **Jetpack Compose** for UI
- **Room** for local persistence
- **ML Kit / CameraX** for UPI QR scanning
- **Material 3** theming with a custom color scheme and typography

## Status

v1.0 — feature-complete for personal use. Built iteratively, screen by screen, using Google Antigravity as the primary development tool.


## License
