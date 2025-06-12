package com.example.kostkita_app.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// Modern Color Palette
private val PrimaryColor = Color(0xFFB8A491)
private val SecondaryColor = Color(0xFFF5B041)
private val AccentColor = Color(0xFF8B7355)
private val SurfaceColor = Color(0xFFFAF8F5)
private val OnSurfaceColor = Color(0xFF3C3C3C)
private val SuccessColor = Color(0xFF27AE60)
private val InfoColor = Color(0xFF3498DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Form States
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePhotoPath by remember { mutableStateOf<String?>(null) }
    var isImageProcessing by remember { mutableStateOf(false) }

    // Animation States
    var isHeaderVisible by remember { mutableStateOf(false) }
    var isFormVisible by remember { mutableStateOf(false) }

    // Entrance animations
    LaunchedEffect(Unit) {
        isHeaderVisible = true
        kotlinx.coroutines.delay(200)
        isFormVisible = true
    }

    // FIXED: Image picker dengan proper handling
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            scope.launch {
                try {
                    isImageProcessing = true
                    android.util.Log.d("EditProfile", "=== PROCESSING IMAGE ===")
                    android.util.Log.d("EditProfile", "Source URI: $sourceUri")

                    // Convert URI to permanent file
                    val savedPath = copyUriToInternalStorage(context, sourceUri)

                    if (savedPath != null) {
                        selectedImageUri = sourceUri // For immediate preview
                        profilePhotoPath = savedPath // For persistence

                        android.util.Log.d("EditProfile", "✅ Image saved: $savedPath")
                        snackbarHostState.showSnackbar("📸 Foto berhasil dipilih!")
                    } else {
                        android.util.Log.e("EditProfile", "❌ Failed to save image")
                        snackbarHostState.showSnackbar("❌ Gagal menyimpan foto")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EditProfile", "❌ Image processing error", e)
                    snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                } finally {
                    isImageProcessing = false
                }
            }
        }
    }

    // Initialize form with user data
    LaunchedEffect(user) {
        user?.let { userData ->
            fullName = userData.fullName
            username = userData.username
            email = userData.email
            profilePhotoPath = userData.profilePhoto
        }
    }

    // Handle update results
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateProfileState.Success -> {
                snackbarHostState.showSnackbar("✅ Profil berhasil diperbarui!")
                kotlinx.coroutines.delay(500)
                navController.previousBackStackEntry?.savedStateHandle?.set("profile_updated", true)
                navController.navigateUp()
            }
            is UpdateProfileState.Error -> {
                snackbarHostState.showSnackbar("❌ ${(updateState as UpdateProfileState.Error).message}")
            }
            else -> { /* Loading or Idle */ }
        }
    }

    Scaffold(
        containerColor = SurfaceColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditProfileTopBar(onBackClick = { navController.navigateUp() })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryColor.copy(alpha = 0.03f),
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Card
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                ) {
                    HeaderCard()
                }

                // Photo Section
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    PhotoSection(
                        currentPhotoPath = profilePhotoPath,
                        selectedImageUri = selectedImageUri,
                        fullName = fullName,
                        isProcessing = isImageProcessing,
                        onImagePick = { imagePickerLauncher.launch("image/*") }
                    )
                }

                // Form Section
                AnimatedVisibility(
                    visible = isFormVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    FormSection(
                        fullName = fullName,
                        username = username,
                        email = email,
                        isLoading = updateState is UpdateProfileState.Loading,
                        onFullNameChange = { fullName = it },
                        onUsernameChange = { username = it },
                        onEmailChange = { email = it }
                    )
                }

                // Save Button
                AnimatedVisibility(
                    visible = isFormVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    SaveButton(
                        enabled = fullName.isNotBlank() &&
                                username.isNotBlank() &&
                                email.isNotBlank() &&
                                updateState !is UpdateProfileState.Loading,
                        isLoading = updateState is UpdateProfileState.Loading,
                        onClick = {
                            user?.let { currentUser ->
                                // Use the saved file path, not the content URI
                                val finalPhotoPath = profilePhotoPath

                                android.util.Log.d("EditProfile", "=== SAVING PROFILE ===")
                                android.util.Log.d("EditProfile", "Photo path to save: $finalPhotoPath")

                                viewModel.updateProfile(
                                    currentUser.copy(
                                        fullName = fullName,
                                        username = username,
                                        email = email,
                                        profilePhoto = finalPhotoPath // Use permanent path
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Edit Profil",
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
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

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SecondaryColor.copy(alpha = 0.1f),
                            PrimaryColor.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PersonPin,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = SecondaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Perbarui Profil Anda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Text(
                    text = "Pastikan informasi Anda selalu terkini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(
    currentPhotoPath: String?,
    selectedImageUri: Uri?,
    fullName: String,
    isProcessing: Boolean,
    onImagePick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Foto Profil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile Image dengan animation
                val imageScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ), label = "imageScale"
                )

                Box(modifier = Modifier.scale(imageScale)) {
                    ProfileImage(
                        selectedImageUri = selectedImageUri,
                        currentPhotoPath = currentPhotoPath,
                        fullName = fullName
                    )
                }

                // Camera Button
                CameraButton(
                    onClick = onImagePick,
                    enabled = !isProcessing,
                    modifier = Modifier.offset(x = 50.dp, y = 50.dp)
                )

                // Processing Overlay
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status Indicator
            StatusIndicator(
                hasNewImage = selectedImageUri != null,
                isProcessing = isProcessing
            )
        }
    }
}

@Composable
private fun ProfileImage(
    selectedImageUri: Uri?,
    currentPhotoPath: String?,
    fullName: String
) {
    when {
        selectedImageUri != null -> {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected Photo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(
                        6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(SuccessColor, InfoColor)
                        ),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
        }
        !currentPhotoPath.isNullOrEmpty() -> {
            AsyncImage(
                model = currentPhotoPath,
                contentDescription = "Current Photo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(
                        6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryColor, SecondaryColor)
                        ),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop,
                onError = { error ->
                    android.util.Log.e("ProfileImage", "Failed to load: $currentPhotoPath", error.result.throwable)
                }
            )
        }
        else -> {
            DefaultAvatar(fullName = fullName)
        }
    }
}

