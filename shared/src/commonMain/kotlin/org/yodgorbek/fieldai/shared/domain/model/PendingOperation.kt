package org.yodgorbek.fieldai.shared.domain.model

data class PendingOperation(
    val id: String,
    val type: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0
)
