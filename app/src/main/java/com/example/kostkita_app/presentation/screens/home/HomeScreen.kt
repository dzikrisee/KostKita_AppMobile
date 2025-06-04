package com.example.kostkita_app.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kostkita_app.domain.model.Payment
import com.example.kostkita_app.domain.model.Room
import com.example.kostkita_app.domain.model.Tenant
import com.example.kostkita_app.presentation.navigation.KostKitaScreens
import com.example.kostkita_app.presentation.screens.room.RoomViewModel
import com.example.kostkita_app.presentation.screens.tenant.TenantViewModel
import com.example.kostkita_app.presentation.screens.payment.PaymentViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Modern Color Palette - matching the splash screen
private val PrimaryColor = Color(0xFFB8A491) // Soft beige from splash
private val SecondaryColor = Color(0xFFF5B041) // Warm orange
private val AccentColor = Color(0xFF8B7355) // Darker beige
private val SurfaceColor = Color(0xFFFAF8F5) // Light cream
private val OnSurfaceColor = Color(0xFF3C3C3C) // Dark gray
private val SuccessColor = Color(0xFF27AE60) // Fresh green
private val WarningColor = Color(0xFFF39C12) // Warm orange
private val ErrorColor = Color(0xFFE74C3C) // Soft red
private val InfoColor = Color(0xFF3498DB) // Sky blue

@Composable
fun HomeScreen(
    navController: NavController,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    roomViewModel: RoomViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val tenants by tenantViewModel.tenants.collectAsState()
    val rooms by roomViewModel.rooms.collectAsState()
    val payments by paymentViewModel.payments.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Statistik", "Aktivitas")

    Scaffold(
        containerColor = SurfaceColor
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header Section
                item {
                    ModernHeaderSection(
                        onProfileClick = {
                            navController.navigate(KostKitaScreens.Profile.route)
                        }
                    )
                }

                // Tab Row
                item {
                    ModernTabRow(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        item {
                            ModernStatsGrid(
                                totalTenants = tenants.size,
                                occupiedRooms = rooms.count { it.statusKamar.lowercase() == "terisi" },
                                totalRooms = rooms.size,
                                monthlyIncome = calculateMonthlyIncome(payments)
                            )
                        }

                        item {
                            ModernQuickActions(navController)
                        }

                        item {
                            ModernActivityFeed(
                                tenants = tenants.takeLast(5),
                                payments = payments.takeLast(5)
                            )
                        }
                    }
                    1 -> {
                        item {
                            ModernChartSection(rooms)
                        }
                    }
                    2 -> {
                        item {
                            ModernActivityTimeline(tenants, payments)
                        }
                    }
                }
            }

            // Modern Floating Navigation Bar
            ModernBottomNavigation(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ModernHeaderSection(onProfileClick: () -> Unit = {}) {
    var visible by remember { mutableStateOf(false) }
    val greeting = getGreeting()

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
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "KostKita",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor.copy(alpha = 0.7f)
                    )
                }

                // Modern Profile Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryColor,
                                    SecondaryColor
                                )
                            )
                        )
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.padding(vertical = 16.dp),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SecondaryColor)
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                ModernTabItem(
                    title = title,
                    isSelected = selectedTab == index
                )
            }
        }
    }
}

@Composable
fun ModernTabItem(title: String, isSelected: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SecondaryColor else AccentColor
        )
    }
}

@Composable
fun ModernStatsGrid(
    totalTenants: Int,
    occupiedRooms: Int,
    totalRooms: Int,
    monthlyIncome: Long
) {
    val stats = listOf(
        StatItem(
            icon = Icons.Default.Groups,
            title = "Total Penghuni",
            value = totalTenants.toString(),
            color = InfoColor,
            trend = "+${(1..5).random()}%"
        ),
        StatItem(
            icon = Icons.Default.MeetingRoom,
            title = "Kamar Terisi",
            value = "$occupiedRooms/$totalRooms",
            color = SuccessColor,
            trend = "${((occupiedRooms.toFloat() / totalRooms.coerceAtLeast(1)) * 100).toInt()}%"
        ),
        StatItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Pendapatan",
            value = formatRupiahCompact(monthlyIncome),
            color = SecondaryColor,
            trend = "+12%"
        ),
        StatItem(
            icon = Icons.Default.CalendarToday,
            title = "Jatuh Tempo",
            value = "${(1..5).random()}",
            color = WarningColor,
            trend = "Hari ini"
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stats.size) { index ->
            ModernStatCard(
                stat = stats[index],
                index = index
            )
        }
    }
}

