package com.nocturne.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("""
        DELETE FROM favorites
        WHERE type = :type AND itemId = :itemId
    """)
    suspend fun removeFavorite(type: String, itemId: String)

    @Query("""
        SELECT * FROM favorites
        WHERE type = :type
        ORDER BY addedAt DESC
    """)
    fun observeFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favorites
            WHERE type = :type AND itemId = :itemId
        )
    """)
    fun observeIsFavorite(type: String, itemId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAllFavorites(): Flow<List<FavoriteEntity>>
}