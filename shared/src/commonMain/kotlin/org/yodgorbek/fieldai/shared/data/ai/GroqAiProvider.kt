package org.yodgorbek.fieldai.shared.data.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import org.yodgorbek.fieldai.shared.BuildConfiguration
import org.yodgorbek.fieldai.shared.domain.ai.AiProvider
import org.yodgorbek.fieldai.shared.domain.ai.AiStreamEvent
import org.yodgorbek.fieldai.shared.domain.model.Conversation

@Serializable
private data class GroqStreamResponse(
    val id: String? = null,
    val choices: List<GroqStreamChoice>? = null
)

@Serializable
private data class GroqStreamChoice(
    val delta: GroqStreamDelta? = null,
    val finish_reason: String? = null
)

@Serializable
private data class GroqStreamDelta(
    val content: String? = null
)

class GroqAiProvider(
    private val httpClient: HttpClient
) : AiProvider {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun sendMessage(
        conversation: Conversation,
        message: String
    ): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Started)
        try {
            httpClient.preparePost("https://api.groq.com/openai/v1/chat/completions") {
                header("Authorization", "Bearer ${BuildConfiguration.GROQ_API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("model", "llama-3.3-70b-versatile") // Upgraded to a stronger model for better results
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", message)
                            }
                        }
                        put("stream", true)
                    }
                )
            }.execute { response ->
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.isBlank()) continue
                    
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") {
                            break
                        }
                        try {
                            val parsed = json.decodeFromString<GroqStreamResponse>(data)
                            val content = parsed.choices?.firstOrNull()?.delta?.content
                            if (content != null) {
                                emit(AiStreamEvent.Token(content))
                            }
                        } catch (e: Exception) {
                            // Ignore parsing errors for individual malformed chunks
                        }
                    }
                }
            }
            
            emit(AiStreamEvent.Completed(messageId = "msg_groq_completed"))
        } catch (e: Exception) {
            emit(AiStreamEvent.Error(e))
        }
    }
}
