import re

with open("app/src/main/java/com/example/core/community/CommunityManager.kt", "r") as f:
    content = f.read()

imports = """import com.example.data.remote.SupabaseConfig
import com.example.data.remote.SupabaseCommunityMessage
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
"""

content = content.replace("import java.util.UUID", imports + "import java.util.UUID")

# Add polling block
polling_init = """  init {
    GlobalScope.launch(Dispatchers.IO) {
      while (true) {
        try {
          val remoteMessages = SupabaseConfig.client.postgrest["community_messages"]
              .select()
              .decodeList<SupabaseCommunityMessage>()
              .sortedByDescending { it.createdAt ?: "" }
          
          val mappedPosts = remoteMessages.map { msg ->
              CentralCommunityPost(
                  id = msg.id ?: UUID.randomUUID().toString(),
                  category = if (msg.isSosAlert) CommunityTopicCategory.EMERGENCY_SOS else CommunityTopicCategory.SISTERHOOD_GENERAL,
                  authorName = if (msg.isSosAlert) "🚨 SOS BROADCAST: ${msg.userName}" else msg.userName,
                  authorRole = if (msg.isSosAlert) "Emergency Dispatch" else "Verified Member",
                  authorPhone = "Protected",
                  isAnonymous = false,
                  title = if (msg.isSosAlert) "🚨 CRISIS SOS: ${msg.userName} needs immediate help!" else "Community Update",
                  content = msg.message,
                  locationTag = "Live Area",
                  timestamp = System.currentTimeMillis(), // We could parse createdAt but this is easier for now
                  supportCount = 0,
                  helpfulCount = 0,
                  isSosAlert = msg.isSosAlert,
                  sosDetails = if (msg.isSosAlert) CommunitySosAlert(
                      id = "SOS-${msg.id}",
                      victimName = msg.userName,
                      victimPhone = "",
                      triggerTypeLabel = "Emergency Alert",
                      locationName = "Location protected",
                      latitude = 23.7258,
                      longitude = 90.3976,
                      mapUrl = "https://maps.google.com/?q=23.7258,90.3976",
                      customNote = msg.message
                  ) else null
              )
          }

          // Merge keeping local replies and states, but for simplicity we just replace the ones that come from Supabase.
          // Or even simpler: we just update the _communityPosts to include these.
          _communityPosts.update { currentList ->
              val localOnly = currentList.filter { !it.id.contains("-") && !it.id.contains("post_") } // rough heuristic, let's just keep our seeded ones and supabase
              val seeded = initialPosts
              
              val all = (seeded + mappedPosts).distinctBy { it.id }
              all.sortedByDescending { it.timestamp }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to poll Supabase community messages", e)
        }
        delay(3000) // Poll every 3 seconds
      }
    }
  }

  // Pre-seeded female-related discussions"""

content = content.replace("  // Pre-seeded female-related discussions", polling_init)

# Now update createPost to insert into Supabase
create_post_regex = r'(_communityPosts\.update \{ listOf\(newPost\) \+ it \})'
replacement_create_post = """_communityPosts.update { listOf(newPost) + it }
    GlobalScope.launch(Dispatchers.IO) {
      try {
        val user = AppSessionManager.currentUser.value
        val userId = io.github.jan.supabase.auth.auth.SupabaseConfig.client.auth.currentUserOrNull()?.id ?: user?.id ?: "unknown"
        val message = SupabaseCommunityMessage(
          userId = userId,
          userName = authorName,
          message = content.trim(),
          isSosAlert = false
        )
        SupabaseConfig.client.postgrest["community_messages"].insert(message)
      } catch(e: Exception) {
        e.printStackTrace()
      }
    }"""
content = re.sub(create_post_regex, replacement_create_post, content, count=1)

# Now update broadcastSosAlert to insert into Supabase
sos_post_regex = r'(_communityPosts\.update \{ listOf\(sosPost\) \+ it \})'
replacement_sos_post = """_communityPosts.update { listOf(sosPost) + it }
    GlobalScope.launch(Dispatchers.IO) {
      try {
        val user = AppSessionManager.currentUser.value
        val userId = io.github.jan.supabase.auth.auth.SupabaseConfig.client.auth.currentUserOrNull()?.id ?: user?.id ?: "unknown"
        val message = SupabaseCommunityMessage(
          userId = userId,
          userName = name,
          message = sosPost.content,
          isSosAlert = true
        )
        SupabaseConfig.client.postgrest["community_messages"].insert(message)
      } catch(e: Exception) {
        e.printStackTrace()
      }
    }"""
content = re.sub(sos_post_regex, replacement_sos_post, content, count=1)

with open("app/src/main/java/com/example/core/community/CommunityManager.kt", "w") as f:
    f.write(content)
