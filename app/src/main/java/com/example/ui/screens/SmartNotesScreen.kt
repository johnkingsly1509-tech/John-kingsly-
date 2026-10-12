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
fun SmartNotesScreen(
    viewModel: StudyMasterViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val summaryResult by viewModel.documentSummaryResult.collectAsState()
    val isSummarizing by viewModel.isSummarizingDoc.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = My Notes, 1 = Document / Text AI Summarizer
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var documentInputText by remember { mutableStateOf("") }

    val isTamil = profile.language == StudyLanguage.TAMIL

    val filteredNotes = remember(allNotes, searchQuery) {
        if (searchQuery.isBlank()) allNotes
        else allNotes.filter {
            it.topicTitle.contains(searchQuery, ignoreCase = true) ||
            it.subjectName.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true) ||
            it.contentTa.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("smart_notes_screen_column"),
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
                        text = if (isTamil) "ஸ்மார்ட் நோட்ஸ் & ஆவணக் கற்றல்" else "Smart Notes & Document Learning",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTamil) "AI சுருக்கம், வினாக்கள் & தனிப்பயன் குறிப்புகள்" else "AI Summaries, Key Definitions & Quizzes",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showAddNoteDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                        .testTag("create_note_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Note", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Sub Tabs: My Notes | AI Document / Text Processor
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isTamil) "சேமித்த குறிப்புகள்" else "My Notes (${filteredNotes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isTamil) "ஆவண AI சுருக்கம்" else "Doc AI Processor", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isTamil) "குறிப்புகளில் தேடுக..." else "Search across notes & topics...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_notes_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (filteredNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTamil) "குறிப்புகள் எதுவும் இல்லை. புதிய குறிப்பைச் சேர்க்க '+' பொத்தானை அழுத்தவும்."
                                   else "No notes found. Tap '+' to create your first exam note.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredNotes) { note ->
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
                                        text = note.subjectName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteNote(note) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = note.topicTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isTamil && note.contentTa.isNotBlank()) note.contentTa else note.content,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (note.tags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "#${note.tags}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // DOCUMENT / TEXT AI LEARNING STUDIO
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "பாட ஆவணம் / உரை உள்ளீடு" else "Study Document / Text Input",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = documentInputText,
                            onValueChange = { documentInputText = it },
                            placeholder = {
                                Text(
                                    if (isTamil) "பாடப் புத்தகத்தின் பகுதியையோ அல்லது குறிப்புகளையோ இங்கே ஒட்டவும் (Paste text)..."
                                    else "Paste chapter text, legal sections, or study materials here for AI extraction...",
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("doc_input_field"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick sample chapter loader buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    documentInputText = "Article 19 guarantees six freedoms: Speech & Expression, Assembly, Association, Movement, Residence, and Profession. All six rights are subject to reasonable restrictions under clauses (2) to (6) in the interests of sovereignty, security, public order, and morality."
                                },
                                label = { Text("Polity Sample", fontSize = 10.sp) }
                            )
                            AssistChip(
                                onClick = {
                                    documentInputText = "Mendel's Law of Segregation states that allele pairs separate during gamete formation and randomly unite at fertilization. Law of Independent Assortment explains inheritance of two different genes."
                                },
                                label = { Text("Science Sample", fontSize = 10.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.summarizeDocumentText(documentInputText) },
                            enabled = !isSummarizing && documentInputText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("summarize_doc_button")
                        ) {
                            if (isSummarizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isTamil) "ஆவணத்தை ஆராய்கிறது..." else "Extracting Key Concepts...")
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTamil) "AI சுருக்கம் & குறிப்புகள் உருவாக்கு" else "Generate AI Summary & Notes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Summary Result Card
            summaryResult?.let { summary ->
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
                                    text = if (isTamil) "AI ஆவண பகுப்பாய்வு முடிவு" else "AI Document Analysis Result",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Button(
                                    onClick = {
                                        viewModel.saveNote(
                                            examId = profile.targetExamId,
                                            subject = "Extracted Document",
                                            topic = "Chapter Summary",
                                            content = summary,
                                            contentTa = summary,
                                            tags = "AI Generated, Doc Extracted"
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isTamil) "சேமி" else "Save Note", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = summary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Manual Note Dialog
    if (showAddNoteDialog) {
        var sub by remember { mutableStateOf("General Studies") }
        var topic by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("Important") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (isTamil) "புதிய குறிப்பு எழுதுக" else "Create New Study Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sub,
                        onValueChange = { sub = it },
                        label = { Text("Subject / பாடம்") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic Title / தலைப்பு") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content / குறிப்புகள்") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (e.g. Polity, Prelims)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topic.isNotBlank() && content.isNotBlank()) {
                            viewModel.saveNote(
                                examId = profile.targetExamId,
                                subject = sub,
                                topic = topic,
                                content = content,
                                contentTa = content,
                                tags = tags
                            )
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text(if (isTamil) "சேமி" else "Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}
