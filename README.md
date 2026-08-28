# Ownly Apps Dash

Kotlin Multiplatform + Compose Multiplatform dashboard for triggering and monitoring **Firebase App Distribution** GitHub Actions workflows.

**Live (WASM):** https://fir-adece.web.app  
**Repo:** `Anas-AppDev/OwnlyAppsDash`

---

## What this project does

- Trigger GitHub Actions workflows for registered apps (currently **Ownly - Partner App** → `nutanalabs/restaurant-app`)
- Two build modes:
  - **Staging** — fixed quick build (`staging` / `debug` / `apk` on `release`)
  - **Run Configuration** — choose flavor, build type, artifact, and branch
- **Workflow history** for the selected branch (run number, id, display name, date/time, status)
- Filter chips: **All** / **In Progress** / **Completed**
- Blocks a new trigger if that branch already has a queued or in-progress run
- Resumes in-progress runs after refresh (loads history from GitHub)
- Targets: **Android**, **iOS**, **WASM** (web)

---

## Architecture

```
UI (DashMainScreen)
  → ViewModel
    → Use cases (Trigger / Status / List runs)
      → Repository
        → GitHub Actions REST API (Ktor)
```

`GITHUB_TOKEN` is read from `local.properties` at build time and embedded via a generated `GithubSecrets` class. **Do not commit** `local.properties`. Prefer a fine-grained PAT limited to the target repo.

---

## Setup

1. Copy `local.properties.example` → `local.properties`
2. Set `GITHUB_TOKEN=...` (scopes: Actions read/write for the target repo)
3. Sync Gradle / open in Android Studio or Cursor

---

## Run configurations

| Config | What it does |
|--------|----------------|
| **Android Run** | Installs and launches the Android app |
| **WASM Run** | Local Gradle WASM dev server (`wasmJsBrowserDevelopmentRun`) |
| **WASM Hosted** | Opens the deployed site: https://fir-adece.web.app |

iOS: open `iosApp` in Xcode and run the `iosApp` scheme.

---

## Deploy (Firebase Hosting)

Hosting serves the **production WASM** build.

**Project:** `fir-adece`  
**URL:** https://fir-adece.web.app  
**Public dir:** `composeApp/build/dist/wasmJs/productionExecutable`

### Steps

1. Install & log in (use the Google account that owns the Firebase project):

   ```bash
   npm install -g firebase-tools
   firebase login
   ```

2. Build production WASM:

   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```

3. Deploy:

   ```bash
   firebase deploy --only hosting
   ```

4. Open https://fir-adece.web.app (hard-refresh if an old build is cached).

Config files: `firebase.json`, `.firebaserc`.

> The hosted WASM embeds `GITHUB_TOKEN` from the machine that ran the Gradle build. Rebuild + redeploy after changing the token. Keep the PAT tightly scoped before sharing the URL widely.

### Optional custom domain

In Firebase Console → Hosting → **Add custom domain**, then add the DNS records Firebase shows (requires a domain you control).

---

## What’s in place so far

- KMP Compose app shell (Android / iOS / WASM)
- GitHub workflow trigger + status polling
- Branch-scoped workflow history + status filters
- In-progress guard before new triggers
- Pink-themed UI (tabs, dropdowns, history chips)
- Firebase Hosting deploy for WASM
- IDE run configs for Android, local WASM, and hosted WASM

---

## Useful commands

```bash
# Local WASM
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Production WASM → Firebase
./gradlew :composeApp:wasmJsBrowserDistribution
firebase deploy --only hosting

# Android debug APK
./gradlew :composeApp:assembleDebug
```
