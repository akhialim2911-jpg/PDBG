package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "managers")
data class Manager(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initialBalance: Double
)

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rank: String? = null,
    val matchesPlayed: Int? = null,
    val winPercentage: Double? = null,
    val category: String? = null, // A+, A, B, C, Rookie
    val status: String = "Available", // Available, Sold, Unsold
    val soldToManagerId: Int? = null,
    val soldAmount: Double? = null,
    val timestamp: Long? = null,
    val sequenceOrder: Int = 0
)
