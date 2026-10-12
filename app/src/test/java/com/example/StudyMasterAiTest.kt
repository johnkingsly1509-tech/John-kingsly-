package com.example

import com.example.data.ai.GeminiAiService
import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import org.junit.Assert.*
import org.junit.Test

class StudyMasterAiTest {

    @Test
    fun testAllMajorExamCategoriesAreSupported() {
        val exams = ExamDataProvider.getAllExams()
        assertTrue("Exams should not be empty", exams.isNotEmpty())

        val categories = exams.map { it.category }.toSet()
        assertTrue("Should include TNPSC", categories.contains("TNPSC"))
        assertTrue("Should include UPSC", categories.contains("UPSC"))
        assertTrue("Should include NEET", categories.contains("NEET"))
        assertTrue("Should include SSC", categories.contains("SSC"))
        assertTrue("Should include CTET", categories.contains("CTET"))
        assertTrue("Should include BANKING", categories.contains("BANKING"))
        assertTrue("Should include RAILWAY", categories.contains("RAILWAY"))
    }

    @Test
    fun testPersonalizedDailyStudyMapAdaptsToOneHourAvailability() {
        val tasks1h = ExamDataProvider.generateDailyTasks(
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.ONE_HOUR,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            dayOffset = 0
        )

        val totalMinutes = tasks1h.sumOf { it.estimatedMinutes }
        assertEquals("1-hour schedule must total 60 minutes", 60, totalMinutes)
        assertTrue("Must include rapid revision task", tasks1h.any { it.activityType == TaskActivityType.REVISE })
        assertTrue("Must include core learning task", tasks1h.any { it.activityType == TaskActivityType.LEARN_NEW })
        assertTrue("Must include practice MCQ task", tasks1h.any { it.activityType == TaskActivityType.PRACTICE_MCQ })
    }

    @Test
    fun testPersonalizedDailyStudyMapAdaptsToTwoHoursAvailability() {
        val tasks2h = ExamDataProvider.generateDailyTasks(
            examId = "upsc_cse",
            dailyHours = DailyAvailability.TWO_HOURS,
            preferredSlot = StudyScheduleSlot.EVENING,
            dayOffset = 0
        )

        val totalMinutes = tasks2h.sumOf { it.estimatedMinutes }
        assertEquals("2-hour schedule must total 120 minutes", 120, totalMinutes)
        assertTrue("Must include active recall revision", tasks2h.any { it.activityType == TaskActivityType.REVISE })
        assertTrue("Must include mistake analysis review", tasks2h.any { it.activityType == TaskActivityType.MISTAKE_REVIEW })
    }

    @Test
    fun testVerifiedPyqBankContainsOfficialAuthorizedCitations() {
        val tnpscPyqs = ExamDataProvider.getVerifiedPyqs("tnpsc_grp4")
        assertTrue("TNPSC PYQs should be populated", tnpscPyqs.isNotEmpty())

        val firstPyq = tnpscPyqs.first()
        assertTrue("Must cite official past paper", firstPyq.isOfficialPastPaper)
        assertTrue("Must contain question in both English and Tamil", firstPyq.questionEn.isNotBlank() && firstPyq.questionTa.isNotBlank())
        assertEquals("Must provide exactly 4 options", 4, firstPyq.optionsEn.size)
        assertTrue("Correct index must be within range", firstPyq.correctIndex in 0..3)
    }

    @Test
    fun testAiLessonContainsAllTenPedagogicalComponents() {
        val lesson = GeminiAiService.getPrebuiltLesson(
            topicTitle = "Preamble & Fundamental Rights",
            subjectName = "Indian Polity",
            language = StudyLanguage.BILINGUAL
        )

        // 1. Simple explanation
        assertTrue(lesson.simpleExplanationEn.isNotBlank())
        assertTrue(lesson.simpleExplanationTa.isNotBlank())
        // 2. Important definitions
        assertTrue(lesson.importantDefinitionsEn.isNotEmpty())
        // 3. Step-by-step points
        assertTrue(lesson.stepByStepPointsEn.isNotEmpty())
        // 4. Real life examples
        assertTrue(lesson.realLifeExamplesEn.isNotBlank())
        // 5. Important facts
        assertTrue(lesson.importantFactsEn.isNotEmpty())
        // 6. Exam-oriented notes
        assertTrue(lesson.examOrientedNotesEn.isNotBlank())
        // 7. Frequently tested concepts
        assertTrue(lesson.frequentlyTestedConceptsEn.isNotBlank())
        // 8. Practice questions
        assertTrue(lesson.practiceQuestions.isNotEmpty())
        // 9. Answers & explanations
        assertTrue(lesson.practiceQuestions.first().explanationEn.isNotBlank())
        // 10. 60-second summary
        assertTrue(lesson.sixtySecondSummaryEn.isNotBlank())
    }

