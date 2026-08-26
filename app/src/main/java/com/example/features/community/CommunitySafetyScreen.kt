package com.example.features.community

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppSessionManager
import com.example.domain.model.AreaSafetyReport
import com.example.domain.model.CentralCommunityInfo
import com.example.domain.model.CentralCommunityPost
import com.example.domain.model.CommunityMember
import com.example.domain.model.CommunityReply
import com.example.domain.model.CommunitySosAlert
import com.example.domain.model.CommunityTopicCategory
import com.example.ui.theme.PinkAccentLilac
import com.example.ui.theme.PinkBorderSubtle
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkHeaderGradientStart
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySafetyScreen(
  viewModel: CommunityViewModel,
  onNavigateBack: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val communityInfo by viewModel.communityInfo.collectAsState()
  val allPosts by viewModel.communityPosts.collectAsState()
  val activeSosAlerts by viewModel.activeSosAlerts.collectAsState()
  val members by viewModel.communityMembers.collectAsState()
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsState()
  val context = LocalContext.current

  // Filter posts based on category and search query
  val filteredPosts = remember(allPosts, uiState.selectedCategory, uiState.searchQuery) {
    allPosts.filter { post ->
      val matchesCategory = (uiState.selectedCategory == CommunityTopicCategory.ALL) || (post.category == uiState.selectedCategory)
      val matchesQuery = uiState.searchQuery.isBlank() ||
          post.title.contains(uiState.searchQuery, ignoreCase = true) ||
          post.content.contains(uiState.searchQuery, ignoreCase = true) ||
          post.authorName.contains(uiState.searchQuery, ignoreCase = true) ||
          post.locationTag.contains(uiState.searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }

  val activeSosCount = activeSosAlerts.count { it.status != "RESOLVED" }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (isBangla) "রমিসা সেন্ট্রাল নারী ফোরাম ও সেফটি নেটওয়ার্ক" else "RAMISA Central Women's Community",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (uiState.isOfflineMode) Color(0xFFF59E0B) else Color(0xFF10B981))
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (uiState.isOfflineMode)
                  (if (isBangla) "📡 অফলাইন মোড (লোকাল ক্যাশে ও SIM SMS সক্রিয়)" else "📡 Offline Mode (Local Cache & SIM SMS)")
                else
                  (if (isBangla) "🟢 ৩,৪৫০ বোন সংযুক্ত • ১৪২ সক্রিয়" else "🟢 3,450 Sisters Connected • 142 Online"),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("btn_back_community")
          ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          // Offline / Online toggle button
          IconButton(
            onClick = { viewModel.toggleOfflineMode() },
            modifier = Modifier.testTag("btn_toggle_offline")
          ) {
            Icon(
              imageVector = if (uiState.isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
              contentDescription = "Toggle Offline Mode",
              tint = if (uiState.isOfflineMode) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.showCreatePostDialog(true) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_new_discussion")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isBangla) "নতুন আলোচনা" else "New Post",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {

      // Notification / Test Broadcast Banner
      AnimatedVisibility(visible = uiState.testSosSuccessMessage != null) {
        uiState.testSosSuccessMessage?.let { msg ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = SafeGreenPrimary.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, SafeGreenPrimary)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreenDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = msg, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SafeGreenDark))
              }
              IconButton(onClick = { viewModel.clearTestSosMessage() }) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Dismiss", tint = SafeGreenDark)
              }
            }
          }
        }
      }

      // Offline Mode Banner Notice
      if (uiState.isOfflineMode) {
        Surface(
          color = Color(0xFFFEF3C7),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isBangla) "অফলাইন মোড সক্রিয়: আগের সকল আলোচনা ও গাইডলাইন পড়া যাবে। পোস্ট করলে তা অফলাইনে সেভ থাকবে এবং ইন্টারনেট এলে স্বয়ংক্রিয়ভাবে সিঙ্ক হবে।"
              else "Offline Mode Active: You can browse cached discussions & safety guides. Posts will queue and auto-sync when online.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Color(0xFF92400E)),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Active SOS Warning Header if any active crisis exists
      if (activeSosCount > 0) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          colors = CardDefaults.cardColors(containerColor = SafetyRedPrimary.copy(alpha = 0.12f)),
          border = BorderStroke(1.5.dp, SafetyRedPrimary),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SafetyRedPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isBangla) "🚨 সক্রিয় জরুরি ব্রডকাস্ট ($activeSosCount টি)" else "🚨 ACTIVE CRISIS SOS ($activeSosCount)",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = SafetyRedDark)
                )
                Text(
                  text = if (isBangla) "কমিউনিটির বোনের তাৎক্ষণিক সাহায্য প্রয়োজন!" else "A sister in the central circle needs rescue assistance!",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = SafetyRedDark)
                )
              }
            }
            Button(
              onClick = {
                viewModel.setSelectedTab(0)
                viewModel.setSelectedCategory(CommunityTopicCategory.EMERGENCY_SOS)
              },
              colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
              shape = RoundedCornerShape(10.dp),
              contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
              Text(text = if (isBangla) "অ্যালার্ট দেখুন" else "View SOS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
          }
        }
      }

      // Navigation Tabs
      val tabTitles = if (isBangla) {
        listOf("💬 ফোরাম ও আলোচনা", "🚨 জরুরি SOS (${activeSosCount})", "👥 বোন ও পরামর্শক", "🗺️ নিরাপদ জোন")
      } else {
        listOf("💬 Discussions", "🚨 Live SOS (${activeSosCount})", "👥 Advisors & Sisters", "🗺️ Safe Zones")
      }

      ScrollableTabRow(
        selectedTabIndex = uiState.selectedTab,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = uiState.selectedTab == index,
            onClick = { viewModel.setSelectedTab(index) },
            text = {
              Text(
                text = title,
                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }

      // Main Tab Contents
      when (uiState.selectedTab) {
        0 -> {
          // Tab 0: Discussions & Messenger Channels
          DiscussionsMessengerView(
            posts = filteredPosts,
            selectedCategory = uiState.selectedCategory,
            searchQuery = uiState.searchQuery,
            isBangla = isBangla,
            onSelectCategory = { viewModel.setSelectedCategory(it) },
            onUpdateSearch = { viewModel.updateSearchQuery(it) },
            onSupportClick = { viewModel.toggleSupport(it) },
            onHelpfulClick = { viewModel.toggleHelpful(it) },
            onReplyClick = { viewModel.openReplyDialog(it) },
            onRespondSos = { viewModel.respondToSosAlert(it) },
            onResolveSos = { viewModel.resolveSosAlert(it) },
            onTriggerSosBroadcast = { viewModel.broadcastManualSosToCommunity() }
          )
        }
        1 -> {
          // Tab 1: Emergency SOS Live Alerts Feed
          EmergencySosFeedView(
            alerts = activeSosAlerts,
            isBangla = isBangla,
            onRespond = { viewModel.respondToSosAlert(it) },
            onResolve = { viewModel.resolveSosAlert(it) },
            onTriggerTest = { viewModel.broadcastManualSosToCommunity() }
          )
        }
        2 -> {
          // Tab 2: Verified Sisters, Proctors & Legal/Health Advisors
          AdvisorsAndMembersView(
            members = members,
            communityInfo = communityInfo,
            isBangla = isBangla,
            pinInput = uiState.pinInput,
            pinError = uiState.pinError,
            pinSuccess = uiState.pinSuccessMessage,
            onPinChange = { viewModel.updatePinInput(it) },
            onVerifyPin = { viewModel.verifyPin() }
          )
        }
        3 -> {
          // Tab 3: Safe Zones & Area Ratings
          SafeZonesView(
            reports = uiState.areaReports,
            selectedFilter = uiState.selectedSafeZoneFilter,
            isBangla = isBangla,
            onFilterChange = { viewModel.setSafeZoneFilter(it) },
            onOpenReportDialog = { viewModel.showReportingDialog(true) }
          )
        }
      }
    }

    // Modal Sheet for Reply / Messenger thread
    uiState.selectedPostForReply?.let { post ->
      ReplyBottomSheet(
        post = post,
        inputText = uiState.replyInputText,
        isAnonymous = uiState.isReplyAnonymous,
        isBangla = isBangla,
        onInputChange = { viewModel.updateReplyInput(it) },
        onToggleAnonymous = { viewModel.toggleReplyAnonymous(it) },
        onSubmit = { viewModel.submitReply() },
        onDismiss = { viewModel.closeReplyDialog() }
      )
    }

    // Dialog for creating a new post
    if (uiState.isCreatePostDialogVisible) {
      CreatePostDialog(
        selectedCategory = uiState.newPostCategory,
        title = uiState.newPostTitle,
        content = uiState.newPostContent,
        location = uiState.newPostLocation,
        isAnonymous = uiState.isNewPostAnonymous,
        isOffline = uiState.isOfflineMode,
        isBangla = isBangla,
        onCategoryChange = { viewModel.updateNewPostCategory(it) },
        onTitleChange = { viewModel.updateNewPostTitle(it) },
        onContentChange = { viewModel.updateNewPostContent(it) },
        onLocationChange = { viewModel.updateNewPostLocation(it) },
        onAnonymousToggle = { viewModel.toggleNewPostAnonymous(it) },
        onSubmit = { viewModel.submitNewPost() },
        onDismiss = { viewModel.showCreatePostDialog(false) }
      )
    }

    // Safe Zone report dialog
    if (uiState.isReportingDialogVisible) {
      CrowdsourceReportDialog(
        isBangla = isBangla,
        onDismiss = { viewModel.showReportingDialog(false) },
        onSubmit = { name, nameBn, score, light, isCaution, note ->
          viewModel.submitCommunityReport(name, nameBn, score, light, isCaution, note)
        }
      )
    }
  }
}

