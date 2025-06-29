package com.example.kostkita_app.presentation.screens.room

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modern Color Palette dari HomeScreen
private val PrimaryColor = Color(0xFFB8A491)
private val SecondaryColor = Color(0xFFF5B041)
private val AccentColor = Color(0xFF8B7355)
private val SurfaceColor = Color(0xFFFAF8F5)
private val OnSurfaceColor = Color(0xFF3C3C3C)
private val SuccessColor = Color(0xFF27AE60)
private val WarningColor = Color(0xFFF39C12)
private val ErrorColor = Color(0xFFE74C3C)
private val InfoColor = Color(0xFF3498DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFormScreen(
    navController: NavController,
    roomId: String? = null,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val room = rooms.find { it.id == roomId }
    val scope = rememberCoroutineScope()

    // Form states
    var nomorKamar by remember { mutableStateOf(room?.nomorKamar ?: "") }
    var tipeKamar by remember { mutableStateOf(room?.tipeKamar ?: "Standard") }
    var hargaBulanan by remember { mutableStateOf(room?.hargaBulanan?.toString() ?: "") }
    var fasilitas by remember { mutableStateOf(room?.fasilitas ?: "") }
    var statusKamar by remember { mutableStateOf(room?.statusKamar ?: "Tersedia") }
    var lantai by remember { mutableStateOf(room?.lantai?.toString() ?: "") }

    // Dropdown states
    var expandedTipe by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }

    // TAMBAHAN: State untuk popup dialog - SAMA SEPERTI PaymentFormScreen
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(200)
        formVisible = true
    }

    // TAMBAHAN: Handle success/error feedback - SAMA SEPERTI PaymentFormScreen
    LaunchedEffect(saveSuccess, saveError) {
        when {
            saveSuccess -> {
                isEditMode = room != null
                dialogMessage = if (room != null) {
                    "Kamar berhasil diperbarui!"
                } else {
                    "Kamar baru berhasil ditambahkan!"
                }
                showSuccessDialog = true
            }
            saveError != null -> {
                dialogMessage = saveError ?: "Terjadi kesalahan"
                showErrorDialog = true
                saveError = null
            }
        }
    }

    val tipeKamarOptions = listOf("Standard", "Deluxe", "VIP")
    val statusOptions = listOf("Tersedia", "Terisi", "Maintenance")

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            ModernTopBar(
                title = if (roomId == null) "Tambah Kamar" else "Detail Kamar",
                onBackClick = { navController.navigateUp() }
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
                // Header Card dengan animasi
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = slideInVertically { -it } + fadeIn()
                ) {
                    ModernHeaderCard(
                        title = if (roomId == null) "Kamar Baru" else "Edit Kamar",
                        subtitle = if (room != null) "Kamar ${room.nomorKamar} - ${room.tipeKamar}"
                        else "Tambahkan kamar baru ke sistem",
                        icon = Icons.Default.MeetingRoom,
                        room = room
                    )
                }

                AnimatedVisibility(
                    visible = formVisible,
                    enter = slideInVertically { it } + fadeIn()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Basic Information Section
                        ModernFormSection(
                            title = "Informasi Dasar",
                            icon = Icons.Default.Info
                        ) {
                            RoomFormModernTextField(
                                value = nomorKamar,
                                onValueChange = { nomorKamar = it },
                                label = "Nomor Kamar",
                                placeholder = "Contoh: 101, A1, B2",
                                leadingIcon = Icons.Default.Room,
                                color = InfoColor
                            )

                            RoomFormModernDropdownField(
                                value = tipeKamar,
                                label = "Tipe Kamar",
                                options = tipeKamarOptions,
                                expanded = expandedTipe,
                                onExpandedChange = { expandedTipe = it },
                                onValueChange = { tipeKamar = it },
                                leadingIcon = Icons.Default.Star,
                                color = InfoColor
                            )

                            RoomFormModernTextField(
                                value = hargaBulanan,
                                onValueChange = { hargaBulanan = it.filter { char -> char.isDigit() } },
                                label = "Harga Bulanan",
                                placeholder = "Masukkan harga sewa",
                                leadingIcon = Icons.Default.AttachMoney,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = "Rp ",
                                color = SuccessColor
                            )
                        }

                        // Additional Information Section
                        ModernFormSection(
                            title = "Detail Kamar",
                            icon = Icons.Default.Home
                        ) {
                            RoomFormModernTextField(
                                value = lantai,
                                onValueChange = { lantai = it.filter { char -> char.isDigit() } },
                                label = "Lantai",
                                placeholder = "Lantai berapa?",
                                leadingIcon = Icons.Default.LocationOn,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                color = SecondaryColor
                            )

                            RoomFormModernDropdownField(
                                value = statusKamar,
                                label = "Status Kamar",
                                options = statusOptions,
                                expanded = expandedStatus,
                                onExpandedChange = { expandedStatus = it },
                                onValueChange = { statusKamar = it },
                                leadingIcon = getStatusIcon(statusKamar),
                                color = getStatusColor(statusKamar)
                            )

                            RoomFormModernTextArea(
                                value = fasilitas,
                                onValueChange = { fasilitas = it },
                                label = "Fasilitas",
                                placeholder = "AC, WiFi, Kamar Mandi Dalam, Lemari, dll",
                                leadingIcon = Icons.Default.Inventory,
                                color = SecondaryColor
                            )
                        }

                        // Save Button - DIMODIFIKASI UNTUK POPUP
                        ModernSaveButton(
                            enabled = nomorKamar.isNotBlank() && hargaBulanan.isNotBlank() && lantai.isNotBlank() && !isSaving,
                            isEdit = room != null,
                            isLoading = isSaving,
                            onClick = {
                                scope.launch {
                                    try {
                                        isSaving = true

                                        if (room == null) {
                                            viewModel.addRoom(
                                                nomorKamar = nomorKamar,
                                                tipeKamar = tipeKamar,
                                                hargaBulanan = hargaBulanan.toIntOrNull() ?: 0,
                                                fasilitas = fasilitas,
                                                statusKamar = statusKamar,
                                                lantai = lantai.toIntOrNull() ?: 1
                                            )
                                        } else {
                                            viewModel.updateRoom(
                                                room.copy(
                                                    nomorKamar = nomorKamar,
                                                    tipeKamar = tipeKamar,
                                                    hargaBulanan = hargaBulanan.toIntOrNull() ?: 0,
                                                    fasilitas = fasilitas,
                                                    statusKamar = statusKamar,
                                                    lantai = lantai.toIntOrNull() ?: 1
                                                )
                                            )
                                        }

                                        // Simulasi delay untuk UX yang lebih baik
                                        delay(1000)
                                        saveSuccess = true

                                    } catch (e: Exception) {
                                        saveError = e.message ?: "Terjadi kesalahan"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // TAMBAHAN: Success Dialog - IDENTIK DENGAN PaymentFormScreen
    if (showSuccessDialog) {
        ModernSuccessDialog(
            message = dialogMessage,
            isEdit = isEditMode,
            onDismiss = {
                showSuccessDialog = false
                saveSuccess = false
                navController.navigateUp()
            }
        )
    }

    // TAMBAHAN: Error Dialog - IDENTIK DENGAN PaymentFormScreen
    if (showErrorDialog) {
        ModernErrorDialog(
            message = dialogMessage,
            onDismiss = {
                showErrorDialog = false
            },
            onRetry = {
                showErrorDialog = false
            }
        )
    }
}

// TAMBAHAN: Success Dialog Component - IDENTIK DENGAN PaymentFormScreen
@Composable
private fun ModernSuccessDialog(
    message: String,
    isEdit: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Success Icon dengan animasi
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SuccessColor.copy(alpha = 0.2f),
                                    SuccessColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = SuccessColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isEdit) "Berhasil Diperbarui!" else "Berhasil Disimpan!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detail info
                Text(
                    text = if (isEdit) "Data kamar telah diperbarui dengan informasi terbaru"
                    else "Data kamar baru telah tersimpan dalam sistem",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessColor,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "OK, Mengerti",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
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

@Composable
private fun ModernHeaderCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    room: com.example.kostkita_app.domain.model.Room?
) {
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor
                    )
                }

                // Status Badge untuk existing room
                room?.let { currentRoom ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getStatusColor(currentRoom.statusKamar).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                getStatusIcon(currentRoom.statusKamar),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = getStatusColor(currentRoom.statusKamar)
                            )
                            Text(
                                text = currentRoom.statusKamar,
                                style = MaterialTheme.typography.labelMedium,
                                color = getStatusColor(currentRoom.statusKamar),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Room details untuk existing room
            room?.let { currentRoom ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RoomDetailItem(
                            icon = Icons.Default.AttachMoney,
                            label = "Harga",
                            value = formatRupiah(currentRoom.hargaBulanan),
                            color = SuccessColor
                        )

                        RoomDetailItem(
                            icon = Icons.Default.LocationOn,
                            label = "Lantai",
                            value = "Lantai ${currentRoom.lantai}",
                            color = InfoColor
                        )

                        RoomDetailItem(
                            icon = Icons.Default.Star,
                            label = "Tipe",
                            value = currentRoom.tipeKamar,
                            color = SecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AccentColor
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceColor
        )
    }
}

@Composable
private fun ModernFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = PrimaryColor
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }

            content()
        }
    }
}

