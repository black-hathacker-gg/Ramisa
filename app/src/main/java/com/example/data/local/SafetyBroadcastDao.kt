package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyBroadcastDao {
  @Query("SELECT * FROM safety_broadcasts ORDER BY id DESC")
  fun getAllBroadcasts(): Flow<List<SafetyBroadcastEntity>>

  @Query("SELECT * FROM safety_broadcasts ORDER BY id DESC")
  suspend fun getAllBroadcastsSync(): List<SafetyBroadcastEntity>

  @Query("SELECT COUNT(*) FROM safety_broadcasts")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBroadcast(broadcast: SafetyBroadcastEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(broadcasts: List<SafetyBroadcastEntity>)
}
