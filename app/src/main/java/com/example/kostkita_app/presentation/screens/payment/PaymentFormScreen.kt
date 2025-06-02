package com.example.kostkita.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita.presentation.screens.room.RoomViewModel
import com.example.kostkita.presentation.screens.tenant.TenantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(
    navController: NavController,
    paymentId: String? = null,
    viewModel: PaymentViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    roomViewModel: RoomViewModel = hiltViewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val tenants by tenantViewModel.tenants.collectAsState()
    val rooms by roomViewModel.rooms.collectAsState()

    val payment = payments.find { it.id == paymentId }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTenantId by remember { mutableStateOf(payment?.tenantId ?: "") }
    var selectedRoomId by remember { mutableStateOf(payment?.roomId ?: "") }
    var bulanTahun by remember { mutableStateOf(payment?.bulanTahun ?: "") }
    var jumlahBayar by remember { mutableStateOf(payment?.jumlahBayar?.toString() ?: "") }
    var statusPembayaran by remember { mutableStateOf(payment?.statusPembayaran ?: "Lunas") }
    var denda by remember { mutableStateOf(payment?.denda?.toString() ?: "0") }

    var expandedTenant by remember { mutableStateOf(false) }
    var expandedRoom by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val statusOptions = listOf("Lunas", "Belum Bayar", "Sebagian")
    val selectedTenant = tenants.find { it.id == selectedTenantId }
    val selectedRoom = rooms.find { it.id == selectedRoomId }

    // Auto-fill room when tenant is selected - FIX THE LOGIC
    LaunchedEffect(selectedTenantId) {
        if (payment == null && selectedTenantId.isNotEmpty()) {
            // Reset previous selections
            selectedRoomId = ""
            jumlahBayar = ""

            // Find the selected tenant
            val selectedTenant = tenants.find { it.id == selectedTenantId }
            selectedTenant?.roomId?.let { roomId ->
                // Set the correct room
                selectedRoomId = roomId

                // Find room details and set price
                val roomDetail = rooms.find { it.id == roomId }
                roomDetail?.let { room ->
                    jumlahBayar = room.hargaBulanan.toString()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (paymentId == null) "Tambah Pembayaran" else "Edit Pembayaran",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            Color(0xFF667EEA).copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
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
                // Form Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF667EEA),
                                            Color(0xFF764BA2)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = if (paymentId == null) "Pembayaran Baru" else "Edit Pembayaran",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isi informasi pembayaran dengan lengkap",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Form Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Informasi Pembayaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Tenant Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Penghuni",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedTenant,
                                onExpandedChange = { expandedTenant = !expandedTenant }
                            ) {
                                OutlinedTextField(
                                    value = selectedTenant?.nama ?: "Pilih Penghuni",
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Pilih penghuni") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenant)
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF667EEA),
                                        focusedLeadingIconColor = Color(0xFF667EEA)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedTenant,
                                    onDismissRequest = { expandedTenant = false }
                                ) {
                                    // Only show tenants that have rooms assigned
                                    tenants.filter { it.roomId != null }.forEach { tenant ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(tenant.nama, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        "${tenant.email} • Kamar ${rooms.find { it.id == tenant.roomId }?.nomorKamar ?: "?"}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedTenantId = tenant.id
                                                expandedTenant = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Room Selection (Auto-filled based on tenant)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Kamar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (selectedRoom != null) {
                                // Display selected room (read-only when auto-filled)
                                OutlinedTextField(
                                    value = "Kamar ${selectedRoom.nomorKamar} - ${selectedRoom.tipeKamar}",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.MeetingRoom, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF10B981),
                                        focusedLeadingIconColor = Color(0xFF10B981)
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    value = "Pilih penghuni terlebih dahulu",
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    leadingIcon = {
                                        Icon(Icons.Default.MeetingRoom, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // Month/Year Field
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Bulan/Tahun",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = bulanTahun,
                                onValueChange = { bulanTahun = it },
                                placeholder = { Text("Contoh: Januari 2025") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667EEA),
                                    focusedLeadingIconColor = Color(0xFF667EEA)
                                )
                            )
                        }

                        // Amount Field (Auto-filled from room price)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Jumlah Bayar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = jumlahBayar,
                                onValueChange = { jumlahBayar = it.filter { char -> char.isDigit() } },
                                placeholder = { Text("Masukkan jumlah") },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                                },
                                prefix = { Text("Rp ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667EEA),
                                    focusedLeadingIconColor = Color(0xFF667EEA)
                                )
                            )
                        }

                        // Status Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Status Pembayaran",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedStatus,
                                onExpandedChange = { expandedStatus = !expandedStatus }
                            ) {
                                OutlinedTextField(
                                    value = statusPembayaran,
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF667EEA),
                                        focusedLeadingIconColor = Color(0xFF667EEA)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedStatus,
                                    onDismissRequest = { expandedStatus = false }
                                ) {
                                    statusOptions.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                statusPembayaran = status
                                                expandedStatus = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Fine Field
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Denda (Opsional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = denda,
                                onValueChange = { denda = it.filter { char -> char.isDigit() } },
                                placeholder = { Text("0") },
                                leadingIcon = {
                                    Icon(Icons.Default.Warning, contentDescription = null)
                                },
                                prefix = { Text("Rp ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667EEA),
                                    focusedLeadingIconColor = Color(0xFF667EEA)
                                )
                            )
                        }
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        if (payment == null) {
                            viewModel.addPayment(
                                tenantId = selectedTenantId,
                                roomId = selectedRoomId,
                                bulanTahun = bulanTahun,
                                jumlahBayar = jumlahBayar.toIntOrNull() ?: 0,
                                statusPembayaran = statusPembayaran,
                                denda = denda.toIntOrNull() ?: 0
                            )
                        } else {
                            viewModel.updatePayment(
                                payment.copy(
                                    tenantId = selectedTenantId,
                                    roomId = selectedRoomId,
                                    bulanTahun = bulanTahun,
                                    jumlahBayar = jumlahBayar.toIntOrNull() ?: 0,
                                    statusPembayaran = statusPembayaran,
                                    denda = denda.toIntOrNull() ?: 0
                                )
                            )
                        }

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (payment == null) "Pembayaran berhasil ditambahkan"
                                else "Pembayaran berhasil diupdate"
                            )
                        }

                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedTenantId.isNotBlank() &&
                            selectedRoomId.isNotBlank() &&
                            bulanTahun.isNotBlank() &&
                            jumlahBayar.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (payment == null) Icons.Default.Save else Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (payment == null) "Simpan Pembayaran" else "Update Pembayaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}