@Composable
private fun RoomFormModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = color
            )
        },
        prefix = if (prefix.isNotEmpty()) { { Text(prefix) } } else null,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLeadingIconColor = color,
            focusedLabelColor = color,
            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
            unfocusedLabelColor = AccentColor,
            unfocusedLeadingIconColor = AccentColor.copy(alpha = 0.7f)
        )
    )
}

@Composable
private fun RoomFormModernTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = color
            )
        },
        minLines = 3,
        maxLines = 5,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLeadingIconColor = color,
            focusedLabelColor = color,
            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
            unfocusedLabelColor = AccentColor,
            unfocusedLeadingIconColor = AccentColor.copy(alpha = 0.7f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomFormModernDropdownField(
    value: String,
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = color
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                focusedLeadingIconColor = color,
                focusedLabelColor = color,
                unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
                unfocusedLabelColor = AccentColor,
                unfocusedLeadingIconColor = AccentColor.copy(alpha = 0.7f)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = OnSurfaceColor
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernSaveButton(
    enabled: Boolean,
    isEdit: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEdit) InfoColor else SuccessColor,
            disabledContainerColor = AccentColor.copy(alpha = 0.3f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isEdit) Icons.Default.Update else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isEdit) "Update Kamar" else "Simpan Kamar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper functions
private fun getStatusIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status.lowercase()) {
        "tersedia" -> Icons.Default.CheckCircle
        "terisi" -> Icons.Default.People
        "maintenance" -> Icons.Default.Build
        else -> Icons.Default.Info
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "tersedia" -> SuccessColor
        "terisi" -> InfoColor
        "maintenance" -> WarningColor
        else -> AccentColor
    }
}

// TAMBAHAN: Error Dialog Component - IDENTIK DENGAN PaymentFormScreen
@Composable
private fun ModernErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Error Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ErrorColor.copy(alpha = 0.2f),
                                    ErrorColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = ErrorColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Terjadi Kesalahan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Silakan coba lagi atau periksa koneksi internet Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tutup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    )
}