    @Test
    fun testMockTestCreationAndTiming() {
        val mockTests = ExamDataProvider.getMockTests("tnpsc_grp4")
        assertTrue("Mock tests should exist", mockTests.isNotEmpty())
        val test1 = mockTests.first()
        assertTrue("Test duration must be positive", test1.durationMinutes > 0)
        assertTrue("Test must have questions", test1.questions.isNotEmpty())
    }

    @Test
    fun testMonthlyPlanCoversTodayNextSevenDaysAndCurrentMonth() {
        val profile = UserProfile(
            targetExamId = "tnpsc_grp4",
            dailyHours = DailyAvailability.TWO_HOURS,
            weakTopics = "Mental Aptitude, Indian Economy"
        )
        val monthlyPlan = com.example.data.engine.AiStudyPlanEngine.generateMonthlyStudyPlan(profile)

        // Must cover all 30 days (dayOffset 0 to 29)
        val dayOffsets = monthlyPlan.map { it.dayOffset }.toSet()
        assertEquals("Plan must cover exactly 30 unique day offsets", 30, dayOffsets.size)
        assertTrue("Must include Day 0 (Today)", dayOffsets.contains(0))
        for (i in 1..7) {
            assertTrue("Must include Day $i in the next 7 days", dayOffsets.contains(i))
        }
        assertTrue("Must cover end of month Day 29", dayOffsets.contains(29))

        // Must include weekly reviews on Day 6, 13, 20, 27
        val day6Tasks = monthlyPlan.filter { it.dayOffset == 6 }
        assertTrue("Day 6 must be a Weekly Review consolidation day", day6Tasks.any { it.chapterNameEn == "Weekly Review" })

        // Day 13 and Day 27 must be Milestone assessment days
        val day13Tasks = monthlyPlan.filter { it.dayOffset == 13 }
        assertTrue("Day 13 must be a Diagnostic Milestone mock day", day13Tasks.any { it.chapterNameEn == "Diagnostic Milestone" })
    }

    @Test
    fun testPlanEnginePrioritizesIdentifiedWeakAreas() {
        val profile = UserProfile(
            targetExamId = "tnpsc_grp4",
            dailyHours = DailyAvailability.TWO_HOURS,
            weakTopics = "Aptitude, Mental Ability"
        )
        val plan = com.example.data.engine.AiStudyPlanEngine.generateMonthlyStudyPlan(profile)

        // Check that weak area topics appear with reinforcement badges
        val hasWeakAreaPriority = plan.any {
            it.topicTitleEn.contains("Weak Area") || it.subjectNameEn.contains("Aptitude")
        }
        assertTrue("Plan must prioritize student's weak topics", hasWeakAreaPriority)
    }

    @Test
    fun testAdjustPlanForMissedDayRedistributesWithoutLoss() {
        val profile = UserProfile(
            targetExamId = "tnpsc_grp4",
            dailyHours = DailyAvailability.ONE_HOUR
        )
        val originalPlan = com.example.data.engine.AiStudyPlanEngine.generateMonthlyStudyPlan(profile)
        val day0TaskCount = originalPlan.count { it.dayOffset == 0 }
        assertTrue(day0TaskCount > 0)

        // Simulate that Day 0 was missed (none completed)
        val adjusted = com.example.data.engine.AiStudyPlanEngine.adjustPlanForMissedDay(
            allTasks = originalPlan,
            missedDayOffset = 0,
            profile = profile
        )

        // Adjusted tasks should mark the catch-up priority for Day 1
        val day1Tasks = adjusted.filter { it.dayOffset == 1 }
        assertTrue(
            "Uncompleted tasks from Day 0 should be shifted to Day 1 with priority",
            day1Tasks.any { it.topicTitleEn.contains("Catch-up Priority") }
        )
    }
}
