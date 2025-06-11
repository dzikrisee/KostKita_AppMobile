package com.example.kostkita_app.presentation.screens.tenant

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita_app.domain.model.Tenant
import com.example.kostkita_app.presentation.navigation.KostKitaScreens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
fun TenantListScreen(
    navController: NavController,
    viewModel: TenantViewModel = hiltViewModel()
) {
    val tenants by viewModel.tenants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var headerVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(200)
        contentVisible = true
    }

    val filteredTenants = tenants.filter { tenant ->
        searchQuery.isEmpty() || tenant.nama.contains(searchQuery, ignoreCase = true) ||
                tenant.email.contains(searchQuery, ignoreCase = true) ||
                tenant.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = SurfaceColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ModernTopBar(
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
            ModernAddFab(
                onClick = { navController.navigate(KostKitaScreens.TenantForm.route) }
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
                // Header Card dengan animasi
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = slideInVertically { -it } + fadeIn()
                ) {
                    ModernHeaderCard(
                        totalTenants = tenants.size,
                        occupiedRooms = tenants.count { it.roomId != null }
                    )
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = slideInVertically { it } + fadeIn()
                ) {
                    Column {
                        // Search Bar
                        ModernSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(16.dp)
                        )

                        // Content
                        when {
                            isLoading -> ModernLoadingContent()
                            filteredTenants.isEmpty() -> ModernEmptyContent(
                                hasSearch = searchQuery.isNotEmpty()
                            )
                            else -> ModernTenantList(
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    totalTenants: Int,
    onBackClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Penghuni Kost",
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Text(
                    "$totalTenants penghuni terdaftar",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentColor
                )
            }
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
        actions = {
            IconButton(onClick = onSyncClick) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Sync",
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
    totalTenants: Int,
    occupiedRooms: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModernStatItem(
                icon = Icons.Default.Groups,
                value = totalTenants.toString(),
                label = "Total Penghuni",
                color = InfoColor
            )

            ModernStatItem(
                icon = Icons.Default.Home,
                value = occupiedRooms.toString(),
                label = "Sudah Berkamar",
                color = SuccessColor
            )

            ModernStatItem(
                icon = Icons.Default.PersonOff,
                value = (totalTenants - occupiedRooms).toString(),
                label = "Belum Berkamar",
                color = WarningColor
            )
        }
    }
}

@Composable
private fun ModernStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AccentColor,
            maxLines = 2
        )
    }
}

@Composable
private fun ModernAddFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = SuccessColor,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Tambah Penghuni",
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Cari nama, email, atau nomor telepon...",
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = AccentColor.copy(alpha = 0.3f),
                focusedLeadingIconColor = PrimaryColor,
                unfocusedLeadingIconColor = AccentColor,
                focusedLabelColor = PrimaryColor,
                unfocusedLabelColor = AccentColor
            )
        )
    }
}

@Composable
private fun ModernLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                strokeWidth = 3.dp
            )
            Text(
                text = "Memuat data penghuni...",
                color = AccentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ModernEmptyContent(hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
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
                    imageVector = if (hasSearch) Icons.Default.SearchOff else Icons.Default.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AccentColor.copy(alpha = 0.6f)
                )
            }

            Text(
                text = if (hasSearch)
                    "Tidak ada penghuni yang cocok dengan pencarian"
                else "Belum ada penghuni terdaftar",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentColor,
                fontWeight = FontWeight.Medium
            )

            if (!hasSearch) {
                Text(
                    text = "Tap tombol + untuk menambah penghuni baru",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ModernTenantList(
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
            ModernTenantCard(
                tenant = tenant,
                onEdit = { onTenantEdit(tenant) },
                onDelete = { onTenantDelete(tenant) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTenantCard(
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main content
            ModernTenantCardHeader(
                tenant = tenant,
                isExpanded = isExpanded
            )

            // Expanded content dengan animasi
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ModernTenantCardDetails(
                    tenant = tenant,
                    onEdit = onEdit,
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ModernDeleteDialog(
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
private fun ModernTenantCardHeader(
    tenant: Tenant,
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar dengan gradient
        Box(
            modifier = Modifier
                .size(60.dp)
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
                text = tenant.nama.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tenant.nama,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = OnSurfaceColor
            )

            Text(
                text = tenant.email,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tenant.phone,
                style = MaterialTheme.typography.bodySmall,
                color = AccentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Status Badge
        ModernStatusBadge(hasRoom = tenant.roomId != null)

        Spacer(modifier = Modifier.width(8.dp))

        // Expand Arrow dengan animasi
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = AccentColor,
            modifier = Modifier.rotate(
                animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "expand_arrow_rotation"
                ).value
            )
        )
    }
}

@Composable
private fun ModernStatusBadge(hasRoom: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (hasRoom) SuccessColor.copy(alpha = 0.15f) else WarningColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (hasRoom) Icons.Default.Home else Icons.Default.HomeWork,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (hasRoom) SuccessColor else WarningColor
            )
            Text(
                text = if (hasRoom) "Ada Kamar" else "Belum Ada Kamar",
                style = MaterialTheme.typography.labelSmall,
                color = if (hasRoom) SuccessColor else WarningColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModernTenantCardDetails(
    tenant: Tenant,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = AccentColor.copy(alpha = 0.2f)
        )

        // Additional Info dengan icons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!tenant.nomorKamar.isNullOrEmpty()) {
                ModernInfoRow(
                    icon = Icons.Default.Room,
                    label = "Nomor Kamar",
                    value = tenant.nomorKamar,
                    color = InfoColor
                )
            }

            if (!tenant.tipeKamar.isNullOrEmpty()) {
                ModernInfoRow(
                    icon = Icons.Default.Home,
                    label = "Tipe Kamar",
                    value = tenant.tipeKamar,
                    color = InfoColor
                )
            }

            if (tenant.pekerjaan.isNotEmpty()) {
                ModernInfoRow(
                    icon = Icons.Default.Work,
                    label = "Pekerjaan",
                    value = tenant.pekerjaan,
                    color = SecondaryColor
                )
            }

            tenant.hargaBulanan?.let { harga ->
                ModernInfoRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Harga Bulanan",
                    value = formatRupiah(harga),
                    color = SuccessColor
                )
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            ModernInfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Tanggal Masuk",
                value = dateFormat.format(Date(tenant.tanggalMasuk)),
                color = PrimaryColor
            )

            if (tenant.emergencyContact.isNotEmpty()) {
                ModernInfoRow(
                    icon = Icons.Default.ContactPhone,
                    label = "Kontak Darurat",
                    value = tenant.emergencyContact,
                    color = WarningColor
                )
            }
        }

        // Action Buttons
        ModernActionButtons(
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun ModernInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AccentColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceColor
            )
        }
    }
}

@Composable
private fun ModernActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
    ) {
        OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = InfoColor
            ),
            border = BorderStroke(1.dp, InfoColor.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", fontWeight = FontWeight.Medium)
        }

        OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE74C3C)
            ),
            border = BorderStroke(1.dp, Color(0xFFE74C3C).copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Hapus", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ModernDeleteDialog(
    tenantName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Konfirmasi Hapus",
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )
        },
        text = {
            Text(
                "Apakah Anda yakin ingin menghapus penghuni $tenantName? Tindakan ini tidak dapat dibatalkan.",
                color = AccentColor
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE74C3C)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Hapus", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f))
            ) {
                Text("Batal", color = AccentColor, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

// Helper function
private fun formatRupiah(amount: Int): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
    return format.format(amount)
}