@Composable
private fun DefaultAvatar(fullName: String) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryColor, SecondaryColor.copy(alpha = 0.8f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (fullName.isNotEmpty()) {
                fullName.split(" ").mapNotNull {
                    it.firstOrNull()?.uppercaseChar()
                }.take(2).joinToString("")
            } else "?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun CameraButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val buttonScale by rememberInfiniteTransition(label = "buttonPulse").animateFloat(
        initialValue = 1f,
        targetValue = if (enabled) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .scale(if (enabled) buttonScale else 0.9f),
        containerColor = if (enabled) SecondaryColor else AccentColor.copy(alpha = 0.5f),
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            hoveredElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = if (enabled) Icons.Default.PhotoCamera else Icons.Default.HourglassEmpty,
            contentDescription = "Change Photo",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun StatusIndicator(
    hasNewImage: Boolean,
    isProcessing: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val icon: ImageVector
        val text: String
        val color: Color

        when {
            isProcessing -> {
                icon = Icons.Default.HourglassEmpty
                text = "Memproses foto..."
                color = InfoColor
            }
            hasNewImage -> {
                icon = Icons.Default.CheckCircle
                text = "Foto baru dipilih"
                color = SuccessColor
            }
            else -> {
                icon = Icons.Default.PhotoCamera
                text = "Ketuk kamera untuk mengubah foto"
                color = AccentColor
            }
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (hasNewImage || isProcessing) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun FormSection(
    fullName: String,
    username: String,
    email: String,
    isLoading: Boolean,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPage,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Informasi Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }

            // Form Fields
            CustomTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Nama Lengkap",
                icon = Icons.Default.Badge,
                enabled = !isLoading
            )

            CustomTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Username",
                icon = Icons.Default.AlternateEmail,
                enabled = !isLoading
            )

            CustomTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Alamat Email",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = !isLoading
            )
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) SecondaryColor else AccentColor.copy(alpha = 0.5f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryColor,
            focusedLeadingIconColor = SecondaryColor,
            focusedLabelColor = SecondaryColor,
            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
            unfocusedLabelColor = AccentColor,
            disabledBorderColor = AccentColor.copy(alpha = 0.2f),
            disabledLabelColor = AccentColor.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(buttonScale),
        shape = RoundedCornerShape(20.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryColor,
            disabledContainerColor = AccentColor.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            disabledElevation = 2.dp
        )
    ) {
        if (isLoading) {
            LoadingContent()
        } else {
            SaveContent()
        }
    }
}

@Composable
private fun LoadingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )
        Text(
            text = "Menyimpan...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SaveContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.SaveAlt,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "💾 Simpan Perubahan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// FIXED: Function to copy URI to permanent storage
private suspend fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PhotoHandler", "=== COPYING IMAGE ===")
            android.util.Log.d("PhotoHandler", "Source URI: $uri")

            // Create unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = "profile_photo_$timestamp.jpg"
            val outputFile = File(context.filesDir, fileName)

            android.util.Log.d("PhotoHandler", "Target file: ${outputFile.absolutePath}")

            // Copy content from URI to file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }

            // Verify file was created successfully
            val success = outputFile.exists() && outputFile.length() > 0
            android.util.Log.d("PhotoHandler", "File created: $success")
            android.util.Log.d("PhotoHandler", "File size: ${outputFile.length()} bytes")

            if (success) {
                outputFile.absolutePath
            } else {
                android.util.Log.e("PhotoHandler", "File creation failed or empty")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("PhotoHandler", "Error copying file", e)
            null
        }
    }
}