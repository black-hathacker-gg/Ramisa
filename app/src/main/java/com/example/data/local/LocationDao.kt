package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLocation(): LocationEntity?

    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocationFlow(): Flow<LocationEntity?>

    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLocations(limit: Int = 50): List<LocationEntity>

    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLocationsFlow(limit: Int = 50): Flow<List<LocationEntity>>

    @Query("SELECT COUNT(*) FROM location_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM location_history WHERE timestamp < :olderThanTimestamp")
    suspend fun deleteOldLocations(olderThanTimestamp: Long)

    @Query("DELETE FROM location_history")
    suspend fun clearAll()
}
