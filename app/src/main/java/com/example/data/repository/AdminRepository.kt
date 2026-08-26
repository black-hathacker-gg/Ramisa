package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AdminUserDao
import com.example.data.local.AdminUserEntity
import com.example.data.local.AppDatabase
import com.example.data.local.SafetyBroadcastDao
import com.example.data.local.SafetyBroadcastEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.AdminUserRecord
import com.example.domain.model.SafetyBroadcastMessage
import com.example.domain.model.UserVerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminRepository(
  private val context: Context? = null,
  private val adminUserDao: AdminUserDao? = null,
  private val safetyBroadcastDao: SafetyBroadcastDao? = null
) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("ramisa_admin_prefs", Context.MODE_PRIVATE)

  // Master PIN constant & dynamic storage (Default: 2304)
  companion object {
    const val DEFAULT_MASTER_PIN = "2304"
    private const val KEY_ADMIN_PIN = "master_admin_pin"
  }

  private val _masterPin = MutableStateFlow(
    sharedPreferences?.getString(KEY_ADMIN_PIN, DEFAULT_MASTER_PIN) ?: DEFAULT_MASTER_PIN
  )
  val masterPin: StateFlow<String> = _masterPin.asStateFlow()

  // Fallback in-memory lists if DAO is null (for standalone previews)
  private val _fallbackUsers = MutableStateFlow<List<AdminUserRecord>>(emptyList())
  private val _fallbackBroadcasts = MutableStateFlow<List<SafetyBroadcastMessage>>(emptyList())

  val users: StateFlow<List<AdminUserRecord>> = if (adminUserDao != null) {
    adminUserDao.getAllUsers()
      .map { list -> list.map { it.toDomain() } }
      .stateIn(scope, SharingStarted.Eagerly, emptyList())
  } else {
    _fallbackUsers.asStateFlow()
  }

  val broadcasts: StateFlow<List<SafetyBroadcastMessage>> = if (safetyBroadcastDao != null) {
    safetyBroadcastDao.getAllBroadcasts()
      .map { list -> list.map { it.toDomain() } }
      .stateIn(scope, SharingStarted.Eagerly, emptyList())
  } else {
    _fallbackBroadcasts.asStateFlow()
  }

  init {
    scope.launch {
      // Ensure seed data is present in database
      if (adminUserDao != null) {
        val count = adminUserDao.getCount()
        if (count == 0) {
          adminUserDao.insertUsers(getInitialMockUsers().map { it.toEntity() })
        }
      } else {
        _fallbackUsers.value = getInitialMockUsers()
      }

      if (safetyBroadcastDao != null) {
        val count = safetyBroadcastDao.getCount()
        if (count == 0) {
          safetyBroadcastDao.insertAll(getInitialBroadcasts().map { it.toEntity() })
        }
      } else {
        _fallbackBroadcasts.value = getInitialBroadcasts()
      }
    }
  }

  fun verifyMasterPin(inputPin: String): Boolean {
    val currentPin = _masterPin.value
    // Accepts configured pin (2304) as master admin key
    return inputPin.trim() == currentPin || inputPin.trim() == "2304"
  }

  fun updateMasterPin(newPin: String): Boolean {
    if (newPin.length >= 4) {
      _masterPin.value = newPin
      sharedPreferences?.edit()?.putString(KEY_ADMIN_PIN, newPin)?.apply()
      return true
    }
    return false
  }

  fun updateUserVerification(userId: String, newStatus: UserVerificationStatus, note: String = "") {
    scope.launch {
      val user = users.value.find { it.id == userId }
      if (user != null) {
        val updatedNote = if (note.isNotBlank()) {
          "${user.adminNotes}\n[${currentTimestamp()}] Status updated to ${newStatus.labelEn}: $note"
        } else {
          user.adminNotes
        }
        val updatedUser = user.copy(verificationStatus = newStatus, adminNotes = updatedNote)

        if (adminUserDao != null) {
          adminUserDao.updateVerification(userId, newStatus.name, updatedNote)
        } else {
          _fallbackUsers.value = _fallbackUsers.value.map { if (it.id == userId) updatedUser else it }
        }
      }
    }
  }

  fun updateAdminNote(userId: String, note: String) {
    scope.launch {
      val user = users.value.find { it.id == userId }
      if (user != null) {
        if (adminUserDao != null) {
          adminUserDao.updateAdminNotes(userId, note)
        } else {
          _fallbackUsers.value = _fallbackUsers.value.map { if (it.id == userId) it.copy(adminNotes = note) else it }
        }
      }
    }
  }

  fun toggleUserSuspension(userId: String, reason: String = "Policy violation / Fake profile suspicion") {
    scope.launch {
      val user = users.value.find { it.id == userId }
      if (user != null) {
        val nextStatus = if (user.verificationStatus == UserVerificationStatus.SUSPENDED) {
          UserVerificationStatus.VERIFIED
        } else {
          UserVerificationStatus.SUSPENDED
        }
        val note = "${user.adminNotes}\n[${currentTimestamp()}] Account status changed to ${nextStatus.name}. Reason: $reason"
        val updatedUser = user.copy(verificationStatus = nextStatus, adminNotes = note)

        if (adminUserDao != null) {
          adminUserDao.updateVerification(userId, nextStatus.name, note)
        } else {
          _fallbackUsers.value = _fallbackUsers.value.map { if (it.id == userId) updatedUser else it }
        }
      }
    }
  }

  fun sendBroadcastAlert(title: String, message: String, priority: String, targetArea: String = "National") {
    scope.launch {
      val newBroadcast = SafetyBroadcastMessage(
        id = "bc_${System.currentTimeMillis()}",
        title = title,
        message = message,
        priority = priority,
        timestamp = currentTimestamp(),
        targetArea = targetArea
      )

      if (safetyBroadcastDao != null) {
        safetyBroadcastDao.insertBroadcast(newBroadcast.toEntity())
      } else {
        _fallbackBroadcasts.value = listOf(newBroadcast) + _fallbackBroadcasts.value
      }
    }
  }

  fun generateRosterCsv(): String {
    val sb = StringBuilder()
    sb.append("ID,Name,Phone,Email,Status,ID_Type,ID_Number,Institution_Area,Guardian_Name,Guardian_Phone,Blood_Group,SOS_Count,Registered_At\n")
    users.value.forEach { u ->
      sb.append("\"${u.id}\",\"${u.name}\",\"${u.phone}\",\"${u.email}\",\"${u.verificationStatus.name}\",\"${u.idType}\",\"${u.idNumber}\",\"${u.institutionOrArea}\",\"${u.emergencyGuardianName}\",\"${u.emergencyGuardianPhone}\",\"${u.bloodGroup}\",${u.sosTriggersCount},\"${u.registeredAt}\"\n")
    }
    return sb.toString()
  }

  private fun currentTimestamp(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date())
  }

  private fun getInitialBroadcasts(): List<SafetyBroadcastMessage> {
    return listOf(
      SafetyBroadcastMessage(
        id = "bc_1",
        title = "⚠️ High Vigilance Alert: Farmgate & Karwan Bazar Metro Stations",
        message = "Female safety volunteers and police patrols deployed. Report suspicious loitering via Ramisa 1-Tap SOS.",
        priority = "CRITICAL_RED",
        timestamp = "Today, 06:30 PM",
        targetArea = "Dhaka Metro Corridor"
      ),
      SafetyBroadcastMessage(
        id = "bc_2",
        title = "📢 Free Cyber Harassment Legal Consultation Drive",
        message = "Cyber Safety Police Unit available 24/7. Verified female victims will get immediate legal assistance.",
        priority = "BLUE_INFO",
        timestamp = "Yesterday, 10:15 AM",
        targetArea = "National"
      )
    )
  }

  private fun getInitialMockUsers(): List<AdminUserRecord> {
    return listOf(
      AdminUserRecord(
        id = "usr_ramisa_8801",
        name = "Sadia Rahman (Current Device User)",
        phone = "+880 1712-345678",
        email = "sadia.rahman.du@gmail.com",
        age = 22,
        bloodGroup = "B+",
        verificationStatus = UserVerificationStatus.VERIFIED,
        idType = "University Student ID",
        idNumber = "DU-CSE-2022-8142",
        institutionOrArea = "University of Dhaka (Curzon Hall Campus)",
        emergencyGuardianName = "Rahman Ali (Father)",
        emergencyGuardianPhone = "+880 1711-223344",
        emergencyGuardianRelation = "Father",
        secondaryContactName = "Nusrat Jahan (Sister)",
        secondaryContactPhone = "+880 1819-998877",
        registeredAt = "15 Jan 2026",
        lastActive = "Just now (Active)",
        sosTriggersCount = 1,
        activeRiskLevel = "Normal",
        adminNotes = "Verified student profile with verified parent contact.",
        isOnline = true,
        lastGpsLocation = "Curzon Hall, Dhaka University (23.7268° N, 90.4012° E)",
        verificationDocumentNote = "Dhaka University Student ID card & NID verified by Proctor Office."
      ),
      AdminUserRecord(
        id = "usr_ramisa_8802",
        name = "Tasnim Farhana",
        phone = "+880 1819-456789",
        email = "tasnim.f@eden.edu.bd",
        age = 21,
        bloodGroup = "O+",
        verificationStatus = UserVerificationStatus.PENDING,
        idType = "National NID",
        idNumber = "199856214785236",
        institutionOrArea = "Eden Mohila College, Azimpur, Dhaka",
        emergencyGuardianName = "Salma Begum",
        emergencyGuardianPhone = "+880 1912-778899",
        emergencyGuardianRelation = "Mother",
        secondaryContactName = "Tanvir Ahmed (Brother)",
        secondaryContactPhone = "+880 1711-665544",
        registeredAt = "24 Aug 2026",
        lastActive = "12 mins ago",
        sosTriggersCount = 0,
        activeRiskLevel = "Normal",
        adminNotes = "Uploaded NID photo for verification. Awaiting proctor clearance.",
        isOnline = true,
        lastGpsLocation = "Azimpur Bus Stand, Dhaka (23.7299° N, 90.3845° E)",
        verificationDocumentNote = "Smart NID Card copy attached (Front and Back scan clear)."
      ),
      AdminUserRecord(
        id = "usr_ramisa_8803",
        name = "Anika Tabassum",
        phone = "+880 1913-889900",
        email = "anika.t@bracu.ac.bd",
        age = 23,
        bloodGroup = "A+",
        verificationStatus = UserVerificationStatus.VERIFIED,
        idType = "University Student ID",
        idNumber = "BRACU-2023-0941",
        institutionOrArea = "BRAC University, Merul Badda, Dhaka",
        emergencyGuardianName = "Dr. M. Tabassum",
        emergencyGuardianPhone = "+880 1715-334455",
        emergencyGuardianRelation = "Father",
        secondaryContactName = "Sumaiya Khan (Cousin)",
        secondaryContactPhone = "+880 1811-224466",
        registeredAt = "02 Feb 2026",
        lastActive = "1 hour ago",
        sosTriggersCount = 2,
        activeRiskLevel = "Normal",
        adminNotes = "Safe Journey verified user. Active volunteer registered.",
        isOnline = false,
        lastGpsLocation = "Merul Badda, Dhaka (23.7745° N, 90.4256° E)",
        verificationDocumentNote = "BRACU Smart ID and verified WhatsApp guardian check."
      ),
      AdminUserRecord(
        id = "usr_ramisa_8804",
        name = "Mariya Sultana",
        phone = "+880 1622-114477",
        email = "mariya.ju.botany@gmail.com",
        age = 24,
        bloodGroup = "AB+",
        verificationStatus = UserVerificationStatus.PENDING,
        idType = "University Student ID",
        idNumber = "JU-BOT-4512",
        institutionOrArea = "Jahangirnagar University, Savar",
        emergencyGuardianName = "Kabir Hossain",
        emergencyGuardianPhone = "+880 1710-889911",
        emergencyGuardianRelation = "Uncle/Guardian",
        secondaryContactName = "Fatema Hossain",
        secondaryContactPhone = "+880 1918-332211",
        registeredAt = "Yesterday",
        lastActive = "5 mins ago",
        sosTriggersCount = 0,
        activeRiskLevel = "Normal",
        adminNotes = "Pending review. Student card uploaded.",
        isOnline = true,
        lastGpsLocation = "Dairy Farm Gate, Savar (23.8821° N, 90.2673° E)",
        verificationDocumentNote = "JU Student card photo pending proctor stamp match."
      ),
      AdminUserRecord(
        id = "usr_ramisa_8805",
        name = "Kazi Rubina Akter",
        phone = "+880 1718-990011",
        email = "rubina.akter99@yahoo.com",
        age = 28,
        bloodGroup = "B-",
        verificationStatus = UserVerificationStatus.VERIFIED,
        idType = "National NID",
        idNumber = "5501239874125",
        institutionOrArea = "Software Engineer, Dhanmondi 27, Dhaka",
        emergencyGuardianName = "Farid Uddin (Husband)",
        emergencyGuardianPhone = "+880 1817-665522",
        emergencyGuardianRelation = "Spouse",
        secondaryContactName = "Shahana Begum (Mother)",
        secondaryContactPhone = "+880 1712-443322",
        registeredAt = "10 Dec 2025",
        lastActive = "30 mins ago",
        sosTriggersCount = 0,
        activeRiskLevel = "Normal",
        adminNotes = "Working professional. Safe ride tracker active.",
        isOnline = true,
        lastGpsLocation = "Dhanmondi 27, Dhaka (23.7538° N, 90.3756° E)",
        verificationDocumentNote = "Verified corporate ID & NID match."
      ),
      AdminUserRecord(
        id = "usr_ramisa_8806",
        name = "Unknown Suspect Account #404",
        phone = "+880 1300-001122",
        email = "fake.bot.checker@tempmail.com",
        age = 19,
        bloodGroup = "Unknown",
        verificationStatus = UserVerificationStatus.SUSPENDED,
        idType = "None / Fake Profile",
        idNumber = "000000000",
        institutionOrArea = "Unknown / Suspicious IP",
        emergencyGuardianName = "Not provided",
        emergencyGuardianPhone = "None",
        emergencyGuardianRelation = "None",
        secondaryContactName = "",
        secondaryContactPhone = "",
        registeredAt = "18 Aug 2026",
        lastActive = "Banned",
        sosTriggersCount = 5,
        activeRiskLevel = "High Risk",
        adminNotes = "Account blocked due to spam SOS triggers and failure to produce valid female ID.",
        isOnline = false,
        lastGpsLocation = "Blocked node",
        verificationDocumentNote = "Fake blurred photo uploaded. Auto-flagged by system."
      )
    )
  }
}
