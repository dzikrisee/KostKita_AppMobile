package com.example.kostkita.presentation.screens.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFormScreen(
    navController: NavController,
    roomId: String? = null,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val room = rooms.find { it.id == roomId }

    var nomorKamar by remember { mutableStateOf(room?.nomorKamar ?: "") }
    var tipeKamar by remember { mutableStateOf(room?.tipeKamar ?: "Standard") }
    var hargaBulanan by remember { mutableStateOf(room?.hargaBulanan?.toString() ?: "") }
    var fasilitas by remember { mutableStateOf(room?.fasilitas ?: "") }
    var statusKamar by remember { mutableStateOf(room?.statusKamar ?: "Tersedia") }
    var lantai by remember { mutableStateOf(room?.lantai?.toString() ?: "") }

    val tipeKamarOptions = listOf("Standard", "Deluxe", "VIP")
    val statusOptions = listOf("Tersedia", "Terisi", "Maintenance")

    var expandedTipe by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (roomId == null) "Tambah Kamar" else "Detail & Edit Kamar") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Detail Kamar Card (hanya tampil saat edit)
            if (room != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kamar ${room.nomorKamar}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (room.statusKamar) {
                                            "Tersedia" -> Color(0xFF4CAF50)
                                            "Terisi" -> Color(0xFFFF9800)
                                            "Maintenance" -> Color(0xFFF44336)
                                            else -> Color.Gray
                                        }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = room.statusKamar,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Detail Information
                        DetailItem(
                            icon = Icons.Default.Star,
                            label = "Tipe Kamar",
                            value = room.tipeKamar
                        )

                        DetailItem(
                            icon = Icons.Default.Person,
                            label = "Harga Bulanan",
                            value = "Rp ${String.format("%,d", room.hargaBulanan)}"
                        )

                        DetailItem(
                            icon = Icons.Default.LocationOn,
                            label = "Lantai",
                            value = "Lantai ${room.lantai}"
                        )

                        if (room.fasilitas.isNotBlank()) {
                            DetailItem(
                                icon = Icons.Default.Info,
                                label = "Fasilitas",
                                value = room.fasilitas
                            )
                        }
                    }
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Form Edit Title
                Text(
                    text = "Edit Informasi Kamar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Form Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (room == null) {
                        Text(
                            text = "Informasi Kamar Baru",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = nomorKamar,
                        onValueChange = { nomorKamar = it },
                        label = { Text("Nomor Kamar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedTipe,
                        onExpandedChange = { expandedTipe = !expandedTipe }
                    ) {
                        OutlinedTextField(
                            value = tipeKamar,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipe Kamar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipe) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipe,
                            onDismissRequest = { expandedTipe = false }
                        ) {
                            tipeKamarOptions.forEach { tipe ->
                                DropdownMenuItem(
                                    text = { Text(tipe) },
                                    onClick = {
                                        tipeKamar = tipe
                                        expandedTipe = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = hargaBulanan,
                        onValueChange = { hargaBulanan = it.filter { char -> char.isDigit() } },
                        label = { Text("Harga Bulanan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("Rp ") }
                    )

                    OutlinedTextField(
                        value = fasilitas,
                        onValueChange = { fasilitas = it },
                        label = { Text("Fasilitas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus }
                    ) {
                        OutlinedTextField(
                            value = statusKamar,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status Kamar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        statusKamar = status
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = lantai,
                        onValueChange = { lantai = it.filter { char -> char.isDigit() } },
                        label = { Text("Lantai") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = nomorKamar.isNotBlank() && hargaBulanan.isNotBlank() && lantai.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (room == null) "Simpan Kamar" else "Update Kamar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}