package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: StudyMasterViewModel,
    onComplete: () -> Unit
) {
    val currentProfile by viewModel.userProfile.collectAsState()
    val allExams = remember { ExamDataProvider.getAllExams() }

    var step by remember { mutableStateOf(1) } // 1: Exam & Language, 2: Routine & Availability, 3: Prep Level & Topics

    var selectedExamId by remember { mutableStateOf(currentProfile.targetExamId) }
    var selectedLanguage by remember { mutableStateOf(currentProfile.language) }
    var selectedDailyHours by remember { mutableStateOf(currentProfile.dailyHours) }
    var selectedSlot by remember { mutableStateOf(currentProfile.scheduleSlot) }
    var selectedLevel by remember { mutableStateOf(currentProfile.prepLevel) }
    var selectedStatus by remember { mutableStateOf(currentProfile.studentStatus) }
    var studentName by remember { mutableStateOf(currentProfile.name) }
    var weakTopics by remember { mutableStateOf(currentProfile.weakTopics) }
    var strongTopics by remember { mutableStateOf(currentProfile.strongTopics) }

    val isTamil = selectedLanguage == StudyLanguage.TAMIL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isTamil) "தனிப்பயனாக்கப்பட்ட படிப்பு வடிவமைப்பு" else "Personalized Study Setup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of 3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                viewModel.saveFullProfile(
                                    name = studentName.ifBlank { "Aspirant" },
                                    targetExamId = selectedExamId,
                                    targetYear = 2026,
                                    targetDate = "2026-11-20",
                                    language = selectedLanguage,
                                    prepLevel = selectedLevel,
                                    dailyHours = selectedDailyHours,
                                    scheduleSlot = selectedSlot,
                                    studentStatus = selectedStatus,
                                    weakTopics = weakTopics,
                                    strongTopics = strongTopics
                                )
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (step == 3) (if (isTamil) "தினசரி வரைபடத்தைத் தொடங்கு" else "Generate Study Map")
                                   else (if (isTamil) "அடுத்தது" else "Next Step"),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("onboarding_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                1 -> {
                    // STEP 1: EXAM SELECTION & LANGUAGE
                    item {
                        Text(
                            text = if (isTamil) "1. உங்கள் இலக்குத் தேர்வைத் தேர்வு செய்க" else "1. Select Target Examination",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allExams) { exam ->
                        val isSelected = selectedExamId == exam.id
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExamId = exam.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedExamId = exam.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isTamil) exam.nameTa else exam.nameEn,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${exam.category} • ${exam.notificationStatus}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTamil) "கற்றல் மொழி விருப்பம்" else "Preferred Learning Language",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(StudyLanguage.values().toList()) { lang ->
                        val isSelected = selectedLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = lang }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedLanguage = lang }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(lang.displayNameTa, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(lang.displayNameEn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: AVAILABILITY & DAILY SCHEDULE
                    item {
                        Text(
                            text = if (isTamil) "2. தினசரி படிப்பு நேரம் & அட்டவணை" else "2. Daily Availability & Timings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTamil) "உங்களிடம் உள்ள நேரத்திற்கு ஏற்ப படிப்பு வரைபடம் துல்லியமாக உருவாகும்."
                                   else "The daily study map will adapt realistic tasks tailored to your hours.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(DailyAvailability.values().toList()) { avail ->
                        val isSelected = selectedDailyHours == avail
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDailyHours = avail }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedDailyHours = avail }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(if (isTamil) avail.labelTa else avail.labelEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${avail.hours} Hours Daily Roadmap", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTamil) "விருப்பமான படிப்பு நேரம் (Preferred Slot)" else "Preferred Daily Study Slot",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(StudyScheduleSlot.values().toList()) { slot ->
                        val isSelected = selectedSlot == slot
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSlot = slot }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedSlot = slot })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTamil) slot.labelTa else slot.labelEn, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTamil) "மாணவர் / பணி நிலை" else "Student Academic Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(StudentStatus.values().toList()) { stat ->
                        val isSelected = selectedStatus == stat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = stat }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedStatus = stat })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTamil) stat.labelTa else stat.labelEn, fontSize = 12.sp)
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: PREP LEVEL & TOPICS
                    item {
                        Text(
                            text = if (isTamil) "3. தற்போதைய தயாரிப்பு நிலை & தலைப்புகள்" else "3. Current Level & Topic Focus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text(if (isTamil) "உங்கள் பெயர்" else "Your Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = if (isTamil) "தயாரிப்பு நிலை" else "Current Preparation Level",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(PrepLevel.values().toList()) { lvl ->
                        val isSelected = selectedLevel == lvl
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLevel = lvl }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedLevel = lvl })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isTamil) lvl.titleTa else lvl.titleEn, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = weakTopics,
                            onValueChange = { weakTopics = it },
                            label = { Text(if (isTamil) "கடினமான / பலவீனமான தலைப்புகள்" else "Weak Topics (Needs more focus)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = strongTopics,
                            onValueChange = { strongTopics = it },
                            label = { Text(if (isTamil) "வலுவான / எளிதான தலைப்புகள்" else "Strong Topics (Quick revision)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
