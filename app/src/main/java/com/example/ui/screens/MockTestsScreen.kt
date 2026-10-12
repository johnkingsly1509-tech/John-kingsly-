package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

@Composable
fun MockTestsScreen(
    viewModel: StudyMasterViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val activeTest by viewModel.activeMockTest.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val testTimerSeconds by viewModel.testTimerSeconds.collectAsState()
    val lastResult by viewModel.lastAttemptResult.collectAsState()
    val pastAttempts by viewModel.mockAttempts.collectAsState()

    val isTamil = profile.language == StudyLanguage.TAMIL
    val mockTests = remember(profile.targetExamId) { ExamDataProvider.getMockTests(profile.targetExamId) }

    var currentQuestionIndex by remember { mutableStateOf(0) }

    if (activeTest != null) {
        // ACTIVE TEST RUNNER
        val test = activeTest!!
        val question = test.questions.getOrNull(currentQuestionIndex) ?: test.questions.first()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("active_test_runner_screen")
        ) {
            // Header: Test Title & Countdown Timer
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) test.titleTa else test.titleEn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of ${test.questions.size}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Timer Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFC62828)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val m = testTimerSeconds / 60
                            val s = testTimerSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", m, s),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Navigation Palette
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(test.questions.size) { idx ->
                    val isCurrent = idx == currentQuestionIndex
                    val isAnswered = userAnswers.containsKey(idx)
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { currentQuestionIndex = idx }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = question.subjectName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) question.questionTa else question.questionEn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val options = if (isTamil) question.optionsTa else question.optionsEn
                        val chosenIndex = userAnswers[currentQuestionIndex]

                        options.forEachIndexed { oIdx, opt ->
                            val isSelected = chosenIndex == oIdx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.selectAnswerForQuestion(currentQuestionIndex, oIdx) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectAnswerForQuestion(currentQuestionIndex, oIdx) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${('A' + oIdx)}. $opt",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation and Submit Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                    enabled = currentQuestionIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTamil) "முந்தையது" else "Prev")
                }

                Button(
                    onClick = { viewModel.submitMockTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("submit_mock_test_button")
                ) {
                    Text(if (isTamil) "சமர்ப்பி" else "Submit Test", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (currentQuestionIndex < test.questions.size - 1) {
                            currentQuestionIndex++
                        }
                    },
                    enabled = currentQuestionIndex < test.questions.size - 1
                ) {
                    Text(if (isTamil) "அடுத்தது" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    } else {
        // MOCK TESTS DASHBOARD & PAST RESULTS
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("mock_tests_list_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (isTamil) "மாதிரித் தேர்வுகள் & செயல்திறன் பகுப்பாய்வு"
                           else "Mock Examinations & Performance Analytics",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // If there is a last result, show the detailed report card
            lastResult?.let { result ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTamil) "தேர்வு மதிப்பீடு அறிக்கை" else "Test Performance Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${result.scorePercentage.toInt()}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (result.scorePercentage >= 60f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ResultMetricItem(label = "Correct", value = "${result.correctAnswers}", color = Color(0xFF2E7D32))
                                ResultMetricItem(label = "Wrong", value = "${result.wrongAnswers}", color = Color(0xFFC62828))
                                ResultMetricItem(label = "Skipped", value = "${result.skippedAnswers}", color = Color.Gray)
                                ResultMetricItem(label = "Time", value = "${result.timeSpentSeconds / 60}m", color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isTamil) "வெளிப்படையான தயாரிப்பு மதிப்பீடு:"
                                               else "Transparent Readiness Assessment:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (result.scorePercentage >= 70f)
                                            (if (isTamil) "முன்னேற்றம் நன்று: நீங்கள் அடிப்படை மற்றும் முக்கிய கேள்விகளில் வலுவாக உள்ளீர்கள். அடுத்ததாக கடின வினாக்களை பயிற்சி செய்யவும்."
                                             else "Strong Performance: Foundational concepts solid. Recommend attempting full length past papers.")
                                        else
                                            (if (isTamil) "மீள்பார்வை தேவை: தவறான வினாக்களுக்கான காரணங்களை ஆராய்ந்து Smart Notes-ல் குறிப்புகளைச் சேர்க்கவும்."
                                             else "Revision Required: Review missed concepts in Mistake Notebook and re-read core definitions."),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isTamil) "கிடைக்கக்கூடிய மாதிரித் தேர்வுகள்" else "Available Timed Tests",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(mockTests) { test ->
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
                            Text(
                                text = if (isTamil) test.titleTa else test.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${test.durationMinutes} mins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${test.questions.size} Questions • Official Pattern Verified",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.startMockTest(test) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_test_${test.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "தேர்வைத் தொடங்கு" else "Start Timed Test", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Past Attempts History
            if (pastAttempts.isNotEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "முந்தைய தேர்வு முடிவுகள்" else "Past Attempt History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(pastAttempts) { attempt ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (isTamil) attempt.testTitleTa else attempt.testTitleEn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${attempt.correctAnswers}/${attempt.totalQuestions} Correct • ${attempt.timeSpentSeconds / 60}m spent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${attempt.scorePercentage.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (attempt.scorePercentage >= 60f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
