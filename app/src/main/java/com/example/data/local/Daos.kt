package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'primary_user' LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 'primary_user' LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET targetExamId = :examId WHERE id = 'primary_user'")
    suspend fun updateTargetExam(examId: String)

    @Query("UPDATE user_profile SET language = :language WHERE id = 'primary_user'")
    suspend fun updateLanguage(language: StudyLanguage)

    @Query("UPDATE user_profile SET totalMinutesStudied = totalMinutesStudied + :minutes WHERE id = 'primary_user'")
    suspend fun addStudyMinutes(minutes: Int)
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dayOffset = :dayOffset ORDER BY id ASC")
    fun getTasksForDay(dayOffset: Int): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks ORDER BY dayOffset ASC, id ASC")
    fun getAllUpcomingTasks(): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTask>)

    @Query("UPDATE daily_tasks SET isCompleted = :completed, actualMinutesSpent = :actualMinutes WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean, actualMinutes: Int)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM daily_tasks WHERE dayOffset = :dayOffset")
    suspend fun deleteTasksForDay(dayOffset: Int)
}

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<StudyNote>>

    @Query("SELECT * FROM study_notes WHERE examId = :examId ORDER BY timestamp DESC")
    fun getNotesByExam(examId: String): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNote)

    @Delete
    suspend fun deleteNote(note: StudyNote)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewTimestamp ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE isMastered = 0 ORDER BY nextReviewTimestamp ASC")
    fun getDueFlashcards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)
}

@Dao
interface MockTestAttemptDao {
    @Query("SELECT * FROM mock_test_attempts ORDER BY completedAt DESC")
    fun getAllAttempts(): Flow<List<MockTestAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: MockTestAttempt)
}
