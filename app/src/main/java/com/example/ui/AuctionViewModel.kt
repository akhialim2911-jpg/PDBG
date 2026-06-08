package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuctionRepository
import com.example.data.Manager
import com.example.data.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AuctionViewModel(private val repository: AuctionRepository) : ViewModel() {

    // Authentication States
    val isLoggedIn = MutableStateFlow(true)
    val userEmail = MutableStateFlow("")
    val userRole = MutableStateFlow("Admin") // "Admin" or "Viewer"

    // Raw Room Flows converted to StateFlow
    val managers: StateFlow<List<Manager>> = repository.allManagers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val players: StateFlow<List<Player>> = repository.allPlayers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Shown/Visisted player IDs in the current pass (for Random Mode repetition prevention)
    private val shownPlayerIds = mutableStateListOf<Int>()

    // Auction Session States
    val auctionMode = MutableStateFlow("Random") // "Random" or "Manual"
    val activePlayer = MutableStateFlow<Player?>(null)
    val currentBid = MutableStateFlow(0.0)
    val highestBidderId = MutableStateFlow<Int?>(null)
    val auctionNumber = MutableStateFlow(0)
    val isAuctionActive = MutableStateFlow(false)

    // Manual Sequence Custom Order Flow List in ViewModel Memory before starting
    val manualSequence = MutableStateFlow<List<Player>>(emptyList())

    // UI Search & Filters
    val managerSearchQuery = MutableStateFlow("")
    val playerSearchQuery = MutableStateFlow("")
    val playerCategoryFilter = MutableStateFlow("All")
    val playerStatusFilter = MutableStateFlow("All") // "All", "Available", "Sold", "Unsold"

    init {
        // Pre-load manual sequence when players database updates
        viewModelScope.launch {
            players.collect { list ->
                if (manualSequence.value.isEmpty() && list.isNotEmpty()) {
                    manualSequence.value = list.filter { it.status == "Available" || it.status == "Unsold" }
                }
            }
        }
    }

    // Dynamic calculations for Managers (Initial, Current spending, Remaining Balance, Player count)
    val managersWithStats: StateFlow<List<ManagerWithStats>> = combine(managers, players) { managerList, playerList ->
        managerList.map { manager ->
            val boughtPlayers = playerList.filter { it.soldToManagerId == manager.id && it.status == "Sold" }
            val totalSpending = boughtPlayers.sumOf { it.soldAmount ?: 0.0 }
            val remainingBalance = manager.initialBalance - totalSpending
            ManagerWithStats(
                manager = manager,
                totalSpending = totalSpending,
                remainingBalance = remainingBalance,
                purchasedCount = boughtPlayers.size,
                purchasedPlayers = boughtPlayers
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Managers Flow for search
    val filteredManagers: StateFlow<List<ManagerWithStats>> = combine(managersWithStats, managerSearchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.manager.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Players Flow for search and category filters
    val filteredPlayers: StateFlow<List<Player>> = combine(
        players,
        playerSearchQuery,
        playerCategoryFilter,
        playerStatusFilter
    ) { list, query, category, status ->
        var result = list
        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (category != "All") {
            result = result.filter { it.category == category }
        }
        if (status != "All") {
            result = result.filter { it.status.equals(status, ignoreCase = true) }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 1. Authentication Actions
    fun login(email: String, role: String) {
        userEmail.value = email
        userRole.value = role
        isLoggedIn.value = true
    }

    fun logout() {
        isLoggedIn.value = false
        userEmail.value = ""
        userRole.value = "Viewer"
    }

    // 2. Manager Actions
    fun addManager(name: String, balance: Double, onResult: (Boolean, String?) -> Unit) {
        if (name.isBlank()) {
            onResult(false, "Manager Name cannot be empty.")
            return
        }
        if (balance <= 0) {
            onResult(false, "Initial Balance must be greater than zero.")
            return
        }
        viewModelScope.launch {
            repository.insertManager(Manager(name = name, initialBalance = balance))
            onResult(true, null)
        }
    }

    fun deleteManager(manager: Manager) {
        viewModelScope.launch {
            repository.deleteManager(manager)
        }
    }

    // 3. Player Actions
    fun addPlayer(
        name: String,
        rank: String?,
        matchesPlayed: Int?,
        winPercentage: Double?,
        category: String?
    ): Boolean {
        if (name.isBlank()) return false
        viewModelScope.launch {
            val count = players.first().size
            repository.insertPlayer(
                Player(
                    name = name,
                    rank = rank,
                    matchesPlayed = matchesPlayed,
                    winPercentage = winPercentage,
                    category = category,
                    sequenceOrder = count
                )
            )
        }
        return true
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }

    // Manual Reorder methods
    fun setManualSequence(list: List<Player>) {
        manualSequence.value = list
    }

    fun moveManualPlayerUp(index: Int) {
        if (index <= 0) return
        val list = manualSequence.value.toMutableList()
        val temp = list[index]
        list[index] = list[index - 1]
        list[index - 1] = temp
        manualSequence.value = list
    }

    fun moveManualPlayerDown(index: Int) {
        val list = manualSequence.value
        if (index >= list.size - 1) return
        val mutable = list.toMutableList()
        val temp = mutable[index]
        mutable[index] = mutable[index + 1]
        mutable[index + 1] = temp
        manualSequence.value = mutable
    }

    fun moveManualPlayerToTop(index: Int) {
        if (index <= 0) return
        val list = manualSequence.value.toMutableList()
        val p = list.removeAt(index)
        list.add(0, p)
        manualSequence.value = list
    }

    fun moveManualPlayerToBottom(index: Int) {
        val list = manualSequence.value
        if (index >= list.size - 1) return
        val mutable = list.toMutableList()
        val p = mutable.removeAt(index)
        mutable.add(p)
        manualSequence.value = mutable
    }

    fun saveManualSequenceToDB() {
        viewModelScope.launch {
            val list = manualSequence.value
            list.forEachIndexed { idx, player ->
                repository.insertPlayer(player.copy(sequenceOrder = idx))
            }
        }
    }

    // 4. Live Auction Logics
    fun startNewAuctionSession(mode: String) {
        auctionMode.value = mode
        isAuctionActive.value = true
        shownPlayerIds.clear()
        auctionNumber.value = 1
        selectNextPlayer()
    }

    fun stopAuctionSession() {
        isAuctionActive.value = false
        activePlayer.value = null
        currentBid.value = 0.0
        highestBidderId.value = null
    }

    fun selectNextPlayer() {
        viewModelScope.launch {
            val pList = repository.allPlayers.first()
            val available = pList.filter { it.status == "Available" || it.status == "Unsold" }

            if (available.isEmpty()) {
                activePlayer.value = null
                return@launch
            }

            if (auctionMode.value == "Random") {
                val pool = available.filter { it.id !in shownPlayerIds }
                if (pool.isEmpty()) {
                    // All shown once, return unsold/available players back to queue automatically
                    shownPlayerIds.clear()
                    val freshPool = available
                    if (freshPool.isEmpty()) {
                        activePlayer.value = null
                    } else {
                        val selected = freshPool.random()
                        shownPlayerIds.add(selected.id)
                        loadPlayerIntoAuction(selected)
                    }
                } else {
                    val selected = pool.random()
                    shownPlayerIds.add(selected.id)
                    loadPlayerIntoAuction(selected)
                }
            } else {
                // Manual selection - use sequenceOrder
                val sorted = available.sortedBy { it.sequenceOrder }
                if (sorted.isEmpty()) {
                    activePlayer.value = null
                } else {
                    // Pick the first available in the custom sequence
                    val selected = sorted.first()
                    loadPlayerIntoAuction(selected)
                }
            }
        }
    }

    private fun loadPlayerIntoAuction(player: Player) {
        activePlayer.value = player
        currentBid.value = 0.0
        highestBidderId.value = null
    }

    fun updateBid(amount: Double, bidderId: Int?) {
        currentBid.value = amount
        highestBidderId.value = bidderId
    }

    fun markActivePlayerUnsold() {
        val current = activePlayer.value ?: return
        viewModelScope.launch {
            val updated = current.copy(status = "Unsold", timestamp = System.currentTimeMillis())
            repository.insertPlayer(updated)
            auctionNumber.value += 1
            selectNextPlayer()
        }
    }

    fun markActivePlayerSold(managerId: Int, amount: Double, onResult: (Boolean, String?) -> Unit) {
        val current = activePlayer.value ?: return
        viewModelScope.launch {
            val statsList = managersWithStats.first()
            val targetStats = statsList.find { it.manager.id == managerId }

            if (targetStats == null) {
                onResult(false, "Selected manager not found.")
                return@launch
            }

            if (amount > targetStats.remainingBalance) {
                onResult(false, "Insufficient Balance")
                return@launch
            }

            // Update player as sold
            val updated = current.copy(
                status = "Sold",
                soldToManagerId = managerId,
                soldAmount = amount,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPlayer(updated)
            auctionNumber.value += 1
            onResult(true, null)
            selectNextPlayer()
        }
    }

    fun resetAllAuctions() {
        viewModelScope.launch {
            val all = repository.allPlayers.first()
            all.forEach {
                repository.insertPlayer(
                    it.copy(
                        status = "Available",
                        soldToManagerId = null,
                        soldAmount = null,
                        timestamp = null
                    )
                )
            }
            shownPlayerIds.clear()
            activePlayer.value = null
            currentBid.value = 0.0
            highestBidderId.value = null
            auctionNumber.value = 0
            isAuctionActive.value = false
        }
    }

    // Load dynamic/demo data to make first launch gorgeous of course!
    fun loadDemoData() {
        viewModelScope.launch {
            repository.clearManagers()
            repository.clearPlayers()

            val demoManagers = listOf(
                Manager(name = "Mumbai Gladiators", initialBalance = 10000.0),
                Manager(name = "Chennai Titans", initialBalance = 9500.0),
                Manager(name = "Delhi Dynamos", initialBalance = 11000.0),
                Manager(name = "Kolkata Royals", initialBalance = 10500.0)
            )
            demoManagers.forEach { repository.insertManager(it) }

            val demoPlayers = listOf(
                Player(name = "Virat Kohli", rank = "Rank 1", matchesPlayed = 250, winPercentage = 68.5, category = "A+", sequenceOrder = 0),
                Player(name = "MS Dhoni", rank = "Rank 3", matchesPlayed = 350, winPercentage = 71.0, category = "A+", sequenceOrder = 1),
                Player(name = "Rohit Sharma", rank = "Rank 5", matchesPlayed = 243, winPercentage = 64.2, category = "A", sequenceOrder = 2),
                Player(name = "Jasprit Bumrah", rank = "Rank 2", matchesPlayed = 120, winPercentage = 75.0, category = "A+", sequenceOrder = 3),
                Player(name = "K L Rahul", rank = "Rank 12", matchesPlayed = 110, winPercentage = 58.0, category = "A", sequenceOrder = 4),
                Player(name = "Hardik Pandya", rank = "Rank 7", matchesPlayed = 140, winPercentage = 62.1, category = "A", sequenceOrder = 5),
                Player(name = "Rishabh Pant", rank = "Rank 15", matchesPlayed = 98, winPercentage = 55.4, category = "B", sequenceOrder = 6),
                Player(name = "Yuzvendra Chahal", rank = "Rank 22", matchesPlayed = 145, winPercentage = 59.8, category = "B", sequenceOrder = 7),
                Player(name = "Ravi Jadeja", rank = "Rank 9", matchesPlayed = 210, winPercentage = 66.0, category = "A", sequenceOrder = 8),
                Player(name = "Suryakumar Yadav", rank = "Rank 4", matchesPlayed = 85, winPercentage = 73.5, category = "A+", sequenceOrder = 9),
                Player(name = "Sanju Samson", rank = "Rank 28", matchesPlayed = 72, winPercentage = 51.0, category = "C", sequenceOrder = 10),
                Player(name = "Ishan Kishan", rank = "Rank 35", matchesPlayed = 64, winPercentage = 49.3, category = "C", sequenceOrder = 11),
                Player(name = "Shubman Gill", rank = "Rank 11", matchesPlayed = 80, winPercentage = 60.5, category = "B", sequenceOrder = 12),
                Player(name = "Yashasvi Jaiswal", rank = "Rank 18", matchesPlayed = 23, winPercentage = 68.0, category = "Rookie", sequenceOrder = 13),
                Player(name = "Rinku Singh", rank = "Rank 25", matchesPlayed = 30, winPercentage = 70.0, category = "Rookie", sequenceOrder = 14)
            )
            repository.insertPlayers(demoPlayers)
        }
    }

    // Clears all storage database for clean slate
    fun clearDatabase() {
        viewModelScope.launch {
            repository.clearManagers()
            repository.clearPlayers()
            shownPlayerIds.clear()
            activePlayer.value = null
            currentBid.value = 0.0
            highestBidderId.value = null
            isAuctionActive.value = false
        }
    }

    // CSV and Data Exports - Standard formats
    fun getSoldPlayersReportCSV(): String {
        val sList = players.value.filter { it.status == "Sold" }
        val mList = managers.value
        val sb = StringBuilder()
        sb.append("Player ID,Name,Category,Rank,Matches Played,Win %,Sold To,Sold Amount,Timestamp\n")
        sList.forEach { p ->
            val mgrName = mList.find { it.id == p.soldToManagerId }?.name ?: "Unknown"
            sb.append("${p.id},\"${p.name}\",${p.category ?: ""},${p.rank ?: ""},${p.matchesPlayed ?: 0},${p.winPercentage ?: 0.0},\"$mgrName\",${p.soldAmount ?: 0.0},${p.timestamp ?: 0L}\n")
        }
        return sb.toString()
    }

    fun getUnsoldPlayersReportCSV(): String {
        val uList = players.value.filter { it.status == "Unsold" || it.status == "Available" }
        val sb = StringBuilder()
        sb.append("Player ID,Name,Category,Rank,Matches Played,Win %,Status\n")
        uList.forEach { p ->
            sb.append("${p.id},\"${p.name}\",${p.category ?: ""},${p.rank ?: ""},${p.matchesPlayed ?: 0},${p.winPercentage ?: 0.0},${p.status}\n")
        }
        return sb.toString()
    }

    fun getManagerSummaryReportCSV(): String {
        val sb = StringBuilder()
        sb.append("Manager ID,Name,Initial Balance,Remaining Balance,Players Purchased,Total Spent\n")
        managersWithStats.value.forEach { stats ->
            sb.append("${stats.manager.id},\"${stats.manager.name}\",${stats.manager.initialBalance},${stats.remainingBalance},${stats.purchasedCount},${stats.totalSpending}\n")
        }
        return sb.toString()
    }

    // Helper to write CSV to a file and get Intent share Uri
    fun writeCSVToFile(context: Context, fileName: String, content: String): Uri? {
        return try {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
            // Use FileProvider or standard URI
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Wrapper item for complete Manager status combining stats
data class ManagerWithStats(
    val manager: Manager,
    val totalSpending: Double,
    val remainingBalance: Double,
    val purchasedCount: Int,
    val purchasedPlayers: List<Player>
)

class AuctionViewModelFactory(private val repository: AuctionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuctionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
