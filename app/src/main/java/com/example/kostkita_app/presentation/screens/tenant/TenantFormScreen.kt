package com.example.kostkita_app.presentation.screens.tenant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita_app.domain.model.Room
import com.example.kostkita_app.presentation.screens.room.RoomViewModel
import kotlinx.coroutines.launch

// Modern Color Palette dari HomeScreen
private val PrimaryColor = Color(0xFFB8A491)
private val SecondaryColor = Color(0xFFF5B041)
private val AccentColor = Color(0xFF8B7355)
private val SurfaceColor = Color(0xFFFAF8F5)
private val OnSurfaceColor = Color(0xFF3C3C3C)
private val SuccessColor = Color(0xFF27AE60)
private val InfoColor = Color(0xFF3498DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantFormScreen(
    navController: NavController,
    tenantId: String? = null,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    roomViewModel: RoomViewModel = hiltViewModel()
) {
    val tenants by tenantViewModel.tenants.collectAsState()
    val rooms by roomViewModel.rooms.collectAsState()
    val tenant = tenants.find { it.id == tenantId }
    val scope = rememberCoroutineScope()

    // Available rooms untuk selection
    val availableRooms = rooms.filter {
        it.statusKamar.equals("tersedia", true) ||
                it.id == tenant?.roomId
    }

    // Form states
    var nama by remember { mutableStateOf(tenant?.nama ?: "") }
    var email by remember { mutableStateOf(tenant?.email ?: "") }
    var phone by remember { mutableStateOf(tenant?.phone ?: "") }
    var pekerjaan by remember { mutableStateOf(tenant?.pekerjaan ?: "") }
    var emergencyContact by remember { mutableStateOf(tenant?.emergencyContact ?: "") }
    var selectedRoomId by remember { mutableStateOf(tenant?.roomId) }
    var showRoomSelection by remember { mutableStateOf(false) }

    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(200)
        formVisible = true
    }

    val selectedRoom = rooms.find { it.id == selectedRoomId }

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            ModernTopBar(
                title = if (tenantId == null) "Tambah Penghuni" else "Edit Penghuni",
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
                        title = if (tenantId == null) "Penghuni Baru" else "Edit Penghuni",
                        subtitle = "Lengkapi informasi penghuni dengan benar",
                        icon = Icons.Default.PersonAdd
                    )
                }

                AnimatedVisibility(
                    visible = formVisible,
                    enter = slideInVertically { it } + fadeIn()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Personal Information Section
                        ModernFormSection(
                            title = "Informasi Pribadi",
                            icon = Icons.Default.Badge
                        ) {
                            ModernTextField(
                                value = nama,
                                onValueChange = { nama = it },
                                label = "Nama Lengkap",
                                placeholder = "Masukkan nama lengkap",
                                leadingIcon = Icons.Default.Person,
                                color = InfoColor
                            )

                            ModernTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                placeholder = "contoh@email.com",
                                leadingIcon = Icons.Default.Email,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                color = InfoColor
                            )

                            ModernTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Nomor Telepon",
                                placeholder = "08xxxxxxxxxx",
                                leadingIcon = Icons.Default.Phone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                color = InfoColor
                            )
                        }

                        // Additional Information Section
                        ModernFormSection(
                            title = "Informasi Tambahan",
                            icon = Icons.Default.Work
                        ) {
                            ModernTextField(
                                value = pekerjaan,
                                onValueChange = { pekerjaan = it },
                                label = "Pekerjaan",
                                placeholder = "Masukkan pekerjaan",
                                leadingIcon = Icons.Default.Work,
                                color = SecondaryColor
                            )

                            ModernTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = "Kontak Darurat",
                                placeholder = "Nomor yang bisa dihubungi",
                                leadingIcon = Icons.Default.ContactPhone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                color = SecondaryColor
                            )
                        }

                        // Room Selection Section
                        ModernRoomSelectionSection(
                            selectedRoom = selectedRoom,
                            availableRooms = availableRooms,
                            onRoomSelect = { showRoomSelection = true },
                            onRoomRemove = { selectedRoomId = null }
                        )

                        // Save Button
                        ModernSaveButton(
                            enabled = nama.isNotBlank() && email.isNotBlank() && phone.isNotBlank(),
                            isEdit = tenant != null,
                            onClick = {
                                scope.launch {
                                    if (tenant == null) {
                                        tenantViewModel.addTenant(
                                            nama = nama,
                                            email = email,
                                            phone = phone,
                                            pekerjaan = pekerjaan,
                                            emergencyContact = emergencyContact,
                                            roomId = selectedRoomId
                                        )
                                    } else {
                                        tenantViewModel.updateTenant(
                                            tenant.copy(
                                                nama = nama,
                                                email = email,
                                                phone = phone,
                                                pekerjaan = pekerjaan,
                                                emergencyContact = emergencyContact,
                                                roomId = selectedRoomId
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

        // Room Selection Bottom Sheet
        if (showRoomSelection) {
            ModalBottomSheet(
                onDismissRequest = { showRoomSelection = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                ModernRoomSelectionContent(
                    availableRooms = availableRooms,
                    onRoomSelected = { room ->
                        selectedRoomId = room.id
                        showRoomSelection = false
                    }
                )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
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

            Column {
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
        }
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
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLeadingIconColor = color,
            focusedLabelColor = color,
            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
            unfocusedLabelColor = AccentColor
        )
    )
}

@Composable
private fun ModernRoomSelectionSection(
    selectedRoom: Room?,
    availableRooms: List<Room>,
    onRoomSelect: () -> Unit,
    onRoomRemove: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = SuccessColor
                        )
                    }
                    Text(
                        text = "Pilih Kamar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                }

                if (availableRooms.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE74C3C).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Tidak Ada Kamar",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE74C3C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (selectedRoom != null) {
                ModernSelectedRoomCard(
                    room = selectedRoom,
                    onRemove = onRoomRemove
                )
            } else {
                ModernRoomSelectButton(
                    enabled = availableRooms.isNotEmpty(),
                    onClick = onRoomSelect
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSelectedRoomCard(
    room: Room,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuccessColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = SuccessColor
                    )
                }

                Column {
                    Text(
                        text = "Kamar ${room.nomorKamar}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "${room.tipeKamar} • Lantai ${room.lantai}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor
                    )
                    Text(
                        text = formatRupiah(room.hargaBulanan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SuccessColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE74C3C).copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFFE74C3C)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernRoomSelectButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        // PERBAIKAN: Hapus casting dan gunakan conditional yang benar
        onClick = if (enabled) onClick else { {} }, // atau gunakan approach lain
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) SurfaceColor else AccentColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) AccentColor.copy(alpha = 0.3f) else AccentColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = if (enabled) AccentColor else AccentColor.copy(alpha = 0.5f)
                )
                Text(
                    text = if (enabled) "Tap untuk memilih kamar" else "Tidak ada kamar tersedia",
                    color = if (enabled) AccentColor else AccentColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (enabled) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = AccentColor
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
                text = if (isEdit) "Update Penghuni" else "Simpan Penghuni",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ModernRoomSelectionContent(
    availableRooms: List<Room>,
    onRoomSelected: (Room) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Pilih Kamar Tersedia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (availableRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AccentColor.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Tidak ada kamar tersedia",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentColor
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = availableRooms.size,
                    key = { index -> availableRooms[index].id }
                ) { index ->
                    val room = availableRooms[index]
                    ModernRoomItem(
                        room = room,
                        onClick = { onRoomSelected(room) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernRoomItem(
    room: Room,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (room.tipeKamar.lowercase()) {
                            "standard" -> Icons.Default.SingleBed
                            "deluxe" -> Icons.Default.KingBed
                            "vip" -> Icons.Default.Star
                            else -> Icons.Default.MeetingRoom
                        },
                        contentDescription = null,
                        tint = SuccessColor
                    )
                }

                Column {
                    Text(
                        text = "Kamar ${room.nomorKamar}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "${room.tipeKamar} • Lantai ${room.lantai}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor
                    )
                    if (room.fasilitas.isNotBlank()) {
                        Text(
                            text = room.fasilitas,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRupiah(room.hargaBulanan),
                    style = MaterialTheme.typography.titleMedium,
                    color = SuccessColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/bulan",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentColor
                )
            }
        }
    }
}

// Helper function
private fun formatRupiah(amount: Int): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
    return format.format(amount)
}