// File: presentation/screens/tenant/TenantFormScreen.kt
package com.example.kostkita.presentation.screens.tenant

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita.domain.model.Room
import com.example.kostkita.presentation.screens.room.RoomViewModel

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

    // Filter available rooms
    val availableRooms = rooms.filter { it.statusKamar.equals("tersedia", true) }

    var nama by remember { mutableStateOf(tenant?.nama ?: "") }
    var email by remember { mutableStateOf(tenant?.email ?: "") }
    var phone by remember { mutableStateOf(tenant?.phone ?: "") }
    var pekerjaan by remember { mutableStateOf(tenant?.pekerjaan ?: "") }
    var emergencyContact by remember { mutableStateOf(tenant?.emergencyContact ?: "") }
    var selectedRoomId by remember { mutableStateOf(tenant?.roomId) }
    var showRoomSelection by remember { mutableStateOf(false) }

    val selectedRoom = rooms.find { it.id == selectedRoomId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tenantId == null) "Tambah Penghuni" else "Edit Penghuni") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Information Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Informasi Pribadi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = nama,
                                onValueChange = { nama = it },
                                label = { Text("Nama Lengkap") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("No. Telepon") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = pekerjaan,
                                onValueChange = { pekerjaan = it },
                                label = { Text("Pekerjaan") },
                                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = { Text("Kontak Darurat") },
                                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Room Selection Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pilih Kamar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (availableRooms.isEmpty() && tenantId == null) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("Tidak ada kamar tersedia")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Selected Room Display
                            if (selectedRoom != null) {
                                SelectedRoomCard(
                                    room = selectedRoom,
                                    onRemove = { selectedRoomId = null }
                                )
                            } else {
                                OutlinedCard(
                                    onClick = { showRoomSelection = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Tap untuk memilih kamar",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Save Button
                item {
                    Button(
                        onClick = {
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = nama.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
                    ) {
                        Icon(
                            if (tenant == null) Icons.Default.Save else Icons.Default.Update,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (tenant == null) "Simpan Penghuni" else "Update Penghuni",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Room Selection Bottom Sheet
        if (showRoomSelection) {
            ModalBottomSheet(
                onDismissRequest = { showRoomSelection = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                RoomSelectionContent(
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

@Composable
fun SelectedRoomCard(
    room: Room,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kamar ${room.nomorKamar}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${room.tipeKamar} • Lantai ${room.lantai}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatRupiah(room.hargaBulanan),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun RoomSelectionContent(
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
                .padding(16.dp)
        ) {
            Text(
                text = "Pilih Kamar Tersedia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Tidak ada kamar tersedia",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableRooms) { room ->
                    RoomSelectionCard(
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
fun RoomSelectionCard(
    room: Room,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color(0xFF10B981).copy(alpha = 0.05f)
                        )
                    )
                )
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
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
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
                        tint = Color(0xFF10B981)
                    )
                }

                Column {
                    Text(
                        text = "Kamar ${room.nomorKamar}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${room.tipeKamar} • Lantai ${room.lantai}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = room.fasilitas,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRupiah(room.hargaBulanan),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/bulan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatRupiah(amount: Int): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
    return format.format(amount)
}