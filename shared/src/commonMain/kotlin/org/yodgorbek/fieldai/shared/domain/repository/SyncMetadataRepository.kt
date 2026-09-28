package org.yodgorbek.fieldai.shared.domain.repository

interface SyncMetadataRepository {
    suspend fun getSyncMetadata(key: String): Long?
    suspend fun updateSyncMetadata(key: String, lastSyncedAt: Long)
}