@Composable
fun ModernStatCard(stat: StatItem, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(stat.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = stat.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = stat.color
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = stat.color.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = stat.trend,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = stat.color
                        )
                    }
                }

                Column {
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = stat.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentColor
                    )
                }
            }
        }
    }
}

@Composable
fun ModernQuickActions(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PersonAdd,
                label = "Tambah\nPenghuni",
                color = InfoColor,
                onClick = { navController.navigate(KostKitaScreens.TenantForm.route) },
                delay = 0
            )

            ModernActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AddHome,
                label = "Tambah\nKamar",
                color = SuccessColor,
                onClick = { navController.navigate(KostKitaScreens.RoomForm.route) },
                delay = 100
            )

            ModernActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Receipt,
                label = "Catat\nBayar",
                color = SecondaryColor,
                onClick = { navController.navigate(KostKitaScreens.PaymentForm.route) },
                delay = 200
            )
        }
    }
}

@Composable
fun ModernActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        modifier = modifier
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.1f),
                                color.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ModernActivityFeed(
    tenants: List<Tenant>,
    payments: List<Payment>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terkini",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PulsingDot()
                Text(
                    text = "Live",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val activities = (tenants.map { ActivityData.Tenant(it) } +
                payments.map { ActivityData.Payment(it) })
            .sortedByDescending {
                when (it) {
                    is ActivityData.Tenant -> it.tenant.tanggalMasuk
                    is ActivityData.Payment -> it.payment.tanggalBayar
                }
            }
            .take(5)

        activities.forEachIndexed { index, activity ->
            ModernActivityCard(activity = activity, index = index)
            if (index < activities.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ModernActivityCard(activity: ActivityData, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 150L)
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when (activity) {
                is ActivityData.Tenant -> ModernTenantActivityItem(activity.tenant)
                is ActivityData.Payment -> ModernPaymentActivityItem(activity.payment)
            }
        }
    }
}

@Composable
fun ModernTenantActivityItem(tenant: Tenant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(InfoColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tenant.nama.take(2).uppercase(),
                color = InfoColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tenant.nama,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceColor
            )
            Text(
                text = "Penghuni baru • ${formatDateRelative(tenant.tanggalMasuk)}",
                style = MaterialTheme.typography.bodySmall,
                color = AccentColor
            )
        }

        Icon(
            Icons.Default.PersonAdd,
            contentDescription = null,
            tint = InfoColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ModernPaymentActivityItem(payment: Payment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (payment.statusPembayaran == "Lunas")
                        SuccessColor.copy(alpha = 0.2f)
                    else
                        ErrorColor.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (payment.statusPembayaran == "Lunas")
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Schedule,
                contentDescription = null,
                tint = if (payment.statusPembayaran == "Lunas")
                    SuccessColor
                else
                    ErrorColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pembayaran ${payment.bulanTahun}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceColor
            )
            Text(
                text = "${formatRupiah(payment.jumlahBayar)} • ${formatDateRelative(payment.tanggalBayar)}",
                style = MaterialTheme.typography.bodySmall,
                color = AccentColor
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (payment.statusPembayaran == "Lunas")
                SuccessColor.copy(alpha = 0.1f)
            else
                ErrorColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = payment.statusPembayaran,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = if (payment.statusPembayaran == "Lunas")
                    SuccessColor
                else
                    ErrorColor
            )
        }
    }
}

