package com.example.kostkita.presentation.screens.tenant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita.domain.model.Tenant
import com.example.kostkita.presentation.navigation.KostKitaScreens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    navController: NavController,
    viewModel: TenantViewModel = hiltViewModel()
) {
    val tenants by viewModel.tenants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    val filteredTenants = tenants.filter { tenant ->
        searchQuery.isEmpty() || tenant.nama.contains(searchQuery, ignoreCase = true) ||
                tenant.email.contains(searchQuery, ignoreCase = true) ||
                tenant.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                totalTenants = tenants.size,
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
            AddTenantFab(
                onClick = { navController.navigate(KostKitaScreens.TenantForm.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(16.dp)
            )

            // Content
            when {
                isLoading -> LoadingContent()
                filteredTenants.isEmpty() -> EmptyContent(
                    hasSearch = searchQuery.isNotEmpty()
                )
                else -> TenantList(
                    tenants = filteredTenants,
                    onTenantEdit = { tenant ->
                        navController.navigate("${KostKitaScreens.TenantForm.route}/${tenant.id}")
                    },
                    onTenantDelete = { tenant ->
                        viewModel.deleteTenant(tenant)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${tenant.nama} telah dihapus",
                                actionLabel = "Batalkan"
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    totalTenants: Int,
    onBackClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Penghuni Kost",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$totalTenants penghuni terdaftar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onSyncClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun AddTenantFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Tambah Penghuni")
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari nama, email, atau nomor telepon...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
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
private fun EmptyContent(hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PersonOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasSearch)
                    "Tidak ada penghuni yang cocok dengan pencarian"
                else "Belum ada penghuni terdaftar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TenantList(
    tenants: List<Tenant>,
    onTenantEdit: (Tenant) -> Unit,
    onTenantDelete: (Tenant) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = tenants,
            key = { it.id }
        ) { tenant ->
            TenantCard(
                tenant = tenant,
                onEdit = { onTenantEdit(tenant) },
                onDelete = { onTenantDelete(tenant) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TenantCard(
    tenant: Tenant,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        onClick = { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main content
            TenantCardHeader(
                tenant = tenant,
                isExpanded = isExpanded
            )

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TenantCardDetails(
                    tenant = tenant,
                    onEdit = onEdit,
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            tenantName = tenant.nama,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun TenantCardHeader(
    tenant: Tenant,
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        TenantAvatar(name = tenant.nama)

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tenant.nama,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tenant.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tenant.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Status Badge
        StatusBadge(hasRoom = tenant.roomId != null)

        // Expand Arrow
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TenantAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(hasRoom: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasRoom)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = if (hasRoom) "Ada Kamar" else "Belum Ada Kamar",
            style = MaterialTheme.typography.labelSmall,
            color = if (hasRoom)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TenantCardDetails(
    tenant: Tenant,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        // Additional Info
        if (!tenant.nomorKamar.isNullOrEmpty()) {
            InfoRow(
                icon = Icons.Default.Room,
                label = "Nomor Kamar",
                value = tenant.nomorKamar
            )
        }

        if (!tenant.tipeKamar.isNullOrEmpty()) {
            InfoRow(
                icon = Icons.Default.Home,
                label = "Tipe Kamar",
                value = tenant.tipeKamar
            )
        }

        if (tenant.pekerjaan.isNotEmpty()) {
            InfoRow(
                icon = Icons.Default.Work,
                label = "Pekerjaan",
                value = tenant.pekerjaan
            )
        }

        tenant.hargaBulanan?.let { harga ->
            InfoRow(
                icon = Icons.Default.AttachMoney,
                label = "Harga Bulanan",
                value = "Rp ${String.format("%,d", harga)}"
            )
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        InfoRow(
            icon = Icons.Default.CalendarToday,
            label = "Tanggal Masuk",
            value = dateFormat.format(Date(tenant.tanggalMasuk))
        )

        if (tenant.emergencyContact.isNotEmpty()) {
            InfoRow(
                icon = Icons.Default.ContactPhone,
                label = "Kontak Darurat",
                value = tenant.emergencyContact
            )
        }

        // Action Buttons
        ActionButtons(
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(
            onClick = onEdit,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit")
        }

        TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Hapus")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    tenantName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfirmasi Hapus") },
        text = {
            Text("Apakah Anda yakin ingin menghapus penghuni $tenantName? Tindakan ini tidak dapat dibatalkan.")
        },
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