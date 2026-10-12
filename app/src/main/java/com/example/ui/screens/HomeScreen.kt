package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import com.example.ui.viewmodel.StudyMasterViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: StudyMasterViewModel,
    onNavigateToAiTeacher: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToMockTests: () -> Unit,
    onNavigateToRevision: () -> Unit,
    onNavigateToSmartNotes: () -> Unit,
    onOpenExamSelector: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val upcomingTasks by viewModel.allUpcomingTasks.collectAsState()
    val activeTimerTaskId by viewModel.activeTimerTaskId.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val mockAttempts by viewModel.mockAttempts.collectAsState()
    val aiPlanStrategy by viewModel.aiPlanStrategy.collectAsState()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsState()
    val reorganizeNotice by viewModel.lastReorganizeNotice.collectAsState()

    var selectedMapTab by remember { mutableStateOf(0) } // 0 = Today, 1 = Next 7 Days, 2 = Current Month (30 Days), 3 = 5-Phase Roadmap
    var selectedMonthDayOffset by remember { mutableStateOf(0) } // selected day in month tab (0..29)
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showReorganizeDialog by remember { mutableStateOf(false) }

    val isTamil = profile.language == StudyLanguage.TAMIL
    val isBilingual = profile.language == StudyLanguage.BILINGUAL
    val currentExam = ExamDataProvider.getExamById(profile.targetExamId)
    val latestMockAccuracy = mockAttempts.firstOrNull()?.scorePercentage

    val completedCount = todayTasks.count { it.isCompleted }
    val totalCount = todayTasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Student Status & Streak Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isTamil) "வணக்கம், ${profile.name}!" else "Hello, ${profile.name}!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isTamil) "இன்றைய படிப்பு இலக்கு: ${profile.dailyHours.hours} மணி நேரம்"
                                       else "Daily Target: ${profile.dailyHours.hours} Hours / Day",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.streakDays} Days",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Exam Milestone Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isTamil) currentExam.nameTa else currentExam.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isTamil) "எதிர்பார்க்கப்படும் தேதி: ${currentExam.expectedDate}" else "Target Date: ${currentExam.expectedDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            TextButton(
                                onClick = onOpenExamSelector,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (isTamil) "மாற்று" else "Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Reorganize / Missed Day Notice Banner
        reorganizeNotice?.let { notice ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = notice, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        }
                        IconButton(onClick = { viewModel.clearReorganizeNotice() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 2. THE DEFINING FEATURE: PERSONALIZED DAILY STUDY MAP
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_study_map_container")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTamil) "AI தினசரி கற்றல் வரைபடம்"
                                           else "AI Daily Study Map Engine",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (isTamil) "பலவீன பகுதிகள் & தேர்வு முடிவுகள் அடிப்படையில் வடிவமைக்கப்பட்டது"
                                       else "Tailored to weak areas, mock scores & study hours",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // AI Plan Generator & Reorganize Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { showAiGeneratorDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("ai_study_plan_generator_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTamil) "AI திட்டம்" else "AI Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { showReorganizeDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("reorganize_plan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Reorganize Missed Days",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "இன்றைய முன்னேற்றம்: $completedCount/$totalCount முடிந்தது"
                                   else "Today's Completion: $completedCount of $totalCount Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Map View Switcher Tabs: Today | Next 7 Days | Month (30 Days) | Roadmap
                    ScrollableTabRow(
                        selectedTabIndex = selectedMapTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        edgePadding = 6.dp
                    ) {
                        Tab(
                            selected = selectedMapTab == 0,
                            onClick = { selectedMapTab = 0 },
                            text = { Text(if (isTamil) "இன்று" else "Today", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedMapTab == 1,
                            onClick = { selectedMapTab = 1 },
                            text = { Text(if (isTamil) "7 நாட்கள்" else "Next 7 Days", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedMapTab == 2,
                            onClick = { selectedMapTab = 2 },
                            text = { Text(if (isTamil) "மாதம் (30 நாட்கள்)" else "Current Month (30d)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedMapTab == 3,
                            onClick = { selectedMapTab = 3 },
                            text = { Text(if (isTamil) "முழு திட்டம்" else "Roadmap", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // 3. TAB CONTENT
        if (selectedMapTab == 0) {
            // Today's Study Tasks
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "இன்றைய படிப்பு அட்டவணை" else "Today's Sequential Schedule",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (todayTasks.isEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "இன்றைய பணிகள் தயாராகிறது..." else "Generating today's customized tasks...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(todayTasks) { task ->
                    DailyTaskCard(
                        task = task,
                        isTamil = isTamil,
                        isBilingual = isBilingual,
                        isTimerActive = activeTimerTaskId == task.id,
                        timerSeconds = timerSeconds,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onStartTimer = { viewModel.startTaskTimer(task.id) },
                        onStopTimer = { viewModel.stopTaskTimer() },
                        onLearnNowClick = {
                            val topic = ExamTopic(task.id, task.topicTitleEn, task.topicTitleTa, "High")
                            viewModel.selectTopic(topic, task.subjectNameEn)
                            onNavigateToAiTeacher()
                        }
                    )
                }
            }
        } else if (selectedMapTab == 1) {
            // Next 7 Days Forecast
            item {
                Text(
                    text = if (isTamil) "அடுத்த 7 நாட்களுக்கான தயாரிப்பு திட்டம்" else "Next 7 Days Preparation Forecast",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            for (offset in 1..7) {
                val dayTasks = upcomingTasks.filter { it.dayOffset == offset }
                if (dayTasks.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, offset)
                                val dateFmt = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(cal.time)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFmt,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${dayTasks.sumOf { it.estimatedMinutes }} mins",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                dayTasks.forEach { t ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isTamil) t.topicTitleTa else t.topicTitleEn,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${t.activityType.labelEn} • ${t.estimatedMinutes}m",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedMapTab == 2) {
            // CURRENT MONTH (30 DAYS INTERACTIVE SCHEDULE)
            item {
                Column {
                    Text(
                        text = if (isTamil) "நடப்பு மாத முழு படிப்புத் திட்டம் (30 நாட்கள்)" else "Current Month Master Study Schedule (30 Days)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTamil) "தேதியைத் தேர்ந்தெடுத்து அன்றைய பாடங்களைக் காண்க" else "Select any day to inspect topics and time allocation",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Horizontal 30-Day Calendar Strip
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(30) { dayIdx ->
                        val isSelected = selectedMonthDayOffset == dayIdx
                        val isWeeklyTest = (dayIdx % 7 == 6)
                        val isMilestone = (dayIdx == 13 || dayIdx == 27)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isMilestone -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                isWeeklyTest -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .clickable { selectedMonthDayOffset = dayIdx }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (dayIdx == 0) "Today" else "Day ${dayIdx + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        isMilestone -> "Mock"
                                        isWeeklyTest -> "Review"
                                        else -> "Core"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Tasks for selected day in month
            val monthDayTasks = upcomingTasks.filter { it.dayOffset == selectedMonthDayOffset }
            item {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, selectedMonthDayOffset)
                val selDateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(cal.time)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Schedule for: $selDateFmt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${monthDayTasks.sumOf { it.estimatedMinutes }} mins total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (monthDayTasks.isEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "இந்த நாளுக்கான பணிகள் தயாராகிறது..." else "Generating day plan...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(monthDayTasks) { task ->
                    DailyTaskCard(
                        task = task,
                        isTamil = isTamil,
                        isBilingual = isBilingual,
                        isTimerActive = activeTimerTaskId == task.id,
                        timerSeconds = timerSeconds,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onStartTimer = { viewModel.startTaskTimer(task.id) },
                        onStopTimer = { viewModel.stopTaskTimer() },
                        onLearnNowClick = {
                            val topic = ExamTopic(task.id, task.topicTitleEn, task.topicTitleTa, "High")
                            viewModel.selectTopic(topic, task.subjectNameEn)
                            onNavigateToAiTeacher()
                        }
                    )
                }
            }
        } else {
            // 5-Phase Exam Preparation Roadmap
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTamil) "5-படிநிலை அதிகாரப்பூர்வ தயாரிப்பு வரைபடம்"
                                   else "5-Phase Comprehensive Preparation Roadmap",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val phases = listOf(
                            Triple("Phase 1: Syllabus Mastery & Baseline Assessment", "பாடத்திட்டத்தை அறிதல் மற்றும் தொடக்க நிலை மதிப்பீடு", "Active (Week 1-3)"),
                            Triple("Phase 2: Core Subject Knowledge & Deep Theory", "முதன்மை பாடங்களின் கோட்பாடுகள் மற்றும் ஆழமான கற்றல்", "Planned (Week 4-12)"),
                            Triple("Phase 3: Topic-wise PYQs & Timed Practice", "தலைப்பு வாரியாக முந்தைய ஆண்டு வினாக்கள் பயிற்சி", "Planned (Week 13-18)"),
                            Triple("Phase 4: Full-Length Mocks & Mistake Rectification", "முழு மாதிரித் தேர்வுகள் மற்றும் தவறுகளை திருத்துதல்", "Planned (Week 19-24)"),
                            Triple("Phase 5: High-Speed Spaced Revision & Final Polish", "இறுதி மின்னல் வேக மீள்பார்வை & தேர்வு நாள் வியூகம்", "Final 30 Days")
                        )

                        phases.forEachIndexed { idx, (en, ta, status) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (idx == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (idx == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isTamil) ta else en,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        color = if (idx == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (idx < phases.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(start = 34.dp))
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Action Learning Cards
        item {
            Text(
                text = if (isTamil) "முக்கிய கற்றல் கருவிகள்" else "Core Learning Modules",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickModuleCard(
                    title = if (isTamil) "AI ஆசிரியர்" else "AI Teacher",
                    subtitle = if (isTamil) "10-படிநிலை எளிய விளக்கம்" else "10-Step Tutor",
                    icon = Icons.Default.Psychology,
                    badgeColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAiTeacher
                )

                QuickModuleCard(
                    title = if (isTamil) "மாதிரித் தேர்வு" else "Mock Tests",
                    subtitle = if (isTamil) "நேரக்கட்டுப்பாடு தேர்வு" else "Timed Exam Simulation",
                    icon = Icons.Default.Quiz,
                    badgeColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMockTests
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickModuleCard(
                    title = if (isTamil) "ஃபிளாஷ்கார்டு" else "Flashcards",
                    subtitle = if (isTamil) "நினைவுபடுத்தல் முறை" else "Spaced Repetition",
                    icon = Icons.Default.FlipCameraAndroid,
                    badgeColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRevision
                )

                QuickModuleCard(
                    title = if (isTamil) "ஸ்மார்ட் நோட்ஸ்" else "Smart Notes",
                    subtitle = if (isTamil) "ஆவண சுருக்கம் & குறிப்புகள்" else "Doc & Text Summary",
                    icon = Icons.Default.NoteAlt,
                    badgeColor = Color(0xFF673AB7),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSmartNotes
                )
            }
        }
    }

    // AI Study Plan Generator Modal Dialog
    if (showAiGeneratorDialog) {
        var editableWeakTopics by remember { mutableStateOf(profile.weakTopics) }

        AlertDialog(
            onDismissRequest = { showAiGeneratorDialog = false },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text(
                    text = if (isTamil) "AI படிப்புத் திட்ட உருவாக்கி" else "AI Study Plan Generator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = if (isTamil) "உங்கள் பலவீன பகுதிகள், கிடைக்கும் நேரம் மற்றும் தேர்வு முடிவுகள் அடிப்படையில் 30-நாள் அட்டவணை உருவாகும்."
                                   else "Generates a personalized 30-day dynamic timetable considering syllabus weightage, study hours, and weak areas.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Key Engine Inputs
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("• Target Exam: ${currentExam.nameEn}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Daily Availability: ${profile.dailyHours.hours} Hours / Day", fontSize = 11.sp)
                                Text("• Practice Accuracy: ${if (latestMockAccuracy != null) "${latestMockAccuracy.toInt()}%" else "Baseline Assessment"}", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editableWeakTopics,
                            onValueChange = { editableWeakTopics = it },
                            label = { Text(if (isTamil) "பலவீன பகுதிகள் (Weak Topics)" else "Identified Weak Topics") },
                            placeholder = { Text("e.g. Mental Aptitude, Economy") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.regenerateStudyPlanWithAi(editableWeakTopics) },
                            enabled = !isGeneratingPlan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_ai_study_plan_generation")
                        ) {
                            if (isGeneratingPlan) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isTamil) "திட்டம் தயாராகிறது..." else "Generating with AI...")
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTamil) "30-நாள் அட்டவணையை உருவாக்குக" else "Generate 30-Day Plan with AI", fontWeight = FontWeight.Bold)
                            }
                        }

                        // AI Strategy Blueprint Result
                        aiPlanStrategy?.let { strategy ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isTamil) "AI ஆசிரியரின் உத்தி & வழிகாட்டுதல்:" else "AI Teacher's Strategic Blueprint:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = strategy, fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiGeneratorDialog = false }) {
                    Text(if (isTamil) "முடிந்தது" else "Done")
                }
            }
        )
    }

    // Reorganize Missed Days Confirmation Dialog
    if (showReorganizeDialog) {
        AlertDialog(
            onDismissRequest = { showReorganizeDialog = false },
            icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
            title = {
                Text(
                    text = if (isTamil) "தவறிய நாட்களை மறுசீரமைக்கவா?" else "Missed a Day? Auto-Adjust Plan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isTamil)
                        "உங்கள் முந்தைய தவறிய தலைப்புகள் தினசரி சுமையை அதிகரிக்காமல் அடுத்த நாட்களுக்கு சமமாக மறுபகிர்வு செய்யப்படும்."
                    else
                        "The AI Study Plan Engine will redistribute your uncompleted tasks across upcoming days without overloading your daily hours."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reorganizeMissedDays()
                        showReorganizeDialog = false
                    }
                ) {
                    Text(if (isTamil) "மறுசீரமை" else "Reorganize Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReorganizeDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun DailyTaskCard(
    task: DailyTask,
    isTamil: Boolean,
    isBilingual: Boolean,
    isTimerActive: Boolean,
    timerSeconds: Int,
    onToggleComplete: () -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onLearnNowClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Time Slot & Activity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.timeSlot,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (task.activityType) {
                        TaskActivityType.REVISE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        TaskActivityType.LEARN_NEW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        TaskActivityType.PRACTICE_MCQ -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        TaskActivityType.MISTAKE_REVIEW -> Color(0xFFE91E63).copy(alpha = 0.15f)
                        TaskActivityType.ANSWER_WRITING -> Color(0xFF673AB7).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = if (isTamil) task.activityType.labelTa else task.activityType.labelEn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.activityType) {
                            TaskActivityType.REVISE -> MaterialTheme.colorScheme.tertiary
                            TaskActivityType.LEARN_NEW -> MaterialTheme.colorScheme.primary
                            TaskActivityType.PRACTICE_MCQ -> MaterialTheme.colorScheme.secondary
                            TaskActivityType.MISTAKE_REVIEW -> Color(0xFFE91E63)
                            TaskActivityType.ANSWER_WRITING -> Color(0xFF673AB7)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject and Topic Titles
            Text(
                text = if (isTamil) task.subjectNameTa else task.subjectNameEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isTamil) task.topicTitleTa else task.topicTitleEn,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isBilingual) {
                Text(
                    text = task.topicTitleTa,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guidance Text
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTamil) task.studyMaterialGuidanceTa else task.studyMaterialGuidanceEn,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Timer, Checkbox, and "Study Topic" Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Stopwatch / Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTimerActive) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { onStopTimer() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PauseCircle,
                                    contentDescription = "Stop Timer",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val mins = timerSeconds / 60
                                val secs = timerSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onStartTimer,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${task.estimatedMinutes}m", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onLearnNowClick,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (isTamil) "கற்றல்" else "Study", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Checkbox to mark complete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleComplete() }
                ) {
                    Text(
                        text = if (task.isCompleted) (if (isTamil) "முடிந்தது" else "Completed") else (if (isTamil) "முடிக்கவும்" else "Mark Done"),
                        fontSize = 11.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )
                }
            }
        }
    }
}

@Composable
fun QuickModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}
