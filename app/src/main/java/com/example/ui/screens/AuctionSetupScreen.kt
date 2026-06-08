package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuctionViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionSetupScreen(
    viewModel: AuctionViewModel,
    onStartAuctionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val managersList by viewModel.managers.collectAsStateWithLifecycle(initialValue = emptyList())
    val playersList by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())

    var activeSection by remember { mutableStateOf("Sequence Mode") } // "Sequence Mode", "Player Pool", "Franchises"
    var selectedMode by remember { mutableStateOf("Random") } // "Random" or "Manual"
    var manualSearchQuery by remember { mutableStateOf("") }

    val manualList by viewModel.manualSequence.collectAsStateWithLifecycle()

    // Sync manual initial sequence if empty
    LaunchedEffect(playersList) {
        if (manualList.isEmpty()) {
            viewModel.setManualSequence(playersList.filter { it.status == "Available" || it.status == "Unsold" })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auction Control Panel", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp)
        ) {
            if (managersList.isEmpty() || playersList.isEmpty()) {
                // Empty advisory block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Auction Database Empty",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Live auctions require at least 1 Manager and 1 Player. Register them in the Managers and Players tabs, or tap below to auto-load sample data.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .fillMaxWidth()
                                .testTag("setup_load_demo_btn")
                        ) {
                            Text("Auto-Load Dynamic League Setup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Configure Live Session",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // STYLED SECTION TABS PANEL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Sequence Mode", "Player Pool", "Franchises")
                    tabs.forEach { tab ->
                        val isSelected = activeSection == tab
                        val bgCol by animateColorAsState(if (isSelected) Gold else Color.Transparent)
                        val textCol by animateColorAsState(if (isSelected) DarkNavy else Color.White)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgCol)
                                .clickable { activeSection = tab }
                                .padding(vertical = 10.dp)
                                .testTag("setup_tab_$tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = textCol,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // MAIN CONTENT CONTAINER BY SELECTED TAB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .padding(16.dp)
                ) {
                    when (activeSection) {
                        "Sequence Mode" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // MODE SELECTOR CARDS ROW inside this tab
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val randomColor by animateColorAsState(if (selectedMode == "Random") RoyalBlue else SurfaceBg)
                                    val manualColor by animateColorAsState(if (selectedMode == "Manual") RoyalBlue else SurfaceBg)

                                    // Random
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = randomColor),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedMode = "Random" }
                                            .testTag("mode_random_card")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Random mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Auto selects players", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                                        }
                                    }

                                    // Manual
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = manualColor),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedMode = "Manual" }
                                            .testTag("mode_manual_card")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(imageVector = Icons.Default.Sort, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Manual sequence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Custom sorted queue", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }

                                // Mode Details Container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    if (selectedMode == "Random") {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Casino,
                                                contentDescription = null,
                                                tint = Gold,
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Automated Roulette Mechanics",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "• Selection pulls randomly from available players.\n" +
                                                        "• Absolute duplication prevention is active (no multi-repeats).\n" +
                                                        "• Unsold pool automatically recycles back to queue once empty.",
                                                color = TextSecondary,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }
                                    } else {
                                        // MANUAL SEQUENCE queue adjustments
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Reorder Sequence Pool",
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Button(
                                                    onClick = { viewModel.saveManualSequenceToDB() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(36.dp).testTag("save_order_btn")
                                                ) {
                                                    Text("Save Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = manualSearchQuery,
                                                onValueChange = { manualSearchQuery = it },
                                                label = { Text("Filter custom sequence list", color = TextSecondary, fontSize = 12.sp) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Gold,
                                                    unfocusedBorderColor = SurfaceBg,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                                    .padding(bottom = 4.dp)
                                            )

                                            val displaySequence = manualList.filter {
                                                it.name.contains(manualSearchQuery, ignoreCase = true)
                                            }

                                            if (displaySequence.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No available players matches search.", color = TextMuted)
                                                }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    itemsIndexed(displaySequence) { index, player ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(SurfaceBg)
                                                                .padding(8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(24.dp)
                                                                        .clip(RoundedCornerShape(12.dp))
                                                                        .background(Gold),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "${index + 1}",
                                                                        color = DarkNavy,
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Column {
                                                                    Text(player.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                                    Text(
                                                                        text = "Cat: ${player.category ?: "B"} | Rank: ${player.rank ?: "N/A"}",
                                                                        color = TextSecondary,
                                                                        fontSize = 10.sp
                                                                    )
                                                                }
                                                            }

                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                IconButton(
                                                                    onClick = { viewModel.moveManualPlayerUp(index) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(16.dp))
                                                                }
                                                                IconButton(
                                                                    onClick = { viewModel.moveManualPlayerDown(index) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(16.dp))
                                                                }
                                                                IconButton(
                                                                    onClick = { viewModel.moveManualPlayerToTop(index) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.VerticalAlignTop, contentDescription = "Top", tint = Gold, modifier = Modifier.size(16.dp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Player Pool" -> {
                            var playerSearchText by remember { mutableStateOf("") }
                            var showAddPlayerDialog by remember { mutableStateOf(false) }

                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Preview Player Pool (${playersList.size})",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(
                                        onClick = { showAddPlayerDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp).testTag("setup_add_player_quick_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Player", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedTextField(
                                    value = playerSearchText,
                                    onValueChange = { playerSearchText = it },
                                    placeholder = { Text("Search player from pool...", color = TextMuted, fontSize = 12.sp) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = SurfaceBg,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp)
                                )

                                val displayPlayers = playersList.filter {
                                    it.name.contains(playerSearchText, ignoreCase = true)
                                }

                                if (displayPlayers.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Text("No players in pool match search query.", color = TextMuted, fontSize = 13.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(displayPlayers) { player ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SurfaceBg)
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Category: ${player.category ?: "B"}", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = player.status, color = if (player.status == "Sold") SuccessGreen else if (player.status == "Unsold") ErrorRed else WarningOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deletePlayer(player) },
                                                    modifier = Modifier.size(32.dp).testTag("setup_remove_player_${player.id}")
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Player", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showAddPlayerDialog) {
                                var name by remember { mutableStateOf("") }
                                var rank by remember { mutableStateOf("") }
                                var matchesPlayed by remember { mutableStateOf("") }
                                var winPercentage by remember { mutableStateOf("") }
                                var category by remember { mutableStateOf("A") }
                                var validationError by remember { mutableStateOf<String?>(null) }
                                val cats = listOf("A+", "A", "B", "C", "Rookie")

                                AlertDialog(
                                    onDismissRequest = { showAddPlayerDialog = false },
                                    containerColor = CardBg,
                                    title = { Text("Add Player to Pool", color = Color.White, fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            AnimatedVisibility(visible = validationError != null) {
                                                Text(validationError ?: "", color = ErrorRed, fontSize = 13.sp)
                                            }

                                            OutlinedTextField(
                                                value = name,
                                                onValueChange = { text -> name = text },
                                                label = { Text("Player Name*", color = TextSecondary, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Gold,
                                                    unfocusedBorderColor = SurfaceBg,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().testTag("setup_player_add_name")
                                            )

                                            OutlinedTextField(
                                                value = rank,
                                                onValueChange = { text -> rank = text },
                                                label = { Text("Rank (Optional)", color = TextSecondary, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Gold,
                                                    unfocusedBorderColor = SurfaceBg,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = matchesPlayed,
                                                    onValueChange = { text -> matchesPlayed = text },
                                                    label = { Text("Matches", color = TextSecondary, fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Gold,
                                                        unfocusedBorderColor = SurfaceBg,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = winPercentage,
                                                    onValueChange = { text -> winPercentage = text },
                                                    label = { Text("Win %", color = TextSecondary, fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Gold,
                                                        unfocusedBorderColor = SurfaceBg,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Text("Tier Category*", color = TextSecondary, fontSize = 12.sp)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                cats.forEach { c ->
                                                    val isChosen = category == c
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isChosen) RoyalBlue else SurfaceBg
                                                        ),
                                                        modifier = Modifier
                                                            .clickable { category = c }
                                                            .weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(c, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (name.isBlank()) {
                                                    validationError = "Name is fully mandatory."
                                                    return@Button
                                                }
                                                val mCount = matchesPlayed.toIntOrNull()
                                                val winPct = winPercentage.toDoubleOrNull()
                                                val success = viewModel.addPlayer(name, if (rank.isBlank()) null else rank, mCount, winPct, category)
                                                if (success) {
                                                    showAddPlayerDialog = false
                                                } else {
                                                    validationError = "Saving failed."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Add Player", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showAddPlayerDialog = false }) {
                                            Text("Cancel", color = TextMuted)
                                        }
                                    }
                                )
                            }
                        }

                        "Franchises" -> {
                            var managerSearchText by remember { mutableStateOf("") }
                            var editingManager by remember { mutableStateOf<com.example.data.Manager?>(null) }
                            var showEditBalanceDialog by remember { mutableStateOf(false) }

                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Preview Franchises & Budgets",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = managerSearchText,
                                    onValueChange = { managerSearchText = it },
                                    placeholder = { Text("Search franchise by name...", color = TextMuted, fontSize = 12.sp) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = SurfaceBg,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp)
                                )

                                val displayManagersWithStats by viewModel.managersWithStats.collectAsStateWithLifecycle(initialValue = emptyList())
                                val filteredDisplayManagers = displayManagersWithStats.filter {
                                    it.manager.name.contains(managerSearchText, ignoreCase = true)
                                }

                                if (filteredDisplayManagers.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Text("No franchises found.", color = TextMuted, fontSize = 13.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(filteredDisplayManagers) { mStats ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = mStats.manager.name,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )

                                                        IconButton(
                                                            onClick = {
                                                                editingManager = mStats.manager
                                                                showEditBalanceDialog = true
                                                            },
                                                            modifier = Modifier.size(32.dp).testTag("setup_edit_balance_btn_${mStats.manager.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit Balance",
                                                                tint = Gold,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text("Total Budget", color = TextMuted, fontSize = 10.sp)
                                                            Text("$${String.format("%.0f", mStats.manager.initialBalance)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Column {
                                                            Text("Spent Balance", color = TextMuted, fontSize = 10.sp)
                                                            Text("$${String.format("%.0f", mStats.totalSpending)}", color = WarningOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text("Remaining", color = TextMuted, fontSize = 10.sp)
                                                            Text("$${String.format("%.0f", mStats.remainingBalance)}", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showEditBalanceDialog && editingManager != null) {
                                var newBalanceText by remember { mutableStateOf(String.format("%.0f", editingManager!!.initialBalance)) }
                                var editError by remember { mutableStateOf<String?>(null) }

                                AlertDialog(
                                    onDismissRequest = { showEditBalanceDialog = false },
                                    containerColor = CardBg,
                                    title = { Text("Edit Budget Balance", color = Color.White, fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(text = "Adjust initial budget balance for ${editingManager!!.name}", color = TextSecondary, fontSize = 12.sp)

                                            if (editError != null) {
                                                Text(text = editError!!, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedTextField(
                                                value = newBalanceText,
                                                onValueChange = { newBalanceText = it },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Gold,
                                                    unfocusedBorderColor = SurfaceBg,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().testTag("setup_edit_balance_input")
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                val parsed = newBalanceText.toDoubleOrNull()
                                                if (parsed == null || parsed <= 0.0) {
                                                    editError = "Must be a valid positive amount."
                                                } else {
                                                    viewModel.updateManagerBalance(editingManager!!, parsed) { success, err ->
                                                        if (success) {
                                                            showEditBalanceDialog = false
                                                        } else {
                                                            editError = err ?: "Update failed."
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy)
                                        ) {
                                            Text("Save Balance", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEditBalanceDialog = false }) {
                                            Text("Cancel", color = TextMuted)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GIANT ELEVATED START BUTTON
                Button(
                    onClick = {
                        viewModel.startNewAuctionSession(selectedMode)
                        onStartAuctionSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_auction_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DarkNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START LIVE AUCTION",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
