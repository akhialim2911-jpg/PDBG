package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // MODE SELECTOR CARDS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val randomColor by animateColorAsState(if (selectedMode == "Random") RoyalBlue else CardBg)
                    val manualColor by animateColorAsState(if (selectedMode == "Manual") RoyalBlue else CardBg)

                    // Random
                    Card(
                        colors = CardDefaults.cardColors(containerColor = randomColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = "Random" }
                            .testTag("mode_random_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Random mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Auto selects players", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }

                    // Manual
                    Card(
                        colors = CardDefaults.cardColors(containerColor = manualColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = "Manual" }
                            .testTag("mode_manual_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Manual sequence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Custom sorted queue", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                // SUB-CONTENT CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .padding(16.dp)
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
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Automated Roulette Mechanics",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Selection pulls randomly from available players.\n" +
                                        "• Absolute duplication prevention is active (no multi-repeats).\n" +
                                        "• Unsold pool automatically recycles back to queue once empty.",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // MANUAL MODE re-ordering list
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reorder Sequence Pool",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        viewModel.saveManualSequenceToDB()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_order_btn")
                                ) {
                                    Text("Save Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Search bar for manual list reorder
                            OutlinedTextField(
                                value = manualSearchQuery,
                                onValueChange = { text -> manualSearchQuery = text },
                                label = { Text("Filter custom sequence list", color = TextSecondary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                            val displaySequence = manualList.filter {
                                it.name.contains(manualSearchQuery, ignoreCase = true)
                            }

                            if (displaySequence.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No available players matches setup search.", color = TextMuted)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
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
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(player.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text(
                                                        text = "Cat: ${player.category ?: "B"} | Rank: ${player.rank ?: "N/A"}",
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
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
