# FieldAI Network & API Documentation

This document outlines the API integrations and data flow used within FieldAI.

## 1. Groq AI Integration (LLM)

FieldAI relies on Groq for rapid, low-latency AI responses required in the field.

- **Base URL:** `https://api.groq.com/openai/v1`
- **Endpoint:** `POST /chat/completions`
- **Model:** `llama-3.1-70b-versatile` (or equivalent high-speed model)
- **Protocol:** HTTP with Server-Sent Events (SSE) for streaming

### Authentication
A bearer token is required. This is loaded via the `BuildConfiguration.GROQ_API_KEY`, secured locally in `local.properties`.

### Request Payload Example
```json
{
  "model": "llama-3.1-70b-versatile",
  "messages": [
    {"role": "system", "content": "You are a specialized field assistant for engineers."},
    {"role": "user", "content": "What is the pressure limit for Valve 4?"}
  ],
  "stream": true,
  "temperature": 0.2
}
```

### SSE Streaming Response
The `GroqAiProvider` consumes chunks as they arrive.
```json
data: {"id":"chatcmpl-123","object":"chat.completion.chunk","choices":[{"delta":{"content":"The"},"finish_reason":null}]}
```

## 2. Sync Engine (Internal Backend Simulator)

The `SyncEngine` interacts with an internal API via `FieldAiApi` to push `PendingOperations`.

- **Base URL:** `https://api.fieldai.com/v1/`
- **Endpoints:**
    - `POST /sync/push` - Pushes a list of pending operations (mutations).
    - `GET /sync/pull` - Pulls any operations the server processed since a given timestamp.
- **WebSocket / Real-time:** `wss://api.fieldai.com/ws`

### Conflict Resolution
Data synchronizes based on `updatedAt` timestamps. FieldAI employs an **offline-first** strategy, meaning local changes always execute immediately, are queued in `PendingOperations`, and are then dispatched when online.
