package com.example

import com.example.data.generator.StudyPlanGenerator
import com.example.data.generator.UserProgress
import com.example.data.model.DailyAvailability
import com.example.data.model.StudyScheduleSlot
import com.example.data.model.TaskActivityType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class StudyPlanGeneratorTest {

    private lateinit var generator: StudyPlanGenerator

    @Before
    fun setUp() {
        generator = StudyPlanGenerator()
    }

    @Test
    fun testGeneratesAccurateDailyTasksForOneHourAvailability() {
        val userProgress = UserProgress(
            weakTopicKeywords = listOf("Aptitude"),
            mockAccuracyScore = 70f
        )
        val result = generator.generatePlan(
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.ONE_HOUR,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            targetExamDateStr = "2026-11-20",
            userProgress = userProgress,
            planDurationDays = 30
        )

        assertNotNull(result)
        assertTrue("Must generate tasks", result.tasks.isNotEmpty())

        // Verify Day 0 tasks sum to exactly 60 minutes
        val day0Tasks = result.tasks.filter { it.dayOffset == 0 }
        assertEquals("1-hour schedule must have 3 tasks", 3, day0Tasks.size)
        val totalMins = day0Tasks.sumOf { it.estimatedMinutes }
        assertEquals("Total day 0 minutes must equal 60", 60, totalMins)

        // Verify pedagogy mix
        assertTrue(day0Tasks.any { it.activityType == TaskActivityType.REVISE })
        assertTrue(day0Tasks.any { it.activityType == TaskActivityType.LEARN_NEW })
        assertTrue(day0Tasks.any { it.activityType == TaskActivityType.PRACTICE_MCQ })
    }

    @Test
    fun testGeneratesAccurateDailyTasksForTwoHoursAvailability() {
        val userProgress = UserProgress(
            weakTopicKeywords = listOf("Economy"),
            mockAccuracyScore = 65f
        )
        val result = generator.generatePlan(
            examId = "upsc_cse",
            dailyHours = DailyAvailability.TWO_HOURS,
            preferredSlot = StudyScheduleSlot.EVENING,
            targetExamDateStr = "2026-05-24",
            userProgress = userProgress,
            planDurationDays = 30
        )

        val day0Tasks = result.tasks.filter { it.dayOffset == 0 }
        assertEquals("2-hour schedule must have 4 tasks", 4, day0Tasks.size)
        val totalMins = day0Tasks.sumOf { it.estimatedMinutes }
        assertEquals("Total day 0 minutes must equal 120", 120, totalMins)
        assertTrue(day0Tasks.any { it.activityType == TaskActivityType.MISTAKE_REVIEW })
    }

    @Test
    fun testSpacedRevisionScheduleInterleaving() {
        val userProgress = UserProgress()
        val result = generator.generatePlan(
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.TWO_HOURS,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            targetExamDateStr = "2026-11-20",
            userProgress = userProgress,
            planDurationDays = 30
        )

        // Day 6 (7th day) must be a Weekly Review consolidation day
        val day6Tasks = result.tasks.filter { it.dayOffset == 6 }
        assertTrue("Day 6 must contain weekly review consolidation", day6Tasks.any { it.chapterNameEn == "Weekly Review" })

        // Day 13 must be a Diagnostic Milestone mock day
        val day13Tasks = result.tasks.filter { it.dayOffset == 13 }
        assertTrue("Day 13 must contain Diagnostic Milestone", day13Tasks.any { it.chapterNameEn == "Diagnostic Milestone" })
    }

    @Test
    fun testPrioritizesNeedsRevisionAndWeakTopics() {
        val userProgress = UserProgress(
            needsRevisionTopicIds = setOf("pol_2"),
            weakTopicKeywords = listOf("Fundamental Rights")
        )
        val result = generator.generatePlan(
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.TWO_HOURS,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            targetExamDateStr = "2026-11-20",
            userProgress = userProgress,
            planDurationDays = 30
        )

        assertTrue("Must schedule weak area tasks", result.weakAreaTasksCount > 0)
        val firstLearnTask = result.tasks.firstOrNull { it.activityType == TaskActivityType.LEARN_NEW }
        assertNotNull(firstLearnTask)
        assertTrue(
            "Highest priority topic should be scheduled first",
            firstLearnTask!!.topicTitleEn.contains("Fundamental Rights") || firstLearnTask.topicTitleEn.contains("Weak Area")
        )
    }

    @Test
    fun testAutoReorganizeReschedulesMissedDaysWithoutOverload() {
        val userProgress = UserProgress(weakTopicKeywords = listOf("Aptitude"))
        val startCal = Calendar.getInstance()
        startCal.add(Calendar.DAY_OF_YEAR, -2) // Plan was started 2 days ago

        val initialPlan = generator.generatePlan(
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.ONE_HOUR,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            targetExamDateStr = "2026-11-20",
            userProgress = userProgress,
            planDurationDays = 10,
            startCalendar = startCal
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // Past tasks remain uncompleted
        val reorgResult = generator.autoReorganize(
            existingTasks = initialPlan.tasks,
            currentDateStr = todayStr,
            examId = "tnpsc_grp4",
            dailyHours = DailyAvailability.ONE_HOUR,
            preferredSlot = StudyScheduleSlot.EARLY_MORNING,
            userProgress = userProgress
        )

        assertTrue("Should detect and reschedule missed tasks", reorgResult.rescheduledTasksCount > 0)
        assertTrue(reorgResult.summaryEn.contains("Auto-reorganized"))

        // Check that adjusted tasks include Catch-up Priority
        val hasCatchUpPriority = reorgResult.adjustedTasks.any {
            it.topicTitleEn.contains("Catch-up Priority")
        }
        assertTrue("Missed tasks must be labeled with Catch-up Priority", hasCatchUpPriority)
    }
}
