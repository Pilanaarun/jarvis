package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.AppTab
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcCoreSection
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ChatSection
import com.example.ui.components.HeaderSection
import com.example.ui.components.ModelSelectorBar
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.screens.SocialScrollerScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.GeometricBg
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: JarvisViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val smartDevices by viewModel.smartDevices.collectAsState()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecognition()
        } else {
            viewModel.updateStatus("Microphone permission denied for voice control.")
        }
    }

    val requestVoiceMic = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startVoiceRecognition()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GeometricBg,
        bottomBar = {
            BottomNavBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GeometricBg)
        ) {
            // Persistent System Header
            HeaderSection(
                securityStatus = uiState.securityStatus,
                isTtsEnabled = uiState.isTtsEnabled,
                onToggleTts = { viewModel.toggleTts() },
                onSecurityClick = { viewModel.selectTab(AppTab.SMART_HOME_SECURITY) }
            )

            // Dynamic Screen Content based on Selected Tab
            when (uiState.selectedTab) {
                AppTab.HOME_ARC_CORE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            ModelSelectorBar(
                                selectedMode = uiState.selectedAiMode,
                                onModeSelected = { viewModel.selectAiMode(it) }
                            )
                        }

                        item {
                            ArcCoreSection(
                                isListening = uiState.isListening,
                                isSpeaking = uiState.isSpeaking,
                                statusMessage = uiState.statusMessage,
                                onVoiceClick = requestVoiceMic
                            )
                        }

                        item {
                            ChatSection(
                                messages = chatMessages,
                                inputText = uiState.currentPromptInput,
                                isProcessing = uiState.isProcessing,
                                isListening = uiState.isListening,
                                onInputChanged = { viewModel.updatePromptInput(it) },
                                onSendMessage = { viewModel.sendMessage() },
                                onVoiceClick = requestVoiceMic,
                                onClearChat = { viewModel.clearChatHistory() }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                AppTab.TASKS_MATRIX -> {
                    TasksScreen(
                        tasks = tasks,
                        onAddTask = { title, cat, priority -> viewModel.addTask(title, cat, priority) },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onClearCompleted = { viewModel.clearCompletedTasks() }
                    )
                }

                AppTab.SMART_HOME_SECURITY -> {
                    SmartHomeScreen(
                        devices = smartDevices,
                        securityStatus = uiState.securityStatus,
                        onToggleDevice = { viewModel.toggleDevice(it) },
                        onTriggerPatrol = { viewModel.triggerSecurityPatrol() }
                    )
                }

                AppTab.SOCIAL_SCROLLER -> {
                    SocialScrollerScreen(
                        state = uiState.socialScrollState,
                        onPlatformSelected = { viewModel.setSocialPlatform(it) },
                        onSpeedSelected = { viewModel.setSocialScrollSpeed(it) },
                        onNextItem = { viewModel.nextSocialItem() },
                        onPrevItem = { viewModel.previousSocialItem() },
                        onToggleAutoScroll = { viewModel.toggleSocialAutoScroll() }
                    )
                }
            }
        }
    }
}
