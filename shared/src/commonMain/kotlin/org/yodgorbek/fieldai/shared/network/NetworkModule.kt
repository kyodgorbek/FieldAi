package org.yodgorbek.fieldai.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(WebSockets) {
                // WebSocket configuration
            }
        }
    }
    
    single<org.yodgorbek.fieldai.shared.domain.ai.AiProvider> {
        org.yodgorbek.fieldai.shared.data.ai.GroqAiProvider(get())
    }
    
    single<org.yodgorbek.fieldai.shared.domain.realtime.StreamingClient> {
        org.yodgorbek.fieldai.shared.data.realtime.WebSocketStreamingClient(get())
    }
    
    single {
        org.yodgorbek.fieldai.shared.network.FieldAiApi(get())
    }
}
