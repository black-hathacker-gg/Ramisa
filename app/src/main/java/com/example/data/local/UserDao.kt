package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE phone = :phone LIMIT 1")
    suspend fun getProfileByPhone(phone: String): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET isLoggedIn = 0")
    suspend fun clearProfile()
}
