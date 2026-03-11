# WeatherAware Expense Tracker

WeatherAware Expense Tracker is a modern Android application built with Jetpack Compose that helps users track their spending while automatically capturing the weather conditions and location at the time of each transaction.

## 🌟 Features

- **Expense Tracking**: Add, view, and delete expenses with categories and descriptions.
- **Weather Integration**: Automatically fetches and saves temperature and weather conditions for every expense using OpenWeatherMap API (or similar).
- **Location Awareness**: Uses GPS to tag the location where the expense occurred.
- **Split Bill Utility**: A dedicated tool to split bills among multiple people, including the ability to attach a "Payer Pic" for visual confirmation.
- **Category Management**: Group expenses into categories like Food, Transport, Shopping, Bills, and Investments.
- **Recent Transactions**: Quick access to your latest spending history.
- **Persistent Storage**: All data is securely stored locally using Room Database.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Architecture**: MVVM (Model-View-ViewModel).
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local data persistence.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & Gson for weather API calls.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) for asynchronous image loading.
- **Location**: Google Play Services Location.
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) for seamless screen transitions.

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- Android SDK 24+.
- An API Key for Weather data (OpenWeatherMap).

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/WeatherAwareExpenseTracker.git
   ```
2. Open the project in Android Studio.
3. Sync Project with Gradle Files.
4. Add your Weather API Key in the `MainActivity.kt` or appropriate configuration file.
5. Run the app on an emulator or physical device.

## 📸 Screenshots
 <img width="100" height="100" alt="1000077432" src="https://github.com/user-attachments/assets/a2077a65-a1c3-44bc-8a22-15d378d12194" />
<img width="100" height="100" alt="1000077431" src="https://github.com/user-attachments/assets/c09e4ac6-ca84-4c25-8bff-ac8c2a875714" />
<img width="100" height="100" alt="1000077432" src="https://github.com/user-attachments/assets/1e3b47a8-3eb6-42a2-b54e-dbb2fbcf900d" />

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.




