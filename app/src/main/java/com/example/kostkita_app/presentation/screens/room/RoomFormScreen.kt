package com.example.kostkita_app.presentation.screens.room

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// Modern Color Palette dari HomeScreen
private val PrimaryColor = Color(0xFFB8A491)
private val SecondaryColor = Color(0xFFF5B041)
private val AccentColor = Color(0xFF8B7355)
private val SurfaceColor = Color(0xFFFAF8F5)
private val OnSurfaceColor = Color(0xFF3C3C3C)
private val SuccessColor = Color(0xFF27AE60)
private val WarningColor = Color(0xFFF39C12)
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

    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(200)
        formVisible = true
    }

    val tipeKamarOptions = listOf("Standard", "Deluxe", "VIP")
    val statusOptions = listOf("Tersedia", "Terisi", "Maintenance")

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            ModernTopBar(
                title = if (roomId == null) "Tambah Kamar" else "Edit Kamar",
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

                        // Save Button
                        ModernSaveButton(
                            enabled = nomorKamar.isNotBlank() && hargaBulanan.isNotBlank() && lantai.isNotBlank(),
                            isEdit = room != null,
                            onClick = {
                                scope.launch {
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
                                    navController.navigateUp()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
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

