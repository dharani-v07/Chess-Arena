<div align="center">
  <h1>♟️ Chess Arena</h1>
  <p>A modern Android chess application built with Kotlin and Jetpack Compose</p>
</div>

## 📱 About

Chess Arena is a full-featured chess application for Android, built with modern Android development practices including Jetpack Compose, Kotlin Coroutines, and Room database.

## ✨ Features

- **Modern UI**: Built with Jetpack Compose for a smooth, responsive interface
- **Chess Gameplay**: Complete chess game logic and rules
- **AI Integration**: Powered by Google Gemini AI for intelligent moves
- **Local Storage**: Room database for game history and preferences
- **Responsive Design**: Material Design 3 with adaptive layouts

## 📸 Screenshots

<!-- Add your screenshots here -->
<div align="center">
  <img src="https://github.com/dharani-v07/Chess-Arena/tree/main/screenshots" alt="Gameplay & Main Screen" width="300">
</div>

## 🚀 Download

Get the latest release APK from [GitHub Releases](../../releases)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines
- **Dependency Injection**: Manual DI
- **Build System**: Gradle with Kotlin DSL
- **AI**: Google Gemini API

## 📋 Prerequisites

- [Android Studio](https://developer.android.com/studio) Koala or later
- Android SDK API 36
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36

## 🏃 Run Locally

1. Clone the repository:
```bash
git clone https://github.com/dharani-v07/Chess-Arena.git
cd Chess-Arena
```

2. Open Android Studio and select the project directory

3. Allow Android Studio to sync the project

4. Create a `.env` file in the project root:
```env
GEMINI_API_KEY=your_api_key_here
```
(See `.env.example` for reference)

5. Build and run the app on an emulator or physical device

## 🔧 Build Release APK

To build a release APK:

```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## 📝 Configuration

### API Keys
The app uses Google Gemini API for AI-powered features. Set your API key in the `.env` file:

```env
GEMINI_API_KEY=your_gemini_api_key
```

### Signing
For release builds, configure signing in `app/build.gradle.kts` or set environment variables:
- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Google for Gemini AI
- Android team for Jetpack Compose
- Chess community for game logic resources

## 📞 Contact

Dharani V - [@dharani-v07](https://github.com/dharani-v07)

---

<div align="center">
  Made with ❤️ by Dharani V
</div>
