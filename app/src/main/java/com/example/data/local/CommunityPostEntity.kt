package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
  @PrimaryKey val id: String,
  val categoryId: String,
  val authorName: String,
  val authorRole: String,
  val authorPhone: String,
  val isAnonymous: Boolean,
  val title: String,
  val content: String,
  val locationTag: String,
  val timestamp: Long,
  val supportCount: Int,
  val helpfulCount: Int,
  val userSupported: Boolean,
  val userHelpful: Boolean,
  val isSosAlert: Boolean,
  val repliesJson: String = "[]"
)

@Dao
interface CommunityDao {
  @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
  fun getAllPosts(): Flow<List<CommunityPostEntity>>

  @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
  suspend fun getAllPostsSync(): List<CommunityPostEntity>

  @Query("SELECT COUNT(*) FROM community_posts")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPost(post: CommunityPostEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPosts(posts: List<CommunityPostEntity>)

  @Query("UPDATE community_posts SET supportCount = :supportCount, userSupported = :userSupported WHERE id = :postId")
  suspend fun updateSupport(postId: String, supportCount: Int, userSupported: Boolean)

  @Query("UPDATE community_posts SET repliesJson = :repliesJson WHERE id = :postId")
  suspend fun updateReplies(postId: String, repliesJson: String)
}
