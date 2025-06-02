# 🏠 KostKita - Smart Boarding House Management

<div align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=material-design&logoColor=white" />
</div>

<div align="center">
  <h3>🌟 Aplikasi Manajemen Kost Modern dengan UI yang Memukau</h3>
  <p>Kelola properti kost Anda dengan mudah, efisien, dan professional</p>
</div>

---

## 🎯 **Tentang KostKita**

KostKita adalah aplikasi manajemen kost berbasis Android yang dibangun dengan teknologi terdepan. Aplikasi ini memungkinkan pemilik kost untuk mengelola penghuni, kamar, dan pembayaran dengan antarmuka yang modern dan intuitif.

### ✨ **Fitur Unggulan**

- 🏡 **Dashboard Interaktif** - Statistik real-time dengan animasi yang menarik
- 👥 **Manajemen Penghuni** - Kelola data penghuni dengan mudah
- 🚪 **Manajemen Kamar** - Monitor status dan detail setiap kamar
- 💰 **Sistem Pembayaran** - Catat dan pantau pembayaran bulanan
- 📊 **Laporan Visual** - Grafik dan chart untuk analisis bisnis
- 🔄 **Sinkronisasi Cloud** - Data tersimpan aman di cloud
- 🎨 **Material Design 3** - UI modern dengan animasi yang smooth

---

## 🛠️ **Teknologi yang Digunakan**

### **Frontend**
- **Jetpack Compose** - UI toolkit modern untuk Android
- **Material Design 3** - Design system terbaru Google
- **Navigation Component** - Navigasi yang type-safe
- **Animated Visibilities** - Animasi yang memukau

### **Architecture**
- **MVVM Pattern** - Arsitektur yang bersih dan terstruktur
- **Clean Architecture** - Separation of concerns yang jelas
- **Repository Pattern** - Abstraksi data layer
- **Dependency Injection** - Hilt untuk DI

### **Data Management**
- **Room Database** - Local storage yang robust
- **Retrofit** - HTTP client untuk API calls
- **Coroutines & Flow** - Asynchronous programming
- **StateFlow** - Reactive state management

### **Libraries**
```kotlin
// UI & Animation
implementation "androidx.compose.animation:animation"
implementation "androidx.compose.material3:material3"

// Architecture
implementation "androidx.lifecycle:lifecycle-viewmodel-compose"
implementation "androidx.navigation:navigation-compose"

// Dependency Injection
implementation "com.google.dagger:hilt-android"
implementation "androidx.hilt:hilt-navigation-compose"

// Database
implementation "androidx.room:room-runtime"
implementation "androidx.room:room-ktx"

// Network
implementation "com.squareup.retrofit2:retrofit"
implementation "com.squareup.retrofit2:converter-gson"
```

---

## 📱 **Screenshots & Demo**

### **Dashboard dengan Animasi Real-time**
- Greeting dinamis berdasarkan waktu
- Statistik animasi dengan spring animation
- Quick actions dengan ripple effects
- Live activity feed dengan timeline

### **Manajemen Penghuni**
- List dengan search dan filter
- Form yang responsive dan validasi
- Expandable cards dengan smooth transition
- Profile avatars dengan gradient

### **Manajemen Kamar**
- Grid dan list view toggle
- Status badges dengan color coding
- Floating action button dengan elevation
- Modal bottom sheets untuk selection

### **Sistem Pembayaran**
- Payment status chips
- Date formatting dengan locale Indonesia
- Currency formatting Rupiah
- Alert dialogs dengan confirmation

---

## 🏗️ **Arsitektur Aplikasi**

```
🏠 KostKita
├── 📱 Presentation Layer
│   ├── 🎨 UI Components (Compose)
│   ├── 🔄 ViewModels (State Management)
│   └── 🧭 Navigation (Type-safe routing)
├── 💼 Domain Layer
│   ├── 📋 Models (Data classes)
│   ├── 📚 Repositories (Interfaces)
│   └── 🎯 Use Cases (Business logic)
└── 💾 Data Layer
    ├── 🏠 Local (Room Database)
    ├── 🌐 Remote (Retrofit API)
    └── 🔄 Mappers (Data transformation)
```

---

## 🚀 **Cara Menjalankan Aplikasi**

### **Prerequisites**
- Android Studio Hedgehog atau terbaru
- JDK 17
- Android SDK 24+ (API level 24)
- Kotlin 1.9.0+

### **Setup Project**
```bash
# Clone repository
git clone https://github.com/yourusername/kostkita.git

# Buka dengan Android Studio
cd kostkita
./gradlew build

# Jalankan aplikasi
./gradlew installDebug
```

