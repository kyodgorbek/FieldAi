package org.yodgorbek.fieldai.shared.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yodgorbek.fieldai.shared.domain.ai.AiProvider
import org.yodgorbek.fieldai.shared.domain.ai.AiStreamEvent
import org.yodgorbek.fieldai.shared.domain.model.Conversation
import org.yodgorbek.fieldai.shared.domain.model.Message
import org.yodgorbek.fieldai.shared.domain.realtime.ConnectionState
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingClient
import kotlinx.datetime.Clock
import org.yodgorbek.fieldai.shared.domain.native.VoiceManager

data class AssistantUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val isStreaming: Boolean = false,
    val isListening: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val error: String? = null
)

sealed interface AssistantUiAction {
    data class SendMessage(val text: String) : AssistantUiAction
    object CancelStreaming : AssistantUiAction
    object ConnectRealtime : AssistantUiAction
    object DisconnectRealtime : AssistantUiAction
    object ToggleVoiceListening : AssistantUiAction
}

class AssistantViewModel(
    private val aiProvider: AiProvider,
    private val streamingClient: StreamingClient,
    private val voiceManager: VoiceManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var voiceListeningJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            streamingClient.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
    }

    fun onAction(action: AssistantUiAction) {
        when (action) {
            is AssistantUiAction.SendMessage -> handleSendMessage(action.text)
            AssistantUiAction.CancelStreaming -> {
                _uiState.value = _uiState.value.copy(isStreaming = false)
            }
            AssistantUiAction.ConnectRealtime -> {
                scope.launch {
                    streamingClient.connect("wss://api.fieldai.com/ws")
                }
            }
            AssistantUiAction.DisconnectRealtime -> {
                scope.launch {
                    streamingClient.disconnect()
                }
            }
            AssistantUiAction.ToggleVoiceListening -> handleToggleVoiceListening()
        }
    }

    private fun handleToggleVoiceListening() {
        if (_uiState.value.isListening) {
            voiceManager.stopListening()
            voiceListeningJob?.cancel()
            _uiState.value = _uiState.value.copy(isListening = false)
        } else {
            _uiState.value = _uiState.value.copy(isListening = true, error = null)
            voiceListeningJob = scope.launch {
                try {
                    voiceManager.startListening().collect { partialText ->
                        // Automatically send the message when speech provides results
                        handleSendMessage(partialText)
                        
                        // Stop listening after a successful send
                        voiceManager.stopListening()
                        _uiState.value = _uiState.value.copy(isListening = false)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        error = e.message ?: "Failed to record voice"
                    )
                }
            }
        }
    }

    private fun handleSendMessage(text: String) {
        val currentConversation = _uiState.value.conversation ?: Conversation(
            id = "conv_${org.yodgorbek.fieldai.shared.util.currentTimeMillis()}",
            title = "New Conversation",
            createdAt = org.yodgorbek.fieldai.shared.util.currentTimeMillis(),
            updatedAt = org.yodgorbek.fieldai.shared.util.currentTimeMillis()
        )

        val userMessage = Message(
            id = "msg_${org.yodgorbek.fieldai.shared.util.currentTimeMillis()}",
            conversationId = currentConversation.id,
            role = Message.Role.USER,
            content = text,
            timestamp = org.yodgorbek.fieldai.shared.util.currentTimeMillis()
        )

        val assistantMessage = Message(
            id = "msg_${org.yodgorbek.fieldai.shared.util.currentTimeMillis() + 1}",
            conversationId = currentConversation.id,
            role = Message.Role.ASSISTANT,
            content = "",
            timestamp = org.yodgorbek.fieldai.shared.util.currentTimeMillis()
        )

        _uiState.value = _uiState.value.copy(
            conversation = currentConversation,
            messages = _uiState.value.messages + userMessage + assistantMessage,
            isStreaming = true,
            error = null
        )

        scope.launch {
            try {
                aiProvider.sendMessage(currentConversation, text).collect { event ->
                    when (event) {
                        is AiStreamEvent.Started -> {
                            _uiState.value = _uiState.value.copy(isStreaming = true)
                        }
                        is AiStreamEvent.Token -> {
                            val updatedMessages = _uiState.value.messages.map {
                                if (it.id == assistantMessage.id) {
                                    it.copy(content = it.content + event.text)
                                } else it
                            }
                            _uiState.value = _uiState.value.copy(messages = updatedMessages)
                        }
                        is AiStreamEvent.Completed -> {
                            _uiState.value = _uiState.value.copy(isStreaming = false)
                        }
                        is AiStreamEvent.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = event.error.message ?: "Unknown error"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = e.message ?: "Failed to send message"
                )
            }
        }
    }
}
