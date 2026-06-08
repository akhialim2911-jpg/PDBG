package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuctionViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: AuctionViewModel,
    onNavigateToManagers: () -> Unit,
    onNavigateToPlayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val managersList by viewModel.managers.collectAsStateWithLifecycle(initialValue = emptyList())
    val playersList by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()

    val totalManagers = managersList.size
    val totalPlayers = playersList.size
    val soldPlayers = playersList.count { it.status == "Sold" }
    val unsoldPlayers = playersList.count { it.status == "Unsold" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Branding Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Auction Master logo",
                        tint = Gold,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auction Master Console",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Admin Mode icon",
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Admin Mode Active",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Title
        item {
            Text(
                text = "Dashboard Overview",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // TWO LARGE DASHBOARD CARDS (Managers, Players)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Managers",
                    count = totalManagers.toString(),
                    subtext = "Active Franchises",
                    icon = Icons.Default.Groups,
                    iconColor = RoyalBlue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("managers_nav_card")
                        .clickable { onNavigateToManagers() }
                )

                DashboardCard(
                    title = "Players",
                    count = totalPlayers.toString(),
                    subtext = "Total Registered",
                    icon = Icons.Default.SportsCricket,
                    iconColor = Gold,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("players_nav_card")
                        .clickable { onNavigateToPlayers() }
                )
            }
        }

        // Live stats summary banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Live Auction Progress",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProgressStat(label = "Available", count = (totalPlayers - soldPlayers).toString(), color = TextSecondary)
                    ProgressStat(label = "Sold", count = soldPlayers.toString(), color = SuccessGreen)
                    ProgressStat(label = "Unsold Queue", count = unsoldPlayers.toString(), color = WarningOrange)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val pct = if (totalPlayers > 0) (soldPlayers.toFloat() / totalPlayers.toFloat()) else 0f
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SuccessGreen,
                    trackColor = SurfaceBg,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pool clearance rate",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }

        // Demo Actions (Super easy to test the live system!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Administrator Setup Helpers",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (userRole == "Admin") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.loadDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("load_demo_button")
                        ) {
                            Text("Load Demo Data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.clearDatabase() },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("clear_db_button")
                        ) {
                            Text("Reset DB", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Viewing as Spectator. Log out and sign in as Administrator to reset/load demo league templates.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    count: String,
    subtext: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(160.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = count,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtext,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ProgressStat(
    label: String,
    count: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 13.sp
        )
        Text(
            text = count,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
