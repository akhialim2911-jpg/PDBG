package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuctionViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: AuctionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val managersList by viewModel.managersWithStats.collectAsStateWithLifecycle()
    val playersList by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())

    // Tabs inside Results Screen
    var selectedTab by remember { mutableStateOf(0) } // 0: Leaderboard, 1: History, 2: Reports & Export
    val tabTitles = listOf("Leaderboard", "Player History", "Reports & Export")

    // Leaderboard Sort logic: Sort by: 1. Total Players Purchased (Descending), 2. Remaining Balance (Descending)
    val sortedLeaderboard = managersList.sortedWith(
        compareByDescending<com.example.ui.ManagerWithStats> { it.purchasedCount }
            .thenByDescending { it.remainingBalance }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Standings", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        containerColor = DarkNavy,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy)
        ) {
            // Tab Header Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkNavy,
                contentColor = Gold,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Gold
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) Color.White else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // LEADERBOARD SCREEN
                    if (sortedLeaderboard.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No standings available. Register managers first.", color = TextMuted)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sortedLeaderboard) { item ->
                                LeaderboardCardItem(stats = item)
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // Avoid overlaps
                            }
                        }
                    }
                }

                1 -> {
                    // PLAYER HISTORY SCREEN (Searches, Categories Filters & Sold/Unsold Statuses inside ViewModel)
                    PlayerHistoryTab(viewModel = viewModel)
                }

                2 -> {
                    // REPORTS & EXPORT
                    ReportsTab(viewModel = viewModel, context = context)
                }
            }
        }
    }
}

@Composable
fun LeaderboardCardItem(stats: com.example.ui.ManagerWithStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stats.manager.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gold)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${stats.purchasedCount} Bought",
                        color = DarkNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SurfaceBg)
            Spacer(modifier = Modifier.height(12.dp))

            // Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Initial Budget", color = TextMuted, fontSize = 11.sp)
                    Text("$${String.format("%.0f", stats.manager.initialBalance)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Spent Total", color = TextMuted, fontSize = 11.sp)
                    Text("$${String.format("%.0f", stats.totalSpending)}", color = WarningOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Space", color = TextMuted, fontSize = 11.sp)
                    Text("$${String.format("%.0f", stats.remainingBalance)}", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PlayerHistoryTab(viewModel: AuctionViewModel) {
    val playersList by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())
    val managersList by viewModel.managers.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchNameQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("All") }
    var soldFilter by remember { mutableStateOf("All") } // "All", "Sold", "Unsold"

    val filteredList = playersList.filter { player ->
        val matchName = player.name.contains(searchNameQuery, ignoreCase = true)
        val matchCat = categoryFilter == "All" || player.category == categoryFilter
        val matchStatus = when (soldFilter) {
            "Sold" -> player.status == "Sold"
            "Unsold" -> player.status == "Unsold"
            else -> true
        }
        matchName && matchCat && matchStatus
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Embedded Search
        OutlinedTextField(
            value = searchNameQuery,
            onValueChange = { text -> searchNameQuery = text },
            label = { Text("Search Players by Name", color = TextSecondary) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalBlue,
                unfocusedBorderColor = SurfaceBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Filters horizontal row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category scroll
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg)
                ) {
                    Text(
                        text = "Cat: $categoryFilter",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("All", "A+", "A", "B", "C", "Rookie").forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = {
                                categoryFilter = cat
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Sold Status scroll
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg)
                ) {
                    Text(
                        text = "Status: $soldFilter",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("All", "Sold", "Unsold").forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter, color = Color.White) },
                            onClick = {
                                soldFilter = filter
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No transaction history available.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { player ->
                    val soldToName = managersList.find { it.id == player.soldToManagerId }?.name ?: "Unknown"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(player.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (player.status == "Sold") SuccessGreen else ErrorRed)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = player.status,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (player.status == "Sold") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Client: $soldToName", color = TextSecondary, fontSize = 13.sp)
                                    Text(
                                        "Price: $${String.format("%.2f", player.soldAmount ?: 0.0)}",
                                        color = Gold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ReportsTab(viewModel: AuctionViewModel, context: Context) {
    var generatedReportName by remember { mutableStateOf<String?>(null) }
    var generatedReportContent by remember { mutableStateOf<String?>(null) }

    fun triggerShare(title: String, csvContent: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(Intent.createChooser(intent, "Export $title via:"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Download League CSV Outputs",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Secure CSV files containing player listings and franchise budgets. Open in Apple Numbers, Google Sheets or Microsoft Excel.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Action Report 1: Sold Players
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sold Players Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("List of all sold players, buyer managers and metrics.", color = TextMuted, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = {
                            val report = viewModel.getSoldPlayersReportCSV()
                            generatedReportName = "Sold_Players_Report.csv"
                            generatedReportContent = report
                            triggerShare("Sold Players Report", report)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("export_sold_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = Color.White)
                    }
                }
            }
        }

        // Action Report 2: Unsold Players
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unsold Players Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Pool status details of active unpurchased profiles.", color = TextMuted, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = {
                            val report = viewModel.getUnsoldPlayersReportCSV()
                            generatedReportName = "Unsold_Players_Report.csv"
                            generatedReportContent = report
                            triggerShare("Unsold Players Report", report)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("export_unsold_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = Color.White)
                    }
                }
            }
        }

        // Action Report 3: Manager Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manager Balance Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Franchise spending patterns and budget limits.", color = TextMuted, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = {
                            val report = viewModel.getManagerSummaryReportCSV()
                            generatedReportName = "Manager_Summary_Report.csv"
                            generatedReportContent = report
                            triggerShare("Franchise Manager Summary", report)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("export_managers_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = Color.White)
                    }
                }
            }
        }

        // Active Preview Text Block
        if (generatedReportName != null && generatedReportContent != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceBg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview: ${generatedReportName}",
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    generatedReportName = null
                                    generatedReportContent = null
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generatedReportContent ?: "",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
