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
fun AiTeacherScreen(
    viewModel: StudyMasterViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val lesson by viewModel.currentLesson.collectAsState()
    val isLoading by viewModel.isLessonLoading.collectAsState()
    val chatHistory by viewModel.tutorChatHistory.collectAsState()
    val isAnsweringDoubt by viewModel.isAnsweringDoubt.collectAsState()

    var doubtQuery by remember { mutableStateOf("") }
    var showTopicPicker by remember { mutableStateOf(false) }
    var selectedOptionQuiz by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showExplanationQuiz by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var isSpeakingAudio by remember { mutableStateOf(false) }

    val isTamil = profile.language == StudyLanguage.TAMIL
    val isBilingual = profile.language == StudyLanguage.BILINGUAL
    val currentExam = ExamDataProvider.getExamById(profile.targetExamId)

    Scaffold(
        bottomBar = {
            // Interactive Ask AI Doubt Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = doubtQuery,
                        onValueChange = { doubtQuery = it },
                        placeholder = {
                            Text(
                                if (isTamil) "ஆசிரியரிடம் சந்தேகம் கேட்கவும்..."
                                else "Ask AI Teacher any doubt on this topic...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_doubt_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 2,
                        trailingIcon = {
                            if (doubtQuery.isNotEmpty()) {
                                IconButton(onClick = { doubtQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (doubtQuery.isNotBlank()) {
                                viewModel.askTutorDoubt(doubtQuery)
                                doubtQuery = ""
                            }
                        },
                        enabled = !isAnsweringDoubt && doubtQuery.isNotBlank(),
                        modifier = Modifier
                            .background(
                                color = if (doubtQuery.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .testTag("send_doubt_button")
                    ) {
                        if (isAnsweringDoubt) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("ai_teacher_screen_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Topic Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "AI இருமொழி ஆசிரியர்" else "AI Bilingual Teacher",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            // Speech Read Aloud Toggle Button
                            IconButton(
                                onClick = {
                                    if (isSpeakingAudio) {
                                        viewModel.stopSpeaking()
                                        isSpeakingAudio = false
                                    } else {
                                        lesson?.let {
                                            val textToRead = if (isTamil) "${it.topicTitleTa}. ${it.simpleExplanationTa}" else "${it.topicTitleEn}. ${it.simpleExplanationEn}"
                                            viewModel.speakText(textToRead)
                                            isSpeakingAudio = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("audio_tts_button")
                            ) {
                                Icon(
                                    imageVector = if (isSpeakingAudio) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Read Aloud",
                                    tint = if (isSpeakingAudio) MaterialTheme.colorScheme.secondary else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selected Topic Pill with change button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTopicPicker = true }
                                .testTag("select_topic_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isTamil) "தற்போதைய பாடம்:" else "Current Topic:",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = selectedTopic?.let { if (isTamil) it.titleTa else it.titleEn } ?: "Constitution Preamble & Fundamental Rights",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Topic",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Language Mode Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StudyLanguage.values().forEach { lang ->
                                val isSelected = profile.language == lang
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clickable { viewModel.setLanguage(lang) }
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            StudyLanguage.TAMIL -> "தமிழ்"
                                            StudyLanguage.ENGLISH -> "English"
                                            StudyLanguage.BILINGUAL -> "Bilingual"
                                            StudyLanguage.FORMAL_ENGLISH -> "Formal"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isTamil) "AI ஆசிரியர் பாடத்தை தயாரிக்கிறார்..."
                                       else "AI Teacher preparing 10-step lesson...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (lesson != null) {
                val currentLesson = lesson!!

                // 1. SIMPLE EXPLANATION
                item {
                    LessonComponentCard(
                        stepNumber = 1,
                        titleEn = "Simple Explanation",
                        titleTa = "எளிய விளக்கம்",
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    ) {
                        Text(
                            text = if (isTamil) currentLesson.simpleExplanationTa else currentLesson.simpleExplanationEn,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                        if (isBilingual) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentLesson.simpleExplanationTa,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // 2. IMPORTANT DEFINITIONS
                item {
                    LessonComponentCard(
                        stepNumber = 2,
                        titleEn = "Important Definitions",
                        titleTa = "முக்கிய வரையறைகள்",
                        icon = Icons.Default.Spellcheck
                    ) {
                        val defs = if (isTamil) currentLesson.importantDefinitionsTa else currentLesson.importantDefinitionsEn
                        defs.forEach { def ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(def, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // 3. STEP-BY-STEP TEACHING
                item {
                    LessonComponentCard(
                        stepNumber = 3,
                        titleEn = "Step-by-Step Learning",
                        titleTa = "படிபடியான கற்றல்",
                        icon = Icons.Default.FormatListNumbered
                    ) {
                        val steps = if (isTamil) currentLesson.stepStepPointsTa() else currentLesson.stepStepPointsEn()
                        steps.forEachIndexed { i, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${i + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(step, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // 4. REAL-LIFE EXAMPLES
                item {
                    LessonComponentCard(
                        stepNumber = 4,
                        titleEn = "Real-Life Application",
                        titleTa = "நடைமுறை பயன்பாடுகள்",
                        icon = Icons.Default.Lightbulb
                    ) {
                        Text(
                            text = if (isTamil) currentLesson.realLifeExamplesTa else currentLesson.realLifeExamplesEn,
                            fontSize = 13.sp
                        )
                    }
                }

                // 5. IMPORTANT FACTS & FIGURES
                item {
                    LessonComponentCard(
                        stepNumber = 5,
                        titleEn = "High-Yield Facts & Concepts",
                        titleTa = "முக்கிய தகவல்கள் & கருத்துகள்",
                        icon = Icons.Default.Star
                    ) {
                        val facts = if (isTamil) currentLesson.importantFactsTa else currentLesson.importantFactsEn
                        facts.forEach { f ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(f, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // 6. EXAM-ORIENTED NOTES
                item {
                    LessonComponentCard(
                        stepNumber = 6,
                        titleEn = "Exam-Oriented Notes",
                        titleTa = "தேர்வுக்கான முக்கிய குறிப்புகள்",
                        icon = Icons.Default.NoteAlt
                    ) {
                        Text(
                            text = if (isTamil) currentLesson.examOrientedNotesTa else currentLesson.examOrientedNotesEn,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // 7. FREQUENTLY TESTED CONCEPTS & WEIGHTAGE
                item {
                    LessonComponentCard(
                        stepNumber = 7,
                        titleEn = "Frequently Tested Patterns",
                        titleTa = "அடிக்கடி கேட்கப்படும் பகுதிகள்",
                        icon = Icons.AutoMirrored.Filled.TrendingUp
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isTamil) currentLesson.frequentlyTestedConceptsTa else currentLesson.frequentlyTestedConceptsEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // 8 & 9. PRACTICE QUESTIONS & EXPLANATIONS
                item {
                    LessonComponentCard(
                        stepNumber = 8,
                        titleEn = "Practice Questions & Scoring",
                        titleTa = "பயிற்சி வினாக்கள் & விடைகள்",
                        icon = Icons.Default.Quiz
                    ) {
                        currentLesson.practiceQuestions.forEachIndexed { qIdx, pq ->
                            val userChoice = selectedOptionQuiz[pq.id]
                            val isRevealed = showExplanationQuiz[pq.id] ?: false

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Q${qIdx + 1}: " + (if (isTamil) pq.questionTa else pq.questionEn),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val options = if (isTamil) pq.optionsTa else pq.optionsEn
                                    options.forEachIndexed { oIdx, opt ->
                                        val isChosen = userChoice == oIdx
                                        val isCorrect = oIdx == pq.correctIndex
                                        val bgColor = when {
                                            isRevealed && isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                            isRevealed && isChosen && !isCorrect -> Color(0xFFC62828).copy(alpha = 0.2f)
                                            isChosen -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = bgColor,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    selectedOptionQuiz = selectedOptionQuiz + (pq.id to oIdx)
                                                    showExplanationQuiz = showExplanationQuiz + (pq.id to true)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isChosen,
                                                    onClick = {
                                                        selectedOptionQuiz = selectedOptionQuiz + (pq.id to oIdx)
                                                        showExplanationQuiz = showExplanationQuiz + (pq.id to true)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(opt, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    if (isRevealed) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = if (isTamil) "விளக்கம்:" else "Explanation:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = if (isTamil) pq.explanationTa else pq.explanationEn,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 10. 60-SECOND REVISION SUMMARY
                item {
                    LessonComponentCard(
                        stepNumber = 10,
                        titleEn = "60-Second Flash Summary",
                        titleTa = "60-வினாடி மின்னல் நினைவூட்டல்",
                        icon = Icons.Default.Bolt
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isTamil) currentLesson.sixtySecondSummaryTa else currentLesson.sixtySecondSummaryEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Chat History Section with Doubt Responses
            if (chatHistory.isNotEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "சந்தேகங்கள் & ஆசிரியர் விடைகள்" else "Doubts & Teacher Answers",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(chatHistory) { (sender, text) ->
                    val isStudent = sender == "Student"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isStudent) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isStudent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = sender,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStudent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Bottom space for the bottom bar
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Topic Selection Dialog
    if (showTopicPicker) {
        AlertDialog(
            onDismissRequest = { showTopicPicker = false },
            title = {
                Text(
                    text = if (isTamil) "பாடத்திட்டத்திலிருந்து தலைப்பைத் தேர்வு செய்க"
                           else "Select Topic from Syllabus",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    currentExam.subjects.forEach { subject ->
                        item {
                            Text(
                                text = if (isTamil) subject.titleTa else subject.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        subject.chapters.forEach { chapter ->
                            chapter.topics.forEach { topic ->
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedTopic?.id == topic.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectTopic(topic, subject.titleEn)
                                                showTopicPicker = false
                                            }
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isTamil) topic.titleTa else topic.titleEn,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopicPicker = false }) {
                    Text(if (isTamil) "மூடு" else "Close")
                }
            }
        )
    }
}

// Helper methods on AiLessonResponse
private fun AiLessonResponse.stepStepPointsTa(): List<String> = this.stepByStepPointsTa
private fun AiLessonResponse.stepStepPointsEn(): List<String> = this.stepByStepPointsEn

@Composable
fun LessonComponentCard(
    stepNumber: Int,
    titleEn: String,
    titleTa: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$stepNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$titleEn ($titleTa)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
