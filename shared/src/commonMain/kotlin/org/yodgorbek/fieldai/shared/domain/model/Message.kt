package org.yodgorbek.fieldai.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val role: Role,
    val content: String,
    val timestamp: Long
) {
    @Serializable
    enum class Role {
        USER, ASSISTANT, SYSTEM, ERROR
    }
}
