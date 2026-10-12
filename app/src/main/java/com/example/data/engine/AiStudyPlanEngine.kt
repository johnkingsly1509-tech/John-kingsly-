package com.example.data.engine

import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import java.text.SimpleDateFormat
import java.util.*

object AiStudyPlanEngine {

    /**
     * Generates a complete 30-day (current month) daily study plan taking into account:
     * - Target examination & official syllabus
     * - Available daily hours (1h, 2h, 3h, 4h, 6h+) and preferred time slot
     * - User's identified weak areas
     * - Practice question & mock test accuracy performance
     * - Systematic spaced revision (Day +1 recall, Day +7 weekly test, Day +14 milestone)
     */
    fun generateMonthlyStudyPlan(
        profile: UserProfile,
        mockAccuracy: Float? = null,
        startDateCalendar: Calendar = Calendar.getInstance()
    ): List<DailyTask> {
        val exam = ExamDataProvider.getExamById(profile.targetExamId)
        val allTasks = mutableListOf<DailyTask>()
        val weakAreaKeywords = profile.weakTopics
            .split(",", ";", "/")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        // Flatten all topics across all subjects in the exam
        val flattenedTopics = mutableListOf<Pair<ExamSubject, ExamTopic>>()
        exam.subjects.forEach { subject ->
            subject.chapters.forEach { chapter ->
                chapter.topics.forEach { topic ->
                    flattenedTopics.add(subject to topic)
                }
            }
        }
        if (flattenedTopics.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Track what was studied on previous day for spaced recall
        var previousDayTopicEn = "Core Fundamentals & Terminology"
        var previousDayTopicTa = "அடிப்படை முக்கிய கோட்பாடுகள்"

        // Generate day 0 (Today) through day 29 (30 Days)
        for (dayOffset in 0..29) {
            val cal = startDateCalendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, dayOffset)
            val dateStr = sdf.format(cal.time)

            val isWeeklyReviewDay = (dayOffset % 7 == 6) // Day 6, 13, 20, 27
            val isBiweeklyMilestone = (dayOffset == 13 || dayOffset == 27)

            if (isBiweeklyMilestone) {
                // Day 14 or 28: Bi-weekly Full Diagnostic Mock & Deep Mistake Elimination
                allTasks.addAll(
                    createMilestoneAssessmentDay(
                        dayOffset = dayOffset,
                        exam = exam,
                        profile = profile,
                        dateStr = dateStr,
                        mockAccuracy = mockAccuracy
                    )
                )
            } else if (isWeeklyReviewDay) {
                // Day 7, 21: Weekly Consolidation, Spaced Revision & Sectional Practice
                allTasks.addAll(
                    createWeeklyReviewDay(
                        dayOffset = dayOffset,
                        exam = exam,
                        profile = profile,
                        dateStr = dateStr,
                        weakAreaKeywords = weakAreaKeywords
                    )
                )
            } else {
                // Standard Daily Study Day
                // Pick topic from flattened list, prioritizing weak areas every 2nd or 3rd day
                val shouldPrioritizeWeak = (dayOffset % 2 == 1) && weakAreaKeywords.isNotEmpty()
                val candidatePair = if (shouldPrioritizeWeak) {
                    flattenedTopics.firstOrNull { (subj, top) ->
                        weakAreaKeywords.any { kw ->
                            subj.titleEn.lowercase().contains(kw) ||
                            subj.titleTa.lowercase().contains(kw) ||
                            top.titleEn.lowercase().contains(kw) ||
                            top.titleTa.lowercase().contains(kw)
                        }
                    } ?: flattenedTopics[dayOffset % flattenedTopics.size]
                } else {
                    flattenedTopics[dayOffset % flattenedTopics.size]
                }

                val subject = candidatePair.first
                val topic = candidatePair.second
                val chapter = subject.chapters.firstOrNull() ?: ExamChapter("c1", "Core Chapter", "அடிப்படை அத்தியாயம்", emptyList())

                val dayTasks = createStandardDayTasks(
                    dayOffset = dayOffset,
                    exam = exam,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    profile = profile,
                    dateStr = dateStr,
                    previousDayTopicEn = previousDayTopicEn,
                    previousDayTopicTa = previousDayTopicTa,
                    isWeakArea = weakAreaKeywords.any { kw ->
                        subject.titleEn.lowercase().contains(kw) || topic.titleEn.lowercase().contains(kw)
                    },
                    mockAccuracy = mockAccuracy
                )

                allTasks.addAll(dayTasks)

                // Update previous day topic for tomorrow's spaced recall
                previousDayTopicEn = topic.titleEn
                previousDayTopicTa = topic.titleTa
            }
        }

        return allTasks
    }

