package com.example.data.generator

import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

data class UserProgress(
    val completedTopicIds: Set<String> = emptySet(),
    val learningTopicIds: Set<String> = emptySet(),
    val needsRevisionTopicIds: Set<String> = emptySet(),
    val weakTopicKeywords: List<String> = emptyList(),
    val strongTopicKeywords: List<String> = emptyList(),
    val mockAccuracyScore: Float? = null,
    val totalMinutesStudied: Int = 0,
    val streakDays: Int = 0
)

data class StudyPlanResult(
    val tasks: List<DailyTask>,
    val totalTopicsScheduled: Int,
    val dailyPaceTopicsPerDay: Float,
    val estimatedSyllabusCompletionDays: Int,
    val weakAreaTasksCount: Int,
    val revisionTasksCount: Int
)

data class ReorganizationResult(
    val adjustedTasks: List<DailyTask>,
    val rescheduledTasksCount: Int,
    val summaryEn: String,
    val summaryTa: String
)

interface IStudyPlanGenerator {
    fun generatePlan(
        examId: String,
        dailyHours: DailyAvailability,
        preferredSlot: StudyScheduleSlot,
        targetExamDateStr: String,
        userProgress: UserProgress,
        planDurationDays: Int = 30,
        startCalendar: Calendar = Calendar.getInstance()
    ): StudyPlanResult

    fun autoReorganize(
        existingTasks: List<DailyTask>,
        currentDateStr: String,
        examId: String,
        dailyHours: DailyAvailability,
        preferredSlot: StudyScheduleSlot,
        userProgress: UserProgress
    ): ReorganizationResult
}

