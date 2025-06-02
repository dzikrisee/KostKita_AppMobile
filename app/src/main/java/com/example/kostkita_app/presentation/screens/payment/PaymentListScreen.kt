package com.example.kostkita.presentation.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita.domain.model.Payment
import com.example.kostkita.domain.model.Room
import com.example.kostkita.domain.model.Tenant
import com.example.kostkita.presentation.navigation.KostKitaScreens
import com.example.kostkita.presentation.screens.room.RoomViewModel
import com.example.kostkita.presentation.screens.tenant.TenantViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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

    Scaffold(
        topBar = {
            PaymentTopBar(
                onBackClick = { navController.navigateUp() },
                onSyncClick = { viewModel.syncWithRemote() }
            )
        },
        floatingActionButton = {
            AddPaymentFab(
                onClick = { navController.navigate(KostKitaScreens.PaymentForm.route) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingContent()
                payments.isEmpty() -> EmptyPaymentContent()
                else -> PaymentList(
                    payments = payments,
                    tenants = tenants,
                    rooms = rooms,
                    onPaymentEdit = { payment ->
                        navController.navigate("${KostKitaScreens.PaymentForm.route}/${payment.id}")
                    },
                    onPaymentDelete = { payment ->
                        viewModel.deletePayment(payment)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentTopBar(
    onBackClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Daftar Pembayaran") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onSyncClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync")
            }
        }
    )
}

@Composable
private fun AddPaymentFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Add Payment")
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyPaymentContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum ada data pembayaran",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentList(
    payments: List<Payment>,
    tenants: List<Tenant>,
    rooms: List<Room>,
    onPaymentEdit: (Payment) -> Unit,
    onPaymentDelete: (Payment) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(payments) { payment ->
            val tenant = tenants.find { it.id == payment.tenantId }
            val room = rooms.find { it.id == payment.roomId }

            PaymentCard(
                payment = payment,
                tenant = tenant,
                room = room,
                onEdit = { onPaymentEdit(payment) },
                onDelete = { onPaymentDelete(payment) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentCard(
    payment: Payment,
    tenant: Tenant?,
    room: Room?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Header with tenant name and status
                PaymentCardHeader(
                    tenantName = tenant?.nama ?: "Unknown",
                    status = payment.statusPembayaran
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Room and period info
                Text(
                    text = "Kamar ${room?.nomorKamar ?: "?"} • ${payment.bulanTahun}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Payment amounts
                PaymentAmounts(
                    totalAmount = payment.jumlahBayar,
                    penalty = payment.denda
                )

                // Payment date
                Text(
                    text = "Dibayar: ${formatDate(payment.tanggalBayar)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        PaymentDeleteDialog(
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun PaymentCardHeader(
    tenantName: String,
    status: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tenantName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        PaymentStatusChip(status = status)
    }
}

@Composable
private fun PaymentAmounts(
    totalAmount: Int,
    penalty: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRupiah(totalAmount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        if (penalty > 0) {
            Column {
                Text(
                    text = "Denda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRupiah(penalty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: String) {
    val (containerColor, contentColor) = when (status.lowercase()) {
        "lunas" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "belum bayar" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Pembayaran") },
        text = { Text("Apakah Anda yakin ingin menghapus pembayaran ini?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Helper functions
private fun formatDate(date: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(date)
}

private fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}