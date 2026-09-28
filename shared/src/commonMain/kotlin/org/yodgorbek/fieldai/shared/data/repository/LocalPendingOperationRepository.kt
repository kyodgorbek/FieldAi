package org.yodgorbek.fieldai.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yodgorbek.fieldai.shared.database.FieldAiDatabase
import org.yodgorbek.fieldai.shared.domain.model.PendingOperation
import org.yodgorbek.fieldai.shared.domain.repository.PendingOperationRepository

class LocalPendingOperationRepository(
    private val database: FieldAiDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PendingOperationRepository {

    private val queries = database.fieldAiDatabaseQueries

    override fun getPendingOperations(): Flow<List<PendingOperation>> {
        return queries.getPendingOperations()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities ->
                entities.map { entity ->
                    PendingOperation(
                        id = entity.id,
                        type = entity.type,
                        payload = entity.payload,
                        createdAt = entity.createdAt,
                        retryCount = entity.retryCount.toInt()
                    )
                }
            }
    }

    override suspend fun insertPendingOperation(operation: PendingOperation) {
        queries.insertPendingOperation(
            id = operation.id,
            type = operation.type,
            payload = operation.payload,
            createdAt = operation.createdAt,
            retryCount = operation.retryCount.toLong()
        )
    }

    override suspend fun deletePendingOperation(id: String) {
        queries.deletePendingOperation(id)
    }

    override suspend fun incrementRetryCount(id: String) {
        queries.incrementRetryCount(id)
    }
}
