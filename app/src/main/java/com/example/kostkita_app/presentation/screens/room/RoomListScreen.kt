package com.example.kostkita_app.presentation.screens.room

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import com.example.kostkita_app.domain.model.Room
import com.example.kostkita_app.domain.model.Tenant
import com.example.kostkita_app.presentation.navigation.KostKitaScreens
import com.example.kostkita_app.presentation.screens.tenant.TenantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
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
        containerColor = SurfaceColor,
        topBar = {
            ModernRoomTopBar(
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
            ModernFAB(
                onClick = { navController.navigate(KostKitaScreens.RoomForm.route) }
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
                    onQueryChange = { searchQuery = it }
                )

                // Filter Section
                ModernFilterSection(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                // Stats Section
                ModernRoomStats(rooms = rooms)

                // Content
                when {
                    isLoading -> ModernLoadingContent()
                    filteredRooms.isEmpty() -> ModernEmptyContent(
                        hasSearch = searchQuery.isNotEmpty() || selectedFilter != "Semua"
                    )
                    else -> ModernRoomContent(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernRoomTopBar(
    totalRooms: Int,
    viewMode: ViewMode,
    onBackClick: () -> Unit,
    onViewModeToggle: () -> Unit,
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
                            text = "Manajemen Kamar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = "$totalRooms kamar terdaftar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onViewModeToggle,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID)
                                Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Change View",
                            tint = SecondaryColor
                        )
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
}

@Composable
private fun ModernSearchSection(
    query: String,
    onQueryChange: (String) -> Unit
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
                    "Cari nomor atau tipe kamar...",
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
private fun ModernRoomStats(rooms: List<Room>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val available = rooms.count { it.statusKamar.equals("tersedia", true) }
        val occupied = rooms.count { it.statusKamar.equals("terisi", true) }
        val maintenance = rooms.size - available - occupied

        ModernStatCard(
            modifier = Modifier.weight(1f),
            value = available,
            label = "Tersedia",
            color = SuccessColor,
            icon = Icons.Default.CheckCircle,
            index = 0
        )
        ModernStatCard(
            modifier = Modifier.weight(1f),
            value = occupied,
            label = "Terisi",
            color = InfoColor,
            icon = Icons.Default.People,
            index = 1
        )
        ModernStatCard(
            modifier = Modifier.weight(1f),
            value = maintenance,
            label = "Perbaikan",
            color = WarningColor,
            icon = Icons.Default.Build,
            index = 2
        )
    }
}

@Composable
private fun ModernStatCard(
    modifier: Modifier = Modifier,
    value: Int,
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
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineSmall,
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
                    text = "Memuat data kamar...",
                    color = AccentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ModernEmptyContent(hasSearch: Boolean) {
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
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = AccentColor
                    )
                }

                Text(
                    text = if (hasSearch)
                        "Tidak ada kamar yang sesuai kriteria"
                    else "Belum ada kamar terdaftar",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceColor,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = if (hasSearch)
                        "Coba ubah filter atau kata kunci pencarian"
                    else "Mulai dengan menambahkan kamar pertama",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor
                )
            }
        }
    }
}

@Composable
private fun ModernRoomContent(
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
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rooms) { index, room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    ModernRoomGridCard(
                        room = room,
                        tenant = tenant,
                        onClick = { onRoomEdit(room) },
                        onDelete = { onRoomDelete(room) },
                        index = index
                    )
                }
            }
        }
        ViewMode.LIST -> {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rooms) { index, room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    ModernRoomListCard(
                        room = room,
                        tenant = tenant,
                        onClick = { onRoomEdit(room) },
                        onDelete = { onRoomDelete(room) },
                        index = index
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernRoomGridCard(
    room: Room,
    tenant: Tenant?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    index: Int
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
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
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    ModernStatusBadge(status = room.statusKamar)
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(32.dp)) // Space for buttons

                    // Room info
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Kamar ${room.nomorKamar}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = room.tipeKamar,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Lantai ${room.lantai}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentColor.copy(alpha = 0.7f)
                        )
                    }

                    // Tenant info if occupied
                    if (tenant != null && room.statusKamar.lowercase() == "terisi") {
                        ModernTenantInfoCard(tenantName = tenant.nama)
                    }

                    // Price
                    Text(
                        text = formatRupiah(room.hargaBulanan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryColor
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDeleteDialog(
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
private fun ModernRoomListCard(
    room: Room,
    tenant: Tenant?,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Room icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                getRoomColor(room.statusKamar).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = getRoomColor(room.statusKamar)
                        )
                    }

                    // Room Info
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
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
                            text = formatRupiah(room.hargaBulanan),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryColor
                        )

                        // Show tenant info if room is occupied
                        if (tenant != null && room.statusKamar.lowercase() == "terisi") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = InfoColor
                                )
                                Text(
                                    text = tenant.nama,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InfoColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        ModernStatusBadge(status = room.statusKamar)
                    }
                }

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ErrorColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Edit",
                        tint = AccentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDeleteDialog(
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
private fun ModernTenantInfoCard(tenantName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = InfoColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = InfoColor
            )
            Text(
                text = tenantName,
                style = MaterialTheme.typography.labelMedium,
                color = InfoColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernStatusBadge(status: String) {
    val (backgroundColor, contentColor, icon) = getRoomStatusColors(status)

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
    roomNumber: String,
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
                    "Hapus Kamar",
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }
        },
        text = {
            Text(
                "Apakah Anda yakin ingin menghapus Kamar $roomNumber? Tindakan ini tidak dapat dibatalkan.",
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
private fun ModernFAB(onClick: () -> Unit) {
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
                contentDescription = "Add Room",
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Tambah Kamar",
                fontWeight = FontWeight.Bold
            )
        }
    }
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
        "tersedia" -> SuccessColor
        "terisi" -> InfoColor
        else -> WarningColor
    }
}

private fun getRoomStatusColors(status: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (status.lowercase()) {
        "tersedia" -> Triple(
            SuccessColor.copy(alpha = 0.15f),
            SuccessColor,
            Icons.Default.CheckCircle
        )
        "terisi" -> Triple(
            InfoColor.copy(alpha = 0.15f),
            InfoColor,
            Icons.Default.People
        )
        else -> Triple(
            WarningColor.copy(alpha = 0.15f),
            WarningColor,
            Icons.Default.Build
        )
    }
}

fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}