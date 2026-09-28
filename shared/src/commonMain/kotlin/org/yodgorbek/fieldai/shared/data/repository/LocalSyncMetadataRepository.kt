package org.yodgorbek.fieldai.shared.data.repository

import org.yodgorbek.fieldai.shared.database.FieldAiDatabase
import org.yodgorbek.fieldai.shared.domain.repository.SyncMetadataRepository

class LocalSyncMetadataRepository(
    private val database: FieldAiDatabase
) : SyncMetadataRepository {

    private val queries = database.fieldAiDatabaseQueries

    override suspend fun getSyncMetadata(key: String): Long? {
        return queries.getSyncMetadata(key).executeAsOneOrNull()
    }

    override suspend fun updateSyncMetadata(key: String, lastSyncedAt: Long) {
        queries.updateSyncMetadata(key, lastSyncedAt)
    }
}
