package com.example.kostkita_app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita_app.presentation.screens.room.RoomViewModel
import com.example.kostkita_app.presentation.screens.tenant.TenantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modern Color Palette - matching HomeScreen
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
    var expandedStatus by remember { mutableStateOf(false) }

    val statusOptions = listOf("Lunas", "Belum Bayar", "Sebagian")
    val selectedTenant = tenants.find { it.id == selectedTenantId }
    val selectedRoom = rooms.find { it.id == selectedRoomId }

    // Auto-fill room when tenant is selected
    LaunchedEffect(selectedTenantId) {
        if (payment == null && selectedTenantId.isNotEmpty()) {
            selectedRoomId = ""
            jumlahBayar = ""

            val selectedTenant = tenants.find { it.id == selectedTenantId }
            selectedTenant?.roomId?.let { roomId ->
                selectedRoomId = roomId
                val roomDetail = rooms.find { it.id == roomId }
                roomDetail?.let { room ->
                    jumlahBayar = room.hargaBulanan.toString()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceColor,
        topBar = {
            ModernFormTopBar(
                title = if (paymentId == null) "Tambah Pembayaran" else "Edit Pembayaran",
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Section
                ModernFormHeader(isEdit = paymentId != null)

                // Tenant Selection Section
                ModernTenantSelection(
                    tenants = tenants,
                    rooms = rooms,
                    selectedTenantId = selectedTenantId,
                    selectedRoom = selectedRoom,
                    expandedTenant = expandedTenant,
                    onExpandedChange = { expandedTenant = it },
                    onTenantSelected = { selectedTenantId = it }
                )

                // Payment Details Section
                ModernPaymentDetails(
                    bulanTahun = bulanTahun,
                    onBulanTahunChange = { bulanTahun = it },
                    jumlahBayar = jumlahBayar,
                    onJumlahBayarChange = { jumlahBayar = it.filter { char -> char.isDigit() } },
                    statusPembayaran = statusPembayaran,
                    statusOptions = statusOptions,
                    expandedStatus = expandedStatus,
                    onExpandedStatusChange = { expandedStatus = it },
                    onStatusSelected = { statusPembayaran = it },
                    denda = denda,
                    onDendaChange = { denda = it.filter { char -> char.isDigit() } }
                )

                // Save Button
                ModernSaveButton(
                    enabled = selectedTenantId.isNotBlank() &&
                            selectedRoomId.isNotBlank() &&
                            bulanTahun.isNotBlank() &&
                            jumlahBayar.isNotBlank(),
                    isEdit = payment != null,
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
                                else "Pembayaran berhasil diperbarui"
                            )
                        }

                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernFormTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryColor.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryColor
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }
        }
    }
}

@Composable
private fun ModernFormHeader(isEdit: Boolean) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SecondaryColor.copy(alpha = 0.1f)
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
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    SecondaryColor,
                                    PrimaryColor
                                )
                            )
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
                        text = if (isEdit) "Edit Pembayaran" else "Pembayaran Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "Isi informasi pembayaran dengan lengkap",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTenantSelection(
    tenants: List<com.example.kostkita_app.domain.model.Tenant>,
    rooms: List<com.example.kostkita_app.domain.model.Room>,
    selectedTenantId: String,
    selectedRoom: com.example.kostkita_app.domain.model.Room?,
    expandedTenant: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTenantSelected: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = InfoColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Informasi Penghuni",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                }

                // Tenant Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTenant,
                    onExpandedChange = onExpandedChange
                ) {
                    val selectedTenant = tenants.find { it.id == selectedTenantId }

                    OutlinedTextField(
                        value = selectedTenant?.nama ?: "Pilih Penghuni",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Pilih penghuni", color = AccentColor.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = InfoColor)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenant)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InfoColor,
                            focusedLeadingIconColor = InfoColor,
                            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedTenant,
                        onDismissRequest = { onExpandedChange(false) }
                    ) {
                        tenants.filter { it.roomId != null }.forEach { tenant ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            tenant.nama,
                                            fontWeight = FontWeight.Medium,
                                            color = OnSurfaceColor
                                        )
                                        Text(
                                            "${tenant.email} • Kamar ${rooms.find { it.id == tenant.roomId }?.nomorKamar ?: "?"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentColor
                                        )
                                    }
                                },
                                onClick = {
                                    onTenantSelected(tenant.id)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    }
                }

                // Room Display (Auto-filled)
                if (selectedRoom != null) {
                    ModernSelectedRoomDisplay(room = selectedRoom)
                } else {
                    ModernEmptyRoomDisplay()
                }
            }
        }
    }
}

@Composable
private fun ModernSelectedRoomDisplay(room: com.example.kostkita_app.domain.model.Room) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SuccessColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
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
                    text = "Rp ${String.format("%,d", room.hargaBulanan)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor
                )
            }

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ModernEmptyRoomDisplay() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Pilih penghuni terlebih dahulu",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPaymentDetails(
    bulanTahun: String,
    onBulanTahunChange: (String) -> Unit,
    jumlahBayar: String,
    onJumlahBayarChange: (String) -> Unit,
    statusPembayaran: String,
    statusOptions: List<String>,
    expandedStatus: Boolean,
    onExpandedStatusChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit,
    denda: String,
    onDendaChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Detail Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                }

                // Month/Year Field
                OutlinedTextField(
                    value = bulanTahun,
                    onValueChange = onBulanTahunChange,
                    label = { Text("Bulan/Tahun", color = AccentColor) },
                    placeholder = { Text("Contoh: Januari 2025", color = AccentColor.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SecondaryColor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryColor,
                        focusedLeadingIconColor = SecondaryColor,
                        focusedLabelColor = SecondaryColor,
                        unfocusedBorderColor = AccentColor.copy(alpha = 0.3f)
                    )
                )

                // Amount Field
                OutlinedTextField(
                    value = jumlahBayar,
                    onValueChange = onJumlahBayarChange,
                    label = { Text("Jumlah Bayar", color = AccentColor) },
                    placeholder = { Text("Masukkan jumlah", color = AccentColor.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SecondaryColor)
                    },
                    prefix = { Text("Rp ", color = SecondaryColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryColor,
                        focusedLeadingIconColor = SecondaryColor,
                        focusedLabelColor = SecondaryColor,
                        unfocusedBorderColor = AccentColor.copy(alpha = 0.3f)
                    )
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = onExpandedStatusChange
                ) {
                    OutlinedTextField(
                        value = statusPembayaran,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Pembayaran", color = AccentColor) },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryColor)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryColor,
                            focusedLeadingIconColor = SecondaryColor,
                            focusedLabelColor = SecondaryColor,
                            unfocusedBorderColor = AccentColor.copy(alpha = 0.3f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { onExpandedStatusChange(false) }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        status,
                                        color = OnSurfaceColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onStatusSelected(status)
                                    onExpandedStatusChange(false)
                                }
                            )
                        }
                    }
                }

                // Fine Field
                OutlinedTextField(
                    value = denda,
                    onValueChange = onDendaChange,
                    label = { Text("Denda (Opsional)", color = AccentColor) },
                    placeholder = { Text("0", color = AccentColor.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor)
                    },
                    prefix = { Text("Rp ", color = WarningColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarningColor,
                        focusedLeadingIconColor = WarningColor,
                        focusedLabelColor = WarningColor,
                        unfocusedBorderColor = AccentColor.copy(alpha = 0.3f)
                    )
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
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(800)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryColor,
                disabledContainerColor = AccentColor.copy(alpha = 0.3f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isEdit) Icons.Default.Update else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Text(
                    text = if (isEdit) "Perbarui Pembayaran" else "Simpan Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}