### **Konfigurasi Backend**
```kotlin
// Update base URL di NetworkModule.kt
.baseUrl("http://your-api-url:3000/api/")
```

### **Login Default**
```
Username: admin
Password: admin123
```

---

## 📂 **Struktur Project**

```
app/src/main/java/com/example/kostkita/
├── 📱 presentation/
│   ├── screens/
│   │   ├── 🏠 home/         # Dashboard & Home
│   │   ├── 👤 auth/         # Login & Authentication
│   │   ├── 👥 tenant/       # Manajemen Penghuni
│   │   ├── 🚪 room/         # Manajemen Kamar
│   │   └── 💰 payment/      # Sistem Pembayaran
│   ├── navigation/          # Navigation setup
│   └── theme/              # Material Design theming
├── 💼 domain/
│   ├── model/              # Data models
│   └── repository/         # Repository interfaces
├── 💾 data/
│   ├── local/              # Room database
│   ├── remote/             # API services
│   ├── repository/         # Repository implementations
│   └── mapper/             # Data mappers
└── 🔧 di/                  # Dependency Injection modules
```

---

## 🎨 **Design Highlights**

### **Material Design 3 Implementation**
- **Dynamic Color** - Adaptive color schemes
- **Elevation & Shadows** - Proper depth hierarchy
- **Typography Scale** - Consistent text styles
- **Component Tokens** - Design system compliance

### **Animation & Micro-interactions**
- **Spring Animations** - Natural motion curves
- **Staggered Animations** - Sequential item appearances
- **Shared Element Transitions** - Smooth navigation
- **Loading States** - Skeleton screens & progress indicators

### **Responsive Design**
- **Adaptive Layouts** - Works on different screen sizes
- **Touch Targets** - Minimum 48dp touch areas
- **Accessibility** - Content descriptions & focus management
- **Dark Theme** - Automatic theme switching

---

## 👨‍💻 **Tim Pengembang**

<table align="center">
  <tr>
    <td align="center">
      <h3>🏆 DZIKRI SETIAWAN</h3>
      <strong>223040072</strong><br>
      <em>Project Lead & Backend Database</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <h3>💻 CANDRA NUR FARITZI</h3>
      <strong>223040156</strong><br>
      <em>UI/UX Designer</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <h3>📱 EGGY BAGUS HIDAYATULLAH</h3>
      <strong>223040135</strong><br>
      <em>Android Developer & Architecture</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <h3>🎨 MUHAMAD ALFATH SEPTIAN</h3>
      <strong>223040014</strong><br>
      <em>Frontend Developer & Animation</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <h3>🔧 Muhammad Daffa Musyaffa</h3>
      <strong>223040048</strong><br>
      <em>DevOps & Testing Engineer</em>
    </td>
  </tr>
</table>

---

## 🔧 **Fitur Mendatang**

- [ ] 📊 **Advanced Analytics** - Dashboard dengan insights mendalam
- [ ] 🔔 **Push Notifications** - Reminder pembayaran otomatis
- [ ] 📷 **Photo Management** - Upload foto kamar dan penghuni
- [ ] 💳 **Payment Gateway** - Integrasi dengan payment online
- [ ] 🗓️ **Calendar Integration** - Jadwal maintenance dan events
- [ ] 📱 **Mobile App for Tenants** - Aplikasi terpisah untuk penghuni
- [ ] 🏗️ **Multi-property Support** - Kelola multiple kost
- [ ] 📈 **Financial Reports** - Laporan keuangan detail

---

## 📄 **Lisensi**

```
MIT License

Copyright (c) 2024 KostKita Development Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.
```

---

## 🤝 **Kontribusi**

Kami menerima kontribusi dari developer lain! Silakan:

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buka Pull Request

---

## 📞 **Kontak & Support**

<div align="center">
  <p>💬 <strong>Ada pertanyaan atau butuh support?</strong></p>
  <p>📧 Email: kostkita.support@example.com</p>
  <p>🐛 Issues: <a href="https://github.com/yourusername/kostkita/issues">GitHub Issues</a></p>
  <p>📖 Wiki: <a href="https://github.com/yourusername/kostkita/wiki">Project Wiki</a></p>
</div>

---

<div align="center">
  <h3>🌟 Jika project ini membantu, berikan ⭐ ya!</h3>
  <p><strong>Made with ❤️ by KostKita Development Team</strong></p>
  
  <img src="https://img.shields.io/github/stars/yourusername/kostkita?style=social" />
  <img src="https://img.shields.io/github/forks/yourusername/kostkita?style=social" />
  <img src="https://img.shields.io/github/watchers/yourusername/kostkita?style=social" />
</div>
