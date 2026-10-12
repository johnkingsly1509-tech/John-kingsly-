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
import com.example.ui.viewmodel.StudyMasterViewModel

@Composable
fun RevisionScreen(
    viewModel: StudyMasterViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val isTamil = profile.language == StudyLanguage.TAMIL

    var currentCardIndex by remember { mutableStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    val activeCard = flashcards.getOrNull(currentCardIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("revision_screen_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTamil) "இடைவெளி நினைவூட்டல் & ஃபிளாஷ்கார்டு" else "Spaced Revision & Memory System",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTamil) "செயலூக்க நினைவுகூர்தல் (Active Recall)" else "Active Recall & Interval Learning",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showAddCardDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                        .testTag("add_flashcard_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Active Spaced Repetition Card Deck
        if (activeCard != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCardFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clickable { isCardFlipped = !isCardFlipped }
                        .testTag("active_flashcard_flip_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = activeCard.subjectName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "Card ${currentCardIndex + 1} of ${flashcards.size}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Center: Question or Answer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (!isCardFlipped) (if (isTamil) activeCard.questionTa else activeCard.questionEn)
                                           else (if (isTamil) activeCard.answerTa else activeCard.answerEn),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (!isCardFlipped) (if (isTamil) "(விடை பார்க்க தட்டவும்)" else "(Tap card to flip answer)")
                                           else (if (isTamil) "அடுத்த மீள்பார்வை இடைவெளி: ${activeCard.intervalDays} நாள்" else "Current Interval: ${activeCard.intervalDays} Days"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Bottom Flip Prompt Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Interval Rating Action Bar (Active when flipped)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(activeCard, 0)
                            isCardFlipped = false
                            if (currentCardIndex < flashcards.size - 1) currentCardIndex++ else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isTamil) "கடினம் (1 நாள்)" else "Hard (1d)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(activeCard, 1)
                            isCardFlipped = false
                            if (currentCardIndex < flashcards.size - 1) currentCardIndex++ else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isTamil) "நன்று (3 நாள்)" else "Good (3d)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(activeCard, 2)
                            isCardFlipped = false
                            if (currentCardIndex < flashcards.size - 1) currentCardIndex++ else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isTamil) "எளிது (7+ நாள்)" else "Easy (7d+)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Spaced Repetition Scientific Framework Explanation
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isTamil) "எபிங்ஹாஸ் மறதி வளைவு & இடைவெளி மீள்பார்வை"
                               else "Ebbinghaus Forgetting Curve & Spaced Repetition",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isTamil)
                            "ஒரு முறை படித்ததை 24 மணி நேரம், 3 நாட்கள் மற்றும் 7 நாட்களில் சுயமாக நினைவுபடுத்தினால் (Active Recall), அது நீண்ட கால நினைவகத்தில் (Long-term memory) ஆழமாகப் பதியும்."
                        else
                            "Revisiting difficult concepts at increasing intervals (Day 1, Day 3, Day 7, Day 14) cements exam facts into long-term recall without endless cramming.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // All Flashcards Deck List
        item {
            Text(
                text = if (isTamil) "அனைத்து ஃபிளாஷ்கார்டுகள் (${flashcards.size})" else "All Saved Flashcards (${flashcards.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(flashcards) { card ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(card.subjectName, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Interval: ${card.intervalDays}d", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isTamil) card.questionTa else card.questionEn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(if (isTamil) card.answerTa else card.answerEn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Add Custom Flashcard Dialog
    if (showAddCardDialog) {
        var sub by remember { mutableStateOf("General Studies") }
        var topic by remember { mutableStateOf("Core Concept") }
        var qEn by remember { mutableStateOf("") }
        var qTa by remember { mutableStateOf("") }
        var aEn by remember { mutableStateOf("") }
        var aTa by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text(if (isTamil) "புதிய ஃபிளாஷ்கார்டு சேர்க்க" else "Add Custom Flashcard", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qEn,
                        onValueChange = { qEn = it },
                        label = { Text("Question (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = qTa,
                        onValueChange = { qTa = it },
                        label = { Text("வினா (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = aEn,
                        onValueChange = { aEn = it },
                        label = { Text("Answer (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = aTa,
                        onValueChange = { aTa = it },
                        label = { Text("விடை (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (qEn.isNotBlank() || qTa.isNotBlank()) {
                            viewModel.createFlashcard(
                                examId = profile.targetExamId,
                                subject = sub,
                                topic = topic,
                                qEn = qEn.ifBlank { qTa },
                                qTa = qTa.ifBlank { qEn },
                                aEn = aEn.ifBlank { aTa },
                                aTa = aTa.ifBlank { aEn }
                            )
                            showAddCardDialog = false
                        }
                    }
                ) {
                    Text(if (isTamil) "சேமி" else "Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}
