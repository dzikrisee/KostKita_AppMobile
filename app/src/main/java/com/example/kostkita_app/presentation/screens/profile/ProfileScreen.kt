package com.example.kostkita_app.presentation.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.kostkita_app.domain.model.User
import com.example.kostkita_app.presentation.navigation.KostKitaScreens
import kotlinx.coroutines.launch
import java.io.File

// Modern Color Palette - matching HomeScreen
private val PrimaryColor = Color(0xFFB8A491) // Soft beige
private val SecondaryColor = Color(0xFFF5B041) // Warm orange
private val AccentColor = Color(0xFF8B7355) // Darker beige
private val SurfaceColor = Color(0xFFFAF8F5) // Light cream
private val OnSurfaceColor = Color(0xFF3C3C3C) // Dark gray
private val SuccessColor = Color(0xFF27AE60) // Fresh green
private val WarningColor = Color(0xFFF39C12) // Warm orange
private val ErrorColor = Color(0xFFE74C3C) // Soft red
private val InfoColor = Color(0xFF3498DB) // Sky blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        if (logoutState is LogoutState.Success) {
            navController.navigate(KostKitaScreens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    // FIXED: Listen untuk update dari EditProfile dengan proper refresh
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry.value) {
        navBackStackEntry.value?.let { entry ->
            if (entry.destination.route == KostKitaScreens.Profile.route) {
                // Check jika ada flag profile_updated dari EditProfile
                val profileUpdated = entry.savedStateHandle.get<Boolean>("profile_updated")
                if (profileUpdated == true) {
                    android.util.Log.d("ProfileScreen", "=== PROFILE UPDATE DETECTED ===")
                    viewModel.forceRefreshAfterUpdate()
                    entry.savedStateHandle.remove<Boolean>("profile_updated")
                }
            }
        }
    }

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil Saya",
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        try {
                            navController.navigateUp()
                        } catch (e: Exception) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryColor.copy(alpha = 0.05f),
                            SurfaceColor,
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Header
                user?.let { currentUser ->
                    ModernProfileHeader(user = currentUser)
                }

                // Quick Stats
                ModernQuickStats()

                // Account Section
                ModernProfileSection(
                    title = "Akun & Keamanan",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profil",
                            subtitle = "Ubah informasi profil Anda",
                            onClick = {
                                try {
                                    navController.navigate(KostKitaScreens.EditProfile.route)
                                } catch (e: Exception) {
                                    // Handle error silently
                                }
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Lock,
                            title = "Ubah Password",
                            subtitle = "Perbarui kata sandi akun",
                            onClick = {
                                try {
                                    navController.navigate(KostKitaScreens.ChangePassword.route)
                                } catch (e: Exception) {
                                    // Handle error silently
                                }
                            }
                        )
                    )
                )

                // App Settings Section
                ModernProfileSection(
                    title = "Pengaturan Aplikasi",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifikasi",
                            subtitle = "Kelola preferensi notifikasi",
                            onClick = { /* TODO */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Language,
                            title = "Bahasa",
                            subtitle = "Indonesia",
                            onClick = { /* TODO */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Palette,
                            title = "Tema",
                            subtitle = "Tampilan aplikasi",
                            onClick = { /* TODO */ }
                        )
                    )
                )

                // Support Section
                ModernProfileSection(
                    title = "Dukungan",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Help,
                            title = "Bantuan & FAQ",
                            subtitle = "Pusat bantuan pengguna",
                            onClick = { /* TODO */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Info,
                            title = "Tentang KostKita",
                            subtitle = "Versi 1.0.0",
                            onClick = { /* TODO */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Star,
                            title = "Beri Rating",
                            subtitle = "Nilai aplikasi di Play Store",
                            onClick = { /* TODO */ }
                        )
                    )
                )

                // Logout Button
                ModernLogoutButton(
                    onClick = { showLogoutDialog = true }
                )
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            ModernLogoutDialog(
                onConfirm = {
                    scope.launch {
                        viewModel.logout()
                    }
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}

@Composable
private fun ModernProfileHeader(user: User) {
    // FIXED: Debug logging untuk trace foto profil
    LaunchedEffect(user.profilePhoto) {
        android.util.Log.d("ProfileScreen", "=== PROFILE PHOTO DEBUG ===")
        android.util.Log.d("ProfileScreen", "User: ${user.username}")
        android.util.Log.d("ProfileScreen", "Profile Photo: ${user.profilePhoto}")
        android.util.Log.d("ProfileScreen", "Photo is empty: ${user.profilePhoto.isNullOrEmpty()}")

        // Check file existence if it's a file path
        if (!user.profilePhoto.isNullOrEmpty() && user.profilePhoto.startsWith("/")) {
            val file = File(user.profilePhoto)
            android.util.Log.d("ProfileScreen", "File exists: ${file.exists()}")
            android.util.Log.d("ProfileScreen", "File readable: ${file.canRead()}")
            android.util.Log.d("ProfileScreen", "File size: ${file.length()} bytes")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FIXED: Avatar dengan proper photo handling
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                ProfilePhotoImage(
                    photoPath = user.profilePhoto,
                    fullName = user.fullName
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info dengan styling modern
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )

            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentColor,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role Badge dengan gradient
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SecondaryColor.copy(alpha = 0.2f),
                                PrimaryColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Text(
                    text = "🏆 ${user.role.uppercase()}",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor
                )
            }
        }
    }
}

