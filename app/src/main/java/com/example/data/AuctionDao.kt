package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuctionDao {
    // Managers
    @Query("SELECT * FROM managers ORDER BY name ASC")
    fun getAllManagers(): Flow<List<Manager>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManager(manager: Manager)

    @Update
    suspend fun updateManager(manager: Manager)

    @Delete
    suspend fun deleteManager(manager: Manager)

    @Query("SELECT * FROM managers WHERE id = :id")
    suspend fun getManagerById(id: Int): Manager?

    @Query("DELETE FROM managers")
    suspend fun clearAllManagers()

    // Players
    @Query("SELECT * FROM players ORDER BY sequenceOrder ASC, name ASC")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<Player>)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Int): Player?

    @Query("DELETE FROM players")
    suspend fun clearAllPlayers()
}
