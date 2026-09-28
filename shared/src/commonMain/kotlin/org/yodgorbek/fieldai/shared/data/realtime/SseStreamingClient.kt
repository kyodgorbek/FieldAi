package org.yodgorbek.fieldai.shared.data.realtime

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
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
import kotlinx.coroutines.launch
import org.yodgorbek.fieldai.shared.domain.realtime.ConnectionState
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingClient
import org.yodgorbek.fieldai.shared.domain.realtime.StreamingEvent

import io.ktor.client.statement.bodyAsChannel

class SseStreamingClient(
    private val httpClient: HttpClient
) : StreamingClient {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<StreamingEvent>()
    override val events: Flow<StreamingEvent> = _events.asSharedFlow()

    private var activeStatement: HttpStatement? = null

    override suspend fun connect(url: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        _connectionState.value = ConnectionState.CONNECTING
        scope.launch {
            try {
                httpClient.prepareGet {
                    url(url)
                }.execute { response ->
                    _connectionState.value = ConnectionState.CONNECTED
                    val channel: ByteReadChannel = response.bodyAsChannel()
                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line()
                        if (line != null && line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ")
                            _events.emit(StreamingEvent.MessageReceived(data))
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
        _connectionState.value = ConnectionState.DISCONNECTED
        // Disconnecting SSE requires cancelling the coroutine scope or statement
    }

    override suspend fun send(payload: String) {
        // SSE is unidirectional, so send operations would typically go through a separate REST endpoint
        throw UnsupportedOperationException("SSE does not support bidirectional messaging. Use REST to send.")
    }
}
