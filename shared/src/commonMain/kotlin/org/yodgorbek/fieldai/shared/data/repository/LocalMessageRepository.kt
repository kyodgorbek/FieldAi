package org.yodgorbek.fieldai.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yodgorbek.fieldai.shared.database.FieldAiDatabase
import org.yodgorbek.fieldai.shared.domain.model.Message
import org.yodgorbek.fieldai.shared.domain.repository.MessageRepository

class LocalMessageRepository(
    private val database: FieldAiDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MessageRepository {

    private val queries = database.fieldAiDatabaseQueries

    override fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return queries.getMessagesForConversation(conversationId)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities ->
                entities.map { entity ->
                    Message(
                        id = entity.id,
                        conversationId = entity.conversationId,
                        role = Message.Role.valueOf(entity.role),
                        content = entity.content,
                        timestamp = entity.timestamp
                    )
                }
            }
    }

    override suspend fun insertMessage(message: Message) {
        queries.insertMessage(
            id = message.id,
            conversationId = message.conversationId,
            role = message.role.name,
            content = message.content,
            timestamp = message.timestamp
        )
    }
}
