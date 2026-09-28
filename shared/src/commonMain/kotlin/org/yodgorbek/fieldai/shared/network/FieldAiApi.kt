package org.yodgorbek.fieldai.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.yodgorbek.fieldai.shared.domain.model.Conversation
import org.yodgorbek.fieldai.shared.domain.model.Document
import org.yodgorbek.fieldai.shared.domain.model.Message

class FieldAiApi(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://api.fieldai.com/v1"

    suspend fun getConversations(): List<Conversation> {
        return httpClient.get("$baseUrl/conversations").body()
    }

    suspend fun getMessages(conversationId: String): List<Message> {
        return httpClient.get("$baseUrl/conversations/$conversationId/messages").body()
    }

    suspend fun getDocuments(): List<Document> {
        return httpClient.get("$baseUrl/documents").body()
    }
    
    suspend fun createDocument(document: Document) {
        httpClient.post("$baseUrl/documents") {
            setBody(document)
        }
    }
    
    suspend fun executeOperation(type: String, payload: String) {
        httpClient.post("$baseUrl/sync/operation") {
            setBody(payload)
        }
    }
}
