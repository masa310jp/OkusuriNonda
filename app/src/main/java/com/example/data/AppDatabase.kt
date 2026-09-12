package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Medicine::class, DoseRecord::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun doseRecordDao(): DoseRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicines ADD COLUMN frequencyType TEXT NOT NULL DEFAULT 'EVERY_DAY'")
                db.execSQL("ALTER TABLE medicines ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE medicines ADD COLUMN daysOfWeek TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medicines ADD COLUMN startDate TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication_reminder_db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
