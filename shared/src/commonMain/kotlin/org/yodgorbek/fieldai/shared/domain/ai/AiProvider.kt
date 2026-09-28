package org.yodgorbek.fieldai.shared.domain.ai

import kotlinx.coroutines.flow.Flow
import org.yodgorbek.fieldai.shared.domain.model.Conversation

interface AiProvider {
    suspend fun sendMessage(
        conversation: Conversation,
        message: String
    ): Flow<AiStreamEvent>
}