class StudyPlanGenerator : IStudyPlanGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun generatePlan(
        examId: String,
        dailyHours: DailyAvailability,
        preferredSlot: StudyScheduleSlot,
        targetExamDateStr: String,
        userProgress: UserProgress,
        planDurationDays: Int,
        startCalendar: Calendar
    ): StudyPlanResult {
        val exam = ExamDataProvider.getExamById(examId)
        val availableMinutes = dailyHours.hours * 60

        // Extract all topics and prioritize by user progress and weak areas
        val allTopics = mutableListOf<Triple<ExamSubject, ExamChapter, ExamTopic>>()
        exam.subjects.forEach { subject ->
            subject.chapters.forEach { chapter ->
                chapter.topics.forEach { topic ->
                    allTopics.add(Triple(subject, chapter, topic))
                }
            }
        }

        if (allTopics.isEmpty()) {
            return StudyPlanResult(emptyList(), 0, 0f, 0, 0, 0)
        }

        // Calculate days to exam
        val daysToExam = calculateDaysRemaining(targetExamDateStr, startCalendar).coerceAtLeast(1)
        val remainingUncompletedCount = allTopics.count { (_, _, topic) -> topic.id !in userProgress.completedTopicIds }
        val dailyPace = (remainingUncompletedCount.toFloat() / daysToExam).coerceAtLeast(0.5f)
        val estimatedCompletionDays = (remainingUncompletedCount / dailyPace).toInt().coerceAtLeast(1)

        // Sort topics:
        // 1. Needs Revision topics first
        // 2. Weak area topics next
        // 3. Not started high-yield topics
        // 4. Completed topics for maintenance
        val prioritizedTopics = allTopics.sortedWith(
            compareByDescending<Triple<ExamSubject, ExamChapter, ExamTopic>> { (_, _, topic) ->
                topic.id in userProgress.needsRevisionTopicIds
            }.thenByDescending { (subj, _, topic) ->
                isWeakTopic(subj, topic, userProgress.weakTopicKeywords)
            }.thenByDescending { (_, _, topic) ->
                topic.highYieldWeightage == "High"
            }.thenBy { (_, _, topic) ->
                topic.id in userProgress.completedTopicIds
            }
        )

        val scheduledTasks = mutableListOf<DailyTask>()
        var topicPointer = 0
        var weakAreaTasksCount = 0
        var revisionTasksCount = 0

        var previousTopicEn = "Core Concepts & Fundamentals"
        var previousTopicTa = "அடிப்படை முக்கிய பாடக்கருத்துகள்"

        for (dayOffset in 0 until planDurationDays) {
            val cal = startCalendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, dayOffset)
            val dateStr = dateFormat.format(cal.time)

            val isWeeklyReview = (dayOffset % 7 == 6)
            val isBiweeklyMilestone = (dayOffset == 13 || dayOffset == 27)

            if (isBiweeklyMilestone) {
                // Bi-weekly Diagnostic Milestone Day
                val milestoneTasks = buildMilestoneTasks(
                    dayOffset = dayOffset,
                    exam = exam,
                    dateStr = dateStr,
                    availableMinutes = availableMinutes
                )
                scheduledTasks.addAll(milestoneTasks)
                revisionTasksCount += milestoneTasks.size
            } else if (isWeeklyReview) {
                // Weekly Consolidation & Timed Sectional Mock Day
                val reviewTasks = buildWeeklyReviewTasks(
                    dayOffset = dayOffset,
                    exam = exam,
                    dateStr = dateStr,
                    availableMinutes = availableMinutes
                )
                scheduledTasks.addAll(reviewTasks)
                revisionTasksCount += reviewTasks.size
            } else {
                // Standard Daily Study Plan
                val currentTriple = prioritizedTopics[topicPointer % prioritizedTopics.size]
                topicPointer++

                val (subject, chapter, topic) = currentTriple
                val isWeak = isWeakTopic(subject, topic, userProgress.weakTopicKeywords)
                if (isWeak) weakAreaTasksCount++

                val dayTasks = buildDailyTaskSet(
                    dayOffset = dayOffset,
                    dateStr = dateStr,
                    exam = exam,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    isWeak = isWeak,
                    previousTopicEn = previousTopicEn,
                    previousTopicTa = previousTopicTa,
                    dailyHours = dailyHours,
                    slot = preferredSlot,
                    mockAccuracy = userProgress.mockAccuracyScore
                )

                scheduledTasks.addAll(dayTasks)
                revisionTasksCount += dayTasks.count { it.activityType == TaskActivityType.REVISE }

                previousTopicEn = topic.titleEn
                previousTopicTa = topic.titleTa
            }
        }

        return StudyPlanResult(
            tasks = scheduledTasks,
            totalTopicsScheduled = allTopics.size,
            dailyPaceTopicsPerDay = dailyPace,
            estimatedSyllabusCompletionDays = estimatedCompletionDays,
            weakAreaTasksCount = weakAreaTasksCount,
            revisionTasksCount = revisionTasksCount
        )
    }

    override fun autoReorganize(
        existingTasks: List<DailyTask>,
        currentDateStr: String,
        examId: String,
        dailyHours: DailyAvailability,
        preferredSlot: StudyScheduleSlot,
        userProgress: UserProgress
    ): ReorganizationResult {
        if (existingTasks.isEmpty()) {
            val freshPlan = generatePlan(
                examId = examId,
                dailyHours = dailyHours,
                preferredSlot = preferredSlot,
                targetExamDateStr = "2026-11-20",
                userProgress = userProgress
            )
            return ReorganizationResult(
                adjustedTasks = freshPlan.tasks,
                rescheduledTasksCount = 0,
                summaryEn = "Created a fresh dynamic study plan based on your current syllabus progress.",
                summaryTa = "உங்கள் தற்போதைய பாடத்திட்ட நிலைக்கு ஏற்ப புதிய படிப்புத் திட்டம் உருவாக்கப்பட்டது."
            )
        }

        // Identify tasks scheduled before today that were never marked completed
        val missedTasks = existingTasks.filter { task ->
            task.dateString < currentDateStr && !task.isCompleted && task.activityType != TaskActivityType.REVISE
        }

        val completedTasks = existingTasks.filter { it.isCompleted }
        val futureOrTodayTasks = existingTasks.filter { it.dateString >= currentDateStr }

        if (missedTasks.isEmpty()) {
            return ReorganizationResult(
                adjustedTasks = existingTasks,
                rescheduledTasksCount = 0,
                summaryEn = "All past study tasks are up to date! Keep up the momentum.",
                summaryTa = "அனைத்து முந்தைய பணிகளும் சரியாக முடிக்கப்பட்டுள்ளன! தொடருங்கள்."
            )
        }

        // Auto-Reorganization Strategy:
        // 1. Tag missed tasks with [Catch-up Priority] and move them into today (Day 0) and tomorrow (Day 1).
        // 2. Adjust subsequent tasks forward by the necessary offsets without increasing daily hours.
        val reorganizedList = mutableListOf<DailyTask>()
        reorganizedList.addAll(completedTasks)

        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        // Reschedule missed core tasks into upcoming slots starting today
        var currentSlotOffset = 0
        missedTasks.forEach { missed ->
            val rescheduledCal = cal.clone() as Calendar
            rescheduledCal.add(Calendar.DAY_OF_YEAR, currentSlotOffset)
            val newDateStr = dateFormat.format(rescheduledCal.time)

            val adjustedTask = missed.copy(
                id = "reorg_${System.currentTimeMillis()}_${missed.id}",
                dayOffset = currentSlotOffset,
                dateString = newDateStr,
                topicTitleEn = "⚡ [Catch-up Priority] " + missed.topicTitleEn.replace("⚡ [Catch-up Priority] ", ""),
                topicTitleTa = "⚡ [முக்கிய முன்னுரிமை] " + missed.topicTitleTa.replace("⚡ [முக்கிய முன்னுரிமை] ", "")
            )
            reorganizedList.add(adjustedTask)
            currentSlotOffset = (currentSlotOffset + 1) % 4 // distribute smoothly across next 4 days
        }

        // Shift future uncompleted tasks forward by 1-2 days to avoid overcrowding
        futureOrTodayTasks.forEach { task ->
            if (!task.isCompleted) {
                val taskCal = cal.clone() as Calendar
                val newOffset = (task.dayOffset + 1).coerceAtMost(29)
                taskCal.add(Calendar.DAY_OF_YEAR, newOffset)
                reorganizedList.add(
                    task.copy(
                        dayOffset = newOffset,
                        dateString = dateFormat.format(taskCal.time)
                    )
                )
            } else {
                reorganizedList.add(task)
            }
        }

        val count = missedTasks.size
        return ReorganizationResult(
            adjustedTasks = reorganizedList.distinctBy { it.id },
            rescheduledTasksCount = count,
            summaryEn = "Auto-reorganized $count missed topic(s) into upcoming sessions with high priority. Daily study duration preserved without overload.",
            summaryTa = "தவறிய $count பாடங்கள் அடுத்தடுத்த நாட்களில் முன்னுரிமையுடன் மறுபகிர்வு செய்யப்பட்டன. தினசரி படிப்பு சுமை அதிகரிக்கப்படவில்லை."
        )
    }

    private fun buildDailyTaskSet(
        dayOffset: Int,
        dateStr: String,
        exam: ExamInfo,
        subject: ExamSubject,
        chapter: ExamChapter,
        topic: ExamTopic,
        isWeak: Boolean,
        previousTopicEn: String,
        previousTopicTa: String,
        dailyHours: DailyAvailability,
        slot: StudyScheduleSlot,
        mockAccuracy: Float?
    ): List<DailyTask> {
        val tasks = mutableListOf<DailyTask>()
        val weakLabelEn = if (isWeak) " [🎯 Weak Area Priority]" else ""
        val weakLabelTa = if (isWeak) " [🎯 பலவீன பகுதி முன்னுரிமை]" else ""

        when (dailyHours) {
            DailyAvailability.ONE_HOUR -> {
                // 15m Yesterday Spaced Recall
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${slot.defaultTimes} - +15m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Spaced Recall: $previousTopicEn",
                        topicTitleTa = "நினைவுகூர்தல்: $previousTopicTa",
                        chapterNameEn = "Spaced Revision",
                        chapterNameTa = "மீள்பார்வை முறை",
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 15,
                        studyMaterialGuidanceEn = "Review flashcards and core summary bullets from yesterday. Zero passive reading.",
                        studyMaterialGuidanceTa = "நேற்று படித்தவற்றின் முக்கிய சுருக்கம் மற்றும் ஃபிளாஷ்கார்டுகளை சுயபரிசோதனை செய்யவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                // 35m Core Topic Mastery
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+15m - +50m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "${topic.titleEn}$weakLabelEn",
                        topicTitleTa = "${topic.titleTa}$weakLabelTa",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = 35,
                        studyMaterialGuidanceEn = "Study definitions and 10-point lesson in AI Teacher tab. Focus on statutory backing.",
                        studyMaterialGuidanceTa = "AI ஆசிரியரின் 10 படிநிலை விளக்கங்களை தமிழில் அல்லது இருமொழியில் படிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                // 10m Targeted Practice MCQs
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "+50m - +60m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "High-Yield Practice Questions",
                        topicTitleTa = "முக்கிய பயிற்சி வினாக்கள்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = 10,
                        studyMaterialGuidanceEn = "Solve 8 past MCQs on today's concept and immediately review wrong options.",
                        studyMaterialGuidanceTa = "8 முந்தைய வினாக்களைத் தீர்த்து உடனடி விளக்கம் பார்க்கவும்.",
                        questionsToSolveCount = 8,
                        dateString = dateStr
                    )
                )
            }

            DailyAvailability.TWO_HOURS -> {
                // 20m Spaced Recall
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${slot.defaultTimes} - +20m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Active Recall of Yesterday: $previousTopicEn",
                        topicTitleTa = "நேற்றைய பாட நினைவுகூர்தல்: $previousTopicTa",
                        chapterNameEn = "Spaced Revision",
                        chapterNameTa = "மீள்பார்வை முறை",
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 20,
                        studyMaterialGuidanceEn = "Use flashcards to test memory. Answer out loud without looking at book.",
                        studyMaterialGuidanceTa = "நேற்றைய முக்கிய சூத்திரங்கள் மற்றும் தகவல்களை ஃபிளாஷ்கார்டு கொண்டு சுயபரிசோதனை செய்யவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                // 55m Core Topic Mastery
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+20m - +75m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "${topic.titleEn}$weakLabelEn",
                        topicTitleTa = "${topic.titleTa}$weakLabelTa",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = 55,
                        studyMaterialGuidanceEn = "Complete official syllabus reading with bilingual audio narration in AI Teacher.",
                        studyMaterialGuidanceTa = "அதிகாரப்பூர்வ பாடப்பகுதியை AI ஆசிரியர் விளக்கத்துடன் ஆழமாகப் படிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                // 30m PYQ Practice Drill
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "+75m - +105m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Verified Past Papers & Question Drill",
                        topicTitleTa = "முந்தைய ஆண்டு வினாக்கள் மற்றும் மாதிரி பயிற்சி",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = 30,
                        studyMaterialGuidanceEn = "Solve 15 topic-specific PYQs from official past exam question papers.",
                        studyMaterialGuidanceTa = "15 முந்தைய ஆண்டு வினாக்களைத் தீர்த்து, தந்திரமான வினாக்களை அடையாளம் காணவும்.",
                        questionsToSolveCount = 15,
                        dateString = dateStr
                    )
                )

                // 15m Mistake Analysis
                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "+105m - +120m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Mistake Analysis & Smart Note Entry",
                        topicTitleTa = "தவறுகளை ஆராய்தல் & ஸ்மார்ட் குறிப்புகள் பதிவு",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.MISTAKE_REVIEW,
                        estimatedMinutes = 15,
                        studyMaterialGuidanceEn = "Write down any missed questions or tricky terms into the Smart Notes tab.",
                        studyMaterialGuidanceTa = "தவறான வினாக்களுக்கான விளக்கங்களை 'Smart Notes' பகுதியில் பதிவு செய்யவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
            }

            else -> {
                // 3-6+ Hours Multislot Rigorous Schedule
                val hours = dailyHours.hours
                val minutesPerSlot = (hours * 60) / 4

                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 1 (Morning): 06:00 - 06:45",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Cumulative Spaced Revision: $previousTopicEn",
                        topicTitleTa = "முந்தைய பாடங்கள் ஆழ்ந்த மீள்பார்வை: $previousTopicTa",
                        chapterNameEn = "Spaced Revision",
                        chapterNameTa = "மீள்பார்வை முறை",
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = minutesPerSlot,
                        studyMaterialGuidanceEn = "Review flashcards, formula sheets, and yesterday's mistake entries.",
                        studyMaterialGuidanceTa = "கடந்த பாடங்களின் ஃபிளாஷ்கார்டுகள் மற்றும் சூத்திரங்களை முழுமையாக நினைவுபடுத்தவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 2 (Mid-Day): 10:00 - 11:30",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "${topic.titleEn}$weakLabelEn",
                        topicTitleTa = "${topic.titleTa}$weakLabelTa",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = minutesPerSlot + 20,
                        studyMaterialGuidanceEn = "Read textbooks, official notifications, and 10-step AI Teacher analysis.",
                        studyMaterialGuidanceTa = "பாடத்திட்டத்தின்படி விரிவான குறிப்புகள் மற்றும் வரைபடங்களை ஆழமாகப் படிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 3 (Afternoon): 14:00 - 15:15",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Timed Exam Simulation & Past Papers",
                        topicTitleTa = "நேரக்கட்டுப்பாடு தேர்வு & முந்தைய வினாக்கள்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = minutesPerSlot,
                        studyMaterialGuidanceEn = "Take a 25-question timed sectional quiz with negative marking calculation.",
                        studyMaterialGuidanceTa = "25 வினாக்கள் கொண்ட மாதிரி வினாடி வினாவை நேரக்கட்டுப்பாட்டுடன் தீர்க்கவும்.",
                        questionsToSolveCount = 25,
                        dateString = dateStr
                    )
                )

                tasks.add(
                    DailyTask(
                        id = "gen_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 4 (Evening): 19:00 - 20:00",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = if (isWeak) "Weak Area Intensive Problem Solving" else "Descriptive Writing & Mistake Elimination",
                        topicTitleTa = if (isWeak) "பலவீன தலைப்பில் கூடுதல் பயிற்சி" else "விளக்க உரை எழுதுதல் & தவறுகள் நீக்கம்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = if (isWeak) TaskActivityType.PRACTICE_MCQ else TaskActivityType.ANSWER_WRITING,
                        estimatedMinutes = (hours * 60) - (minutesPerSlot * 3 + 20),
                        studyMaterialGuidanceEn = "Write 1 mains structured answer (Intro, Body, Conclusion) or solve 10 tricky math/reasoning problems.",
                        studyMaterialGuidanceTa = "மாதிரி முதன்மை தேர்வு வினாவுக்கு கட்டமைப்புடன் விடை எழுதி சுயமாக மதிப்பீடு செய்யவும்.",
                        questionsToSolveCount = 10,
                        dateString = dateStr
                    )
                )
            }
        }

        return tasks
    }

    private fun buildWeeklyReviewTasks(
        dayOffset: Int,
        exam: ExamInfo,
        dateStr: String,
        availableMinutes: Int
    ): List<DailyTask> {
        return listOf(
            DailyTask(
                id = "gen_${dayOffset}_1",
                dayOffset = dayOffset,
                timeSlot = "Slot 1 (Morning)",
                examId = exam.id,
                subjectNameEn = "Weekly Consolidation / வாராந்திர ஒருங்கிணைப்பு",
                subjectNameTa = "வாராந்திர முழு மீள்பார்வை",
                topicTitleEn = "Comprehensive Review of Past 6 Days Topics",
                topicTitleTa = "கடந்த 6 நாட்களில் கற்ற அனைத்து பாடங்களின் முழுமையான மீள்பார்வை",
                chapterNameEn = "Weekly Review",
                chapterNameTa = "வாராந்திர மீள்பார்வை",
                activityType = TaskActivityType.REVISE,
                estimatedMinutes = availableMinutes / 2,
                studyMaterialGuidanceEn = "Reread summary notes, flip all flashcards marked 'Hard', and test active recall.",
                studyMaterialGuidanceTa = "வாரத்தில் படித்த அனைத்து முக்கிய குறிப்புகளையும் ஃபிளாஷ்கார்டுகளையும் முழுமையாக வாசிக்கவும்.",
                questionsToSolveCount = 0,
                dateString = dateStr
            ),
            DailyTask(
                id = "gen_${dayOffset}_2",
                dayOffset = dayOffset,
                timeSlot = "Slot 2 (Evening)",
                examId = exam.id,
                subjectNameEn = "Weekly Mock Test / வாராந்திர மாதிரித் தேர்வு",
                subjectNameTa = "வாராந்திர மாதிரித் தேர்வு & மதிப்பீடு",
                topicTitleEn = "Full Sectional Timed Mock Test",
                topicTitleTa = "முழு பகுதி நேரக்கட்டுப்பாடு மாதிரித் தேர்வு",
                chapterNameEn = "Weekly Mock",
                chapterNameTa = "மாதிரித் தேர்வு",
                activityType = TaskActivityType.PRACTICE_MCQ,
                estimatedMinutes = availableMinutes / 2,
                studyMaterialGuidanceEn = "Attempt a timed mock test in the Mock Tests tab. Analyze weak areas and mistake log.",
                studyMaterialGuidanceTa = "மாதிரித் தேர்வு பகுதியில் நேரக்கட்டுப்பாட்டுடன் தேர்வை எழுதி தவறுகளை ஆராயவும்.",
                questionsToSolveCount = 25,
                dateString = dateStr
            )
        )
    }

    private fun buildMilestoneTasks(
        dayOffset: Int,
        exam: ExamInfo,
        dateStr: String,
        availableMinutes: Int
    ): List<DailyTask> {
        return listOf(
            DailyTask(
                id = "gen_${dayOffset}_1",
                dayOffset = dayOffset,
                timeSlot = "Milestone Slot 1",
                examId = exam.id,
                subjectNameEn = "Bi-Weekly Diagnostic Exam",
                subjectNameTa = "இரண்டு வார முழு மாதிரித் தேர்வு",
                topicTitleEn = "Official Exam Simulation & Benchmark Test",
                topicTitleTa = "அதிகாரப்பூர்வ தேர்வு முறை மாதிரி மதிப்பீடு",
                chapterNameEn = "Diagnostic Milestone",
                chapterNameTa = "முக்கிய மதிப்பீடு",
                activityType = TaskActivityType.PRACTICE_MCQ,
                estimatedMinutes = (availableMinutes * 0.6).toInt(),
                studyMaterialGuidanceEn = "Simulate actual examination timing and environment. Strict negative marking applies.",
                studyMaterialGuidanceTa = "உண்மையான தேர்வுச் சூழலில் மாதிரித் தேர்வை எழுதி மதிப்பெண்களை கணக்கிடவும்.",
                questionsToSolveCount = 30,
                dateString = dateStr
            ),
            DailyTask(
                id = "gen_${dayOffset}_2",
                dayOffset = dayOffset,
                timeSlot = "Milestone Slot 2",
                examId = exam.id,
                subjectNameEn = "Mistake Elimination Marathon",
                subjectNameTa = "தவறுகளை முழுமையாக நீக்கும் பயிற்சி",
                topicTitleEn = "In-depth Error Rectification & Doubt Clarification",
                topicTitleTa = "தவறான வினாக்களுக்கான மூல காரணங்களை ஆராய்ந்து சரிசெய்தல்",
                chapterNameEn = "Mistake Analysis",
                chapterNameTa = "தவறுகள் ஆய்வு",
                activityType = TaskActivityType.MISTAKE_REVIEW,
                estimatedMinutes = (availableMinutes * 0.4).toInt(),
                studyMaterialGuidanceEn = "Ask AI Teacher doubts on every question missed today. Save key formulas to Smart Notes.",
                studyMaterialGuidanceTa = "தவறிய வினாக்கள் குறித்து AI ஆசிரியரிடம் விளக்கம் பெற்று ஸ்மார்ட் குறிப்புகளில் பதிவு செய்யவும்.",
                questionsToSolveCount = 0,
                dateString = dateStr
            )
        )
    }

    private fun isWeakTopic(subject: ExamSubject, topic: ExamTopic, weakKeywords: List<String>): Boolean {
        if (weakKeywords.isEmpty()) return false
        val subName = subject.titleEn.lowercase()
        val topName = topic.titleEn.lowercase()
        val subNameTa = subject.titleTa.lowercase()
        val topNameTa = topic.titleTa.lowercase()

        return weakKeywords.any { kw ->
            val k = kw.trim().lowercase()
            k.isNotBlank() && (subName.contains(k) || topName.contains(k) || subNameTa.contains(k) || topNameTa.contains(k))
        }
    }

    private fun calculateDaysRemaining(targetDateStr: String, currentCal: Calendar): Int {
        return try {
            val targetCal = Calendar.getInstance()
            val parsedDate = dateFormat.parse(targetDateStr)
            if (parsedDate != null) {
                targetCal.time = parsedDate
                val diffMillis = targetCal.timeInMillis - currentCal.timeInMillis
                max(1, (diffMillis / (24L * 60L * 60L * 1000L)).toInt())
            } else 180
        } catch (e: Exception) {
            180
        }
    }
}
