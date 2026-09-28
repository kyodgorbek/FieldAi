package org.yodgorbek.fieldai.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yodgorbek.fieldai.shared.database.FieldAiDatabase
import org.yodgorbek.fieldai.shared.domain.model.Conversation
import org.yodgorbek.fieldai.shared.domain.repository.ConversationRepository

class LocalConversationRepository(
    private val database: FieldAiDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ConversationRepository {

    private val queries = database.fieldAiDatabaseQueries

    override fun getAllConversations(): Flow<List<Conversation>> {
        return queries.getAllConversations()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities ->
                entities.map { entity ->
                    Conversation(
                        id = entity.id,
                        title = entity.title,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
    }

    override suspend fun getConversationById(id: String): Conversation? {
        val entity = queries.getConversationById(id).executeAsOneOrNull() ?: return null
        return Conversation(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override suspend fun insertConversation(conversation: Conversation) {
        queries.insertConversation(
            id = conversation.id,
            title = conversation.title,
            createdAt = conversation.createdAt,
            updatedAt = conversation.updatedAt
        )
    }

    override suspend fun deleteConversation(id: String) {
        queries.deleteConversation(id)
    }
}
