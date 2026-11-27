package com.nocturne.player.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN albumArtist TEXT"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN genre TEXT"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN year INTEGER"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN composer TEXT"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN trackNumber INTEGER"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN discNumber INTEGER"
        )

        db.execSQL(
            "ALTER TABLE audio_tracks ADD COLUMN isrc TEXT"
        )
    }
}

        val MIGRATION_2_3 = object : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "ALTER TABLE audio_tracks ADD COLUMN audioFormat TEXT"
            )

            db.execSQL(
                "ALTER TABLE audio_tracks ADD COLUMN audioQuality TEXT"
            )

            db.execSQL(
                "ALTER TABLE audio_tracks ADD COLUMN isDolbyAtmos INTEGER"
            )
    }
}

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorites (
                        type TEXT NOT NULL,
                        itemId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        addedAt INTEGER NOT NULL,
                        PRIMARY KEY(type, itemId)
                    )
                """.trimIndent())
            }
       }


val MIGRATION_4_5 = object : Migration(4, 5) {  

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS play_stats (
                trackId INTEGER NOT NULL,
                playCount INTEGER NOT NULL DEFAULT 0,
                lastPlayed INTEGER,
                PRIMARY KEY(trackId)
            )
        """.trimIndent())
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS daily_play_stats (
                trackId INTEGER NOT NULL,
                date TEXT NOT NULL,
                playCount INTEGER NOT NULL,
                PRIMARY KEY(trackId, date)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_daily_play_stats_date
            ON daily_play_stats(date)
        """.trimIndent())
    }
}
