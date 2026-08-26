package com.example.features.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.AdminUserRecord
import com.example.domain.model.UserVerificationStatus
import com.example.ui.theme.*

private val AlertRed = Color(0xFFD32F2F)
private val WarningYellow = Color(0xFFF57F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  viewModel: AdminViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val filteredUsers by viewModel.filteredUsers.collectAsState()
  val allUsers by viewModel.users.collectAsState()
  val broadcasts by viewModel.broadcasts.collectAsState()
  val context = LocalContext.current

  // Status message snackbar/toast
  LaunchedEffect(uiState.statusMessage) {
    uiState.statusMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      viewModel.clearStatusMessage()
    }
  }

  if (!uiState.isAuthenticated) {
    AdminAuthLockScreen(
      pinInput = uiState.adminPinInput,
      errorMessage = uiState.authError,
      onPinChange = viewModel::onPinChange,
      onAuthenticate = { viewModel.authenticateAdmin() },
      onNavigateBack = onNavigateBack
    )
  } else {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "RAMISA Safety Control Room",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = SafeGreenDark,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "MASTER ADMIN",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "ইউজার ভেরিফিকেশন, অভিভাবক কন্টাক্ট ও ডিসপ্যাচ প্যানেল",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.85f)
              )
            }
          },
          navigationIcon = {
            IconButton(onClick = onNavigateBack) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
          },
          actions = {
            IconButton(onClick = { viewModel.openExportDialog() }) {
              Icon(imageVector = Icons.Default.Download, contentDescription = "Export Data", tint = Color.White)
            }
            IconButton(onClick = { viewModel.openBroadcastDialog() }) {
              Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast Alert", tint = Color.White)
            }
            IconButton(onClick = { viewModel.logoutAdmin() }) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PinkPrimaryDark,
            titleContentColor = Color.White
          )
        )
      }
    ) { innerPadding ->
      LazyColumn(
        modifier = modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(MaterialTheme.colorScheme.background)
      ) {
        // 1. Dashboard Metric Stats
        item {
          AdminMetricsHeader(
            allUsers = allUsers,
            modifier = Modifier.padding(16.dp)
          )
        }

        // 2. Broadcast Quick Banner
        if (broadcasts.isNotEmpty()) {
          item {
            AdminBroadcastsCard(
              broadcast = broadcasts.first(),
              onNewBroadcastClick = { viewModel.openBroadcastDialog() },
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
          }
        }

        // 3. Search and Filters
        item {
          Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
              value = uiState.searchQuery,
              onValueChange = viewModel::setSearchQuery,
              placeholder = { Text("নাম, ফোন নম্বর, NID/Student ID বা এলাকা দিয়ে খুঁজুন...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PinkPrimary) },
              trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                  IconButton(onClick = { viewModel.setSearchQuery("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_search_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinkPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              val pendingCount = allUsers.count { it.verificationStatus == UserVerificationStatus.PENDING }
              val verifiedCount = allUsers.count { it.verificationStatus == UserVerificationStatus.VERIFIED }
              val suspendedCount = allUsers.count { it.verificationStatus == UserVerificationStatus.SUSPENDED }

              item {
                FilterChip(
                  selected = uiState.selectedFilter == AdminFilter.ALL,
                  onClick = { viewModel.setFilter(AdminFilter.ALL) },
                  label = { Text("সকল ইউজার (${allUsers.size})") }
                )
              }
              item {
                FilterChip(
                  selected = uiState.selectedFilter == AdminFilter.PENDING,
                  onClick = { viewModel.setFilter(AdminFilter.PENDING) },
                  label = { Text("⏳ পেন্ডিং রিভিউ ($pendingCount)") },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningYellow.copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF8D6E00)
                  )
                )
              }
              item {
                FilterChip(
                  selected = uiState.selectedFilter == AdminFilter.VERIFIED,
                  onClick = { viewModel.setFilter(AdminFilter.VERIFIED) },
                  label = { Text("🛡️ ভেরিফাইড ($verifiedCount)") }
                )
              }
              item {
                FilterChip(
                  selected = uiState.selectedFilter == AdminFilter.SUSPENDED,
                  onClick = { viewModel.setFilter(AdminFilter.SUSPENDED) },
                  label = { Text("🚫 সাসপেন্ডেড ($suspendedCount)") }
                )
              }
            }
          }
        }

        // 4. Users List Header
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "নিবন্ধিত ব্যবহারকারীদের তালিকা (${filteredUsers.size})",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { viewModel.openExportDialog() }) {
              Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("CSV এক্সপোর্ট", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        // 5. Users List
        if (filteredUsers.isEmpty()) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "কোন ব্যবহারকারী খুঁজে পাওয়া যায়নি",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "অনুগ্রহ করে অন্য নাম, ফোন নম্বর বা ফিল্টার সিলেক্ট করুন",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        } else {
          items(filteredUsers, key = { it.id }) { user ->
            AdminUserCard(
              user = user,
              onInspect = { viewModel.selectUser(user) },
              onVerifyClick = { viewModel.openVerificationDialog(user) },
              onCallUser = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone.replace(" ", "")}"))
                context.startActivity(intent)
              },
              onCallGuardian = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.emergencyGuardianPhone.replace(" ", "")}"))
                context.startActivity(intent)
              },
              onToggleSuspend = {
                viewModel.toggleSuspendUser(user.id, "Admin Manual Moderation")
              },
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }

  // --- Modals & Dialogs ---

  // 1. User Detail BottomSheet / Modal Dialog
  if (uiState.selectedUserForDetail != null && !uiState.showVerificationDialog) {
    val user = uiState.selectedUserForDetail!!
    UserDetailDialog(
      user = user,
      onDismiss = { viewModel.selectUser(null) },
      onApprove = { viewModel.approveUserVerification(user.id) },
      onReject = { viewModel.rejectUserVerification(user.id) },
      onToggleSuspend = { viewModel.toggleSuspendUser(user.id, "Admin detail action") },
      onSaveNote = { note -> viewModel.updateAdminNote(user.id, note) }
    )
  }

  // 2. Verification Action Dialog
  if (uiState.showVerificationDialog && uiState.selectedUserForDetail != null) {
    val user = uiState.selectedUserForDetail!!
    VerificationApprovalDialog(
      user = user,
      onDismiss = { viewModel.closeVerificationDialog() },
      onApprove = { note -> viewModel.approveUserVerification(user.id, note) },
      onReject = { reason -> viewModel.rejectUserVerification(user.id, reason) }
    )
  }

  // 3. Broadcast Alert Dialog
  if (uiState.showBroadcastDialog) {
    AdminBroadcastDialog(
      onDismiss = { viewModel.closeBroadcastDialog() },
      onSend = { title, msg, priority ->
        viewModel.sendBroadcastAlert(title, msg, priority)
      }
    )
  }

  // 4. Export CSV / Roster Dialog
  if (uiState.showExportDialog) {
    ExportRosterDialog(
      csvData = uiState.exportDataContent,
      onDismiss = { viewModel.closeExportDialog() },
      onCopy = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ramisa User Roster", uiState.exportDataContent)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "📋 পুরো ইউজার রোস্টার ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
fun AdminAuthLockScreen(
  pinInput: String,
  errorMessage: String?,
  onPinChange: (String) -> Unit,
  onAuthenticate: () -> Unit,
  onNavigateBack: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(PinkPrimaryDark, Color(0xFF2C101E), Color(0xFF16060F))
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = PinkPrimary.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, PinkPrimary),
        modifier = Modifier.size(90.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = "Admin Shield",
            tint = Color.White,
            modifier = Modifier.size(50.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "RAMISA Safety Control Room",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Text(
        text = "সুপার অ্যাডমিন ও কন্ট্রোল প্যানেল লগইন",
        style = MaterialTheme.typography.bodyMedium,
        color = PinkAccentLilac,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "ইউজার ডেটাবেজ, এনআইডি/আইডি ভেরিফিকেশন ও এমার্জেন্সি মনিটরিংয়ে প্রবেশের জন্য মাস্টার অ্যাডমিন পিন কোড প্রবেশ করান।",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          OutlinedTextField(
            value = pinInput,
            onValueChange = onPinChange,
            label = { Text("মাস্টার অ্যাডমিন PIN (ডিফল্ট: 2304)", color = Color.White.copy(alpha = 0.8f)) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = PinkPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAuthenticate() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_pin_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = PinkPrimary,
              unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
            )
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = errorMessage,
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = onAuthenticate,
            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("admin_login_button")
          ) {
            Icon(Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("কন্ট্রোল প্যানেলে প্রবেশ করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          TextButton(onClick = onNavigateBack) {
            Text("ফিরে যান", color = Color.White.copy(alpha = 0.75f))
          }
        }
      }
    }
  }
}

@Composable
fun AdminMetricsHeader(
  allUsers: List<AdminUserRecord>,
  modifier: Modifier = Modifier
) {
  val total = allUsers.size
  val pending = allUsers.count { it.verificationStatus == UserVerificationStatus.PENDING }
  val verified = allUsers.count { it.verificationStatus == UserVerificationStatus.VERIFIED }
  val activeSos = allUsers.sumOf { it.sosTriggersCount }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(SafeGreenDark, CircleShape)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "সিস্টেম স্ট্যাটাস: সেন্ট্রাল নোড সক্রিয়",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SafeGreenDark)
          )
        }
        Text(
          text = "সার্ভার: Dhaka-Cloud-01",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Metric 1: Total
        MetricBox(
          title = "মোট ইউজার",
          count = total.toString(),
          color = PinkPrimary,
          icon = Icons.Default.People,
          modifier = Modifier.weight(1f)
        )

        // Metric 2: Pending
        MetricBox(
          title = "পেন্ডিং ভেরিফাই",
          count = pending.toString(),
          color = WarningYellow,
          icon = Icons.Default.HourglassTop,
          modifier = Modifier.weight(1f)
        )

        // Metric 3: Verified
        MetricBox(
          title = "ভেরিফাইড আইডি",
          count = verified.toString(),
          color = SafeGreenDark,
          icon = Icons.Default.Verified,
          modifier = Modifier.weight(1f)
        )

        // Metric 4: SOS Logs
        MetricBox(
          title = "SOS ট্র্যাকার",
          count = activeSos.toString(),
          color = AlertRed,
          icon = Icons.Default.Sos,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun MetricBox(
  title: String,
  count: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = color.copy(alpha = 0.1f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = count,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = color
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun AdminBroadcastsCard(
  broadcast: com.example.domain.model.SafetyBroadcastMessage,
  onNewBroadcastClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = AlertRed.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = AlertRed,
        modifier = Modifier.size(36.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "সেন্ট্রাল সতর্কতা বার্তা",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AlertRed)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${broadcast.timestamp}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = broadcast.title,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = broadcast.message,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      IconButton(onClick = onNewBroadcastClick) {
        Icon(Icons.Default.AddAlert, contentDescription = "New Alert", tint = AlertRed)
      }
    }
  }
}

@Composable
fun AdminUserCard(
  user: AdminUserRecord,
  onInspect: () -> Unit,
  onVerifyClick: () -> Unit,
  onCallUser: () -> Unit,
  onCallGuardian: () -> Unit,
  onToggleSuspend: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPending = user.verificationStatus == UserVerificationStatus.PENDING
  val isSuspended = user.verificationStatus == UserVerificationStatus.SUSPENDED
  val isVerified = user.verificationStatus == UserVerificationStatus.VERIFIED

  val borderColor = when {
    isSuspended -> AlertRed.copy(alpha = 0.5f)
    isPending -> WarningYellow.copy(alpha = 0.7f)
    isVerified -> SafeGreenDark.copy(alpha = 0.4f)
    else -> MaterialTheme.colorScheme.outlineVariant
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onInspect() }
      .testTag("admin_user_card_${user.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSuspended) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Avatar, Name, Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Avatar
        Box {
          Surface(
            shape = CircleShape,
            color = if (isSuspended) AlertRed.copy(alpha = 0.15f) else PinkPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(46.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = user.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSuspended) AlertRed else PinkPrimary
              )
            }
          }
          if (user.isOnline) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .background(SafeGreenDark, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .align(Alignment.BottomEnd)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = user.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Text(
            text = "📞 ${user.phone} • 🩸 ব্লাড: ${user.bloodGroup}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when {
            isSuspended -> AlertRed.copy(alpha = 0.15f)
            isPending -> WarningYellow.copy(alpha = 0.2f)
            isVerified -> SafeGreenDark.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val icon = when {
              isSuspended -> Icons.Default.Block
              isPending -> Icons.Default.HourglassTop
              isVerified -> Icons.Default.Verified
              else -> Icons.Default.Cancel
            }
            val tint = when {
              isSuspended -> AlertRed
              isPending -> Color(0xFF8D6E00)
              isVerified -> SafeGreenDark
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = user.verificationStatus.labelBn,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              color = tint
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(10.dp))

      // ID Info & Institution
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "🪪 ${user.idType}: ${user.idNumber}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "🏫 ${user.institutionOrArea}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Guardian Info
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "🛡️ অভিভাবক: ${user.emergencyGuardianName} (${user.emergencyGuardianRelation})",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium)
            )
            Text(
              text = "নম্বর: ${user.emergencyGuardianPhone}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onCallGuardian, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Phone, contentDescription = "Call Guardian", tint = SafeGreenDark, modifier = Modifier.size(18.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Inspect Details
        OutlinedButton(
          onClick = onInspect,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ডিটেইলস", style = MaterialTheme.typography.labelSmall)
        }

        // Call User
        IconButton(
          onClick = onCallUser,
          modifier = Modifier.size(36.dp),
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = PinkPrimary.copy(alpha = 0.1f))
        ) {
          Icon(Icons.Default.Phone, contentDescription = "Call User", tint = PinkPrimary, modifier = Modifier.size(18.dp))
        }

        // Verification Trigger (if pending)
        if (isPending) {
          Button(
            onClick = onVerifyClick,
            colors = ButtonDefaults.buttonColors(containerColor = SafeGreenDark),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("যাচাই করুন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        } else {
          // Suspend / Unsuspend
          OutlinedButton(
            onClick = onToggleSuspend,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = if (isSuspended) SafeGreenDark else AlertRed
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = if (isSuspended) Icons.Default.LockOpen else Icons.Default.Block,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isSuspended) "আনব্যান" else "সাসপেন্ড", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailDialog(
  user: AdminUserRecord,
  onDismiss: () -> Unit,
  onApprove: () -> Unit,
  onReject: () -> Unit,
  onToggleSuspend: () -> Unit,
  onSaveNote: (String) -> Unit
) {
  val context = LocalContext.current
  var adminNoteInput by remember { mutableStateOf(user.adminNotes) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "ব্যবহারকারীর পূর্ণাঙ্গ তথ্য",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "আইডি: ${user.id}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
          // Profile card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PinkPrimary.copy(alpha = 0.06f)),
            border = BorderStroke(1.dp, PinkPrimary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text("📧 ইমেইল: ${user.email}", style = MaterialTheme.typography.bodySmall)
              Text("📞 মোবাইল: ${user.phone}", style = MaterialTheme.typography.bodySmall)
              Text("🩸 রক্তের গ্রুপ: ${user.bloodGroup} | বয়স: ${user.age} বছর", style = MaterialTheme.typography.bodySmall)
              Text("📅 রেজিস্ট্রেশন: ${user.registeredAt} | শেষ সক্রিয়: ${user.lastActive}", style = MaterialTheme.typography.bodySmall)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Identification & Uploaded Document Card
          Text(
            text = "🪪 পরিচয়পত্র ও ভেরিফিকেশন ডকুমেন্ট",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "ডকুমেন্ট টাইপ:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                Text(text = user.idType, style = MaterialTheme.typography.bodySmall.copy(color = PinkPrimaryDark, fontWeight = FontWeight.Bold))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "আইডি নম্বর:", style = MaterialTheme.typography.bodySmall)
                Text(text = user.idNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "শিক্ষা প্রতিষ্ঠান/এলাকা:", style = MaterialTheme.typography.bodySmall)
                Text(text = user.institutionOrArea, style = MaterialTheme.typography.bodySmall)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Image, contentDescription = null, tint = PinkPrimary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("সংযুক্ত আইডি কার্ড স্ক্যান কপি", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Text(user.verificationDocumentNote, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Emergency Contacts
          Text(
            text = "🚨 জরুরি অভিভাবক ও কন্টাক্ট নম্বর",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("১ম অভিভাবক: ${user.emergencyGuardianName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                  Text("সম্পর্ক: ${user.emergencyGuardianRelation} • ${user.emergencyGuardianPhone}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.emergencyGuardianPhone.replace(" ", "")}"))
                  context.startActivity(intent)
                }) {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = SafeGreenDark)
                }
              }

              if (user.secondaryContactName.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("২য় কন্টাক্ট: ${user.secondaryContactName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Text("নম্বর: ${user.secondaryContactPhone}", style = MaterialTheme.typography.bodySmall)
                  }
                  IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.secondaryContactPhone.replace(" ", "")}"))
                    context.startActivity(intent)
                  }) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SafeGreenDark)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live GPS & Safety Status
          Text(
            text = "📍 লাইভ জিপিএস ও নিরাপত্তা ঝুঁকি লেভেল",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("শেষ অবস্থান: ${user.lastGpsLocation}", style = MaterialTheme.typography.bodySmall)
              Text("SOS ট্র্রিগার ইতিহাস: ${user.sosTriggersCount} বার বিপদ সংকেত পাঠানো হয়েছে", style = MaterialTheme.typography.bodySmall)
              Text("রিস্ক লেভেল: ${user.activeRiskLevel}", style = MaterialTheme.typography.bodySmall.copy(color = if (user.activeRiskLevel == "High Risk") AlertRed else SafeGreenDark, fontWeight = FontWeight.Bold))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Admin Internal Moderation Notes
          Text(
            text = "📝 অ্যাডমিন ইন্টারনাল নোট ও মন্তব্য",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = adminNoteInput,
            onValueChange = { adminNoteInput = it },
            placeholder = { Text("ব্যবহারকারী সম্পর্কিত অভ্যন্তরীণ পর্যবেক্ষণ লিখুন...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          Button(
            onClick = { onSaveNote(adminNoteInput) },
            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("নোট সেভ করুন", style = MaterialTheme.typography.labelSmall)
          }

          Spacer(modifier = Modifier.height(20.dp))
        }

        // Action Buttons at bottom
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (user.verificationStatus == UserVerificationStatus.PENDING) {
            Button(
              onClick = onApprove,
              colors = ButtonDefaults.buttonColors(containerColor = SafeGreenDark),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(4.dp))
              Text("অনুমোদন দিন")
            }
            OutlinedButton(
              onClick = onReject,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Cancel, contentDescription = null)
              Spacer(modifier = Modifier.width(4.dp))
              Text("বাতিল করুন")
            }
          } else {
            val isSuspended = user.verificationStatus == UserVerificationStatus.SUSPENDED
            Button(
              onClick = onToggleSuspend,
              colors = ButtonDefaults.buttonColors(containerColor = if (isSuspended) SafeGreenDark else AlertRed),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(if (isSuspended) Icons.Default.LockOpen else Icons.Default.Block, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isSuspended) "ইউজারের নিষেধাজ্ঞা প্রত্যাহার করুন" else "সাসপেন্ড / অ্যাকাউন্ট সাময়িক ব্লক করুন")
            }
          }
        }
      }
    }
  }
}

@Composable
fun VerificationApprovalDialog(
  user: AdminUserRecord,
  onDismiss: () -> Unit,
  onApprove: (String) -> Unit,
  onReject: (String) -> Unit
) {
  var noteInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SafeGreenDark)
        Spacer(modifier = Modifier.width(8.dp))
        Text("আইডি ভেরিফিকেশন সিদ্ধান্ত", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column {
        Text(
          text = "ইউজার: ${user.name}",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "আইডি: ${user.idType} (${user.idNumber})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "প্রতিষ্ঠান/ঠিকানা: ${user.institutionOrArea}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "অভিভাবক: ${user.emergencyGuardianName} (${user.emergencyGuardianPhone})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          label = { Text("যাচাইকরণ মন্তব্য / কারণ (ঐচ্ছিক)") },
          placeholder = { Text("যেমন: NID ও অভিভাবকের কল কনফার্ম করা হয়েছে") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onApprove(noteInput) },
        colors = ButtonDefaults.buttonColors(containerColor = SafeGreenDark)
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("ভেরিফাই অনুমোদন (Approve)")
      }
    },
    dismissButton = {
      TextButton(
        onClick = { onReject(noteInput.ifBlank { "Invalid or unverified document" }) },
        colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
      ) {
        Text("বাতিল (Reject)")
      }
    }
  )
}

@Composable
fun AdminBroadcastDialog(
  onDismiss: () -> Unit,
  onSend: (String, String, String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var message by remember { mutableStateOf("") }
  var priority by remember { mutableStateOf("CRITICAL_RED") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Campaign, contentDescription = null, tint = AlertRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text("জরুরি নিরাপত্তা ব্রডকাস্ট", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column {
        Text(
          text = "এই মেসেজটি সমস্ত নিবন্ধিত ব্যবহারকারীর অ্যাপে পুশ নোটিফিকেশন ও অ্যালার্ট ব্যানার হিসেবে দেখানো হবে।",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("অ্যালার্ট শিরোনাম") },
          placeholder = { Text("যেমন: ফার্মগেট এলাকায় বিশেষ সতর্কতা") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = message,
          onValueChange = { message = it },
          label = { Text("বিস্তারিত নিরাপত্তা বার্তা") },
          placeholder = { Text("জরুরি নির্দেশনা বা পরামর্শ লিখুন...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = priority == "CRITICAL_RED",
            onClick = { priority = "CRITICAL_RED" },
            label = { Text("🚨 লাল সতর্কতা") }
          )
          FilterChip(
            selected = priority == "BLUE_INFO",
            onClick = { priority = "BLUE_INFO" },
            label = { Text("ℹ️ সাধারণ তথ্য") }
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && message.isNotBlank()) {
            onSend(title, message, priority)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
        enabled = title.isNotBlank() && message.isNotBlank()
      ) {
        Text("সবার কাছে ব্রডকাস্ট পাঠান")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("বাতিল")
      }
    }
  )
}

@Composable
fun ExportRosterDialog(
  csvData: String,
  onDismiss: () -> Unit,
  onCopy: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Download, contentDescription = null, tint = PinkPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("ইউজার ডেটা রোস্টার এক্সপোর্ট", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column {
        Text(
          text = "সকল নিবন্ধিত নারীর জরুরি কন্টাক্ট, অভিভাবক ও আইডি সংক্রান্ত CSV ফরম্যাট প্রিভিউ:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1E1E1E),
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          Text(
            text = csvData,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF81C784)),
            modifier = Modifier
              .padding(10.dp)
              .verticalScroll(rememberScrollState())
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onCopy,
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("রোস্টার কপি করুন")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("বন্ধ করুন")
      }
    }
  )
}