// -------------------------------------------------------------
// TAB 0: DISCUSSIONS & MESSENGER TOPICS VIEW
// -------------------------------------------------------------
@Composable
fun DiscussionsMessengerView(
  posts: List<CentralCommunityPost>,
  selectedCategory: CommunityTopicCategory,
  searchQuery: String,
  isBangla: Boolean,
  onSelectCategory: (CommunityTopicCategory) -> Unit,
  onUpdateSearch: (String) -> Unit,
  onSupportClick: (String) -> Unit,
  onHelpfulClick: (String) -> Unit,
  onReplyClick: (CentralCommunityPost) -> Unit,
  onRespondSos: (String) -> Unit,
  onResolveSos: (String) -> Unit,
  onTriggerSosBroadcast: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))

      // Central Community Banner Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PinkContainer.copy(alpha = 0.65f)),
        border = BorderStroke(1.dp, PinkBorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(PinkPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (isBangla) "রমিসা সেন্ট্রাল নারী ফোরাম ও নেটওয়ার্ক" else "RAMISA Central Sisterhood Network",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PinkPrimaryDark)
              )
              Text(
                text = if (isBangla) "একটি কেন্দ্রীয় প্ল্যাটফর্ম • সংকট, স্বাস্থ্য, আইনি পরামর্শ ও সহযাত্রী"
                else "One Central Community • Crisis SOS, Health, Legal & Safe-Walks",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Quick SOS broadcast shortcut inside community
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onTriggerSosBroadcast,
              modifier = Modifier
                .weight(1f)
                .testTag("btn_community_manual_sos"),
              colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isBangla) "🚨 সেন্ট্রাল SOS অ্যালার্ট পাঠান" else "🚨 Broadcast Central SOS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onUpdateSearch,
        placeholder = {
          Text(
            text = if (isBangla) "টপিক, এলাকা, ডাক্তার বা আইনি পরামর্শ খুঁজুন..." else "Search issues, area, legal/health topics...",
            style = MaterialTheme.typography.bodySmall
          )
        },
        leadingIcon = { Icon(imageVector = Icons.Default.Chat, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .testTag("tf_search_community"),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
      )
    }

    // Horizontal Topic Category Chips (Messenger Channels)
    item {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(CommunityTopicCategory.values()) { category ->
          val isSelected = selectedCategory == category
          val chipTitle = if (isBangla) category.titleBn else category.titleEn

          FilterChip(
            selected = isSelected,
            onClick = { onSelectCategory(category) },
            label = {
              Text(
                text = chipTitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = when (category) {
                CommunityTopicCategory.EMERGENCY_SOS -> SafetyRedPrimary
                CommunityTopicCategory.HARASSMENT_ALERT -> Color(0xFFD97706)
                CommunityTopicCategory.LEGAL_CYBER_HELP -> Color(0xFF2563EB)
                CommunityTopicCategory.HEALTH_WELLNESS -> SafeGreenPrimary
                CommunityTopicCategory.SAFE_WALK_BUDDY -> Color(0xFF0D9488)
                else -> MaterialTheme.colorScheme.primary
              },
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
          )
        }
      }
    }

    // Posts Feed
    if (posts.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (isBangla) "এই ক্যাটাগরিতে এখনো কোনো আলোচনা নেই" else "No discussions found in this channel",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isBangla) "নিচের 'নতুন আলোচনা' বাটনে ট্যাপ করে প্রথম পোস্টটি করুন!" else "Tap 'New Post' below to share your experience or ask a question.",
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(posts, key = { it.id }) { post ->
        CommunityPostCard(
          post = post,
          isBangla = isBangla,
          onSupportClick = { onSupportClick(post.id) },
          onHelpfulClick = { onHelpfulClick(post.id) },
          onReplyClick = { onReplyClick(post) },
          onRespondSos = { post.sosDetails?.id?.let { onRespondSos(it) } },
          onResolveSos = { post.sosDetails?.id?.let { onResolveSos(it) } }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(72.dp))
    }
  }
}

