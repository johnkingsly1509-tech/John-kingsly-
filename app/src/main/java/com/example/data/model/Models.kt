package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonClass

enum class StudyLanguage(val code: String, val displayNameEn: String, val displayNameTa: String) {
    TAMIL("ta", "Simple Tamil", "எளிய தமிழ்"),
    ENGLISH("en", "Simple English", "எளிய ஆங்கிலம்"),
    BILINGUAL("bi", "Tamil + English Mix", "தமிழ் + English கலவை"),
    FORMAL_ENGLISH("formal", "Formal Exam English", "தேர்வு ஆங்கிலக் குறிப்புகள்")
}

enum class PrepLevel(val titleEn: String, val titleTa: String) {
    BEGINNER("Beginner (Just Starting)", "தொடக்க நிலை (இப்போது தொடங்குபவர்)"),
    INTERMEDIATE("Intermediate (Covered Basics)", "இடைநிலை (அடிப்படை படித்தவர்)"),
    ADVANCED("Advanced (Revising & Mock Tests)", "முதுநிலை (மீள்பார்வை & மாதிரித் தேர்வுகள்)")
}

enum class DailyAvailability(val hours: Int, val labelEn: String, val labelTa: String) {
    ONE_HOUR(1, "1 Hour / Day (High-Yield Focus)", "1 மணி / நாள் (முக்கிய தலைப்புகள் மட்டும்)"),
    TWO_HOURS(2, "2 Hours / Day (Balanced Routine)", "2 மணி / நாள் (சமச்சீர் அட்டவணை)"),
    THREE_HOURS(3, "3 Hours / Day (Steady Progress)", "3 மணி / நாள் (நிலையான முன்னேற்றம்)"),
    FOUR_HOURS(4, "4 Hours / Day (Comprehensive)", "4 மணி / நாள் (விரிவான படிப்பு)"),
    SIX_HOURS(6, "6+ Hours / Day (Full-Time Rigor)", "6+ மணி / நாள் (முழுநேர தீவிர தயாரிப்பு)")
}

enum class StudyScheduleSlot(val labelEn: String, val labelTa: String, val defaultTimes: String) {
    EARLY_MORNING("Early Morning (5 AM - 8 AM)", "அதிகாலை (காலை 5 - 8)", "05:30 AM"),
    AFTERNOON("Afternoon (1 PM - 4 PM)", "பிற்பகல் (மதியம் 1 - 4)", "02:00 PM"),
    EVENING("Evening (5 PM - 8 PM)", "மாலை (மாலை 5 - 8)", "06:00 PM"),
    NIGHT("Night (8 PM - 11 PM)", "இரவு (இரவு 8 - 11)", "09:00 PM")
}

enum class StudentStatus(val labelEn: String, val labelTa: String) {
    FULL_TIME("Full-time Aspirant", "முழுநேர தேர்வு தயாரிப்பாளர்"),
    COLLEGE("College Student alongside", "கல்லூரி மாணவர்"),
    SCHOOL("School Student alongside", "பள்ளி மாணவர்"),
    WORKING("Working Professional alongside", "பணியில் உள்ளவர்")
}

enum class TopicStatus {
    NOT_STARTED,
    LEARNING,
    NEEDS_REVISION,
    COMPLETED
}