    private fun createStandardDayTasks(
        dayOffset: Int,
        exam: ExamInfo,
        subject: ExamSubject,
        chapter: ExamChapter,
        topic: ExamTopic,
        profile: UserProfile,
        dateStr: String,
        previousDayTopicEn: String,
        previousDayTopicTa: String,
        isWeakArea: Boolean,
        mockAccuracy: Float?
    ): List<DailyTask> {
        val tasks = mutableListOf<DailyTask>()
        val slot = profile.scheduleSlot
        val hours = profile.dailyHours.hours

        // Adaptation based on Mock Accuracy:
        // If accuracy is low (<60%), increase active revision and question drill time
        val isLowAccuracy = (mockAccuracy != null && mockAccuracy < 60f)

        when (profile.dailyHours) {
            DailyAvailability.ONE_HOUR -> {
                // 60-Minute High-Yield Schedule
                // Task 1: Rapid Yesterday Revision (15m)
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${slot.defaultTimes} - +15m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Spaced Recall: $previousDayTopicEn",
                        topicTitleTa = "இடைவெளி மீள்பார்வை: $previousDayTopicTa",
                        chapterNameEn = "Spaced Revision",
                        chapterNameTa = "மீள்பார்வை முறை",
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 15,
                        studyMaterialGuidanceEn = "Review flashcards and core summary bullets from yesterday. Zero passive rereading.",
                        studyMaterialGuidanceTa = "நேற்று படித்தவற்றின் முக்கிய சுருக்கம் மற்றும் ஃபிளாஷ்கார்டுகளை சுயபரிசோதனை செய்யவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )

                // Task 2: Core Topic Learning (35m)
                val weakNoteEn = if (isWeakArea) " [🎯 High-Priority Weak Area Focus]" else ""
                val weakNoteTa = if (isWeakArea) " [🎯 பலவீன பகுதி சிறப்பு கவனம்]" else ""

                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+15m - +50m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "${topic.titleEn}$weakNoteEn",
                        topicTitleTa = "${topic.titleTa}$weakNoteTa",
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

                // Task 3: High-Yield Practice Questions (10m)
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "+50m - +60m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Targeted Practice MCQs",
                        topicTitleTa = "இலக்கு பயிற்சி வினாக்கள்",
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
                // 120-Minute Balanced Schedule
                // Task 1: Spaced Active Recall (20m)
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${slot.defaultTimes} - +20m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Active Recall of Yesterday: $previousDayTopicEn",
                        topicTitleTa = "நேற்றைய பாட நினைவுகூர்தல்: $previousDayTopicTa",
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

                // Task 2: Core Topic Mastery (55m)
                val weakPrefixEn = if (isWeakArea) "🎯 [Weak Area Reinforcement] " else ""
                val weakPrefixTa = if (isWeakArea) "🎯 [பலவீன பகுதி வலுவூட்டல்] " else ""

                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+20m - +75m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "$weakPrefixEn${topic.titleEn}",
                        topicTitleTa = "$weakPrefixTa${topic.titleTa}",
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

                // Task 3: PYQ Practice Drill (30m)
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_3",
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

                // Task 4: Mistake Analysis & Note Logging (15m)
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "+105m - +120m",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = if (isLowAccuracy) "Intensive Mistake Rectification & Formula Logging" else "Mistake Analysis & Smart Note Entry",
                        topicTitleTa = if (isLowAccuracy) "தீவிர தவறுகள் திருத்தம் & சூத்திரங்கள் பதிவு" else "தவறுகளை ஆராய்தல் & ஸ்மார்ட் குறிப்புகள் பதிவு",
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
                // 3 to 6+ Hours Rigorous Preparation Schedule
                val minutesPerSlot = (hours * 60) / 4

                // Slot 1: Deep Spaced Revision
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 1 (Morning): 06:00 - 06:45",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Cumulative Spaced Revision: $previousDayTopicEn",
                        topicTitleTa = "முந்தைய பாடங்கள் ஆழ்ந்த மீள்பார்வை: $previousDayTopicTa",
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

                // Slot 2: Core Topic Intensive Study
                val weakFlag = if (isWeakArea) " 🎯 [Priority Topic]" else ""
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 2 (Mid-Day): 10:00 - 11:30",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "${topic.titleEn}$weakFlag",
                        topicTitleTa = "${topic.titleTa}$weakFlag",
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

                // Slot 3: Timed MCQ Simulation
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_3",
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

                // Slot 4: Descriptive Answer Writing or Weak Area Drill
                tasks.add(
                    DailyTask(
                        id = "plan_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 4 (Evening): 19:00 - 20:00",
                        examId = exam.id,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = if (isWeakArea) "Weak Area Intensive Problem Solving" else "Descriptive Writing & Mistake Elimination",
                        topicTitleTa = if (isWeakArea) "பலவீன தலைப்பில் கூடுதல் பயிற்சி" else "விளக்க உரை எழுதுதல் & தவறுகள் நீக்கம்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = if (isWeakArea) TaskActivityType.PRACTICE_MCQ else TaskActivityType.ANSWER_WRITING,
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

    private fun createWeeklyReviewDay(
        dayOffset: Int,
        exam: ExamInfo,
        profile: UserProfile,
        dateStr: String,
        weakAreaKeywords: List<String>
    ): List<DailyTask> {
        val tasks = mutableListOf<DailyTask>()
        val totalMins = profile.dailyHours.hours * 60

        tasks.add(
            DailyTask(
                id = "plan_${dayOffset}_1",
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
                estimatedMinutes = totalMins / 2,
                studyMaterialGuidanceEn = "Reread summary notes, flip all flashcards marked 'Hard', and test active recall.",
                studyMaterialGuidanceTa = "வாரத்தில் படித்த அனைத்து முக்கிய குறிப்புகளையும் ஃபிளாஷ்கார்டுகளையும் முழுமையாக வாசிக்கவும்.",
                questionsToSolveCount = 0,
                dateString = dateStr
            )
        )

        tasks.add(
            DailyTask(
                id = "plan_${dayOffset}_2",
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
                estimatedMinutes = totalMins / 2,
                studyMaterialGuidanceEn = "Attempt a timed mock test in the Mock Tests tab. Analyze weak areas and mistake log.",
                studyMaterialGuidanceTa = "மாதிரித் தேர்வு பகுதியில் நேரக்கட்டுப்பாட்டுடன் தேர்வை எழுதி தவறுகளை ஆராயவும்.",
                questionsToSolveCount = 25,
                dateString = dateStr
            )
        )

        return tasks
    }

    private fun createMilestoneAssessmentDay(
        dayOffset: Int,
        exam: ExamInfo,
        profile: UserProfile,
        dateStr: String,
        mockAccuracy: Float?
    ): List<DailyTask> {
        val tasks = mutableListOf<DailyTask>()
        val totalMins = profile.dailyHours.hours * 60

        tasks.add(
            DailyTask(
                id = "plan_${dayOffset}_1",
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
                estimatedMinutes = (totalMins * 0.6).toInt(),
                studyMaterialGuidanceEn = "Simulate actual examination timing and environment. Strict negative marking applies.",
                studyMaterialGuidanceTa = "உண்மையான தேர்வுச் சூழலில் மாதிரித் தேர்வை எழுதி மதிப்பெண்களை கணக்கிடவும்.",
                questionsToSolveCount = 30,
                dateString = dateStr
            )
        )

        tasks.add(
            DailyTask(
                id = "plan_${dayOffset}_2",
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
                estimatedMinutes = (totalMins * 0.4).toInt(),
                studyMaterialGuidanceEn = "Ask AI Teacher doubts on every question missed today. Save key formulas to Smart Notes.",
                studyMaterialGuidanceTa = "தவறிய வினாக்கள் குறித்து AI ஆசிரியரிடம் விளக்கம் பெற்று ஸ்மார்ட் குறிப்புகளில் பதிவு செய்யவும்.",
                questionsToSolveCount = 0,
                dateString = dateStr
            )
        )

        return tasks
    }

    /**
     * Logic to adjust and redistribute the plan when a day is missed:
     * 1. Finds uncompleted core learning tasks from the missed day.
     * 2. Moves high-priority/weak-area tasks forward into the next day.
     * 3. Gently shifts lower-priority tasks downstream without increasing daily hours.
     */
    fun adjustPlanForMissedDay(
        allTasks: List<DailyTask>,
        missedDayOffset: Int,
        profile: UserProfile
    ): List<DailyTask> {
        val updatedList = allTasks.toMutableList()

        // Uncompleted tasks from missed day
        val uncompletedTasks = updatedList.filter { it.dayOffset == missedDayOffset && !it.isCompleted }
        if (uncompletedTasks.isEmpty()) return allTasks

        // Shift future tasks starting from (missedDayOffset + 1) downstream by 1 day
        // to gracefully absorb the missed day without increasing daily workload
        val shiftedTasks = updatedList.map { task ->
            if (task.dayOffset == missedDayOffset && !task.isCompleted) {
                // Move missed uncompleted task to next active day (missedDayOffset + 1)
                task.copy(
                    dayOffset = missedDayOffset + 1,
                    topicTitleEn = "⚡ [Catch-up Priority] " + task.topicTitleEn.replace("⚡ [Catch-up Priority] ", ""),
                    topicTitleTa = "⚡ [முக்கிய முன்னுரிமை] " + task.topicTitleTa.replace("⚡ [முக்கிய முன்னுரிமை] ", "")
                )
            } else if (task.dayOffset > missedDayOffset && task.dayOffset < 29) {
                // Shift subsequent days downstream by 1 day
                task.copy(dayOffset = (task.dayOffset + 1).coerceAtMost(29))
            } else {
                task
            }
        }

        return shiftedTasks
    }
}
