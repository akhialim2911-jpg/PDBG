package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun LiveAuctionScreen(
    viewModel: AuctionViewModel,
    modifier: Modifier = Modifier
) {
    val activePlayer by viewModel.activePlayer.collectAsStateWithLifecycle()
    val managersStats by viewModel.managersWithStats.collectAsStateWithLifecycle(initialValue = emptyList())
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val currentBid by viewModel.currentBid.collectAsStateWithLifecycle()
    val highestBidderId by viewModel.highestBidderId.collectAsStateWithLifecycle()
    val auctionNumber by viewModel.auctionNumber.collectAsStateWithLifecycle()
    val isSessionActive by viewModel.isAuctionActive.collectAsStateWithLifecycle()

    var showSoldDialog by remember { mutableStateOf(false) }

    val highestBidderName = managersStats.find { it.manager.id == highestBidderId }?.manager?.name ?: "No Bids Placed"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Bidding Room", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy),
                actions = {
                    if (isSessionActive && userRole == "Admin") {
                        TextButton(
                            onClick = { viewModel.stopAuctionSession() },
                            modifier = Modifier.testTag("end_auction_session_btn")
                        ) {
                            Text("Stop Session", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        },
        containerColor = DarkNavy,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isSessionActive || activePlayer == null) {
                // Empty state or session stopped
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (activePlayer == null && isSessionActive) "No More Players for Bidding!" else "No Active Auction Session",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (activePlayer == null && isSessionActive) 
                            "All registered players have been processed. Go to Reports or Results for summary output." 
                            else "Initialize modes inside the Setup panel and hit 'Start Live Auction' to open the projection room.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val player = activePlayer!!
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TOP SECTION: PLAYER CARD
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceBg)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${player.category ?: "A+"} Category",
                                        color = Gold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = player.rank ?: "Rank Unspecified",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // LARGE PLAYER NAME DISPLAY (Projector optimization)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = player.name,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Divider(
                                    color = SurfaceBg,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .padding(vertical = 8.dp)
                                )
                            }

                            // STATS MATRIX ROW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("MATCHES", color = TextMuted, fontSize = 12.sp)
                                    Text("${player.matchesPlayed ?: "-"}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("WIN RATE", color = TextMuted, fontSize = 12.sp)
                                    Text(player.winPercentage?.let { "$it%" } ?: "-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MIDDLE SECTION: AUCTION STATUS (Current bid, Bidder, Number)
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE BIDDING",
                                    color = Gold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Round #${auctionNumber}",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Massive Current Bid Display suitable for TV screens/Projectors
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$${String.format("%.2f", currentBid)}",
                                    color = SuccessGreen,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Highest Bidder: $highestBidderName",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Bid Increment Shortcuts (Admin Only)
                            if (userRole == "Admin") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(100.0, 500.0, 1000.0).forEach { amt ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    viewModel.updateBid(currentBid + amt, highestBidderId)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+$${amt.toInt()}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Read-Only Viewer Broadcast Screen",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTTOM SECTION: LARGE ACTION BUTTONS (Sold, Unsold)
                    if (userRole == "Admin") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // UNSOLD
                            Button(
                                onClick = { viewModel.markActivePlayerUnsold() },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("unsold_flow_button")
                            ) {
                                Text("UNSOLD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            }

                            // SOLD
                            Button(
                                onClick = { showSoldDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("sold_flow_button")
                            ) {
                                Text("SOLD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    } else {
                        // Viewer Notice Spacer replacement
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBg)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bidding updates refresh in real-time. Contact local league organizer admin to make bids.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // SOLD CONFIRMATION DIALOG (Select franchise manager & Enter Sold Price value)
        if (showSoldDialog && activePlayer != null) {
            var selectedManagerId by remember { mutableStateOf<Int?>(null) }
            var soldAmountText by remember { mutableStateOf(currentBid.toString()) }
            var dropdownExpanded by remember { mutableStateOf(false) }
            var inlineError by remember { mutableStateOf<String?>(null) }

            val chosenManagerStats = managersStats.find { it.manager.id == selectedManagerId }

            AlertDialog(
                onDismissRequest = { showSoldDialog = false },
                containerColor = CardBg,
                title = { Text("Complete Player Trade", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedVisibility(visible = inlineError != null) {
                            Text(
                                text = inlineError ?: "",
                                color = ErrorRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Assign player \"${activePlayer?.name}\" to franchise manager below:",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        // Custom Dropdown Picker for Franchise Managers
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                                    .testTag("manager_dropdown"),
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = chosenManagerStats?.manager?.name ?: "Select Purchaser Manager*",
                                        color = if (selectedManagerId != null) Color.White else TextSecondary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .background(CardBg)
                                    .fillMaxWidth(0.9f)
                            ) {
                                managersStats.forEach { mStats ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(mStats.manager.name, color = Color.White)
                                                Text("Bal: $${String.format("%.0f", mStats.remainingBalance)}", color = SuccessGreen)
                                            }
                                        },
                                        onClick = {
                                            selectedManagerId = mStats.manager.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Display active remaining balances
                        if (chosenManagerStats != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Franchise Balance Status",
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Initial Budget:", fontSize = 13.sp, color = TextSecondary)
                                        Text("$${String.format("%.2f", chosenManagerStats.manager.initialBalance)}", fontSize = 13.sp, color = Color.White)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Remaining Space:", fontSize = 13.sp, color = TextSecondary)
                                        Text("$${String.format("%.2f", chosenManagerStats.remainingBalance)}", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Input Sold Cost
                        OutlinedTextField(
                            value = soldAmountText,
                            onValueChange = { text -> soldAmountText = text },
                            label = { Text("Final Trade Sold Amount ($)*", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sold_amount_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mgrId = selectedManagerId
                            if (mgrId == null) {
                                inlineError = "Please pick a purchaser franchise manager."
                                return@Button
                            }
                            val cashAmount = soldAmountText.toDoubleOrNull()
                            if (cashAmount == null || cashAmount <= 0) {
                                inlineError = "Sold amount must be greater than zero."
                                return@Button
                            }

                            // Trigger complete logic in ViewModel which handles balance validations
                            viewModel.markActivePlayerSold(mgrId!!, cashAmount) { success, errMsg ->
                                if (success) {
                                    showSoldDialog = false
                                    inlineError = null
                                } else {
                                    inlineError = errMsg // Might yield "Insufficient Balance"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("sold_confirm_submit")
                    ) {
                        Text("Confirm Trade", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSoldDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}
