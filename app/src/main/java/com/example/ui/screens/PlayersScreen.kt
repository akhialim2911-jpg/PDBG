package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuctionViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    viewModel: AuctionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playersList by viewModel.filteredPlayers.collectAsStateWithLifecycle(initialValue = emptyList())
    val searchQuery by viewModel.playerSearchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.playerCategoryFilter.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.playerStatusFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "A+", "A", "B", "C", "Rookie")
    val statuses = listOf("All", "Available", "Sold", "Unsold")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registered Players", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Gold,
                contentColor = DarkNavy,
                modifier = Modifier.testTag("add_player_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Player")
            }
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.playerSearchQuery.value = it },
                label = { Text("Search Player by Name", color = TextSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = SurfaceBg,
                    focusedLabelColor = RoyalBlue,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("player_search_input")
            )

            // Category Filter Scroll Row
            Text(
                text = "Filter Category",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RoyalBlue else CardBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.playerCategoryFilter.value = cat }
                            .testTag("category_filter_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Auction Status Filter Scroll Row
            Text(
                text = "Filter Status",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statuses) { stat ->
                    val isSelected = selectedStatusFilter.equals(stat, ignoreCase = true)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Gold else CardBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.playerStatusFilter.value = stat }
                            .testTag("status_filter_$stat")
                    ) {
                        Text(
                            text = stat,
                            color = if (isSelected) DarkNavy else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (playersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No players found.",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Try adjusting filters or registering new entries.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playersList) { player ->
                        PlayerCardItem(
                            player = player,
                            isAdmin = true,
                            onDelete = { viewModel.deletePlayer(player) }
                        )
                    }
                }
            }
        }

        // Add Player Dialog
        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var rank by remember { mutableStateOf("") }
            var matchesPlayed by remember { mutableStateOf("") }
            var winPercentage by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("A") }
            var validationError by remember { mutableStateOf<String?>(null) }

            val cats = listOf("A+", "A", "B", "C", "Rookie")

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = CardBg,
                title = { Text("Add Player Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedVisibility(visible = validationError != null) {
                            Text(validationError ?: "", color = ErrorRed, fontSize = 14.sp)
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { text -> name = text },
                            label = { Text("Player Name*", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("player_add_name")
                        )

                        OutlinedTextField(
                            value = rank,
                            onValueChange = { text -> rank = text },
                            label = { Text("Rank (Optional)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("player_add_rank")
                        )

                        OutlinedTextField(
                            value = matchesPlayed,
                            onValueChange = { text -> matchesPlayed = text },
                            label = { Text("Matches Played (Optional)", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("player_add_matches")
                        )

                        OutlinedTextField(
                            value = winPercentage,
                            onValueChange = { text -> winPercentage = text },
                            label = { Text("Win Percentage % (Optional)", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("player_add_win_pct")
                        )

                        Text("Tier Category*", color = TextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(c, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                showAddDialog = false
                            } else {
                                validationError = "Saving failed."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Player", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerCardItem(
    player: com.example.data.Player,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = player.rank ?: "No Rank Registered",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoyalBlue)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${player.category ?: "C"}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAdmin) {
                        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_player_btn")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SurfaceBg)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Matches", color = TextMuted, fontSize = 11.sp)
                    Text("${player.matchesPlayed ?: "-"}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Win %", color = TextMuted, fontSize = 11.sp)
                    Text(player.winPercentage?.let { "$it%" } ?: "-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Auction Status", color = TextMuted, fontSize = 11.sp)
                    val statusColor = when (player.status) {
                        "Sold" -> SuccessGreen
                        "Unsold" -> ErrorRed
                        else -> WarningOrange
                    }
                    Text(
                        text = player.status,
                        color = statusColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (player.status == "Sold" && player.soldAmount != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sold Price:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        "$${String.format("%.2f", player.soldAmount)}",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
