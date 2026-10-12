package com.example.data.repository

import android.content.Context
import com.example.data.local.StudyMasterDatabase
import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class StudyMasterRepository(context: Context) {
    private val db = StudyMasterDatabase.getDatabase(context)
    private val profileDao = db.userProfileDao()
    private val taskDao = db.dailyTaskDao()
    private val noteDao = db.studyNoteDao()
    private val flashcardDao = db.flashcardDao()
    private val attemptDao = db.mockTestAttemptDao()

    val userProfile: Flow<UserProfile?> = profileDao.getUserProfileFlow()
    val allNotes: Flow<List<StudyNote>> = noteDao.getAllNotes()
    val allFlashcards: Flow<List<Flashcard>> = flashcardDao.getAllFlashcards()
    val mockAttempts: Flow<List<MockTestAttempt>> = attemptDao.getAllAttempts()

    fun getTasksForDay(dayOffset: Int): Flow<List<DailyTask>> = taskDao.getTasksForDay(dayOffset)
    fun getAllUpcomingTasks(): Flow<List<DailyTask>> = taskDao.getAllUpcomingTasks()

    suspend fun initializeDefaultDataIfNeeded() {
        val currentProfile = profileDao.getUserProfile()
        if (currentProfile == null) {
            val defaultProfile = UserProfile()
            profileDao.insertOrUpdateProfile(defaultProfile)
            regeneratePlanForUser(defaultProfile)

            // Seed initial flashcards
            val initialFc = ExamDataProvider.getInitialFlashcards(defaultProfile.targetExamId)
            flashcardDao.insertFlashcards(initialFc)

            // Seed a welcome note
            noteDao.insertNote(
                StudyNote(
                    id = "welcome_note_1",
                    examId = defaultProfile.targetExamId,
                    subjectName = "Preparation Strategy / தயாரிப்பு உத்தி",
                    topicTitle = "Daily Golden Rule for Aspirants",
                    content = "1. Always finish yesterday's revision before starting a new chapter.\n2. Never skip active recall questions.\n3. Log difficult MCQs in the mistake notebook.",
                    contentTa = "1. புதிய பாடத்தைத் தொடங்கும் முன் நேற்றைய பாடத்தை கட்டாயம் மீள்பார்வை செய்யவும்.\n2. பயிற்சி வினாக்களைத் தவிர்க்க வேண்டாம்.\n3. கடினமான வினாக்களை ஸ்மார்ட் குறிப்புகளில் சேமிக்கவும்.",
                    tags = "Strategy, Daily Habits"
                )
            )
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        profileDao.insertOrUpdateProfile(profile)
        regeneratePlanForUser(profile)
    }

    suspend fun updateTargetExam(examId: String) {
        val current = profileDao.getUserProfile() ?: UserProfile()
        val updated = current.copy(targetExamId = examId)
        profileDao.insertOrUpdateProfile(updated)
        regeneratePlanForUser(updated)
    }

    suspend fun updateLanguage(language: StudyLanguage) {
        profileDao.updateLanguage(language)
    }

    suspend fun markTaskComplete(taskId: String, isCompleted: Boolean, actualMinutes: Int) {
        taskDao.updateTaskCompletion(taskId, isCompleted, actualMinutes)
        if (isCompleted && actualMinutes > 0) {
            profileDao.addStudyMinutes(actualMinutes)
        }
    }

    private val planGenerator: com.example.data.generator.IStudyPlanGenerator = com.example.data.generator.StudyPlanGenerator()

    suspend fun regeneratePlanForUser(profile: UserProfile, mockAccuracy: Float? = null) {
        taskDao.clearAllTasks()
        val weakKeywords = profile.weakTopics.split(",", ";", "/").map { it.trim() }.filter { it.isNotBlank() }
        val strongKeywords = profile.strongTopics.split(",", ";", "/").map { it.trim() }.filter { it.isNotBlank() }
        val userProgress = com.example.data.generator.UserProgress(
            weakTopicKeywords = weakKeywords,
            strongTopicKeywords = strongKeywords,
            mockAccuracyScore = mockAccuracy,
            totalMinutesStudied = profile.totalMinutesStudied,
            streakDays = profile.streakDays
        )

        val planResult = planGenerator.generatePlan(
            examId = profile.targetExamId,
            dailyHours = profile.dailyHours,
            preferredSlot = profile.scheduleSlot,
            targetExamDateStr = profile.targetExamDate,
            userProgress = userProgress
        )
        taskDao.insertTasks(planResult.tasks)
    }

    suspend fun reorganizeMissedDays(missedDayOffset: Int = 0): com.example.data.generator.ReorganizationResult {
        val profile = profileDao.getUserProfile() ?: UserProfile()
        val currentTasks = taskDao.getAllUpcomingTasks().firstOrNull() ?: emptyList()
        val weakKeywords = profile.weakTopics.split(",", ";", "/").map { it.trim() }.filter { it.isNotBlank() }
        val userProgress = com.example.data.generator.UserProgress(
            weakTopicKeywords = weakKeywords,
            totalMinutesStudied = profile.totalMinutesStudied,
            streakDays = profile.streakDays
        )
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val reorgResult = planGenerator.autoReorganize(
            existingTasks = currentTasks,
            currentDateStr = todayStr,
            examId = profile.targetExamId,
            dailyHours = profile.dailyHours,
            preferredSlot = profile.scheduleSlot,
            userProgress = userProgress
        )

        taskDao.clearAllTasks()
        taskDao.insertTasks(reorgResult.adjustedTasks)
        return reorgResult
    }

    suspend fun autoCheckAndReorganizeOnStartup(): com.example.data.generator.ReorganizationResult? {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val currentTasks = taskDao.getAllUpcomingTasks().firstOrNull() ?: emptyList()
        val hasMissedPastTasks = currentTasks.any { it.dateString < todayStr && !it.isCompleted }

        return if (hasMissedPastTasks) {
            reorganizeMissedDays(0)
        } else null
    }

    suspend fun addStudyNote(
        examId: String,
        subjectName: String,
        topicTitle: String,
        content: String,
        contentTa: String,
        tags: String
    ) {
        val note = StudyNote(
            id = UUID.randomUUID().toString(),
            examId = examId,
            subjectName = subjectName,
            topicTitle = topicTitle,
            content = content,
            contentTa = contentTa,
            tags = tags
        )
        noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: StudyNote) {
        noteDao.deleteNote(note)
    }

    suspend fun updateFlashcardInterval(card: Flashcard, quality: Int) {
        // Spaced repetition interval algorithm (SuperMemo SM-2 inspired simplified)
        // quality: 0 = Repeat/Hard, 1 = Good, 2 = Easy
        val newInterval = when (quality) {
            0 -> 1 // review again tomorrow
            1 -> if (card.intervalDays <= 1) 3 else (card.intervalDays * 2)
            else -> if (card.intervalDays <= 1) 5 else (card.intervalDays * 3)
        }
        val isMastered = newInterval >= 14
        val nextReview = System.currentTimeMillis() + (newInterval * 24L * 60L * 60L * 1000L)
        val updated = card.copy(
            intervalDays = newInterval,
            repetitions = card.repetitions + 1,
            nextReviewTimestamp = nextReview,
            isMastered = isMastered
        )
        flashcardDao.updateFlashcard(updated)
    }

    suspend fun addFlashcard(card: Flashcard) {
        flashcardDao.insertFlashcards(listOf(card))
    }

    suspend fun recordMockTestAttempt(attempt: MockTestAttempt) {
        attemptDao.insertAttempt(attempt)
    }
}
