package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromLanguage(value: StudyLanguage): String = value.name

    @TypeConverter
    fun toLanguage(value: String): StudyLanguage = runCatching { StudyLanguage.valueOf(value) }.getOrDefault(StudyLanguage.BILINGUAL)

    @TypeConverter
    fun fromPrepLevel(value: PrepLevel): String = value.name

    @TypeConverter
    fun toPrepLevel(value: String): PrepLevel = runCatching { PrepLevel.valueOf(value) }.getOrDefault(PrepLevel.BEGINNER)

    @TypeConverter
    fun fromDailyHours(value: DailyAvailability): String = value.name

    @TypeConverter
    fun toDailyHours(value: String): DailyAvailability = runCatching { DailyAvailability.valueOf(value) }.getOrDefault(DailyAvailability.TWO_HOURS)

    @TypeConverter
    fun fromScheduleSlot(value: StudyScheduleSlot): String = value.name

    @TypeConverter
    fun toScheduleSlot(value: String): StudyScheduleSlot = runCatching { StudyScheduleSlot.valueOf(value) }.getOrDefault(StudyScheduleSlot.EARLY_MORNING)

    @TypeConverter
    fun fromStudentStatus(value: StudentStatus): String = value.name

    @TypeConverter
    fun toStudentStatus(value: String): StudentStatus = runCatching { StudentStatus.valueOf(value) }.getOrDefault(StudentStatus.FULL_TIME)

    @TypeConverter
    fun fromTaskActivityType(value: TaskActivityType): String = value.name

    @TypeConverter
    fun toTaskActivityType(value: String): TaskActivityType = runCatching { TaskActivityType.valueOf(value) }.getOrDefault(TaskActivityType.LEARN_NEW)
}

@Database(
    entities = [
        UserProfile::class,
        DailyTask::class,
        StudyNote::class,
        Flashcard::class,
        MockTestAttempt::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyMasterDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun mockTestAttemptDao(): MockTestAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: StudyMasterDatabase? = null

        fun getDatabase(context: Context): StudyMasterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyMasterDatabase::class.java,
                    "study_master_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
