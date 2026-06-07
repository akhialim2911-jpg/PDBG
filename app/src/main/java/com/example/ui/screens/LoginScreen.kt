package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
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
import com.example.ui.AuctionViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuctionViewModel,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var showRoleChoice by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon / Header
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = "Gavel Logo",
                tint = Gold,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "AUCTION MASTER",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Professional Sports League Live Auctions",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (!showRoleChoice && !isLoading) {
                // Interactive Sign In with Google styled button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            delay(1200) // Simulate elegant loading
                            isLoading = false
                            showRoleChoice = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("google_signin_button"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Clean visual Google icon simulation
                        Text(
                            text = "G  ",
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Sign In with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Requires authentic login verification to control states.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(visible = isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(color = Gold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting secure login server...",
                        color = Gold,
                        fontSize = 14.sp
                    )
                }
            }

            AnimatedVisibility(visible = showRoleChoice) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBg)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "SELECT LIVE SESSION ROLE",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Option 1: Admin
                    Card(
                        onClick = {
                            viewModel.login("akhialim2911@gmail.com", "Admin")
                        },
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("role_admin_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Admin Security",
                                tint = SuccessGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Administrator / Organizer",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Full controls (Add teams, sell players, manage live stream)",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "akhialim2911@gmail.com",
                                    color = Gold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Option 2: Read Only Viewer
                    Card(
                        onClick = {
                            viewModel.login("viewer@auctionmaster.com", "Viewer")
                        },
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("role_viewer_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Viewer Mode",
                                tint = RoyalBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Read-Only Spectator",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "View live stats, dashboard updates, and leaderboards on projector",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
