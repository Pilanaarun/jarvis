package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.local.SmartDeviceEntity
import com.example.data.local.TaskEntity
import com.example.data.remote.GeminiApiClient
import com.example.voice.SpeechToTextHelper
import com.example.voice.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppTab {
    HOME_ARC_CORE,
    TASKS_MATRIX,
    SMART_HOME_SECURITY,
    SOCIAL_SCROLLER
}

enum class AiModelMode(val displayName: String, val badgeText: String) {
    JARVIS_CORE("JARVIS Core", "v3.5 Flash"),
    CHATGPT_PRO("ChatGPT Style", "Pro Reasoner"),
    VOICE_FAST("Voice Fast", "Low Latency")
}

enum class SocialPlatform(val displayName: String) {
    INSTAGRAM("Instagram Reels"),
    YOUTUBE("YouTube Shorts"),
    ALL("Universal Feed")
}

data class SocialScrollState(
    val platform: SocialPlatform = SocialPlatform.INSTAGRAM,
    val isAutoScrolling: Boolean = false,
    val scrollSpeedSec: Int = 5,
    val currentItemIndex: Int = 1,
    val totalItemsScrolled: Int = 12,
    val lastActionMessage: String = "Ready for voice or tap command"
)

data class UiState(
    val selectedTab: AppTab = AppTab.HOME_ARC_CORE,
    val selectedAiMode: AiModelMode = AiModelMode.JARVIS_CORE,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isTtsEnabled: Boolean = true,
    val currentPromptInput: String = "",
    val isProcessing: Boolean = false,
    val statusMessage: String = "JARVIS Online • All Systems Operational",
    val securityStatus: String = "SECURED", // SECURED, BREACH_WARNING, PATROL_ACTIVE
    val socialScrollState: SocialScrollState = SocialScrollState()
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: JarvisRepository
    private val ttsHelper = TextToSpeechHelper(application)
    private val sttHelper = SpeechToTextHelper(application)

    val tasks: StateFlow<List<TaskEntity>>
    val chatMessages: StateFlow<List<ChatMessageEntity>>
    val smartDevices: StateFlow<List<SmartDeviceEntity>>

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _tasksFlow = MutableStateFlow<List<TaskEntity>>(emptyList())
    private val _chatMessagesFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    private val _smartDevicesFlow = MutableStateFlow<List<SmartDeviceEntity>>(emptyList())

    private var autoScrollJob: Job? = null

    init {
        val database = JarvisDatabase.getDatabase(application)
        repository = JarvisRepository(database.taskDao(), database.chatMessageDao(), database.smartDeviceDao())

        tasks = _tasksFlow.asStateFlow()
        chatMessages = _chatMessagesFlow.asStateFlow()
        smartDevices = _smartDevicesFlow.asStateFlow()

        viewModelScope.launch {
            repository.initializeDefaultSmartDevices()

            launch {
                repository.allTasks.collect { _tasksFlow.value = it }
            }
            launch {
                repository.allChatMessages.collect { msgs ->
                    _chatMessagesFlow.value = msgs
                    if (msgs.isEmpty()) {
                        // Insert welcome message
                        repository.insertChatMessage(
                            ChatMessageEntity(
                                sender = "JARVIS",
                                text = "Greeting Sir! I am JARVIS, your personal cybernetic AI assistant. I am ready to manage your daily tasks, control smart devices, monitor security feeds, execute voice commands, and scroll Instagram or YouTube hands-free. How may I serve you today?",
                                aiMode = _uiState.value.selectedAiMode.name
                            )
                        )
                    }
                }
            }
            launch {
                repository.allSmartDevices.collect { _smartDevicesFlow.value = it }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectAiMode(mode: AiModelMode) {
        _uiState.value = _uiState.value.copy(selectedAiMode = mode)
        updateStatus("AI Engine updated to ${mode.displayName}")
    }

    fun updatePromptInput(input: String) {
        _uiState.value = _uiState.value.copy(currentPromptInput = input)
    }

    fun toggleTts() {
        val next = !_uiState.value.isTtsEnabled
        _uiState.value = _uiState.value.copy(isTtsEnabled = next)
        ttsHelper.isVoiceEnabled = next
        if (!next) ttsHelper.stop()
    }

    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(statusMessage = status)
    }

    fun sendMessage(promptText: String? = null) {
        val text = promptText ?: _uiState.value.currentPromptInput
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(
            currentPromptInput = "",
            isProcessing = true,
            statusMessage = "Analyzing command: '$text'..."
        )

        viewModelScope.launch {
            // Save user message
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = text,
                    aiMode = _uiState.value.selectedAiMode.name
                )
            )

            // Check if voice command matches a direct action (Task, Smart Home, Social Scroll)
            val actionHandled = parseAndExecuteSystemCommand(text)

            val aiResponseText = if (actionHandled.isNotBlank()) {
                actionHandled
            } else {
                // Call Gemini / AI Client
                GeminiApiClient.generateResponse(text, _uiState.value.selectedAiMode.name)
            }

            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    text = aiResponseText,
                    aiMode = _uiState.value.selectedAiMode.name
                )
            )

            _uiState.value = _uiState.value.copy(isProcessing = false)
            updateStatus("Command Processed • JARVIS Ready")

            // TTS Readout
            if (_uiState.value.isTtsEnabled) {
                _uiState.value = _uiState.value.copy(isSpeaking = true)
                ttsHelper.speak(aiResponseText) {
                    _uiState.value = _uiState.value.copy(isSpeaking = false)
                }
            }
        }
    }

    private suspend fun parseAndExecuteSystemCommand(command: String): String {
        val lower = command.lowercase()

        // 1. Task Creation Command ("remind me to...", "add task...")
        if (lower.contains("remind me to") || lower.contains("add task") || lower.contains("task create")) {
            val taskTitle = command.replace(Regex("(?i)(remind me to|add task|task create)"), "").trim()
            if (taskTitle.isNotBlank()) {
                repository.insertTask(
                    TaskEntity(
                        title = taskTitle.capitalize(),
                        category = "Voice Command",
                        priority = "High"
                    )
                )
                selectTab(AppTab.TASKS_MATRIX)
                return "Understood Sir. I have added the task '$taskTitle' to your daily matrix."
            }
        }

        // 2. Smart Home Voice Commands ("turn on living room lights", "lock gate", "turn off ac")
        if (lower.contains("light") || lower.contains("lamp") || lower.contains("ac") || lower.contains("thermostat") || lower.contains("lock") || lower.contains("gate")) {
            val currentList = smartDevices.value
            val matchDevice = currentList.find { lower.contains(it.name.lowercase()) || lower.contains(it.type.lowercase()) }
            if (matchDevice != null) {
                val newState = !lower.contains("off")
                val updated = matchDevice.copy(isOn = newState)
                repository.updateSmartDevice(updated)
                selectTab(AppTab.SMART_HOME_SECURITY)
                return "Affirmative Sir. ${matchDevice.name} is now ${if (newState) "Activated (ON)" else "Deactivated (OFF)"}."
            }
        }

        // 3. Social Media Scrolling Voice Commands ("scroll instagram", "scroll down", "next video", "start auto scroll")
        if (lower.contains("scroll") || lower.contains("instagram") || lower.contains("youtube") || lower.contains("reels") || lower.contains("shorts") || lower.contains("next")) {
            selectTab(AppTab.SOCIAL_SCROLLER)
            return when {
                lower.contains("start") || lower.contains("auto") -> {
                    startSocialAutoScroll()
                    "Social Auto-Scroller initiated Sir. Hands-free scroll running at ${_uiState.value.socialScrollState.scrollSpeedSec}s interval."
                }
                lower.contains("stop") || lower.contains("pause") -> {
                    stopSocialAutoScroll()
                    "Social Auto-Scroll paused as instructed, Sir."
                }
                lower.contains("instagram") -> {
                    setSocialPlatform(SocialPlatform.INSTAGRAM)
                    nextSocialItem()
                    "Switched to Instagram Reels feed and scrolled to next reel, Sir."
                }
                lower.contains("youtube") -> {
                    setSocialPlatform(SocialPlatform.YOUTUBE)
                    nextSocialItem()
                    "Switched to YouTube Shorts feed and scrolled to next short, Sir."
                }
                else -> {
                    nextSocialItem()
                    "Scrolled to next video in feed, Sir."
                }
            }
        }

        return "" // Let AI answer general query
    }

    fun startVoiceRecognition() {
        if (_uiState.value.isListening) {
            sttHelper.stopListening()
            _uiState.value = _uiState.value.copy(isListening = false)
            return
        }

        ttsHelper.stop()
        _uiState.value = _uiState.value.copy(isSpeaking = false)

        sttHelper.startListening(
            onResult = { recognizedText ->
                _uiState.value = _uiState.value.copy(isListening = false, currentPromptInput = recognizedText)
                sendMessage(recognizedText)
            },
            onError = { errorMsg ->
                _uiState.value = _uiState.value.copy(isListening = false)
                updateStatus("Voice Recognition: $errorMsg")
            },
            onStateChanged = { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        )
    }

    // Tasks Management
    fun addTask(title: String, category: String = "General", priority: String = "Medium") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(TaskEntity(title = title, category = category, priority = priority))
            updateStatus("Task '$title' saved.")
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }

    // Smart Home & Security Controls
    fun toggleDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            repository.updateSmartDevice(device.copy(isOn = !device.isOn))
            updateStatus("${device.name} state changed.")
        }
    }

    fun updateDeviceValue(device: SmartDeviceEntity, newValue: String) {
        viewModelScope.launch {
            repository.updateSmartDevice(device.copy(value = newValue))
        }
    }

    fun triggerSecurityPatrol() {
        val states = listOf("SECURED", "PATROL_ACTIVE", "SCANNING PERIMETER")
        val next = states[(states.indexOf(_uiState.value.securityStatus) + 1) % states.size]
        _uiState.value = _uiState.value.copy(securityStatus = next)
        updateStatus("Security Surveillance status set to: $next")
    }

    // Social Media Hands-Free Scrolling Automator
    fun setSocialPlatform(platform: SocialPlatform) {
        _uiState.value = _uiState.value.copy(
            socialScrollState = _uiState.value.socialScrollState.copy(
                platform = platform,
                lastActionMessage = "Active Platform: ${platform.displayName}"
            )
        )
    }

    fun setSocialScrollSpeed(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            socialScrollState = _uiState.value.socialScrollState.copy(scrollSpeedSec = seconds)
        )
        if (_uiState.value.socialScrollState.isAutoScrolling) {
            startSocialAutoScroll() // Restart interval with new speed
        }
    }

    fun nextSocialItem() {
        val state = _uiState.value.socialScrollState
        val nextIndex = (state.currentItemIndex % 15) + 1
        val newTotal = state.totalItemsScrolled + 1
        _uiState.value = _uiState.value.copy(
            socialScrollState = state.copy(
                currentItemIndex = nextIndex,
                totalItemsScrolled = newTotal,
                lastActionMessage = "Scrolled to Reel #$nextIndex on ${state.platform.displayName}"
            )
        )
    }

    fun previousSocialItem() {
        val state = _uiState.value.socialScrollState
        val prevIndex = if (state.currentItemIndex <= 1) 15 else state.currentItemIndex - 1
        _uiState.value = _uiState.value.copy(
            socialScrollState = state.copy(
                currentItemIndex = prevIndex,
                lastActionMessage = "Returned to Reel #$prevIndex on ${state.platform.displayName}"
            )
        )
    }

    fun toggleSocialAutoScroll() {
        if (_uiState.value.socialScrollState.isAutoScrolling) {
            stopSocialAutoScroll()
        } else {
            startSocialAutoScroll()
        }
    }

    private fun startSocialAutoScroll() {
        autoScrollJob?.cancel()
        _uiState.value = _uiState.value.copy(
            socialScrollState = _uiState.value.socialScrollState.copy(
                isAutoScrolling = true,
                lastActionMessage = "Auto-scrolling every ${_uiState.value.socialScrollState.scrollSpeedSec} seconds"
            )
        )
        autoScrollJob = viewModelScope.launch {
            while (_uiState.value.socialScrollState.isAutoScrolling) {
                delay(_uiState.value.socialScrollState.scrollSpeedSec * 1000L)
                nextSocialItem()
            }
        }
    }

    private fun stopSocialAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
        _uiState.value = _uiState.value.copy(
            socialScrollState = _uiState.value.socialScrollState.copy(
                isAutoScrolling = false,
                lastActionMessage = "Auto-scrolling paused"
            )
        )
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            updateStatus("Chat transcript wiped.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoScrollJob?.cancel()
        ttsHelper.shutdown()
        sttHelper.destroy()
    }
}
