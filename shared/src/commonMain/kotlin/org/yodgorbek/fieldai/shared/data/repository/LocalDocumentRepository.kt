package org.yodgorbek.fieldai.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yodgorbek.fieldai.shared.database.FieldAiDatabase
import org.yodgorbek.fieldai.shared.domain.model.Document
import org.yodgorbek.fieldai.shared.domain.repository.DocumentRepository

class LocalDocumentRepository(
    private val database: FieldAiDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DocumentRepository {

    private val queries = database.fieldAiDatabaseQueries

    override fun getAllDocuments(): Flow<List<Document>> {
        return queries.getAllDocuments()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities ->
                entities.map { entity ->
                    Document(
                        id = entity.id,
                        title = entity.title,
                        summary = entity.summary,
                        localPath = entity.localPath,
                        remoteUrl = entity.remoteUrl,
                        createdAt = entity.createdAt,
                        isSynced = entity.isSynced != 0L
                    )
                }
            }
    }

    override suspend fun insertDocument(document: Document) {
        queries.insertDocument(
            id = document.id,
            title = document.title,
            summary = document.summary,
            localPath = document.localPath,
            remoteUrl = document.remoteUrl,
            createdAt = document.createdAt,
            isSynced = if (document.isSynced) 1L else 0L
        )
    }
}
