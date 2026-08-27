package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain.model.EmergencyContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmergencyContact::class,
        UserProfileEntity::class,
        AdminUserEntity::class,
        SafetyBroadcastEntity::class,
        CommunityPostEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun userDao(): UserDao
    abstract fun adminUserDao(): AdminUserDao
    abstract fun safetyBroadcastDao(): SafetyBroadcastDao
    abstract fun communityDao(): CommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ramisa_safety_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {


                    // Seed Admin Users
                    val adminDao = database.adminUserDao()
                    if (adminDao.getCount() == 0) {
                        adminDao.insertUsers(listOf(
                            AdminUserEntity(
                                id = "usr_ramisa_8801",
                                name = "RAMISA User (Current Device User)",
                                phone = "+880 1712-345678",
                                email = "sadia.rahman.du@gmail.com",
                                age = 22,
                                bloodGroup = "B+",
                                verificationStatus = "VERIFIED",
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
                            AdminUserEntity(
                                id = "usr_ramisa_8802",
                                name = "Tasnim Farhana",
                                phone = "+880 1819-456789",
                                email = "tasnim.f@eden.edu.bd",
                                age = 21,
                                bloodGroup = "O+",
                                verificationStatus = "PENDING",
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
                            AdminUserEntity(
                                id = "usr_ramisa_8803",
                                name = "Anika Tabassum",
                                phone = "+880 1913-889900",
                                email = "anika.t@bracu.ac.bd",
                                age = 23,
                                bloodGroup = "A+",
                                verificationStatus = "VERIFIED",
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
                            AdminUserEntity(
                                id = "usr_ramisa_8804",
                                name = "Mariya Sultana",
                                phone = "+880 1622-114477",
                                email = "mariya.ju.botany@gmail.com",
                                age = 24,
                                bloodGroup = "AB+",
                                verificationStatus = "PENDING",
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
                            AdminUserEntity(
                                id = "usr_ramisa_8805",
                                name = "Kazi Rubina Akter",
                                phone = "+880 1718-990011",
                                email = "rubina.akter99@yahoo.com",
                                age = 28,
                                bloodGroup = "B-",
                                verificationStatus = "VERIFIED",
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
                            AdminUserEntity(
                                id = "usr_ramisa_8806",
                                name = "Unknown Suspect Account #404",
                                phone = "+880 1300-001122",
                                email = "fake.bot.checker@tempmail.com",
                                age = 19,
                                bloodGroup = "Unknown",
                                verificationStatus = "SUSPENDED",
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
                        ))
                    }

                    // Seed Broadcasts
                    val broadcastDao = database.safetyBroadcastDao()
                    if (broadcastDao.getCount() == 0) {
                        broadcastDao.insertAll(listOf(
                            SafetyBroadcastEntity(
                                id = "bc_1",
                                title = "⚠️ High Vigilance Alert: Farmgate & Karwan Bazar Metro Stations",
                                message = "Female safety volunteers and police patrols deployed. Report suspicious loitering via Ramisa 1-Tap SOS.",
                                priority = "CRITICAL_RED",
                                timestamp = "Today, 06:30 PM",
                                targetArea = "Dhaka Metro Corridor"
                            ),
                            SafetyBroadcastEntity(
                                id = "bc_2",
                                title = "📢 Free Cyber Harassment Legal Consultation Drive",
                                message = "Cyber Safety Police Unit available 24/7. Verified female victims will get immediate legal assistance.",
                                priority = "BLUE_INFO",
                                timestamp = "Yesterday, 10:15 AM",
                                targetArea = "National"
                            )
                        ))
                    }

                    // Seed Community Posts
                    val communityDao = database.communityDao()
                    if (communityDao.getCount() == 0) {
                        communityDao.insertPosts(listOf(
                            CommunityPostEntity(
                                id = "post_sos_1",
                                categoryId = "EMERGENCY_SOS",
                                authorName = "Nusrat Jahan",
                                authorRole = "Campus Guard Proctor",
                                authorPhone = "+880 1711-889900",
                                isAnonymous = false,
                                title = "🚨 Notice: Extra Volunteers Stationed Near Curzon & Nilkhet",
                                content = "To all students and night commuters: 4 female student volunteers and campus proctors are on duty along Curzon Hall gate and Nilkhet till 10:30 PM. Call anytime if you need safe walk assistance.",
                                locationTag = "Dhaka University Campus",
                                timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
                                supportCount = 42,
                                helpfulCount = 19,
                                userSupported = false,
                                userHelpful = false,
                                isSosAlert = false,
                                repliesJson = """[{"id":"rep_1","postId":"post_sos_1","authorName":"Tasnim Hossain","authorRole":"Volunteer","isAnonymous":false,"content":"I am near TSC tea stalls. If anyone needs an escort to Rokeya hall, let me know here!","timestamp":${System.currentTimeMillis() - 1000 * 60 * 18},"likes":8}]"""
                            ),
                            CommunityPostEntity(
                                id = "post_harass_1",
                                categoryId = "HARASSMENT_ALERT",
                                authorName = "বেনামী বোন (Anonymous Sister)",
                                authorRole = "Verified Member",
                                authorPhone = "Hidden",
                                isAnonymous = true,
                                title = "⚠️ Beware: Dim lighting near Farmgate Footbridge West Exit",
                                content = "Today around 7:45 PM near the west staircase of Farmgate overbridge, a group of suspicious boys were making inappropriate catcalls. I used Ramisa's Fake Call tool and safely moved into the nearby pharmacy. Please avoid that staircase after sunset or walk in groups.",
                                locationTag = "Farmgate, Dhaka",
                                timestamp = System.currentTimeMillis() - 1000 * 60 * 80,
                                supportCount = 58,
                                helpfulCount = 34,
                                userSupported = false,
                                userHelpful = false,
                                isSosAlert = false,
                                repliesJson = """[{"id":"rep_2","postId":"post_harass_1","authorName":"Advocate Farzana Karim","authorRole":"Legal Advisor","isAnonymous":false,"content":"Thank you for reporting, sister! We have notified the Tejgaon police patrol beat box. If this persists, file a report to Police Cyber & Women Support Center hotline 01320000888.","timestamp":${System.currentTimeMillis() - 1000 * 60 * 45},"likes":14}]"""
                            )
                        ))
                    }
                }
            }
        }
    }
}
