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
        SafetyBroadcastEntity::class,
        CommunityPostEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun userDao(): UserDao
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
                .allowMainThreadQueries()
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
                    // Seed Broadcasts
                    val broadcastDao = database.safetyBroadcastDao()
                    if (broadcastDao.getCount() == 0) {
                        broadcastDao.insertAll(listOf(
                            SafetyBroadcastEntity(
                                id = "bc_1",
                                title = "High Vigilance Alert: Farmgate & Karwan Bazar Metro Stations",
                                message = "Female safety volunteers and police patrols deployed. Report suspicious loitering via Ramisa 1-Tap SOS.",
                                priority = "CRITICAL_RED",
                                timestamp = "Today, 06:30 PM",
                                targetArea = "Dhaka Metro Corridor"
                            ),
                            SafetyBroadcastEntity(
                                id = "bc_2",
                                title = "Free Cyber Harassment Legal Consultation Drive",
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
                                title = "Notice: Extra Volunteers Stationed Near Curzon & Nilkhet",
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
                                title = "Beware: Dim lighting near Farmgate Footbridge West Exit",
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
