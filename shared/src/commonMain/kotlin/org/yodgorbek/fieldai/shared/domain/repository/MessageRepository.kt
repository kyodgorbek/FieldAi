package org.yodgorbek.fieldai.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yodgorbek.fieldai.shared.domain.model.Message

interface MessageRepository {
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    suspend fun insertMessage(message: Message)
}
