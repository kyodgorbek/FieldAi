package org.yodgorbek.fieldai.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yodgorbek.fieldai.shared.domain.model.Document

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    suspend fun insertDocument(document: Document)
}
