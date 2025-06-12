package com.example.kostkita_app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita_app.domain.model.Payment
import com.example.kostkita_app.domain.model.Room
import com.example.kostkita_app.domain.model.Tenant
import com.example.kostkita_app.presentation.navigation.KostKitaScreens
import com.example.kostkita_app.presentation.screens.room.RoomViewModel
import com.example.kostkita_app.presentation.screens.tenant.TenantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
fun PaymentListScreen(
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    roomViewModel: RoomViewModel = hiltViewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val tenants by tenantViewModel.tenants.collectAsState()
    val rooms by roomViewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf("Semua", "Lunas", "Belum Bayar", "Sebagian")
    val filteredPayments = filterPayments(payments, selectedFilter, searchQuery, tenants)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceColor,
        topBar = {
            ModernPaymentTopBar(
                totalPayments = payments.size,
                onBackClick = { navController.navigateUp() },
                onSyncClick = {
                    viewModel.syncWithRemote()
                    scope.launch {
                        snackbarHostState.showSnackbar("Sinkronisasi dimulai...")
                    }
                }
            )
        },
        floatingActionButton = {
            ModernPaymentFAB(
                onClick = { navController.navigate(KostKitaScreens.PaymentForm.route) }
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
            ) {
                // Search Section
                ModernSearchSection(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Cari nama penghuni atau bulan..."
                )

                // Filter Section
                ModernFilterSection(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                // Payment Stats
                ModernPaymentStats(payments = payments)

                // Content
                when {
                    isLoading -> ModernLoadingContent()
                    filteredPayments.isEmpty() -> ModernEmptyPaymentContent(
                        hasSearch = searchQuery.isNotEmpty() || selectedFilter != "Semua"
                    )
                    else -> ModernPaymentContent(
                        payments = filteredPayments,
                        tenants = tenants,
                        rooms = rooms,
                        onPaymentEdit = { payment ->
                            navController.navigate("${KostKitaScreens.PaymentForm.route}/${payment.id}")
                        },
                        onPaymentDelete = { payment ->
                            viewModel.deletePayment(payment)
                            scope.launch {
                                snackbarHostState.showSnackbar("Pembayaran telah dihapus")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernPaymentTopBar(
    totalPayments: Int,
    onBackClick: () -> Unit,
    onSyncClick: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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

                    Column {
                        Text(
                            text = "Daftar Pembayaran",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = "$totalPayments transaksi tercatat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor
                        )
                    }
                }

                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InfoColor.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = InfoColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    placeholder,
                    color = AccentColor.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = AccentColor
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = AccentColor
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ModernFilterSection(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(filters) { index, filter ->
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = scaleIn() + fadeIn()
            ) {
                ModernFilterChip(
                    label = filter,
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) SecondaryColor else Color.White
    val contentColor = if (selected) Color.White else OnSurfaceColor

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModernPaymentStats(payments: List<Payment>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val lunas = payments.count { it.statusPembayaran.equals("lunas", true) }
        val belumBayar = payments.count { it.statusPembayaran.equals("belum bayar", true) }
        val sebagian = payments.count { it.statusPembayaran.equals("sebagian", true) }

        val totalIncome = payments
            .filter { it.statusPembayaran.equals("lunas", true) }
            .sumOf { it.jumlahBayar }

        ModernPaymentStatCard(
            modifier = Modifier.weight(1f),
            value = lunas.toString(),
            label = "Lunas",
            color = SuccessColor,
            icon = Icons.Default.CheckCircle,
            index = 0
        )
        ModernPaymentStatCard(
            modifier = Modifier.weight(1f),
            value = belumBayar.toString(),
            label = "Belum",
            color = ErrorColor,
            icon = Icons.Default.Schedule,
            index = 1
        )
        ModernPaymentStatCard(
            modifier = Modifier.weight(1f),
            value = formatRupiahCompact(totalIncome.toLong()),
            label = "Total",
            color = SecondaryColor,
            icon = Icons.Default.AttachMoney,
            index = 2
        )
    }
}

@Composable
private fun ModernPaymentStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ModernLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = PrimaryColor,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Memuat data pembayaran...",
                    color = AccentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ModernEmptyPaymentContent(hasSearch: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AccentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = AccentColor
                    )
                }

                Text(
                    text = if (hasSearch)
                        "Tidak ada pembayaran yang sesuai"
                    else "Belum ada data pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceColor,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = if (hasSearch)
                        "Coba ubah filter atau kata kunci"
                    else "Mulai dengan mencatat pembayaran pertama",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor
                )
            }
        }
    }
}

@Composable
private fun ModernPaymentContent(
    payments: List<Payment>,
    tenants: List<Tenant>,
    rooms: List<Room>,
    onPaymentEdit: (Payment) -> Unit,
    onPaymentDelete: (Payment) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 4.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(payments) { index, payment ->
            val tenant = tenants.find { it.id == payment.tenantId }
            val room = rooms.find { it.id == payment.roomId }

            ModernPaymentCard(
                payment = payment,
                tenant = tenant,
                room = room,
                onClick = { onPaymentEdit(payment) },
                onDelete = { onPaymentDelete(payment) },
                index = index
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPaymentCard(
    payment: Payment,
    tenant: Tenant?,
    room: Room?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    index: Int
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        Card(
            onClick = onClick,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with tenant name and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tenant Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
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
                            Text(
                                text = (tenant?.nama?.take(2) ?: "?").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Column {
                            Text(
                                text = tenant?.nama ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceColor
                            )
                            Text(
                                text = "Kamar ${room?.nomorKamar ?: "?"} • ${payment.bulanTahun}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentColor
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModernPaymentStatusBadge(status = payment.statusPembayaran)

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ErrorColor.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Payment details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Jumlah Bayar",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentColor
                        )
                        Text(
                            text = formatRupiah(payment.jumlahBayar),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryColor
                        )
                    }

                    if (payment.denda > 0) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Denda",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentColor
                            )
                            Text(
                                text = formatRupiah(payment.denda),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorColor
                            )
                        }
                    }
                }

                // Payment date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentColor
                    )
                    Text(
                        text = "Dibayar: ${formatDate(payment.tanggalBayar)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentColor
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDeleteDialog(
            title = "Hapus Pembayaran",
            message = "Apakah Anda yakin ingin menghapus pembayaran ini?",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun ModernPaymentStatusBadge(status: String) {
    val (backgroundColor, contentColor, icon) = getPaymentStatusColors(status)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModernDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ErrorColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = ErrorColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }
        },
        text = {
            Text(
                message,
                color = AccentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hapus",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AccentColor
                )
            ) {
                Text(
                    "Batal",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun ModernPaymentFAB(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = SecondaryColor,
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Payment",
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Catat Bayar",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper functions
private fun filterPayments(
    payments: List<Payment>,
    filter: String,
    searchQuery: String,
    tenants: List<Tenant>
): List<Payment> {
    return payments.filter { payment ->
        val matchesFilter = when (filter) {
            "Semua" -> true
            else -> payment.statusPembayaran.equals(filter, ignoreCase = true)
        }

        val tenant = tenants.find { it.id == payment.tenantId }
        val matchesSearch = if (searchQuery.isEmpty()) {
            true
        } else {
            tenant?.nama?.contains(searchQuery, ignoreCase = true) == true ||
                    payment.bulanTahun.contains(searchQuery, ignoreCase = true)
        }

        matchesFilter && matchesSearch
    }
}

private fun getPaymentStatusColors(status: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (status.lowercase()) {
        "lunas" -> Triple(
            SuccessColor.copy(alpha = 0.15f),
            SuccessColor,
            Icons.Default.CheckCircle
        )
        "belum bayar" -> Triple(
            ErrorColor.copy(alpha = 0.15f),
            ErrorColor,
            Icons.Default.Schedule
        )
        else -> Triple(
            WarningColor.copy(alpha = 0.15f),
            WarningColor,
            Icons.Default.Warning
        )
    }
}

private fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

private fun formatRupiahCompact(amount: Long): String {
    return when {
        amount >= 1_000_000_000 -> "Rp ${amount / 1_000_000_000}M"
        amount >= 1_000_000 -> "Rp ${amount / 1_000_000}jt"
        amount >= 1_000 -> "Rp ${amount / 1_000}rb"
        else -> "Rp $amount"
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(Date(timestamp))
}