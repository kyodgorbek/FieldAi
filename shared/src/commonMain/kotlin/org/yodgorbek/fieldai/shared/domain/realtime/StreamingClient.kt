package org.yodgorbek.fieldai.shared.domain.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StreamingClient {
    val connectionState: StateFlow<ConnectionState>
    val events: Flow<StreamingEvent>

    suspend fun connect(url: String)
    suspend fun disconnect()
    suspend fun send(payload: String)
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

sealed interface StreamingEvent {
    data class MessageReceived(val payload: String) : StreamingEvent
    data class Error(val error: Throwable) : StreamingEvent
}
