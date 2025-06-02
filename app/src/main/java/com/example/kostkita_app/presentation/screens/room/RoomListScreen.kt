package com.example.kostkita.presentation.screens.room

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita.domain.model.Room
import com.example.kostkita.domain.model.Tenant
import com.example.kostkita.presentation.navigation.KostKitaScreens
import com.example.kostkita.presentation.screens.tenant.TenantViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

enum class ViewMode { GRID, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    navController: NavController,
    viewModel: RoomViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val tenants by tenantViewModel.tenants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedFilter by remember { mutableStateOf("Semua") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf("Semua", "Tersedia", "Terisi", "Maintenance")
    val filteredRooms = filterRooms(rooms, selectedFilter, searchQuery)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RoomTopBar(
                totalRooms = rooms.size,
                viewMode = viewMode,
                onBackClick = { navController.navigateUp() },
                onViewModeToggle = {
                    viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                },
                onSyncClick = {
                    viewModel.syncWithRemote()
                    scope.launch {
                        snackbarHostState.showSnackbar("Sinkronisasi dimulai...")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(KostKitaScreens.RoomForm.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            RoomSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // Filter Chips
            FilterChipsRow(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Room Stats
            RoomStatsRow(rooms = rooms)

            // Content
            when {
                isLoading -> LoadingContent()
                filteredRooms.isEmpty() -> EmptyRoomContent(
                    hasSearch = searchQuery.isNotEmpty() || selectedFilter != "Semua"
                )
                else -> RoomContent(
                    rooms = filteredRooms,
                    tenants = tenants,
                    viewMode = viewMode,
                    onRoomEdit = { room ->
                        navController.navigate("${KostKitaScreens.RoomForm.route}/${room.id}")
                    },
                    onRoomDelete = { room ->
                        viewModel.deleteRoom(room)
                        scope.launch {
                            snackbarHostState.showSnackbar("Kamar ${room.nomorKamar} telah dihapus")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomTopBar(
    totalRooms: Int,
    viewMode: ViewMode,
    onBackClick: () -> Unit,
    onViewModeToggle: () -> Unit,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Manajemen Kamar",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$totalRooms kamar terdaftar",
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
            IconButton(onClick = onViewModeToggle) {
                Icon(
                    imageVector = if (viewMode == ViewMode.GRID)
                        Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Change View"
                )
            }
            IconButton(onClick = onSyncClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync")
            }
        }
    )
}

@Composable
private fun RoomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari nomor atau tipe kamar...") },
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun FilterChipsRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                leadingIcon = if (selectedFilter == filter) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun RoomStatsRow(rooms: List<Room>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val available = rooms.count { it.statusKamar.equals("tersedia", true) }
        val occupied = rooms.count { it.statusKamar.equals("terisi", true) }
        val maintenance = rooms.size - available - occupied

        StatCard(
            modifier = Modifier.weight(1f),
            value = available,
            label = "Tersedia",
            color = Color(0xFF10B981)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = occupied,
            label = "Terisi",
            color = Color(0xFF3B82F6)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = maintenance,
            label = "Perbaikan",
            color = Color(0xFFF59E0B)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: Int,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
private fun EmptyRoomContent(hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearch)
                    "Tidak ada kamar yang sesuai kriteria"
                else "Belum ada kamar terdaftar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoomContent(
    rooms: List<Room>,
    tenants: List<Tenant>,
    viewMode: ViewMode,
    onRoomEdit: (Room) -> Unit,
    onRoomDelete: (Room) -> Unit
) {
    when (viewMode) {
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rooms) { _, room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    RoomGridCard(
                        room = room,
                        tenant = tenant,
                        onClick = { onRoomEdit(room) },
                        onDelete = { onRoomDelete(room) }
                    )
                }
            }
        }
        ViewMode.LIST -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rooms) { _, room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    RoomListCard(
                        room = room,
                        tenant = tenant,
                        onClick = { onRoomEdit(room) },
                        onDelete = { onRoomDelete(room) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomGridCard(
    room: Room,
    tenant: Tenant?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getRoomColor(room.statusKamar).copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Delete button
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Room info
                Column {
                    Text(
                        text = "Kamar ${room.nomorKamar}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = room.tipeKamar,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Lantai ${room.lantai}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tenant info if occupied
                if (tenant != null && room.statusKamar.lowercase() == "terisi") {
                    TenantInfoCard(tenantName = tenant.nama)
                }

                // Price and status
                Column {
                    Text(
                        text = formatRupiah(room.hargaBulanan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RoomStatusBadge(status = room.statusKamar)
                }
            }
        }
    }

    if (showDeleteDialog) {
        RoomDeleteDialog(
            roomNumber = room.nomorKamar,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListCard(
    room: Room,
    tenant: Tenant?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (tenant != null) 140.dp else 120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                    text = formatRupiah(room.hargaBulanan),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Show tenant info if room is occupied
                if (tenant != null && room.statusKamar.lowercase() == "terisi") {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dihuni: ${tenant.nama}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                RoomStatusBadge(status = room.statusKamar)
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        RoomDeleteDialog(
            roomNumber = room.nomorKamar,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun TenantInfoCard(tenantName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tenantName,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RoomStatusBadge(status: String) {
    val (backgroundColor, contentColor, icon) = getRoomStatusColors(status)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
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
private fun RoomDeleteDialog(
    roomNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Kamar") },
        text = { Text("Apakah Anda yakin ingin menghapus Kamar $roomNumber?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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
private fun filterRooms(
    rooms: List<Room>,
    filter: String,
    searchQuery: String
): List<Room> {
    return rooms.filter { room ->
        val matchesFilter = when (filter) {
            "Semua" -> true
            else -> room.statusKamar.equals(filter, ignoreCase = true)
        }
        val matchesSearch = room.nomorKamar.contains(searchQuery, ignoreCase = true) ||
                room.tipeKamar.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }
}

private fun getRoomColor(status: String): Color {
    return when (status.lowercase()) {
        "tersedia" -> Color(0xFF10B981)
        "terisi" -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }
}

private fun getRoomStatusColors(status: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (status.lowercase()) {
        "tersedia" -> Triple(
            Color(0xFF10B981).copy(alpha = 0.2f),
            Color(0xFF10B981),
            Icons.Default.CheckCircle
        )
        "terisi" -> Triple(
            Color(0xFF3B82F6).copy(alpha = 0.2f),
            Color(0xFF3B82F6),
            Icons.Default.People
        )
        else -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.2f),
            Color(0xFFF59E0B),
            Icons.Default.Build
        )
    }
}

fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}