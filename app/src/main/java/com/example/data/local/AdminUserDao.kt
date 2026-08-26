package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminUserDao {
  @Query("SELECT * FROM admin_users ORDER BY registeredAt DESC")
  fun getAllUsers(): Flow<List<AdminUserEntity>>

  @Query("SELECT * FROM admin_users ORDER BY registeredAt DESC")
  suspend fun getAllUsersSync(): List<AdminUserEntity>

  @Query("SELECT * FROM admin_users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): AdminUserEntity?

  @Query("SELECT COUNT(*) FROM admin_users")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<AdminUserEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(user: AdminUserEntity)

  @Update
  suspend fun updateUser(user: AdminUserEntity)

  @Query("UPDATE admin_users SET verificationStatus = :status, adminNotes = :notes WHERE id = :userId")
  suspend fun updateVerification(userId: String, status: String, notes: String)

  @Query("UPDATE admin_users SET adminNotes = :notes WHERE id = :userId")
  suspend fun updateAdminNotes(userId: String, notes: String)
}
