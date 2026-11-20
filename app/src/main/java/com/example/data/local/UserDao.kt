package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 ORDER BY updatedAt DESC LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE substr(id, 1, 6) = 'guest_' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getGuestProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun savePendingProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET isLoggedIn = 0")
    suspend fun clearProfile()

    @Transaction
    suspend fun activateProfile(profile: UserProfileEntity) {
        clearProfile()
        saveProfile(profile.copy(isLoggedIn = true))
    }

    @Transaction
    suspend fun updateActiveProfile(profile: UserProfileEntity): Boolean {
        if (getProfileById(profile.id)?.isLoggedIn != true) return false
        saveProfile(profile.copy(isLoggedIn = true))
        return true
    }
}
