package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.ManagerWithStats
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagersScreen(
    viewModel: AuctionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val managersList by viewModel.filteredManagers.collectAsStateWithLifecycle(initialValue = emptyList())
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val searchQuery by viewModel.managerSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("League Managers", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        floatingActionButton = {
            if (userRole == "Admin") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Gold,
                    contentColor = DarkNavy,
                    modifier = Modifier.testTag("add_manager_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Manager")
                }
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
                onValueChange = { viewModel.managerSearchQuery.value = it },
                label = { Text("Search Manager by Name", color = TextSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = SurfaceBg,
                    focusedLabelColor = RoyalBlue,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("manager_search_input")
            )

            if (managersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "No managers registered yet." else "No matching managers found.",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                        if (userRole == "Admin" && searchQuery.isBlank()) {
                            Text(
                                text = "Tap the + button to register franchises.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
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
                    items(managersList) { stats ->
                        ManagerCardItem(
                            stats = stats,
                            isAdmin = userRole == "Admin",
                            onDelete = { viewModel.deleteManager(stats.manager) }
                        )
                    }
                }
            }
        }

        // Add Manager Dialog
        if (showAddDialog) {
            var managerName by remember { mutableStateOf("") }
            var managerBalance by remember { mutableStateOf("") }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = CardBg,
                title = { Text("Add New Franchise", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnimatedVisibility(visible = errorMessage != null) {
                            Text(errorMessage ?: "", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        OutlinedTextField(
                            value = managerName,
                            onValueChange = { text -> managerName = text },
                            label = { Text("Manager / Team Name*", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("manager_add_name")
                        )

                        OutlinedTextField(
                            value = managerBalance,
                            onValueChange = { text -> managerBalance = text },
                            label = { Text("Initial Budget Balance ($)*", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = SurfaceBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("manager_add_balance")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bal = managerBalance.toDoubleOrNull() ?: 0.0
                            viewModel.addManager(managerName, bal) { success, err ->
                                if (success) {
                                    showAddDialog = false
                                } else {
                                    errorMessage = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("manager_confirm_add")
                    ) {
                        Text("Add Franchise", fontWeight = FontWeight.Bold)
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
fun ManagerCardItem(
    stats: ManagerWithStats,
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
                Text(
                    text = stats.manager.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isAdmin) {
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_manager_btn")) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
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
                    Text("Total Budget", color = TextMuted, fontSize = 12.sp)
                    Text("$${String.format("%.2f", stats.manager.initialBalance)}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("Spent Balance", color = TextMuted, fontSize = 12.sp)
                    Text("$${String.format("%.2f", stats.totalSpending)}", color = WarningOrange, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                // Auto-calculation of dynamic properties
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", color = TextMuted, fontSize = 12.sp)
                    Text(
                        "$${String.format("%.2f", stats.remainingBalance)}",
                        color = if (stats.remainingBalance > 1000) SuccessGreen else ErrorRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Purchase stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceBg)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Players Purchased:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stats.purchasedCount.toString(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