// -------------------------------------------------------------
// POST CARD COMPONENT (Messenger / Forum Style)
// -------------------------------------------------------------
@Composable
fun CommunityPostCard(
  post: CentralCommunityPost,
  isBangla: Boolean,
  onSupportClick: () -> Unit,
  onHelpfulClick: () -> Unit,
  onReplyClick: () -> Unit,
  onRespondSos: () -> Unit,
  onResolveSos: () -> Unit
) {
  val context = LocalContext.current
  val isSos = post.isSosAlert || post.category == CommunityTopicCategory.EMERGENCY_SOS

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .testTag("card_post_${post.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSos) SafetyRedPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = if (isSos) 1.8.dp else 1.dp,
      color = if (isSos) SafetyRedPrimary else PinkBorderSubtle
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {

      // Header: Category Badge + Time + Location
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Badge
        val categoryColor = when (post.category) {
          CommunityTopicCategory.EMERGENCY_SOS -> SafetyRedPrimary
          CommunityTopicCategory.HARASSMENT_ALERT -> Color(0xFFD97706)
          CommunityTopicCategory.LEGAL_CYBER_HELP -> Color(0xFF2563EB)
          CommunityTopicCategory.HEALTH_WELLNESS -> SafeGreenPrimary
          CommunityTopicCategory.SAFE_WALK_BUDDY -> Color(0xFF0D9488)
          else -> MaterialTheme.colorScheme.primary
        }

        Surface(
          color = categoryColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (isBangla) post.category.titleBn else post.category.titleEn,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = categoryColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (post.isOfflineQueued) {
            Surface(
              color = Color(0xFFFEF3C7),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (isBangla) "📥 অফলাইন সংরক্ষিত" else "📥 Offline Queued",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFFB45309)),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          Text(
            text = formatTimeAgo(post.timestamp, isBangla),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Author Row: Avatar + Name + Role + Location Tag
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
              if (post.isAnonymous) Color(0xFF6B7280)
              else if (isSos) SafetyRedPrimary
              else MaterialTheme.colorScheme.primary
            ),
          contentAlignment = Alignment.Center
        ) {
          if (post.isAnonymous) {
            Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          } else {
            Text(
              text = post.authorName.take(1).uppercase(),
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = post.authorName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            if (post.isAnonymous) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "🔒",
                fontSize = 12.sp
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${post.authorRole} • ${post.locationTag}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title & Content
      Text(
        text = post.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        color = if (isSos) SafetyRedDark else MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = post.content,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
        color = MaterialTheme.colorScheme.onSurface
      )

      // If Emergency SOS: Render Live Location & Direct Actions
      if (post.sosDetails != null) {
        val sos = post.sosDetails
        Spacer(modifier = Modifier.height(10.dp))
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SafetyRedPrimary.copy(alpha = 0.15f)),
          border = BorderStroke(1.dp, SafetyRedPrimary.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "📍 ${sos.locationName}",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SafetyRedDark)
            )
            Text(
              text = "🩸 Blood Group: ${sos.victimBloodGroup} • Responding: ${sos.respondingCount} Sisters",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = SafetyRedDark)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sos.victimPhone}"))
                  context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "কল দিন" else "Call", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              OutlinedButton(
                onClick = {
                  val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sos.mapUrl))
                  context.startActivity(mapIntent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "ম্যাপ দেখুন" else "Maps", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              Button(
                onClick = onRespondSos,
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreenPrimary),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(text = if (isBangla) "🙋‍♀️ আমি আসছি" else "🙋‍♀️ Responding", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Action Bar: Support Reaction, Helpful Reaction, Replies count & Share
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Support Heart reaction
        Row(
          modifier = Modifier
            .clickable(onClick = onSupportClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (post.userSupported) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Support",
            tint = if (post.userSupported) SafetyRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isBangla) "পাশে আছি (${post.supportCount})" else "Support (${post.supportCount})",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = if (post.userSupported) FontWeight.Bold else FontWeight.Normal,
              color = if (post.userSupported) SafetyRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          )
        }

        // Helpful Lightbulb reaction
        Row(
          modifier = Modifier
            .clickable(onClick = onHelpfulClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (post.userHelpful) Icons.Default.Lightbulb else Icons.Outlined.Lightbulb,
            contentDescription = "Helpful",
            tint = if (post.userHelpful) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isBangla) "সহায়ক (${post.helpfulCount})" else "Helpful (${post.helpfulCount})",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = if (post.userHelpful) FontWeight.Bold else FontWeight.Normal,
              color = if (post.userHelpful) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          )
        }

        // Reply / Comment Button
        Row(
          modifier = Modifier
            .clickable(onClick = onReplyClick)
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .testTag("btn_reply_${post.id}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Comment,
            contentDescription = "Reply",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isBangla) "মন্তব্য (${post.replies.size})" else "Replies (${post.replies.size})",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 12.sp
            )
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 1: EMERGENCY SOS LIVE FEED VIEW
// -------------------------------------------------------------
@Composable
fun EmergencySosFeedView(
  alerts: List<CommunitySosAlert>,
  isBangla: Boolean,
  onRespond: (String) -> Unit,
  onResolve: (String) -> Unit,
  onTriggerTest: () -> Unit
) {
  val context = LocalContext.current
  val activeList = alerts.filter { it.status != "RESOLVED" }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyRedPrimary.copy(alpha = 0.1f)),
        border = BorderStroke(1.5.dp, SafetyRedPrimary),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SafetyRedPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (isBangla) "সেন্ট্রাল ইমার্জেন্সি SOS নেটওয়ার্ক" else "Central Emergency SOS Network",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = SafetyRedDark)
              )
              Text(
                text = if (isBangla) "৩ বার ঝাঁকালে বা লকস্ক্রিনে চাপলে এখানে স্বয়ংক্রিয়ভাবে লাইভ ব্রডকাস্ট হয়"
                else "3x Phone Shake or Lock Screen SOS auto-broadcasts here with live location",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = SafetyRedDark
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = onTriggerTest,
            colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (isBangla) "🚨 জরুরি ব্রডকাস্ট টেস্ট করুন" else "🚨 Broadcast Emergency SOS Now",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    if (activeList.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SafeGreenPrimary.copy(alpha = 0.1f)),
          border = BorderStroke(1.dp, SafeGreenPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreenDark, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (isBangla) "সব বোন বর্তমানে সুরক্ষিত আছেন" else "All sisters are currently safe",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SafeGreenDark)
            )
            Text(
              text = if (isBangla) "বর্তমানে কোনো সক্রিয় বিপদ বা জরুরি উদ্ধার অ্যালার্ট নেই।" else "No active crisis alerts in the central network right now.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(activeList, key = { it.id }) { alert ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SafetyRedPrimary.copy(alpha = 0.08f)),
          border = BorderStroke(2.dp, SafetyRedPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(color = SafetyRedPrimary, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "🚨 LIVE SOS #${alert.id}",
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Text(
                text = formatTimeAgo(alert.timestamp, isBangla),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = SafetyRedDark
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Victim: ${alert.victimName}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = SafetyRedDark)
            )
            Text(
              text = "Trigger: ${alert.triggerTypeLabel} • Blood Group: ${alert.victimBloodGroup}",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = SafetyRedDark
            )
            Text(
              text = "📍 Location: ${alert.locationName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )

            if (alert.customNote.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "\"${alert.customNote}\"",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${alert.victimPhone}"))
                  context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "কল দিন" else "Call", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              OutlinedButton(
                onClick = {
                  val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(alert.mapUrl))
                  context.startActivity(mapIntent)
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "ম্যাপ" else "Map", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              Button(
                onClick = { onRespond(alert.id) },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreenPrimary)
              ) {
                Text(text = if (isBangla) "🙋‍♀️ রেসপন্ড" else "🙋‍♀️ Respond", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: VERIFIED ADVISORS & ACTIVE SISTERS VIEW
// -------------------------------------------------------------
@Composable
fun AdvisorsAndMembersView(
  members: List<CommunityMember>,
  communityInfo: CentralCommunityInfo,
  isBangla: Boolean,
  pinInput: String,
  pinError: String?,
  pinSuccess: String?,
  onPinChange: (String) -> Unit,
  onVerifyPin: () -> Unit
) {
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // PIN Verification Card for Central Access
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PinkContainer.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, PinkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isBangla) "সেন্ট্রাল মেম্বার ভেরিফিকেশন পিন" else "Central Member Verification PIN",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isBangla) "রমিসা সেন্ট্রাল নারী ফোরামের নিরাপত্তা নিশ্চিত করতে গোপন পিন (Default: 1234) দিয়ে ভেরিফাই করুন।"
            else "Enter Central Security PIN (Default: 1234) to confirm verified sisterhood access.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = pinInput,
              onValueChange = onPinChange,
              placeholder = { Text("1234") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              singleLine = true
            )
            Button(
              onClick = onVerifyPin,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(text = if (isBangla) "ভেরিফাই" else "Verify", fontWeight = FontWeight.Bold)
            }
          }

          if (pinError != null) {
            Text(text = pinError, color = SafetyRedPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
          }
          if (pinSuccess != null) {
            Text(text = pinSuccess, color = SafeGreenPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
          }
        }
      }
    }

    item {
      Text(
        text = if (isBangla) "অন-ডিউটি রেসপন্ডার, ডাক্তার ও আইনি পরামর্শক" else "On-Duty Responders, Doctors & Legal Advisors",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
      )
    }

    items(members, key = { it.id }) { member ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, PinkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  if (member.role.contains("Legal")) Color(0xFF2563EB)
                  else if (member.role.contains("Health") || member.role.contains("Doctor")) SafeGreenPrimary
                  else if (member.role.contains("Admin") || member.role.contains("Proctor")) SafetyRedPrimary
                  else MaterialTheme.colorScheme.primary
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = member.avatarInitial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${member.role} • ${member.status}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
              context.startActivity(intent)
            }
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Call",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 3: SAFE ZONES VIEW
// -------------------------------------------------------------
@Composable
fun SafeZonesView(
  reports: List<AreaSafetyReport>,
  selectedFilter: String,
  isBangla: Boolean,
  onFilterChange: (String) -> Unit,
  onOpenReportDialog: () -> Unit
) {
  val filteredReports = remember(reports, selectedFilter) {
    when (selectedFilter) {
      "SAFE" -> reports.filter { it.safetyScore >= 80 }
      "CAUTION" -> reports.filter { it.isCautionZone || it.safetyScore < 70 }
      else -> reports
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PinkContainer.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, PinkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (isBangla) "ক্রাউডসোর্সড এরিয়া সেফটি ইনডেক্স" else "Crowdsourced Area Safety Index",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isBangla) "ঢাকার বিভিন্ন এলাকার স্ট্রিটলাইট, পুলিশ টহল ও ভিড়ের লাইভ তথ্য"
            else "Live ratings on street lighting, police beat-boxes & commuter feedback.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = onOpenReportDialog,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isBangla) "আপনার এলাকার রেটিং বা তথ্য যোগ করুন" else "Add Area Safety Feedback",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Filter Chips
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = selectedFilter == "ALL",
          onClick = { onFilterChange("ALL") },
          label = { Text(if (isBangla) "সকল এলাকা" else "All Areas") }
        )
        FilterChip(
          selected = selectedFilter == "SAFE",
          onClick = { onFilterChange("SAFE") },
          label = { Text(if (isBangla) "🛡️ নিরাপদ জোন (৮০%+)" else "🛡️ Safe Zones (80%+)") }
        )
        FilterChip(
          selected = selectedFilter == "CAUTION",
          onClick = { onFilterChange("CAUTION") },
          label = { Text(if (isBangla) "⚠️ সতর্কতা জোন" else "⚠️ Caution Zones") }
        )
      }
    }

    items(filteredReports, key = { it.id }) { report ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (report.isCautionZone) SafetyRedPrimary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (report.isCautionZone) SafetyRedPrimary.copy(alpha = 0.5f) else PinkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isBangla) report.areaNameBn else report.areaName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.weight(1f)
            )
            Surface(
              color = if (report.safetyScore >= 80) SafeGreenPrimary else SafetyRedPrimary,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "${report.safetyScore}% Safe",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "💡 Lighting: ${report.lightingRating}/5.0 • 👮 Police Patrol: ${report.policePatrolRating}/5.0 • 👥 Crowd: ${if (isBangla) report.crowdDensityBn else report.crowdDensity}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          val tip = if (isBangla) report.safetyTipsBn.firstOrNull() else report.safetyTips.firstOrNull()
          if (!tip.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "📌 $tip",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
              color = if (report.isCautionZone) SafetyRedDark else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// MESSENGER REPLY BOTTOM SHEET (Live Thread Discussion)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBottomSheet(
  post: CentralCommunityPost,
  inputText: String,
  isAnonymous: Boolean,
  isBangla: Boolean,
  onInputChange: (String) -> Unit,
  onToggleAnonymous: (Boolean) -> Unit,
  onSubmit: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 24.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isBangla) "💬 আলোচনা ও পরামর্শ থ্রেড" else "💬 Messenger Discussion Thread",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
          )
          Text(
            text = post.title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        TextButton(onClick = onDismiss) {
          Text(text = if (isBangla) "বন্ধ করুন" else "Close")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Divider()
      Spacer(modifier = Modifier.height(8.dp))

      // Replies list
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f, fill = false)
          .height(280.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (post.replies.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isBangla) "এখনো কোনো মন্তব্য নেই। আপনার মতামত বা পরামর্শ শেয়ার করুন!"
                else "No replies yet. Be the first sister to offer guidance or support!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          items(post.replies, key = { it.id }) { reply ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (reply.isAnonymous) Color(0xFFF3F4F6) else PinkContainer.copy(alpha = 0.5f)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = reply.authorName,
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "• ${reply.authorRole}",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = formatTimeAgo(reply.timestamp, isBangla),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = reply.content,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Anonymous reply toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isAnonymous) Icons.Default.VisibilityOff else Icons.Default.Person,
            contentDescription = null,
            tint = if (isAnonymous) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isBangla) "বেনামে মন্তব্য করুন (নাম গোপন থাকবে)" else "Reply Anonymously (Name Hidden)",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = if (isAnonymous) FontWeight.Bold else FontWeight.Normal)
          )
        }
        Switch(
          checked = isAnonymous,
          onCheckedChange = onToggleAnonymous,
          colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Messenger Chat Input Field
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = onInputChange,
          placeholder = {
            Text(
              text = if (isBangla) "আপনার পরামর্শ বা সান্ত্বনা লিখুন..." else "Write advice or message...",
              style = MaterialTheme.typography.bodySmall
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("tf_reply_input"),
          shape = RoundedCornerShape(24.dp),
          maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = onSubmit,
          enabled = inputText.isNotBlank(),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            .testTag("btn_send_reply")
        ) {
          Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CREATE NEW DISCUSSION DIALOG (Supports Anonymous & Categories)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
  selectedCategory: CommunityTopicCategory,
  title: String,
  content: String,
  location: String,
  isAnonymous: Boolean,
  isOffline: Boolean,
  isBangla: Boolean,
  onCategoryChange: (CommunityTopicCategory) -> Unit,
  onTitleChange: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onLocationChange: (String) -> Unit,
  onAnonymousToggle: (Boolean) -> Unit,
  onSubmit: () -> Unit,
  onDismiss: () -> Unit
) {
  var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = if (isBangla) "নতুন আলোচনা বা সমস্যা পোস্ট করুন" else "Start Discussion in Central Forum",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
        )
        if (isOffline) {
          Text(
            text = if (isBangla) "📡 অফলাইন মোড: পোস্টটি লোকালি সেভ থাকবে ও পরে সিঙ্ক হবে"
            else "📡 Offline Mode: Will be saved locally & auto-synced",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD97706), fontSize = 11.sp)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Category Selector
        Text(text = if (isBangla) "ক্যাটাগরি নির্বাচন করুন:" else "Select Topic Channel:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))

        ExposedDropdownMenuBox(
          expanded = isCategoryDropdownExpanded,
          onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
        ) {
          OutlinedTextField(
            value = if (isBangla) selectedCategory.titleBn else selectedCategory.titleEn,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = isCategoryDropdownExpanded,
            onDismissRequest = { isCategoryDropdownExpanded = false }
          ) {
            CommunityTopicCategory.values().filter { it != CommunityTopicCategory.ALL }.forEach { cat ->
              DropdownMenuItem(
                text = { Text(if (isBangla) cat.titleBn else cat.titleEn) },
                onClick = {
                  onCategoryChange(cat)
                  isCategoryDropdownExpanded = false
                }
              )
            }
          }
        }

        // Title
        OutlinedTextField(
          value = title,
          onValueChange = onTitleChange,
          label = { Text(if (isBangla) "শিরোনাম / সারসংক্ষেপ" else "Topic Title") },
          placeholder = { Text("e.g. Bus harassment / Legal guidance") },
          modifier = Modifier.fillMaxWidth().testTag("tf_new_post_title"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        // Location tag
        OutlinedTextField(
          value = location,
          onValueChange = onLocationChange,
          label = { Text(if (isBangla) "এলাকা / ক্যাম্পাস" else "Area / Campus Tag") },
          placeholder = { Text("e.g. TSC / Dhanmondi / Mirpur") },
          modifier = Modifier.fillMaxWidth().testTag("tf_new_post_location"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        // Content
        OutlinedTextField(
          value = content,
          onValueChange = onContentChange,
          label = { Text(if (isBangla) "বিস্তারিত বর্ণনা বা প্রশ্ন" else "Detailed Message or Question") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .testTag("tf_new_post_content"),
          shape = RoundedCornerShape(10.dp),
          maxLines = 5
        )

        // Anonymous Switch
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isBangla) "🔒 নাম গোপন রাখুন (Anonymous)" else "🔒 Post Anonymously",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = if (isBangla) "সংবেদনশীল বা ব্যক্তিগত ঘটনায় আপনার পরিচয় প্রকাশ পাবে না।"
                else "Your identity will be completely hidden from other users.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isAnonymous,
              onCheckedChange = onAnonymousToggle
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onSubmit,
        enabled = title.isNotBlank() && content.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_submit_post")
      ) {
        Text(text = if (isBangla) "পোস্ট করুন" else "Publish", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = if (isBangla) "বাতিল" else "Cancel")
      }
    }
  )
}

// -------------------------------------------------------------
// SAFE ZONE FEEDBACK DIALOG
// -------------------------------------------------------------
@Composable
fun CrowdsourceReportDialog(
  isBangla: Boolean,
  onDismiss: () -> Unit,
  onSubmit: (String, String, Int, Float, Boolean, String) -> Unit
) {
  var areaName by remember { mutableStateOf("") }
  var safetyScore by remember { mutableStateOf(80) }
  var lighting by remember { mutableStateOf(4.0f) }
  var isCaution by remember { mutableStateOf(false) }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isBangla) "এলাকার নিরাপত্তা রেটিং দিন" else "Crowdsource Area Safety Report",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = areaName,
          onValueChange = { areaName = it },
          label = { Text(if (isBangla) "এলাকার নাম" else "Area Name") },
          placeholder = { Text("e.g. Dhanmondi 27 / TSC") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = if (isBangla) "⚠️ ঝুঁকিপূর্ণ এলাকা?" else "⚠️ Caution Area?", style = MaterialTheme.typography.bodySmall)
          Switch(checked = isCaution, onCheckedChange = { isCaution = it })
        }

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text(if (isBangla) "নিরাপত্তা টিপস বা মন্তব্য" else "Safety Tip / Note") },
          placeholder = { Text("e.g. Street lights are fixed, safe till 10 PM") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(areaName, areaName, if (isCaution) 55 else 88, lighting, isCaution, note)
        },
        enabled = areaName.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(if (isBangla) "জমা দিন" else "Submit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isBangla) "বাতিল" else "Cancel")
      }
    }
  )
}

// -------------------------------------------------------------
// TIME AGO FORMATTER
// -------------------------------------------------------------
fun formatTimeAgo(timestamp: Long, isBangla: Boolean): String {
  val diff = System.currentTimeMillis() - timestamp
  val seconds = diff / 1000
  val minutes = seconds / 60
  val hours = minutes / 60
  val days = hours / 24

  return when {
    minutes < 1 -> if (isBangla) "এইমাত্র" else "Just now"
    minutes < 60 -> if (isBangla) "${minutes} মিনিট আগে" else "${minutes}m ago"
    hours < 24 -> if (isBangla) "${hours} ঘণ্টা আগে" else "${hours}h ago"
    else -> if (isBangla) "${days} দিন আগে" else "${days}d ago"
  }
}
