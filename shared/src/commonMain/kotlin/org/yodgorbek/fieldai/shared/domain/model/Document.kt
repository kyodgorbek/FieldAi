package org.yodgorbek.fieldai.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    val title: String,
    val summary: String?,
    val localPath: String?,
    val remoteUrl: String?,
    val createdAt: Long,
    val isSynced: Boolean
)
