package org.yodgorbek.fieldai.shared.presentation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.yodgorbek.fieldai.shared.domain.ai.AiProvider
import org.yodgorbek.fieldai.shared.domain.ai.AiStreamEvent
import org.yodgorbek.fieldai.shared.domain.model.Conversation
import org.yodgorbek.fieldai.shared.domain.native.VoiceManager
import org.yodgorbek.fieldai.shared.domain.realtime.ConnectionState
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingClient
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FakeAiProvider : AiProvider {
    var responseTokens = listOf("Hello", " ", "World")
    
    override fun sendMessage(conversation: Conversation, message: String): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Started)
        responseTokens.forEach { token ->
            emit(AiStreamEvent.Token(token))
        }
        emit(AiStreamEvent.Completed)
    }
}

class FakeStreamingClient : StreamingClient {
    val connectionFlow = MutableStateFlow(ConnectionState.DISCONNECTED)
    
    override val connectionState: Flow<ConnectionState>
        get() = connectionFlow

    override suspend fun connect(url: String) {
        connectionFlow.value = ConnectionState.CONNECTED
    }

    override suspend fun disconnect() {
        connectionFlow.value = ConnectionState.DISCONNECTED
    }

    override suspend fun sendMessage(message: String) {
        // No-op
    }
}

class FakeVoiceManager : VoiceManager {
    var isListening = false
    
    override fun startListening(): Flow<String> {
        isListening = true
        return flowOf("Test voice input")
    }

    override fun stopListening() {
        isListening = false
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private lateinit var viewModel: AssistantViewModel
    private lateinit var fakeAiProvider: FakeAiProvider
    private lateinit var fakeStreamingClient: FakeStreamingClient
    private lateinit var fakeVoiceManager: FakeVoiceManager

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAiProvider = FakeAiProvider()
        fakeStreamingClient = FakeStreamingClient()
        fakeVoiceManager = FakeVoiceManager()
        
        viewModel = AssistantViewModel(
            aiProvider = fakeAiProvider,
            streamingClient = fakeStreamingClient,
            voiceManager = fakeVoiceManager
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isStreaming)
            assertFalse(initialState.isListening)
            assertEquals(ConnectionState.DISCONNECTED, initialState.connectionState)
            assertTrue(initialState.messages.isEmpty())
        }
    }

    @Test
    fun `connect realtime updates connection state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial state
            
            viewModel.onAction(AssistantUiAction.ConnectRealtime)
            
            val connectedState = awaitItem()
            assertEquals(ConnectionState.CONNECTED, connectedState.connectionState)
        }
    }

    @Test
    fun `send message triggers AI provider and updates messages`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            
            viewModel.onAction(AssistantUiAction.SendMessage("Test question"))
            
            // Should emit loading state with user + empty assistant message
            val loadingState = awaitItem()
            assertTrue(loadingState.isStreaming)
            assertEquals(2, loadingState.messages.size)
            assertEquals("Test question", loadingState.messages[0].content)
            assertEquals("", loadingState.messages[1].content)
            
            // It will stream tokens: "Hello", " ", "World"
            val token1State = awaitItem()
            assertEquals("Hello", token1State.messages[1].content)
            
            val token2State = awaitItem()
            assertEquals("Hello ", token2State.messages[1].content)
            
            val token3State = awaitItem()
            assertEquals("Hello World", token3State.messages[1].content)
            
            // And then complete
            val completedState = awaitItem()
            assertFalse(completedState.isStreaming)
            assertEquals("Hello World", completedState.messages[1].content)
        }
    }
    
    @Test
    fun `toggle voice listening starts listening and sends message`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            
            viewModel.onAction(AssistantUiAction.ToggleVoiceListening)
            
            val listeningState = awaitItem()
            assertTrue(listeningState.isListening)
            
            // After VoiceManager emits "Test voice input", it sends message automatically
            val autoSendState = awaitItem()
            assertFalse(autoSendState.isListening) // Stopped listening after send
            assertTrue(autoSendState.isStreaming) // Now streaming the AI response
            assertEquals("Test voice input", autoSendState.messages[0].content)
            
            // Cancel and clear remaining test items
            cancelAndIgnoreRemainingEvents()
        }
    }
}
