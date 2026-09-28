package org.yodgorbek.fieldai.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yodgorbek.fieldai.shared.domain.model.Conversation

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    suspend fun getConversationById(id: String): Conversation?
    suspend fun insertConversation(conversation: Conversation)
    suspend fun deleteConversation(id: String)
}
