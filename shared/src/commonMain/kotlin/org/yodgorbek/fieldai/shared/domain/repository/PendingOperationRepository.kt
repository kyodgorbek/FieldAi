package org.yodgorbek.fieldai.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yodgorbek.fieldai.shared.domain.model.PendingOperation

interface PendingOperationRepository {
    fun getPendingOperations(): Flow<List<PendingOperation>>
    suspend fun insertPendingOperation(operation: PendingOperation)
    suspend fun deletePendingOperation(id: String)
    suspend fun incrementRetryCount(id: String)
}
