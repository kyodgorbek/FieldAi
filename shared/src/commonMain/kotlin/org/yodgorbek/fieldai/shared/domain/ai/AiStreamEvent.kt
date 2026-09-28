package org.yodgorbek.fieldai.shared.domain.ai

sealed interface AiStreamEvent {
    data class Token(val text: String) : AiStreamEvent
    data class Completed(val messageId: String) : AiStreamEvent
    data class Error(val error: Throwable) : AiStreamEvent
    data object Started : AiStreamEvent
}
