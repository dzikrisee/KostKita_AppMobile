package com.example.kostkita_app.presentation.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// Modern Color Palette - matching EditProfileScreen dan ProfileScreen
private val PrimaryColor = Color(0xFFB8A491) // Soft beige
private val SecondaryColor = Color(0xFFF5B041) // Warm orange
private val AccentColor = Color(0xFF8B7355) // Darker beige
private val SurfaceColor = Color(0xFFFAF8F5) // Light cream
private val OnSurfaceColor = Color(0xFF3C3C3C) // Dark gray
private val SuccessColor = Color(0xFF27AE60) // Fresh green
private val WarningColor = Color(0xFFF39C12) // Warm orange
private val ErrorColor = Color(0xFFE74C3C) // Soft red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibility states
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Collect state
    val changePasswordState by viewModel.changePasswordState.collectAsState()
    val isLoading = changePasswordState is ChangePasswordState.Loading

    // Validation
    val isFormValid = oldPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            newPassword == confirmPassword &&
            newPassword.length >= 8

    // Handle state changes
    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is ChangePasswordState.Success -> {
                snackbarHostState.showSnackbar("✅ Password berhasil diubah!")
                navController.popBackStack()
            }
            is ChangePasswordState.Error -> {
                snackbarHostState.showSnackbar("❌ ${(changePasswordState as ChangePasswordState.Error).message}")
            }
            else -> Unit
        }
    }

    // Modern gradient background - sama seperti EditProfileScreen
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            SecondaryColor.copy(alpha = 0.1f),
            PrimaryColor.copy(alpha = 0.1f),
            SurfaceColor
        )
    )

    Scaffold(
        containerColor = SurfaceColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ChangePasswordTopBar(onBackClick = { navController.popBackStack() })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Content tanpa custom top bar - gunakan sistem TopAppBar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // HeaderCard - sama seperti EditProfileScreen
                    HeaderCard()

                    Spacer(modifier = Modifier.height(32.dp))

                    // Password Fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        ModernPasswordField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = "Password Lama",
                            icon = Icons.Default.Lock,
                            placeholder = "Masukkan password lama",
                            isVisible = oldPasswordVisible,
                            onVisibilityToggle = { oldPasswordVisible = !oldPasswordVisible }
                        )

                        ModernPasswordField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Password Baru",
                            icon = Icons.Default.LockReset,
                            placeholder = "Masukkan password baru",
                            isVisible = newPasswordVisible,
                            onVisibilityToggle = { newPasswordVisible = !newPasswordVisible }
                        )

                        ModernPasswordField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Konfirmasi Password",
                            icon = Icons.Default.Shield,
                            placeholder = "Konfirmasi password baru",
                            isVisible = confirmPasswordVisible,
                            onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                            isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Requirements
                    PasswordRequirements(
                        password = newPassword,
                        confirmPassword = confirmPassword
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Change Password Button
                    ModernChangePasswordButton(
                        isLoading = isLoading,
                        isEnabled = isFormValid,
                        onClick = {
                            viewModel.changePassword(oldPassword, newPassword)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ubah Password",
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
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = SecondaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Keamanan Akun",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Text(
                    text = "Perbarui password untuk keamanan yang lebih baik",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) ErrorColor
                else SecondaryColor
            )
        },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) "Sembunyikan password" else "Tampilkan password",
                    tint = AccentColor
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) ErrorColor else SecondaryColor,
            unfocusedBorderColor = if (isError) ErrorColor else AccentColor.copy(alpha = 0.5f),
            focusedLabelColor = if (isError) ErrorColor else SecondaryColor,
            cursorColor = SecondaryColor,
            errorBorderColor = ErrorColor
        ),
        isError = isError,
        singleLine = true
    )
}

@Composable
private fun PasswordRequirements(
    password: String,
    confirmPassword: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Persyaratan Password:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor
            )

            RequirementItem(
                text = "Minimal 8 karakter",
                isMet = password.length >= 8
            )

            RequirementItem(
                text = "Password harus sama",
                isMet = confirmPassword.isNotEmpty() && password == confirmPassword
            )
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isMet) SuccessColor else AccentColor.copy(alpha = 0.6f)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) SuccessColor else AccentColor
        )
    }
}

@Composable
private fun ModernChangePasswordButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isLoading) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(animatedScale),
        shape = RoundedCornerShape(20.dp), // Sama seperti EditProfileScreen
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryColor, // Warna yang sama dengan EditProfile
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isLoading) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = if (isLoading) "Mengubah Password..." else "🔐 Ubah Password",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}