enum class TaskActivityType(val labelEn: String, val labelTa: String, val iconName: String) {
    REVISE("Revise Past Concepts", "முந்தைய கருத்துகள் மீள்பார்வை", "refresh"),
    LEARN_NEW("Learn New Core Topic", "புதிய முக்கிய தலைப்பு கற்றல்", "menu_book"),
    PRACTICE_MCQ("Practice Questions", "பயிற்சி வினாக்கள் தீர்த்தல்", "quiz"),
    MISTAKE_REVIEW("Review Mistakes & Doubts", "தவறுகளை ஆராய்தல் & சந்தேகங்கள்", "bug_report"),
    ANSWER_WRITING("Descriptive Answer Writing", "விளக்க உரை எழுதுதல் பயிற்சி", "edit_note")
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "primary_user",
    val name: String = "Aspirant",
    val targetExamId: String = "tnpsc_grp4",
    val targetExamYear: Int = 2026,
    val targetExamDate: String = "2026-11-15",
    val language: StudyLanguage = StudyLanguage.BILINGUAL,
    val prepLevel: PrepLevel = PrepLevel.BEGINNER,
    val dailyHours: DailyAvailability = DailyAvailability.TWO_HOURS,
    val scheduleSlot: StudyScheduleSlot = StudyScheduleSlot.EARLY_MORNING,
    val studentStatus: StudentStatus = StudentStatus.FULL_TIME,
    val weakTopics: String = "Mental Aptitude, Indian Economy",
    val strongTopics: String = "Indian Polity, Tamil Literature",
    val streakDays: Int = 4,
    val totalMinutesStudied: Int = 420
)

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey val id: String,
    val dayOffset: Int, // 0 for today, 1 for tomorrow, etc.
    val timeSlot: String, // e.g. "06:00 AM - 06:45 AM"
    val examId: String,
    val subjectNameEn: String,
    val subjectNameTa: String,
    val topicTitleEn: String,
    val topicTitleTa: String,
    val chapterNameEn: String,
    val chapterNameTa: String,
    val activityType: TaskActivityType,
    val estimatedMinutes: Int,
    val actualMinutesSpent: Int = 0,
    val isCompleted: Boolean = false,
    val studyMaterialGuidanceEn: String,
    val studyMaterialGuidanceTa: String,
    val questionsToSolveCount: Int = 10,
    val dateString: String // YYYY-MM-DD
)

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey val id: String,
    val examId: String,
    val subjectName: String,
    val topicTitle: String,
    val content: String,
    val contentTa: String = "",
    val tags: String = "Important",
    val isGeneratedFromDoc: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val examId: String,
    val subjectName: String,
    val topicTitle: String,
    val questionEn: String,
    val questionTa: String,
    val answerEn: String,
    val answerTa: String,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false
)

@Entity(tableName = "mock_test_attempts")
data class MockTestAttempt(
    @PrimaryKey val id: String,
    val testId: String,
    val testTitleEn: String,
    val testTitleTa: String,
    val examId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val skippedAnswers: Int,
    val scorePercentage: Float,
    val timeSpentSeconds: Int,
    val completedAt: Long = System.currentTimeMillis()
)

data class ExamInfo(
    val id: String,
    val nameEn: String,
    val nameTa: String,
    val category: String, // UPSC, TNPSC, CTET, NEET, SSC, BANKING, RAILWAY, OTHER
    val badgeColorHex: String,
    val notificationStatus: String,
    val expectedDate: String,
    val patternEn: String,
    val patternTa: String,
    val totalMarks: Int,
    val hasNegativeMarking: Boolean,
    val negativeMarkingRatio: String,
    val subjects: List<ExamSubject>
)

data class ExamSubject(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val weightagePercentage: Int,
    val chapters: List<ExamChapter>
)

data class ExamChapter(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val topics: List<ExamTopic>
)

data class ExamTopic(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val highYieldWeightage: String, // High, Medium, Low
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    val defaultNotesEn: String = "",
    val defaultNotesTa: String = ""
)

data class PyqQuestion(
    val id: String,
    val examId: String,
    val examNameTag: String, // e.g. "TNPSC Group 4 - 2022" or "UPSC Prelims 2023"
    val subjectName: String,
    val topicName: String,
    val questionEn: String,
    val questionTa: String,
    val optionsEn: List<String>,
    val optionsTa: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationTa: String,
    val isOfficialPastPaper: Boolean = true
)

data class MockTest(
    val id: String,
    val examId: String,
    val titleEn: String,
    val titleTa: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val questions: List<PyqQuestion>
)

data class AiLessonResponse(
    val topicTitleEn: String,
    val topicTitleTa: String,
    val simpleExplanationEn: String,
    val simpleExplanationTa: String,
    val importantDefinitionsEn: List<String>,
    val importantDefinitionsTa: List<String>,
    val stepByStepPointsEn: List<String>,
    val stepByStepPointsTa: List<String>,
    val realLifeExamplesEn: String,
    val realLifeExamplesTa: String,
    val importantFactsEn: List<String>,
    val importantFactsTa: List<String>,
    val examOrientedNotesEn: String,
    val examOrientedNotesTa: String,
    val frequentlyTestedConceptsEn: String,
    val frequentlyTestedConceptsTa: String,
    val practiceQuestions: List<PyqQuestion>,
    val sixtySecondSummaryEn: String,
    val sixtySecondSummaryTa: String
)
