# Akari (明かり) — gentle pacing for ME/CFS

A native Android energy diary built for symptom-contingent pacing in
Myalgic Encephalomyelitis / Chronic Fatigue Syndrome. The day's energy is a
paper lantern: it glows in the morning and softly dims as energy is spent.
The goal is to stay within the energy envelope and prevent Post-Exertional
Malaise (PEM) — never to do more.

> This is a personal diary, not medical advice or a treatment device.

## What it does

- **Morning intention** — set the day's battery (traffic light + slider) and sleep quality
- **Lantern** — the energy envelope, dimming through the day as activities are logged
- **One-tap activity presets** with costs split across physical / cognitive / emotional effort
- **Rest logged as a deliberate act** ("savasana counts")
- **One-tap PEM flag** — the most valuable data point
- **Trigger detection** — activities most often logged in the 48 h before crashes (PEM is delayed 12–72 h, so the diary reveals what can't be felt)
- **Crash mode** — near-dark screen, four large buttons, silent confirmation
- **Health Connect** — automatic heart rate, resting HR, sleep, and steps from a wearable, plus a heart-rate pacing ceiling (resting + 15 bpm)
- **Manual vitals, food & meds notes, symptom log**
- **CSV export for doctors, JSON backup/restore.** All data stays on the phone — no account, no cloud.

## Tech stack

- Kotlin + Jetpack Compose (Material 3, custom washi/Akari theme)
- Room (offline diary storage)
- Health Connect client (wearable vitals, read-only, optional)
- No network access at all — the app has no INTERNET permission

## Run locally

**Prerequisites:** Android Studio (Ladybug / 2024.2+) with JDK 17.

1. Open Android Studio → **Open** → select this directory.
2. Let Gradle sync (the wrapper is regenerated automatically on first sync;
   from a terminal you can run `gradle wrapper --gradle-version 8.11.1` once
   if you have a system Gradle).
3. Run the **app** configuration on a device (minSdk 27 / Android 8.1+).

No API keys required.

## CI / releases

Every push to `main` builds a debug APK via GitHub Actions and refreshes a
rolling **`latest`** GitHub release with `akari-latest.apk` — same pipeline
as Myceliyum. To install on her phone: open the release page, download the
APK, allow "install unknown apps".

## Publish this repo

```bash
cd akari
git init -b main
git add .
git commit -m "Akari v1 — gentle pacing for ME/CFS"
gh repo create thotsl4yer69/Akari --public --source=. --push
```

The first Actions run will validate the build and publish the APK.

## Health Connect setup (optional, for automatic vitals)

1. Install **Health Connect** from the Play Store (built into Android 14+).
2. Connect the wearable's app (Garmin Connect / Fitbit / Samsung Health…) to Health Connect.
3. In Akari → Settings → **Allow reading wearable data**.
4. Set her resting heart rate in Settings to enable the pacing ceiling.

Community-favourite wearables for ME/CFS pacing: Garmin (Body Battery),
or the Visible app with its Polar armband.
