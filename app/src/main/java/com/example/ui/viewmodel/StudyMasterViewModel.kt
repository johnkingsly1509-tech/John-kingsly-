package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.model.*
import com.example.data.provider.ExamDataProvider
import com.example.data.repository.StudyMasterRepository
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyMasterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyMasterRepository(application)
    val ttsHelper = TextToSpeechHelper(application)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfile()
        )

    val todayTasks: StateFlow<List<DailyTask>> = repository.getTasksForDay(0)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUpcomingTasks: StateFlow<List<DailyTask>> = repository.getAllUpcomingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<StudyNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mockAttempts: StateFlow<List<MockTestAttempt>> = repository.mockAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Teacher State
    private val _selectedTopic = MutableStateFlow<ExamTopic?>(null)
    val selectedTopic: StateFlow<ExamTopic?> = _selectedTopic.asStateFlow()

    private val _currentLesson = MutableStateFlow<AiLessonResponse?>(null)
    val currentLesson: StateFlow<AiLessonResponse?> = _currentLesson.asStateFlow()

    private val _isLessonLoading = MutableStateFlow(false)
    val isLessonLoading: StateFlow<Boolean> = _isLessonLoading.asStateFlow()

    private val _tutorChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val tutorChatHistory: StateFlow<List<Pair<String, String>>> = _tutorChatHistory.asStateFlow()

    private val _isAnsweringDoubt = MutableStateFlow(false)
    val isAnsweringDoubt: StateFlow<Boolean> = _isAnsweringDoubt.asStateFlow()

    // Active Task Timer State
    private val _activeTimerTaskId = MutableStateFlow<String?>(null)
    val activeTimerTaskId: StateFlow<String?> = _activeTimerTaskId.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()
    private var timerJob: Job? = null

    // Mock Test State
    private val _activeMockTest = MutableStateFlow<MockTest?>(null)
    val activeMockTest: StateFlow<MockTest?> = _activeMockTest.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // questionIndex -> selectedOptionIndex
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    private val _testTimerSeconds = MutableStateFlow(0)
    val testTimerSeconds: StateFlow<Int> = _testTimerSeconds.asStateFlow()
    private var testTimerJob: Job? = null

    private val _lastAttemptResult = MutableStateFlow<MockTestAttempt?>(null)
    val lastAttemptResult: StateFlow<MockTestAttempt?> = _lastAttemptResult.asStateFlow()

    // Smart Document Processor State
    private val _documentSummaryResult = MutableStateFlow<String?>(null)
    val documentSummaryResult: StateFlow<String?> = _documentSummaryResult.asStateFlow()

    private val _isSummarizingDoc = MutableStateFlow(false)
    val isSummarizingDoc: StateFlow<Boolean> = _isSummarizingDoc.asStateFlow()

    // AI Study Plan Engine Strategy & Generation State
    private val _aiPlanStrategy = MutableStateFlow<String?>(null)
    val aiPlanStrategy: StateFlow<String?> = _aiPlanStrategy.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _lastReorganizeNotice = MutableStateFlow<String?>(null)
    val lastReorganizeNotice: StateFlow<String?> = _lastReorganizeNotice.asStateFlow()

    // Admin Mode state
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            val autoReorg = repository.autoCheckAndReorganizeOnStartup()
            if (autoReorg != null) {
                val isTa = userProfile.value.language == StudyLanguage.TAMIL
                _lastReorganizeNotice.value = if (isTa) autoReorg.summaryTa else autoReorg.summaryEn
            }
            // Set initial selected topic
            val defaultExam = ExamDataProvider.getExamById("tnpsc_grp4")
            val defaultTopic = defaultExam.subjects.firstOrNull()?.chapters?.firstOrNull()?.topics?.firstOrNull()
            _selectedTopic.value = defaultTopic
            loadLessonForTopic(defaultTopic?.titleEn ?: "Constitution Preamble & Fundamental Rights", "Indian Polity", defaultExam.nameEn)
        }
    }

    fun setTargetExam(examId: String) {
        viewModelScope.launch {
            repository.updateTargetExam(examId)
            val exam = ExamDataProvider.getExamById(examId)
            val firstTopic = exam.subjects.firstOrNull()?.chapters?.firstOrNull()?.topics?.firstOrNull()
            _selectedTopic.value = firstTopic
            if (firstTopic != null) {
                loadLessonForTopic(firstTopic.titleEn, exam.subjects.first().titleEn, exam.nameEn)
            }
        }
    }

    fun setLanguage(language: StudyLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(language)
            // Reload lesson with new language
            val topic = _selectedTopic.value
            val currentExam = ExamDataProvider.getExamById(userProfile.value.targetExamId)
            if (topic != null) {
                loadLessonForTopic(topic.titleEn, currentExam.subjects.firstOrNull()?.titleEn ?: "General Studies", currentExam.nameEn)
            }
        }
    }

    fun saveFullProfile(
        name: String,
        targetExamId: String,
        targetYear: Int,
        targetDate: String,
        language: StudyLanguage,
        prepLevel: PrepLevel,
        dailyHours: DailyAvailability,
        scheduleSlot: StudyScheduleSlot,
        studentStatus: StudentStatus,
        weakTopics: String,
        strongTopics: String
    ) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                targetExamId = targetExamId,
                targetExamYear = targetYear,
                targetExamDate = targetDate,
                language = language,
                prepLevel = prepLevel,
                dailyHours = dailyHours,
                scheduleSlot = scheduleSlot,
                studentStatus = studentStatus,
                weakTopics = weakTopics,
                strongTopics = strongTopics
            )
            repository.saveProfile(updated)
            setTargetExam(targetExamId)
        }
    }

    fun selectTopic(topic: ExamTopic, subjectName: String) {
        _selectedTopic.value = topic
        val currentExam = ExamDataProvider.getExamById(userProfile.value.targetExamId)
        loadLessonForTopic(topic.titleEn, subjectName, currentExam.nameEn)
    }

    fun loadLessonForTopic(topicTitle: String, subjectName: String, examName: String) {
        viewModelScope.launch {
            _isLessonLoading.value = true
            _currentLesson.value = GeminiAiService.generateTopicLesson(
                topicTitle = topicTitle,
                subjectName = subjectName,
                examName = examName,
                language = userProfile.value.language
            )
            _isLessonLoading.value = false
        }
    }

    fun askTutorDoubt(question: String) {
        if (question.isBlank()) return
        val currentTopic = _selectedTopic.value?.titleEn ?: "General Syllabus"
        viewModelScope.launch {
            _isAnsweringDoubt.value = true
            val currentList = _tutorChatHistory.value.toMutableList()
            currentList.add("Student" to question)
            _tutorChatHistory.value = currentList

            val answer = GeminiAiService.askTutorDoubt(
                question = question,
                currentTopic = currentTopic,
                language = userProfile.value.language
            )
            currentList.add("Study Master AI" to answer)
            _tutorChatHistory.value = currentList
            _isAnsweringDoubt.value = false

            // Auto-speak answer intro if desired
            ttsHelper.speak(answer.take(150), userProfile.value.language)
        }
    }

    fun toggleTaskCompletion(task: DailyTask) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            val minutes = if (newCompleted) task.estimatedMinutes else 0
            repository.markTaskComplete(task.id, newCompleted, minutes)
        }
    }

    fun startTaskTimer(taskId: String) {
        stopTaskTimer()
        _activeTimerTaskId.value = taskId
        _timerSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun stopTaskTimer() {
        val currentTask = _activeTimerTaskId.value
        val elapsedSec = _timerSeconds.value
        timerJob?.cancel()
        timerJob = null
        _activeTimerTaskId.value = null
        _timerSeconds.value = 0

        if (currentTask != null && elapsedSec >= 60) {
            val minutes = elapsedSec / 60
            viewModelScope.launch {
                repository.markTaskComplete(currentTask, true, minutes)
            }
        }
    }

    fun reorganizeMissedDays() {
        adjustForMissedDay(0)
    }

    fun adjustForMissedDay(dayOffset: Int = 0) {
        viewModelScope.launch {
            val reorgResult = repository.reorganizeMissedDays(dayOffset)
            val isTa = userProfile.value.language == StudyLanguage.TAMIL
            _lastReorganizeNotice.value = if (isTa) reorgResult.summaryTa else reorgResult.summaryEn
        }
    }

    fun clearReorganizeNotice() {
        _lastReorganizeNotice.value = null
    }

    fun regenerateStudyPlanWithAi(weakTopicsOverride: String? = null) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val profile = userProfile.value
            val currentWeak = weakTopicsOverride ?: profile.weakTopics
            val updatedProfile = profile.copy(weakTopics = currentWeak)
            if (weakTopicsOverride != null) {
                repository.saveProfile(updatedProfile)
            }
            val latestAccuracy = mockAttempts.value.firstOrNull()?.scorePercentage
            val exam = ExamDataProvider.getExamById(updatedProfile.targetExamId)

            // 1. Regenerate 30-day task plan
            repository.regeneratePlanForUser(updatedProfile, latestAccuracy)

            // 2. Generate strategic AI teacher insights
            val strategy = GeminiAiService.generateStudyPlanStrategy(updatedProfile, exam.nameEn, latestAccuracy)
            _aiPlanStrategy.value = strategy
            _isGeneratingPlan.value = false
        }
    }

    // Mock Test Handling
    fun startMockTest(test: MockTest) {
        _activeMockTest.value = test
        _userAnswers.value = emptyMap()
        _testTimerSeconds.value = test.durationMinutes * 60
        _lastAttemptResult.value = null

        testTimerJob?.cancel()
        testTimerJob = viewModelScope.launch {
            while (_testTimerSeconds.value > 0) {
                delay(1000)
                _testTimerSeconds.value -= 1
            }
            submitMockTest()
        }
    }

    fun selectAnswerForQuestion(questionIndex: Int, optionIndex: Int) {
        val current = _userAnswers.value.toMutableMap()
        current[questionIndex] = optionIndex
        _userAnswers.value = current
    }

    fun submitMockTest() {
        val test = _activeMockTest.value ?: return
        testTimerJob?.cancel()
        testTimerJob = null

        val answers = _userAnswers.value
        var correct = 0
        var wrong = 0
        var skipped = 0

        test.questions.forEachIndexed { index, question ->
            val userSelected = answers[index]
            if (userSelected == null) {
                skipped++
            } else if (userSelected == question.correctIndex) {
                correct++
            } else {
                wrong++
            }
        }

        val total = test.questions.size
        val scorePercent = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val timeSpent = (test.durationMinutes * 60) - _testTimerSeconds.value

        val attempt = MockTestAttempt(
            id = "attempt_${System.currentTimeMillis()}",
            testId = test.id,
            testTitleEn = test.titleEn,
            testTitleTa = test.titleTa,
            examId = test.examId,
            totalQuestions = total,
            correctAnswers = correct,
            wrongAnswers = wrong,
            skippedAnswers = skipped,
            scorePercentage = scorePercent,
            timeSpentSeconds = timeSpent
        )

        viewModelScope.launch {
            repository.recordMockTestAttempt(attempt)
            _lastAttemptResult.value = attempt
        }
    }

    fun closeActiveTest() {
        _activeMockTest.value = null
        testTimerJob?.cancel()
    }

    // Flashcard Handling
    fun reviewFlashcard(card: Flashcard, quality: Int) {
        viewModelScope.launch {
            repository.updateFlashcardInterval(card, quality)
        }
    }

    fun createFlashcard(examId: String, subject: String, topic: String, qEn: String, qTa: String, aEn: String, aTa: String) {
        viewModelScope.launch {
            val card = Flashcard(
                id = "fc_${System.currentTimeMillis()}",
                examId = examId,
                subjectName = subject,
                topicTitle = topic,
                questionEn = qEn,
                questionTa = qTa,
                answerEn = aEn,
                answerTa = aTa
            )
            repository.addFlashcard(card)
        }
    }

    // Notes Handling
    fun saveNote(examId: String, subject: String, topic: String, content: String, contentTa: String, tags: String) {
        viewModelScope.launch {
            repository.addStudyNote(examId, subject, topic, content, contentTa, tags)
        }
    }

    fun deleteNote(note: StudyNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun summarizeDocumentText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSummarizingDoc.value = true
            _documentSummaryResult.value = GeminiAiService.summarizeDocument(text, userProfile.value.language)
            _isSummarizingDoc.value = false
        }
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun speakText(text: String) {
        ttsHelper.speak(text, userProfile.value.language)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        testTimerJob?.cancel()
        ttsHelper.shutdown()
    }
}