// FIXED: Komponen terpisah untuk handle foto profil
@Composable
private fun ProfilePhotoImage(
    photoPath: String?,
    fullName: String
) {
    var imageLoadState by remember { mutableStateOf("loading") }
    var shouldShowDefault by remember { mutableStateOf(false) }

    // Reset state when photoPath changes
    LaunchedEffect(photoPath) {
        android.util.Log.d("ProfilePhoto", "=== LOADING PHOTO ===")
        android.util.Log.d("ProfilePhoto", "Path: $photoPath")
        shouldShowDefault = false
        imageLoadState = "loading"
    }

    if (!photoPath.isNullOrEmpty() && !shouldShowDefault) {
        AsyncImage(
            model = photoPath,
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryColor,
                            SecondaryColor
                        )
                    ),
                    CircleShape
                ),
            contentScale = ContentScale.Crop,
            onSuccess = {
                imageLoadState = "success"
                android.util.Log.d("ProfilePhoto", "✅ Photo loaded successfully")
            },
            onError = { error ->
                imageLoadState = "error"
                shouldShowDefault = true
                android.util.Log.e("ProfilePhoto", "❌ Photo load failed: ${error.result.throwable?.message}")
            },
            onLoading = {
                imageLoadState = "loading"
                android.util.Log.d("ProfilePhoto", "🔄 Loading photo...")
            }
        )
    } else {
        // Default Avatar
        DefaultProfileAvatar(fullName = fullName)
    }

    // Show current state for debugging
    LaunchedEffect(imageLoadState) {
        android.util.Log.d("ProfilePhoto", "Current state: $imageLoadState")
    }
}

@Composable
private fun DefaultProfileAvatar(fullName: String) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        PrimaryColor,
                        SecondaryColor
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fullName.split(" ").map {
                it.firstOrNull()?.uppercaseChar() ?: ""
            }.take(2).joinToString(""),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ModernQuickStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModernStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Today,
            value = "30",
            label = "Hari Aktif",
            color = SuccessColor
        )
        ModernStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            value = "24/7",
            label = "Support",
            color = InfoColor
        )
        ModernStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Security,
            value = "100%",
            label = "Aman",
            color = WarningColor
        )
    }
}

@Composable
private fun ModernStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AccentColor
            )
        }
    }
}

@Composable
private fun ModernProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ModernProfileItemRow(
                        item = item,
                        showDivider = index < items.lastIndex
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernProfileItemRow(
    item: ProfileItem,
    showDivider: Boolean = true
) {
    Card(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon dengan background gradient
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.2f),
                                    SecondaryColor.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceColor
                    )
                    if (item.subtitle.isNotEmpty()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor
                        )
                    }
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AccentColor
                )
            }

            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = AccentColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernLogoutButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ErrorColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = ErrorColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Keluar dari Akun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ErrorColor
            )
        }
    }
}

@Composable
private fun ModernLogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Konfirmasi Keluar",
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }
        },
        text = {
            Text(
                "Apakah Anda yakin ingin keluar dari aplikasi KostKita?",
                color = AccentColor
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Keluar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AccentColor
                )
            ) {
                Text("Batal")
            }
        }
    )
}

data class ProfileItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit
)