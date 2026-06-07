package com.example.data

import kotlinx.coroutines.flow.Flow

class AuctionRepository(private val auctionDao: AuctionDao) {
    val allManagers: Flow<List<Manager>> = auctionDao.getAllManagers()
    val allPlayers: Flow<List<Player>> = auctionDao.getAllPlayers()

    suspend fun insertManager(manager: Manager) = auctionDao.insertManager(manager)
    suspend fun updateManager(manager: Manager) = auctionDao.updateManager(manager)
    suspend fun deleteManager(manager: Manager) = auctionDao.deleteManager(manager)
    suspend fun getManagerById(id: Int) = auctionDao.getManagerById(id)
    suspend fun clearManagers() = auctionDao.clearAllManagers()

    suspend fun insertPlayer(player: Player) = auctionDao.insertPlayer(player)
    suspend fun insertPlayers(players: List<Player>) = auctionDao.insertPlayers(players)
    suspend fun updatePlayer(player: Player) = auctionDao.updatePlayer(player)
    suspend fun deletePlayer(player: Player) = auctionDao.deletePlayer(player)
    suspend fun getPlayerById(id: Int) = auctionDao.getPlayerById(id)
    suspend fun clearPlayers() = auctionDao.clearAllPlayers()
}
