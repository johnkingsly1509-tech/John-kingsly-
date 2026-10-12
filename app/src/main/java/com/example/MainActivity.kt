package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyLanguage
import com.example.data.provider.ExamDataProvider
import com.example.ui.components.StudyMasterTopBar
import com.example.ui.screens.*
import com.example.ui.theme.StudyMasterTheme
import com.example.ui.viewmodel.StudyMasterViewModel

enum class MainNavTab(val labelEn: String, val labelTa: String, val icon: ImageVector) {
    HOME("Daily Map", "தினசரி வரைபடம்", Icons.Default.Map),
    AI_TEACHER("AI Teacher", "AI ஆசிரியர்", Icons.Default.Psychology),
    SYLLABUS("Syllabus & PYQ", "பாடத்திட்டம்", Icons.AutoMirrored.Filled.MenuBook),
    TESTS("Mock Tests", "மாதிரித் தேர்வு", Icons.Default.Quiz),
    REVISION("Revision & Notes", "மீள்பார்வை", Icons.Default.FlipCameraAndroid)
}

class MainActivity : ComponentActivity() {
    private val viewModel: StudyMasterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyMasterTheme {
                val profile by viewModel.userProfile.collectAsState()
                val isAdminMode by viewModel.isAdminMode.collectAsState()

                var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
                var showExamSelectorDialog by remember { mutableStateOf(false) }
                var showOnboardingModal by remember { mutableStateOf(false) }

                val isTamil = profile.language == StudyLanguage.TAMIL

                if (showOnboardingModal) {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onComplete = { showOnboardingModal = false }
                    )
                } else if (isAdminMode) {
                    AdminScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.toggleAdminMode() }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("main_scaffold"),
                        topBar = {
                            StudyMasterTopBar(
                                profile = profile,
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onExamChangeClick = { showExamSelectorDialog = true },
                                onAdminToggleClick = { viewModel.toggleAdminMode() },
                                isAdminMode = isAdminMode
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("main_bottom_nav_bar")
                            ) {
                                MainNavTab.values().forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.labelEn,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (isTamil) tab.labelTa else tab.labelEn,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainNavTab.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToAiTeacher = { currentTab = MainNavTab.AI_TEACHER },
                                    onNavigateToSyllabus = { currentTab = MainNavTab.SYLLABUS },
                                    onNavigateToMockTests = { currentTab = MainNavTab.TESTS },
                                    onNavigateToRevision = { currentTab = MainNavTab.REVISION },
                                    onNavigateToSmartNotes = { currentTab = MainNavTab.REVISION },
                                    onOpenExamSelector = { showExamSelectorDialog = true }
                                )
                                MainNavTab.AI_TEACHER -> AiTeacherScreen(viewModel = viewModel)
                                MainNavTab.SYLLABUS -> SyllabusScreen(
                                    viewModel = viewModel,
                                    onNavigateToAiTeacher = { currentTab = MainNavTab.AI_TEACHER }
                                )
                                MainNavTab.TESTS -> MockTestsScreen(viewModel = viewModel)
                                MainNavTab.REVISION -> RevisionTabContainer(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Switch Target Exam Dialog
                if (showExamSelectorDialog) {
                    val allExams = remember { ExamDataProvider.getAllExams() }
                    AlertDialog(
                        onDismissRequest = { showExamSelectorDialog = false },
                        title = {
                            Text(
                                text = if (isTamil) "இலக்குத் தேர்வை மாற்றுக" else "Switch Target Examination",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                            ) {
                                items(allExams) { exam ->
                                    val isSelected = profile.targetExamId == exam.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setTargetExam(exam.id)
                                                showExamSelectorDialog = false
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.setTargetExam(exam.id)
                                                    showExamSelectorDialog = false
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(if (isTamil) exam.nameTa else exam.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(exam.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showExamSelectorDialog = false
                                    showOnboardingModal = true
                                }
                            ) {
                                Text(if (isTamil) "முழு அமைப்புகள்" else "Full Setup Wizard")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExamSelectorDialog = false }) {
                                Text(if (isTamil) "மூடு" else "Close")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RevisionTabContainer(
    viewModel: StudyMasterViewModel
) {
    var subTab by remember { mutableStateOf(0) } // 0 = Flashcards / Spaced Repetition, 1 = Smart Notes & Docs
    val profile by viewModel.userProfile.collectAsState()
    val isTamil = profile.language == StudyLanguage.TAMIL

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text(if (isTamil) "ஃபிளாஷ்கார்டு (மீள்பார்வை)" else "Flashcards", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text(if (isTamil) "ஸ்மார்ட் நோட்ஸ் & ஆவணம்" else "Notes & Doc Learning", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (subTab == 0) {
                RevisionScreen(viewModel = viewModel)
            } else {
                SmartNotesScreen(viewModel = viewModel)
            }
        }
    }
}
