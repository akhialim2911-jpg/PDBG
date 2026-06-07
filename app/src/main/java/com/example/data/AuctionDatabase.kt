package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Manager::class, Player::class], version = 1, exportSchema = false)
abstract class AuctionDatabase : RoomDatabase() {
    abstract fun auctionDao(): AuctionDao

    companion object {
        @Volatile
        private var INSTANCE: AuctionDatabase? = null

        fun getDatabase(context: Context): AuctionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuctionDatabase::class.java,
                    "auction_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
