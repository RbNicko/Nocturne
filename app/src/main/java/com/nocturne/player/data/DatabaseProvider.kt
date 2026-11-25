package com.nocturne.player.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: MusicDatabase? = null

    fun getDatabase(context: Context): MusicDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                MusicDatabase::class.java,
                "nocturne_database"
            )
                .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3
                )

                .build()

            INSTANCE = instance

            instance
        }
    }
}