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