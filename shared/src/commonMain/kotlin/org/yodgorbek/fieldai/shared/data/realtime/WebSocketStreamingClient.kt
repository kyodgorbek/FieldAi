package org.yodgorbek.fieldai.shared.data.realtime

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.yodgorbek.fieldai.shared.domain.realtime.ConnectionState
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingClient
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingEvent

class WebSocketStreamingClient(
    private val httpClient: HttpClient
) : StreamingClient {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var session: WebSocketSession? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<StreamingEvent>()
    override val events: Flow<StreamingEvent> = _events.asSharedFlow()

    override suspend fun connect(url: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        _connectionState.value = ConnectionState.CONNECTING
        try {
            session = httpClient.webSocketSession {
                url(url)
            }
            _connectionState.value = ConnectionState.CONNECTED
            listenForMessages()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.FAILED
            _events.emit(StreamingEvent.Error(e))
        }
    }

    private fun listenForMessages() {
        scope.launch {
            try {
                session?.let { ws ->
                    for (frame in ws.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            _events.emit(StreamingEvent.MessageReceived(text))
                        }
                    }
                }
            } catch (e: Exception) {
                _events.emit(StreamingEvent.Error(e))
                _connectionState.value = ConnectionState.FAILED
            } finally {
                if (_connectionState.value == ConnectionState.CONNECTED) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }
    }

    override suspend fun disconnect() {
        session?.close()
        session = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun send(payload: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            session?.send(Frame.Text(payload))
        }
    }
}