@Composable
fun ModernChartSection(rooms: List<Room>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Statistik Hunian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )
            Spacer(modifier = Modifier.height(20.dp))

            val occupied = rooms.count { it.statusKamar.lowercase() == "terisi" }
            val available = rooms.count { it.statusKamar.lowercase() == "tersedia" }
            val maintenance = rooms.size - occupied - available

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernStatisticItem(
                    label = "Terisi",
                    value = occupied,
                    color = SuccessColor,
                    percentage = if (rooms.isNotEmpty()) (occupied.toFloat() / rooms.size * 100).toInt() else 0
                )
                ModernStatisticItem(
                    label = "Tersedia",
                    value = available,
                    color = InfoColor,
                    percentage = if (rooms.isNotEmpty()) (available.toFloat() / rooms.size * 100).toInt() else 0
                )
                ModernStatisticItem(
                    label = "Maintenance",
                    value = maintenance,
                    color = WarningColor,
                    percentage = if (rooms.isNotEmpty()) (maintenance.toFloat() / rooms.size * 100).toInt() else 0
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (rooms.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentColor.copy(alpha = 0.2f))
                ) {
                    if (occupied > 0) {
                        Box(
                            modifier = Modifier
                                .weight(occupied.toFloat())
                                .fillMaxHeight()
                                .background(SuccessColor)
                        )
                    }
                    if (available > 0) {
                        Box(
                            modifier = Modifier
                                .weight(available.toFloat())
                                .fillMaxHeight()
                                .background(InfoColor)
                        )
                    }
                    if (maintenance > 0) {
                        Box(
                            modifier = Modifier
                                .weight(maintenance.toFloat())
                                .fillMaxHeight()
                                .background(WarningColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatisticItem(
    label: String,
    value: Int,
    color: Color,
    percentage: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceColor
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = AccentColor
        )
    }
}

@Composable
fun ModernActivityTimeline(
    tenants: List<Tenant>,
    payments: List<Payment>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Timeline Aktivitas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val allActivities = buildList {
                tenants.forEach { tenant ->
                    add(
                        TimelineEvent(
                            timestamp = tenant.tanggalMasuk,
                            title = "Penghuni Baru",
                            description = "${tenant.nama} bergabung",
                            icon = Icons.Default.PersonAdd,
                            color = InfoColor
                        )
                    )
                }
                payments.forEach { payment ->
                    add(
                        TimelineEvent(
                            timestamp = payment.tanggalBayar,
                            title = "Pembayaran",
                            description = "${payment.bulanTahun} - ${formatRupiah(payment.jumlahBayar)}",
                            icon = Icons.Default.AttachMoney,
                            color = if (payment.statusPembayaran == "Lunas") SuccessColor else ErrorColor
                        )
                    )
                }
            }.sortedByDescending { it.timestamp }.take(5)

            allActivities.forEachIndexed { index, event ->
                ModernTimelineItem(
                    event = event,
                    isLast = index == allActivities.lastIndex
                )
            }
        }
    }
}

@Composable
fun ModernTimelineItem(
    event: TimelineEvent,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(event.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = event.color
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(AccentColor.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = formatDateRelative(event.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentColor
                    )
                }
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentColor
                )
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernNavItem(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                selected = true,
                onClick = { }
            )
            ModernNavItem(
                icon = Icons.Default.Person,
                label = "Penghuni",
                selected = false,
                onClick = { navController.navigate(KostKitaScreens.TenantList.route) }
            )
            ModernNavItem(
                icon = Icons.Default.MeetingRoom,
                label = "Kamar",
                selected = false,
                onClick = { navController.navigate(KostKitaScreens.RoomList.route) }
            )
            ModernNavItem(
                icon = Icons.Default.Receipt,
                label = "Bayar",
                selected = false,
                onClick = { navController.navigate(KostKitaScreens.PaymentList.route) }
            )
        }
    }
}

@Composable
fun ModernNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) SecondaryColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) SecondaryColor else AccentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) SecondaryColor else AccentColor,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(SuccessColor)
    )
}

// Data classes
data class StatItem(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val color: Color,
    val trend: String
)

sealed class ActivityData {
    data class Tenant(val tenant: com.example.kostkita_app.domain.model.Tenant) : ActivityData()
    data class Payment(val payment: com.example.kostkita_app.domain.model.Payment) : ActivityData()
}

data class TimelineEvent(
    val timestamp: Long,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

// Helper functions
fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Selamat Pagi ☀️"
        hour < 15 -> "Selamat Siang 🌤️"
        hour < 18 -> "Selamat Sore 🌅"
        else -> "Selamat Malam 🌙"
    }
}

fun calculateMonthlyIncome(payments: List<Payment>): Long {
    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
    return payments
        .filter { it.bulanTahun.contains(currentMonth) && it.statusPembayaran == "Lunas" }
        .sumOf { it.jumlahBayar.toLong() }
}

fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

fun formatRupiahCompact(amount: Long): String {
    return when {
        amount >= 1_000_000_000 -> "Rp ${amount / 1_000_000_000}M"
        amount >= 1_000_000 -> "Rp ${amount / 1_000_000}jt"
        amount >= 1_000 -> "Rp ${amount / 1_000}rb"
        else -> "Rp $amount"
    }
}

fun formatDateRelative(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days == 0L -> "hari ini"
        days == 1L -> "kemarin"
        days < 7 -> "$days hari lalu"
        days < 30 -> "${days / 7} minggu lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(timestamp))
    }
}