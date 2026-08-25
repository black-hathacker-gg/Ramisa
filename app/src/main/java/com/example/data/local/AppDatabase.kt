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

@Database(entities = [EmergencyContact::class, UserProfileEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun userDao(): UserDao

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
                .addCallback(DatabaseCallback(context))
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
                    val dao = database.emergencyContactDao()
                    if (dao.getCount() == 0) {
                        dao.insertAll(listOf(
                            EmergencyContact("c1", "Mother (আম্মা)", "+880 1819-112233", "Mother", 1, smsEnabled = true, callEnabled = true, pushEnabled = true, isVerified = true),
                            EmergencyContact("c2", "Father (আব্বু)", "+880 1711-223344", "Father", 2, smsEnabled = true, callEnabled = true, pushEnabled = true, isVerified = true),
                            EmergencyContact("c3", "Sister (আপু)", "+880 1912-334455", "Sister", 3, smsEnabled = true, callEnabled = true, pushEnabled = false, isVerified = true)
                        ))
                    }
                }
            }
        }
    }
}
