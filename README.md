# Patmusic 🎵

A professional offline Android music player with a sleek dark UI.

## Features

- 🎵 **Songs** — Browse all music on your device
- 💿 **Albums** — Grouped by album with artwork
- 🎤 **Artists** — Grouped by artist
- ❤️ **Favorites** — Save your favourite tracks
- 🔀 **Shuffle** — Randomise playback
- 🔁 **Repeat** — Repeat one or all
- 🔔 **Notification Controls** — Play/pause/skip from notification bar
- 🔍 **Search** — Find songs fast

## Build with Codemagic

1. Push this repo to GitHub
2. Connect your GitHub account to [Codemagic](https://codemagic.io)
3. Select the **Patmusic** repository
4. Codemagic will auto-detect `codemagic.yaml` and start building
5. Download the APK from the build artifacts

## Tech Stack

- Kotlin
- MediaPlayer + MediaSession
- Foreground Service
- ViewBinding + LiveData + ViewModel
- Glide for image loading
- Material Design 3 components

## Minimum Requirements

- Android 7.0 (API 24)+
- READ_MEDIA_AUDIO permission
