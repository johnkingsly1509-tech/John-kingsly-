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

@Composable
fun SyllabusScreen(
    viewModel: StudyMasterViewModel,
    onNavigateToAiTeacher: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val allExams = remember { ExamDataProvider.getAllExams() }
    var selectedExamId by remember { mutableStateOf(profile.targetExamId) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Syllabus Tracker, 1 = Verified PYQ Bank, 2 = Official Notification & Pattern

    val isTamil = profile.language == StudyLanguage.TAMIL
    val isBilingual = profile.language == StudyLanguage.BILINGUAL
    val activeExam = ExamDataProvider.getExamById(selectedExamId)
    val verifiedPyqs = remember(selectedExamId) { ExamDataProvider.getVerifiedPyqs(selectedExamId) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("syllabus_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exam Selection Chip Row
        item {
            Text(
                text = if (isTamil) "தேர்வு பாடத்திட்டம் & முந்தைய வினாக்கள்" else "Exam Syllabus & Verified Past Papers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allExams) { exam ->
                    val isSelected = exam.id == selectedExamId
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedExamId = exam.id
                            if (profile.targetExamId != exam.id) {
                                viewModel.setTargetExam(exam.id)
                            }
                        },
                        label = {
                            Text(
                                text = if (isTamil) exam.nameTa else exam.nameEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }
        }

        // Sub Tabs: Syllabus Tracker | PYQ Bank | Pattern & Notifications
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isTamil) "பாடத்திட்டம்" else "Syllabus Tracker", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isTamil) "முந்தைய வினாக்கள்" else "Verified PYQs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (isTamil) "அறிவிப்பு & விதி" else "Pattern & Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // SYLLABUS TRACKER
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "பாடத்திட்ட நிறைவு விகிதம்" else "Syllabus Coverage Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "35% Complete",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.35f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Subject / Chapter / Topics
            activeExam.subjects.forEach { subject ->
                item {
                    Text(
                        text = (if (isTamil) subject.titleTa else subject.titleEn) + " (${subject.weightagePercentage}% Weightage)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                subject.chapters.forEach { chapter ->
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isTamil) chapter.titleTa else chapter.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                chapter.topics.forEach { topic ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = when (topic.status) {
                                                    TopicStatus.COMPLETED -> Color(0xFF2E7D32)
                                                    TopicStatus.LEARNING -> MaterialTheme.colorScheme.secondary
                                                    TopicStatus.NEEDS_REVISION -> Color(0xFFE91E63)
                                                    TopicStatus.NOT_STARTED -> Color.Gray.copy(alpha = 0.4f)
                                                },
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (isTamil) topic.titleTa else topic.titleEn,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${topic.highYieldWeightage} Yield • ${topic.status.name.replace("_", " ")}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = {
                                                viewModel.selectTopic(topic, subject.titleEn)
                                                onNavigateToAiTeacher()
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (isTamil) "கற்றல்" else "Learn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // VERIFIED PREVIOUS YEAR QUESTIONS BANK
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "அங்கீகரிக்கப்பட்ட முந்தைய ஆண்டு வினாக்கள் மட்டுமே. AI கற்பனை வினாக்கள் அல்ல."
                                   else "Verified official past year question papers only. Clearly distinguished from simulated AI items.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            items(verifiedPyqs) { pyq ->
                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = pyq.examNameTag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(pyq.topicName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isTamil) pyq.questionTa else pyq.questionEn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val options = if (isTamil) pyq.optionsTa else pyq.optionsEn
                        options.forEachIndexed { idx, opt ->
                            val isCorrect = idx == pyq.correctIndex
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isExpanded && isCorrect) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${('A' + idx)}. $opt",
                                        fontSize = 12.sp,
                                        fontWeight = if (isExpanded && isCorrect) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isExpanded && isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) (if (isTamil) "விளக்கத்தை மறை" else "Hide Solution")
                                       else (if (isTamil) "சரியான விடை & விளக்கம் பார்க்க" else "Show Verified Solution"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isTamil) "அதிகாரப்பூர்வ விடை விளக்கம்:" else "Official Source Solution:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isTamil) pyq.explanationTa else pyq.explanationEn,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PATTERN, ELIGIBILITY & RULES
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "அதிகாரப்பூர்வ தேர்வு முறை & விதிகள்" else "Official Pattern & Regulations",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Examination / தேர்வு:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (isTamil) activeExam.nameTa else activeExam.nameEn, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Official Status / நிலை:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = activeExam.notificationStatus, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Exam Pattern / தேர்வு கட்டமைப்பு:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (isTamil) activeExam.patternTa else activeExam.patternEn, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Negative Marking / தவறான விடைகளுக்கான விதி:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = activeExam.negativeMarkingRatio, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (activeExam.hasNegativeMarking) Color(0xFFC62828) else Color(0xFF2E7D32))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Total Marks / மொத்த மதிப்பெண்கள்:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${activeExam.totalMarks} Marks", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
