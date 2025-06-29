package com.example.kostkita_app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
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
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // State untuk popup dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

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

    LaunchedEffect(payment) {
        payment?.let { p ->
            selectedTenantId = p.tenantId
            selectedRoomId = p.roomId
            bulanTahun = p.bulanTahun
            jumlahBayar = p.jumlahBayar.toString()
            statusPembayaran = p.statusPembayaran
            denda = p.denda.toString()
        }
    }

    // Handle success/error feedback
    LaunchedEffect(saveSuccess, saveError) {
        when {
            saveSuccess -> {
                isEditMode = payment != null
                dialogMessage = if (payment != null) {
                    "Pembayaran berhasil diperbarui!"
                } else {
                    "Pembayaran baru berhasil ditambahkan!"
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
//        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceColor,
        topBar = {
            ModernFormTopBar(
                title = if (paymentId == null) "Tambah Pembayaran" else "Detail Pembayaran",
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
            // Update bagian Column content di PaymentFormScreen.kt:

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Payment Detail Card - HANYA MUNCUL SAAT EDIT (paymentId != null)
                if (paymentId != null && payment != null) {
                    PaymentDetailCard(
                        payment = payment,
                        tenants = tenants,
                        rooms = rooms
                    )
                }

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
                            jumlahBayar.isNotBlank() &&
                            !isSaving, // Tambahkan ini
                    isEdit = payment != null,
                    isLoading = isSaving, // Tambahkan ini
                    onClick = {
                        scope.launch {
                            try {
                                isSaving = true
                                saveSuccess = false
                                saveError = null

                                if (payment == null) {
                                    // Tambah pembayaran baru
                                    viewModel.addPayment(
                                        tenantId = selectedTenantId,
                                        roomId = selectedRoomId,
                                        bulanTahun = bulanTahun,
                                        jumlahBayar = jumlahBayar.toIntOrNull() ?: 0,
                                        statusPembayaran = statusPembayaran,
                                        denda = denda.toIntOrNull() ?: 0
                                    )
                                } else {
                                    // Update pembayaran
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

                                // Simulasi delay untuk UX yang lebih baik
                                kotlinx.coroutines.delay(1000)
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

    // Error Dialog
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

// Success Dialog Component
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
                    text = if (isEdit) "Data pembayaran telah diperbarui dengan informasi terbaru"
                    else "Data pembayaran baru telah tersimpan dalam sistem",
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
                // Error Icon dengan animasi
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
                    text = "Ops, Ada Masalah!",
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
                    color = ErrorColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Silakan periksa kembali data yang dimasukkan atau coba lagi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tutup button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentColor
                    ),
                    border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Tutup",
                        fontWeight = FontWeight.Medium
                    )
                }

                // Coba Lagi button
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningColor,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Coba Lagi",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
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
private fun PaymentDetailCard(
    payment: com.example.kostkita_app.domain.model.Payment?,
    tenants: List<com.example.kostkita_app.domain.model.Tenant>,
    rooms: List<com.example.kostkita_app.domain.model.Room>
) {
    if (payment == null) return

    val tenant = tenants.find { it.id == payment.tenantId }
    val room = rooms.find { it.id == payment.roomId }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
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
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                SecondaryColor.copy(alpha = 0.1f),
                                PrimaryColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detail Pembayaran",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = "ID: ${payment.id.take(8)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentColor
                        )
                    }

                    // Status Badge
                    StatusBadge(status = payment.statusPembayaran)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Detail Information
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tenant & Room Info
                    DetailRow(
                        icon = Icons.Default.Person,
                        label = "Penghuni",
                        value = tenant?.nama ?: "Tidak diketahui",
                        subtitle = "Kamar ${room?.nomorKamar ?: "N/A"} - ${room?.tipeKamar ?: "N/A"}"
                    )

                    // Payment Period
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Periode",
                        value = payment.bulanTahun,
                        subtitle = "Tanggal Bayar: ${formatTimestamp(payment.tanggalBayar)}"
                    )

                    // Amount Details
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Jumlah Bayar",
                        value = formatCurrency(payment.jumlahBayar),
                        subtitle = if (payment.denda > 0) "Denda: ${formatCurrency(payment.denda)}" else "Tidak ada denda"
                    )

                    // Total Amount
                    if (payment.denda > 0) {
                        HorizontalDivider(
                            color = AccentColor.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        DetailRow(
                            icon = Icons.Default.Calculate,
                            label = "Total Keseluruhan",
                            value = formatCurrency(payment.jumlahBayar + payment.denda),
                            subtitle = "Pembayaran + Denda",
                            isTotal = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "lunas" -> SecondaryColor to Color.White
        "belum lunas" -> ErrorColor to Color.White
        "pending" -> WarningColor to Color.White
        else -> AccentColor to Color.White
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtitle: String? = null,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isTotal) SecondaryColor else AccentColor,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isTotal) SecondaryColor else OnSurfaceColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentColor.copy(alpha = 0.8f)
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
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1000)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn()
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEdit) WarningColor else SuccessColor,
                contentColor = Color.White,
                disabledContainerColor = AccentColor.copy(alpha = 0.3f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (enabled) 4.dp else 0.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = if (isEdit) "Memperbarui..." else "Menyimpan...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        if (isEdit) Icons.Default.Update else Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isEdit) "📝 Perbarui Pembayaran" else "💾 Simpan Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper functions - tambahkan di bagian bawah file
private fun formatCurrency(amount: Int): String {
    return "Rp ${String.format("%,d", amount).replace(',', '.')}"
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
    return format.format(date)
}