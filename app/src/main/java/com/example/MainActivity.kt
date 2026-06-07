package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuctionDatabase
import com.example.data.AuctionRepository
import com.example.ui.AuctionViewModel
import com.example.ui.AuctionViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup Room Database, Repository, and ViewModel Factory
        val database = AuctionDatabase.getDatabase(this)
        val repository = AuctionRepository(database.auctionDao())
        val viewModelFactory = AuctionViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AuctionViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        LoginScreen(viewModel = viewModel)
                    } else {
                        MainNavigationContainer(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationContainer(viewModel: AuctionViewModel) {
    // Bottom Tab state (0: Home, 1: Auction, 2: Results)
    var currentTab by remember { mutableStateOf(0) }
    
    // Sub-navigation inside Home tab (e.g. "HomeMain", "ManagersScreen", "PlayersScreen")
    var currentHomeSubScreen by remember { mutableStateOf("HomeMain") }

    val isAuctionActive by viewModel.isAuctionActive.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkNavy,
                contentColor = Gold,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_bar")
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = {
                        currentTab = 0
                        // Clicking Home returns to Home Dashboard Main list
                        currentHomeSubScreen = "HomeMain"
                    },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", color = if (currentTab == 0) Color.White else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        selectedTextColor = Color.White,
                        indicatorColor = Gold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )

                // Live Auction tab
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Gavel, contentDescription = "Auction") },
                    label = { Text("Auction", color = if (currentTab == 1) Color.White else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        selectedTextColor = Color.White,
                        indicatorColor = Gold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_auction_tab")
                )

                // Leaderboard Results tab
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Results") },
                    label = { Text("Results", color = if (currentTab == 2) Color.White else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        selectedTextColor = Color.White,
                        indicatorColor = Gold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_results_tab")
                )
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkNavy)
        ) {
            when (currentTab) {
                0 -> {
                    // Home Tab supporting sub-destinations for Managers & Players details to prevent clutter
                    when (currentHomeSubScreen) {
                        "HomeMain" -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToManagers = {
                                    currentHomeSubScreen = "ManagersScreen"
                                },
                                onNavigateToPlayers = {
                                    currentHomeSubScreen = "PlayersScreen"
                                }
                            )
                        }
                        "ManagersScreen" -> {
                            ManagersScreen(
                                viewModel = viewModel,
                                onBack = { currentHomeSubScreen = "HomeMain" }
                            )
                        }
                        "PlayersScreen" -> {
                            PlayersScreen(
                                viewModel = viewModel,
                                onBack = { currentHomeSubScreen = "HomeMain" }
                            )
                        }
                    }
                }

                1 -> {
                    // Auction Tab: Redirects dynamically to Live Bidding Room when a session is active
                    if (isAuctionActive) {
                        LiveAuctionScreen(viewModel = viewModel)
                    } else {
                        AuctionSetupScreen(
                            viewModel = viewModel,
                            onStartAuctionSuccess = {
                                // Handled automatically by dynamic VM state reading
                            }
                        )
                    }
                }

                2 -> {
                    // Standings & Leaderboard Results Tab
                    ResultsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
